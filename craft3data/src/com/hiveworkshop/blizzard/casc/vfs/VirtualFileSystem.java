package com.hiveworkshop.blizzard.casc.vfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.hiveworkshop.blizzard.casc.Key;
import com.hiveworkshop.blizzard.casc.StorageReference;
import com.hiveworkshop.blizzard.casc.nio.MalformedCASCStructureException;
import com.hiveworkshop.blizzard.casc.storage.BankStream;
import com.hiveworkshop.blizzard.casc.storage.Storage;

/**
 * High level file system API using TVFS directories to extract files from a
 * store.
 */
public final class VirtualFileSystem {
	/**
	 * VFS storage reference key prefix.
	 */
	public static final String CONFIGURATION_KEY_PREFIX = "vfs-";

	/**
	 * Root VFS storage reference.
	 */
	public static final String ROOT_KEY = "root";

	/**
	 * Character encoding used internally by file paths.
	 */
	public static final Charset PATH_ENCODING = Charset.forName("UTF8");

	/**
	 * Path separator used by path strings.
	 */
	public static final String PATH_SEPERATOR = "\\";

	/**
	 * Convert a path string into path fragments for resolution in the VFS.
	 *
	 * @param filePath Path string to convert.
	 * @return Path fragments.
	 * @throws CharacterCodingException If the path string cannot be encoded into
	 *                                  fragments.
	 */
	public static byte[][] convertFilePath(final String filePath) throws CharacterCodingException {
		final String[] fragmentStrings = filePath.toLowerCase(Locale.ROOT).split(PATH_SEPERATOR);
		final byte[][] pathFragments = new byte[fragmentStrings.length][];

		final CharsetEncoder encoder = PATH_ENCODING.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		for (int index = 0; index < fragmentStrings.length; index += 1) {
			pathFragments[index] = encoder.encode(CharBuffer.wrap(fragmentStrings[index])).array();
		}

		return pathFragments;
	}

	/**
	 * Convert path fragments used internally by VFS into a path string.
	 *
	 * @param pathFragments Path fragments to convert.
	 * @return Path string.
	 * @throws CharacterCodingException If the path fragments cannot be decoded into
	 *                                  a valid String.
	 */
	public static String convertPathFragments(final byte[][] pathFragments) throws CharacterCodingException {
		final String[] fragmentStrings = new String[pathFragments.length];

		final CharsetDecoder decoder = PATH_ENCODING.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

		for (int index = 0; index < fragmentStrings.length; index += 1) {
			fragmentStrings[index] = decoder.decode(ByteBuffer.wrap(pathFragments[index])).toString();
		}

		return String.join(PATH_SEPERATOR, fragmentStrings);
	}

	private final Storage storage;
	private final TVFSDecoder decoder = new TVFSDecoder();
	private final TVFSFile tvfsRoot;
	private final TreeMap<Key, TVFSFile> tvfsCache = new TreeMap<>();
	private final TreeMap<Key, StorageReference> tvfsStorageReferences = new TreeMap<>();

	private TVFSFile resolveTVFS(final Key encodingKey) throws IOException {
		TVFSFile tvfsFile = null;
		final StorageReference storageReference = tvfsStorageReferences.get(encodingKey);
		if (storageReference != null) {
			synchronized (this) {
				tvfsFile = tvfsCache.get(encodingKey);
				if (tvfsFile == null) {
					final ByteBuffer rootBuffer = fetchStoredBuffer(storageReference);
					tvfsFile = decoder.loadFile(rootBuffer);

					tvfsCache.put(storageReference.getEncodingKey(), tvfsFile);
				}
			}
		}
		return tvfsFile;
	}

	private void recursiveFilePathRetrieve(final byte[][] parentPathFragments,
			final ArrayList<PathResult> pathStringList, final PathNode currentNode) throws IOException {
		byte[][] currentPathFragments = parentPathFragments;

		// append node fragments
		final int fragmentCount = currentNode.getPathFragmentCount();
		if (fragmentCount > 0) {
			final int basePathFragmentsIndex = currentPathFragments.length - 1;
			currentPathFragments = Arrays.copyOf(currentPathFragments, fragmentCount - 1 + currentPathFragments.length);

			final byte[] sourceFragment = currentPathFragments[basePathFragmentsIndex];
			int fragmentIndex = 0;
			final byte[] fragment = currentNode.getFragment(fragmentIndex++);
			final int joinOffset = sourceFragment.length;
			int newFragmentLength = joinOffset + fragment.length;
			int joinFragmentOffset = joinOffset;
			final boolean fragmentNeedsSlashHack = (sourceFragment.length == 0 || fragment.length == 0)
					&& !(sourceFragment.length == 0 && fragment.length == 0)
					&& (sourceFragment.length == 0 || sourceFragment[sourceFragment.length - 1] != (byte) '\\')
					&& (fragment.length == 0 || fragment[0] != (byte) '\\');
			if (fragmentNeedsSlashHack) {
				newFragmentLength++;
				joinFragmentOffset++;
			}
			final byte[] joinedFragment = Arrays.copyOf(sourceFragment, newFragmentLength);
			System.arraycopy(fragment, 0, joinedFragment, joinFragmentOffset, fragment.length);
			if (fragmentNeedsSlashHack) {
				joinedFragment[joinOffset] = (byte) '\\';
			}

			currentPathFragments[basePathFragmentsIndex] = joinedFragment;

			for (; fragmentIndex < fragmentCount; fragmentIndex += 1) {
				currentPathFragments[basePathFragmentsIndex + fragmentIndex] = currentNode.getFragment(fragmentIndex);
			}
		}

		if (currentNode instanceof PrefixNode) {
			final PrefixNode prefixNode = (PrefixNode) currentNode;

			final int childCount = prefixNode.getNodeCount();
			for (int index = 0; index < childCount; index += 1) {
				recursiveFilePathRetrieve(currentPathFragments, pathStringList, prefixNode.getNode(index));
			}
		} else if (currentNode instanceof FileNode) {
			final FileNode fileNode = (FileNode) currentNode;

			final int fileReferenceCount = fileNode.getFileReferenceCount();
			if (fileReferenceCount == 1) {
				// check if nested VFS
				final Key encodingKey = fileNode.getFileReference(0).getEncodingKey();
				final TVFSFile tvfsFile = resolveTVFS(encodingKey);

				if (tvfsFile != null) {
					// file is also a folder
					final byte[][] folderPathFragments = Arrays.copyOf(currentPathFragments,
							currentPathFragments.length + 1);
					folderPathFragments[currentPathFragments.length] = new byte[0];

					final int rootCount = tvfsFile.getRootNodeCount();
					for (int rootIndex = 0; rootIndex < rootCount; rootIndex += 1) {
						final PathNode root = tvfsFile.getRootNode(rootIndex);
						recursiveFilePathRetrieve(folderPathFragments, pathStringList, root);
					}
				}

				pathStringList.add(new PathResult(currentNode, currentPathFragments));
			}
		} else {
			throw new IllegalArgumentException("unsupported node type");
		}
	}

	/**
	 * Method to recursively get all file paths in this file system.
	 *
	 * @return List of file path strings.
	 * @throws IOException If an exception is thrown when loading a TVFS file or
	 *                     decoding path fragments into a path string.
	 */
	public List<PathResult> getAllFilePaths() throws IOException {
		final ArrayList<PathResult> pathStringList = new ArrayList<PathResult>();

		final int rootCount = tvfsRoot.getRootNodeCount();
		for (int rootIndex = 0; rootIndex < rootCount; rootIndex += 1) {
			final PathNode root = tvfsRoot.getRootNode(rootIndex);
			recursiveFilePathRetrieve(new byte[1][0], pathStringList, root);
		}

		return pathStringList;
	}

	private PathNode resolvePathNode(final byte[][] pathFragments) throws IOException {
		final int fragmentIndex = 0;
		final int fragmentOffset = 0;

		return null;
	}

	public VirtualFileSystem(final Storage storage, final Map<String, String> buildConfiguration) throws IOException {
		this.storage = storage;

		int vfsNumber = 0;
		String configurationKey;
		while (buildConfiguration
				.containsKey(configurationKey = CONFIGURATION_KEY_PREFIX + Integer.toUnsignedString(++vfsNumber))) {
			final StorageReference storageReference = new StorageReference(configurationKey, buildConfiguration);
			tvfsStorageReferences.put(storageReference.getEncodingKey(), storageReference);
		}

		final StorageReference rootReference = new StorageReference(CONFIGURATION_KEY_PREFIX + ROOT_KEY,
				buildConfiguration);
		final ByteBuffer rootBuffer = fetchStoredBuffer(rootReference);
		tvfsRoot = decoder.loadFile(rootBuffer);

		tvfsCache.put(rootReference.getEncodingKey(), tvfsRoot);
	}

	private ByteBuffer fetchStoredBuffer(final StorageReference storageReference) throws IOException {
		final long size = storageReference.getSize();
		if (size > Integer.MAX_VALUE) {
			throw new MalformedCASCStructureException("stored data too large to process");
		}

		final BankStream bankStream = storage.getBanks(storageReference.getEncodingKey());
		final ByteBuffer storedBuffer = ByteBuffer.allocate((int) size);
		try {
			while (bankStream.hasNextBank()) {
				bankStream.getBank(storedBuffer);
			}
		} catch (final BufferOverflowException e) {
			throw new MalformedCASCStructureException("stored data is bigger than expected");
		}

		if (storedBuffer.hasRemaining()) {
			throw new MalformedCASCStructureException("stored data is smaller than expected");
		}

		storedBuffer.rewind();
		return storedBuffer;
	}

	public final class PathResult {
		final PathNode node;
		final byte[][] pathFragments;

		/**
		 * Internal constructor for path results.
		 *
		 * @param node          Resolved node.
		 * @param pathFragments Path of resolved node.
		 */
		private PathResult(final PathNode node, final byte[][] pathFragments) {
			this.node = node;
			this.pathFragments = pathFragments;
		}

		public boolean isFile() {
			return node instanceof FileNode;
		}

		/**
		 * Get the size of the file in bytes.
		 * <p>
		 * If this result is not a file a value of 0 is returned.
		 *
		 * @return File size in bytes.
		 */
		public long getFileSize() {
			long size = 0L;

			if (isFile()) {
				final FileNode fileNode = (FileNode) node;
				final int fileReferenceCount = fileNode.getFileReferenceCount();
				for (int fileReferenceIndex = 0; fileReferenceIndex < fileReferenceCount; fileReferenceIndex += 1) {
					final com.hiveworkshop.blizzard.casc.vfs.StorageReference fileReference = fileNode
							.getFileReference(fileReferenceIndex);
					size = Math.max(size, fileReference.getOffset() + fileReference.getSize());
				}
			}

			return size;
		}

		/**
		 * Returns true if this file completely exists in storage.
		 * <p>
		 * The virtual file system structure lists all files, even ones that may not be
		 * in storage. Only files that are in storage can have their file buffer read.
		 * <p>
		 * If this result is not a file then it exists in storage as it has no storage
		 * footprint.
		 *
		 * @return True if the file exists in storage.
		 */
		public boolean existsInStorage() {
			boolean exists = true;

			if (isFile()) {
				final FileNode fileNode = (FileNode) node;
				final int fileReferenceCount = fileNode.getFileReferenceCount();
				for (int fileReferenceIndex = 0; fileReferenceIndex < fileReferenceCount; fileReferenceIndex += 1) {
					final com.hiveworkshop.blizzard.casc.vfs.StorageReference fileReference = fileNode
							.getFileReference(fileReferenceIndex);
					exists = exists && storage.hasBanks(fileReference.getEncodingKey());
				}
			}

			return exists;
		}

		/**
		 * Returns if this path result represents a TVFS file node used by this file
		 * system.
		 * <p>
		 * Such nodes logically act as folders in the file path but also contain file
		 * data used by this file system. Such behaviour may be incompatible with
		 * standard file systems which do not support both a folder and file at the same
		 * path.
		 * <p>
		 * Results that are not files cannot be a TVFS file.
		 *
		 * @return If this node is a TVFS file used by this file system.
		 */
		public boolean isTVFS() {
			if (!isFile()) {
				return false;
			}

			final FileNode fileNode = (FileNode) node;
			final com.hiveworkshop.blizzard.casc.vfs.StorageReference fileReference = fileNode.getFileReference(0);
			return tvfsStorageReferences.containsKey(fileReference.getEncodingKey());
		}

		/**
		 * Fully read this file into the specified destination buffer. If no buffer is
		 * specified a new one will be allocated.
		 * <p>
		 * The specified buffer must have at least getFileSize bytes remaining.
		 *
		 * @param destBuffer Buffer to be written to.
		 * @return Buffer that was written to.
		 * @throws IOException      If an error occurs during reading.
		 * @throws OutOfMemoryError If no buffer is specified and the file is too big
		 *                          for a single buffer.
		 */
		public ByteBuffer readFile(ByteBuffer destBuffer) throws IOException {
			if (!isFile()) {
				throw new FileNotFoundException("result is not a file");
			}

			final long fileSize = getFileSize();
			if (fileSize > Integer.MAX_VALUE) {
				throw new OutOfMemoryError("file too big to process");
			}

			if (destBuffer == null) {
				destBuffer = ByteBuffer.allocate((int) fileSize);
			} else if (destBuffer.remaining() < fileSize) {
				throw new BufferOverflowException();
			}

			final ByteBuffer fileBuffer = destBuffer.slice();

			final FileNode fileNode = (FileNode) node;
			final int fileReferenceCount = fileNode.getFileReferenceCount();
			for (int fileReferenceIndex = 0; fileReferenceIndex < fileReferenceCount; fileReferenceIndex += 1) {
				final com.hiveworkshop.blizzard.casc.vfs.StorageReference fileReference = fileNode
						.getFileReference(fileReferenceIndex);

				final long logicalSize = fileReference.getSize();
				if (logicalSize != fileReference.getActualSize()) {
					throw new MalformedCASCStructureException("inconsistent size");
				}
				final long logicalOffset = fileReference.getOffset();

				final BankStream bankStream = storage.getBanks(fileReference.getEncodingKey());
				// TODO test if compressed and logical sizes match stored sizes.

				fileBuffer.limit((int) (logicalOffset + logicalSize));
				fileBuffer.position((int) logicalOffset);
				while (bankStream.hasNextBank()) {
					bankStream.getBank(fileBuffer);
				}
			}

			destBuffer.position(destBuffer.position() + (int) fileSize);
			return destBuffer;
		}

		public byte[][] getPathFragments() {
			return pathFragments;
		}

		public String getPath() throws CharacterCodingException {
			return convertPathFragments(pathFragments);
		}
	}
}
