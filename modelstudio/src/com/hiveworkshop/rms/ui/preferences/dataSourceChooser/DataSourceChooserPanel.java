package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.WindowsRegistry;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataSourceChooserPanel extends JPanel {

	private final List<DataSourceDescriptor> dataSourceDescriptors;
	private final JFileChooser fileChooser;
	private final DataSourceTree dataSourceTree;
	private String wcDirectory;
	private final AddDataSources addDataSources = new AddDataSources();

	public DataSourceChooserPanel(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		setLayout(new MigLayout("fill, gap 0", "[sg group1][grow][sg group1]", "[][]"));
		dataSourceDescriptors = new ArrayList<>();
		fileChooser = new JFileChooser();

		getWindowsRegistryDirectory();

		dataSourceTree = new DataSourceTree(dataSourceDescriptors);

		JPanel leftPanel = getLeftPanel();

		JPanel rightPanel = getRightPanel();


		JScrollPane dstScrollpane = new JScrollPane(dataSourceTree);
		dstScrollpane.setPreferredSize(new Dimension(500, 400));

		JPanel bottomPanel = getBottomPanel();

		add(leftPanel, "growy");
		add(dstScrollpane, "growx, growy");
		add(rightPanel, "growy, wrap");
		add(bottomPanel, "spanx");

		dataSourceTree.setCellRenderer(new DataTreeRenderer());

		loadDefaults(dataSourceDescriptorDefaults);
	}

	private JPanel getBottomPanel() {
		JLabel warcraft3InstallLocated = new JLabel("'Path' Registry Key: ");
		warcraft3InstallLocated.setFont(new Font("Consolas", Font.BOLD, getFont().getSize()));

		JLabel warcraft3InstallPath = new JLabel(wcDirectory == null ? "Not found" : wcDirectory);
		warcraft3InstallPath.setFont(new Font("Consolas", Font.PLAIN, getFont().getSize()));
		if (wcDirectory == null) {
			warcraft3InstallPath.setForeground(Color.RED);
		}

		JPanel bottomPanel = new JPanel(new MigLayout("gap 0"));
		bottomPanel.add(warcraft3InstallLocated);
		bottomPanel.add(warcraft3InstallPath);
		return bottomPanel;
	}

	private JPanel getLeftPanel() {
		JButton clearList = getButton("Clear All", e -> clearAll(), true);
		JButton addWarcraft3Installation = getButton("Add War3 Install Directory", e -> addSource(addDataSources.addWar3InstallDirectory()), true);
		JButton resetAllToDefaults = getButton("Reset to Defaults", e -> loadDefaults(null), true);

		JButton enterHDMode = getButton("Reforged Graphics Mode", e -> enterHDMode(), true);
		JButton enterSDMode = getButton("Classic Graphics Mode", e -> enterSDMode(), true);

		JPanel leftPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		leftPanel.add(clearList, "growx, wrap");
		leftPanel.add(addWarcraft3Installation, "growx, wrap");
		leftPanel.add(resetAllToDefaults, "growx, wrap");
		leftPanel.add(new JLabel("-----"), "alignx center, wrap");
		leftPanel.add(enterHDMode, "growx, wrap");
		leftPanel.add(enterSDMode, "growx, wrap");
		return leftPanel;
	}

	private JPanel getRightPanel() {
		JButton addCASCButton = getButton("Add CASC", e -> addSource(addDataSources.addCASC()), true);
		JButton addMPQButton = getButton("Add MPQ", e -> addSource(addDataSources.addMPQ()), true);
		JButton addFolderButton = getButton("Add Folder", e -> addSource(addDataSources.addFolder()), true);

		JButton addDefaultCascPrefixes = getButton("Add Default CASC Mod", e -> dataSourceTree.addDefaultCASCMod(), false);
		JButton addSpecificCascPrefix = getButton("Add Specific CASC Mod", e -> dataSourceTree.addSpecificCASCMod(), false);

		JButton deleteSelection = getButton("Delete Selection", e -> dataSourceTree.deleteSelection(), false);
		JButton moveSelectionUp = getButton("Move Up", e -> dataSourceTree.move(true), false);
		JButton moveSelectionDown = getButton("Move Down", e -> dataSourceTree.move(false), false);

		JPanel rightPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		rightPanel.add(addCASCButton, "growx, wrap");
		rightPanel.add(addMPQButton, "growx, wrap");
		rightPanel.add(addFolderButton, "growx, wrap");
		rightPanel.add(new JLabel("-----"), "alignx center, wrap");
		rightPanel.add(addDefaultCascPrefixes, "growx, wrap");
		rightPanel.add(addSpecificCascPrefix, "growx, wrap");
		rightPanel.add(new JLabel("-----"), "alignx center, wrap");
		rightPanel.add(deleteSelection, "growx, wrap");
		rightPanel.add(moveSelectionUp, "growx, wrap");
		rightPanel.add(moveSelectionDown, "growx, wrap");

		dataSourceTree.addTreeSelectionListener(e -> dataSourceTreeListener(addDefaultCascPrefixes, addSpecificCascPrefix, deleteSelection, moveSelectionUp, moveSelectionDown, e));
		return rightPanel;
	}

	private JButton getButton(String buttonText, ActionListener actionListener, boolean setEnabled) {
		JButton button = new JButton(buttonText);
		button.addActionListener(actionListener);
		button.setEnabled(setEnabled);
		return button;
	}

	private void clearAll() {
		dataSourceDescriptors.clear();
		dataSourceTree.reloadTree();
	}

	private void dataSourceTreeListener(JButton addDefaultCascPrefixes,
	                                    JButton addSpecificCascPrefix,
	                                    JButton deleteSelection,
	                                    JButton moveSelectionUp,
	                                    JButton moveSelectionDown,
	                                    javax.swing.event.TreeSelectionEvent e) {

		TreePath selectionPath = e.getNewLeadSelectionPath();

		boolean cascSelected = dataSourceTree.isCascSelected(selectionPath);
		addDefaultCascPrefixes.setEnabled(cascSelected);
		addSpecificCascPrefix.setEnabled(cascSelected);

		deleteSelection.setEnabled(selectionPath != null);

		moveSelectionUp.setEnabled(dataSourceTree.canMoveUp(selectionPath));
		moveSelectionDown.setEnabled(dataSourceTree.canMoveDown(selectionPath));
	}


	private void getWindowsRegistryDirectory() {
		String usrSw = "HKEY_CURRENT_USER\\Software\\";
		String beW3 = "Blizzard Entertainment\\Warcraft III";
		wcDirectory = WindowsRegistry.readRegistry(usrSw + beW3, "InstallPathX");
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(usrSw + beW3, "InstallPathX");
		}
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(
					usrSw + "Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\" + beW3,
					"InstallPath");
		}
		if (wcDirectory != null) {
			wcDirectory = wcDirectory.trim();
			fileChooser.setCurrentDirectory(new File(wcDirectory));
		}
	}

	private void addSource(DataSourceDescriptor sourceDescriptor){
		if(sourceDescriptor != null){
			dataSourceDescriptors.add(sourceDescriptor);
			dataSourceTree.reloadTree();
		}
	}
	private void addSource(List<DataSourceDescriptor> sourceDescriptors){
		if(sourceDescriptors != null){
			dataSourceDescriptors.addAll(sourceDescriptors);
			dataSourceTree.reloadTree();
		}
	}

	private void enterSDMode() {
		if ((dataSourceDescriptors.size() == 1) && (dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor)) {
			CascDataSourceDescriptor casc = (CascDataSourceDescriptor) dataSourceDescriptors.get(0);
			if (casc.getPrefixes().size() == 5) {
				casc.deletePrefix(4);
				casc.deletePrefix(3);
				dataSourceTree.reloadTree();
			} else {
				showMessage("Your Warcraft III data CASC configuration is not in the HD mode.");
			}
		} else {
			showMessage(
					"Your Warcraft III data configuration is not a standard Reforged CASC setup, " +
							"so this automation feature is unavailable." +
							"\nTo use this feature, please press 'Clear All' and then " +
							"'Add War3 Install Directory' to choose a Reforged installation.");
		}
	}

	private void enterHDMode() {
		if ((dataSourceDescriptors.size() == 1) && (dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor)) {
			final CascDataSourceDescriptor casc = (CascDataSourceDescriptor) dataSourceDescriptors.get(0);
			if (casc.getPrefixes().size() == 3) {
				String localesMod = getLocalesMod(casc);
				if (localesMod != null) {
					casc.addPrefix("war3.w3mod\\_hd.w3mod");
					casc.addPrefix(localesMod.replace("_locales", "_hd.w3mod\\_locales"));
					dataSourceTree.reloadTree();
				} else {
					showMessage(
							"Your Warcraft III data CASC configuration is not in the SD mode or " +
									"is not configured in the expected way. You will need to apply " +
									"HD mode manually by adding the appropriate CASC mods.");
				}
			} else {
				showMessage("Your Warcraft III data CASC configuration is not in the SD mode.");
			}
		} else {
			showMessage(
					"Your Warcraft III data configuration is not a standard Reforged CASC setup, " +
							"so this automation feature is unavailable." +
							"\nTo use this feature, please press 'Clear All' and then " +
							"'Add War3 Install Directory' to choose a Reforged installation.");
		}
	}

	private String getLocalesMod(CascDataSourceDescriptor casc) {
		String localesMod = null;
		for (String possiblePrefix : casc.getPrefixes()) {
			if (possiblePrefix.contains("_locales")) {
				localesMod = possiblePrefix;
				break;
			}
		}
		return localesMod;
	}

	protected void loadDefaults(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		dataSourceDescriptors.clear();
		if (dataSourceDescriptorDefaults == null) {
			if (wcDirectory != null) {
				dataSourceDescriptors.addAll(addDataSources.addWarcraft3Installation(Paths.get(wcDirectory), false));
			}
		} else {
			for (final DataSourceDescriptor dataSourceDescriptor : dataSourceDescriptorDefaults) {
				dataSourceDescriptors.add(dataSourceDescriptor.duplicate());
			}
		}
		dataSourceTree.reloadTree();
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceDescriptors;
	}


	private void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static boolean showDataSourceChooser(List<DataSourceDescriptor> dataSources) {
		final DataSourceChooserPanel dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);

		int opt = JOptionPane.showConfirmDialog(null, dataSourceChooserPanel,
				"Retera Model Studio " + MainFrame.getVersion() + ": Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (opt == JOptionPane.OK_OPTION) {
			SaveProfile.get().setDataSources(dataSourceChooserPanel.getDataSourceDescriptors());
			SaveProfile.save();
			GameDataFileSystem.refresh(SaveProfile.get().getDataSources());

			// cache priority order...
			UnitOptionPanel.dropRaceCache();
			DataTableHolder.dropCache();
			ModelOptionPanel.dropCache();
			WEString.dropCache();
			BLPHandler.get().dropCache();
			return true;
		} else {
			return false;
		}
	}
}