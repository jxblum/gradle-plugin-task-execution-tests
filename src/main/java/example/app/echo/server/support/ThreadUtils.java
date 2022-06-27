package example.app.echo.server.support;

import java.util.concurrent.ThreadFactory;

/**
 * Abstract utility class used to process {@link Thread Threads}.
 *
 * @author John Blum
 * @see java.lang.Thread
 * @since 1.0.0
 */
public abstract class ThreadUtils {

	public static ThreadFactory newThreadFactory(String threadName) {

		return runnable -> {

			Thread thread = new Thread(runnable, threadName);

			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.setUncaughtExceptionHandler(newUncaughtExceptionHandler());

			return thread;
		};
	}

	private static Thread.UncaughtExceptionHandler newUncaughtExceptionHandler() {

		return (thread, throwable) -> {
			System.err.printf("Error in Echo Thread [%d]: '%s'", thread.getId(), throwable.getMessage());
			System.err.flush();
		};
	}
}
