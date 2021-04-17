package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Layer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LayerFlagsPanel extends JPanel {
	private final JCheckBox unshaded;
	private final JCheckBox sphereEnvMap;
	private final JCheckBox twoSided;
	private final JCheckBox unfogged;
	private final JCheckBox noDepthTest;
	private final JCheckBox noDepthSet;
	private final JCheckBox unlit;

	public LayerFlagsPanel() {
		setLayout(new MigLayout());
		unshaded = new JCheckBox("Unshaded");
		add(unshaded, "wrap");

		sphereEnvMap = new JCheckBox("SphereEnvMap");
		add(sphereEnvMap, "wrap");

		twoSided = new JCheckBox("TwoSided");
		add(twoSided, "wrap");

		unfogged = new JCheckBox("Unfogged");
		add(unfogged, "wrap");

		noDepthTest = new JCheckBox("NoDepthTest");
		add(noDepthTest, "wrap");

		noDepthSet = new JCheckBox("NoDepthSet");
		add(noDepthSet, "wrap");

		unlit = new JCheckBox("Unlit");
		add(unlit, "wrap");
	}

	public void setLayer(final Layer layer) {
		unshaded.setSelected(layer.getUnshaded());
		sphereEnvMap.setSelected(layer.getSphereEnvMap());
		twoSided.setSelected(layer.getTwoSided());
		unfogged.setSelected(layer.getUnfogged());
		noDepthTest.setSelected(layer.getNoDepthTest());
		noDepthSet.setSelected(layer.getNoDepthSet());
		unlit.setSelected(layer.getUnlit());
	}
}
