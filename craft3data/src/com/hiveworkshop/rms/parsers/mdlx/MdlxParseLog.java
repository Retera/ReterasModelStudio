package com.hiveworkshop.rms.parsers.mdlx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Headless replacement for the TRMS fork's {@code ExceptionPopup}: the parser reports unknown tokens and chunk
 * errors here instead of opening Swing dialogs. Callers read the collected messages after a load through
 * {@link #drain()}; nothing is ever shown on screen from this class.
 */
public final class MdlxParseLog {
	private static final List<String> messages = Collections.synchronizedList(new ArrayList<>());
	private static volatile Throwable firstException;
	private static final boolean DEBUG = Boolean.getBoolean("rms.mdlx.debug");

	private MdlxParseLog() {
	}

	public static void addStringToShow(final String s) {
		messages.add(s);
	}

	public static void clearStringsToShow() {
		messages.clear();
	}

	public static void setFirstException(final Throwable e) {
		if (firstException == null) {
			firstException = e;
		}
	}

	public static Throwable getFirstException() {
		return firstException;
	}

	public static void clearFirstException() {
		firstException = null;
	}

	public static void display(final Throwable e) {
		setFirstException(e);
		messages.add(String.valueOf(e));
	}

	public static void display(final String s, final Exception e) {
		setFirstException(e);
		messages.add(s + ": " + e);
	}

	public static void display(final Throwable e, final String s) {
		display(s, e instanceof Exception ? (Exception) e : new RuntimeException(e));
	}

	/** Prints the collected messages to stderr (the fork opened a dialog here) without clearing them. */
	public static void displayIfNotEmpty() {
		if (!messages.isEmpty()) {
			synchronized (messages) {
				for (final String message : messages) {
					System.err.println("[mdlx] " + message);
				}
			}
		}
	}

	/** Returns and clears everything collected since the last clear. */
	public static List<String> drain() {
		final List<String> copy;
		synchronized (messages) {
			copy = new ArrayList<>(messages);
			messages.clear();
		}
		return copy;
	}

	public static void clear() {
		messages.clear();
		firstException = null;
	}

	/** The fork's {@code System.out.println} chatter; only printed with {@code -Drms.mdlx.debug=true}. */
	public static void debug(final Object message) {
		if (DEBUG) {
			System.out.println(message);
		}
	}
}
