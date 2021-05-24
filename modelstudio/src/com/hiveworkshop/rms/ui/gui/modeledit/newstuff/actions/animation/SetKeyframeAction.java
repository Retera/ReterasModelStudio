package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;
	private final Runnable structureChangeListener;
	private Entry entry;
	private Entry orgEntry;
	private int time;
	private int orgTime;

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, Entry entry, Entry oldEntry, final Runnable structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgEntry = oldEntry;
		this.structureChangeListener = structureChangeListener;

	}

	public SetKeyframeAction(TimelineContainer node, AnimFlag timeline, int orgTime, Entry entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgTime = orgTime;
		time = entry.time;
		orgEntry = timeline.getEntryAt(orgTime);
	}

	public SetKeyframeAction(TimelineContainer node, AnimFlag timeline, Entry entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgTime = entry.time;
		time = entry.time;
		orgEntry = timeline.getEntryAt(orgTime);
	}

	@Override
	public void undo() {
		if (timeline.tans()) {
			if (orgEntry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}
		timeline.setEntry(orgEntry);
		structureChangeListener.run();
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot set interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}
		timeline.setEntry(entry);
		structureChangeListener.run();
	}

	@Override
	public String actionName() {
		return "set keyframe";
	}

}
