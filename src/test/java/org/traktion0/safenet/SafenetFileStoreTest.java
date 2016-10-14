package org.traktion0.safenet;

import org.junit.Test;
import org.traktion0.safenet.filesystem.SafenetFileStore;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * Created by paul on 25/09/16.
 */
public class SafenetFileStoreTest {

    private static final String TEST_URI_PATH = "safe://localhost/testdir/testfile.txt";

    @Test
    public void testNameWithAbsoluteURI() {
        URI uri = URI.create(TEST_URI_PATH);
        SafenetFileStore safenetFileStore = new SafenetFileStore(uri);

        assertEquals(TEST_URI_PATH, safenetFileStore.name());
    }

    @Test
    public void testTypeWithAbsoluteURI() {
        URI uri = URI.create(TEST_URI_PATH);
        SafenetFileStore safenetFileStore = new SafenetFileStore(uri);

        assertEquals("safe", safenetFileStore.type());
    }
}
