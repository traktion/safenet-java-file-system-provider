package org.traktion0.safenet.filesystem;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

/**
 * Created by paul on 05/09/16.
 */
public class SafenetPath implements Path {

    private final FileSystem fileSystem;
    private final URI uri;

    public SafenetPath(FileSystem safenetFs, URI uri)
    {
        this.fileSystem = safenetFs;
        this.uri = uri;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return toString().substring(0,1).equals(fileSystem.getSeparator());
    }

    @Override
    public Path getRoot() {
        return new SafenetPath(fileSystem, URI.create(fileSystem.getSeparator()));
    }

    @Override
    public Path getFileName() {
        String pathString = uri.getPath();
        URI fileUri;
        if (pathString.indexOf(fileSystem.getSeparator()) != -1) {
            fileUri = URI.create(pathString.substring(pathString.lastIndexOf(fileSystem.getSeparator())));
        } else {
            fileUri = URI.create(pathString);
        }

        return new SafenetPath(fileSystem, fileUri);
    }

    @Override
    public Path getParent() {
        String pathString = StringUtils.stripEnd(uri.getPath(), fileSystem.getSeparator());
        int toPos = pathString.lastIndexOf(fileSystem.getSeparator());

        if (toPos != -1 && toPos != 0) {
            URI fileUri = URI.create(pathString.substring(0, toPos));
            return new SafenetPath(fileSystem, fileUri);
        }

        return null;
    }

    @Override
    public int getNameCount() {
        return getNameParts().length;
    }

    private String[] getNameParts() {
        return StringUtils.split(uri.getPath(), fileSystem.getSeparator());
    }

    @Override
    public Path getName(int i) {
        int nameCount = getNameCount();
        if (i < 0 || i > nameCount) throw new IllegalArgumentException("getName index out of range: " + nameCount);

        String[] nameParts = getNameParts();
        URI nameUri = URI.create(nameParts[i]);

        return new SafenetPath(fileSystem, nameUri);
    }

    @Override
    public Path subpath(int fromIndex, int toIndex) {
        int nameCount = getNameCount();
        if (fromIndex < 0 || fromIndex > nameCount) throw new IllegalArgumentException("getName from index out of range: " + fromIndex);
        if (toIndex < 0 || toIndex > nameCount) throw new IllegalArgumentException("getName to index out of range: " + toIndex);
        if (fromIndex > toIndex) throw new IllegalArgumentException("getName indexes out of order: " + fromIndex + " > " + toIndex);

        String[] nameParts = getNameParts();
        String subpathFragment = "";
        for (int i=fromIndex; i<toIndex; i++) {
            subpathFragment += nameParts[i] + fileSystem.getSeparator();
        }
        subpathFragment = StringUtils.stripEnd(subpathFragment, fileSystem.getSeparator());
        URI subpathUri = URI.create(subpathFragment);

        return new SafenetPath(fileSystem, subpathUri);
    }

    @Override
    public boolean startsWith(Path path) {
        return toString().startsWith(path.toString());
    }

    @Override
    public boolean startsWith(String s) {
        return toString().startsWith(s);
    }

    @Override
    public boolean endsWith(Path path) {
        return toString().endsWith(path.toString());
    }

    @Override
    public boolean endsWith(String s) {
        return toString().endsWith(s);
    }

    @Override
    public Path normalize() {
        String[] nameParts = getNameParts();
        Deque<String> newNameParts = new ArrayDeque<>();
        for (String part: nameParts) {
            if (part.equals("..") && newNameParts.size() > 0) {
                newNameParts.removeLast();
            } else {
                newNameParts.add(part);
            }
        }

        String separator = fileSystem.getSeparator();
        String pathFragment = "";
        for (String part: newNameParts) {
            if (pathFragment.length() > 0 || isAbsolute()) {
                pathFragment += separator;
            }
            pathFragment += part;
        }

        // PG: Remove superfluous "./" strings
        pathFragment = StringUtils.replace(pathFragment, "." + separator, "");

        URI normalizedUri = URI.create(pathFragment);

        return new SafenetPath(fileSystem, normalizedUri);
    }

    @Override
    public Path resolve(Path otherPath) {
        if (otherPath.isAbsolute()) return otherPath.normalize();

        return new SafenetPath(fileSystem, URI.create(uri.getPath() + fileSystem.getSeparator() + otherPath.toString())).normalize();
    }

    @Override
    public Path resolve(String otherPathString) {
        Path otherPath = new SafenetPath(fileSystem, URI.create(otherPathString));

        return resolve(otherPath);
    }

    @Override
    public Path resolveSibling(Path otherPath) {
        Path parent = getParent();
        if (otherPath.isAbsolute() || parent == null) return otherPath.normalize();

        return new SafenetPath(fileSystem, URI.create(parent.toString() + fileSystem.getSeparator() + otherPath.toString())).normalize();
    }

    @Override
    public Path resolveSibling(String otherPathString) {
        Path otherPath = new SafenetPath(fileSystem, URI.create(otherPathString));

        return resolveSibling(otherPath);
    }

    @Override
    public Path relativize(Path otherPath) {
        if (isAbsolute() && !otherPath.isAbsolute()
                || !isAbsolute() && otherPath.isAbsolute() ) {
            throw new IllegalArgumentException("Other path is not a Path that can be relativized against this path");
        }

        String firstString = normalize().toString();
        String secondString = otherPath.normalize().toString();

        if (secondString.length() < firstString.length()) {
            String tempString = secondString;
            secondString = firstString;
            firstString = tempString;
        }

        String differentPart = StringUtils.difference(firstString, secondString).substring(1);

        return new SafenetPath(fileSystem, URI.create(differentPart));
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        } else {
            // PG:ASSERT: There is no default directory - assume everything is relative to root
            return new SafenetPath(fileSystem, URI.create(fileSystem.getSeparator() + toString())).normalize();
        }
    }

    @Override
    public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return toAbsolutePath().normalize();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>... kinds) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator()
    {
        return new Iterator<Path>() {
            private final String[] pathParts = getNameParts();
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < getNameCount();
            }

            @Override
            public Path next() {
                URI uri = URI.create(pathParts[pos]);
                pos++;
                return new SafenetPath(fileSystem, uri);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int compareTo(Path path) {
        return toString().compareTo(path.toString());
    }

    @Override
    public String toString() {
        return uri.getPath();
    }
}

