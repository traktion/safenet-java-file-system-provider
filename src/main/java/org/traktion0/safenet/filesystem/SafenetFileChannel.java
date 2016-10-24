package org.traktion0.safenet.filesystem;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.traktion0.safenet.client.beans.SafenetFile;
import org.traktion0.safenet.client.commands.SafenetBadRequestException;
import org.traktion0.safenet.client.commands.SafenetFactory;

import java.io.IOException;
import java.io.InputStream;
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
        int bytesRead = read(byteBuffer, position);
        incrementPosition(bytesRead);
        return bytesRead;
    }

    private void incrementPosition(long increment) {
        position += increment;
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int offset, int length) throws IOException {
        long totalBytesRead = 0;
        int bytesRead;

        for (int i=offset; i<(offset + length); i++) {
            bytesRead = read(byteBuffers[i]);
            totalBytesRead += bytesRead;
            byteBuffers[i].position(bytesRead);
            incrementPosition(bytesRead);
        }

        return totalBytesRead;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        int bufferLength = byteBuffer.capacity();
        String pathString = path.normalize().toString();

        try {
            String message = safenetFactory.makeCreateFileCommand(pathString, byteBuffer.array()).execute();

            if (message.equals("ok")) {
                incrementPosition(bufferLength);
                byteBuffer.position(bufferLength);
                return bufferLength;
            } else {
                return 0;
            }
        } catch(HystrixRuntimeException | SafenetBadRequestException e) {
            throw new IOException("Create file '" + pathString + "' failed.", e);
        }
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int offset, int length) throws IOException {
        // PG:ASSERT: Combined byte buffers must be less than a the maximum ByteBuffer size (2GB)

        int totalBufferSize = 0;
        for (int i=offset; i<(offset + length); i++) {
            totalBufferSize += byteBuffers[i].array().length;
        }

        // PG: On totalBufferSize overflow, this will throw IllegalArgumentException or IndexOutOfBoundsException on put
        //     As SafenetCreateFile can only accept a single write, this needs a better approach.
        ByteBuffer combinedByteBuffer = ByteBuffer.allocate(totalBufferSize);
        for (int i=offset; i<(offset + length); i++) {
            combinedByteBuffer.put(byteBuffers[i]);
        }

        return write(combinedByteBuffer);
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
        String pathString = path.normalize().toString();

        try {
            SafenetFile safenetFile = safenetFactory.makeGetFileAttributesCommand(pathString).execute();
            return safenetFile.getContentLength();
        } catch(HystrixRuntimeException | SafenetBadRequestException e) {
            throw new IOException("Get file attributes for '" + pathString + "' failed.", e);
        }
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
    public int read(ByteBuffer byteBuffer, long fromPosition) throws IOException {
        int bufferLength = byteBuffer.capacity();
        //String pathString = path.normalize().toString() + String.format("?offset=%1$s&length=%2$s", fromPosition, bufferLength);
        String pathString = path.normalize().toString();

        try {

            byte[] buf = new byte[bufferLength];

            SafenetFile safenetFile = safenetFactory.makeGetFileCommand(pathString, fromPosition, bufferLength).execute();
            InputStream inputStream = safenetFile.getInputStream();

            int bytesRead = inputStream.read(buf);
            byteBuffer.clear();
            byteBuffer.put(buf);
            inputStream.close();

            return bytesRead;
        } catch(HystrixRuntimeException | SafenetBadRequestException e) {
            throw new IOException("Get file '" + pathString + "' failed.", e);
        }
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
