package org.traktion0.safenet.filesystem;

import org.apache.commons.io.FilenameUtils;
import org.traktion0.safenet.client.beans.Info;
import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.commands.SafenetFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by paul on 05/09/16.
 */
public class SafenetFileSystem extends FileSystem {
    private static final String SEPARATOR = "/";

    private final FileSystemProvider provider;
    private final URI uri;
    private final FileStore fileStore;
    private final SafenetFactory safenetFactory;

    private boolean isOpen;

    public SafenetFileSystem(FileSystemProvider provider, URI uri, SafenetFactory safenetFactory) {
        this.provider = provider;
        this.uri = uri;
        fileStore = new SafenetFileStore(uri);
        this.isOpen = true;
        this.safenetFactory = safenetFactory;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        if (safenetFactory != null) {
            safenetFactory.makeDeleteAuthTokenCommand().execute();
        }
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
        String path = uri.getPath();
        if (path.equals("")) path = getSeparator();

        SafenetDirectory rootDirectory = safenetFactory.makeGetDirectoryCommand(path).execute();
        List<Info> subDirectories = rootDirectory.getSubDirectories();
        FileSystem fileSystem = this;

        return () -> new Iterator<Path>() {
            private int pos = 0;
            @Override
            public boolean hasNext() {
                return pos < subDirectories.size();
            }

            @Override
            public Path next() {
                if (pos >= subDirectories.size()) {
                    throw new NoSuchElementException();
                }
                return new SafenetPath(fileSystem, URI.create(subDirectories.get(pos++).getName()));
            }
        };
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return () -> new Iterator<FileStore>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < 1;
            }

            @Override
            public FileStore next() {
                if (pos >= 1) {
                    throw new NoSuchElementException();
                }
                pos++;
                return fileStore;
            }
        };
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

        URI pathUri = URI.create(fullPath);
        return new SafenetPath(this, pathUri);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        if (!syntaxAndPattern.contains(":")) throw new IllegalArgumentException("Syntax/pattern string must contain ':' delimiter.");

        String syntaxStr = syntaxAndPattern.substring(0, syntaxAndPattern.indexOf(":"));
        String patternStr = syntaxAndPattern.substring(syntaxAndPattern.indexOf(":")+1);

        return path -> {
            if (syntaxStr.equals("regex")) {
                Pattern pattern = Pattern.compile(patternStr);
                return pattern.matcher(path.toString()).matches();
            } else if (syntaxStr.equals("glob")) {
                return FilenameUtils.wildcardMatch(path.toString(), patternStr);
            }
            throw new IllegalArgumentException("Invalid syntax - regex or glob required.");
        };
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }


}
