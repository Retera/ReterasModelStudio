package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class UVView extends ModelDependentView {
	//	ImageIcon UVIcon = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png")));
//	UVPanel uvPanel;
	UVDisplayPanel uvDisplayPanel;
	JPanel dudPanel;

	public UVView(String s) {
		super("Texture Coordinate Editor", new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadTabImage("UVMap.png"))), new JPanel());
//		uvPanel = new UVPanel();
		uvDisplayPanel = new UVDisplayPanel();
		dudPanel = new JPanel(new MigLayout());
		this.setComponent(dudPanel);
	}

	@Override
	public UVView setModelPanel(ModelPanel modelPanel) {
		if (modelPanel == null) {
			getViewProperties().setTitle("Texture Coordinate Editor");

			this.setComponent(dudPanel);
		} else {
			System.out.println("UVView: setModelPanel");
			getViewProperties().setTitle("Texture Coordinate Editor: " + modelPanel.getModel().getName());
			uvDisplayPanel.setModel(modelPanel.getModelHandler(), modelPanel.getUVViewportActivityManager());
//			UVPanel panel = this.uvPanel.setModel(modelPanel.getModelHandler());
			System.out.println("UVView: model set, initing panel");
//			panel.initViewport();
//			panel.init();
			System.out.println("UVView: panel initiated, getting menu holder panel");
//			JPanel menuHolderPanel = panel.getMenuHolderPanel();
			System.out.println("UVView: menu holder panel gotten, setting component");
//			this.setComponent(menuHolderPanel);
			this.setComponent(uvDisplayPanel);
			System.out.println("UVView: component set");
		}
		return this;
	}

	@Override
	public UVView reload() {
//		uvPanel.repaint();
		uvDisplayPanel.repaint();
		return this;
	}
}
