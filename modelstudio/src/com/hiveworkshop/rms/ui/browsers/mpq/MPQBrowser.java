package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.util.SklViewer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public final class MPQBrowser extends JPanel {

	private final JTree tree;
	private final DefaultTreeModel treeModel;
	private final Map<String, Icon> iconMap;
	private final Map<String, Filter> extensionToFilter = new HashMap<>();
	private final Map<String, Boolean> currentlyFiltered = new HashMap<>();
	private String currentSearch = "";
	private MPQTreeNode root;
	private Set<String> mergedListFile;
	private List<Filter> filters;
	private Filter otherFilter;
	private JCheckBoxMenuItem checkAll;
	private final Set<TreePath> expandedPaths;
	private final Set<TreePath> collapsedPaths;

	public MPQBrowser() {
		iconMap = new HashMap<>();

		mergedListFile = GameDataFileSystem.getDefault().getMergedListfile();

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setEnabled(false);
		menuBar.add(fileMenu);

		menuBar.add(createAndAddFilters());
		menuBar.add(getSearchMenu());

		root = createMPQTree();
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(createTreeCellRenderer());
		tree.addKeyListener(getKeyListener());
		tree.addTreeExpansionListener(getTreeExpansionListener());
		expandedPaths = new HashSet<>();
		collapsedPaths = new HashSet<>();

		setLayout(new BorderLayout());
		add(menuBar, BorderLayout.BEFORE_FIRST_LINE);
		add(new JScrollPane(tree), BorderLayout.CENTER);

		//	private final JFileChooser exportFileChooser;
		MouseAdapterExtension mouseAdapterExtension = new MouseAdapterExtension(this, tree);
		tree.addMouseListener(mouseAdapterExtension);
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

	private JMenu getSearchMenu() {
		JMenu searchMenu = new JMenu("Search");
		searchMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		searchMenu.getPopupMenu().setLayout(new MigLayout());

		JTextField searchField = new JTextField();
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
		return searchMenu;
	}

	private JMenu createAndAddFilters() {
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

		JMenu filtersMenu = new JMenu("Filters");
		filtersMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		for (Filter filter : filters) {
			filtersMenu.add(filter.getFilterCheckBoxItem());
			filter.addActionListener(e -> setFilteredExtensions(filter.getExtensions(), filter.getFilterState()));

			filter.getFilterCheckBoxItem().putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);

			for (String ext : filter.getExtensions()) {
				extensionToFilter.put(ext, filter);
				currentlyFiltered.put(ext, true);
			}
		}

		checkAll = new JCheckBoxMenuItem("All", true);
		checkAll.addActionListener(e -> setFiltered(checkAll.getState()));
		checkAll.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		filtersMenu.add(checkAll);
		return filtersMenu;
	}

	protected void openTreePath(TreePath treePath) {
		if (treePath != null) {
			MPQTreeNode lastPathComponent = (MPQTreeNode) treePath.getLastPathComponent();
			if (lastPathComponent != null && lastPathComponent.isLeaf()) {
				loadFileByType(lastPathComponent.getPath());
			}
		}
	}

	private DefaultTreeCellRenderer createTreeCellRenderer() {
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
					if (expanded) {
						setIcon(new ImageIcon(BLPHandler
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
					} else {
						setIcon(new ImageIcon(BLPHandler
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
					}
				}
				return treeCellRendererComponent;
			}
		};
	}

	private KeyListener getKeyListener() {
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
					openTreePath(treePath);
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

	// Set the all the filter checkboxes to the value of the "All" checkbox and updates the filter map
	private void setFiltered(boolean b) {
		for (Filter filter : filters) {
			filter.getFilterCheckBoxItem().setSelected(b);
			setFilteredExtensions(filter.getExtensions(), b);
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
			boolean fitsSearch = (currentSearch.equals("")
					|| (node.isLeaf()
					&& node.getSubPathName().toLowerCase(Locale.ROOT).contains(currentSearch.toLowerCase(Locale.ROOT))));
			node.setVisible(filtered != null && filtered && fitsSearch
					|| filtered == null && otherFilter.getFilterCheckBoxItem().isSelected() && fitsSearch);
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
		MPQTreeNode root = new MPQTreeNode(null, "", "", "");

		List<String> listFile = new ArrayList<>(mergedListFile);
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

			MPQTreeNode leafNode = new MPQTreeNode(currentNode, string, pathParts.get(pathParts.size() - 1), extension);
			currentNode.addChild(pathParts.get(pathParts.size() - 1), leafNode);
		}
		root.sort();
		return root;
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


	private static void loadFileByType(String filepath) {
		System.out.println("loading file");
		ModelLoader.loadFile(GameDataFileSystem.getDefault().getFile(filepath), true);
		System.out.println("File path: \"" + filepath + "\"");
		if(filepath.endsWith(".skl")){
			System.out.println("opening frame?");
			new SklViewer().createAndShowHTMLPanel(filepath, "View SKL");
		}
	}
}