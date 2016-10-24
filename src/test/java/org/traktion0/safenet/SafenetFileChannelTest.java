package org.traktion0.safenet;

import org.junit.Test;
import org.traktion0.safenet.client.commands.SafenetFactory;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;
import org.traktion0.safenet.filesystem.SafenetPath;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by paul on 16/10/16.
 */
public class SafenetFileChannelTest {

    private static final String URI_HOST_STRING = "safe://localhost/";

    @Test
    public void testNewFileChannelSizeReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileAttributesReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        long fileSize;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("file.txt")),
                    options
            );

            fileSize = fileChannel.size();
        }

        verify(safenetFactory, times(1)).makeGetFileAttributesCommand(anyString());
        verify(safenetFactory.makeGetFileAttributesCommand(anyString()), times(1)).execute();

        assertEquals("Size of file channel mismatch", 3067, fileSize);
    }

    @Test
    public void testReadFromPositionReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsTextSuccess(49, 48);
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf = ByteBuffer.allocate(48);
        int readLength = 0;
        String readContent;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("file.txt")),
                    options
            );

            readLength = fileChannel.read(buf, 49);
            readContent = new String(buf.array(), 0, readLength);
        }

        verify(safenetFactory, times(1)).makeGetFileCommand(anyString(), anyLong(), anyLong());
        verify(safenetFactory.makeGetFileCommand(anyString(), anyLong(), anyLong()), times(1)).execute();

        assertEquals("g elit, sed do eiusmod temporincididunt ut labor", readContent);
    }

    @Test
    public void testReadBuffersFromPositionThreePartialBuffersReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsTextSuccess();
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf1 = ByteBuffer.allocate(48);
        ByteBuffer buf2 = ByteBuffer.allocate(48);
        ByteBuffer buf3 = ByteBuffer.allocate(48);
        ByteBuffer[] byteBuffers = {buf1, buf2, buf3};

        long readLength;
        String readContent = "";
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("file.txt")),
                    options
            );

            readLength = fileChannel.read(byteBuffers, 0, 3);
            for (ByteBuffer buf: byteBuffers) {
                readContent += new String(buf.array(), 0, buf.position());
            }
        }

        verify(safenetFactory, times(3)).makeGetFileCommand(anyString(), anyLong(), anyLong());
        verify(safenetFactory.makeGetFileCommand(anyString(), anyLong(), anyLong()), times(3)).execute();

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor" +
                "incididunt ut labore et dolore magna aliqua.";
        assertEquals(expected, readContent);
        assertEquals(122, readLength);
    }

    @Test
    public void testReadBuffersFromPositionOneExactBufferReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsTextSuccess();
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf1 = ByteBuffer.allocate(48);
        ByteBuffer buf2 = ByteBuffer.allocate(48);
        ByteBuffer buf3 = ByteBuffer.allocate(48);
        ByteBuffer[] byteBuffers = {buf1, buf2, buf3};

        long readLength;
        String readContent = "";
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("file.txt")),
                    options
            );

            readLength = fileChannel.read(byteBuffers, 0, 1);
            for (ByteBuffer buf: byteBuffers) {
                readContent += new String(buf.array(), 0, buf.position());
            }
        }

        verify(safenetFactory, times(1)).makeGetFileCommand(anyString(), anyLong(), anyLong());
        verify(safenetFactory.makeGetFileCommand(anyString(), anyLong(), anyLong()), times(1)).execute();

        assertEquals("Lorem ipsum dolor sit amet, consectetur adipisci", readContent);
        assertEquals(48, readLength);
    }

    @Test
    public void testReadBuffersFromPositionThreeBuffersReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsTextSuccess();
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf1 = ByteBuffer.allocate(8);
        ByteBuffer buf2 = ByteBuffer.allocate(8);
        ByteBuffer buf3 = ByteBuffer.allocate(8);
        ByteBuffer[] byteBuffers = {buf1, buf2, buf3};

        long readLength;
        String readContent = "";
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("file.txt")),
                    options
            );

            readLength = fileChannel.read(byteBuffers, 0, 3);
            for (ByteBuffer buf: byteBuffers) {
                readContent += new String(buf.array(), 0, buf.position());
            }
        }

        verify(safenetFactory, times(3)).makeGetFileCommand(anyString(), anyLong(), anyLong());
        verify(safenetFactory.makeGetFileCommand(anyString(), anyLong(), anyLong()), times(3)).execute();

        assertEquals("Lorem ipsum dolor sit am", readContent);
        assertEquals(24, readLength);
    }
}
