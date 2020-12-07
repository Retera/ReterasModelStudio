package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.MpqDataSourceDescriptor;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.WindowsRegistry;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class DataSourceChooserPanel extends JPanel {
	private static final ImageIcon CASCIcon;
	private static final ImageIcon MPQIcon;
	private static final ImageIcon FolderIcon;

	static {
		CASCIcon = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/CASC.png");
		MPQIcon = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/MPQ.png");
		FolderIcon = getImageIcon("/UI/Widgets/ReteraStudio/DataSourceIcons/Folder.png");
	}

	public DataSourceChooserPanel(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		dataSourceDescriptors = new ArrayList<>();
		fileChooser = new JFileChooser();

		getWindowsRegistryDirectory();

		final JButton clearList = getButton("Clear All", e -> clearAll(), true);

		final JButton addWarcraft3Installation = getButton("Add War3 Install Directory", e -> addWar3InstallDirectory(), true);

		final JButton addDefaultCascPrefixes = getButton("Add Default CASC Mod", e -> addDefaultCASCMod(), false);

		final JButton addSpecificCascPrefix = getButton("Add Specific CASC Mod", e -> addSpecificCASCMod(), false);

		final JButton deleteSelection = getButton("Delete Selection", e -> deleteSelection(), false);

		final JButton moveSelectionUp = getButton("Move Up", e -> moveUp(), false);

		final JButton moveSelectionDown = getButton("Move Down", e -> moveDown(), false);

		final JButton enterHDMode = getButton("Reforged Graphics Mode", e -> enterHDMode(), true);

		final JButton enterSDMode = getButton("Classic Graphics Mode", e -> enterSDMode(), true);

		final JButton resetAllToDefaults = getButton("Reset to Defaults", e -> loadDefaults(null), true);

		final JButton addCASCButton = getButton("Add CASC", e -> addCASC(), true);

		final JButton addMPQButton = getButton("Add MPQ", e -> addMPQ(), true);

		final JButton addFolderButton = getButton("Add Folder", e -> addFolder(), true);

		final JLabel separatorLabel = new JLabel("-----");
		separatorLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		final JLabel separatorLabel2 = new JLabel("-----");
		separatorLabel2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		final JLabel separatorLabelLeftHandSide = new JLabel("-----");
		separatorLabelLeftHandSide.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		final JLabel warcraft3InstallLocated = new JLabel("'Path' Registry Key: ");
		final JLabel warcraft3InstallPath = new JLabel(wcDirectory == null ? "Not found" : wcDirectory);
		warcraft3InstallLocated.setFont(new Font("Consolas", Font.BOLD, getFont().getSize()));
		warcraft3InstallPath.setFont(new Font("Consolas", Font.PLAIN, getFont().getSize()));
		if (wcDirectory == null) {
			warcraft3InstallPath.setForeground(Color.RED);
		}
		root = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(root);
		dataSourceTree = new JTree(model);
		dataSourceTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		dataSourceTree.setRootVisible(false);

		dataSourceTree.addTreeSelectionListener(e -> dataSourceTreeListener(addDefaultCascPrefixes, addSpecificCascPrefix, deleteSelection, moveSelectionUp, moveSelectionDown, e));
		final JScrollPane dstScrollpane = new JScrollPane(dataSourceTree);
		dstScrollpane.setPreferredSize(new Dimension(500, 400));

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(clearList)
										.addComponent(addWarcraft3Installation)
										.addComponent(resetAllToDefaults)
										.addComponent(separatorLabelLeftHandSide)
										.addComponent(enterHDMode)
										.addComponent(enterSDMode))
								.addComponent(dstScrollpane)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(addCASCButton)
										.addComponent(addMPQButton)
										.addComponent(addFolderButton)
										.addComponent(separatorLabel)
										.addComponent(addDefaultCascPrefixes)
										.addComponent(addSpecificCascPrefix)
										.addComponent(separatorLabel2)
										.addComponent(deleteSelection)
										.addComponent(moveSelectionUp)
										.addComponent(moveSelectionDown)))
						.addGroup(layout.createSequentialGroup()
								.addComponent(warcraft3InstallLocated)
								.addComponent(warcraft3InstallPath))).addGap(8));

		layout.linkSize(SwingConstants.HORIZONTAL, clearList, addWarcraft3Installation, enterHDMode, enterSDMode,
				resetAllToDefaults);
		layout.linkSize(SwingConstants.HORIZONTAL, addCASCButton, addMPQButton, addFolderButton, addDefaultCascPrefixes,
				addSpecificCascPrefix, deleteSelection, moveSelectionUp, moveSelectionDown);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(clearList)
								.addComponent(addWarcraft3Installation)
								.addComponent(resetAllToDefaults)
								.addComponent(separatorLabelLeftHandSide)
								.addComponent(enterHDMode)
								.addComponent(enterSDMode))
						.addComponent(dstScrollpane)
						.addGroup(layout.createSequentialGroup()
								.addComponent(addCASCButton)
								.addComponent(addMPQButton)
								.addComponent(addFolderButton)
								.addComponent(separatorLabel)
								.addComponent(addDefaultCascPrefixes)
								.addComponent(addSpecificCascPrefix)
								.addComponent(separatorLabel2)
								.addComponent(deleteSelection)
								.addComponent(moveSelectionUp)
								.addComponent(moveSelectionDown))).addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(warcraft3InstallLocated)
						.addComponent(warcraft3InstallPath)).addGap(8));

		dataSourceTree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
			                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				final Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				final JLabel label = (JLabel) treeCellRendererComponent;

				if (value instanceof DataSourceDescTreeNode) {
					final DataSourceDescriptor descriptor = ((DataSourceDescTreeNode) value).getDescriptor();
					if (descriptor instanceof CascDataSourceDescriptor) {
						label.setIcon(CASCIcon);
					} else if (descriptor instanceof MpqDataSourceDescriptor) {
						label.setIcon(MPQIcon);
					} else if (descriptor instanceof FolderDataSourceDescriptor) {
						label.setIcon(FolderIcon);
					} else {
						label.setIcon(null);
					}
				}
				return treeCellRendererComponent;
			}
		});

		setLayout(layout);
		loadDefaults(dataSourceDescriptorDefaults);
	}

	private final List<DataSourceDescriptor> dataSourceDescriptors;
	private String wcDirectory;
	private final JFileChooser fileChooser;
	private final DefaultMutableTreeNode root;
	private final DefaultTreeModel model;
	private final JTree dataSourceTree;

	private static ImageIcon getImageIcon(String iconPath) {
		ImageIcon imageIcon = null;
		try {
			imageIcon = new ImageIcon(ImageIO.read(DataSourceChooserPanel.class.getResource(iconPath))
					.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		} catch (final IOException ignored) {
		}
		return imageIcon;
	}

	private JButton ugg(String s, ActionListener actionListener, boolean setEnabled) {
		final JButton enterHDMode = new JButton(s);
		enterHDMode.setEnabled(setEnabled);
		enterHDMode.addActionListener(actionListener);
		return enterHDMode;
	}

	private JButton getButton(String buttonText, ActionListener actionListener, boolean setEnabled) {
		final JButton button = new JButton(buttonText);
		button.addActionListener(actionListener);
		button.setEnabled(setEnabled);
		return button;
	}

	private void dataSourceTreeListener(JButton addDefaultCascPrefixes, JButton addSpecificCascPrefix, JButton deleteSelection, JButton moveSelectionUp, JButton moveSelectionDown, javax.swing.event.TreeSelectionEvent e) {
		boolean cascSelected = false;
		final TreePath selectionPath = e.getNewLeadSelectionPath();
		if (selectionPath != null) {
			Object lastPathComponent = selectionPath.getLastPathComponent();
			if (lastPathComponent instanceof DefaultMutableTreeNode) {
				final TreeNode parent = ((DefaultMutableTreeNode) lastPathComponent).getParent();
				if (parent instanceof DataSourceDescTreeNode) {
					lastPathComponent = parent;
				}
			}
			if (lastPathComponent instanceof DataSourceDescTreeNode) {
				final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
						.getDescriptor();
				if (dataSourceDescriptor instanceof CascDataSourceDescriptor) {
					cascSelected = true;
				}
			}
		}
		addDefaultCascPrefixes.setEnabled(cascSelected);
		addSpecificCascPrefix.setEnabled(cascSelected);
		deleteSelection.setEnabled(selectionPath != null);
		moveSelectionUp.setEnabled(selectionPath != null);
		moveSelectionDown.setEnabled(selectionPath != null);
	}

	private void clearAll() {
		dataSourceDescriptors.clear();
		reloadTree();
	}

	private void getWindowsRegistryDirectory() {
		wcDirectory = WindowsRegistry.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III",
				"InstallPathX");
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry
					.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III", "InstallPathX");
		}
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(
					"HKEY_CURRENT_USER\\Software\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\Blizzard Entertainment\\Warcraft III",
					"InstallPath");
		}
		if (wcDirectory != null) {
			wcDirectory = wcDirectory.trim();
			fileChooser.setCurrentDirectory(new File(wcDirectory));
		}
	}

	private void addWar3InstallDirectory() {
		fileChooser.setFileFilter(null);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int result = fileChooser.showOpenDialog(DataSourceChooserPanel.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				// Is it a CASC war3
				final Path installPathPath = selectedFile.toPath();
				addWarcraft3Installation(installPathPath, true);
				reloadTree();
			}
		}
	}

	private void addDefaultCASCMod() {
		final TreePath selectionPath = dataSourceTree.getSelectionPath();
		if (selectionPath != null) {
			final Object lastPathComponent = selectionPath.getLastPathComponent();
			if (lastPathComponent instanceof DataSourceDescTreeNode) {
				final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
						.getDescriptor();
				if (dataSourceDescriptor instanceof CascDataSourceDescriptor) {
					final CascDataSourceDescriptor casc = (CascDataSourceDescriptor) dataSourceDescriptor;
					addDefaultCASCPrefixes(Paths.get(casc.getGameInstallPath()), casc, true);
					reloadTree();
				}
			}
		}
	}

	private void addFolder() {
		fileChooser.setFileFilter(null);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int result = fileChooser.showOpenDialog(DataSourceChooserPanel.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				dataSourceDescriptors.add(new FolderDataSourceDescriptor(selectedFile.getPath()));
				reloadTree();
			}
		}
	}

	private void addMPQ() {
		fileChooser.setFileFilter(new FileNameExtensionFilter("MPQ Archive File", "mpq", "w3x", "w3m"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int result = fileChooser.showOpenDialog(DataSourceChooserPanel.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				dataSourceDescriptors.add(new MpqDataSourceDescriptor(selectedFile.getPath()));
				reloadTree();
			}
		}
	}

	private void addCASC() {
		fileChooser.setFileFilter(null);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int result = fileChooser.showOpenDialog(DataSourceChooserPanel.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				dataSourceDescriptors
						.add(new CascDataSourceDescriptor(selectedFile.getPath(), new ArrayList<>()));
				reloadTree();
			}
		}
	}

	private void moveDown() {
		final TreePath[] selectionPaths = dataSourceTree.getSelectionPaths();
		final int[] selectionRows = dataSourceTree.getSelectionRows();
		for (final TreePath selectionPath : selectionPaths) {
			if (selectionPath != null) {
				final Object lastPathComponent = selectionPath.getLastPathComponent();
				if (lastPathComponent instanceof DefaultMutableTreeNode) {
					final TreeNode parent = ((DefaultMutableTreeNode) lastPathComponent).getParent();
					if (parent instanceof DataSourceDescTreeNode) {
						final DataSourceDescriptor parentDescriptor = ((DataSourceDescTreeNode) parent)
								.getDescriptor();
						if (parentDescriptor instanceof CascDataSourceDescriptor) {
							((CascDataSourceDescriptor) parentDescriptor)
									.movePrefixDown(parent.getIndex((TreeNode) lastPathComponent));
							reloadTree();
							dataSourceTree.addSelectionPath(selectionPath);
						}
					}
				}
				if (lastPathComponent instanceof DataSourceDescTreeNode) {
					final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
							.getDescriptor();
					final int indexOf = dataSourceDescriptors.indexOf(dataSourceDescriptor);
					if (indexOf < (dataSourceDescriptors.size() - 1)) {
						Collections.swap(dataSourceDescriptors, indexOf, indexOf + 1);
					}
					reloadTree();
				}
			}
		}
		dataSourceTree.clearSelection();
		for (final int row : selectionRows) {
			if ((row + 1) < dataSourceTree.getRowCount()) {
				dataSourceTree.addSelectionRow(row + 1);
			}
		}
	}

	private void moveUp() {
		final TreePath[] selectionPaths = dataSourceTree.getSelectionPaths();
		final int[] selectionRows = dataSourceTree.getSelectionRows();
		for (final TreePath selectionPath : selectionPaths) {
			if (selectionPath != null) {
				final Object lastPathComponent = selectionPath.getLastPathComponent();
				if (lastPathComponent instanceof DefaultMutableTreeNode) {
					final TreeNode parent = ((DefaultMutableTreeNode) lastPathComponent).getParent();
					if (parent instanceof DataSourceDescTreeNode) {
						final DataSourceDescriptor parentDescriptor = ((DataSourceDescTreeNode) parent)
								.getDescriptor();
						if (parentDescriptor instanceof CascDataSourceDescriptor) {
							((CascDataSourceDescriptor) parentDescriptor)
									.movePrefixUp(parent.getIndex((TreeNode) lastPathComponent));
							reloadTree();
						}
					}
				}
				if (lastPathComponent instanceof DataSourceDescTreeNode) {
					final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
							.getDescriptor();
					final int indexOf = dataSourceDescriptors.indexOf(dataSourceDescriptor);
					if (indexOf > 0) {
						Collections.swap(dataSourceDescriptors, indexOf, indexOf - 1);
					}
					reloadTree();
				}
			}
		}
		dataSourceTree.clearSelection();
		for (final int row : selectionRows) {
			if ((row - 1) > 0) {
				dataSourceTree.addSelectionRow(row - 1);
			}
		}
	}

	private void deleteSelection() {
		final TreePath[] selectionPaths = dataSourceTree.getSelectionPaths();
		for (final TreePath selectionPath : selectionPaths) {
			if (selectionPath != null) {
				final Object lastPathComponent = selectionPath.getLastPathComponent();
				if (lastPathComponent instanceof DefaultMutableTreeNode) {
					final TreeNode parent = ((DefaultMutableTreeNode) lastPathComponent).getParent();
					if (parent instanceof DataSourceDescTreeNode) {
						final DataSourceDescriptor parentDescriptor = ((DataSourceDescTreeNode) parent)
								.getDescriptor();
						if (parentDescriptor instanceof CascDataSourceDescriptor) {
							((CascDataSourceDescriptor) parentDescriptor)
									.deletePrefix(parent.getIndex((TreeNode) lastPathComponent));
							reloadTree();
						}
					}
				}
				if (lastPathComponent instanceof DataSourceDescTreeNode) {
					final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
							.getDescriptor();
					dataSourceDescriptors.remove(dataSourceDescriptor);
					reloadTree();
				}
			}
		}
	}

	private void addSpecificCASCMod() {
		//				JOptionPane.showInputDialog(DataSourceChooserPanel.this, "Enter the name of a CASC Mod:");
		final TreePath selectionPath = dataSourceTree.getSelectionPath();
		if (selectionPath != null) {
			Object lastPathComponent = selectionPath.getLastPathComponent();
			if (lastPathComponent instanceof DefaultMutableTreeNode) {
				final TreeNode parent = ((DefaultMutableTreeNode) lastPathComponent).getParent();
				if (parent instanceof DataSourceDescTreeNode) {
					lastPathComponent = parent;
				}
			}
			if (lastPathComponent instanceof DataSourceDescTreeNode) {
				final DataSourceDescriptor dataSourceDescriptor = ((DataSourceDescTreeNode) lastPathComponent)
						.getDescriptor();
				if (dataSourceDescriptor instanceof CascDataSourceDescriptor) {
					final CascDataSourceDescriptor casc = (CascDataSourceDescriptor) dataSourceDescriptor;
					addSpecificCASCPrefix(Paths.get(casc.getGameInstallPath()), casc);
					reloadTree();
				}
			}
		}
	}

	private void enterSDMode() {
		if ((dataSourceDescriptors.size() == 1) && (dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor)) {
			final CascDataSourceDescriptor cascDataSourceDescriptor = (CascDataSourceDescriptor) dataSourceDescriptors
					.get(0);
			if (cascDataSourceDescriptor.getPrefixes().size() == 5) {
				cascDataSourceDescriptor.deletePrefix(4);
				cascDataSourceDescriptor.deletePrefix(3);
				reloadTree();
			} else {
				JOptionPane.showMessageDialog(this, "Your Warcraft III data CASC configuration is not in the HD mode.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this,
					"Your Warcraft III data configuration is not a standard Reforged CASC setup, so this automation feature is unavailable." +
							"\nTo use this feature, please press 'Clear All' and then 'Add War3 Install Directory' to choose a Reforged installation.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void enterHDMode() {
		if ((dataSourceDescriptors.size() == 1) && (dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor)) {
			final CascDataSourceDescriptor cascDataSourceDescriptor = (CascDataSourceDescriptor) dataSourceDescriptors
					.get(0);
			if (cascDataSourceDescriptor.getPrefixes().size() == 3) {
				String localesMod = null;
				for (final String possiblePrefix : cascDataSourceDescriptor.getPrefixes()) {
					if (possiblePrefix.contains("_locales")) {
						localesMod = possiblePrefix;
						break;
					}
				}
				if (localesMod != null) {
					cascDataSourceDescriptor.addPrefix("war3.w3mod\\_hd.w3mod");
					cascDataSourceDescriptor.addPrefix(localesMod.replace("_locales", "_hd.w3mod\\_locales"));
					reloadTree();
				} else {
					JOptionPane.showMessageDialog(this,
							"Your Warcraft III data CASC configuration is not in the SD mode or is not configured in the expected way. You will need to apply HD mode manually by adding the appropriate CASC mods.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Your Warcraft III data CASC configuration is not in the SD mode.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this,
					"Your Warcraft III data configuration is not a standard Reforged CASC setup, so this automation feature is unavailable." +
							"\nTo use this feature, please press 'Clear All' and then 'Add War3 Install Directory' to choose a Reforged installation.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addWarcraft3Installation(final Path installPathPath, final boolean allowPopup) {
		if (Files.exists(installPathPath.resolve("Data/indices"))) {
			final CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString(),
					new ArrayList<>());
			dataSourceDescriptors.add(dataSourceDesc);
			addDefaultCASCPrefixes(installPathPath, dataSourceDesc, allowPopup);
		} else {
			addFromMPQ(installPathPath, "War3.mpq");
			addFromMPQ(installPathPath, "War3Local.mpq");
			addFromMPQ(installPathPath, "War3x.mpq");
			addFromMPQ(installPathPath, "War3xlocal.mpq");
			addFromMPQ(installPathPath, "war3patch.mpq");
			addFromMPQ(installPathPath, "Deprecated.mpq");
			addFromFolder(installPathPath, "war3.w3mod");
			addFromFolder(installPathPath, "war3.w3mod/_locales/enus.w3mod");
			addFromFolder(installPathPath, "war3.w3mod/_deprecated.w3mod");
			addFromFolder(installPathPath, "war3.w3mod/_hd.w3mod");
			addFromFolder(installPathPath, "war3.w3mod/_hd.w3mod/_locales/enus.w3mod");
		}
	}

	private void addFromFolder(Path installPathPath, String s) {
		if (Files.exists(installPathPath.resolve(s))) {
			dataSourceDescriptors.add(new FolderDataSourceDescriptor(installPathPath.resolve(s).toString()));
		}
	}

	private void addFromMPQ(Path installPathPath, String s) {
		if (Files.exists(installPathPath.resolve(s))) {
			dataSourceDescriptors.add(new MpqDataSourceDescriptor(installPathPath.resolve(s).toString()));
		}
	}

	protected void loadDefaults(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		dataSourceDescriptors.clear();
		if (dataSourceDescriptorDefaults == null) {
			if (wcDirectory != null) {
				addWarcraft3Installation(Paths.get(wcDirectory), false);
			}
		} else {
			for (final DataSourceDescriptor dataSourceDescriptor : dataSourceDescriptorDefaults) {
				dataSourceDescriptors.add(dataSourceDescriptor.duplicate());
			}
		}
		reloadTree();
	}

	private void reloadTree() {
		final TreePath selectionPath = dataSourceTree.getSelectionPath();
		int selectedRow = -1;
		if (selectionPath != null) {
			selectedRow = dataSourceTree.getRowForPath(selectionPath);
		}
		for (int i = root.getChildCount() - 1; i >= 0; i--) {
			model.removeNodeFromParent((MutableTreeNode) root.getChildAt(i));
		}
		for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
			final DataSourceDescTreeNode newChild = new DataSourceDescTreeNode(descriptor);
			if (descriptor instanceof CascDataSourceDescriptor) {
				final CascDataSourceDescriptor cascDescriptor = (CascDataSourceDescriptor) descriptor;
				if (cascDescriptor.getPrefixes().isEmpty()) {
					newChild.setUserObject(newChild.getUserObject() + " (WARNING: No Mods Selected)");
				}
				for (final String prefix : cascDescriptor.getPrefixes()) {
					model.insertNodeInto(new DefaultMutableTreeNode(prefix), newChild, newChild.getChildCount());
				}
			}
			model.insertNodeInto(newChild, root, root.getChildCount());
		}
		dataSourceTree.expandPath(new TreePath(root));
		for (int i = 0; i < dataSourceTree.getRowCount(); i++) {
			dataSourceTree.expandRow(i);
		}
		if ((selectedRow >= 0) && (selectedRow < dataSourceTree.getRowCount())) {
			dataSourceTree.setSelectionRow(selectedRow);
		}
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceDescriptors;
	}

	private static final class DataSourceDescTreeNode extends DefaultMutableTreeNode {

		private final DataSourceDescriptor descriptor;

		public DataSourceDescTreeNode(final DataSourceDescriptor descriptor) {
			super(descriptor.getDisplayName());
			this.descriptor = descriptor;
		}

		public DataSourceDescriptor getDescriptor() {
			return descriptor;
		}
	}

	private enum SupportedCascPatchFormat {
		PATCH130, PATCH131, PATCH132, UNKNOWN_FUTURE_PATCH
	}

	public static void main(final String[] args) {
		setupLookAndFeel("Aluminium");
		final JFrame dataSourceChooserFrame = new JFrame("DataSourceChooserPanel");
		dataSourceChooserFrame.setContentPane(new DataSourceChooserPanel(null));
		dataSourceChooserFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		dataSourceChooserFrame.pack();
		dataSourceChooserFrame.setLocationRelativeTo(null);
		dataSourceChooserFrame.setVisible(true);
	}

	private void addSpecificCASCPrefix(final Path installPathPath, final CascDataSourceDescriptor dataSourceDesc) {
		// It's CASC. Now the question: what prefixes do we use?
		try {
			final WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true);
			final DefaultComboBoxModel<String> prefixes = new DefaultComboBoxModel<>();
			try {
				final WarcraftIIICASC.FileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				final List<String> allFiles = rootFileSystem.enumerateFiles();
				for (final String file : allFiles) {
					if (rootFileSystem.isNestedFileSystem(file)) {
						prefixes.addElement(file);
					}
				}
			} finally {
				tempCascReader.close();
			}
			final JComboBox<String> prefixChoiceComboBox = new JComboBox<>(prefixes);
			prefixChoiceComboBox.setEditable(true);

			final JPanel comboBoxPanel = new JPanel(new BorderLayout());
			comboBoxPanel.add(prefixChoiceComboBox, BorderLayout.CENTER);
			comboBoxPanel.add(new JLabel("Choose a .w3mod:"), BorderLayout.BEFORE_FIRST_LINE);

			if (JOptionPane.showConfirmDialog(DataSourceChooserPanel.this, comboBoxPanel, "Choose Mod",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
				final Object selectedItem = prefixChoiceComboBox.getSelectedItem();
				if (selectedItem != null) {
					final String newPrefixName = selectedItem.toString();
					dataSourceDesc.addPrefix(newPrefixName);
				}
			}
		} catch (final Exception e1) {
			ExceptionPopup.display(e1);
			e1.printStackTrace();
		}
	}

	private void addDefaultCASCPrefixes(final Path installPathPath, final CascDataSourceDescriptor dataSourceDesc,
	                                    final boolean allowPopup) {
		// It's CASC. Now the question: what prefixes do we use?
		String locale = null;
		String launcherDbLocale = null;
		String originalInstallLocale = null;
		List<String> launcherDBLang = null;
		try {
			launcherDBLang = Files.readAllLines(installPathPath.resolve("Launcher.db"));
		} catch (final Exception ignored) {
		}
		if (launcherDBLang != null) {
			if (launcherDBLang.size() > 0) {
				final String dbLangString = launcherDBLang.get(0);
				if (dbLangString.length() == 4) {
					launcherDbLocale = dbLangString;
				}
			}
		}
		try {
			try (WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true)) {
				final String tags = tempCascReader.getBuildInfo().getField(tempCascReader.getActiveRecordIndex(),
						"Tags");
				final String[] splitTags = tags.split("\\?");
				for (final String splitTag : splitTags) {
					final String trimmedTag = splitTag.trim();
					final int spaceIndex = trimmedTag.indexOf(' ');
					if (spaceIndex != -1) {
						final String firstPart = trimmedTag.substring(0, spaceIndex);
						final String secondPart = trimmedTag.substring(spaceIndex + 1);
						if (secondPart.equals("speech") || secondPart.equals("text")) {
							if (firstPart.length() == 4) {
								originalInstallLocale = firstPart;
							}
						}
					}
				}
//				if (originalInstallLocale == null) {
//					locale = launcherDbLocale;
//				} else if ((launcherDbLocale == null) && (originalInstallLocale != null)) {
//					locale = originalInstallLocale;
//				} else if ((launcherDbLocale != null) && (originalInstallLocale != null)
//						&& originalInstallLocale.equals(launcherDbLocale)) {
//					locale = launcherDbLocale;
//				}
				final SupportedCascPatchFormat patchFormat;
				final WarcraftIIICASC.FileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				if (rootFileSystem.isFile("war3.mpq\\units\\unitdata.slk")) {
					patchFormat = SupportedCascPatchFormat.PATCH130;
				} else if (tempCascReader.getRootFileSystem()
						.isFile("war3.w3mod\\_hd.w3mod\\units\\human\\footman\\footman.mdx")) {
					patchFormat = SupportedCascPatchFormat.PATCH132;
				} else if (tempCascReader.getRootFileSystem().isFile("war3.w3mod\\units\\unitdata.slk")) {
					patchFormat = SupportedCascPatchFormat.PATCH131;
				} else {
					patchFormat = SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH;
				}
				// Now, we really want to know the locale.
				locale = getLocale(allowPopup, locale, launcherDbLocale, originalInstallLocale, tempCascReader, patchFormat, rootFileSystem);

				if (locale == null) return;
				final String lowerLocale = locale.toLowerCase();
				final List<String> defaultPrefixes;
				// Reforged?????? It's probably going to break my code
				defaultPrefixes = getPatchPrefixes(patchFormat, lowerLocale);
				for (final String prefix : defaultPrefixes) {
					dataSourceDesc.addPrefix(prefix);
				}
			}
		} catch (final Exception e1) {
			ExceptionPopup.display(e1);
			e1.printStackTrace();
		}
	}

	private List<String> getPatchPrefixes(SupportedCascPatchFormat patchFormat, String lowerLocale) {
		final List<String> defaultPrefixes;
		switch (patchFormat) {
			case PATCH130 -> {
				System.out.println("Detected Patch 1.30");
				// We used to have this, maybe some people still do?
				final String[] prefixes = {"war3.mpq", "deprecated.mpq", lowerLocale + "-war3local.mpq"};
				defaultPrefixes = Arrays.asList(prefixes);
			}
			case PATCH131 -> {
				System.out.println("Detected Patch 1.31");
				// This is what I have right now
				final String[] prefixes = {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
						"war3.w3mod\\_locales\\" + lowerLocale + ".w3mod"};
				defaultPrefixes = Arrays.asList(prefixes);
			}
			case PATCH132 -> {
				System.out.println("Detected Patch 1.32+");
				// This is what I have right now
				final String[] prefixes = {"war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
						"war3.w3mod\\_locales\\" + lowerLocale + ".w3mod", "war3.w3mod\\_hd.w3mod",
						"war3.w3mod\\_hd.w3mod\\_locales\\" + lowerLocale + ".w3mod"};
				defaultPrefixes = Arrays.asList(prefixes);
			}
			case UNKNOWN_FUTURE_PATCH -> {
				final String[] prefixes = {"war3.w3mod", "war3.w3mod", "war3.w3mod\\_deprecated.w3mod",
						"war3.w3mod\\_locales\\" + lowerLocale + ".w3mod"};
				JOptionPane.showMessageDialog(DataSourceChooserPanel.this,
						"The Warcraft III Installation you have selected seems to be too new, or is not a supported version. The suggested prefix list from Patch 1.31 will be used.\nThis will probably fail, and you will need more advanced configuration.",
						"Error", JOptionPane.ERROR_MESSAGE);
				defaultPrefixes = Arrays.asList(prefixes);
			}
			default -> defaultPrefixes = new ArrayList<>();
		}
		return defaultPrefixes;
	}

	private String getLocale(boolean allowPopup, String locale, String launcherDbLocale, String originalInstallLocale, WarcraftIIICASC tempCascReader, SupportedCascPatchFormat patchFormat, WarcraftIIICASC.FileSystem rootFileSystem) throws IOException {
		if (locale == null) {
			// gather list of locales from CASC
			final Set<String> localeOptions = new HashSet<>();
			if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
				final ByteBuffer buffer = rootFileSystem.readFileData("index");
				final Set<String> categories = new HashSet<>();
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(buffer.array())))) {
					String line;
					while ((line = reader.readLine()) != null) {
						final String[] splitLine = line.split("\\|");
						if (splitLine.length >= 3) {
							final String category = splitLine[2];
							categories.add(category);
						}
					}
				}
				for (final String category : categories) {
					if (category.length() == 4) {
						localeOptions.add(category);
					}
				}
			}
			if (localeOptions.isEmpty()) {
				localeOptions.addAll(Arrays.asList("zhCN", "ruRU", "esES", "itIT", "zhTW",
						"frFR", "enUS", "koKR", "deDE", "plPL"));
			}
			final JPanel userChooseLocalePanel = new JPanel();
			userChooseLocalePanel.setLayout(new GridLayout(localeOptions.size() + 5, 1));
			userChooseLocalePanel.add(new JLabel("Originally installed locale: " + originalInstallLocale
					+ ", Launcher.db locale: " + launcherDbLocale));
			userChooseLocalePanel.add(
					new JLabel("Locale could not be determined automatically. Please choose your locale."));
			userChooseLocalePanel
					.add(new JLabel("An incorrect choice may cause the Retera Model Studio to fail to start."));
			userChooseLocalePanel.add(new JLabel(
					"Any option is valid if you have started the game using that locale at least once."));
			final ButtonGroup buttonGroup = new ButtonGroup();
			final List<JRadioButton> buttons = new ArrayList<>();
			boolean firstGoodButton = true;
			for (final String localeOptionString : localeOptions) {
				final JRadioButton radioButton = new JRadioButton(localeOptionString);
				boolean bad = false;
				switch (patchFormat) {
					case PATCH130: {
						final String filePathToTest = localeOptionString.toLowerCase()
								+ "-war3local.mpq\\units\\campaignunitstrings.txt";
						if (!tempCascReader.getRootFileSystem().isFile(filePathToTest)
								|| !tempCascReader.getRootFileSystem().isFileAvailable(filePathToTest)) {
							radioButton.setForeground(Color.RED.darker());
							bad = true;
						}
						break;
					}
					case PATCH132:
					case PATCH131: {
						final String filePathToTest = "war3.w3mod\\_locales\\" + localeOptionString.toLowerCase()
								+ ".w3mod\\units\\campaignunitstrings.txt";
						if (!tempCascReader.getRootFileSystem().isFile(filePathToTest)
								|| !tempCascReader.getRootFileSystem().isFileAvailable(filePathToTest)) {
							radioButton.setForeground(Color.RED.darker());
							bad = true;
						}
						break;
					}
					default:
						break;
				}
				userChooseLocalePanel.add(radioButton);
				buttonGroup.add(radioButton);
				buttons.add(radioButton);
				if (!bad && (firstGoodButton || localeOptionString.toLowerCase().equals("enus"))) {
					firstGoodButton = false;
					radioButton.setSelected(true);
				}
			}
			final int confirmationResult = allowPopup
					? JOptionPane.showConfirmDialog(DataSourceChooserPanel.this, userChooseLocalePanel,
					"Choose Locale", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
					: JOptionPane.OK_OPTION;
			JRadioButton selectedButton = null;
			for (final JRadioButton button : buttons) {
				if (button.isSelected()) {
					selectedButton = button;
				}
			}
			if ((confirmationResult != JOptionPane.OK_OPTION)) {
				return null;
			}
			if (selectedButton == null) {
				JOptionPane.showMessageDialog(DataSourceChooserPanel.this,
						"User did not choose a locale! Aborting!", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			locale = selectedButton.getText();
		}
		return locale;
	}

	public static void setupLookAndFeel(final String jtattooTheme) {

		// setup the look and feel properties
		final Properties props = new Properties();
		// props.put("windowDecoration", "false");
		//
		props.put("logoString", "RMS");
		// props.put("licenseKey", "INSERT YOUR LICENSE KEY HERE");
		//
		// props.put("selectionBackgroundColor", "180 240 197");
		// props.put("menuSelectionBackgroundColor", "180 240 197");
		//
		// props.put("controlColor", "218 254 230");
		// props.put("controlColorLight", "218 254 230");
		// props.put("controlColorDark", "180 240 197");
		//
		// props.put("buttonColor", "218 230 254");
		// props.put("buttonColorLight", "255 255 255");
		// props.put("buttonColorDark", "244 242 232");
		//
		// props.put("rolloverColor", "218 254 230");
		// props.put("rolloverColorLight", "218 254 230");
		// props.put("rolloverColorDark", "180 240 197");
		//
		// props.put("windowTitleForegroundColor", "0 0 0");
		// props.put("windowTitleBackgroundColor", "180 240 197");
		// props.put("windowTitleColorLight", "218 254 230");
		// props.put("windowTitleColorDark", "180 240 197");
		// props.put("windowBorderColor", "218 254 230");

		// set your theme
		switch (jtattooTheme) {
			case "Noire" -> NoireLookAndFeel.setCurrentTheme(props);
			case "HiFi" -> HiFiLookAndFeel.setCurrentTheme(props);
			case "Acryl" -> AcrylLookAndFeel.setCurrentTheme(props);
			case "Aluminium" -> AluminiumLookAndFeel.setCurrentTheme(props);
		}
		// select the Look and Feel
		try {
			UIManager.setLookAndFeel(
					"com.jtattoo.plaf." + jtattooTheme.toLowerCase() + "." + jtattooTheme + "LookAndFeel");
		} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
