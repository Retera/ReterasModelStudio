package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.ProgramVersion;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DataSourceChooserPanel extends JPanel {

	private final DataSourceTree dataSourceTree;
	private final DataSourceTracker dataSourceTracker;

	public DataSourceChooserPanel(final List<DataSourceDescriptor> currentDescriptors) {
		setLayout(new MigLayout("fill, gap 0", "[sg group1][grow][sg group1]", "[][]"));
		dataSourceTracker = new DataSourceTracker(this);
		dataSourceTree = new DataSourceTree(dataSourceTracker.getInitialDescriptors(currentDescriptors), this);

		JPanel leftPanel = getLeftPanel();
		JPanel rightPanel = getRightPanel();

		JScrollPane dstScrollpane = new JScrollPane(dataSourceTree);
		dstScrollpane.setPreferredSize(new Dimension(500, 400));

		JPanel bottomPanel = getBottomPanel();

		add(leftPanel, "growy");
		add(dstScrollpane, "growx, growy");
		add(rightPanel, "growy, wrap");
		add(bottomPanel, "spanx");
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
		JButton clearList = Button.create("Clear All", e -> clearAll());
		JButton addWarcraft3Installation = Button.create("Add War3 Install Directory", e -> dataSourceTree.addDataSources(dataSourceTracker.getWar3InstallDirectory()));
		JButton resetAllToDefaults = Button.create("Reset to Defaults", e -> loadDefaults());

		JButton enterHDMode = Button.create("Reforged Graphics Mode", e -> enterHDMode());
		JButton enterSDMode = Button.create("Classic Graphics Mode", e -> enterSDMode());

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
		JButton addCASCButton = Button.create("Add CASC", e -> dataSourceTree.addDataSources(dataSourceTracker.getCASC()));
		JButton addMPQButton = Button.create("Add MPQ", e -> dataSourceTree.addDataSources(dataSourceTracker.getMPQ()));
		JButton addFolderButton = Button.create("Add Folder", e -> dataSourceTree.addDataSources(dataSourceTracker.getFolder()));

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

	private void clearAll() {
		dataSourceTree.clearAll();
	}

	private void enterSDMode() {
		CascDataSourceDescriptor casc = dataSourceTree.getCascDataSourceDescriptor();
		if (casc != null) {
			if (casc.getPrefixes().size() == 5) {
				casc.removePrefix(4);
				casc.removePrefix(3);
				dataSourceTree.rebuildTree();
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
		CascDataSourceDescriptor casc  = dataSourceTree.getCascDataSourceDescriptor();
		if (casc != null) {
			if (casc.getPrefixes().size() == 3) {
				String localesMod = getLocalesMod(casc);
				if (localesMod != null) {
					casc.addPrefix("war3.w3mod\\_hd.w3mod");
					casc.addPrefix(localesMod.replace("_locales", "_hd.w3mod\\_locales"));
					dataSourceTree.rebuildTree();
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
		dataSourceTree.clearAll();
		dataSourceTree.addDataSources(dataSourceTracker.getInitialDescriptors(null));
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceTree.getDataSourceDescriptors();
	}


	private void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static boolean showDataSourceChooser(List<DataSourceDescriptor> dataSources) {
		final DataSourceChooserPanel dataSourceChooserPanel = new DataSourceChooserPanel(dataSources);

		int opt = JOptionPane.showConfirmDialog(null, dataSourceChooserPanel,
				"Retera Model Studio " + ProgramVersion.get() + ": Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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