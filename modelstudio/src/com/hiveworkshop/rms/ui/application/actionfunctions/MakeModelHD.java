package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.RecalculateTangentsAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetFormatVersionAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialShaderStringAction;
import com.hiveworkshop.rms.editor.actions.tools.ConvertToSkinBonesAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MakeModelHD extends ActionFunction {
	public MakeModelHD(){
		super(TextKey.SD_TO_HD, MakeModelHD::makeItHD);
	}

	public static void makeItHD(ModelHandler modelHandler) {
		if(modelHandler != null && modelHandler.getModel().getFormatVersion()==800){
			List<UndoAction> undoActions = new ArrayList<>();
			EditableModel model = modelHandler.getModel();
			undoActions.add(new SetFormatVersionAction(1000, model, null));

			for (Geoset geoset : model.getGeosets()) {
				undoActions.add(new RecalculateTangentsAction(geoset.getVertices()));
				undoActions.add(new ConvertToSkinBonesAction(geoset, null));
			}
			for (Material material : model.getMaterials()) {
				undoActions.add(new SetMaterialShaderStringAction(model, material, "Shader_HD_DefaultUnit", null));
			}
			for(IdObject idObject : model.getIdObjects()){
				System.out.println("making idObject HD");
				Vec3 vec3 = idObject.getPivotPoint();
				float[] bindPose = new float[]{
						0.0f,   0.0f,    0.0f,   0.0f,
						0.0f,   0.0f,    0.0f,   0.0f,
						0.0f,  vec3.x, vec3.y, vec3.z, };
				idObject.setBindPose(bindPose);
			}

			modelHandler.getUndoManager().pushAction(new CompoundAction("Make model HD", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
		}
	}
}
