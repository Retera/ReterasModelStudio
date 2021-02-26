package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;
	//	private final int trackTime;
//	private final Object keyframeValue;
//	private final Object keyframeInTan;
//	private final Object keyframeOutTan;
	private final ModelStructureChangeListener structureChangeListener;
	private AnimFlag.Entry entry;

	public AddKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
	                         final Object keyframeValue, final Object keyframeInTan, final Object keyframeOutTan,
	                         final ModelStructureChangeListener structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.entry = new AnimFlag.Entry(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
//		this.trackTime = trackTime;
//		this.keyframeValue = keyframeValue;
//		this.keyframeInTan = keyframeInTan;
//		this.keyframeOutTan = keyframeOutTan;
		this.structureChangeListener = structureChangeListener;
	}

	public AddKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
	                         final Object keyframeValue, final ModelStructureChangeListener structureChangeListener) {
		this(node, timeline, trackTime, keyframeValue, null, null, structureChangeListener);
	}

	public AddKeyframeAction(final TimelineContainer node, final AnimFlag timeline, AnimFlag.Entry entry,
	                         final ModelStructureChangeListener structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		this.structureChangeListener = structureChangeListener;
//		this(node, timeline, trackTime, keyframeValue, null, null, structureChangeListener);
	}

	@Override
	public void undo() {
		timeline.removeKeyframe(entry.time);
		structureChangeListener.keyframeRemoved(node, timeline, entry.time);
//		timeline.removeKeyframe(trackTime);
//		structureChangeListener.keyframeRemoved(node, timeline, trackTime);
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
//			if (keyframeInTan == null) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
//						timeline.addKeyframe(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
		}
//		else {
//						timeline.addKeyframe(trackTime, keyframeValue);
//		}
		timeline.addEntry(entry);
//		structureChangeListener.keyframeAdded(node, timeline, trackTime);
		structureChangeListener.keyframeAdded(node, timeline, entry.time);
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
