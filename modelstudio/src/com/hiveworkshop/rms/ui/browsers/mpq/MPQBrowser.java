package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.util.Callback;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public final class MPQBrowser extends JPanel {
	private final class MouseAdapterExtension extends MouseAdapter {
		private final JPopupMenu contextMenu;
		private TreePath clickedPath;

		private MouseAdapterExtension(final JPopupMenu contextMenu) {
			this.contextMenu = contextMenu;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			clickedPath = tree.getPathForLocation(e.getX(), e.getY());
			if (SwingUtilities.isRightMouseButton(e)) {
				contextMenu.show(tree, e.getX(), e.getY());
			}
		}

		public TreePath getClickedPath() {
			return clickedPath;
		}
	}

	private final JTree tree;
	//	private final JFileChooser exportFileChooser;
	private final MouseAdapterExtension mouseAdapterExtension;
	private final CompoundDataSource gameDataFileSystem;
	private final DefaultTreeModel treeModel;
	private final Map<String, Icon> iconMap;
	private final Map<String, Filter> extensionToFilter = new HashMap<>();
	private final Map<String, Boolean> currentlyFiltered = new HashMap<>();
	String currentSearch = "";
	MPQTreeNode root;
	Set<String> mergedListFile;
	private List<Filter> filters;
	private Filter otherFilter;
	private JCheckBoxMenuItem checkAll;
	private Set<TreePath> expandedPaths;
	private Set<TreePath> collapsedPaths;

	public MPQBrowser(final CompoundDataSource gameDataFileSystem, final Callback<String> fileOpenCallback,
	                  final Callback<String> useAsTextureCallback) {
		this.gameDataFileSystem = gameDataFileSystem;
		iconMap = new HashMap<>();

		mergedListFile = gameDataFileSystem.getMergedListfile();

		final JMenuBar menuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu("File");
		fileMenu.setEnabled(false);
		menuBar.add(fileMenu);

		final JMenu filtersMenu = new JMenu("Filters");
		filtersMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		menuBar.add(filtersMenu);

		createAndAddFilters(filtersMenu);

		filtersMenu.addSeparator();

		final JMenu searchMenu = new JMenu("Search");
		searchMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		menuBar.add(searchMenu);

		createSearchMenu(searchMenu);

		root = createMPQTree();
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(createTreeCellRenderer());
		tree.addMouseListener(getMouseClickedListener(fileOpenCallback));
		tree.addKeyListener(getKeyListener(fileOpenCallback));
		tree.addTreeExpansionListener(getTreeExpansionListener());
		expandedPaths = new HashSet<>();
		collapsedPaths = new HashSet<>();

		setLayout(new BorderLayout());
		add(menuBar, BorderLayout.BEFORE_FIRST_LINE);
		add(new JScrollPane(tree), BorderLayout.CENTER);

//		exportFileChooser = getFileChooser();

		final JPopupMenu contextMenu = new JPopupMenu();

		final JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(e -> useAsTextureActionRes(fileOpenCallback));
		contextMenu.add(openItem);

		final JMenuItem extractItem = new JMenuItem("Export");
		extractItem.addActionListener(e -> exportItemActionRes(gameDataFileSystem));
		contextMenu.add(extractItem);

		contextMenu.addSeparator();

		final JMenuItem copyPathToClipboardItem = new JMenuItem("Copy Path to Clipboard");
		copyPathToClipboardItem.addActionListener(e -> copyItemPathToClipboard());
		contextMenu.add(copyPathToClipboardItem);

		final JMenuItem useAsTextureItem = new JMenuItem("Use as Texture");
		useAsTextureItem.addActionListener(e -> useAsTextureActionRes(useAsTextureCallback));
		contextMenu.add(useAsTextureItem);

		mouseAdapterExtension = new MouseAdapterExtension(contextMenu);
		tree.addMouseListener(mouseAdapterExtension);
	}

	public void createSearchMenu(JMenu searchMenu) {
		searchMenu.getPopupMenu().setLayout(new MigLayout());

		final JTextField searchField = new JTextField();
		Dimension prefSize = searchField.getPreferredSize();
		prefSize.width = 100;
		searchField.setMinimumSize(prefSize);
		searchField.setPreferredSize(prefSize);
		searchField.addKeyListener(getSearchOnEnter(searchField));
//		searchMenu.add(searchField, "growx, span 2, wrap"); // this put the search box on the menu bar
		searchMenu.getPopupMenu().add(searchField, "growx, span 2, wrap");

		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> searchFilter(searchField.getText()));
		searchMenu.getPopupMenu().add(searchButton, "growx");

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> {
			searchFilter("");
			searchField.setText("");
		});
		searchMenu.getPopupMenu().add(clearButton, "growx");
	}

	public KeyAdapter getSearchOnEnter(JTextField searchField) {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keyCode: " + e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchFilter(searchField.getText());
				}
			}
		};
	}

	private static File getDummyFile(final String extension) {
		final File dummy = new File(extension);
		if (!dummy.exists()) {
			try {
				if (!dummy.createNewFile()) {
					return null;
				}
			} catch (final IOException e) {
				return null;
			}
		}
		return dummy;
	}

	private void createAndAddFilters(JMenu filtersMenu) {
		filters = new ArrayList<>();
		filters.add(new Filter("Text", new String[] {".txt"}));
		filters.add(new Filter("Sylk", new String[] {".slk"}));
		filters.add(new Filter("Script", new String[] {".ai", ".wai", ".j", ".js", ".pld"}));
		filters.add(new Filter("Html", new String[] {".htm", ".html"}));
		filters.add(new Filter("Models", new String[] {".mdl", ".mdx"}));
		filters.add(new Filter("Images", new String[] {".bmp", ".tga", ".jpg", ".jpeg", ".pcx", ".blp", ".dds"}));
		filters.add(new Filter("Maps", new String[] {".w3m", ".w3x", ".w3n"}));
		filters.add(new Filter("Sounds", new String[] {".wav"}));
		filters.add(new Filter("Music", new String[] {".mp3", ".mid"}));
		otherFilter = new Filter("Other", true);
		filters.add(otherFilter);

		for (final Filter filter : filters) {
			filtersMenu.add(filter.getFilterCheckBoxItem());
			filter.addActionListener(e -> setFilteredExtensions(filter.extensions, filter.filterCheckBoxItem.getState()));

			filter.getFilterCheckBoxItem().putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);

			for (final String ext : filter.extensions) {
				extensionToFilter.put(ext, filter);
				currentlyFiltered.put(ext, true);
			}
		}

		checkAll = new JCheckBoxMenuItem("All", true);
		checkAll.addActionListener(e -> setFiltered(checkAll.getState()));
		checkAll.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		filtersMenu.add(checkAll);
	}

	private MouseAdapter getMouseClickedListener(Callback<String> fileOpenCallback) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
					openTreePath(treePath, fileOpenCallback);
				}
			}
		};
	}

	private void openTreePath(TreePath treePath, Callback<String> fileOpenCallback) {
		if (treePath != null) {
			final MPQTreeNode lastPathComponent = (MPQTreeNode) treePath.getLastPathComponent();
			if (lastPathComponent != null && lastPathComponent.isLeaf()) {
				fileOpenCallback.run(lastPathComponent.getPath());
			}
		}
	}

	private DefaultTreeCellRenderer createTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
			                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				final Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				if (leaf) {
					final String name = value.toString();
					if (name.indexOf('.') > -1) {
						final String ext = name.substring(name.lastIndexOf('.'));
						Icon systemIcon = iconMap.get(ext);
						if (systemIcon != null) {
							setIcon(systemIcon);
						}
					}
				} else {
					if (expanded) {
						setIcon(new ImageIcon(BLPHandler.get()
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
					} else {
						setIcon(new ImageIcon(BLPHandler.get()
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
					}
				}
				return treeCellRendererComponent;
			}
		};
	}

	private KeyListener getKeyListener(Callback<String> fileOpenCallback) {
		return new KeyListener() {
			boolean stillPressed = false;

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !stillPressed) {
					stillPressed = true;
					TreePath treePath = tree.getSelectionPath();
					openTreePath(treePath, fileOpenCallback);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && stillPressed) {
					stillPressed = false;
				}
			}
		};
	}

	// Fill a map with icons to be used. Only fetch Icon if not already in the map.
	private void getIcons(String ext) {
		if (!iconMap.containsKey(ext)) {
			final File tempFile = getDummyFile(ext);
			if (tempFile != null) {
				final Icon systemIcon = FileSystemView.getFileSystemView().getSystemIcon(tempFile);
				if (systemIcon != null) {
					iconMap.put(ext, systemIcon);
				}
				tempFile.delete();
			}
		}
	}

	// Set the all the filter checkboxes to the value of the "All" checkbox and updates the filter map
	private void setFiltered(boolean b) {
		for (final Filter filter : filters) {
			filter.getFilterCheckBoxItem().setSelected(b);
			setFilteredExtensions(filter.extensions, b);
		}
		refreshTree();
	}

	// Go through all nodes recursively and update the visible tag for all based on the current filter setting
	private void checkChildren(MPQTreeNode node) {
		if (node.getTotalChildCount() > 0) {
			List<MPQTreeNode> children = node.getChildren();
			children.addAll(node.getHiddenChildren());

			for (MPQTreeNode child : children) {
				checkChildren(child);
			}

			node.updateChildrenVisibility();
			node.setVisible(node.hasVisibleChildren());
		} else {
			Boolean filtered = currentlyFiltered.get(node.getExtension());
			boolean fitsSearch = (currentSearch.equals("") || (node.isLeaf() && node.getSubPathName().contains(currentSearch)));
//			node.setVisible(filtered != null && filtered
//					|| filtered == null && otherFilter.getFilterCheckBoxItem().isSelected());
			node.setVisible(filtered != null && filtered && fitsSearch
					|| filtered == null && otherFilter.getFilterCheckBoxItem().isSelected() && fitsSearch);
		}
	}

	private void copyItemPathToClipboard() {
		final MPQTreeNode clickedNode = ((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent());
		final StringSelection selection = new StringSelection(clickedNode.getPath());
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

	private void useAsTextureActionRes(Callback<String> useAsTextureCallback) {
		useAsTextureCallback.run(((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent()).getPath());
	}

	private void exportItemActionRes(CompoundDataSource gameDataFileSystem) {
		final MPQTreeNode clickedNode = ((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent());
		com.hiveworkshop.rms.ui.application.FileDialog fileDialog = new com.hiveworkshop.rms.ui.application.FileDialog(this);

		fileDialog.exportInternalFile(clickedNode.getPath());
		// TODO batch save? (setSelectedFiles, getSelectedFiles, forEach -> if exists promt: [overwrite/skip/cancel batch save])
		// TODO make it possible to save as png (technically possible but the encoding is still blp)
	}



	// Refreshed the tree. Not sure if all of this is necessary
	public void refreshTree() {
		root.sort();
		treeModel.setRoot(root);
		treeModel.reload();
		tree.repaint();
		openTree();
	}

	// Updates the map used to look up filtered extensions,
	// sets the "All" checkbox to match if all checkboxes
	// and checked and refreshes the tree
	public void setFilteredExtensions(String[] extensions, boolean filtered) {
		for (String extension : extensions) {
			currentlyFiltered.put(extension, filtered);
		}
		checkAll.setState(!currentlyFiltered.containsValue(false));
		checkChildren(root);
		refreshTree();
	}

	private void searchFilter(String searchText) {
		currentSearch = searchText;
		checkChildren(root);
		refreshTree();
	}

	public MPQTreeNode createMPQTree() {
		final MPQTreeNode root = new MPQTreeNode(null, "", "", "");

		final List<String> listFile = new ArrayList<>(mergedListFile);
		Collections.sort(listFile);

		for (String string : listFile) {
			String extension = "";
			if (string.indexOf('.') != -1) {
				extension = string.substring(string.lastIndexOf('.'));
				getIcons(extension);
			}
			if ((currentlyFiltered.get(extension) == null && !otherFilter.getFilterCheckBoxItem().isSelected()) ||
					(currentlyFiltered.get(extension) != null && !currentlyFiltered.get(extension))) {
				continue;
			}
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

			final MPQTreeNode leafNode = new MPQTreeNode(currentNode, string, pathParts.get(pathParts.size() - 1), extension);
			currentNode.addChild(pathParts.get(pathParts.size() - 1), leafNode);
		}
		root.sort();
		return root;
	}

	private static final class Filter {
		private final String[] extensions;
		private final String name;
		private final JCheckBoxMenuItem filterCheckBoxItem;
		private boolean isOtherFilter;

		public Filter(final String name, final String[] extensions) {
			this.name = name;
			this.extensions = extensions;
			filterCheckBoxItem = new JCheckBoxMenuItem(getDescription(), true);
		}

		public Filter(final String name, final boolean isOtherFilter) {
			this.name = name;
			this.isOtherFilter = isOtherFilter;
			extensions = new String[] {};
			filterCheckBoxItem = new JCheckBoxMenuItem(name, true);
		}

		public boolean passes(final String path) {
			for (final String extension : extensions) {
				if (path.endsWith(extension)) {
					return true;
				}
			}
			return false;
		}

		public boolean isOtherFilter() {
			return isOtherFilter;
		}

		public String getDescription() {
			final StringBuilder descBuilder = new StringBuilder(name);
			descBuilder.append(" (");
			if (extensions.length > 0) {
				descBuilder.append("*");
				descBuilder.append(extensions[0]);
				for (int i = 1; i < extensions.length; i++) {
					descBuilder.append(", *");
					descBuilder.append(extensions[i]);
				}
			}
			descBuilder.append(")");
			return descBuilder.toString();
		}

		public void addItemListener(final ItemListener itemListener) {
			filterCheckBoxItem.addItemListener(itemListener);
		}

		public void addActionListener(final ActionListener listener) {
			filterCheckBoxItem.addActionListener(listener);
		}

		public JCheckBoxMenuItem getFilterCheckBoxItem() {
			return filterCheckBoxItem;
		}
	}

	private TreeExpansionListener getTreeExpansionListener() {
		return new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				expandedPaths.add(event.getPath());
				collapsedPaths.remove(event.getPath());
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				expandedPaths.remove(event.getPath());
				collapsedPaths.add(event.getPath());
//				System.out.println("event path: " + event.getPath());
			}
		};
	}

	private void openTree() {
		if (!expandedPaths.isEmpty()) {
			for (TreePath path : collapsedPaths) {
				expandedPaths.removeIf(path::isDescendant);
			}
			collapsedPaths.clear();
//			System.out.println(expandedPaths.size() + " paths in list 2");
//			System.out.println(Arrays.toString(expandedPaths.toArray(new TreePath[0])));

			for (TreePath treePath : expandedPaths) {
				try {
					tree.makeVisible(treePath);
					if (tree.isVisible(treePath) && tree.isCollapsed(treePath)) {
						tree.expandPath(treePath);
					}
				} catch (Exception e) {
					System.out.println("faild on: " + treePath);
				}
			}
		}
	}
}