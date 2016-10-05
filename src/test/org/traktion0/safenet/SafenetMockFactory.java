package org.traktion0.safenet;

import org.traktion0.safenet.client.beans.Info;
import org.traktion0.safenet.client.beans.SafenetDirectory;
import org.traktion0.safenet.client.beans.SafenetFile;
import org.traktion0.safenet.client.commands.*;

import javax.ws.rs.WebApplicationException;

import java.time.OffsetDateTime;

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
}