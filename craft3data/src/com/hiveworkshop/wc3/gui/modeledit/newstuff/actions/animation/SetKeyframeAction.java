package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.matrixeater.localization.LocalizationManager;

public class SetKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag timeline;
	private final int trackTime;
	private final Object keyframeValue;
	private final Object keyframeInTan;
	private final Object keyframeOutTan;
	private final Object keyframeOldValue;
	private final Object keyframeOldInTan;
	private final Object keyframeOldOutTan;
	private final Runnable structureChangeListener;

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
			final Object keyframeValue, final Object keyframeInTan, final Object keyframeOutTan,
			final Object keyframeOldValue, final Object keyframeOldInTan, final Object keyframeOldOutTan,
			final Runnable structureChangeListener) {
		this.node = node;
		this.timeline = timeline;
		this.trackTime = trackTime;
		this.keyframeValue = keyframeValue;
		this.keyframeInTan = keyframeInTan;
		this.keyframeOutTan = keyframeOutTan;
		this.keyframeOldValue = keyframeOldValue;
		this.keyframeOldInTan = keyframeOldInTan;
		this.keyframeOldOutTan = keyframeOldOutTan;
		this.structureChangeListener = structureChangeListener;
	}

	public SetKeyframeAction(final TimelineContainer node, final AnimFlag timeline, final int trackTime,
			final Object keyframeValue, final Object keyframeOldValue, final Runnable structureChangeListener) {
		this(node, timeline, trackTime, keyframeValue, null, null, keyframeOldValue, null, null,
				structureChangeListener);
	}

	@Override
	public void undo() {
		if (timeline.tans()) {
			if (keyframeOldInTan == null) {
				throw new IllegalStateException(
					LocalizationManager.getInstance().get("exception.setkeyframeaction_undo"));
			}
			timeline.setKeyframe(trackTime, keyframeOldValue, keyframeOldInTan, keyframeOldOutTan);
		} else {
			timeline.setKeyframe(trackTime, keyframeOldValue);
		}
		structureChangeListener.run();
	}

	@Override
	public void redo() {
		if (timeline.tans()) {
			if (keyframeInTan == null) {
				throw new IllegalStateException(
					LocalizationManager.getInstance().get("exception.setkeyframeaction_redo"));
			}
			timeline.setKeyframe(trackTime, keyframeValue, keyframeInTan, keyframeOutTan);
		} else {
			timeline.setKeyframe(trackTime, keyframeValue);
		}
		structureChangeListener.run();
	}

	@Override
	public String actionName() {
		return LocalizationManager.getInstance().get("string.setkeyframeaction_actionname");
	}

}
