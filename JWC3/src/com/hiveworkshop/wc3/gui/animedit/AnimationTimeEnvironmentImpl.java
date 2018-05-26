package com.hiveworkshop.wc3.gui.animedit;

import com.hiveworkshop.wc3.gui.animedit.TimeBoundChangeListener.TimeBoundChangeNotifier;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.Animation;

public class AnimationTimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	private int currentTime;
	private int globalSequenceLength = -1;
	private Animation animation;
	private final TimeBoundChangeNotifier notifier = new TimeBoundChangeNotifier();

	public void setCurrentTime(final int currentTime) {
		this.currentTime = currentTime;
	}

	public void setAnimation(final Animation animation) {
		this.animation = animation;
		notifier.timeBoundsChanged(animation.getStart(), animation.getEnd());
	}

	public void setGlobalSeq(final int globalSeq) {
		this.globalSequenceLength = globalSeq;
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
		if (this.globalSequenceLength == globalSeqId) {
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
