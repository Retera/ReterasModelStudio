package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.AnimationControllerListener;

public class TimeEnvironmentImpl implements TimeBoundProvider {


	public static int FRAMES_PER_UPDATE = 1000 / 60;
	boolean live = false;
	private int animationTime;
	private Animation animation;
	private AnimationControllerListener.LoopType loopType = AnimationControllerListener.LoopType.DEFAULT_LOOP;

	protected float animationSpeed = 1f;
	private int start;
	private boolean looping = true;
	private int currentTime;
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

	public Animation setAnimation(Animation animation) {
		this.animation = animation;
		updateLastMillis();
		if (loopType == AnimationControllerListener.LoopType.DEFAULT_LOOP) {
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
	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
			return animationTime;
		}
		return 0;
	}

	//	public TimeBoundProvider getCurrentAnimation() {
//		if (staticViewMode) {
//			return null;
//		}
//		return this;
//	}
	public Animation getCurrentAnimation() {
		return animation;
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

	public int setAnimationTime(int newTime) {
		animationTime = newTime;
		return animationTime;
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

	public void setStart(final int startTime) {
		start = startTime;

		if (globalSequenceLength == -1) {
			currentTime = Math.min(startTime, currentTime);

			notifier.timeBoundsChanged(getStart(), getEnd());
		}
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

	public float getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
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


	public boolean isLive() {
		return live;
	}

	public void setLive(final boolean live) {
		this.live = live;
	}

	public void updateAnimationTime() {
		long timeSkip = System.currentTimeMillis() - lastUpdateMillis;
		updateLastMillis();
		if ((animation != null) && (animation.length() > 0)) {
//			System.out.println("animationTime: " + animationTime);
			if (looping) {
				animationTime = (int) ((animationTime + (long) (timeSkip * animationSpeed)) % animation.length());
			} else {
				animationTime = Math.min(animation.length(), (int) (animationTime + (timeSkip * animationSpeed)));
				if (animationTime > animation.length()) {
					live = false;
				}
			}
		}
	}

	public void updateLastMillis() {
		lastUpdateMillis = System.currentTimeMillis();
	}
}
