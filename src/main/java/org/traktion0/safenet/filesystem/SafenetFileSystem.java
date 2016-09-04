package java.org.traktion0.safenet.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

/**
 * Created by paul on 05/09/16.
 */
public class SafenetFileSystem extends FileSystem {
    private final SafenetFileSystemProvider provider;
    private final URI uri;
    private final FileStore fileStore;

    public SafenetFileSystem(SafenetFileSystemProvider provider, URI uri) {
        this.provider = provider;
        this.uri = uri;
        fileStore = new SafenetFileStore(uri);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Collections.singletonList(fileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.singleton("basic");
    }

    @Override
    public Path getPath(String s, String... strings) {
        return null;
    }

    @Override
    public PathMatcher getPathMatcher(String s) {
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }
}
