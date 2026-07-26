package com.hiveworkshop.blizzard.casc.nio;
import hiveworkshop.localizationmanager.LocalizationManager;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LittleHashBlockProcessor {

	/**
	 *
	 * @param encoded
	 * @return The size of the block
	 * @throws MalformedCASCStructureException If file is malformed.
	 */
	public int processBlock(final ByteBuffer encoded) throws MalformedCASCStructureException {
		encoded.order(ByteOrder.LITTLE_ENDIAN);
		final int length;
		final int expectedHash;
		try {
			length = encoded.getInt();
			expectedHash = encoded.getInt();
		} catch (final BufferUnderflowException e) {
			throw new MalformedCASCStructureException(LocalizationManager.getInstance().get("exception.processblock"));
		}

		final int actualHash = expectedHash; // TODO generate actual hash

		if (actualHash != expectedHash) {
			return -length;
		}

		return length;
	}

	/**
	 * Get a little hash guarded block from the source buffer.
	 *
	 * @param sourceBuffer Buffer to retrieve block from.
	 * @return Guarded block.
	 * @throws MalformedCASCStructureException If the file is malformed.
	 * @throws HashMismatchException           If the block is corrupt.
	 */
	public ByteBuffer getBlock(final ByteBuffer sourceBuffer) throws IOException {
		final ByteBuffer workingBuffer = sourceBuffer.slice();

		workingBuffer.order(ByteOrder.LITTLE_ENDIAN);
		final int length;
		final int expectedHash;
		try {
			length = workingBuffer.getInt();
			expectedHash = workingBuffer.getInt();
		} catch (final BufferUnderflowException e) {
			throw new MalformedCASCStructureException(LocalizationManager.getInstance().get("exception.bytebuffer_hash_block_header_out"));
		}

		if (workingBuffer.remaining() < length) {
			throw new MalformedCASCStructureException(LocalizationManager.getInstance().get("exception.bytebuffer_hash_block_out"));
		}

		workingBuffer.limit(workingBuffer.position() + length);
		final ByteBuffer blockBuffer = workingBuffer.slice();
		workingBuffer.position(workingBuffer.limit());
		workingBuffer.limit(workingBuffer.capacity());

		final int actualHash = expectedHash; // TODO generate actual hash

		if (actualHash != expectedHash) {
			throw new HashMismatchException(LocalizationManager.getInstance().get("exception.bytebuffer_hash_block"));
		}

		sourceBuffer.position(sourceBuffer.position() + workingBuffer.position());

		return blockBuffer;
	}
}
