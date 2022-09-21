package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.EditUVsPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.RootWindowUgg;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVView;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPanel;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class ToolsMenu extends JMenu {

	public ToolsMenu() {
		super("Tools");

		setMnemonic(KeyEvent.VK_T);
		getAccessibleContext().setAccessibleDescription("Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");

		add(new ScaleModel().getMenuItem());

		add(new ViewSkinning().getMenuItem());
		add(new FlipFaces().getMenuItem());
		add(new FlipNormals().getMenuItem());

		add(new JSeparator());

		add(createMenuItem("Edit UV Mapping", KeyEvent.VK_U, e -> EditUVsPanel.showEditUVs()));

		RootWindowUgg rootWindow = ProgramGlobals.getRootWindowUgg();
		add(createMenuItem("Edit UV Mapping2", KeyEvent.VK_A, e -> rootWindow.newWindow(new UVView("Edit UV Mapping").setModelPanel(ProgramGlobals.getCurrentModelPanel()))));

		JMenuItem editTextures = new JMenuItem("Edit Textures");
		editTextures.setMnemonic(KeyEvent.VK_T);
		editTextures.addActionListener(e -> EditTexturesPanel.showPanel());
		add(editTextures);

		add(new RigSelection().getMenuItem());

		JMenu tweaksSubmenu = new JMenu("Tweaks");
		tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
		tweaksSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to tweak conversion mistakes.");
		add(tweaksSubmenu);

		tweaksSubmenu.add(createMenuItem("Flip All UVs U", KeyEvent.VK_U, e -> FlipUVs.flipAllUVsDim(ProgramGlobals.getCurrentModelPanel().getModelHandler(), Vec2.X_AXIS, new Vec2(0.5, 0.5))));
		tweaksSubmenu.add(createMenuItem("Flip All UVs V", KeyEvent.VK_V, e -> FlipUVs.flipAllUVsDim(ProgramGlobals.getCurrentModelPanel().getModelHandler(), Vec2.Y_AXIS, new Vec2(0.5, 0.5))));

		tweaksSubmenu.add(new FlipUVs.FlipUVsX().getMenuItem());
		tweaksSubmenu.add(new FlipUVs.FlipUVsY().getMenuItem());

		tweaksSubmenu.add(createMenuItem("Swap All UVs U for V", KeyEvent.VK_S, e -> FlipUVs.InvertAllUVs.inverseAllUVs(ProgramGlobals.getCurrentModelPanel().getModelHandler())));

		JMenu mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		add(mirrorSubmenu);

		JCheckBoxMenuItem mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
		mirrorFlip.setMnemonic(KeyEvent.VK_A);

		mirrorSubmenu.add(createMenuItem("Mirror X", KeyEvent.VK_X, e -> MirrorSelection.mirrorAxis(ProgramGlobals.getCurrentModelPanel().getModelHandler(), (byte) 0, mirrorFlip.isSelected(), null)));
		mirrorSubmenu.add(createMenuItem("Mirror Y", KeyEvent.VK_Y, e -> MirrorSelection.mirrorAxis(ProgramGlobals.getCurrentModelPanel().getModelHandler(), (byte) 1, mirrorFlip.isSelected(), null)));
		mirrorSubmenu.add(createMenuItem("Mirror Z", KeyEvent.VK_Z, e -> MirrorSelection.mirrorAxis(ProgramGlobals.getCurrentModelPanel().getModelHandler(), (byte) 2, mirrorFlip.isSelected(), null)));

		mirrorSubmenu.add(new JSeparator());

		mirrorSubmenu.add(mirrorFlip);
	}
}
