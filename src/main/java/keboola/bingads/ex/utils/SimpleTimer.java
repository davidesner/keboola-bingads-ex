package keboola.bingads.ex.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author David Esner
 */
public class SimpleTimer {
	private long startTime;
	private final long timeOut;

	
	public SimpleTimer(long timeOut) {
		super();
		this.timeOut = timeOut;
	}

	public void startTimer() {
		startTime = System.currentTimeMillis();
	}

	public boolean isTimedOut() {
		return (System.currentTimeMillis() - this.startTime) >= this.timeOut;
	}

	public static void reallySleep(long millis) {
        boolean threadInterrupted = false;
        final long nanos = TimeUnit.MILLISECONDS.toNanos(millis);
        final long end = System.nanoTime() + nanos;
        long remaining;
        try {
            do {
                remaining = end - System.nanoTime();
                if (remaining <= 0) {
                    break;
                }
                try {
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(remaining));
                } catch (InterruptedException e) {
                    threadInterrupted = true;
                }
            } while (remaining > 0);
        } finally {
            if (threadInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
