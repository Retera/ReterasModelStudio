package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.editor.StaticMeshScaleAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ScaleModel extends ActionFunction {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";

	public ScaleModel(){
		super(TextKey.SCALE_MODEL, () -> showPopup());
	}

	public static void showPopup(){
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			StaticMeshScaleAction action = startScaleModel(new Vec3(0, 0, 0), modelHandler.getModel());
			Vec3 scaleVec = new Vec3(1,1,1);
			float[] scaleDiff = new float[]{1, 1};
			JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));
			SmartNumberSlider smartNumberSlider = new SmartNumberSlider("Scale", 100, 1, 300, (i) -> updateScaleModel(action, scaleVec, i / 100f, scaleDiff));
			smartNumberSlider.setMaxUpperLimit(1000);
			smartNumberSlider.setMinLowerLimit(1);
			sliderPanel.add(smartNumberSlider, SLIDER_CONSTRAINTS);
			int opt = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), sliderPanel, "Scale model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			applyScaleModel(modelHandler, action, opt == JOptionPane.OK_OPTION);
		}
	}

	private static StaticMeshScaleAction startScaleModel(Vec3 center, EditableModel model){
		Set<GeosetVertex> vertices = new HashSet<>();
		for(Geoset geoset : model.getGeosets()){
			vertices.addAll(geoset.getVertices());
		}
		return new StaticMeshScaleAction(vertices, model.getIdObjects(), model.getCameras(), center);
	}
	private static void updateScaleModel(StaticMeshScaleAction action, Vec3 scale, float scaleFloat, float[] lastScaleFloat){
		float newScale = 0f + (scaleFloat/lastScaleFloat[0]);
		lastScaleFloat[1]*=newScale;
//		System.out.println("scaleFloat: " + scaleFloat + ", lastScale: " + lastScaleFloat[0] + ", newScale: " + newScale + ", (" + lastScaleFloat[1] + ")");
		lastScaleFloat[0]=scaleFloat;
		scale.set(newScale,newScale,newScale);
		action.updateScale(scale);

	}
	private static void applyScaleModel(ModelHandler modelHandler, StaticMeshScaleAction action, boolean doApply){
		if(doApply){
			modelHandler.getUndoManager().pushAction(action);
		} else {
			action.undo();
		}
	}
}
