package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.EditableModel;

/**
 * Attaches a track to a global sequence (creating a new one of the given
 * length when {@code newSequence} is true) or detaches it (pass null).
 */
public final class SetTrackGlobalSequenceAction implements UndoAction {
	private final EditableModel model;
	private final AnimFlag track;
	private final Integer newSequence;
	private final boolean addSequenceToModel;
	private final Integer oldSequence;
	private final boolean oldHasGlobalSeq;
	private final ModelStructureChangeListener listener;
	private boolean sequenceWasAdded;

	public SetTrackGlobalSequenceAction(final EditableModel model, final AnimFlag track, final Integer newSequence,
			final boolean addSequenceToModel, final ModelStructureChangeListener listener) {
		this.model = model;
		this.track = track;
		this.newSequence = newSequence;
		this.addSequenceToModel = addSequenceToModel;
		this.oldSequence = track.getGlobalSeq();
		this.oldHasGlobalSeq = track.hasGlobalSeq();
		this.listener = listener;
	}

	private static int indexOfIdentity(final java.util.List<Integer> list, final Integer item) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == item) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void redo() {
		if ((newSequence != null) && addSequenceToModel
				&& (indexOfIdentity(model.getGlobalSeqs(), newSequence) < 0)) {
			model.add(newSequence);
			sequenceWasAdded = true;
		}
		track.setGlobalSeq(newSequence);
		track.setHasGlobalSeq(newSequence != null);
		listener.globalSequenceLengthChanged(indexOfIdentity(model.getGlobalSeqs(), newSequence), newSequence);
	}

	@Override
	public void undo() {
		track.setGlobalSeq(oldSequence);
		track.setHasGlobalSeq(oldHasGlobalSeq);
		if (sequenceWasAdded) {
			final int index = indexOfIdentity(model.getGlobalSeqs(), newSequence);
			if (index >= 0) {
				model.getGlobalSeqs().remove(index);
			}
			sequenceWasAdded = false;
		}
		listener.globalSequenceLengthChanged(indexOfIdentity(model.getGlobalSeqs(), oldSequence), oldSequence);
	}

	@Override
	public String actionName() {
		return newSequence == null ? "detach global sequence" : "attach global sequence";
	}
}
