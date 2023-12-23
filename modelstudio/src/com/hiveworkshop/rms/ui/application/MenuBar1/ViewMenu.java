package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ViewMenu extends JMenu {

	public ViewMenu() {
		super("View");
		setMnemonic(KeyEvent.VK_V);
		getAccessibleContext().setAccessibleDescription("Allows the user to control view settings.");

		JCheckBoxMenuItem showNodeForward = new JCheckBoxMenuItem("Show Node Forward", true);
		showNodeForward.setMnemonic(KeyEvent.VK_F);
		showNodeForward.setSelected(ProgramGlobals.getPrefs().showNodeForward());
		showNodeForward.addActionListener(e -> ProgramGlobals.getPrefs().setShowNodeForward(showNodeForward.isSelected()));
		showNodeForward.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showNodeForward);

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

		add(new JSeparator());
		SmartNumberSlider slider = new SmartNumberSlider("Node Size", ProgramGlobals.getPrefs().getNodeBoxSize(), 1, 20, s -> ProgramGlobals.getPrefs().setNodeBoxSize(s), false, true);
		slider.setMaxUpperLimit(100);
		slider.setLayout(new MigLayout("ins 0", "[left, 70][right, 100][right, 30]"));
		add(slider);
		add(new JSeparator());

		JCheckBoxMenuItem showVertexModifyControls = new JCheckBoxMenuItem("Show Viewport Buttons", ProgramGlobals.getPrefs().showVMControls());
		// showVertexModifyControls.setMnemonic(KeyEvent.VK_V);
		showVertexModifyControls.addActionListener(e -> ProgramGlobals.getPrefs().setShowVertexModifierControls(showVertexModifyControls.isSelected()));
		showVertexModifyControls.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
		add(showVertexModifyControls);

	}
}
