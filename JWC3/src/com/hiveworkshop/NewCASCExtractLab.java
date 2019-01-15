package com.hiveworkshop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.hiveworkshop.blizzard.casc.ConfigurationFile;
import com.hiveworkshop.blizzard.casc.info.Info;
import com.hiveworkshop.blizzard.casc.storage.Storage;
import com.hiveworkshop.blizzard.casc.vfs.VirtualFileSystem;
import com.hiveworkshop.blizzard.casc.vfs.VirtualFileSystem.PathResult;

public class NewCASCExtractLab {

	public static void main(final String[] args) {
		// final var localAchiveFolder = Paths.get("C:\\Program Files (x86)\\StarCraft
		// II\\SC2Data\\data");
		final Path dataFolder = Paths.get("/home/etheller/.wine/drive_c/Program Files (x86)/Warcraft III/Data");
		final Path extractFolder = Paths.get("/home/etheller/Documents/CASC");

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
		try (final Storage storage = new Storage(dataFolder, false, true)) {
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
			final ArrayList<Callable<?>> jobList = new ArrayList<Callable<?>>();
			for (final PathResult pathResult : allFilePaths) {
				final String filePath = pathResult.getPath();
				String filePathFixed = filePath.replace("\\\\", "\\").replace('\\', '/');
				if (filePathFixed.charAt(0) == '/') {
					filePathFixed = filePathFixed.substring(1);
				}
				final Path outputPath = extractFolder.resolve(filePathFixed);
				final long fileSize = pathResult.getFileSize();
				final boolean exists = pathResult.existsInStorage();
				if (exists && !pathResult.isTVFS()) {
					final Callable<Object> job = new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Files.createDirectories(outputPath.getParent());
							try (final FileChannel fileChannel = FileChannel.open(outputPath, StandardOpenOption.CREATE,
									StandardOpenOption.WRITE, StandardOpenOption.READ,
									StandardOpenOption.TRUNCATE_EXISTING)) {
								final MappedByteBuffer fileBuffer = fileChannel.map(MapMode.READ_WRITE, 0L, fileSize);

								pathResult.readFile(fileBuffer);
								totalExtracted.addAndGet(fileSize);
							}

							/*
							 * pathResult.readFile(null); totalExtracted.addAndGet(fileSize);
							 */

							return null;
						}
					};

					jobList.add(job);

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

				System.out.println(filePath + " : " + fileSize + " : " + (exists ? "yes" : "no"));
			}

			System.out.println("extracting files");
			final ExecutorService executor = Executors.newFixedThreadPool(8);
			try {
				final List<Future<?>> jobFutures = (List) executor.invokeAll(jobList);
				for (final Future<?> jobFuture : jobFutures) {
					try {
						jobFuture.get();
					} catch (final ExecutionException e) {
						System.out.println("error extracting file");
						e.printStackTrace();
					}
				}
			} catch (final InterruptedException e) {
				System.out.println("interruption during execution");
				e.printStackTrace();
			} finally {
				executor.shutdownNow();
			}

			System.out.println("shuttting down thread pool");
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (final InterruptedException e) {
				System.out.println("unable to shutdown executor");
				e.printStackTrace();
			}

			final long endTime = System.nanoTime();

			System.out.println("total path string count: " + allFilePaths.size());

			final double runtime = (endTime - startTime) / 1000000000d;
			System.out.println("running time to process all files: " + runtime + "s");

			System.out.println("average process speed: " + totalExtracted.get() / runtime + "B/sec");

			System.out.println("success");
		} catch (final IOException e) {
			System.out.println("an exception occured");
			e.printStackTrace(System.out);
			System.out.println("fail");
			return;
		}

		System.out.println("end");
	}

}
