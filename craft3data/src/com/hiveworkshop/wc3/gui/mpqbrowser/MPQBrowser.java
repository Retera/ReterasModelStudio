package com.hiveworkshop.wc3.gui.mpqbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.etheller.collections.ArrayList;
import com.etheller.collections.List;
import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.Callback;
import com.localizationmanager.localization.localizationmanager;

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
	private MouseAdapterExtension mouseAdapterExtension;
	private List<Filter> filters;
	private final MpqCodebase mpqCodebase;
	private DefaultTreeModel treeModel;
	private final Map<String, Filter> extensionToFilter = new HashMap<>();
	private Filter otherFilter;

	public MPQBrowser(final MpqCodebase mpqCodebase, final Callback<String> fileOpenCallback,
			final Callback<String> useAsTextureCallback) {
		this.mpqCodebase = mpqCodebase;
		final JMenuBar menuBar = new JMenuBar();
		final JMenu fileMenu = new JMenu(LocalizationManager.getInstance().get("menu.mpqbrowser_mpqbrowser_filemenu"));
		fileMenu.setEnabled(false);
		menuBar.add(fileMenu);
		final JMenu filtersMenu = new JMenu(LocalizationManager.getInstance().get("menu.mpqbrowser_mpqbrowser_filtersmenu"));
		menuBar.add(filtersMenu);
		filters = new ArrayList<>();
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_text"), new String[] { ".txt" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_sylk"), new String[] { ".slk" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_script"), new String[] { ".ai", ".wai", ".j", ".js", ".pld" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_html"), new String[] { ".htm", ".html" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_models"), new String[] { ".mdl", ".mdx" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_images"), new String[] { ".bmp", ".tga", ".jpg", ".jpeg", ".pcx", ".blp", ".dds" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_maps"), new String[] { ".w3m", ".w3x", ".w3n" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_sounds"), new String[] { ".wav", ".flac" }));
		filters.add(new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_music"), new String[] { ".mp3", ".mid" }));
		otherFilter = new Filter(LocalizationManager.getInstance().get("filter.mpqbrowser_mpqbrowser_other"), true);
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
		final JMenuItem allItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_allitem"));
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
		final JMenuItem noneItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_noneitem"));
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
		final MPQTreeNode root = createMPQTree(mpqCodebase);
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
				}
				else {
					if (expanded) {
						setIcon(new ImageIcon(BLPHandler.get()
								.getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
					}
					else {
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
		final JMenuItem openItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_openitem"));
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				fileOpenCallback
						.run(((MPQTreeNode) mouseAdapterExtension.getClickedPath().getLastPathComponent()).getPath());
			}
		});
		final JMenuItem extractItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_extractitem"));
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
									LocalizationManager.getInstance().get("dialog.mpqbrowser_exportfilechooser_1") +  "\"" + selectedFile.getName() + "\" " +
									LocalizationManager.getInstance().get("dialog.mpqbrowser_exportfilechooser_2"),
									LocalizationManager.getInstance().get("dialog.mpqbrowser_exportfilechooser_3"), JOptionPane.WARNING_MESSAGE,
									JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
								return;
							}
							else {
								selectedFile.delete();
							}
						}
						else {
							try {
								Files.copy(mpqCodebase.getResourceAsStream(clickedNode.getPath()),
										selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							}
							catch (final IOException e1) {
								ExceptionPopup.display(e1);
								e1.printStackTrace();
							}
						}
					}
				}
			}
		});
		final JMenuItem copyPathToClipboardItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_copypathtoclipboarditem"));
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
		final JMenuItem useAsTextureItem = new JMenuItem(LocalizationManager.getInstance().get("menuitem.mpqbrowser_mpqbrowser_useastextureitem"));
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
		final MPQTreeNode newTree = createMPQTree(mpqCodebase);
		treeModel.setRoot(newTree);
	}

	public MPQTreeNode createMPQTree(final MpqCodebase mpqCodebase) {
		final MPQTreeNode root = new MPQTreeNode(null, "", "");
		final SetView<String> mergedListfile = mpqCodebase.getMergedListfile();
		final List<String> listfile = new ArrayList<>();
		for (final String string : mergedListfile) {
			listfile.add(string);
		}
		List.Util.sort(listfile);
		for (String string : listfile) {
			final int periodIndex = string.lastIndexOf('.');
			boolean foundMatch = false;
			if (periodIndex != -1) {
				final String extension = string.substring(periodIndex);
				final Filter filter = extensionToFilter.get(extension.toLowerCase(Locale.US));
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
			}
			catch (final IOException e) {
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
