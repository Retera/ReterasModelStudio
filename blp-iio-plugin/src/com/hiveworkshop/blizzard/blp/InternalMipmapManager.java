package com.hiveworkshop.blizzard.blp;

import static com.hiveworkshop.blizzard.blp.BLPCommon.MIPMAP_MAX;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.hiveworkshop.lang.LocalizedFormatedString;

/**
 * Object for managing internally stored BLP mipmap data chunks. This is used by
 * BLP version 1 and later.
 * <p>
 * The location and size of mipmap data chunks within a stream are managed.
 * Methods are provided to read or write mipmap data chunks in a stream. Methods
 * are provided to configure mipmap chunk management.
 * 
 * @author ImperialGood
 */
class InternalMipmapManager {
	private final int[] chunkOffsets = new int[MIPMAP_MAX];
	private final int[] chunkSizes = new int[MIPMAP_MAX];
	private long chunkStreamPos = 0l;

	public InternalMipmapManager() {

	}

	/**
	 * Extracts a mipmap data chunk for the requested mipmap level from the
	 * given stream and returns it as unprocessed data. A warning handler must
	 * be provided to process any warnings that occur during extraction.
	 * <p>
	 * If the chunk size is too big to process a warning will be emitted and as
	 * much data as allowed will be returned. If the chunk extends beyond the
	 * EOF a warning will be emitted and as much data as available be be
	 * returned.
	 * <p>
	 * Chunks with 0 size generate no I/O.
	 * 
	 * @param src
	 *            stream to source mipmap data chunks from.
	 * @param mipmap
	 *            the mipmap level.
	 * @param warning
	 *            warning handler function.
	 * @return a byte array containing the mipmap data chunk.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public byte[] getMipmapDataChunk(ImageInputStream src, int mipmap,
			Consumer<LocalizedFormatedString> warning) throws IOException {
		final long offset = chunkOffsets[mipmap] & 0xFFFFFFFFL;
		final long sizeLong = chunkSizes[mipmap] & 0xFFFFFFFFL;

		// process chunk size
		final int size;
		final int sizeMax = Integer.MAX_VALUE;
		if (sizeLong > sizeMax) {
			warning.accept(new LocalizedFormatedString("com.hiveworkshop.text.blp",
					"BadChunkSize", sizeLong, sizeMax));
			size = sizeMax;
		} else {
			size = (int) sizeLong;
		}

		// allocate buffer
		byte[] buff = new byte[size];

		// set stream to correct position
		if (size > 0)
			src.seek(offset);

		// read data
		int len = size;
		int off = 0;
		while (len > 0) {
			final int read = src.read(buff, off, len);

			// end of file before full read
			if (read == -1) {
				warning.accept(new LocalizedFormatedString("com.hiveworkshop.text.blp",
						"BadChunkPos", size, off));
				buff = Arrays.copyOf(buff, off);
				break;
			}

			len -= read;
			off += read;
		}

		return buff;
	}

	/**
	 * Inserts a mipmap data chunk for the requested mipmap level to the given
	 * stream. An empty array can be used to remove chunks.
	 * <p>
	 * The mipmap data chunk block offset must be set before calling this.
	 * Failure to do so may cause file corruption.
	 * <p>
	 * For best results the mipmaps should be set only once in rising numeric
	 * order.
	 * 
	 * @param dst
	 *            stream to place mipmap data chunks to.
	 * @param mipmap
	 *            the mipmap level.
	 * @param chunk
	 *            a byte array containing the mipmap data chunk.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public void setMipmapDataChunk(ImageOutputStream dst, int mipmap,
			byte[] chunk) throws IOException {
		final int len = chunk.length;

		// TODO compact/defragment stream

		// chunk logical position
		chunkSizes[mipmap] = len;
		final long offset = len > 0 ? chunkStreamPos : 0;
		if (offset > 0xFFFFFFFFL)
			throw new IOException("Stream offset too big.");
		chunkOffsets[mipmap] = (int) offset;

		// write chunk
		if (len > 0) {
			dst.seek(chunkStreamPos);
			dst.write(chunk);
		}

		chunkStreamPos += len;
	}

	/**
	 * Set the offset of the mipmap data chunk block to the current stream
	 * position.
	 * <p>
	 * This method is intended to be called before any mipmap data chunks are
	 * set. Calling it while any mipmap data chunks are set will result in
	 * undefined behavior.
	 * 
	 * @param src
	 *            stream to place mipmap data chunks to.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public void setMipmapDataChunkBlockOffset(ImageInputStream src)
			throws IOException {
		final long offset = src.getStreamPosition();
		if (offset > 0xFFFFFFFFL)
			throw new IOException("Stream offset too big.");
		chunkStreamPos = offset;
	}

	/**
	 * Flushes the stream to the minimum position needed to read the requested
	 * mipmap or higher.
	 * 
	 * @param src
	 *            stream to flush.
	 * @param mipmap
	 *            the mipmap level.
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	public void flushToMipmap(ImageInputStream src, int mipmap)
			throws IOException {
		// find lowest offset to allow the mipmaps to be read
		long pos = Long.MAX_VALUE;
		for (int i = mipmap + 1; i < MIPMAP_MAX; i += 1) {
			long newpos = chunkOffsets[i] & 0xFFFFFFFFL;
			if (newpos < pos)
				pos = newpos;
		}

		src.flushBefore(pos);
	}
	
	public void readObject(ImageInputStream in) throws IOException {
		in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		
		// read mipmap chunk descriptions
		in.readFully(chunkOffsets, 0, chunkOffsets.length);
		in.readFully(chunkSizes, 0, chunkSizes.length);

		// find end of mipmap block
		long pos = 0;
		for (int i = 0; i < MIPMAP_MAX; i += 1) {
			long newpos = chunkOffsets[i] & 0xFFFFFFFFL;
			if (chunkSizes[i] != 0 && newpos > pos)
				pos = newpos;
		}
		
		chunkStreamPos = pos;
	}
	
	public void writeObject(ImageOutputStream out) throws IOException {
		out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		
		// write mipmap chunk descriptions
		out.writeInts(chunkOffsets, 0, chunkOffsets.length);
		out.writeInts(chunkSizes, 0, chunkSizes.length);
	}
}
