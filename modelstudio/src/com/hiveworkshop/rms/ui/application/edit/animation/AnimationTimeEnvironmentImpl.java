package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

public class AnimationTimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	private int currentTime;
	private int globalSequenceLength = -1;
	private Animation animation;
	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();

	public void setCurrentTime(final int currentTime) {
		this.currentTime = currentTime;
	}

	public void setAnimation(final Animation animation) {
		this.animation = animation;
		notifier.timeBoundsChanged(animation.getStart(), animation.getEnd());
	}

	public void setGlobalSeq(final int globalSeq) {
		globalSequenceLength = globalSeq;
		notifier.timeBoundsChanged(0, globalSequenceLength);
	}

	public int getGlobalSequenceLength() {
		return globalSequenceLength;
	}

	@Override
	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
			return currentTime;
		}
		return 0;
	}

	@Override
	public BasicTimeBoundProvider getCurrentAnimation() {
		return animation;
	}

	@Override
	public int getGlobalSeqTime(final int globalSeqId) {
		if (globalSequenceLength == globalSeqId) {
			return currentTime;
		}
		return 0;
	}

	@Override
	public int getStart() {
		if (globalSequenceLength == -1 && animation != null) {
			return animation.getStart();
		}
		return 0;
	}

	@Override
	public int getEnd() {
		if (globalSequenceLength == -1) {
			if (animation != null) {
				return animation.getEnd();
			} else {
				return 1;
			}
		}
		return globalSequenceLength;
	}

	@Override
	public void addChangeListener(final TimeBoundChangeListener listener) {
		notifier.subscribe(listener);
	}

}
