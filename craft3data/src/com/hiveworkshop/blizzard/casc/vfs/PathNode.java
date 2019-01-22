package com.hiveworkshop.blizzard.casc.vfs;

import java.io.PrintStream;
import java.util.List;

import com.hiveworkshop.ReteraCASCUtils;

/**
 * Represents a path node. Path nodes can either be prefix nodes or file nodes.
 */
public abstract class PathNode {
	/**
	 * Array of path fragments. Each fragment represents part of a path string. Due
	 * to the potential for multi byte encoding, one cannot assume that each
	 * fragment can be assembled into a valid string.
	 */
	private final byte[][] pathFragments;

	protected PathNode(final List<byte[]> pathFragments) {
		this.pathFragments = pathFragments.toArray(new byte[0][]);
	}

	public void printPaths(final PrintStream out) {
		printPaths(out, "");
	}

	protected String getPathString(final String path) {
		// final var pathAdd = String.join("\\", pathFragments);
		// return path + pathAdd;
		return path;
	}

	protected abstract void printPaths(final PrintStream out, final String pathPrefix);

	int matchFragment(final byte[] pathFragment, final int fragmentOffset, final int requestedFragmentNumber) {
		if (pathFragments.length == 0) {
			return 0;
		}
		final byte[] requestedFragment = pathFragments[requestedFragmentNumber];
		if (ReteraCASCUtils.arraysEquals(pathFragment, fragmentOffset, fragmentOffset + requestedFragment.length,
				requestedFragment, 0, requestedFragment.length)) {
			return requestedFragment.length;
		}
		return -1;
	}

	int getPathFragmentCount() {
		return pathFragments.length;
	}

	byte[] getFragment(final int index) {
		return pathFragments[index];
	}
}
