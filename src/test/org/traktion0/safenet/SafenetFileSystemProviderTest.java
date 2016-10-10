package org.traktion0.safenet;

import org.junit.Test;
import org.traktion0.safenet.client.commands.SafenetFactory;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;
import org.traktion0.safenet.filesystem.SafenetPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by paul on 25/09/16.
 */
public class SafenetFileSystemProviderTest {

    private static final String URI_HOST_STRING = "safe://localhost/";

    @Test
    public void testNewFileSystemFromFileSystems() throws IOException {
        Map<String, Object> env = new HashMap<>();
        env.put("SafenetFactory", SafenetMockFactory.makeBasicSafenetFactoryMock());

        URI uri = URI.create(URI_HOST_STRING);
        String className;
        boolean isOpen;

        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            className = fileSystem.getClass().getName();
            isOpen = fileSystem.isOpen();
        }

        assertEquals("Provider return unexected type", "org.traktion0.safenet.filesystem.SafenetFileSystem", className);
        assertTrue("Provider return a closed filesystem", isOpen);
    }

    @Test
    public void testGetFileSystemFromFileSystems() throws IOException {
        Map<String, Object> env = new HashMap<>();
        env.put("SafenetFactory", SafenetMockFactory.makeBasicSafenetFactoryMock());

        URI uri = URI.create(URI_HOST_STRING);
        String className;
        boolean isOpen;

        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            FileSystem existingFileSystem = FileSystems.getFileSystem(uri);
            className = existingFileSystem.getClass().getName();
            isOpen = fileSystem.isOpen();
        }

        assertEquals("Provider return unexected type", "org.traktion0.safenet.filesystem.SafenetFileSystem", className);
        assertTrue("Provider return a closed filesystem", isOpen);
    }

    @Test
    public void testGetPathFromURI() throws IOException {
        Map<String, Object> env = new HashMap<>();
        env.put("SafenetFactory", SafenetMockFactory.makeBasicSafenetFactoryMock());

        URI uri = URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt");
        Path path;

        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(uri, env)) {
            path = provider.getPath(uri);
        }

        assertEquals("URI from Path mismatches original URI", uri.getPath(), path.toUri().getPath());
    }

    @Test
    public void testCreateDirectorySuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithCreateDirectoryReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            Path path = new SafenetPath(fileSystem, URI.create("app/filesystemdir"));
            provider.createDirectory(path);
        }

        verify(safenetFactory, times(1)).makeCreateDirectoryCommand(anyString());
        verify(safenetFactory.makeCreateDirectoryCommand(anyString()), times(1)).execute();
    }

    @Test(expected = IOException.class)
    public void testCreateDirectoryFailure() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithCreateDirectoryThrowsException();
        env.put("SafenetFactory", safenetFactory);

        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            Path path = new SafenetPath(fileSystem, URI.create("app/filesystemdir"));
            provider.createDirectory(path);
        }
    }

    @Test
    public void testReadBasicFileAttributesForFileReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileAttributesReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        BasicFileAttributes basicFileAttributes;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            basicFileAttributes = provider.readAttributes(
                    new SafenetPath(fileSystem, URI.create("app/file.txt")),
                    BasicFileAttributes.class
            );
        }

        verify(safenetFactory, times(1)).makeGetFileAttributesCommand(anyString());
        verify(safenetFactory.makeGetFileAttributesCommand(anyString()), times(1)).execute();

        assertEquals(FileTime.fromMillis(Instant.parse("2016-10-04T09:34:44.523Z").toEpochMilli()), basicFileAttributes.creationTime());
        assertEquals(FileTime.fromMillis(Instant.parse("2016-10-05T10:24:24.123Z").toEpochMilli()), basicFileAttributes.lastModifiedTime());
        assertEquals(true, basicFileAttributes.isRegularFile());
        assertEquals(false, basicFileAttributes.isDirectory());
        assertEquals(false, basicFileAttributes.isOther());
        assertEquals(false, basicFileAttributes.isSymbolicLink());
        assertEquals(3067, basicFileAttributes.size());
    }

    @Test
    public void testReadBasicFileAttributesForDirectoryReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetDirectoryReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        BasicFileAttributes basicFileAttributes;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            basicFileAttributes = provider.readAttributes(
                    new SafenetPath(fileSystem, URI.create("app/directory")),
                    BasicFileAttributes.class
            );
        }

        verify(safenetFactory, times(1)).makeGetDirectoryCommand(anyString());
        verify(safenetFactory.makeGetDirectoryCommand(anyString()), times(1)).execute();

        assertEquals(FileTime.from(1475701221, TimeUnit.SECONDS), basicFileAttributes.lastModifiedTime());
        assertEquals(FileTime.from(1475701203, TimeUnit.SECONDS), basicFileAttributes.creationTime());
        assertEquals(false, basicFileAttributes.isRegularFile());
        assertEquals(true, basicFileAttributes.isDirectory());
        assertEquals(false, basicFileAttributes.isOther());
        assertEquals(false, basicFileAttributes.isSymbolicLink());
        assertEquals(0, basicFileAttributes.size());
    }

    @Test
    public void testNewFileChannelReadTextFileReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsTextSuccess();
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf = ByteBuffer.allocate(48);
        int readLength = 0;
        String firstRead, secondRead, thirdRead;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("app/file.txt")),
                    options
            );

            readLength = fileChannel.read(buf);
            firstRead = new String(buf.array(), 0, readLength);

            readLength = fileChannel.read(buf);
            secondRead = new String(buf.array(), 0, readLength);

            readLength = fileChannel.read(buf);
            thirdRead = new String(buf.array(), 0, readLength);
        }

        verify(safenetFactory, times(3)).makeGetFileCommand(anyString());
        verify(safenetFactory.makeGetFileCommand(anyString()), times(3)).execute();

        assertEquals("Lorem ipsum dolor sit amet, consectetur adipisci", firstRead);
        assertEquals("ng elit, sed do eiusmod temporincididunt ut labo", secondRead);
        assertEquals("re et dolore magna aliqua.", thirdRead);
    }

    @Test
    public void testNewFileChannelReadImageFileReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetFileReturnsImageSuccess();
        env.put("SafenetFactory", safenetFactory);

        ByteBuffer buf = ByteBuffer.allocate(200000);
        int readLength = 0;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.READ);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("app/image.jpg")),
                    options
            );

            readLength = fileChannel.read(buf);
        }

        verify(safenetFactory, times(1)).makeGetFileCommand(anyString());
        verify(safenetFactory.makeGetFileCommand(anyString()), times(1)).execute();

        assertEquals(167924, readLength);
    }

    @Test
    public void testNewFileChannelWriteTextFileReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithCreateFileReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        String fileContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor" +
                "incididunt ut labore et dolore magna aliqua.";

        int writeLength = 0;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("app/file.txt")),
                    options
            );

            ByteBuffer byteBuffer = ByteBuffer.wrap(fileContent.getBytes());
            writeLength = fileChannel.write(byteBuffer);
        }

        verify(safenetFactory, times(1)).makeCreateFileCommand(anyString(), any(byte[].class));
        verify(safenetFactory.makeCreateFileCommand(anyString(), any(byte[].class)), times(1)).execute();

        assertEquals(122, writeLength);
    }

    @Test
    public void testNewFileChannelWriteImageFileReturnsSuccess() throws IOException {
        Map<String, Object> env = new HashMap<>();
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithCreateFileReturnsSuccess();
        env.put("SafenetFactory", safenetFactory);

        byte imageFileContent[];
        File imageFile = new File("src/test/resources/maidsafe_layered_haze.jpg");
        try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
            imageFileContent = new byte[(int)imageFile.length()];
            fileInputStream.read(imageFileContent);
        } catch (IOException e) {
            throw new IOException(e);
        }

        int writeLength = 0;
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        try (FileSystem fileSystem = provider.newFileSystem(URI.create(URI_HOST_STRING), env)) {
            HashSet<StandardOpenOption> options = new HashSet<>();
            options.add(StandardOpenOption.WRITE);
            FileChannel fileChannel = provider.newFileChannel(
                    new SafenetPath(fileSystem, URI.create("app/image.jpg")),
                    options
            );

            ByteBuffer byteBuffer = ByteBuffer.wrap(imageFileContent);
            writeLength = fileChannel.write(byteBuffer);
        }

        verify(safenetFactory, times(1)).makeCreateFileCommand(anyString(), any(byte[].class));
        verify(safenetFactory.makeCreateFileCommand(anyString(), any(byte[].class)), times(1)).execute();

        assertEquals(167924, writeLength);
    }
}
