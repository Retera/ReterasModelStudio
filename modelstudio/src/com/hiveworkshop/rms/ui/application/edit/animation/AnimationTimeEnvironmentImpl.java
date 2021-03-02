package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.application.viewer.AnimationControllerListener;

public class AnimationTimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	private int currentTime;
	float animationSpeed = 1f;
	private int start;
	private int globalSequenceLength = -1;
	private Animation animation;
	private int end;
	private int animationTime;
	private long lastUpdateMillis = System.currentTimeMillis();
	private AnimationControllerListener.LoopType loopType = AnimationControllerListener.LoopType.DEFAULT_LOOP;
	private boolean looping = true;
	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();
	private boolean staticViewMode;

	public void setCurrentTime(final int currentTime) {
		this.currentTime = currentTime;
	}

	public void setStart(final int start) {
		this.start = start;
		notifier.timeBoundsChanged(start, end);
	}

	public void setEnd(final int end) {
		this.end = end;
		notifier.timeBoundsChanged(start, end);
	}

	public void setBounds(final int startTime, final int endTime) {
		start = startTime;
		end = endTime;
		if (globalSequenceLength == -1) {
			currentTime = 0;
			notifier.timeBoundsChanged(start, end);
		}
	}

	public void setStaticViewMode(final boolean staticViewMode) {
		this.staticViewMode = staticViewMode;
	}

	public void setGlobalSeq(final int globalSeq) {
		globalSequenceLength = globalSeq;
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
	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
			return animationTime;
		}
		return 0;
	}

	@Override
	public Animation getCurrentAnimation() {
		return animation;
	}

	public Animation setAnimation(Animation animation) {
		this.animation = animation;
		currentTime = 0;
		animationTime = 0;
		updateLastMillis();
		if (loopType == AnimationControllerListener.LoopType.DEFAULT_LOOP) {
			looping = animation != null && !animation.isNonLooping();
		}
		start = getAnimationStart();
		end = getAnimationEnd();
		notifier.timeBoundsChanged(start, end);
		return this.animation;
	}

	public void updateLastMillis() {
		lastUpdateMillis = System.currentTimeMillis();
	}


	public void setLoopType(final AnimationControllerListener.LoopType loopType) {
		this.loopType = loopType;
		switch (loopType) {
			case ALWAYS_LOOP -> looping = true;
			case DEFAULT_LOOP -> looping = animation != null && !animation.isNonLooping();
			case NEVER_LOOP -> looping = false;
		}
	}

	public void setAlwaysLooping() {
		this.loopType = AnimationControllerListener.LoopType.ALWAYS_LOOP;
		looping = true;
	}

	public void setDefaultLooping() {
		this.loopType = AnimationControllerListener.LoopType.DEFAULT_LOOP;
		looping = looping = animation != null && !animation.isNonLooping();
	}

	public void setNeverLooping() {
		this.loopType = AnimationControllerListener.LoopType.NEVER_LOOP;
		looping = false;
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

	private int getAnimationStart() {
		if (animation != null) {
			return animation.getStart();
		}
		return 0;
	}

	private int getAnimationEnd() {
		if (animation != null) {
			return animation.getEnd();
		}
		return 0;
	}

	public void setTimeScale(float timeScale) {
		this.animationSpeed = timeScale;
	}


	@Override
	public float getAnimationSpeed() {
		return animationSpeed;
	}

//	@Override
//	public void setLevelOfDetail(final int levelOfDetail) {
//		perspectiveViewport.setLevelOfDetail(levelOfDetail);
//	}

}
