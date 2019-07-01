package com.hiveworkshop.wc3.casc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.hiveworkshop.blizzard.casc.ConfigurationFile;
import com.hiveworkshop.blizzard.casc.info.Info;
import com.hiveworkshop.blizzard.casc.storage.Storage;
import com.hiveworkshop.blizzard.casc.vfs.VirtualFileSystem;
import com.hiveworkshop.blizzard.casc.vfs.VirtualFileSystem.PathResult;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.user.WindowsRegistry;

/**
 * CASC was dangerously close to put Matrix Eater in a casket, so we named it
 * Cascket. Put your casc in a basket
 *
 * @author etheller
 *
 */
public class Cascket implements Codebase {
	private final Map<String, PathResult> pathToResult = new HashMap<>();
	private final String[] prefixes = { "war3.mpq", "deprecated.mpq",
			getLocalization().toLowerCase() + "-war3local.mpq", "war3.w3mod", "deprecated.w3mod",
			"war3.w3mod\\_locales\\" + getLocalization().toLowerCase() + ".w3mod" };

	public static String getLocalization() {
		try {
			final String readRegistry = WindowsRegistry.readRegistry(
					"HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Classic Launcher\\w3", "Locale");
			if ((readRegistry == null) || (readRegistry.length() < 1)) {
				return "enUS";
			}
			return readRegistry;
		} catch (final Exception exc) {
			return "enUS";
		}
	}

	public Cascket(final Path dataFolder) {

		// final var localIndexFile = Paths.get("C:\\Program Files (x86)\\StarCraft
		// II\\SC2Data\\data\\0000000139.idx");
		System.out.println("opening info");
		final Path infoFile = dataFolder.resolveSibling(Info.BUILD_INFO_FILE_NAME);
		Info info = null;
		try {
			info = new Info(ByteBuffer.wrap(Files.readAllBytes(infoFile)));
		} catch (final IOException e) {
			System.out.println("an exception occured");
			e.printStackTrace(System.out);
			System.out.println("fail");
			return;
		}

		System.out.println("extracting build configuration key");
		if (info.getRecordCount() < 1) {
			System.out.println("info contains no records");
			System.out.println("fail");
			return;
		}
		final String buildKeyField = "Build Key";
		final int fieldIndex = info.getFieldIndex(buildKeyField);
		if (fieldIndex == -1) {
			System.out.println("info missing field");
			System.out.println("fail");
			return;
		}
		final String buildKey = info.getField(0, fieldIndex);

		System.out.println("opening configuration");
		ConfigurationFile buildConfiguration = null;
		try {
			buildConfiguration = ConfigurationFile.lookupConfigurationFile(dataFolder, buildKey);
		} catch (final IOException e) {
			System.out.println("an exception occured");
			e.printStackTrace(System.out);
			System.out.println("fail");
			return;
		}

		System.out.println("opening store");
		try {
			final Storage storage = new Storage(dataFolder, false, true);
			System.out.println("mounting VFS");
			final VirtualFileSystem vfs = new VirtualFileSystem(storage, buildConfiguration.getConfiguration());

			System.out.println("getting all paths");
			final List<PathResult> allFilePaths = vfs.getAllFilePaths();

			final long startTime = System.nanoTime();
			/*
			 * final var repeatCount = 120; for (var i = 0 ; i < repeatCount ; i+= 1) {
			 * allFilePaths = vfs.getAllFilePaths(); }
			 */

			final AtomicLong totalExtracted = new AtomicLong(0L);
			for (final PathResult pathResult : allFilePaths) {
				final String filePath = pathResult.getPath();
				String filePathFixed = filePath.replace("\\\\", "\\");
				if (filePathFixed.charAt(0) == '\\') {
					filePathFixed = filePathFixed.substring(1);
				}
				final long fileSize = pathResult.getFileSize();
				final boolean exists = pathResult.existsInStorage();
				if (exists && !pathResult.isTVFS()) {
					pathToResult.put(filePathFixed, pathResult);

					/*
					 * try { Files.createDirectories(outputPath.getParent()); try (final var
					 * fileChannel = FileChannel.open(outputPath, StandardOpenOption.CREATE,
					 * StandardOpenOption.WRITE, StandardOpenOption.READ,
					 * StandardOpenOption.TRUNCATE_EXISTING)) { final var fileBuffer =
					 * fileChannel.map(MapMode.READ_WRITE, 0L, fileSize);
					 *
					 * pathResult.readFile(fileBuffer); totalExtracted+= fileSize; } } catch
					 * (IOException e) { System.out.println("extract failed");
					 * e.printStackTrace(System.out); }
					 */
				}
//
//				if (filePath.contains(".txt") && filePath.contains("enus")) {
//					System.out.println(filePath + " : " + fileSize + " : " + (exists ? "yes" : "no"));
//				}
			}

			System.out.println("shuttting down thread pool");

			final long endTime = System.nanoTime();

			System.out.println("total path string count: " + allFilePaths.size());

			final double runtime = (endTime - startTime) / 1000000000d;
			System.out.println("running time to process all files: " + runtime + "s");

			System.out.println("average process speed: " + (totalExtracted.get() / runtime) + "B/sec");

			System.out.println("success");
		} catch (final IOException e) {
			System.out.println("an exception occured");
			e.printStackTrace(System.out);
			System.out.println("fail");
			return;
		}
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) {
		final PathResult pathResult = getResult(filepath);
		if (pathResult != null) {
			final ByteBuffer buffer = ByteBuffer.allocate((int) pathResult.getFileSize());
			final byte[] data = new byte[buffer.remaining()];
			try {
				pathResult.readFile(buffer);
				buffer.flip();
				buffer.get(data);
				return new ByteArrayInputStream(data);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public File getFile(final String filepath) {
		final PathResult pathResult = getResult(filepath);
		if (pathResult != null) {
			try {
				String tmpdir = System.getProperty("java.io.tmpdir");
				if (!tmpdir.endsWith(File.separator)) {
					tmpdir += File.separator;
				}
				final String tempDir = tmpdir + "MatrixEaterExtract/";
				final File tempProduct = new File(tempDir + filepath.replace('\\', File.separatorChar));
				tempProduct.delete();
				tempProduct.getParentFile().mkdirs();
				try (final FileChannel fileChannel = FileChannel.open(tempProduct.toPath(), StandardOpenOption.CREATE,
						StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING)) {
					final MappedByteBuffer fileBuffer = fileChannel.map(MapMode.READ_WRITE, 0L,
							pathResult.getFileSize());
					pathResult.readFile(fileBuffer);
				}
				tempProduct.deleteOnExit();
				return tempProduct;
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private PathResult getResult(String filepath) {
		filepath = filepath.toLowerCase();
		for (final String prefix : prefixes) {
			final String lookupPath = prefix + "\\" + filepath;
			final PathResult file = pathToResult.get(lookupPath);
			if (file != null) {
				return file;
			}
		}
		return pathToResult.get(filepath);
	}

	@Override
	public boolean has(final String filepath) {
		return getResult(filepath) != null;
	}

	public Collection<String> getListfile() {
		return pathToResult.keySet();
	}

}
