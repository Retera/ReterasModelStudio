package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;
	//	private AnimFlag.Entry oldEntry;
//	private final int trackTime;
//	private final Object keyframeValue;
//	private final Object keyframeInTan;
//	private final Object keyframeOutTan;
//	private final Object keyframeOldValue;
//	private final Object keyframeOldInTan;
//	private final Object keyframeOldOutTan;
	private final Runnable structureChangeListener;
	private AnimFlag.Entry entry;
	private AnimFlag.Entry orgEntry;
	private int time;
	private int orgTime;

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
	                         final Object keyframeValue, final Object keyframeInTan, final Object keyframeOutTan,
	                         final Object keyframeOldValue, final Object keyframeOldInTan, final Object keyframeOldOutTan,
	                         final Runnable structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
//		this.trackTime = trackTime;
//		this.keyframeValue = keyframeValue;
//		this.keyframeInTan = keyframeInTan;
//		this.keyframeOutTan = keyframeOutTan;
//		this.keyframeOldValue = keyframeOldValue;
//		this.keyframeOldInTan = keyframeOldInTan;
//		this.keyframeOldOutTan = keyframeOldOutTan;
		this.structureChangeListener = structureChangeListener;
	}

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
	                         final Object keyframeValue, final Object keyframeOldValue, final Runnable structureChangeListener) {
		this(node, timeline, trackTime, keyframeValue, null, null, keyframeOldValue, null, null,
				structureChangeListener);
	}

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, AnimFlag.Entry entry, AnimFlag.Entry oldEntry, final Runnable structureChangeListener) {
//		this(node, timeline, trackTime, keyframeValue, null, null, keyframeOldValue, null, null, structureChangeListener);
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgEntry = oldEntry;
		this.structureChangeListener = structureChangeListener;

	}

	public SetKeyframeAction(TimelineContainer node, AnimFlag timeline, int orgTime, AnimFlag.Entry entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgTime = orgTime;
		time = entry.time;
		int index = timeline.getTimes().indexOf(orgTime);
		orgEntry = timeline.getEntry(index);
	}

	public SetKeyframeAction(TimelineContainer node, AnimFlag timeline, AnimFlag.Entry entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.orgTime = entry.time;
		time = entry.time;
		int index = timeline.getTimes().indexOf(orgTime);
		orgEntry = timeline.getEntry(index);
	}

	@Override
	public void undo() {
		if (timeline.tans()) {
//			if (keyframeOldInTan == null) {
			if (orgEntry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
//			timeline.setKeyframe(trackTime, keyframeOldValue, keyframeOldInTan, keyframeOldOutTan);
//		} else {
//			timeline.setKeyframe(trackTime, keyframeOldValue);
		}
		timeline.setEntry(orgEntry);
		structureChangeListener.run();
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
//			if (keyframeInTan == null) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot set interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
//			timeline.setKeyframe(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
//		} else {
//			timeline.setKeyframe(trackTime, keyframeValue);
		}
		timeline.setEntry(entry);
		structureChangeListener.run();
	}

	@Override
	public String actionName() {
		return "set keyframe";
	}

}
