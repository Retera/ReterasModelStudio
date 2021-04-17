package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

public class TimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	protected float animationSpeed = 1f;
	private int start;
	private int currentTime;
	private int end;
	private int globalSequenceLength = -1;
	private boolean staticViewMode;

	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();

	public TimeEnvironmentImpl(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public void setCurrentTime(final int currentTime) {
		this.currentTime = currentTime;
	}

	public void setBounds(Animation animation) {
		setBounds(animation.getStart(), animation.getEnd());
	}

	public void setBounds(final int startTime, final int endTime) {
		setStart(startTime);
		setEnd(endTime);
		//		globalSequenceLength = -1;
		if (globalSequenceLength == -1) {
			currentTime = 0;
			notifier.timeBoundsChanged(start, end);
		}
	}

	@Override
	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
//			System.out.println("currentTime: " + currentTime);
			return currentTime;
		}
		return 0;
	}

	@Override
	public TimeBoundProvider getCurrentAnimation() {
		if (staticViewMode) {
			return null;
		}
		return this;
	}

	public void setStaticViewMode(final boolean staticViewMode) {
		this.staticViewMode = staticViewMode;
	}

	public void setGlobalSeq(final int globalSeq) {
		globalSequenceLength = globalSeq;
		if (globalSequenceLength != -1) {
			currentTime = 0;
		}
		notifier.timeBoundsChanged(0, globalSequenceLength);
	}

	public int getGlobalSequenceLength() {
		return globalSequenceLength;
	}

	public Integer getGlobalSeq() {
		if (globalSequenceLength == -1) {
			return null;
		}
		return globalSequenceLength;
	}

	@Override
	public int getStart() {
		if (globalSequenceLength == -1) {
			return start;
		}
		return 0;
	}

	public void setStart(final int startTime) {
		start = startTime;

		if (globalSequenceLength == -1) {
			currentTime = Math.min(startTime, currentTime);

			notifier.timeBoundsChanged(getStart(), getEnd());
		}
	}

	@Override
	public int getGlobalSeqTime(final int globalSeqId) {
		if (globalSequenceLength == globalSeqId) {
			return currentTime;
		}
		return 0;
	}

	@Override
	public int getEnd() {
		if (globalSequenceLength == -1) {
			return end;
		}
		return globalSequenceLength;
	}

	public void setEnd(final int endTime) {
		end = endTime;
		if (globalSequenceLength == -1) {
			currentTime = Math.min(endTime, currentTime);
			notifier.timeBoundsChanged(getStart(), getEnd());
		}
	}

	@Override
	public void addChangeListener(final TimeBoundChangeListener listener) {
		notifier.subscribe(listener);
	}

	@Override
	public float getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
	}
}
