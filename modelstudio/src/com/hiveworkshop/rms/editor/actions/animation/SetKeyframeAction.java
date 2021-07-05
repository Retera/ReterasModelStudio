package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;

public class SetKeyframeAction implements UndoAction {
	private final AnimFlag<?> timeline;
	private final Runnable structureChangeListener;
	private final Entry<?> entry;
	private final Entry<?> orgEntry;
	private final int time;

	public SetKeyframeAction(AnimFlag<?> timeline, Entry<?> entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timeline = timeline;
		this.entry = entry;
		time = entry.time;
		orgEntry = timeline.getEntryAt(time);
	}

	@Override
	public UndoAction undo() {
		if (timeline.tans()) {
			if (orgEntry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}
		timeline.setOrAddEntryT(time, orgEntry);
		if (structureChangeListener != null) {
			structureChangeListener.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if (timeline.tans()) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot set interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}
		timeline.setOrAddEntryT(time, entry);
		if (structureChangeListener != null) {
			structureChangeListener.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set keyframe";
	}

}
