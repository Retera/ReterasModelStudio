package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.List;

public class DuplicateAnimationAction implements UndoAction {
	private final List<UndoAction> undoActions = new ArrayList<>();
	private final ModelStructureChangeListener changeListener;

	public DuplicateAnimationAction(EditableModel model, Sequence sequenceToCopy, String name, ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;

		Sequence newSequence = sequenceToCopy.deepCopy();

		if(newSequence instanceof Animation && name != null){
			((Animation) newSequence).setName(name);
		}


		for (AnimFlag<?> animFlag : ModelUtils.getAllAnimFlags(model)) {
			if(animFlag.hasSequence(sequenceToCopy)){
				AddFlagEntryMapAction<?> addAction = getAddEntryMapAction(sequenceToCopy, newSequence, animFlag);
				undoActions.add(addAction);
			}
		}

		for (EventObject e : model.getEvents()) {
			if (e.hasSequence(sequenceToCopy)) {
				undoActions.add(new AddEventTrackAction(e, newSequence, new ArrayList<>(e.getEventTrack(sequenceToCopy)), null));
			}
		}

		undoActions.add(new AddSequenceAction(model, newSequence, null));
	}

	private <Q> AddFlagEntryMapAction<Q> getAddEntryMapAction(Sequence sequenceToCopy, Sequence sequence, AnimFlag<Q> animFlag) {
		return new AddFlagEntryMapAction<>(animFlag, sequence, animFlag.getSequenceEntryMapCopy(sequenceToCopy), null);
	}

	@Override
	public UndoAction undo() {
		for(UndoAction undoAction : undoActions){
			undoAction.undo();
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(UndoAction undoAction : undoActions){
			undoAction.redo();
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Duplicate Sequence";
	}

}
