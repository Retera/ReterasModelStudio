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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class DataSourceChooserPanel extends JPanel {

	private final DataSourceTree dataSourceTree;
	private final DataSourceTracker dataSourceTracker;

	public DataSourceChooserPanel(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		setLayout(new MigLayout("fill, gap 0", "[sg group1][grow][sg group1]", "[][]"));
		dataSourceTracker = new DataSourceTracker(dataSourceDescriptorDefaults, this);
		dataSourceTree = new DataSourceTree(dataSourceTracker.getDataSourceDescriptors(), this);

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
		dataSourceTree.reloadTree();
	}

	private JPanel getBottomPanel() {
		JLabel warcraft3InstallLocated = new JLabel("'Path' Registry Key: ");
		warcraft3InstallLocated.setFont(new Font("Consolas", Font.BOLD, getFont().getSize()));

		String wcDirectory = dataSourceTracker.getWcDirectory();
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
		JButton addWarcraft3Installation = getButton("Add War3 Install Directory", e -> dataSourceTracker.addWar3InstallDirectory(dataSourceTree::reloadTree), true);
		JButton resetAllToDefaults = getButton("Reset to Defaults", e -> loadDefaults(), true);

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
		JButton addCASCButton = getButton("Add CASC", e -> dataSourceTracker.addCASC(dataSourceTree::reloadTree), true);
		JButton addMPQButton = getButton("Add MPQ", e -> dataSourceTracker.addMPQ(dataSourceTree::reloadTree), true);
		JButton addFolderButton = getButton("Add Folder", e -> dataSourceTracker.addFolder(dataSourceTree::reloadTree), true);

		JPanel rightPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		rightPanel.add(addCASCButton, "growx, wrap");
		rightPanel.add(addMPQButton, "growx, wrap");
		rightPanel.add(addFolderButton, "growx, wrap");
		rightPanel.add(new JLabel("-----"), "alignx center, wrap");
		rightPanel.add(dataSourceTree.getAddDefaultCascButton(), "growx, wrap");
		rightPanel.add(dataSourceTree.getAddSpecificCascButton(), "growx, wrap");
		rightPanel.add(new JLabel("-----"), "alignx center, wrap");
		rightPanel.add(dataSourceTree.getDeleteButton(), "growx, wrap");
		rightPanel.add(dataSourceTree.getMoveUpButton(), "growx, wrap");
		rightPanel.add(dataSourceTree.getMoveDownButton(), "growx, wrap");

		return rightPanel;
	}

	private JButton getButton(String buttonText, ActionListener actionListener, boolean setEnabled) {
		JButton button = new JButton(buttonText);
		button.addActionListener(actionListener);
		button.setEnabled(setEnabled);
		return button;
	}

	private void clearAll() {
		dataSourceTracker.clear();
		dataSourceTree.reloadTree();
	}

	private void enterSDMode() {
		CascDataSourceDescriptor casc = dataSourceTracker.getCascDataSourceDescriptor();
		if (casc != null) {
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
		CascDataSourceDescriptor casc  = dataSourceTracker.getCascDataSourceDescriptor();
		if (casc != null) {
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

	protected void loadDefaults() {
		dataSourceTracker.loadDefaults(null);
		dataSourceTree.reloadTree();
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceTracker.getDataSourceDescriptors();
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