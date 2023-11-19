package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.tools.RoundKeyframesPanel;
import com.hiveworkshop.rms.ui.application.tools.SimplifyKeyframesPanel;
import com.hiveworkshop.rms.ui.preferences.panels.ProgramPrefWindow;

import javax.swing.*;
import java.awt.event.KeyEvent;

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

		add(createMenuItem("Preferences Window", KeyEvent.VK_P, e -> ProgramPrefWindow.showPanel()));
	}

	private JMenu getOptimizeMenu() {
		final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
//		optimizeMenu.add(new LinearizeAnimations().getMenuItem());
		optimizeMenu.add(new SmartLinearizeAnimations().getMenuItem());

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
}
