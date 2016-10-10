package org.traktion0.safenet;

import org.traktion0.safenet.client.beans.Info;
import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.beans.SafenetFile;
import org.traktion0.safenet.client.commands.*;

import javax.ws.rs.WebApplicationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by paul on 04/10/16.
 */
public abstract class SafenetMockFactory {

    public static SafenetFactory makeBasicSafenetFactoryMock() {
        // PG: Note that closing a FileSystem attempts to delete an auth token, so mock this
        DeleteAuthToken deleteAuthToken = mock(DeleteAuthToken.class);
        SafenetFactory safenetFactory = mock(SafenetFactory.class);
        when(safenetFactory.makeDeleteAuthTokenCommand()).thenReturn(deleteAuthToken);
        when(deleteAuthToken.execute()).thenReturn("ok");

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithCreateDirectoryReturnsSuccess() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        // PG: Note that closing a FileSystem attempts to delete an auth token, so mock this
        CreateDirectory createDirectory = mock(CreateDirectory.class);
        when(safenetFactory.makeCreateDirectoryCommand(anyString())).thenReturn(createDirectory);
        when(createDirectory.execute()).thenReturn("ok");

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithCreateDirectoryThrowsException() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        // PG: Note that closing a FileSystem attempts to delete an auth token, so mock this
        CreateDirectory createDirectory = mock(CreateDirectory.class);
        WebApplicationException e = new WebApplicationException("Not Found", 404);
        when(safenetFactory.makeCreateDirectoryCommand(anyString())).thenReturn(createDirectory);
        when(createDirectory.execute()).thenThrow(new SafenetBadRequestException(e.getMessage(), e.getCause()));

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithGetFileAttributesReturnsSuccess() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        SafenetFile safenetFile = new SafenetFile();
        safenetFile.setContentLength(3067);
        safenetFile.setContentRange("bytes 0-3067/3067");
        safenetFile.setAcceptRanges("bytes");
        safenetFile.setContentType("image/svg+xml");
        safenetFile.setCreatedOn(OffsetDateTime.parse("2016-10-04T09:34:44.523Z"));
        safenetFile.setLastModified(OffsetDateTime.parse("2016-10-05T10:24:24.123Z"));

        GetFileAttributes getFileAttributes = mock(GetFileAttributes.class);
        when(safenetFactory.makeGetFileAttributesCommand(anyString())).thenReturn(getFileAttributes);
        when(getFileAttributes.execute()).thenReturn(safenetFile);

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithGetDirectoryReturnsSuccess() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        GetFileAttributes getFileAttributes = mock(GetFileAttributes.class);
        WebApplicationException e = new WebApplicationException("Not Found", 404);
        when(safenetFactory.makeGetFileAttributesCommand(anyString())).thenReturn(getFileAttributes);
        when(getFileAttributes.execute()).thenThrow(new SafenetBadRequestException(e.getMessage(), e.getCause()));

        SafenetDirectory safenetDirectory = new SafenetDirectory();
        Info info = new Info();
        info.setCreatedOn(1475701203);
        info.setModifiedOn(1475701221);
        safenetDirectory.setInfo(info);

        GetDirectory getDirectory = mock(GetDirectory.class);
        when(safenetFactory.makeGetDirectoryCommand(anyString())).thenReturn(getDirectory);
        when(getDirectory.execute()).thenReturn(safenetDirectory);

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithGetFileReturnsTextSuccess() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        SafenetFile safenetFile = new SafenetFile();
        String fileContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor" +
                "incididunt ut labore et dolore magna aliqua.";
        InputStream stream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        safenetFile.setInputStream(stream);
        safenetFile.setContentLength(3067);
        safenetFile.setContentRange("bytes 0-3067/3067");
        safenetFile.setAcceptRanges("bytes");
        safenetFile.setContentType("image/svg+xml");
        safenetFile.setCreatedOn(OffsetDateTime.parse("2016-10-04T09:34:44.523Z"));
        safenetFile.setLastModified(OffsetDateTime.parse("2016-10-05T10:24:24.123Z"));

        GetFile getFile = mock(GetFile.class);
        when(safenetFactory.makeGetFileCommand(anyString())).thenReturn(getFile);
        when(getFile.execute()).thenReturn(safenetFile);

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithGetFileReturnsImageSuccess() throws IOException {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        try {
            File imageFile = new File("src/test/resources/maidsafe_layered_haze.jpg");
            FileInputStream fileInputStream = new FileInputStream(imageFile);

            SafenetFile safenetFile = new SafenetFile();
            safenetFile.setInputStream(fileInputStream);
            safenetFile.setContentLength(167924);
            safenetFile.setContentRange("bytes 0-167924/167924");
            safenetFile.setAcceptRanges("bytes");
            safenetFile.setContentType("image/jpg");
            safenetFile.setCreatedOn(OffsetDateTime.parse("2016-10-08T13:34:44.523Z"));
            safenetFile.setLastModified(OffsetDateTime.parse("2016-09-13T15:24:24.123Z"));

            GetFile getFile = mock(GetFile.class);
            when(safenetFactory.makeGetFileCommand(anyString())).thenReturn(getFile);
            when(getFile.execute()).thenReturn(safenetFile);

            return safenetFactory;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static SafenetFactory makeSafenetFactoryMockWithCreateFileReturnsSuccess() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        CreateFile createFile = mock(CreateFile.class);
        when(safenetFactory.makeCreateFileCommand(anyString(), any(byte[].class))).thenReturn(createFile);
        when(createFile.execute()).thenReturn("ok");

        return safenetFactory;
    }

    public static SafenetFactory makeSafenetFactoryMockWithGetDirectoryReturnsRootDirectories() {
        SafenetFactory safenetFactory = makeBasicSafenetFactoryMock();

        GetDirectory getDirectory = mock(GetDirectory.class);
        SafenetDirectory safenetDirectory = new SafenetDirectory();

        List<Info> rootDirectories = new ArrayList<>();
        Info app = new Info();
        app.setName("app");
        rootDirectories.add(app);
        Info drive = new Info();
        drive.setName("drive");
        rootDirectories.add(drive);

        safenetDirectory.setSubDirectories(rootDirectories);
        when(safenetFactory.makeGetDirectoryCommand(anyString())).thenReturn(getDirectory);
        when(getDirectory.execute()).thenReturn(safenetDirectory);

        return safenetFactory;
    }
}
