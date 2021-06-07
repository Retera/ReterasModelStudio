package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.MainPanelLinkActions;
import com.hiveworkshop.rms.ui.application.MenuBarActions;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.tools.SimplifyKeyframesPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class EditMenu extends JMenu {

	public EditMenu(MainPanelLinkActions linkActions) {
		super("Edit");

//		setToolTipText("Allows the user to use various tools to edit the currently selected model.");
		getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model.");
		setMnemonic(KeyEvent.VK_E);
		add(ProgramGlobals.getUndoHandler().getUndo());

		add(ProgramGlobals.getUndoHandler().getRedo());


		add(new JSeparator());
		add(getOptimizeMenu());

		add(createMenuItem("Recalculate Normals", -1, KeyStroke.getKeyStroke("control N"), e -> ModelEditActions.recalculateNormals()));
		add(createMenuItem("Recalculate Extents", -1, KeyStroke.getKeyStroke("control shift E"), e -> ModelEditActions.recalculateExtents()));

		add(new JSeparator());

		TransferActionListener transferActionListener = new TransferActionListener();
		ActionListener copyActionListener = e -> MenuBarActions.copyCutPast(transferActionListener, e);

		add(createMenuItem("Cut", KeyStroke.getKeyStroke("control X"), (String) TransferHandler.getCutAction().getValue(Action.NAME), copyActionListener));
		add(createMenuItem("Copy", KeyStroke.getKeyStroke("control C"), (String) TransferHandler.getCopyAction().getValue(Action.NAME), copyActionListener));
		add(createMenuItem("Paste", KeyStroke.getKeyStroke("control V"), (String) TransferHandler.getPasteAction().getValue(Action.NAME), copyActionListener));
//		add(createMenuItem("Duplicate", -1, KeyStroke.getKeyStroke("control D"), mainPanel.cloneAction));
		add(createMenuItem("Duplicate", -1, KeyStroke.getKeyStroke("control D"), e -> linkActions.cloneActionRes()));

		add(new JSeparator());

//        add(createMenuItem("Snap Vertices", -1, KeyStroke.getKeyStroke("control shift W"), e -> MenuBarActions.getSnapVerticiesAction(mainPanel)));
		add(createMenuItem("Snap Vertices", -1, KeyStroke.getKeyStroke("control shift W"), e -> ModelEditActions.snapVertices()));
		add(createMenuItem("Snap Normals", -1, KeyStroke.getKeyStroke("control L"), e -> ModelEditActions.snapNormals()));

		add(new JSeparator());

		add(createMenuItem("Select All", -1, KeyStroke.getKeyStroke("control A"), e -> linkActions.selectAllActionRes()));
		add(createMenuItem("Invert Selection", -1, KeyStroke.getKeyStroke("control I"), e -> linkActions.invertSelectActionRes()));
		add(createMenuItem("Expand Selection", -1, KeyStroke.getKeyStroke("control E"), e -> linkActions.getExpandSelectionActionRes()));
//		add(createMenuItem("Select All", -1, KeyStroke.getKeyStroke("control A"), mainPanel.selectAllAction));
//		add(createMenuItem("Invert Selection", -1, KeyStroke.getKeyStroke("control I"), mainPanel.invertSelectAction));
//		add(createMenuItem("Expand Selection", -1, KeyStroke.getKeyStroke("control E"), mainPanel.expandSelectionAction));

		addSeparator();

		add(createMenuItem("Delete", KeyEvent.VK_D, e -> linkActions.deleteActionRes()));
//		add(createMenuItem("Delete", KeyEvent.VK_D, mainPanel.deleteAction));

		addSeparator();

		add(createMenuItem("Preferences Window", KeyEvent.VK_P, e -> MenuBarActions.openPreferences()));
	}

	private JMenu getOptimizeMenu() {
		final JMenu optimizeMenu = createMenu("Optimize", KeyEvent.VK_O);
		optimizeMenu.add(createMenuItem("Linearize Animations", KeyEvent.VK_L, e -> ModelEditActions.linearizeAnimations()));

		optimizeMenu.add(createMenuItem("Simplify Keyframes (Experimental)", KeyEvent.VK_K, e -> SimplifyKeyframesPanel.simplifyKeyframes()));

		optimizeMenu.add(createMenuItem("Simplify Keyframes tool", KeyEvent.VK_K, e -> SimplifyKeyframesPanel.showPopup()));

		optimizeMenu.add(createMenuItem("Minimize Geosets", KeyEvent.VK_K, e -> MenuBarActions.minimizeGeoset()));

		optimizeMenu.add(createMenuItem("Simplify Selected Geometry", KeyEvent.VK_K, e -> ModelEditActions.simplifyGeometry()));

		optimizeMenu.add(createMenuItem("Sort Nodes", KeyEvent.VK_S, e -> MenuBarActions.sortBones()));

		JMenuItem flushUnusedTexture = new JMenuItem("Flush Unused Texture");
		flushUnusedTexture.setEnabled(false);
		flushUnusedTexture.setMnemonic(KeyEvent.VK_F);
		optimizeMenu.add(flushUnusedTexture);

		optimizeMenu.add(createMenuItem("Remove Materials Duplicates", KeyEvent.VK_S, e -> MenuBarActions.removeMaterialDuplicates()));
		return optimizeMenu;
	}
}
