package com.hiveworkshop.blizzard.blp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.IIOException;

/**
 * Object for managing externally stored BLP mipmap data chunks. This is used by
 * BLP0.
 * <p>
 * The retrieval and extraction of mipmap data chunks from accompanying files
 * are managed. Methods are provided to read or write mipmap data chunks.
 * 
 * @author ImperialGood
 */
class ExternalMipmapManager {
	/**
	 * Parent folder of the BLP0 file. Where the mipmaps files get saved.
	 */
	private final Path root;

	/**
	 * The base file name of the BLP0 file. All files for the same BLP0 file
	 * share this name.
	 */
	private final String name;

	/**
	 * Get the file for the specified mipmap level.
	 * 
	 * @param mipmap
	 *            the mipmap level.
	 * @return the file.
	 */
	private Path getMipmapFilePath(int mipmap) {
		return root.resolve(name + String.format(".b%02d", mipmap));
	}

	/**
	 * Constructs from a BLP0 file.
	 * <p>
	 * The Path must represent a file with the '.blp' suffix. The file itself is
	 * not manipulated and is assumed to exist.
	 * 
	 * @param file
	 *            a blp file.
	 * @throws IOException
	 *             if the file is not acceptable.
	 */
	public ExternalMipmapManager(Path file) throws IOException {
		final String fileName = file.getFileName().toString();

		if (!fileName.endsWith(".blp"))
			throw new IIOException(String.format(
					"Malformed file path: Got '%s' expected '*.blp'.",
					file.toString()));

		root = file.getParent();
		name = fileName.substring(0, fileName.length() - ".blp".length());
	}

	/**
	 * Extracts a mipmap data chunk for the requested mipmap level and returns
	 * it as unprocessed data.
	 * 
	 * @param mipmap
	 *            the mipmap level.
	 * @return a byte array containing the mipmap data chunk.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public byte[] getMipmapDataChunk(int mipmap) throws IOException {
		return Files.readAllBytes(getMipmapFilePath(mipmap));
	}

	/**
	 * Writes a mipmap data chunk for the requested mipmap level. A null chunk
	 * can be used to completely remove saved chunks.
	 * 
	 * @param mipmap
	 *            the mipmap level.
	 * @param chunk
	 *            a byte array containing the mipmap data chunk.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public void setMipmapDataChunk(int mipmap, byte[] chunk) throws IOException {
		final Path filePath = getMipmapFilePath(mipmap);

		if (chunk == null) {
			Files.deleteIfExists(filePath);
			return;
		}

		Files.write(filePath, chunk);
	}
}
