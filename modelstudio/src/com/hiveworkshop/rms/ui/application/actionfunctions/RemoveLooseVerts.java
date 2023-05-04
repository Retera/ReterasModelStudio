package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.TwiPopup;

import java.util.HashSet;
import java.util.Set;

public class RemoveLooseVerts extends ActionFunction {
	public RemoveLooseVerts(){
		super(TextKey.REMOVE_LOOSE_VERTS, RemoveLooseVerts::doRemove);
	}

	public static void doRemove(ModelHandler modelHandler){
		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();

		Set<GeosetVertex> looseVerts = new HashSet<>();
		model.getGeosets().forEach(g -> g.getVertices().stream().filter(v -> v.getTriangles().isEmpty()).forEach(looseVerts::add));


		System.out.println("looseVertsToRemove: " + looseVerts.size());

		if (!looseVerts.isEmpty()) {
			TwiPopup.quickDismissPopup(ProgramGlobals.getMainPanel(), looseVerts.size() + " loose vertices removed", "Removed Loose Verts");

			DeleteAction deleteAction = new DeleteAction(looseVerts, modelView, false, ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction((deleteAction).redo());
		} else {
			TwiPopup.quickDismissPopup(ProgramGlobals.getMainPanel(), "Found no loose vertices", "No Loose Verts Removed");
		}
	}

}
