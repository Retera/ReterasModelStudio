package com.hiveworkshop.rms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez
 */
public class WindowsRegistry {

	/**
	 * 
	 * @param location path in the registry
	 * @param key      registry key
	 * @return registry value or null if not found
	 */
	public static String readRegistry(final String location, final String key) {
		try {
			// Run reg query, then read output with StreamReader (internal class)
			final Process process = Runtime.getRuntime().exec("reg query \"" + location + "\" /v " + key);

			final StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			final String output = reader.getResult();

			// Output has the following format:
			// \n<Version information>\n\n\t<key>\t<registry type>\t<value>
			if (!output.contains("REG_SZ")) {
				return null;
			}

			// Parse out the value
			final String[] parsed = output.split("REG_SZ");
			String finalout = parsed[parsed.length - 1];
			finalout = finalout.trim();
			return finalout;
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @param location path in the registry
	 * @return registry values and sub-paths or empty array if none found
	 */
	public static String[] readRegistry(final String location) {
		try {
			// Run reg query, then read output with StreamReader (internal class)
			final Process process = Runtime.getRuntime().exec("reg query \"" + location + "\"");

			final StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			final String output = reader.getResult();
			// Output has the following format:
			//\n<<location>\n[\t<key>\t<registry type>\t<value>\n]\n>[<sub location>\n]
			return output.trim().split("\n");
		} catch (final Exception e) {
			return null;
		}
	}

	static class StreamReader extends Thread {
		private final InputStream is;
		private final StringWriter sw = new StringWriter();

		public StreamReader(final InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1) {
					sw.write(c);
				}
			} catch (final IOException ignored) {
			}
		}

		public String getResult() {
			return sw.toString();
		}
	}

	public static void main(final String[] args) {

		// Sample usage
		final String value = WindowsRegistry.readRegistry(
				"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\" + "Explorer\\Shell Folders", "Personal");
		System.out.println(value);
	}
}