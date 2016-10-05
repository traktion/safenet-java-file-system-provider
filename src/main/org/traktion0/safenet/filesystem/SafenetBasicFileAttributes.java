package org.traktion0.safenet.filesystem;

import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.beans.SafenetFile;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Created by paul on 05/10/16.
 */
public class SafenetBasicFileAttributes implements BasicFileAttributes {
    private final FileTime lastModifiedTime;
    private final FileTime lastAccessTime;
    private final FileTime creationTime;
    private final long size;
    private final boolean isRegularFile;
    private final boolean isDirectory;
    private final boolean isSymbolicLink;
    private final boolean isOther;

    public SafenetBasicFileAttributes(SafenetFile safenetFile) {
        lastModifiedTime = FileTime.fromMillis(Instant.parse(safenetFile.getLastModified().toString()).toEpochMilli());
        lastAccessTime = lastModifiedTime;
        creationTime = FileTime.fromMillis(Instant.parse(safenetFile.getCreatedOn().toString()).toEpochMilli());
        size = safenetFile.getContentLength();
        isRegularFile = true;
        isDirectory = false;
        isSymbolicLink = false;
        isOther = false;
    }

    public SafenetBasicFileAttributes(SafenetDirectory safenetDirectory) {
        lastModifiedTime = FileTime.from(safenetDirectory.getInfo().getModifiedOn(), TimeUnit.SECONDS);
        lastAccessTime = lastModifiedTime;
        creationTime = FileTime.from(safenetDirectory.getInfo().getCreatedOn(), TimeUnit.SECONDS);
        size = 0;
        isRegularFile = false;
        isDirectory = true;
        isSymbolicLink = false;
        isOther = false;
    }

    @Override
    public FileTime lastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public FileTime lastAccessTime() {
        return null;
    }

    @Override
    public FileTime creationTime() {
        return creationTime;
    }

    @Override
    public boolean isRegularFile() {
        return isRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return isSymbolicLink;
    }

    @Override
    public boolean isOther() {
        return isOther;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public Object fileKey() {
        return null;
    }
}
