package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFlagAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LayerFlagsPanel extends JPanel {

	private final ModelHandler modelHandler;
	private Layer layer;

	public LayerFlagsPanel(ModelHandler modelHandler, Layer layer) {
		super(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Flags"));
		this.modelHandler = modelHandler;
		this.layer = layer;

//		setOpaque(true);
//		setBackground(Color.MAGENTA);

		JCheckBox unshaded = new JCheckBox("Unshaded");
		unshaded.setSelected(layer.getUnshaded());
		unshaded.addActionListener(e -> toggleFlag("Unshaded"));
		add(unshaded, "wrap");

		JCheckBox sphereEnvMap = new JCheckBox("SphereEnvMap");
		sphereEnvMap.setSelected(layer.getSphereEnvMap());
		sphereEnvMap.addActionListener(e -> toggleFlag("SphereEnvMap"));
		add(sphereEnvMap, "wrap");

		JCheckBox twoSided = new JCheckBox("TwoSided");
		twoSided.setSelected(layer.getTwoSided());
		twoSided.addActionListener(e -> toggleFlag("TwoSided"));
		add(twoSided, "wrap");

		JCheckBox unfogged = new JCheckBox("Unfogged");
		unfogged.setSelected(layer.getUnfogged());
		unfogged.addActionListener(e -> toggleFlag("Unfogged"));
		add(unfogged, "wrap");

		JCheckBox noDepthTest = new JCheckBox("NoDepthTest");
		noDepthTest.setSelected(layer.getNoDepthTest());
		noDepthTest.addActionListener(e -> toggleFlag("NoDepthTest"));
		add(noDepthTest, "wrap");

		JCheckBox noDepthSet = new JCheckBox("NoDepthSet");
		noDepthSet.setSelected(layer.getNoDepthSet());
		noDepthSet.addActionListener(e -> toggleFlag("NoDepthSet"));
		add(noDepthSet, "wrap");

		JCheckBox unlit = new JCheckBox("Unlit");
		unlit.setSelected(layer.getUnlit());
		unlit.addActionListener(e -> toggleFlag("Unlit"));
		add(unlit, "wrap");
	}

	public LayerFlagsPanel(ModelHandler modelHandler) {
		super(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Flags"));
		this.modelHandler = modelHandler;

//		setOpaque(true);
//		setBackground(Color.MAGENTA);

		JCheckBox unshaded = new JCheckBox("Unshaded");
		unshaded.addActionListener(e -> toggleFlag("Unshaded"));
		add(unshaded, "wrap");

		JCheckBox sphereEnvMap = new JCheckBox("SphereEnvMap");
		sphereEnvMap.addActionListener(e -> toggleFlag("SphereEnvMap"));
		add(sphereEnvMap, "wrap");

		JCheckBox twoSided = new JCheckBox("TwoSided");
		twoSided.addActionListener(e -> toggleFlag("TwoSided"));
		add(twoSided, "wrap");

		JCheckBox unfogged = new JCheckBox("Unfogged");
		unfogged.addActionListener(e -> toggleFlag("Unfogged"));
		add(unfogged, "wrap");

		JCheckBox noDepthTest = new JCheckBox("NoDepthTest");
		noDepthTest.addActionListener(e -> toggleFlag("NoDepthTest"));
		add(noDepthTest, "wrap");

		JCheckBox noDepthSet = new JCheckBox("NoDepthSet");
		noDepthSet.addActionListener(e -> toggleFlag("NoDepthSet"));
		add(noDepthSet, "wrap");

		JCheckBox unlit = new JCheckBox("Unlit");
		unlit.addActionListener(e -> toggleFlag("Unlit"));
		add(unlit, "wrap");
	}

//	public LayerFlagsPanel setLayer(Layer layer){
//
//		unshaded.setSelected(layer.getUnshaded());
//		sphereEnvMap.setSelected(layer.getSphereEnvMap());
//		twoSided.setSelected(layer.getTwoSided());
//		unfogged.setSelected(layer.getUnfogged());
//		noDepthTest.setSelected(layer.getNoDepthTest());
//		noDepthSet.setSelected(layer.getNoDepthSet());
//		unlit.setSelected(layer.getUnlit());
//		return this;
//	}

	private void toggleFlag(String flag) {
		if (layer != null) {
			modelHandler.getUndoManager().pushAction(new SetLayerFlagAction(layer, flag, ModelStructureChangeListener.changeListener).redo());
		}
	}
}
