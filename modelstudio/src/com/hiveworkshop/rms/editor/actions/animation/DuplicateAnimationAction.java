package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.List;

public class DuplicateAnimationAction implements UndoAction {
	private final List<UndoAction> undoActions = new ArrayList<>();
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public DuplicateAnimationAction(EditableModel model, Sequence sequenceToCopy, String name, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		actionName = "Duplicate " + sequenceToCopy.getName();

		Sequence newSequence = sequenceToCopy.deepCopy();

		if (newSequence instanceof Animation && name != null) {
			newSequence.setName(name);
		}

		ModelUtils.doForAnimFlags(model, a -> {
			if (a.hasSequence(sequenceToCopy)) undoActions.add(getAddEntryMapAction(sequenceToCopy, newSequence, a));
		});

		for (EventObject e : model.getEvents()) {
			if (e.hasSequence(sequenceToCopy)) {
				undoActions.add(new AddEventTrackAction(e, newSequence, new ArrayList<>(e.getEventTrack(sequenceToCopy)), null));
			}
		}
		if (newSequence instanceof Animation) {
			for (Geoset g : model.getGeosets()) {
				ExtLog newExtLog = g.getAnimExtent((Animation) sequenceToCopy).deepCopy();
				undoActions.add(new ConsumerAction<>(e -> g.add((Animation) newSequence, e), newExtLog, null, "Geoset Ext"));
			}
		}

		undoActions.add(new AddSequenceAction(model, newSequence, null));
	}

	private <Q> AddFlagEntryMapAction<Q> getAddEntryMapAction(Sequence sequenceToCopy, Sequence sequence, AnimFlag<Q> animFlag) {
		return new AddFlagEntryMapAction<>(animFlag, sequence, animFlag.getSequenceEntryMapCopy(sequenceToCopy), null);
	}

	@Override
	public DuplicateAnimationAction undo() {
		for (UndoAction undoAction : undoActions) {
			undoAction.undo();
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public DuplicateAnimationAction redo() {
		for (UndoAction undoAction : undoActions) {
			undoAction.redo();
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

}
