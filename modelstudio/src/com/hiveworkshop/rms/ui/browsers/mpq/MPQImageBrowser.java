package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class MPQImageBrowser extends JPanel {

	private final JTree tree;
	private final DefaultTreeModel treeModel;
	private final Map<String, Icon> iconMap;
	private String currentSearch = "";
	private MPQTreeNode root;
	private Set<String> mergedListFile;
	private final Set<TreePath> expandedPaths;
	private final Set<TreePath> collapsedPaths;

	private String selectedPath;

	private ZoomableImagePreviewPanel comp;

	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;

	public MPQImageBrowser() {
		super(new MigLayout("fill", "[][grow]", "[grow]"));
		iconMap = new HashMap<>();

		mergedListFile = GameDataFileSystem.getDefault().getMergedListfile();
		String imageRegexFilter = ".+(\\.bmp|\\.tga|\\.jpg|\\.jpeg|\\.pcx|\\.blp|\\.dds)";
		mergedListFile.removeIf(o -> !o.matches(imageRegexFilter));
		fetchImageIcons();

		JMenuBar menuBar = new JMenuBar();
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

		JPanel leftPanel = new JPanel(new BorderLayout());
//		setLayout(new BorderLayout());
		leftPanel.add(menuBar, BorderLayout.BEFORE_FIRST_LINE);
		leftPanel.add(new JScrollPane(tree), BorderLayout.CENTER);
		add(leftPanel, "growy");


		JPanel rightPanel = new JPanel(new MigLayout("fill", "[]", "[grow][]"));
		rightPanel.add(getImageViewerPanel(), "growx, growy, wrap");

		TwiComboBox<ImageUtils.ColorMode> colorModeBox = getColorModeBox();
		rightPanel.add(colorModeBox);
		add(rightPanel, "growx, growy");

		//	private final JFileChooser exportFileChooser;
		MPQImageMouseAdapter mouseAdapterExtension = new MPQImageMouseAdapter(this, tree);
		tree.addMouseListener(mouseAdapterExtension);
	}


	private JPanel getImageViewerPanel() {
		JPanel imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
		return imageViewerPanel;
	}


	private TwiComboBox<ImageUtils.ColorMode> getColorModeBox() {
		TwiComboBox<ImageUtils.ColorMode> colorModeGroup = new TwiComboBox<>(ImageUtils.ColorMode.values(), ImageUtils.ColorMode.GREEN_GREEN);
		colorModeGroup.addOnSelectItemListener(this::setColorMode);
		colorModeGroup.selectOrFirst(ImageUtils.ColorMode.RGBA);
		return colorModeGroup;
	}
	private void setColorMode(ImageUtils.ColorMode colorMode){
		this.colorMode = colorMode;
		loadBitmap(selectedPath);
	}

	private void loadBitmap(String filepath) {
		selectedPath = filepath;
		if (filepath != null) {
			BufferedImage image = getImage(filepath);
			comp.setImage(image);
			comp.resetZoom();
			comp.revalidate();
			comp.repaint();
//			imageViewerPanel.revalidate();
//			imageViewerPanel.repaint();
		}
	}

	private BufferedImage getImage(String filepath){
		BufferedImage texture = BLPHandler.getGameTex(filepath);
		if(texture != null){
			if(colorMode == ImageUtils.ColorMode.RGBA){
				return texture;
			} else {
				return ImageUtils.getBufferedImageIsolateChannel(texture, colorMode);
			}
		} else {
			int imageSize = 128;
			final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.BLACK);
			int size = imageSize-6;
			GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, size);
			int dist1 = (imageSize - size)/2;
			int dist2 = imageSize-dist1;
			GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return image;
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

	private void fetchImageIcons(){
		String[] imageExtension = {".bmp", ".tga", ".jpg", ".jpeg", ".pcx", ".blp", ".dds"};
		for(String extension : imageExtension){
			getIcons(extension);
		}
	}

	protected void openTreePath(TreePath treePath) {
		if (treePath != null) {
			MPQTreeNode lastPathComponent = (MPQTreeNode) treePath.getLastPathComponent();
			if (lastPathComponent != null && lastPathComponent.isLeaf()) {
				loadBitmap(lastPathComponent.getPath());
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
						BufferedImage gameTex = BLPHandler
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp");
						if(gameTex != null){
							setIcon(new ImageIcon(gameTex));
						}
					} else {
						BufferedImage gameTex = BLPHandler
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp");
						if(gameTex != null){
							setIcon(new ImageIcon(gameTex));
						}
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
			boolean fitsSearch = currentSearch.equals("")
					|| (node.isLeaf()
						&& node.getSubPathName().toLowerCase(Locale.ROOT)
							.contains(currentSearch.toLowerCase(Locale.ROOT)));
			node.setVisible(fitsSearch);
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
		String imageRegexFilter = ".+(\\.bmp|\\.tga|\\.jpg|\\.jpeg|\\.pcx|\\.blp|\\.dds)";
		mergedListFile.removeIf(o -> !o.matches(imageRegexFilter));
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

	public static void showPanel(){
		MPQImageBrowser mpqImageBrowser = new MPQImageBrowser();
		mpqImageBrowser.setPreferredSize(new Dimension(800, 650));
		FramePopup.show(mpqImageBrowser, ProgramGlobals.getMainPanel(), "Browse Textures");
	}
}