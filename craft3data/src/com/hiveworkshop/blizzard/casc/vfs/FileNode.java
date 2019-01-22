package com.hiveworkshop.blizzard.casc.vfs;

import java.io.PrintStream;
import java.util.List;

/**
 * A file system node containing a logical file.
 */
public class FileNode extends PathNode {
	private final StorageReference[] references;

	protected FileNode(final List<byte[]> pathFragments, final List<StorageReference> references) {
		super(pathFragments);
		this.references = references.toArray(new StorageReference[0]);
	}

	@Override
	protected void printPaths(final PrintStream out, final String pathPrefix) {
		final String pathString = getPathString(pathPrefix);
		for (final StorageReference reference : references) {
			out.println(pathString + " : " + reference);
		}
	}

	int getFileReferenceCount() {
		return references.length;
	}

	StorageReference getFileReference(final int index) {
		return references[index];
	}

}
