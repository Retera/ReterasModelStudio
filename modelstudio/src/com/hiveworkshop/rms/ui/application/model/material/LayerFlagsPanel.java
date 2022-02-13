package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFlagAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LayerFlagsPanel extends JPanel {

	private final ModelHandler modelHandler;
	private Layer layer;

	public LayerFlagsPanel(ModelHandler modelHandler, Layer layer) {
		super(new MigLayout("ins 3, gap 3, wrap 1", "", ""));
		setBorder(BorderFactory.createTitledBorder("Flags"));
		this.modelHandler = modelHandler;
		this.layer = layer;

//		setOpaque(true);
//		setBackground(Color.MAGENTA);

		JCheckBox unshaded = new JCheckBox(MdlUtils.TOKEN_UNSHADED);
		unshaded.setSelected(layer.getUnshaded());
		unshaded.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNSHADED));
		add(unshaded, "");

		JCheckBox sphereEnvMap = new JCheckBox(MdlUtils.TOKEN_SPHERE_ENV_MAP);
		sphereEnvMap.setSelected(layer.getSphereEnvMap());
		sphereEnvMap.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_SPHERE_ENV_MAP));
		add(sphereEnvMap, "");

		JCheckBox twoSided = new JCheckBox(MdlUtils.TOKEN_TWO_SIDED);
		twoSided.setSelected(layer.getTwoSided());
		twoSided.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_TWO_SIDED));
		add(twoSided, "");

		JCheckBox unfogged = new JCheckBox(MdlUtils.TOKEN_UNFOGGED);
		unfogged.setSelected(layer.getUnfogged());
		unfogged.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNFOGGED));
		add(unfogged, "");

		JCheckBox noDepthTest = new JCheckBox(MdlUtils.TOKEN_NO_DEPTH_TEST);
		noDepthTest.setSelected(layer.getNoDepthTest());
		noDepthTest.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_NO_DEPTH_TEST));
		add(noDepthTest, "");

		JCheckBox noDepthSet = new JCheckBox(MdlUtils.TOKEN_NO_DEPTH_SET);
		noDepthSet.setSelected(layer.getNoDepthSet());
		noDepthSet.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_NO_DEPTH_SET));
		add(noDepthSet, "");

		JCheckBox unlit = new JCheckBox(MdlUtils.TOKEN_UNLIT);
		unlit.setSelected(layer.getUnlit());
		unlit.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNLIT));
		add(unlit, "");
	}

	public LayerFlagsPanel(ModelHandler modelHandler) {
		super(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Flags"));
		this.modelHandler = modelHandler;

//		setOpaque(true);
//		setBackground(Color.MAGENTA);

		JCheckBox unshaded = new JCheckBox(MdlUtils.TOKEN_UNSHADED);
		unshaded.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNSHADED));
		add(unshaded, "wrap");

		JCheckBox sphereEnvMap = new JCheckBox(MdlUtils.TOKEN_SPHERE_ENV_MAP);
		sphereEnvMap.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_SPHERE_ENV_MAP));
		add(sphereEnvMap, "wrap");

		JCheckBox twoSided = new JCheckBox(MdlUtils.TOKEN_TWO_SIDED);
		twoSided.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_TWO_SIDED));
		add(twoSided, "wrap");

		JCheckBox unfogged = new JCheckBox(MdlUtils.TOKEN_UNFOGGED);
		unfogged.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNFOGGED));
		add(unfogged, "wrap");

		JCheckBox noDepthTest = new JCheckBox(MdlUtils.TOKEN_NO_DEPTH_TEST);
		noDepthTest.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_NO_DEPTH_TEST));
		add(noDepthTest, "wrap");

		JCheckBox noDepthSet = new JCheckBox(MdlUtils.TOKEN_NO_DEPTH_SET);
		noDepthSet.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_NO_DEPTH_SET));
		add(noDepthSet, "wrap");

		JCheckBox unlit = new JCheckBox(MdlUtils.TOKEN_UNLIT);
		unlit.addActionListener(e -> toggleFlag(MdlUtils.TOKEN_UNLIT));
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
