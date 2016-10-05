package org.traktion0.safenet;

import org.junit.Test;
import org.traktion0.safenet.client.commands.SafenetFactory;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;
import org.traktion0.safenet.filesystem.SafenetPath;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by paul on 25/09/16.
 */
public class SafenetFileSystemProviderTest {

    private static final String URI_HOST_STRING = "safe://traktion0/";

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
}
