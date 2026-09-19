package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import java.util.ArrayList;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;

/**
 * Changes a track's interpolation. Moving to Hermite or Bezier gives every
 * key flat tangents (copies of its value) when it had none; moving away from
 * them drops the tangents. Undo restores the previous type and tangents.
 */
public final class SetTrackInterpolationAction implements UndoAction {
	private final AnimFlag track;
	private final InterpolationType newType;
	private final InterpolationType oldType;
	private final ArrayList<Object> oldInTans;
	private final ArrayList<Object> oldOutTans;
	private final Runnable refresh;

	@SuppressWarnings("unchecked")
	public SetTrackInterpolationAction(final AnimFlag track, final InterpolationType newType,
			final Runnable refresh) {
		this.track = track;
		this.newType = newType;
		this.oldType = track.getInterpTypeAsEnum();
		this.oldInTans = new ArrayList<Object>(track.getInTans());
		this.oldOutTans = new ArrayList<Object>(track.getOutTans());
		this.refresh = refresh;
	}

	private static boolean usesTangents(final InterpolationType type) {
		return (type == InterpolationType.HERMITE) || (type == InterpolationType.BEZIER);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void redo() {
		track.setInterpType(newType);
		if (usesTangents(newType)) {
			if (track.getInTans().size() != track.getValues().size()) {
				track.getInTans().clear();
				track.getOutTans().clear();
				for (final Object value : track.getValues()) {
					track.getInTans().add(AnimFlag.cloneValue(value));
					track.getOutTans().add(AnimFlag.cloneValue(value));
				}
			}
		} else {
			track.getInTans().clear();
			track.getOutTans().clear();
		}
		refresh.run();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void undo() {
		track.setInterpType(oldType);
		track.getInTans().clear();
		track.getInTans().addAll(oldInTans);
		track.getOutTans().clear();
		track.getOutTans().addAll(oldOutTans);
		refresh.run();
	}

	@Override
	public String actionName() {
		return "set interpolation to " + newType;
	}
}
