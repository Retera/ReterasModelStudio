package com.hiveworkshop.blizzard.casc.vfs;

import java.io.PrintStream;
import java.util.List;

/**
 * Prefix nodes generate a path prefix for other nodes.
 */
public class PrefixNode extends PathNode {
	/**
	 * Array of child node that this node forms a prefix of.
	 */
	private final PathNode[] nodes;

	protected PrefixNode(final List<byte[]> pathFragments, final List<PathNode> nodes) {
		super(pathFragments);
		this.nodes = nodes.toArray(new PathNode[0]);
	}

	@Override
	protected void printPaths(final PrintStream out, final String pathPrefix) {
		final String pathString = getPathString(pathPrefix);
		for (final PathNode node : nodes) {
			node.printPaths(out, pathString);
		}
	}

	int getNodeCount() {
		return nodes.length;
	}

	PathNode getNode(final int index) {
		return nodes[index];
	}
}
