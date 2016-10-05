package org.traktion0.safenet;

import org.traktion0.safenet.client.commands.CreateDirectory;
import org.traktion0.safenet.client.commands.DeleteAuthToken;
import org.traktion0.safenet.client.commands.SafenetBadRequestException;
import org.traktion0.safenet.client.commands.SafenetFactory;

import javax.ws.rs.WebApplicationException;

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
}
