package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.FlipFacesAction;
import com.hiveworkshop.rms.editor.actions.tools.MirrorModelAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MirrorSelection {

	public static class MirrorX extends ActionFunction{
		MirrorX(){
			super(TextKey.MIRROR_X, MirrorX::mirror);
		}
		private static void mirror(ModelHandler modelHandler){
			mirrorAxis(modelHandler, Vec3.X_AXIS, true, null);
		}
	}

	public static class MirrorY extends ActionFunction{
		MirrorY(){
			super(TextKey.MIRROR_Y, MirrorY::mirror);
		}
		private static void mirror(ModelHandler modelHandler){
			mirrorAxis(modelHandler, Vec3.Y_AXIS, true, null);
		}
	}

	public static class MirrorZ extends ActionFunction{
		MirrorZ(){
			super(TextKey.MIRROR_Z, MirrorZ::mirror);
		}
		private static void mirror(ModelHandler modelHandler){
			mirrorAxis(modelHandler, Vec3.Z_AXIS, true, null);
		}
	}


	public static void mirrorAxis(ModelHandler modelHandler, Vec3 axis, boolean mirrorFlip, Vec3 center) {
		if (modelHandler != null) {
			ModelView modelView = modelHandler.getModelView();
			if(center == null){
				center = modelView.getSelectionCenter();
			}

			List<UndoAction> undoActions =  new ArrayList<>();
			MirrorModelAction mirror = new MirrorModelAction(modelView.getSelectedVertices(), modelView.getSelectedIdObjects(), axis, center);
			undoActions.add(mirror);
			if (mirrorFlip) {
				undoActions.add(new FlipFacesAction(modelView.getSelectedVertices()));
			}

			modelHandler.getUndoManager().pushAction(new CompoundAction(mirror.actionName(), undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
		}
	}
}
