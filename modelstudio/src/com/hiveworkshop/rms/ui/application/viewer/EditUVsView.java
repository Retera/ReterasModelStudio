package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class EditUVsView extends ModelDependentView {
	private final UVPanel uvPanel;

	public EditUVsView(){
		super("Edit UVs", new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png"))), new JPanel());
		uvPanel = new UVPanel();
		uvPanel.initViewport();
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0, wrap 1, fill", "[grow]","[][grow]"));
		panel.add(uvPanel.getMenuBar());
		panel.add(uvPanel, "growx, growy");
		this.setComponent(panel);
	}

	@Override
	public ModelDependentView setModelPanel(ModelPanel modelPanel) {
		uvPanel.setModel(modelPanel);
		if(modelPanel != null){
//			uvPanel.setModel(modelPanel);
			System.out.println("set ModelPanel, ancestor: " + this.getTopLevelAncestor());
			SwingUtilities.invokeLater(() -> uvPanel.init());
			System.out.println("returning UV View");
		}
		reload();
		return this;
	}

	@Override
	public ModelDependentView reload() {
		uvPanel.repaint();
		return this;
	}
}
