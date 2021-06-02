package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createAndAddMenuItem;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class ToolsMenu extends JMenu {

	public ToolsMenu(MainPanel mainPanel) {
		super("Tools");
		MainPanelLinkActions linkActions = mainPanel.getMainPanelLinkActions();

		setMnemonic(KeyEvent.VK_T);
		getAccessibleContext().setAccessibleDescription("Allows the user to use various model editing tools. (You must open a model before you may use this menu.)");

		JMenuItem showMatrices = new JMenuItem("View Selected \"Matrices\"");
		// showMatrices.setMnemonic(KeyEvent.VK_V);
		showMatrices.addActionListener(e -> ModelEditActions.viewMatrices(mainPanel));
		add(showMatrices);

		JMenuItem insideOut = new JMenuItem("Flip all selected faces");
		insideOut.setMnemonic(KeyEvent.VK_I);
		insideOut.addActionListener(e -> ModelEditActions.insideOut(mainPanel));
		insideOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
		add(insideOut);

		JMenuItem insideOutNormals = new JMenuItem("Flip all selected normals");
		insideOutNormals.addActionListener(e -> ModelEditActions.insideOutNormals(mainPanel));
		add(insideOutNormals);

		add(new JSeparator());

		add(createMenuItem("Edit UV Mapping", KeyEvent.VK_U, e -> EditUVsPanel.showEditUVs(mainPanel)));

		JMenuItem editTextures = new JMenuItem("Edit Textures");
		editTextures.setMnemonic(KeyEvent.VK_T);
		editTextures.addActionListener(e -> EditTexturesPopupPanel.show(mainPanel));
		add(editTextures);

		add(createMenuItem("Rig Selection", KeyEvent.VK_R, KeyStroke.getKeyStroke("control W"), e -> linkActions.rigActionRes(ProgramGlobals.getCurrentModelPanel())));
//		add(createMenuItem("Rig Selection", KeyEvent.VK_R, KeyStroke.getKeyStroke("control W"), mainPanel.rigAction));

		JMenu tweaksSubmenu = new JMenu("Tweaks");
		tweaksSubmenu.setMnemonic(KeyEvent.VK_T);
		tweaksSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to tweak conversion mistakes.");
		add(tweaksSubmenu);
		createAndAddMenuItem("Flip All UVs U", tweaksSubmenu, KeyEvent.VK_U, e -> ModelEditActions.flipAllUVsU(mainPanel));

		JMenuItem flipAllUVsV = new JMenuItem("Flip All UVs V");
		// flipAllUVsV.setMnemonic(KeyEvent.VK_V);
		flipAllUVsV.addActionListener(e -> ModelEditActions.flipAllUVsV(mainPanel));
		tweaksSubmenu.add(flipAllUVsV);

		createAndAddMenuItem("Swap All UVs U for V", tweaksSubmenu, KeyEvent.VK_S, e -> ModelEditActions.inverseAllUVs(mainPanel));

		JMenu mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");
		add(mirrorSubmenu);

		JCheckBoxMenuItem mirrorFlip = new JCheckBoxMenuItem("Automatically flip after mirror (preserves surface)", true);
		mirrorFlip.setMnemonic(KeyEvent.VK_A);

		mirrorSubmenu.add(createMenuItem("Mirror X", KeyEvent.VK_X, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 0, mirrorFlip.isSelected())));
		mirrorSubmenu.add(createMenuItem("Mirror Y", KeyEvent.VK_Y, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 1, mirrorFlip.isSelected())));
		mirrorSubmenu.add(createMenuItem("Mirror Z", KeyEvent.VK_Z, e -> ModelEditActions.mirrorAxis(mainPanel, (byte) 2, mirrorFlip.isSelected())));

		mirrorSubmenu.add(new JSeparator());

		mirrorSubmenu.add(mirrorFlip);
	}
}
