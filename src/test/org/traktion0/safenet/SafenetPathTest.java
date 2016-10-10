package org.traktion0.safenet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.traktion0.safenet.filesystem.SafenetFileSystemProvider;
import org.traktion0.safenet.filesystem.SafenetPath;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by paul on 23/09/16.
 */
public class SafenetPathTest {

    private static final String URI_HOST_STRING = "safe://localhost/";
    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> env = new HashMap<>();
        env.put("SafenetFactory", SafenetMockFactory.makeBasicSafenetFactoryMock());

        URI uri = URI.create(URI_HOST_STRING);
        SafenetFileSystemProvider provider = new SafenetFileSystemProvider();
        fileSystem = provider.newFileSystem(uri, env);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    /*
    // PG: This is failing as the FileSystemProvider cannot be found, despite being specified as a resource.
    @Test
    public void testCreatePathWithPaths() throws IOException {
        Path path = Paths.get(URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertTrue("Absolute path string not identified as absolute", path.isAbsolute());
    }*/

    @Test
    public void testPathIsAbsoluteWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertTrue("Absolute path string was not identified as absolute", path.isAbsolute());
    }

    @Test
    public void testPathIsAbsoluteWithoutAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertTrue("Relative path string was identified as absolute", !path.isAbsolute());
    }

    @Test
    public void testGetRootWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path rootPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING));

        assertEquals("Get root reports an invalid root", rootPath.toString(), path.getRoot().toString());
    }

    @Test
    public void testGetRootWithRelativePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path rootPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING));

        assertEquals("Get root reports an invalid root", rootPath.toString(), path.getRoot().toString());
    }

    @Test
    public void testGetParentWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path parentPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir"));

        assertEquals("Get parent reports an invalid name", parentPath.toString(), path.getParent().toString());
    }

    @Test
    public void testGetParentWithAbsoluteRootPath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING));

        assertEquals("Get parent with root path returns non-null value", null, path.getParent());
    }

    @Test
    public void testGetNameCountWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertEquals("Get name count returns an invalid number for an absolute path", 3, path.getNameCount());
    }

    @Test
    public void testGetNameCountWithRootPath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING));

        assertEquals("Get name count returns an invalid number for an absolute root path", 0, path.getNameCount());
    }

    @Test
    public void testGetNameCountWithSingleCharPath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "a"));

        assertEquals("Get name count returns an invalid number for a single char absolute path", 1, path.getNameCount());
    }

    @Test
    public void testGetNameCountWithRelativePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertEquals("Get name count returns an invalid number for a relative path", 3, path.getNameCount());
    }

    @Test
    public void testGetNameWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertEquals("Get name returns an invalid name for position 0", "testdir", path.getName(0).toString());
        assertEquals("Get name returns an invalid name for position 1", "testsubdir", path.getName(1).toString());
        assertEquals("Get name returns an invalid name for position 2", "testfile.txt", path.getName(2).toString());
    }

    @Test
    public void testGetNameWithRelativePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertEquals("Get name returns an invalid name for position 0", "testdir", path.getName(0).toString());
        assertEquals("Get name returns an invalid name for position 1", "testsubdir", path.getName(1).toString());
        assertEquals("Get name returns an invalid name for position 2", "testfile.txt", path.getName(2).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNameWithAbsolutePathAndNegativeIndexReturnException() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertTrue("Get name fails to throw an exception when out of bounds", path.getName(-1).isAbsolute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNameWithAbsolutePathAndOverrunIndexReturnException() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertTrue("Get name fails to throw an exception when out of bounds", path.getName(4).isAbsolute());
    }

    @Test
    public void testSubPathWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));

        assertEquals("Subpath returns an invalid path for position 0, 1", "testdir", path.subpath(0, 1).toString());
        assertEquals("Subpath returns an invalid path for position 0, 2", "testdir/testsubdir", path.subpath(0, 2).toString());
        assertEquals("Subpath returns an invalid path for position 0, 3", "testdir/testsubdir/testfile.txt", path.subpath(0, 3).toString());
        assertEquals("Subpath returns an invalid path for position 1, 3", "testsubdir/testfile.txt", path.subpath(1, 3).toString());
    }

    @Test
    public void testSubPathWithRelativePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertEquals("Subpath returns an invalid path for position 0, 1", "testdir", path.subpath(0, 1).toString());
        assertEquals("Subpath returns an invalid path for position 0, 1", "testdir/testsubdir", path.subpath(0, 2).toString());
        assertEquals("Subpath returns an invalid path for position 0, 1", "testdir/testsubdir/testfile.txt", path.subpath(0, 3).toString());
        assertEquals("Subpath returns an invalid path for position 1, 3", "testsubdir/testfile.txt", path.subpath(1, 3).toString());
    }

    @Test
    public void testStartsWithPathReturnsMatch() throws IOException {
        Path sourcePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path compareShortPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir"));
        Path compareLongPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/test"));
        Path sourceRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path compareRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir"));

        assertTrue("Starts with mismatches short path", sourcePath.startsWith(compareShortPath));
        assertTrue("Starts with mismatches long path", sourcePath.startsWith(compareLongPath));
        assertTrue("Starts with mismatches long path", sourceRelativePath.startsWith(compareRelativePath));
    }

    @Test
    public void testStartsWithPathReturnsMismatch() throws IOException {
        Path sourcePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path compareShortPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "differentdir"));
        Path compareLongPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "differentdir/differentsubdir/test"));
        Path sourceRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path compareRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir"));

        assertFalse("Starts with matches short path", sourcePath.startsWith(compareShortPath));
        assertFalse("Starts with matches long path", sourcePath.startsWith(compareLongPath));
        assertFalse("Starts with matches relative path", sourcePath.startsWith(compareRelativePath));
        assertFalse("Starts with mismatches long path", sourceRelativePath.startsWith(compareShortPath));
    }

    @Test
    public void testStartsWithStringReturnsMatch() throws IOException {
        Path absolutePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path relativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertTrue("Starts with string mismatches short path", absolutePath.startsWith("/testdir"));
        assertTrue("Starts with string mismatches long path", absolutePath.startsWith("/testdir/testsubdir/test"));
        assertTrue("Starts with string mismatches relative paths", relativePath.startsWith("testdir/testsubdir"));
    }

    @Test
    public void testStartsWithStringReturnsMismatch() throws IOException {
        Path absolutePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path relativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertFalse("Starts with string matches relative path", absolutePath.startsWith("testdir/testsubdir"));
        assertFalse("Starts with matches relative and absolute paths", relativePath.startsWith("/testdir/testsubdir"));
    }

    @Test
    public void testEndsWithPathReturnsMatch() throws IOException {
        Path sourcePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path compareShortPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testfile.txt"));
        Path sourceRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path compareRelativePath = new SafenetPath(fileSystem, URI.create("dir/testsubdir/testfile.txt"));

        assertTrue("Ends with absolute mismatches short path", sourcePath.endsWith(compareShortPath));
        assertTrue("Ends with absolute mismatches relative path", sourcePath.endsWith(compareRelativePath));
        assertTrue("Ends with relative mismatches relative path", sourceRelativePath.endsWith(compareRelativePath));
    }

    @Test
    public void testEndsWithPathReturnsMismatch() throws IOException {
        Path sourcePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path compareShortPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testfile"));
        Path compareLongPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "dir/testsub/testfile.txt"));
        Path sourceRelativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path compareRelativePath = new SafenetPath(fileSystem, URI.create("dir/testsubdir/file.txt"));

        assertFalse("Ends with matches short path", sourcePath.endsWith(compareShortPath));
        assertFalse("Ends with matches long path", sourcePath.endsWith(compareLongPath));
        assertFalse("Ends with matches relative path", sourcePath.endsWith(compareRelativePath));
        assertFalse("Ends with mismatches long path", sourceRelativePath.endsWith(compareShortPath));
    }

    @Test
    public void testEndsWithStringReturnsMatch() throws IOException {
        Path absolutePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path relativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertTrue("Ends with string mismatches short path", absolutePath.endsWith("testfile.txt"));
        assertTrue("Ends with string mismatches long path", absolutePath.endsWith("dir/testsubdir/testfile.txt"));
        assertTrue("Ends with string mismatches relative paths", relativePath.endsWith("testsubdir/testfile.txt"));
    }

    @Test
    public void testEndsWithStringReturnsMismatch() throws IOException {
        Path absolutePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path relativePath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));

        assertFalse("Ends with string matches relative path", absolutePath.endsWith("testdir/testsubdir"));
        assertFalse("Ends with matches relative and absolute paths", relativePath.endsWith("/testdir/testsubdir"));
    }

    @Test
    public void testNormalizeWithNormalizedPath() throws IOException {
        Path rawPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path normalizedPath = rawPath.normalize();

        assertEquals("Normalized path mismatches raw path", rawPath.toString(), normalizedPath.toString());
    }

    @Test
    public void testNormalizeWithSimpleNonNormalizedPath() throws IOException {
        Path rawPath = new SafenetPath(fileSystem, URI.create("testdir/testsubdir/testfile.txt"));
        Path normalizedPath = new SafenetPath(fileSystem, URI.create("./testdir/./testsubdir/testfile.txt")).normalize();

        assertEquals("Normalized path mismatches raw path", rawPath.toString(), normalizedPath.toString());
    }

    @Test
    public void testNormalizeWithComplexRelativeNonNormalizedPath() throws IOException {
        Path rawPath = new SafenetPath(fileSystem, URI.create("testsubdir/testfile.txt"));
        Path normalizedPath = new SafenetPath(fileSystem, URI.create("testdir/../testsubdir//testfile.txt")).normalize();

        assertEquals("Normalized path mismatches raw path", rawPath.toString(), normalizedPath.toString());
    }

    @Test
    public void testNormalizeWithComplexAbsoluteNonNormalizedPath() throws IOException {
        Path rawPath = new SafenetPath(fileSystem, URI.create("/testsubdir/testfile.txt"));
        Path normalizedPath = new SafenetPath(fileSystem, URI.create("/testdir/../testsubdir//testfile.txt")).normalize();

        assertEquals("Normalized path mismatches raw path", rawPath.toString(), normalizedPath.toString());
    }

    @Test
    public void testToAbsoluteWithAbsolutePath() throws IOException {
        Path absolutePath1 = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testsubdir/testfile.txt"));
        Path absolutePath2 = absolutePath1.toAbsolutePath();

        assertEquals("Absolute paths mismatch", absolutePath1.toString(), absolutePath2.toString());
    }

    @Test
    public void testToAbsoluteWithRelativePath() throws IOException {
        Path absolutePath1 = new SafenetPath(fileSystem, URI.create("testsubdir/testfile.txt"));
        Path absolutePath2 = absolutePath1.toAbsolutePath();

        assertNotEquals("Absolute and relative paths match", absolutePath1.toString(), absolutePath2.toString());
    }

    @Test
    public void testCompareToWithMatchingPaths() throws IOException {
        Path path1 = new SafenetPath(fileSystem, URI.create("/testsubdir/testfile.txt"));
        Path path2 = new SafenetPath(fileSystem, URI.create("/testsubdir/testfile.txt"));

        assertEquals("Absolute paths mismatch", 0, path1.compareTo(path2));
    }

    @Test
    public void testCompareToWithMismatchingPathsLessThan() throws IOException {
        Path path1 = new SafenetPath(fileSystem, URI.create("/atestsubdir/testfile.txt"));
        Path path2 = new SafenetPath(fileSystem, URI.create("/btestsubdir/testfile.txt"));

        assertEquals("Absolute paths mismatch", -1, path1.compareTo(path2));
    }

    @Test
    public void testCompareToWithMismatchingPathsGreaterThan() throws IOException {
        Path path1 = new SafenetPath(fileSystem, URI.create("/btestsubdir/testfile.txt"));
        Path path2 = new SafenetPath(fileSystem, URI.create("/atestsubdir/testfile.txt"));

        assertEquals("Absolute paths mismatch", 1, path1.compareTo(path2));
    }

    @Test
    public void testIteratorWithAbsolutePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Iterator<Path> pathIterator = path.iterator();
        String builtPath = "";

        while (pathIterator.hasNext()) {
            Path element = pathIterator.next();
            builtPath += fileSystem.getSeparator() + element.getFileName();
        }

        assertEquals("Iterator built path mismatches original relative path", path.toString(), builtPath);
    }

    @Test
    public void testIteratorWithRelativePath() throws IOException {
        Path path = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Iterator<Path> pathIterator = path.iterator();
        String builtPath = "";

        while (pathIterator.hasNext()) {
            Path element = pathIterator.next();
            builtPath += fileSystem.getSeparator() + element.getFileName();
        }

        assertEquals("Iterator built path mismatches original absolute path", path.toString(), builtPath);
    }

    @Test
    public void testToRealPathWithNonNormalizedRelativePath() throws IOException {
        Path realPath = new SafenetPath(fileSystem, URI.create("./testdir/../testsubdir//testfile.txt")).toRealPath();

        assertEquals("Normalized path mismatches raw path", "/testsubdir/testfile.txt", realPath.toString());
    }

    @Test
    public void testResolveWithNonNormalizedRelativeWithTrailingSlashPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));
        Path realPath = new SafenetPath(fileSystem, URI.create("../testsubdir//testfile.txt"));

        assertEquals("Resolved path is invalid", "/testdir/testsubdir/testfile.txt", basePath.resolve(realPath).toString());
    }

    @Test
    public void testResolveWithNonNormalizedRelativeWithoutTrailingSlashPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir"));
        Path realPath = new SafenetPath(fileSystem, URI.create("../testsubdir//testfile.txt"));

        assertEquals("Resolved path is invalid", "/testdir/testsubdir/testfile.txt", basePath.resolve(realPath).toString());
    }

    @Test
    public void testResolveWithNonNormalizedRelativeFilePath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testfile.txt"));
        Path realPath = new SafenetPath(fileSystem, URI.create("../testsubdir//testfile.txt"));

        assertEquals("Resolved path is invalid", "/testdir/testsubdir/testsubdir/testfile.txt", basePath.resolve(realPath).toString());
    }

    @Test
    public void testResolveStringWithNonNormalizedRelativeWithTrailingSlashPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));

        assertEquals("Resolved path is invalid", "/testdir/testsubdir/testfile.txt", basePath.resolve("../testsubdir/./testfile.txt").toString());
    }

    @Test
    public void testResolveSiblingWithNonNormalizedRelativeWithTrailingSlashPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));
        Path siblingPath = new SafenetPath(fileSystem, URI.create("testsubdirsibling//testfile.txt"));

        assertEquals("Resolved sibling path is invalid", "/testdir/testsubdirsibling/testfile.txt", basePath.resolveSibling(siblingPath).toString());
    }

    @Test
    public void testResolveSiblingWithAbsoluteBaseAndAbsoluteSiblingPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));
        Path siblingPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testsubdirsibling//testfile.txt"));

        assertEquals("Resolved sibling path is invalid", "/testsubdirsibling/testfile.txt", basePath.resolveSibling(siblingPath).toString());
    }

    @Test
    public void testResolveSiblingWithRootBaseAndRelativeSiblingPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING));
        Path siblingPath = new SafenetPath(fileSystem, URI.create("testsubdirsibling//testfile.txt"));

        assertEquals("Resolved sibling path is invalid", "testsubdirsibling/testfile.txt", basePath.resolveSibling(siblingPath).toString());
    }

    @Test
    public void testResolveSiblingStringWithNonNormalizedRelativeWithTrailingSlashPath() throws IOException {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));

        assertEquals("Resolved sibling string path is invalid", "/testdir/testsubdirsibling/testfile.txt", basePath.resolveSibling("testsubdirsibling//testfile.txt").toString());
    }

    @Test
    public void testRelativizeWithTwoAbsolutePaths() {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));
        Path extraPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testsubdir2/testfile.txt"));

        assertEquals("Relativized path is invalid", "testsubdir2/testfile.txt", basePath.relativize(extraPath).toString());
    }

    @Test
    public void testRelativizeWithTwoAbsolutePathsReversed() {
        Path basePath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/testsubdir2/testfile.txt"));
        Path extraPath = new SafenetPath(fileSystem, URI.create(URI_HOST_STRING + "testdir/testsubdir/"));

        assertEquals("Relativized path is invalid", "testsubdir2/testfile.txt", basePath.relativize(extraPath).toString());
    }
}
