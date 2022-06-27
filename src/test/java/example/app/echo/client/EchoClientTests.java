package example.app.echo.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import example.app.echo.server.EchoOne;
import example.app.echo.server.support.NetworkUtils;

/**
 * Tests for {@link EchoOne} server.
 *
 * @author John Blum
 * @see java.net.Socket
 * @see org.junit.Test
 * @see example.app.echo.server.EchoOne
 * @since 1.0.0
 */
public class EchoClientTests {

	private static final int PORT = Integer.getInteger("example.app.echo.client.port", EchoOne.DEFAULT_PORT);
	private static final String HOST = "localhost";

	private static String log(String message, Object... args) {

		String resolvedMessage = String.format(message, args);

		System.err.println(resolvedMessage);
		System.err.flush();

		return resolvedMessage;
	}

	@Test
	public void serverEchosClientMessage() throws IOException {

		String message = "Hello Test!";

		try (Socket client = new Socket(HOST, PORT)) {

			NetworkUtils.sendMessage(client, message);

			String echoMessage = NetworkUtils.readMessage(client);

			log("[client] ECHO SERVER: '%s'%n", echoMessage);

			assertThat(echoMessage).isEqualTo(message);
		}
	}
}
