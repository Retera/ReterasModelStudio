package com.hiveworkshop.blizzard.casc.vfs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public int getPathFragmentCount() {
		return pathFragments.length;
	}

	public byte[] getFragment(final int index) {
		return pathFragments[index];
	}

	@Override
	public String toString() {
		return PathNode.toString(this.pathFragments);
	}

	public static String toString(final byte[][] fragments) {
		return Stream.of(fragments).map(it -> {
			try {
				return new String(it, StandardCharsets.UTF_8);
			} catch (Exception e) {
				return "";
			}
		}).collect(Collectors.joining("/"));
	}
}
