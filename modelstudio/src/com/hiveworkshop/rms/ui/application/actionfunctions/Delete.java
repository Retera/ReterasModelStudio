package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Delete extends ActionFunction {
	public Delete() {
		super(TextKey.DELETE, Delete::deleteActionRes, "DELETE");
	}

	public static void deleteActionRes(ModelHandler modelHandler) {
		if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().getTimeSliderView().getTimeSliderPanel().getKeyframeHandler().deleteSelectedKeyframes();
		} else {
			ModelView modelView = modelHandler.getModelView();
			List<UndoAction> undoActions = new ArrayList<>();
			if (!modelView.getSelectedVertices().isEmpty()){
				boolean onlyTriangles = ProgramGlobals.getSelectionItemType() == SelectionItemTypes.FACE;
				DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), modelView, onlyTriangles, null);
				undoActions.add(deleteAction);
			}
			if (!modelView.getSelectedIdObjects().isEmpty() || !modelView.getSelectedCameras().isEmpty()){
				DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), modelView, null);
				undoActions.add(deleteNodesAction);
			}
			if (!undoActions.isEmpty()) {
				String name = undoActions.size() == 1 ? undoActions.get(0).actionName() : "Delete Components";

				CompoundAction compoundAction = new CompoundAction(name, undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);
				modelHandler.getUndoManager().pushAction(compoundAction.redo());
			}
		}
		ProgramGlobals.getMainPanel().repaintSelfAndChildren();
	}

	public static void deleteKeyframes(ModelHandler modelHandler, boolean useAllKFs, boolean visKFs) {
		TimeEnvironmentImpl timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();

		Collection<TimelineContainer> objects = new HashSet<>();
		if (useAllKFs) {
			objects.addAll(modelHandler.getModelView().getEditableIdObjects());
		} else {
			objects.addAll(modelHandler.getModelView().getSelectedIdObjects());
		}
		if (useAllKFs && visKFs) {
			for (Geoset geoset: modelHandler.getModelView().getEditableGeosets()) {
				if (modelHandler.getModelView().isEditable(geoset) && !geoset.getAnimFlags().isEmpty()) {
					objects.add(geoset);
				}
			}
		}

		List<UndoAction> actions = new ArrayList<>();
		for (TimelineContainer object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if (flag.hasEntryAt(sequence, time)) {
					actions.add(new RemoveFlagEntryAction<>(flag, time, sequence, null));
				}
			}
		}

//		KeyFrame keyFrame = timeToKey.get(time);
		if (!actions.isEmpty()) {
			ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
			modelHandler.getUndoManager().pushAction(new CompoundAction("delete keyframe", actions, changeListener::keyframesUpdated).redo());
		}
	}

	public Collection<TimelineContainer> getSelectionToUse(ModelHandler modelHandler, boolean useAllKFs, boolean visKFs) {
		Collection<TimelineContainer> collectionToUse = new HashSet<>();
		if (useAllKFs) {
			collectionToUse.addAll(modelHandler.getModelView().getEditableIdObjects());
		} else {
			collectionToUse.addAll(modelHandler.getModelView().getSelectedIdObjects());
		}
		if (useAllKFs && visKFs) {
			for (Geoset geoset: modelHandler.getModelView().getEditableGeosets()) {
				if (modelHandler.getModelView().isEditable(geoset) && !geoset.getAnimFlags().isEmpty()) {
					collectionToUse.add(geoset);
				}
			}
		}
		return collectionToUse;
	}
}
