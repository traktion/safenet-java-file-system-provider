package org.traktion0.safenet.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

/**
 * Created by paul on 05/09/16.
 */
public class SafenetFileStore extends FileStore {

    private final URI uri;

    public SafenetFileStore(URI uri) {
        this.uri = uri;
    }

    @Override
    public String name() {
        return uri.getScheme() + "://" + uri.getHost();
    }

    @Override
    public String type() {
        return "safe";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> aClass) {
        return false;
    }

    @Override
    public boolean supportsFileAttributeView(String s) {
        return false;
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> aClass) {
        return null;
    }

    @Override
    public Object getAttribute(String s) throws IOException {
        return null;
    }


}
