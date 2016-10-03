package org.traktion0.safenet.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 * Created by paul on 05/09/16.
 */
public class SafenetFileSystem extends FileSystem {
    private static final String SEPARATOR = "/";

    private final FileSystemProvider provider;
    private final URI uri;
    private final FileStore fileStore;

    private boolean isOpen;

    public SafenetFileSystem(FileSystemProvider provider, URI uri) {
        this.provider = provider;
        this.uri = uri;
        fileStore = new SafenetFileStore(uri);
        this.isOpen = true;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    @Override
    public Path getPath(String s, String... strings) {
        String fullPath = s;
        for (String pathPart: strings) {
            fullPath += getSeparator() + pathPart;
        }

        URI uri = URI.create(fullPath);
        return new SafenetPath(this, uri);
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
