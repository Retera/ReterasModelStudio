package com.hiveworkshop.wc3.gui.modeledit.components.material;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.mdl.Layer;
import hiveworkshop.localizationmanager.localizationmanager;

import net.miginfocom.swing.MigLayout;

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
		unshaded = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_unshaded"));
		add(unshaded, "wrap");
		sphereEnvMap = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_sphere_env_map"));
		add(sphereEnvMap, "wrap");
		twoSided = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_two_sided"));
		add(twoSided, "wrap");
		unfogged = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_unfogged"));
		add(unfogged, "wrap");
		noDepthTest = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_no_depth_test"));
		add(noDepthTest, "wrap");
		noDepthSet = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_no_depth_set"));
		add(noDepthSet, "wrap");
		unlit = new JCheckBox(LocalizationManager.getInstance().get("checkbox.layerflagspanel_unlit"));
		add(unlit, "wrap");
	}

	public void setLayer(final Layer layer) {
		unshaded.setSelected(layer.isUnshaded());
		sphereEnvMap.setSelected(layer.isSphereEnvironmentMap());
		twoSided.setSelected(layer.isTwoSided());
		unfogged.setSelected(layer.isUnfogged());
		noDepthTest.setSelected(layer.isNoDepthTest());
		noDepthSet.setSelected(layer.isNoDepthSet());
		unlit.setSelected(layer.isUnlit());
	}
}
