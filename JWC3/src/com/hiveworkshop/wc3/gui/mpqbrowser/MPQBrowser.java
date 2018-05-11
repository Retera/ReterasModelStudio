package com.hiveworkshop.wc3.gui.mpqbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.Icon;
import javax.swing.JFileChooser;
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
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.Callback;

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

	public MPQBrowser(final MpqCodebase mpqCodebase, final Callback<String> fileOpenCallback) {
		final MPQTreeNode root = new MPQTreeNode(null, "", "");
		final SetView<String> mergedListfile = mpqCodebase.getMergedListfile();
		final List<String> listfile = new ArrayList<>();
		for (final String string : mergedListfile) {
			listfile.add(string);
		}
		List.Util.sort(listfile);
		for (String string : listfile) {
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
		final DefaultTreeModel treeModel = new DefaultTreeModel(root);
		this.tree = new JTree(treeModel);
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
				return treeCellRendererComponent;
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					fileOpenCallback
							.run(((MPQTreeNode) tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent())
									.getPath());
				}
			}
		});
		setLayout(new BorderLayout());
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
								Files.copy(mpqCodebase.getResourceAsStream(clickedNode.getPath()),
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
		mouseAdapterExtension = new MouseAdapterExtension(contextMenu);
		tree.addMouseListener(mouseAdapterExtension);
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
}
