package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.SmartButtonGroup;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ViewMenu extends JMenu {

	public ViewMenu(MainPanel mainPanel) {
		super("View");
		getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");

		JCheckBoxMenuItem textureModels = new JCheckBoxMenuItem("Texture Models", true);
		textureModels.setMnemonic(KeyEvent.VK_T);
		textureModels.setSelected(ProgramGlobals.prefs.textureModels());
		textureModels.addActionListener(e -> ProgramGlobals.prefs.setTextureModels(textureModels.isSelected()));
		textureModels.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(textureModels);

		JCheckBoxMenuItem showNormals = new JCheckBoxMenuItem("Show Normals", true);
		showNormals.setMnemonic(KeyEvent.VK_N);
		showNormals.setSelected(ProgramGlobals.prefs.showNormals());
		showNormals.addActionListener(e -> ProgramGlobals.prefs.setShowNormals(showNormals.isSelected()));
		showNormals.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showNormals);

		JCheckBoxMenuItem renderParticles = new JCheckBoxMenuItem("Render Particles", true);
		renderParticles.setMnemonic(KeyEvent.VK_P);
		renderParticles.setSelected(ProgramGlobals.prefs.getRenderParticles());
		renderParticles.addActionListener(e -> ProgramGlobals.prefs.setRenderParticles(renderParticles.isSelected()));
		renderParticles.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(renderParticles);

		JCheckBoxMenuItem showPerspectiveGrid = new JCheckBoxMenuItem("Show Perspective Grid", true);
		showPerspectiveGrid.setMnemonic(KeyEvent.VK_G);
		showPerspectiveGrid.setSelected(ProgramGlobals.prefs.showPerspectiveGrid());
		showPerspectiveGrid.addActionListener(e -> ProgramGlobals.prefs.setShowPerspectiveGrid(showPerspectiveGrid.isSelected()));
		showPerspectiveGrid.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showPerspectiveGrid);

		JMenuItem newDirectory = new JMenuItem("Change Game Directory");
		newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
		newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
		newDirectory.setMnemonic(KeyEvent.VK_D);
		newDirectory.addActionListener(e -> mainPanel.getUndoHandler().refreshUndo());
//		viewMenu.add(newDirectory);

		add(new JSeparator());

		JCheckBoxMenuItem showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
		// showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
		showVertexModifyControls.addActionListener(e -> ProgramGlobals.prefs.setShowVertexModifierControls(showVertexModifyControls.isSelected()));
		showVertexModifyControls.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showVertexModifyControls);

		add(new JSeparator());

		JMenu viewModeMenu = new JMenu("3D View Mode");
		add(viewModeMenu);

		SmartButtonGroup viewModes2 = new SmartButtonGroup();

		viewModes2.addJRadioButtonMenuItem("Wireframe", e -> ProgramGlobals.prefs.setViewMode(0));
		viewModes2.addJRadioButtonMenuItem("Solid", e -> ProgramGlobals.prefs.setViewMode(1));
		viewModes2.setSelectedIndex(ProgramGlobals.prefs.getViewMode());

		viewModeMenu.add(viewModes2.getButton(0));
		viewModeMenu.add(viewModes2.getButton(1));
	}
}
