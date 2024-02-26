package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.util.TwiTreeStuff.TwiTreeExpansionListener;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public abstract class MPQFilterableBrowser extends JPanel {

	protected final JTree tree;
	private final DefaultTreeModel treeModel;
	private final Map<String, Icon> iconMap;
	protected MPQTreeNode root;
	private Set<String> mergedListFile;

	protected final MPQFilterHandler filterHandeler;

	protected final TwiTreeExpansionListener expansionListener;
	private final Consumer<String> pathConsumer;

	public MPQFilterableBrowser(Consumer<String> pathConsumer) {
		super(new BorderLayout());
		this.pathConsumer = pathConsumer;
		iconMap = new HashMap<>();

		mergedListFile = GameDataFileSystem.getDefault().getMergedListfile();


		this.filterHandeler = new MPQFilterHandler(this::doUpdateTree);
		addFilters();

		JMenuBar menuBar = getMenuBar();

		root = createMPQTree();
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(createTreeCellRenderer());
		expansionListener = new TwiTreeExpansionListener();
		tree.addTreeExpansionListener(expansionListener);

		add(menuBar, BorderLayout.BEFORE_FIRST_LINE);
		add(new JScrollPane(tree), BorderLayout.CENTER);

		//	private final JFileChooser exportFileChooser;
	}

	protected abstract JMenuBar getMenuBar();

	protected abstract void addFilters();

	protected MPQFilterableBrowser addFilter(String name, String... extensions) {
		filterHandeler.addFilter(name, extensions);
		return this;
	}
	protected MPQFilterableBrowser setUseOtherFilter(boolean useOtherFilter) {
		filterHandeler.setUseOtherFilter(useOtherFilter);
		return this;
	}

	protected void openTreePath(TreePath treePath) {
		if (treePath != null) {
			MPQTreeNode lastPathComponent = (MPQTreeNode) treePath.getLastPathComponent();
			if (lastPathComponent != null && lastPathComponent.isLeaf()) {
				pathConsumer.accept(lastPathComponent.getPath());
			}
		}
	}

	protected MPQFilterableBrowser addTreeKeyListener(KeyListener keyListener) {
		tree.addKeyListener(keyListener);
		return this;
	}

	protected MPQFilterableBrowser addMPQMouseAdapter(MPQMouseAdapter MPQMouseAdapter) {
		tree.addMouseListener(MPQMouseAdapter);
		tree.addMouseMotionListener(MPQMouseAdapter);
		addMouseWheelListener(MPQMouseAdapter);
		return this;
	}

	protected DefaultTreeCellRenderer createTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				if (leaf) {
					String name = value.toString();
					if (name.indexOf('.') > -1) {
						String ext = name.substring(name.lastIndexOf('.'));
						Icon systemIcon = iconMap.get(ext);
						if (systemIcon != null) {
							setIcon(systemIcon);
						}
					}
				} else {
					BufferedImage gameTex;
					if (expanded) {
						gameTex = BLPHandler.getImage("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp");
					} else {
						gameTex = BLPHandler.getImage("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp");
					}
					if (gameTex != null) {
						setIcon(new ImageIcon(gameTex));
					}
				}
				return treeCellRendererComponent;
			}
		};
	}

	// Fill a map with icons to be used. Only fetch Icon if not already in the map.
	private void getIcons(String ext) {
		if (!iconMap.containsKey(ext)) {
			File tempFile = getDummyFile(ext);
			if (tempFile != null) {
				Icon systemIcon = FileSystemView.getFileSystemView().getSystemIcon(tempFile);
				if (systemIcon != null) {
					iconMap.put(ext, systemIcon);
				}
				tempFile.delete();
			}
		}
	}

	private static File getDummyFile(String extension) {
		File dummy = new File(extension);
		if (!dummy.exists()) {
			try {
				if (!dummy.createNewFile()) {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
		}
		return dummy;
	}

	// Go through all nodes recursively and update the visible tag for all based on the current filter setting
	private void checkChildren(MPQTreeNode node) {
		if (0 < node.getTotalChildCount()) {
			List<MPQTreeNode> children = node.getChildren();
			children.addAll(node.getHiddenChildren());

			for (MPQTreeNode child : children) {
				checkChildren(child);
			}

			node.updateChildrenVisibility();
			node.setVisible(node.hasVisibleChildren());
		} else {
			node.setVisible(filterHandeler.isFiltered(node));
		}
	}

	public TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}

//	private void exportItemActionRes() {
//		new FileDialog(this).exportInternalFile(getMouseClickedPath());
//		// TODO batch save? (setSelectedFiles, getSelectedFiles, forEach -> if exists promt: [overwrite/skip/cancel batch save])
//		// TODO make it possible to save as png (technically possible but the encoding is still blp)
//	}

	public void reloadFileSystem() {
		mergedListFile = GameDataFileSystem.getDefault().getMergedListfile();
		root = createMPQTree();
		refreshTree();
	}

	// Refreshed the tree. Not sure if all of this is necessary
	public void refreshTree() {
		root.sort();
		treeModel.setRoot(root);
		treeModel.reload();
		tree.repaint();
		expansionListener.openTree(tree);
	}

	protected void doUpdateTree() {
		System.out.println("do Update tree! " + this.getClass().getSimpleName());
		checkChildren(root);
		refreshTree();
	}

	public MPQTreeNode createMPQTree() {
		MPQTreeNode root = new MPQTreeNode(null, "", "", "");

		List<String> listFile = new ArrayList<>(mergedListFile);
		Collections.sort(listFile);

		for (String string : listFile) {
			String extension = "";
			if (string.indexOf('.') != -1) {
				extension = string.substring(string.lastIndexOf('.'));
				getIcons(extension);
			}

			if (!filterHandeler.isFiltered1(extension)) {
				MPQTreeNode currentNode = root;

				List<String> pathParts = Arrays.asList(string.split("\\\\"));

				for (int i = 0; i < pathParts.size() - 1; i++) {
					String currPathPart = pathParts.get(i);
					MPQTreeNode child = currentNode.getChild(currPathPart);
					if (child == null) {
						String totPa = String.join("\\", pathParts.subList(0, i + 1));
						child = new MPQTreeNode(currentNode, totPa, currPathPart, "");
						currentNode.addChild(currPathPart, child);
					}
					currentNode = child;
				}

				MPQTreeNode leafNode = new MPQTreeNode(currentNode, string, pathParts.get(pathParts.size() - 1), extension);
				currentNode.addChild(pathParts.get(pathParts.size() - 1), leafNode);
			}
		}
		root.sort();
		return root;
	}
}