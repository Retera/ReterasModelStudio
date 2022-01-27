package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.EditUVsPanel;
import com.hiveworkshop.rms.ui.application.MainPanelLinkActions;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPanel;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class ToolsMenu extends JMenu {

	public ToolsMenu(MainPanelLinkActions linkActions) {
		super("Tools");

		setMnemonic(KeyEvent.VK_T);
		getAccessibleContext().setAccessibleDescription("Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");

		add(new ScaleModel().getMenuItem());

		add(new ViewMatrices().getMenuItem());
		add(new FlipFaces().getMenuItem());
		add(new FlipNormals().getMenuItem());

		add(new JSeparator());

		add(createMenuItem("Edit UV Mapping", KeyEvent.VK_U, e -> EditUVsPanel.showEditUVs()));

//		RootWindowUgg rootWindow = ProgramGlobals.getRootWindowUgg();
//		WindowHandler2 windowHandler2 = rootWindow.getWindowHandler2();
//		add(createMenuItem("Edit UV Mapping2", KeyEvent.VK_A, e -> windowHandler2.openNewWindowWithKB(new UVView("Edit UV Mapping").setModelPanel(ProgramGlobals.getCurrentModelPanel()), rootWindow)));

		JMenuItem editTextures = new JMenuItem("Edit Textures");
		editTextures.setMnemonic(KeyEvent.VK_T);
		editTextures.addActionListener(e -> EditTexturesPanel.showPanel());
		add(editTextures);

		add(new RigSelection().getMenuItem());

		JMenu tweaksSubmenu = new JMenu("Tweaks");
		tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
		tweaksSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to tweak conversion mistakes.");
		add(tweaksSubmenu);
//		createAndAddMenuItem("Flip All UVs U", tweaksSubmenu, KeyEvent.VK_U, e -> ModelEditActions.flipAllUVsU());
		tweaksSubmenu.add(createMenuItem("Flip All UVs U", KeyEvent.VK_U, e -> ModelEditActions.flipAllUVsU()));

		JMenuItem flipAllUVsV = new JMenuItem("Flip All UVs V");
		// flipAllUVsV.setMnemonic(KeyEvent.VK_V);
		flipAllUVsV.addActionListener(e -> ModelEditActions.flipAllUVsV());
		tweaksSubmenu.add(flipAllUVsV);

		tweaksSubmenu.add(createMenuItem("Swap All UVs U for V", KeyEvent.VK_S, e -> ModelEditActions.inverseAllUVs()));

		JMenu mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		add(mirrorSubmenu);

		JCheckBoxMenuItem mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
		mirrorFlip.setMnemonic(KeyEvent.VK_A);

		mirrorSubmenu.add(createMenuItem("Mirror X", KeyEvent.VK_X, e -> ModelEditActions.mirrorAxis((byte) 0, mirrorFlip.isSelected())));
		mirrorSubmenu.add(createMenuItem("Mirror Y", KeyEvent.VK_Y, e -> ModelEditActions.mirrorAxis((byte) 1, mirrorFlip.isSelected())));
		mirrorSubmenu.add(createMenuItem("Mirror Z", KeyEvent.VK_Z, e -> ModelEditActions.mirrorAxis((byte) 2, mirrorFlip.isSelected())));

		mirrorSubmenu.add(new JSeparator());

		mirrorSubmenu.add(mirrorFlip);
	}
}
