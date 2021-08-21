package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;

public class AddKeyframeAction_T<T> implements UndoAction {
	private final AnimFlag<T> timeline;

	private final Entry<T> entry;

	public AddKeyframeAction_T(AnimFlag<T> timeline, Entry<T> entry) {
		this.timeline = timeline;
		this.entry = entry;
	}

	@Override
	public UndoAction undo() {
		timeline.removeKeyframe(entry.time);
		return this;
	}

	@Override
	public UndoAction redo() {
		if (timeline.tans()) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}

		timeline.setOrAddEntry(entry.time, entry);
		return this;
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
