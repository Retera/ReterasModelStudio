package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTableHolder;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.tools.RoundKeyframesPanel;
import com.hiveworkshop.rms.ui.application.tools.SimplifyKeyframesPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQBrowser;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.FramePopup;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class EditMenu extends JMenu {

	public EditMenu() {
		super("Edit");

//		setToolTipText("Allows the user to use various tools to edit the currently selected model.");
		getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
		setMnemonic(KeyEvent.VK_E);

		add(ProgramGlobals.getUndoHandler().getUndo());
		add(ProgramGlobals.getUndoHandler().getRedo());

		add(new JSeparator());
		add(getOptimizeMenu());

		add(new RecalculateNormals().getMenuItem());
		add(new RecalculateExtents().getMenuItem());

		add(new JSeparator());

		add(CopyCutPast.getCutItem());
		add(CopyCutPast.getCopyItem());
		add(CopyCutPast.getPasteItem());
		add(new Duplicate().getMenuItem());

		add(new JSeparator());

		add(new SnapVertices().getMenuItem());
		add(new SnapNormals().getMenuItem());

		add(new JSeparator());

		add(Select.getSelectAllMenuItem());
		add(Select.getInvertSelectMenuItem());
		add(Select.getExpandSelectionMenuItem());
		add(Select.getShrinkSelectionMenuItem());
		add(Select.getSelectLinkedGeometryMenuItem());
		add(Select.getSelectNodeGeometryMenuItem());
		add(Select.getSelectGeometryNodesMenuItem());
		add(Select.getDeselectRandomMenuItem());

		addSeparator();

		add(new Delete().getMenuItem());

		addSeparator();

		add(createMenuItem("Preferences Window", KeyEvent.VK_P, e -> openPreferences()));
	}

	private JMenu getOptimizeMenu() {
		final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
		optimizeMenu.add(new LinearizeAnimations().getMenuItem());

		optimizeMenu.add(SimplifyKeyframesPanel.getMenuItem());
		optimizeMenu.add(RoundKeyframesPanel.getMenuItem());
		optimizeMenu.add(new MinimizeGeosets().getMenuItem());
		optimizeMenu.add(new SimplifyGeometry().getMenuItem());
		optimizeMenu.add(new SortNodes().getMenuItem());
		optimizeMenu.add(new RemoveUnusedBones().getMenuItem());
		optimizeMenu.add(new RemoveLooseVerts().getMenuItem());

		optimizeMenu.add(new RemoveMaterialDuplicates().getMenuItem());
		return optimizeMenu;
	}



	public static void openPreferences() {
		ProgramPreferences programPreferences = new ProgramPreferences();
		programPreferences.loadFrom(ProgramGlobals.getPrefs());
		List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
		ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences, priorDataSources);
		JPanel prefPanel = new JPanel(new MigLayout("fill"));
		prefPanel.add(programPreferencesPanel, "growx, growy, spanx, wrap");


		JButton okButton = new JButton("OK");
		prefPanel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		prefPanel.add(cancelButton);

		JFrame frame = FramePopup.get(prefPanel, ProgramGlobals.getMainPanel(), "Preferences");
		okButton.addActionListener(e -> {
			saveSettings(programPreferences, priorDataSources, programPreferencesPanel);
			frame.setVisible(false);
			frame.dispose();
		});
		cancelButton.addActionListener(e -> {frame.setVisible(false);frame.dispose();});

		frame.setVisible(true);

//		int ret = JOptionPane.showConfirmDialog(
//				ProgramGlobals.getMainPanel(),
//				programPreferencesPanel,
//				"Preferences",
//				JOptionPane.OK_CANCEL_OPTION,
//				JOptionPane.PLAIN_MESSAGE);
//
//		if (ret == JOptionPane.OK_OPTION) {
//			saveSettings(programPreferences, priorDataSources, programPreferencesPanel);
//		}
	}


	private static void saveSettings(ProgramPreferences programPreferences, List<DataSourceDescriptor> priorDataSources, ProgramPreferencesPanel programPreferencesPanel) {
		ProgramGlobals.getEditorColorPrefs().setFrom(programPreferencesPanel.getColorPrefs());
		programPreferences.setEditorColors(ProgramGlobals.getEditorColorPrefs());
		ProgramGlobals.getPrefs().loadFrom(programPreferences);
		List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
		boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
		if (changedDataSources) {
			SaveProfile.get().setDataSources(dataSources);
		}
		SaveProfile.save();
		if (changedDataSources) {
			updateDataSource();
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().dataSourcesChanged();
//				dataSourcesChanged(MenuBar.directoryChangeNotifier, mainPanel.modelPanels);
		}
	}

	public static void updateDataSource() {
		GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
		// cache priority order...
		UnitOptionPanel.dropRaceCache();
		DataTableHolder.dropCache();
		ModelOptionPanel.dropCache();
		WEString.dropCache();
		BLPHandler.get().dropCache();
		ProgramGlobals.getMenuBar().updateTeamColors();
		traverseAndReloadData(ProgramGlobals.getRootWindowUgg());
	}


	public static void traverseAndReloadData(DockingWindow window) {
		int childWindowCount = window.getChildWindowCount();
		for (int i = 0; i < childWindowCount; i++) {
			DockingWindow childWindow = window.getChildWindow(i);
			traverseAndReloadData(childWindow);
			if (childWindow instanceof View) {
				View view = (View) childWindow;
				Component component = view.getComponent();
				if (component instanceof JScrollPane) {
					JScrollPane pane = (JScrollPane) component;
					Component viewportView = pane.getViewport().getView();
					if (viewportView instanceof UnitEditorTree) {
						UnitEditorTree unitEditorTree = (UnitEditorTree) viewportView;
						WorldEditorDataType dataType = unitEditorTree.getDataType();
						if (dataType == WorldEditorDataType.UNITS) {
							System.out.println("saw unit tree");
							unitEditorTree.setUnitDataAndReloadVerySlowly();
						} else if (dataType == WorldEditorDataType.DOODADS) {
							System.out.println("saw doodad tree");
							unitEditorTree.setUnitDataAndReloadVerySlowly();
						}
					}
				} else if (component instanceof MPQBrowser) {
					System.out.println("saw mpq tree");
					MPQBrowser comp = (MPQBrowser) component;
					comp.refreshTree();
				}
			}
		}
	}
}
