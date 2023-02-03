package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetKeyframeAction_T;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;

public final class KeyFrameWrapper<T> {
	private final TimelineContainer node;
	private final AnimFlag<T> sourceTimeline;
	private final Entry<T> entry;
	private final boolean fromExisting;

	public KeyFrameWrapper(TimelineContainer node, AnimFlag<T> sourceTimeline, T value, T inTan, T outTan) {
		this.node = node;
		this.sourceTimeline = sourceTimeline;
		entry = new Entry<>(0, value, inTan, outTan);
		fromExisting = true;
	}

	public KeyFrameWrapper(TimelineContainer node, AnimFlag<T> sourceTimeline, Entry<T> entry) {
		this.node = node;
		this.sourceTimeline = sourceTimeline;
		this.entry = entry;
		fromExisting = true;
	}

	public KeyFrameWrapper(TimelineContainer node, AnimFlag<T> sourceTimeline, Entry<T> entry, boolean fromExisting) {
		this.node = node;
		this.sourceTimeline = sourceTimeline;
		this.entry = entry;
		this.fromExisting = fromExisting;
	}

	public AnimFlag<T> getSourceTimeline() {
		return sourceTimeline;
	}

	public Entry<T> getEntry() {
		return entry;
	}

	public TimelineContainer getNode() {
		return node;
	}

	public boolean isFromExisting() {
		return fromExisting;
	}

	public UndoAction getSetEntryAction(Sequence sequence, int time) {
		Entry<T> newEntry = entry.deepCopy().setTime(time);
		return new SetKeyframeAction_T<>(sourceTimeline, newEntry, sequence, null);
	}
}
