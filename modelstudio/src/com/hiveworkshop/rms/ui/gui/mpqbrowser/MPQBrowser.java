package com.hiveworkshop.rms.ui.gui.mpqbrowser;

import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Callback;

import javax.swing.*;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
	private final JFileChooser exportFileChooser;
	private final MouseAdapterExtension mouseAdapterExtension;
	private final List<Filter> filters;
	private final CompoundDataSource gameDataFileSystem;
	private final DefaultTreeModel treeModel;
	private final Map<String, Filter> extensionToFilter = new HashMap<>();
	private final Filter otherFilter;

	public MPQBrowser(final CompoundDataSource gameDataFileSystem, final Callback<String> fileOpenCallback,
					  final Callback<String> useAsTextureCallback) {
		this.gameDataFileSystem = gameDataFileSystem;
		final JMenuBar menuBar = new JMenuBar();
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setEnabled(false);
		menuBar.add(fileMenu);
		final JMenu filtersMenu = new JMenu("Filters");
		menuBar.add(filtersMenu);
		filters = new ArrayList<>();
		filters.add(new Filter("Text", new String[] { ".txt" }));
		filters.add(new Filter("Sylk", new String[] { ".slk" }));
		filters.add(new Filter("Script", new String[] { ".ai", ".wai", ".j", ".js", ".pld" }));
		filters.add(new Filter("Html", new String[] { ".htm", ".html" }));
		filters.add(new Filter("Models", new String[] { ".mdl", ".mdx" }));
		filters.add(new Filter("Images", new String[] { ".bmp", ".tga", ".jpg", ".jpeg", ".pcx", ".blp", ".dds" }));
		filters.add(new Filter("Maps", new String[] { ".w3m", ".w3x", ".w3n" }));
		filters.add(new Filter("Sounds", new String[] { ".wav" }));
		filters.add(new Filter("Music", new String[] { ".mp3", ".mid" }));
		otherFilter = new Filter("Other", true);
		filters.add(otherFilter);
		for (final Filter filter : filters) {
			filtersMenu.add(filter.getFilterCheckBoxItem());
			filter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					refreshTree();
				}
			});
			for (final String ext : filter.extensions) {
				extensionToFilter.put(ext, filter);
			}
		}
		filtersMenu.addSeparator();
		final JMenuItem allItem = new JMenuItem("All");
		filtersMenu.add(allItem);
		allItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final Filter filter : filters) {
					filter.getFilterCheckBoxItem().setSelected(true);
				}
				refreshTree();
			}
		});
		final JMenuItem noneItem = new JMenuItem("None");
		filtersMenu.add(noneItem);
		noneItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final Filter filter : filters) {
					filter.getFilterCheckBoxItem().setSelected(false);
				}
				refreshTree();
			}
		});
		final MPQTreeNode root = createMPQTree(gameDataFileSystem);
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
					final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				final Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				if (leaf) {
					final String name = value.toString();

					final int indexOf = name.indexOf('.');
					if (indexOf != -1) {
						final String ext = name.substring(indexOf);
						final File tempFile = getDummyFile(ext);
						if (tempFile != null) {
							final Icon systemIcon = FileSystemView.getFileSystemView().getSystemIcon(tempFile);
							if (systemIcon != null) {
								setIcon(systemIcon);
							}
							tempFile.delete();
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
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					final MPQTreeNode lastPathComponent = (MPQTreeNode) tree.getPathForLocation(e.getX(), e.getY())
							.getLastPathComponent();
					if (lastPathComponent != null) {
						fileOpenCallback.run(lastPathComponent.getPath());
					}
				}
			}
		});
		setLayout(new BorderLayout());
		add(menuBar, BorderLayout.BEFORE_FIRST_LINE);
		add(new JScrollPane(tree), BorderLayout.CENTER);

		exportFileChooser = new JFileChooser(SaveProfile.get().getPath());

		final JPopupMenu contextMenu = new JPopupMenu();
		final JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				fileOpenCallback
						.run(((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent()).getPath());
			}
		});
		final JMenuItem extractItem = new JMenuItem("Export");
		extractItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final MPQTreeNode clickedNode = ((MPQTreeNode) mouseAdapterExtension.getClickedPath()
						.getLastPathComponent());
				exportFileChooser.setSelectedFile(
						new File(exportFileChooser.getCurrentDirectory() + "/" + clickedNode.getSubPathName()));
				if (exportFileChooser.showSaveDialog(MPQBrowser.this) == JFileChooser.APPROVE_OPTION) {
					final File selectedFile = exportFileChooser.getSelectedFile();
					if (selectedFile != null) {
						if (selectedFile.exists()) {
							if (JOptionPane.showConfirmDialog(MPQBrowser.this,
									"File \"" + selectedFile.getName() + "\" already exists. Overwrite anyway?",
									"Export File", JOptionPane.WARNING_MESSAGE,
									JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
								return;
							} else {
								selectedFile.delete();
							}
						} else {
							try {
								Files.copy(gameDataFileSystem.getResourceAsStream(clickedNode.getPath()),
										selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							} catch (final IOException e1) {
								ExceptionPopup.display(e1);
								e1.printStackTrace();
							}
						}
					}
				}
			}
		});
		final JMenuItem copyPathToClipboardItem = new JMenuItem("Copy Path to Clipboard");
		copyPathToClipboardItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final MPQTreeNode clickedNode = ((MPQTreeNode) mouseAdapterExtension.getClickedPath()
						.getLastPathComponent());
				final StringSelection selection = new StringSelection(clickedNode.getPath());
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		});
		contextMenu.add(openItem);
		contextMenu.add(extractItem);
		contextMenu.addSeparator();
		contextMenu.add(copyPathToClipboardItem);
		final JMenuItem useAsTextureItem = new JMenuItem("Use as Texture");
		useAsTextureItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				useAsTextureCallback
						.run(((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent()).getPath());
			}
		});
		contextMenu.add(useAsTextureItem);
		mouseAdapterExtension = new MouseAdapterExtension(contextMenu);
		tree.addMouseListener(mouseAdapterExtension);
	}

	public void refreshTree() {
		final MPQTreeNode newTree = createMPQTree(gameDataFileSystem);
		treeModel.setRoot(newTree);
	}

	public MPQTreeNode createMPQTree(final CompoundDataSource gameDataFileSystem) {
		final MPQTreeNode root = new MPQTreeNode(null, "", "");
		final Set<String> mergedListfile = gameDataFileSystem.getMergedListfile();
		final List<String> listfile = new ArrayList<>();
		for (final String string : mergedListfile) {
			listfile.add(string);
		}
		Collections.sort(listfile);
		for (String string : listfile) {
			final int periodIndex = string.indexOf('.');
			boolean foundMatch = false;
			if (periodIndex != -1) {
				final String extension = string.substring(periodIndex);
				final Filter filter = extensionToFilter.get(extension);
				if (filter != null) {
					foundMatch = true;
					if (!filter.getFilterCheckBoxItem().isSelected()) {
						continue;
					}
				}
			}
			if (!foundMatch && !otherFilter.getFilterCheckBoxItem().isSelected()) {
				continue;
			}
			MPQTreeNode currentNode = root;
			final StringBuilder totalPath = new StringBuilder();
			for (int slashIndex = string.indexOf('\\'); slashIndex != -1; slashIndex = string.indexOf('\\')) {
				final String prefixName = string.substring(0, slashIndex);
				if (totalPath.length() > 0) {
					totalPath.append("\\");
				}
				totalPath.append(prefixName);
				MPQTreeNode child = currentNode.getChild(prefixName);
				if (child == null) {
					child = new MPQTreeNode(currentNode, totalPath.toString(), prefixName);
					currentNode.addChild(prefixName, child);
				}
				currentNode = child;
				string = string.substring(slashIndex + 1);
			}
			if (totalPath.length() > 0) {
				totalPath.append("\\");
			}
			totalPath.append(string);
			final MPQTreeNode leafNode = new MPQTreeNode(currentNode, totalPath.toString(), string);
			currentNode.addChild(string, leafNode);
		}
		root.sort();
		return root;
	}

	private static File getDummyFile(final String extension) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		if (!tmpdir.endsWith(File.separator)) {
			tmpdir += File.separator;
		}
		final String tempDir = tmpdir + "MatrixEaterExtract/";
		final String dummyPath = tempDir + extension;
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
}
