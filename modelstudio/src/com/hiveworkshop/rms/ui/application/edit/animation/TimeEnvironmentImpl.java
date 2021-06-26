package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.PreviewPanel;

public class TimeEnvironmentImpl implements TimeBoundProvider {


	public static int FRAMES_PER_UPDATE = 1000 / 60;
	boolean live = false;
	private int animationTime;
	private Animation animation;
	private PreviewPanel.LoopType loopType = PreviewPanel.LoopType.DEFAULT_LOOP;

	protected float animationSpeed = 1f;
	private int start;
	private boolean looping = true;
	private boolean staticViewMode;
	private int globalSequenceLength = -1;
	private int end;
	private long lastUpdateMillis = System.currentTimeMillis();

	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();


	public TimeEnvironmentImpl() {
		this.start = 0;
		this.end = 1;
	}

	public TimeEnvironmentImpl(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public TimeEnvironmentImpl setBounds(Animation animation) {
		setBounds(animation.getStart(), animation.getEnd());
		return this;
	}

	public TimeEnvironmentImpl setBounds(final int startTime, final int endTime) {
		start = startTime;
		end = endTime;
		//		globalSequenceLength = -1;
		if (globalSequenceLength == -1) {
			animationTime = Math.min(endTime, animationTime);
			animationTime = Math.max(startTime, animationTime);

			notifier.timeBoundsChanged(start, end);
		}
		return this;
	}

	public Animation setAnimation(Animation animation) {
		this.animation = animation;
		if (animation != null) {
			setBounds(animation);
		}
		updateLastMillis();
		if (loopType == PreviewPanel.LoopType.DEFAULT_LOOP) {
			looping = animation != null && !animation.isNonLooping();
		}
		return this.animation;
	}


	//	public int getAnimationTime() {
//		if (globalSequenceLength == -1) {
////			System.out.println("currentTime: " + currentTime);
//			return currentTime;
//		}
//		return 0;
//	}

	public int getTrackTime() {
		if (globalSequenceLength == -1) {
			return animationTime;
		} else if (globalSequenceLength > 0) {
			return (int) (lastUpdateMillis % globalSequenceLength);
		}
		return 0;
	}

	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
			return animationTime;
		}
		return 0;
	}

	public Animation getCurrentAnimation() {
		return animation;
	}

	public TimeEnvironmentImpl setStaticViewMode(final boolean staticViewMode) {
		this.staticViewMode = staticViewMode;
		return this;
	}

	public TimeEnvironmentImpl setGlobalSeq(final int globalSeq) {
		globalSequenceLength = globalSeq;
		if (globalSequenceLength != -1) {
//			currentTime = 0;
//			animationTime = 0;
		}
		notifier.timeBoundsChanged(0, globalSequenceLength);
		return this;
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

	public int setAnimationTime(int newTime) {
		animationTime = newTime;
		updateLastMillis();
		return animationTime;
	}

	public TimeEnvironmentImpl setRelativeAnimationTime(int newTime) {
		animationTime = start + newTime;
		updateLastMillis();
		return this;
	}

	public int stepAnimationTime(int timeStep) {
		animationTime = animationTime + timeStep;
		return animationTime;
	}

	@Override
	public int getStart() {
		if (globalSequenceLength == -1) {
			return start;
		}
		return 0;
	}

	public int getAnimStart(boolean hasGlobalSeq, Integer globalSeqLength) {
		if (globalSeqLength == null || globalSeqLength < 0 || !hasGlobalSeq) {
			if (animation != null) {
				return animation.getStart();
			}
			return start;
		}
		return 0;
	}

	public TimeEnvironmentImpl setStart(final int startTime) {
		start = startTime;

		if (globalSequenceLength == -1) {
			animationTime = Math.max(startTime, animationTime);

			notifier.timeBoundsChanged(getStart(), getEnd());
		}
		return this;
	}

	//	public int getGlobalSeqTime(final int globalSeqId) {
//		if (globalSequenceLength == globalSeqId) {
//			return currentTime;
//		}
//		return 0;
//	}
	public int getGlobalSeqTime(final int globalSeqLength) {
		if (globalSeqLength == 0) {
			return 0;
		}
		return (int) (lastUpdateMillis % globalSeqLength);
	}

	public int getRenderTime(boolean hasGlobalSeq, Integer globalSeqLength) {
		if (globalSeqLength == null || globalSeqLength < 0 || !hasGlobalSeq) {
			return animationTime;
		} else if (globalSeqLength == 0) {
			return 0;
		}
		return (int) (lastUpdateMillis % globalSeqLength);
	}

	@Override
	public int getEnd() {
		if (globalSequenceLength == -1) {
			return end;
		}
		return globalSequenceLength;
	}

	public int getAnimEnd(boolean hasGlobalSeq, Integer globalSeqLength) {
		if (globalSeqLength == null || globalSeqLength < 0 || !hasGlobalSeq) {
			if (animation != null) {
				return animation.getEnd();
			}
			return end;
		}
		return globalSeqLength;
	}

	public TimeEnvironmentImpl setEnd(final int endTime) {
		end = endTime;
		if (globalSequenceLength == -1) {
			animationTime = Math.min(endTime, animationTime);
			notifier.timeBoundsChanged(getStart(), getEnd());
		}
		return this;
	}

	@Override
	public void addChangeListener(final TimeBoundChangeListener listener) {
		notifier.subscribe(listener);
	}

	public float getAnimationSpeed() {
		return animationSpeed;
	}

	public TimeEnvironmentImpl setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
		return this;
	}


	public TimeEnvironmentImpl setLoopType(final PreviewPanel.LoopType loopType) {
		this.loopType = loopType;
		switch (loopType) {
			case ALWAYS_LOOP -> looping = true;
			case DEFAULT_LOOP -> looping = animation != null && !animation.isNonLooping();
			case NEVER_LOOP -> looping = false;
		}
		return this;
	}

	public TimeEnvironmentImpl setAlwaysLooping() {
		this.loopType = PreviewPanel.LoopType.ALWAYS_LOOP;
		looping = true;
		return this;
	}

	public TimeEnvironmentImpl setDefaultLooping() {
		this.loopType = PreviewPanel.LoopType.DEFAULT_LOOP;
		looping = looping = animation != null && !animation.isNonLooping();
		return this;
	}

	public TimeEnvironmentImpl setNeverLooping() {
		this.loopType = PreviewPanel.LoopType.NEVER_LOOP;
		looping = false;
		return this;
	}


	public boolean isLive() {
		return live;
	}

	public TimeEnvironmentImpl setLive(final boolean live) {
		this.live = live;
		updateLastMillis();
		return this;
	}

	public TimeEnvironmentImpl updateAnimationTime() {
		long timeSkip = System.currentTimeMillis() - lastUpdateMillis;
		updateLastMillis();
//		if ((animation != null) && (end-start > 0)) {
		if ((live) && (end - start > 0)) {
//			System.out.println("animationTime: " + animationTime + ", speed: " + animationSpeed);
			if (looping) {
//				animationTime = start + (int) ((animationTime - start + (long) (timeSkip * animationSpeed)) % animation.length());
				animationTime = start + (int) (((animationTime - start) + (long) (timeSkip * animationSpeed)) % (end - start));
			} else {
//				if (animationTime >= animation.length()) {
				if (animationTime >= end) {
					live = false;
				}
				animationTime = Math.min(end, (int) (animationTime + (timeSkip * animationSpeed)));
			}
		}
		return this;
	}

	public TimeEnvironmentImpl updateLastMillis() {
		lastUpdateMillis = System.currentTimeMillis();
		return this;
	}
}
