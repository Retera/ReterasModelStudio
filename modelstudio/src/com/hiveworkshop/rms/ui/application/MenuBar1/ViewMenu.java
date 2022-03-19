package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.SmartButtonGroup;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ViewMenu extends JMenu {

	public ViewMenu() {
		super("View");
		setMnemonic(KeyEvent.VK_V);
		getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");

		JCheckBoxMenuItem textureModels = new JCheckBoxMenuItem("Texture Models", true);
		textureModels.setMnemonic(KeyEvent.VK_T);
		textureModels.setSelected(ProgramGlobals.getPrefs().textureModels());
		textureModels.addActionListener(e -> ProgramGlobals.getPrefs().setTextureModels(textureModels.isSelected()));
		textureModels.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(textureModels);

		JCheckBoxMenuItem showNormals = new JCheckBoxMenuItem("Show Normals", true);
		showNormals.setMnemonic(KeyEvent.VK_N);
		showNormals.setSelected(ProgramGlobals.getPrefs().showNormals());
		showNormals.addActionListener(e -> ProgramGlobals.getPrefs().setShowNormals(showNormals.isSelected()));
		showNormals.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showNormals);

		JCheckBoxMenuItem show3dVerts = new JCheckBoxMenuItem("Show 3D Vertices", true);
		show3dVerts.setMnemonic(KeyEvent.VK_V);
		show3dVerts.setSelected(ProgramGlobals.getPrefs().show3dVerts());
		show3dVerts.addActionListener(e -> ProgramGlobals.getPrefs().setShow3dVerts(show3dVerts.isSelected()));
		show3dVerts.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(show3dVerts);

		JCheckBoxMenuItem renderParticles = new JCheckBoxMenuItem("Render Particles", true);
		renderParticles.setMnemonic(KeyEvent.VK_P);
		renderParticles.setSelected(ProgramGlobals.getPrefs().getRenderParticles());
		renderParticles.addActionListener(e -> ProgramGlobals.getPrefs().setRenderParticles(renderParticles.isSelected()));
		renderParticles.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(renderParticles);

		JCheckBoxMenuItem showPerspectiveGrid = new JCheckBoxMenuItem("Show Perspective Grid", true);
		showPerspectiveGrid.setMnemonic(KeyEvent.VK_G);
		showPerspectiveGrid.setSelected(ProgramGlobals.getPrefs().showPerspectiveGrid());
		showPerspectiveGrid.addActionListener(e -> ProgramGlobals.getPrefs().setShowPerspectiveGrid(showPerspectiveGrid.isSelected()));
		showPerspectiveGrid.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showPerspectiveGrid);

		JMenuItem newDirectory = new JMenuItem("Change Game Directory");
		newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
		newDirectory.setToolTipText("Changes the directory from which to load texture files for the 3D display.");
		newDirectory.setMnemonic(KeyEvent.VK_D);
		newDirectory.addActionListener(e -> ProgramGlobals.getUndoHandler().refreshUndo());
//		viewMenu.add(newDirectory);

		add(new JSeparator());

		JCheckBoxMenuItem showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", true);
		// showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
		showVertexModifyControls.addActionListener(e -> ProgramGlobals.getPrefs().setShowVertexModifierControls(showVertexModifyControls.isSelected()));
		showVertexModifyControls.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showVertexModifyControls);

		add(new JSeparator());

		JMenu viewModeMenu = new JMenu("3D View Mode");
		add(viewModeMenu);

		SmartButtonGroup viewModes2 = new SmartButtonGroup();

		viewModes2.addJRadioButtonMenuItem("Wireframe", e -> ProgramGlobals.getPrefs().setViewMode(0));
		viewModes2.addJRadioButtonMenuItem("Solid", e -> ProgramGlobals.getPrefs().setViewMode(1));
		viewModes2.setSelectedIndex(ProgramGlobals.getPrefs().viewMode());

		viewModeMenu.add(viewModes2.getButton(0));
		viewModeMenu.add(viewModes2.getButton(1));
	}
}
