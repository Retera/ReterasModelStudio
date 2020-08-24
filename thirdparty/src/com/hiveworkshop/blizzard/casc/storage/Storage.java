package com.hiveworkshop.blizzard.casc.storage;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.hiveworkshop.blizzard.casc.Key;
import com.hiveworkshop.blizzard.casc.nio.MalformedCASCStructureException;

/**
 * Main data storage of a CASC archive. It consists of index files which point
 * to storage containers in data files.
 */
public class Storage implements AutoCloseable {
	/**
	 * The name of the data folder containing the configuration files.
	 */
	public static final String DATA_FOLDER_NAME = "data";
	
	/**
	 * Number of index files used by a data store.
	 */
	private static final int INDEX_COUNT = 16;

	/**
	 * Usual number of copies of a specific index located in the folder. This is an
	 * estimate only used to increase search performance and will not effect
	 * results.
	 */
	private static final int INDEX_COPIES = 2;
	
	/**
	 * File extension used by storage index files.
	 */
	public static final String INDEX_FILE_EXTENSION = "idx";
	
	/**
	 * File name of data files. 3 character extension is the index.
	 */
	public static final String DATA_FILE_NAME = "data";
	
	/**
	 * Largest permitted data file index.
	 */
	public static final int DATA_FILE_INDEX_MAXIMUM = 999;
	
	/**
	 * Extension length used by data files. Defined by the length needed to store
	 * DATA_FILE_INDEX_MAXIMUM as a decimal string.
	 */
	public static final int DATA_FILE_EXTENSION_LENGTH = 3;

	/**
	 * Converts an encoding key into an index file number.
	 * 
	 * @param encodingKey Input encoding key.
	 * @param keyLength   Length of key to be processed.
	 * @return Index number.
	 */
	public static int getBucketIndex(final byte[] encodingKey, final int keyLength) {
		var accumulator = 0;
		for (var i = 0; i < keyLength; i += 1) {
			accumulator ^= encodingKey[i];
		}
		final var nibbleMask = (1 << 4) - 1;
		return accumulator & nibbleMask ^ accumulator >> 4 & nibbleMask;
	}

	private Path folder;

	private final HashMap<Integer, FileChannel> channelMap = new HashMap<>();
	
	private final IndexFile[] indicies = new IndexFile[INDEX_COUNT];

	/**
	 * Index file versions loaded. Possibly useful for debugging.
	 */
	private final long[] idxVersions = new long[INDEX_COUNT];
	
	/**
	 * Used to track closed status of the store.
	 */
	private boolean closed = false;
	
	private boolean useMemoryMapping;
	
	private int encodingKeyLength;
	
	/**
	 * Construct a storage object from the provided data folder.
	 * <p>
	 * Using memory mapping should give the best performance. However some platforms
	 * or file systems might not support it.
	 * 
	 * @param dataFolder       Path of the CASC data folder.
	 * @param useOld           Use other (old?) version of index files.
	 * @param useMemoryMapping If IO should be memory mapped.
	 * @throws IOException If there was a problem loading from the data folder.
	 */
	public Storage(final Path dataFolder, final boolean useOld, final boolean useMemoryMapping) throws IOException {
		folder = dataFolder.resolve(DATA_FOLDER_NAME);
		this.useMemoryMapping = useMemoryMapping;

		final var indexFiles = new ArrayList<Path>(INDEX_COUNT * INDEX_COPIES);
		try (final var indexFileIterator = Files.newDirectoryStream(folder, "*." + INDEX_FILE_EXTENSION)) {
			for (final var indexFile : indexFileIterator) {
				indexFiles.add(indexFile);
			}
		}
		
		class IndexFileNameMeta {
			private Path filePath;
			private int index;
			private long version;
		}
		
		final var metaMap = new HashMap<Integer, ArrayList<IndexFileNameMeta>>(INDEX_COUNT);
		
		for (final var indexFile : indexFiles) {
			final var fileName = indexFile.getFileName().toString();
			
			final var fileMeta = new IndexFileNameMeta();
			fileMeta.filePath = indexFile;
			fileMeta.index = Integer.parseUnsignedInt(fileName.substring(0, 2), 16);
			fileMeta.version = Long.parseUnsignedLong(fileName.substring(2, 10), 16);
			
			var bucketList = metaMap.get(fileMeta.index);
			if (bucketList == null) {
				bucketList = new ArrayList<>();
				metaMap.put(fileMeta.index, bucketList);
			}
			
			bucketList.add(fileMeta);
		}
		
		Comparator<IndexFileNameMeta> bucketOrder =  (left, right) -> {
			return (int)(left.version - right.version);
		};
		if (!useOld) {
			bucketOrder = Collections.reverseOrder(bucketOrder);
		}
		
		for (var index = 0 ; index < indicies.length ; index+= 1) {
			var bucketList = metaMap.get(index);
			if (bucketList == null) {
				throw new MalformedCASCStructureException("storage index file missing");
			}
			
			Collections.sort(bucketList, bucketOrder);
			
			final var fileMeta = bucketList.get(0);
			idxVersions[index] = fileMeta.version;
			indicies[index] = new IndexFile(loadFileFully(fileMeta.filePath));
		}
		
		// resolve index key length being used
		var index = 0;
		encodingKeyLength = indicies[index++].getEncodingKeyLength();
		for ( ; index < indicies.length ; index+= 1) {
			if (encodingKeyLength != indicies[index].getEncodingKeyLength()) {
				throw new MalformedCASCStructureException("inconsistent encoding key length between index files");
			}
		}
	}
	
	@Override
	public synchronized void close() throws IOException {
		if (closed) {
			return;
		}

		IOException exception = null;
		for (final var channelEntry : channelMap.entrySet()) {
			try {
				channelEntry.getValue().close();
			} catch (IOException e) {
				if (exception != null) {
					exception.addSuppressed(e);
				} else {
					exception = e;
				}
			}
		}

		closed = true;

		if (exception != null) {
			throw new IOException("IOExceptions occured during closure", exception);
		}
	}
	
	public boolean hasBanks(final Key encodingKey) {
		final var bucketIndex = getBucketIndex(encodingKey.getKey(), encodingKeyLength);
		final var index = indicies[bucketIndex];
		final var indexEntry = index.getEntry(encodingKey);
		
		return indexEntry != null;
	}
	
	public BankStream getBanks(final Key encodingKey) throws IOException {
		final var bucketIndex = getBucketIndex(encodingKey.getKey(), encodingKeyLength);
		final var index = indicies[bucketIndex];
		final var indexEntry = index.getEntry(encodingKey);
		
		if (indexEntry == null) {
			throw new FileNotFoundException("encoding key not in store indicies");
		}
		
		final var dataOffset = indexEntry.getDataOffset();
		final var storeIndex = index.getStoreIndex(dataOffset);
		final var storeOffset = index.getStoreOffset(dataOffset);
		
		final var storageBuffer = getStorageBuffer(storeIndex, storeOffset, indexEntry.getFileSize());
		
		return new BankStream(storageBuffer, indexEntry.getKey());
	}

	private synchronized FileChannel getDataFileChannel(final int index) throws IOException {
		if (closed) {
			throw new ClosedChannelException();
		}
		
		var fileChannel = channelMap.get(index);
		if (fileChannel == null) {
			if (index > DATA_FILE_INDEX_MAXIMUM) {
				throw new MalformedCASCStructureException("storage data file index too large");
			}
			
			final var builder = new StringBuilder();
			builder.append(DATA_FILE_NAME);
			builder.append('.');
			final var extensionNumber = Integer.toUnsignedString(index);
			final var extensionZeroCount = DATA_FILE_EXTENSION_LENGTH - extensionNumber.length();
			for (var i = 0 ; i < extensionZeroCount ; i+= 1) {
				builder.append('0');
			}
			builder.append(extensionNumber);

			final var filePath = folder.resolve(builder.toString());
			fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
			channelMap.put(index, fileChannel);
		}
		
		return fileChannel;
	}
	
	/**
	 * Fetch a buffer from storage.
	 * 
	 * @param index  Data file index.
	 * @param offset Data file offset.
	 * @param length Requested buffer length.
	 * @return Storage buffer.
	 * @throws IOException If a problem occurs when preparing the storage buffer.
	 */
	private ByteBuffer getStorageBuffer(final int index, final long offset, final long length) throws IOException {
		final var fileChannel = getDataFileChannel(index);
		if (length > Integer.MAX_VALUE) {
			throw new MalformedCASCStructureException("data buffer too large to process");
		}
		
		final ByteBuffer storageBuffer;
		if (useMemoryMapping) {
			final var mappedBuffer = fileChannel.map(MapMode.READ_ONLY, offset, length);
			mappedBuffer.load();
			storageBuffer = mappedBuffer;
		} else {
			storageBuffer = ByteBuffer.allocate((int)length);
			while (storageBuffer.hasRemaining() && fileChannel.read(storageBuffer, offset + storageBuffer.position()) != -1);
			
			if (storageBuffer.hasRemaining()) {
				throw new EOFException("unexpected end of file");
			}
			storageBuffer.clear();
		}
		
		return storageBuffer;
	}

	/**
	 * Loads a file fully into memory. Memory mapping is used if allowed.
	 * 
	 * @param file Path of file to load into memory.
	 * @return File buffered into memory.
	 * @throws IOException If an IO exception occurs.
	 */
	private ByteBuffer loadFileFully(final Path file) throws IOException {
		final ByteBuffer fileBuffer;
		try (final var channel = FileChannel.open(file, StandardOpenOption.READ)) {
			final var fileLength = channel.size();
			if (fileLength > Integer.MAX_VALUE) {
				throw new MalformedCASCStructureException("file too large to process");
			}
			
			if (useMemoryMapping) {
				final var mappedBuffer = channel.map(MapMode.READ_ONLY, 0, fileLength);
				mappedBuffer.load();
				fileBuffer = mappedBuffer;
			} else {
				fileBuffer = ByteBuffer.allocate((int)fileLength);
				while (fileBuffer.hasRemaining() && channel.read(fileBuffer, fileBuffer.position()) != -1);
				
				if (fileBuffer.hasRemaining()) {
					throw new EOFException("unexpected end of file");
				}
				fileBuffer.clear();
			}
		}
		
		return fileBuffer;
	}

}
