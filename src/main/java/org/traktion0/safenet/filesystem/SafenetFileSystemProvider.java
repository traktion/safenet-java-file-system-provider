package org.traktion0.safenet.filesystem;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.traktion0.safenet.client.beans.Info;
import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.beans.SafenetFile;
import org.traktion0.safenet.client.commands.SafenetBadRequestException;
import org.traktion0.safenet.client.commands.SafenetFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by paul on 04/09/16.
 */
public class SafenetFileSystemProvider extends FileSystemProvider {

    private final Map<URI, FileSystem> fileSystems = new HashMap<>();
    private SafenetFactory safenetFactory;

    @Override
    public String getScheme() {
        return "safe";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> map) throws IOException {

        synchronized (fileSystems) {
            if (fileSystems.containsKey(uri) && fileSystems.get(uri).isOpen()) {
                throw new FileSystemAlreadyExistsException();
            }

            if (map.containsKey("SafenetFactory")) {
                safenetFactory = (SafenetFactory) map.get("SafenetFactory");
            } else {
                throw new IOException("Required SafenetFactory not provided.");
            }

            FileSystem fs = new SafenetFileSystem(this, uri, safenetFactory);
            fileSystems.put(uri, fs);

            return fs;
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {

        synchronized (fileSystems) {
            FileSystem fs = fileSystems.get(uri);
            if (fs == null || !fs.isOpen())
                throw new FileSystemNotFoundException();

            return fs;
        }
    }

    @Override
    public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        SafenetFileSystem fs = (SafenetFileSystem) path.getFileSystem();
        SafenetDirectory safenetDirectory = safenetFactory.makeGetDirectoryCommand(path.toString()).execute();

        List<Path> filesAndSubdirectories = new ArrayList<>();
        for (Info info: safenetDirectory.getSubDirectories()){
            filesAndSubdirectories.add(resolveInfoToPath(path, info));
        }
        for (Info info: safenetDirectory.getFiles()){
            filesAndSubdirectories.add(resolveInfoToPath(path, info));
        }

        return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
                return new Iterator<Path>() {
                    private int pos = 0;
                    private Path nextPath;

                    @Override
                    public boolean hasNext() {
                        if (pos < filesAndSubdirectories.size()) {
                            nextPath = filesAndSubdirectories.get(pos);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public Path next() {
                        if (pos >= filesAndSubdirectories.size()) {
                            throw new NoSuchElementException();
                        }
                        pos++;
                        return nextPath;
                    }
                };
            }

            @Override
            public void close() throws IOException {
                // PG:TODO: This may need implementing
            }
        };
    }

    private Path resolveInfoToPath(Path path, Info info) {
        String pathString = path.resolve(info.getName()).toString();
        return new SafenetPath(path.getFileSystem(), URI.create(pathString));
    }

    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> map) throws IOException {
        return super.newFileSystem(path, map);
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... openOptions) throws IOException {
        return super.newInputStream(path, openOptions);
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... openOptions) throws IOException {
        return super.newOutputStream(path, openOptions);
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
        return new SafenetFileChannel(safenetFactory, path, set, fileAttributes);
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> set, ExecutorService executorService, FileAttribute<?>... fileAttributes) throws IOException {
        return super.newAsynchronousFileChannel(path, set, executorService, fileAttributes);
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {
        String pathString = path.normalize().toString();
        try {
            safenetFactory.makeCreateDirectoryCommand(pathString).execute();
        } catch(HystrixRuntimeException | SafenetBadRequestException e) {
            throw new IOException("Create directory '" + pathString + "' failed.", e);
        }
    }

    @Override
    public void delete(Path path) throws IOException {

    }

    @Override
    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public boolean isSameFile(Path path, Path path1) throws IOException {
        return false;
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        String pathString = path.normalize().toString();
        try {
            // PG:TODO: Factory this out
            try {
                if (aClass == BasicFileAttributes.class) {
                    SafenetFile safenetFile = safenetFactory.makeGetFileAttributesCommand(pathString).execute();
                    return (A) new SafenetBasicFileAttributes(safenetFile);
                } else {
                    return null;
                }
            } catch (SafenetBadRequestException e) {
                // PG: Currently, there is no way to get info on a file or a directory, so have to test for file first
                //     then fall back to test a directory
                if (aClass == BasicFileAttributes.class) {
                    SafenetDirectory safenetDirectory = safenetFactory.makeGetDirectoryCommand(pathString).execute();
                    return (A) new SafenetBasicFileAttributes(safenetDirectory);
                } else {
                    return null;
                }
            }
        } catch(HystrixRuntimeException | SafenetBadRequestException e) {
            throw new IOException("Get File/Directory Attributes '" + pathString + "' failed.", e);
        }
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }
}
