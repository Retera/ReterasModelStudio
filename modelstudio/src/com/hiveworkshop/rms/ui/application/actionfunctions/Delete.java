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

import java.util.*;

public class Delete extends ActionFunction {
	public Delete(){
		super(TextKey.DELETE, Delete::deleteActionRes, "DELETE");
	}

	public static void deleteActionRes(ModelHandler modelHandler) {
		if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().getTimeSliderView().getTimeSliderPanel().getKeyframeHandler().deleteSelectedKeyframes();
		} else {
			ModelView modelView = modelHandler.getModelView();
			DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), modelView, ProgramGlobals.getSelectionItemType() == SelectionItemTypes.FACE, ModelStructureChangeListener.changeListener);
			DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), ModelStructureChangeListener.changeListener, modelView.getModel());
			CompoundAction compoundAction = new CompoundAction("deleted components", Arrays.asList(deleteAction, deleteNodesAction));
			modelHandler.getUndoManager().pushAction(compoundAction.redo());
		}
		ProgramGlobals.getMainPanel().repaintSelfAndChildren();
	}

	public static void deleteKeyframes(ModelHandler modelHandler, boolean useAllKFs, boolean visKFs){
		TimeEnvironmentImpl timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
		int time = timeEnvironment.getEnvTrackTime();
		Sequence sequence = timeEnvironment.getCurrentSequence();

		Collection<TimelineContainer> objects = new HashSet<>();
		if(useAllKFs){
			objects.addAll(modelHandler.getModelView().getEditableIdObjects());
		} else {
			objects.addAll(modelHandler.getModelView().getSelectedIdObjects());
		}
		if(useAllKFs && visKFs){
			for(Geoset geoset: modelHandler.getModelView().getEditableGeosets()){
				if(modelHandler.getModelView().isEditable(geoset) && !geoset.getAnimFlags().isEmpty()){
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
		if(useAllKFs){
			collectionToUse.addAll(modelHandler.getModelView().getEditableIdObjects());
		} else {
			collectionToUse.addAll(modelHandler.getModelView().getSelectedIdObjects());
		}
		if(useAllKFs && visKFs){
			for(Geoset geoset: modelHandler.getModelView().getEditableGeosets()){
				if(modelHandler.getModelView().isEditable(geoset) && !geoset.getAnimFlags().isEmpty()){
					collectionToUse.add(geoset);
				}
			}
		}
		return collectionToUse;
	}
}
