package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.TimelineContainer;

public class AddKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;
	private final int trackTime;
	private final Object keyframeValue;
	private final Object keyframeInTan;
	private final Object keyframeOutTan;
	private final ModelStructureChangeListener structureChangeListener;

	public AddKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
			final Object keyframeValue, final Object keyframeInTan, final Object keyframeOutTan,
			final ModelStructureChangeListener structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.trackTime = trackTime;
		this.keyframeValue = keyframeValue;
		this.keyframeInTan = keyframeInTan;
		this.keyframeOutTan = keyframeOutTan;
		this.structureChangeListener = structureChangeListener;
	}

	public AddKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
			final Object keyframeValue, final ModelStructureChangeListener structureChangeListener) {
		this(node, timeline, trackTime, keyframeValue, null, null, structureChangeListener);
	}

	@Override
	public void undo() {
		timeline.removeKeyframe(trackTime);
		structureChangeListener.keyframeRemoved(node, timeline, trackTime);
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
			if (keyframeInTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
			timeline.addKeyframe(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
		} else {
			timeline.addKeyframe(trackTime, keyframeValue);
		}
		structureChangeListener.keyframeAdded(node, timeline, trackTime);
	}

	@Override
	public String actionName() {
		return "add keyframe";
	}

}
