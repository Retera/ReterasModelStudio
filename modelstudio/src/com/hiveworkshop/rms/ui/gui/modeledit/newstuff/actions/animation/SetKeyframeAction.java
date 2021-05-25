package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetKeyframeAction implements UndoAction {
	private final TimelineContainer node;
	private final AnimFlag<?> timeline;
	private final Runnable structureChangeListener;
	private final Entry<?> entry;
	private final Entry<?> orgEntry;
	private final int time;

	public SetKeyframeAction(TimelineContainer node, AnimFlag<?> timeline, Entry<?> entry, Runnable structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.node = node;
		this.timeline = timeline;
		this.entry = entry;
		time = entry.time;
		orgEntry = timeline.getEntryAt(time);
	}

	@Override
	public void undo() {
		if (timeline.tans()) {
			if (orgEntry.inTan == null) {
				throw new IllegalStateException(
						"Cannot add interpolation information (inTan/outTan) for keyframe, animation data was \"Linear\" or \"DontInterp\" during previous user action");
			}
		}
		timeline.setOrAddEntryT(time, orgEntry);
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
		timeline.setOrAddEntryT(time, entry);
		structureChangeListener.run();
	}

	@Override
	public String actionName() {
		return "set keyframe";
	}

}
