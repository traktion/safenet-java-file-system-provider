package org.traktion0.safenet.filesystem;

import org.traktion0.safenet.client.beans.SafenetFile;
import org.traktion0.safenet.client.commands.SafenetFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * Created by paul on 06/10/16.
 */
public class SafenetFileChannel extends FileChannel {
    private final SafenetFactory safenetFactory;
    private final Path path;
    private final Set<? extends OpenOption> set;


    private long position;

    public SafenetFileChannel(SafenetFactory safenetFactory, Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) {
        this.safenetFactory = safenetFactory;
        this.path = path;
        this.set = set;
        this.position = 0;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        int bufferLength = byteBuffer.array().length;
        String pathString = path.normalize().toString() + String.format("?offset=%1$s&length=%2$s", position, bufferLength);
        SafenetFile safenetFile = safenetFactory.makeGetFileCommand(pathString).execute();

        byte[] buf = new byte[bufferLength];
        int bytesRead = safenetFile.getInputStream().read(buf);
        byteBuffer.clear();
        byteBuffer.put(buf);
        incrementPosition(bytesRead);

        return bytesRead;
    }

    private void incrementPosition(long increment) {
        position += increment;
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        int bufferLength = byteBuffer.array().length;
        String pathString = path.normalize().toString();

        String message = safenetFactory.makeCreateFileCommand(pathString, byteBuffer.array()).execute();

        if (message.equals("ok")) {
            incrementPosition(bufferLength);
            return bufferLength;
        } else {
            return 0;
        }
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public FileChannel position(long l) throws IOException {
        return null;
    }

    @Override
    public long size() throws IOException {
        return 0;
    }

    @Override
    public FileChannel truncate(long l) throws IOException {
        return null;
    }

    @Override
    public void force(boolean b) throws IOException {

    }

    @Override
    public long transferTo(long l, long l1, WritableByteChannel writableByteChannel) throws IOException {
        return 0;
    }

    @Override
    public long transferFrom(ReadableByteChannel readableByteChannel, long l, long l1) throws IOException {
        return 0;
    }

    @Override
    public int read(ByteBuffer byteBuffer, long l) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer byteBuffer, long l) throws IOException {
        return 0;
    }

    @Override
    public MappedByteBuffer map(MapMode mapMode, long l, long l1) throws IOException {
        return null;
    }

    @Override
    public FileLock lock(long l, long l1, boolean b) throws IOException {
        return null;
    }

    @Override
    public FileLock tryLock(long l, long l1, boolean b) throws IOException {
        return null;
    }

    @Override
    protected void implCloseChannel() throws IOException {

    }
}
