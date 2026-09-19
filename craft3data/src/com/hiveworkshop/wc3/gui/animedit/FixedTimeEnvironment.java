package com.hiveworkshop.wc3.gui.animedit;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EditableModel;

/**
 * An animation environment frozen at one absolute time, for evaluating a
 * track's value there (Insert Key Here). The sequence that contains the time
 * becomes the current animation; if none does, the whole model span is used
 * so interpolation still works between the neighbouring keys.
 */
public final class FixedTimeEnvironment implements AnimatedRenderEnvironment {
	private final int time;
	private final BasicTimeBoundProvider bounds;

	public FixedTimeEnvironment(final EditableModel model, final int time) {
		this.time = time;
		Animation containing = null;
		int maxEnd = 0;
		for (final Animation animation : model.getAnims()) {
			maxEnd = Math.max(maxEnd, animation.getIntervalEnd());
			if ((animation.getIntervalStart() <= time) && (time <= animation.getIntervalEnd())) {
				containing = animation;
			}
		}
		final int start = containing == null ? 0 : containing.getIntervalStart();
		final int end = containing == null ? Math.max(maxEnd, time) : containing.getIntervalEnd();
		bounds = new BasicTimeBoundProvider() {
			@Override
			public int getStart() {
				return start;
			}

			@Override
			public int getEnd() {
				return end;
			}
		};
	}

	@Override
	public int getAnimationTime() {
		return time;
	}

	@Override
	public BasicTimeBoundProvider getCurrentAnimation() {
		return bounds;
	}

	@Override
	public int getGlobalSeqTime(final int length) {
		return length <= 0 ? 0 : time % length;
	}
}
