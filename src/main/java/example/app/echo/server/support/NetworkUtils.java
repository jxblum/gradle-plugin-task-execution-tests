package example.app.echo.server.support;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract utility class used to process network {@link Socket Sockets}.
 *
 * @author John Blum
 * @see java.net.Socket
 * @since 1.0.0
 */
public abstract class NetworkUtils {

	@SuppressWarnings("all")
	public static void nullSafeClose(Closeable closeable) {

		if (closeable != null) {
			try (closeable) { }
			catch (IOException ignore) { }
		}
	}

	public static String readMessage(Socket socket) {

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return reader.readLine();
		}
		catch (IOException cause) {
			throw new RuntimeException(String.format("Failed to read message: '%s'",
				cause.getMessage()), cause);
		}
	}

	public static String sendMessage(Socket socket, String message) {

		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			writer.println(message);
			writer.flush();
			return message;
		}
		catch (IOException cause) {
			throw new RuntimeException(String.format("Failed to send message [%s]: '%s'",
				message, cause.getMessage()), cause);
		}
	}
}
