/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/* Included in MatrixEater by Retera for deployability */

package com.badlogic.gdx.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads shared libraries from a natives jar file (desktop) or arm folders
 * (Android). For desktop projects, have the natives jar in the classpath, for
 * Android projects put the shared libraries in the libs/armeabi and
 * libs/armeabi-v7a folders.
 *
 * @author mzechner
 * @author Nathan Sweet
 */
public class SharedLibraryLoader {
	static public boolean isWindows = System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = System.getProperty("os.name").contains("Linux");
	static public boolean isMac = System.getProperty("os.name").contains("Mac");
	static public boolean isIos = false;
	static public boolean isAndroid = false;
	static public boolean isARM = System.getProperty("os.arch").startsWith("arm");
	static public boolean is64Bit = System.getProperty("os.arch").equals("amd64") || System.getProperty("os.arch").equals("x86_64");

	// JDK 8 only.
	static public String abi = System.getProperty("sun.arch.abi") != null ? System.getProperty("sun.arch.abi") : "";

	static {
		final String vm = System.getProperty("java.runtime.name");
		if (vm != null && vm.contains("Android Runtime")) {
			isAndroid = true;
			isWindows = false;
			isLinux = false;
			isMac = false;
			is64Bit = false;
		}
		if (!isAndroid && !isWindows && !isLinux && !isMac) {
			isIos = true;
			is64Bit = false;
		}
	}

	static private final HashSet<String> loadedLibraries = new HashSet<>();

	private String nativesJar;

	public SharedLibraryLoader() {
	}

	/**
	 * Fetches the natives from the given natives jar file. Used for testing a
	 * shared lib on the fly.
	 */
	public SharedLibraryLoader(final String nativesJar) {
		this.nativesJar = nativesJar;
	}

	/**
	 * Maps a platform independent library name to a platform dependent name.
	 */
	public String mapLibraryName(final String libraryName) {
		if (isWindows) {
			return libraryName + (is64Bit ? "64.dll" : ".dll");
		}
		if (isLinux) {
			return "lib" + libraryName + (isARM ? "arm" + abi : "") + (is64Bit ? "64.so" : ".so");
		}
		if (isMac) {
			return "lib" + libraryName + (is64Bit ? "64.dylib" : ".dylib");
		}
		return libraryName;
	}

	/**
	 * Loads a shared library for the platform the application is running on.
	 *
	 * @param libraryName The platform independent library name. If not contain a
	 *                    prefix (eg lib) or suffix (eg .dll).
	 */
	public void load(final String libraryName) {
//		// in case of iOS, things have been linked statically to the executable, bail out.
//		if (isIos) {
//			return;
//		}

		synchronized (SharedLibraryLoader.class) {
			if (isLoaded(libraryName)) {
				return;
			}
			final String platformName = mapLibraryName(libraryName);
			try {
				if (isAndroid) {
					System.loadLibrary(platformName);
				} else {
					loadFile(platformName);
				}
				setLoaded(libraryName);
			} catch (final Throwable ex) {
				throw new RuntimeException("Couldn't load shared library '" + platformName + "' for target: "
						+ System.getProperty("os.name") + (is64Bit ? ", 64-bit" : ", 32-bit"), ex);
			}
		}
	}

	private InputStream readFile(final String path) {
		if (nativesJar == null) {
			final InputStream input = SharedLibraryLoader.class.getResourceAsStream("/" + path);
			if (input == null) {
				throw new RuntimeException("Unable to read file for extraction: " + path);
			}
			return input;
		}

		// Read from JAR.
		try (final ZipFile file = new ZipFile(nativesJar)){
			final ZipEntry entry = file.getEntry(path);
			if (entry == null) {
				throw new RuntimeException("Couldn't find '" + path + "' in JAR: " + nativesJar);
			}
			return file.getInputStream(entry);
		} catch (final IOException ex) {
			throw new RuntimeException("Error reading '" + path + "' in JAR: " + nativesJar, ex);
		}
	}

	/**
	 * Extracts the specified file to the specified directory if it does not already
	 * exist or the CRC does not match. If file extraction fails and the file exists
	 * at java.library.path, that file is returned.
	 *
	 * @param sourcePath The file to extract from the classpath or JAR.
	 * @param dirName    The name of the subdirectory where the file will be
	 *                   extracted. If null, the file's CRC will be used.
	 * @return The extracted file.
	 */
	public File extractFile(final String sourcePath, String dirName) throws IOException {
		try (InputStream input = readFile(sourcePath);) {

			final String sourceCrc = crc(input);
			if (dirName == null) {
				dirName = sourceCrc;
			}

			File extractedFile = getExtractedFile(dirName, new File(sourcePath).getName());
			if (extractedFile == null) {
				extractedFile = getExtractedFile(UUID.randomUUID().toString(), new File(sourcePath).getName());
				if (extractedFile == null) {
					throw new RuntimeException(
							"Unable to find writable path to extract file. Is the user home directory writable?");
				}
			}
			return extractFile(sourcePath, sourceCrc, extractedFile);
		} catch (final RuntimeException ex) {
			// Fallback to file at java.library.path location, eg for applets.
			final File file = new File(System.getProperty("java.library.path"), sourcePath);
			System.out.println("\tfailed to extract file. Hoping to find file at \"" + file.getAbsolutePath() + "\"");
			if (file.exists()) {
				System.out.println("\t\tfile found!");
				return file;
			}
			System.out.println("\t\tfile not found");
			throw new RuntimeException("Could not extract \"" + sourcePath + "\"", ex);
		}
	}

	/**
	 * Extracts the specified file into the temp directory if it does not already
	 * exist or the CRC does not match. If file extraction fails and the file exists
	 * at java.library.path, that file is returned.
	 *
	 * @param sourcePath The file to extract from the classpath or JAR.
	 * @param dir        The location where the extracted file will be written.
	 */
	public void extractFileTo(final String sourcePath, final File dir) throws IOException {
		try (InputStream input = readFile(sourcePath)) {
			extractFile(sourcePath, crc(input), new File(dir, new File(sourcePath).getName()));
		}
	}

	/**
	 * Returns a path to a file that can be written. Tries multiple locations and
	 * verifies writing succeeds.
	 *
	 * @return null if a writable path could not be found.
	 */
	private File getExtractedFile(final String dirName, final String fileName) {
		String[] dirs = getTempDirsToTry(dirName);

		File firstWritebleFile = getFirstWritebleFile(fileName, dirs);

		if(firstWritebleFile == null && System.getenv("APP_SANDBOX_CONTAINER_ID") != null){
			String dir5 = System.getProperty("java.library.path");
			return new File(dir5, fileName);
		}
		return firstWritebleFile;
	}

	private String[] getTempDirsToTry(final String dirName){
		String[] dirs = new String[4];
		dirs[0] = System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/" + dirName; // Temp directory with username in path.
		dirs[1] = getSystemTempFile(dirName); // System provided temp directory.
		dirs[2] = System.getProperty("user.home") + "/.libgdx/" + dirName; // User home.
		dirs[3] = ".temp/" + dirName; // Relative directory.
		return dirs;
	}

	private File getFirstWritebleFile(String fileName, String... testDir){
		for (String dir : testDir) {
			if(dir != null){
				File file = new File(dir, fileName);
				System.out.println("Checking filepath: \"" + file.getPath() + "\"");
				if (StreamUtils.canWrite(file)) {
					System.out.println("\tvalid path!");
					return file;
				}
			}
		}
		System.out.println("\tno path were valid!");
		return null;
	}

	private File extractFile(final String sourcePath, final String sourceCrc, final File extractedFile) {
		String extractedCrc = getFileCrC(extractedFile);

		// If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
		if (extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
			try (final InputStream input = readFile(sourcePath);){
				extractedFile.getParentFile().mkdirs();
				System.out.println("Extracting \"" + sourcePath + "\" to \"" + extractedFile.getPath() + "\"");
				writeStreamTo(extractedFile, input);
			} catch (final IOException ex) {
				throw new RuntimeException(
						"Error extracting file: " + sourcePath + "\nTo: " + extractedFile.getAbsolutePath(), ex);
			}
		}

		return extractedFile;
	}

	private String getFileCrC(File extractedFile) {
		if (extractedFile.exists()) {
			try (InputStream input = new FileInputStream(extractedFile)){
				return crc(input);
			} catch (final FileNotFoundException ignored) {
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private void writeStreamTo(File extractedFile, InputStream input) throws IOException {
		try (final FileOutputStream output = new FileOutputStream(extractedFile)) {
			final byte[] buffer = new byte[4096];
			int length;
			while ((length = input.read(buffer)) != -1) {
				output.write(buffer, 0, length);
			}
		}
	}

	/**
	 * Extracts the source file and calls System.load. Attemps to extract and load
	 * from multiple locations. Throws runtime exception if all fail.
	 */
	private void loadFile(final String sourcePath) {
		try (InputStream input = readFile(sourcePath)) {
			final String sourceCrc = crc(input);
			final String fileName = new File(sourcePath).getName();

			String[] dirs = getTempDirsToTry(sourceCrc);

			List<Throwable> throwables = tryLoadFileFrom(fileName, sourcePath, sourceCrc, dirs);

			if(throwables != null){
				// Fallback to java.library.path location, eg for applets.
				String dir5 = System.getProperty("java.library.path");
				File file = new File(dir5, sourcePath);
				if (file.exists()) {
					System.load(file.getAbsolutePath());
					return;
				}

				throw new RuntimeException(throwables.get(0));
			}
		} catch (IOException e){

		}
	}

	private List<Throwable> tryLoadFileFrom(String fileName, String sourcePath, String sourceCrc, String... dirs) {
		List<Throwable> exceptionList = new ArrayList<>();
		for (String dir : dirs){
			if(dir != null){
				File file = new File(dir, fileName);
				try {
					System.out.println("loading file to: " + file.getPath());
					System.load(extractFile(sourcePath, sourceCrc, file).getAbsolutePath());
					return null;
				} catch (final Throwable ex) {
					exceptionList.add(ex);
				}
			}
		}
		return exceptionList;
	}

	public String getSystemTempFile(String dirName){
		// System provided temp directory.
		try {
			File file = File.createTempFile(dirName, null);
			if (file.delete()) {
				return file.getPath();
			}
		} catch (final IOException ignored) {
		}
		return null;
	}

	/**
	 * Returns a CRC of the remaining bytes in the stream.
	 */
	public String crc(final InputStream input) {
		if (input == null) {
			throw new IllegalArgumentException("input cannot be null.");
		}
		final CRC32 crc = new CRC32();
		final byte[] buffer = new byte[4096];
		try {
			int length;
			while ((length = input.read(buffer)) != -1) {
				crc.update(buffer, 0, length);
			}
		} catch (final Exception ex) {
			StreamUtils.closeQuietly(input);
		}
		return Long.toString(crc.getValue(), 16);
	}

	/**
	 * Sets the library as loaded, for when application code wants to handle libary
	 * loading itself.
	 */
	static public synchronized void setLoaded(final String libraryName) {
		loadedLibraries.add(libraryName);
	}

	static public synchronized boolean isLoaded(final String libraryName) {
		return loadedLibraries.contains(libraryName);
	}
}
