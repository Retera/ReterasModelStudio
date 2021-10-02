package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.HashMap;
import java.util.Map;

public final class ScaleSequencesLengthsAction implements UndoAction {
	private final EditableModel mdl;
	private final Map<Sequence, Integer> sequenceToNewLength;
	private final Map<Sequence, Integer> sequenceToOldLength = new HashMap<>();
	private final ModelStructureChangeListener changeListener;

	public ScaleSequencesLengthsAction(EditableModel mdl, Map<Sequence, Integer> sequenceToNewLength, ModelStructureChangeListener changeListener) {
		this.mdl = mdl;
		this.sequenceToNewLength = sequenceToNewLength;
		this.changeListener = changeListener;

		for (Animation animation : mdl.getAnims()) {
			sequenceToOldLength.put(animation, animation.getLength());
		}
		for (GlobalSeq globalSeq : mdl.getGlobalSeqs()) {
			sequenceToOldLength.put(globalSeq, globalSeq.getLength());
		}
	}

	@Override
	public UndoAction undo() {
		setSequenceLengths(sequenceToOldLength);
		if (changeListener != null) {
			changeListener.keyframesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		setSequenceLengths(sequenceToNewLength);
		if (changeListener != null) {
			changeListener.keyframesUpdated();
		}
		return this;
	}

	private void setSequenceLengths(Map<Sequence, Integer> animLengthMap) {
		for (final AnimFlag<?> af : ModelUtils.getAllAnimFlags(mdl)) {
			for (Sequence sequence : af.getAnimMap().keySet()) {
				Integer newLength = animLengthMap.get(sequence);
				if (newLength != null && newLength != sequence.getLength()) {
					af.timeScale2(sequence, newLength, 0);
				}
			}
		}

		for (final EventObject e : mdl.getEvents()) {
			for (Sequence sequence : e.getEventTrackAnimMap().keySet()) {
				Integer newLength = animLengthMap.get(sequence);
				if (newLength != null && newLength != sequence.getLength()) {
					e.timeScale(sequence, newLength, 0);
				}
			}
		}
		animLengthMap.forEach(Sequence::setLength);
	}

	@Override
	public String actionName() {
		return "edit animation length(s)";
	}

}
