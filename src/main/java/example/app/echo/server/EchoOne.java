package example.app.echo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import example.app.echo.server.support.NetworkUtils;
import example.app.echo.server.support.ThreadUtils;

/**
 * Server used to echo a client message.
 *
 * @author John Blum
 * @see java.net.ServerSocket
 * @see java.net.Socket
 * @since 1.0.0
 */
public class EchoOne implements Runnable {

	protected static final boolean REUSE_ADDRESS = true;

	public static final int DEFAULT_PORT = 4096;

	protected static final int NUMBER_OF_THREADS = 10;
	protected static final int SO_TIMEOUT_MS = 0;

	public static void main(String[] args) throws IOException {

		int resolvedPort = resolvePort(args);

		new EchoOne(resolvedPort).run();
	}

	private static String log(String message, Object... args) {

		String resolveMessage = String.format(message, args);

		System.out.println(resolveMessage);
		System.out.flush();

		return resolveMessage;
	}

	private static int resolvePort(String[] args) {

		if (args != null && args.length > 0) {
			try {
				return Integer.parseInt(args[0]);
			}
			catch (Throwable ignore) { }
		}

		return Integer.getInteger("example.app.echo.server.port", DEFAULT_PORT);
	}

	private final ExecutorService echoService;

	private final ServerSocket serverSocket;

	public EchoOne(int port) throws IOException {

		this.echoService = newExecutorService("Echo Thread");
		this.serverSocket = newServerSocket(port);

		registerEchoServerShutdownHook();
	}

	private ExecutorService newExecutorService(String threadName) {
		return Executors.newFixedThreadPool(NUMBER_OF_THREADS, ThreadUtils.newThreadFactory(threadName));
	}

	private ServerSocket newServerSocket(int port) throws IOException {

		ServerSocket serverSocket = new ServerSocket();

		serverSocket.setReuseAddress(REUSE_ADDRESS);
		serverSocket.setSoTimeout(SO_TIMEOUT_MS);
		serverSocket.bind(new InetSocketAddress(port));

		return serverSocket;
	}

	private void registerEchoServerShutdownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			NetworkUtils.nullSafeClose(this.serverSocket);
			this.echoService.shutdown();
			log("EchoOne server shutting down%n");
		}));
	}

	protected ExecutorService getEchoService() {
		return this.echoService;
	}

	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	public int getPort() {
		return getServerSocket().getLocalPort();
	}

	public void run() {

		ServerSocket serverSocket = getServerSocket();

		log("EchoOne server running, listening on port [%d]%n", getPort());

		try {
			while (isRunning(serverSocket)) {
				handleEcho(serverSocket.accept());
			}
		}
		catch (IOException cause) {
			throw new RuntimeException(String.format("Failed to start server listening on port [%d]",
				getPort()), cause);
		}
	}

	private boolean isRunning(ServerSocket serverSocket) {
		return !(serverSocket == null || serverSocket.isClosed());
	}

	private void handleEcho(Socket socket) {
		getEchoService().submit(new EchoHandler(socket));
	}

	static class EchoHandler implements Runnable {

		private final Socket socket;

		EchoHandler(Socket socket) {
			this.socket = Objects.requireNonNull(socket, "Socket is required");
		}

		Socket getSocket() {
			return this.socket;
		}

		@Override
		public void run() {

			Socket socket = getSocket();

			String message = NetworkUtils.sendMessage(socket, NetworkUtils.readMessage(socket));

			log("[server] ECHO CLIENT: '%s'", message);
		}
	}
}
