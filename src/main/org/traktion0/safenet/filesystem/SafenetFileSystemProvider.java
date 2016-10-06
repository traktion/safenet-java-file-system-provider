package org.traktion0.safenet.filesystem;

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
import com.netflix.hystrix.exception.HystrixRuntimeException;

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
        URI uriHost = getUriHostOnly(uri);

        synchronized (fileSystems) {
            if (fileSystems.containsKey(uriHost) && fileSystems.get(uriHost).isOpen()) {
                throw new FileSystemAlreadyExistsException();
            }

            if (map.containsKey("SafenetFactory")) {
                safenetFactory = (SafenetFactory) map.get("SafenetFactory");
            } else {
                throw new IOException("Required SafenetFactory not provided.");
            }

            FileSystem fs = new SafenetFileSystem(this, uriHost, safenetFactory);
            fileSystems.put(uriHost, fs);

            return fs;
        }
    }

    private URI getUriHostOnly(URI uri) {
        return URI.create(uri.getScheme() + "://" + uri.getHost());
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        URI uriHost = getUriHostOnly(uri);

        synchronized (fileSystems) {
            FileSystem fs = fileSystems.get(uriHost);
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
        return null;
        /*SafenetFileSystem fs = (SafenetFileSystem) path.getFileSystem();
        try (
                final FtpAgent agent = queue.getAgent();
        ) {
            final String absPath = path.toRealPath().toString();
            final Iterator<String> names
                    = agent.getDirectoryNames(absPath).iterator();
            return new DirectoryStream<Path>()
            {
                @Override
                public Iterator<Path> iterator()
                {
                    return new Iterator<Path>()
                    {
                        @Override
                        public boolean hasNext()
                        {
                            return names.hasNext();
                        }

                        @Override
                        public Path next()
                        {
                            if (!hasNext())
                                throw new NoSuchElementException();
                            return fs.getPath(names.next());
                        }

                        @Override
                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public void close()
                        throws IOException
                {
                }
            };
        }*/
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
