package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.MergeGeosetsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.*;

public class MinimizeGeosets extends ActionFunction {
	public MinimizeGeosets(){
		super(TextKey.MINIMIZE_GEOSETS, MinimizeGeosets::minimizeGeoset);

	}

	public static void minimizeGeoset(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();

		List<UndoAction> mergeActions = new ArrayList<>();
		Set<Geoset> geosetsToMerge = new HashSet<>();
		Set<Geoset> geosetsToKeep = new HashSet<>();

		for (Geoset geoset : model.getGeosets()) {
			for (Geoset retainedGeoset : geosetsToKeep) {
				if (isGeosetsMergable(geoset, retainedGeoset)) {
					geosetsToMerge.add(geoset);
					mergeActions.add(new MergeGeosetsAction(retainedGeoset, geoset, modelHandler.getModelView(), null));
					break;
				}
			}
			if (!geosetsToMerge.contains(geoset)) {
				geosetsToKeep.add(geoset);
			}
		}

		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		UndoAction undoAction = new CompoundAction("Minimize Geosets", mergeActions, changeListener::geosetsUpdated);
		modelHandler.getUndoManager().pushAction(undoAction.redo());
	}

	private static boolean isGeosetsMergable(Geoset geoset, Geoset retainedGeoset) {
		return retainedGeoset.getMaterial().equals(geoset.getMaterial())
				&& retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup()
				&& retainedGeoset.getUnselectable() == geoset.getUnselectable()
				&& retainedGeoset.isDropShadow() == geoset.isDropShadow()
				&& Math.abs(retainedGeoset.getStaticAlpha() - geoset.getStaticAlpha()) < 0.001
				&& retainedGeoset.getStaticColor().equalLocs(geoset.getStaticColor())
				&& equalFlags(retainedGeoset.getVisibilityFlag(), geoset.getVisibilityFlag())
				&& equalFlags(retainedGeoset.find("Color"), geoset.find("Color"));
	}

	private static boolean equalFlags(AnimFlag<?> flag1, AnimFlag<?> flag2) {
		return Objects.equals(flag1, flag2)
				|| flag1 == null && flag2.size() == 0
				|| flag2 == null && flag1.size() == 0;
	}
}
