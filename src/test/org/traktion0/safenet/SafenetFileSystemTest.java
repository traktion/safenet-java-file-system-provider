package org.traktion0.safenet;

import org.junit.Test;
import org.traktion0.safenet.client.commands.SafenetFactory;
import org.traktion0.safenet.filesystem.SafenetFileSystem;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by paul on 09/10/16.
 */
public class SafenetFileSystemTest {

    @Test
    public void testGetFileStoresReturnsSuccess() throws IOException {
        SafenetFactory safenetFactory = SafenetMockFactory.makeBasicSafenetFactoryMock();
        SafenetFileSystemProvider provider = mock(SafenetFileSystemProvider.class);

        String storeNames = "";
        SafenetFileSystem safenetFileSystem = new SafenetFileSystem(provider, URI.create("safe://localhost"), safenetFactory);
        for (FileStore fileStore: safenetFileSystem.getFileStores()) {
            storeNames += fileStore.name();
        }

        assertEquals("FileStore names mismatch", "safe://localhost", storeNames);
    }

    @Test
    public void testGetRootDirectoriesReturnsSuccess() throws IOException {
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetDirectoryReturnsRootDirectories();
        SafenetFileSystemProvider provider = mock(SafenetFileSystemProvider.class);

        String rootDirectories = "";
        SafenetFileSystem safenetFileSystem = new SafenetFileSystem(provider, URI.create("safe://localhost"), safenetFactory);
        for (Path path: safenetFileSystem.getRootDirectories()) {
            rootDirectories += path.toString() + ":";
        }

        assertEquals("Root directory names mismatch", "app:drive:", rootDirectories);
    }

    @Test
    public void testGetPathMatcherWithGlobMatch() {
        String matches = getPathMatcher("glob:*pp");

        assertEquals("Matcher mismatch", "app:", matches);
    }

    @Test
    public void testGetPathMatcherWithGlobMismatch() {
        String matches = getPathMatcher("glob:p*");

        assertEquals("Matcher mismatch", "", matches);
    }

    @Test
    public void testGetPathMatcherWithRegexMatch() {
        String matches = getPathMatcher("regex:a[p]+");

        assertEquals("Matcher mismatch", "app:", matches);
    }

    @Test
    public void testGetPathMatcherWithRegexMismatch() {
        String matches = getPathMatcher("regex:p*");

        assertEquals("Matcher mismatch", "", matches);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPathMatcherWithBadMatchStringNoDelimiter() {
        getPathMatcher("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPathMatcherWithBadMatchStringInvalidSyntax() {
        getPathMatcher("invalid:a*");
    }

    private String getPathMatcher(String syntaxPattern) {
        SafenetFactory safenetFactory = SafenetMockFactory.makeSafenetFactoryMockWithGetDirectoryReturnsRootDirectories();
        SafenetFileSystemProvider provider = mock(SafenetFileSystemProvider.class);

        String matches = "";
        SafenetFileSystem safenetFileSystem = new SafenetFileSystem(provider, URI.create("safe://localhost"), safenetFactory);
        PathMatcher pathMatcher = safenetFileSystem.getPathMatcher(syntaxPattern);
        for (Path path: safenetFileSystem.getRootDirectories()) {
            if (pathMatcher.matches(path)) matches += path.toString() + ":";
        }

        return matches;
    }
}
