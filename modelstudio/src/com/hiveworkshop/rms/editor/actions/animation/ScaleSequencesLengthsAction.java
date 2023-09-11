package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.*;

public final class ScaleSequencesLengthsAction implements UndoAction {
	private final Map<Sequence, Integer> sequenceToNewLength;
	private final Map<Sequence, Integer> sequenceToOldLength = new HashMap<>();
	private final ModelStructureChangeListener changeListener;
	private final List<UndoAction> undoActions = new ArrayList<>();

	public ScaleSequencesLengthsAction(EditableModel mdl, Map<Sequence, Integer> sequenceToNewLength, ModelStructureChangeListener changeListener) {
		this.sequenceToNewLength = sequenceToNewLength;
		this.changeListener = changeListener;

		for (Animation animation : mdl.getAnims()) {
			sequenceToOldLength.put(animation, animation.getLength());
		}
		for (GlobalSeq globalSeq : mdl.getGlobalSeqs()) {
			sequenceToOldLength.put(globalSeq, globalSeq.getLength());
		}


		ModelUtils.doForAnimFlags(mdl, af -> addChangeLengthAction(sequenceToNewLength, undoActions, af));

		for (final EventObject e : mdl.getEvents()) {
			addChangeLengthAction(sequenceToNewLength, undoActions, e);
		}
	}

	@Override
	public UndoAction undo() {
		undoActions.forEach(UndoAction::undo);
		sequenceToOldLength.forEach(Sequence::setLength);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		sequenceToNewLength.forEach(Sequence::setLength);
		undoActions.forEach(UndoAction::redo);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	private void addChangeLengthAction(Map<Sequence, Integer> animLengthMap, List<UndoAction> undoActions, EventObject e) {
		for (Sequence sequence : e.getEventTrackAnimMap().keySet()) {
			Integer newLength = animLengthMap.get(sequence);
			if (newLength != null && newLength != sequence.getLength()) {
				TreeSet<Integer> entryMapCopy = new TreeSet<>();
				TreeSet<Integer> eventTrack = e.getEventTrack(sequence);
				double ratio = newLength / ((double)sequence.getLength());
				eventTrack.forEach(i -> entryMapCopy.add((int)(i*ratio)));
				undoActions.add(new SetEventTrackAction(e, sequence, entryMapCopy, null));
			}
		}
	}

	private <Q> void addChangeLengthAction(Map<Sequence, Integer> animLengthMap, List<UndoAction> undoActions, AnimFlag<Q> af) {
		for (Sequence sequence : af.getAnimMap().keySet()) {
			Integer newLength = animLengthMap.get(sequence);
			if (newLength != null && newLength != sequence.getLength()) {
				TreeMap<Integer, Entry<Q>> entryMapCopy = af.getSequenceEntryMapCopy(sequence);
				double ratio = newLength / ((double)sequence.getLength());
				AnimFlagUtils.scaleMapEntries(ratio, entryMapCopy);
				undoActions.add(new SetFlagEntryMapAction<>(af, sequence, entryMapCopy, null));
			}
		}
	}

	@Override
	public String actionName() {
		return "edit animation length(s)";
	}

}
