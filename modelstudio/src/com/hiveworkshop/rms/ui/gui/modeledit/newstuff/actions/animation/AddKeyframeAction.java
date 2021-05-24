package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;

	private final ModelStructureChangeListener structureChangeListener;
	private Entry<?> entry;

	public AddKeyframeAction(TimelineContainer node, AnimFlag<?> timeline, int trackTime,
	                         Object keyframeValue, Object keyframeInTan, Object keyframeOutTan,
	                         ModelStructureChangeListener structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.entry = new Entry(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
		this.structureChangeListener = structureChangeListener;
	}

	public AddKeyframeAction(TimelineContainer node, AnimFlag<?> timeline, int trackTime,
	                         Object keyframeValue, ModelStructureChangeListener structureChangeListener) {
		this(node, timeline, trackTime, keyframeValue, null, null, structureChangeListener);
	}

	public AddKeyframeAction(TimelineContainer node, AnimFlag<?> timeline, Entry<?> entry,
	                         ModelStructureChangeListener structureChangeListener) {
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
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
			if (entry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}

		timeline.addEntry(entry);
		structureChangeListener.keyframeAdded(node, timeline, entry.time);
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
