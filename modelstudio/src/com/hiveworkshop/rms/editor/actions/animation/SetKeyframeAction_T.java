package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class SetKeyframeAction_T<T> implements UndoAction {
	private final Runnable changeListener;
	private final Sequence sequence;
	private final AnimFlag<T> timeline;
	private final Entry<T> entry;
	private final Entry<T> orgEntry;
	private final int time;

	public SetKeyframeAction_T(AnimFlag<T> timeline, Entry<T> entry, Sequence sequence, Runnable changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.timeline = timeline;
		this.entry = entry;
		time = entry.time;
		orgEntry = timeline.getEntryAt(sequence, time);
	}

	@Override
	public SetKeyframeAction_T<T> undo() {
		if (orgEntry != null) {
			checkValid(orgEntry);
			timeline.addEntry(orgEntry, sequence);
		} else {
			timeline.removeKeyframe(time, sequence);
		}
		if (changeListener != null) {
			changeListener.run();
		}
		return this;
	}

	@Override
	public SetKeyframeAction_T<T> redo() {
		checkValid(entry);
		timeline.addEntry(entry, sequence);
		if (changeListener != null) {
			changeListener.run();
		}
		return this;
	}

	private void checkValid(Entry<T> entry) {
		if (timeline.tans() && entry.inTan == null) {
			throw new IllegalStateException(
					"Cannot set interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
		}
	}

	@Override
	public String actionName() {
		return "Set Keyframe";
	}
}
