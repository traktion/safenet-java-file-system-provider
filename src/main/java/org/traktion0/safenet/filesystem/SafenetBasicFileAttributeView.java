package org.traktion0.safenet.filesystem;

import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.beans.SafenetFile;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;

/**
 * Created by paul on 22/10/16.
 */
public class SafenetBasicFileAttributeView implements BasicFileAttributeView {

    private SafenetFile safenetFile;
    private SafenetDirectory safenetDirectory;

    public SafenetBasicFileAttributeView(SafenetFile safenetFile) {
        this.safenetFile = safenetFile;
    }

    public SafenetBasicFileAttributeView(SafenetDirectory safenetDirectory) {
        this.safenetDirectory = safenetDirectory;
    }

    @Override
    public String name() {
        return "basic";
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        if (safenetFile != null) {
            return new SafenetBasicFileAttributes(safenetFile);
        } else if (safenetDirectory != null) {
            return new SafenetBasicFileAttributes(safenetDirectory);
        }
        return null;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime,
                         FileTime lastAccessTime,
                         FileTime createTime) throws IOException {
        if (safenetFile != null) {
            safenetFile.setLastModified(OffsetDateTime.parse(lastAccessTime.toString()));
            safenetFile.setCreatedOn(OffsetDateTime.parse(createTime.toString()));
        } else if (safenetDirectory != null) {
            safenetDirectory.getInfo().setCreatedOn(lastAccessTime.toMillis());
            safenetDirectory.getInfo().setCreatedOn(createTime.toMillis());
        }
    }
}
