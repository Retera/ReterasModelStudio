package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.viewer.LoopType;

public class TimeEnvironmentImpl {

	public static int FRAMES_PER_UPDATE = 1000 / 60;
	private int end;
	private int start;
	private int length;

	boolean live = false;
	private boolean looping = true;

	private int animationTime;
	private int globalSeqTime = 0;
	protected float animationSpeed = 1f;

	private Sequence sequence;
	private Animation animation;
	private GlobalSeq globalSeq = null; // I think this is used to view a models global sequences (w/o animating other things)

	private LoopType loopType = LoopType.DEFAULT_LOOP;

	private boolean staticViewMode;
	private long lastUpdateMillis = System.currentTimeMillis();

	private final TimeBoundChangeListener notifier = new TimeBoundChangeListener();


	public TimeEnvironmentImpl() {
		this.start = 0;
		this.end = 1;
		this.length = 1;
	}

	public TimeEnvironmentImpl setBounds(final int startTime, final int endTime) {
		start = startTime;
		end = endTime;
		length = endTime-startTime;

		if (globalSeq == null) {
			animationTime = Math.min(length, animationTime);
			animationTime = Math.max(0, animationTime);

			notifier.timeBoundsChanged(start, end);
		}
		return this;
	}

//	public TimeEnvironmentImpl setAnimation(Animation animation) {
//		this.animation = animation;
//		if (animation != null) {
//			setBounds(animation.getStart(), animation.getEnd());
//			globalSeq = null;
//		}
//		updateLastMillis();
//		if (loopType == PreviewPanel.LoopType.DEFAULT_LOOP) {
//			looping = animation != null && !animation.isNonLooping();
//		}
//		return this;
//	}

	public TimeEnvironmentImpl setStaticViewMode(final boolean staticViewMode) {
		this.staticViewMode = staticViewMode;
		return this;
	}

//	public TimeEnvironmentImpl setGlobalSeq(final GlobalSeq globalSeq) {
//		this.globalSeq = globalSeq;
//		if (globalSeq != null) {
//			setBounds(0, globalSeq.length);
//			notifier.timeBoundsChanged(0, globalSeq.getLength());
//		}
//		return this;
//	}


	public TimeEnvironmentImpl setSequence(final Sequence sequence) {
		if (sequence instanceof Animation) {
			this.animation = (Animation) sequence;
//			setBounds(sequence.getStart(), sequence.getEnd());
			setBounds(0, sequence.getLength());
			globalSeq = null;
			if (loopType == LoopType.DEFAULT_LOOP) {
				looping = animation != null && !animation.isNonLooping();
			}
		} else if (sequence instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) sequence;
			setBounds(0, sequence.length);
			notifier.timeBoundsChanged(0, sequence.getLength());
		} else {
			globalSeq = null;
			this.animation = null;
		}
		return this;
	}

	public Animation getCurrentAnimation() {
		return animation;
	}

	public GlobalSeq getGlobalSeq() {
		return globalSeq;
	}

	public Sequence getCurrentSequence() {
		if(globalSeq == null){
			return animation;
		}
		return globalSeq;
	}

	public int getLength() {
		return length;
	}
	public float getTimeRatio() {
		return animationTime/((float)length);
	}

	public int getEnvTrackTime() {
		if (globalSeq == null || globalSeq.getLength() > 0) {
			return animationTime;
		}
		return 0;
//		if (globalSeq == null) {
//			return animationTime;
//		} else if (globalSeq.getLength() > 0) {
//			return (int) (lastUpdateMillis % globalSeq.getLength());
//		}
//		return 0;
	}

	public int getTrackTime(GlobalSeq globalSeq) {
		if (globalSeq == null && this.globalSeq == null) {
			return animationTime;
		} else if ((globalSeq != null && this.globalSeq == null) && globalSeq.getLength() > 0) {
//			return (int) (lastUpdateMillis % globalSeq.getLength());
			return (globalSeqTime % globalSeq.getLength());
		} else if (globalSeq == this.globalSeq && globalSeq.getLength() > 0) {
			return animationTime;
		}
		return 0;
	}

	public int getAnimationTime() {
		if (globalSeq == null) {
			return animationTime;
		}
		return 0;
	}

	public int setAnimationTime(int newTime) {
		animationTime = newTime;
		updateLastMillis();
		return animationTime;
	}

	public TimeEnvironmentImpl setRelativeAnimationTime(int newTime) {
		animationTime = newTime;
		updateLastMillis();
		return this;
	}

	public int stepAnimationTime(int timeStep) {
//		animationTime = animationTime + timeStep;
		animationTime = (animationTime + timeStep + length + 1) % (length + 1);
		return animationTime;
	}

	public void addChangeListener(final TimeSliderPanel listener) {
		notifier.subscribe(listener);
	}

	public float getAnimationSpeed() {
		return animationSpeed;
	}

	public TimeEnvironmentImpl setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
		return this;
	}


	public TimeEnvironmentImpl setLoopType(final LoopType loopType) {
		this.loopType = loopType;
		switch (loopType) {
			case ALWAYS_LOOP -> looping = true;
			case DEFAULT_LOOP -> looping = animation != null && !animation.isNonLooping();
			case NEVER_LOOP -> looping = false;
		}
		return this;
	}

	public TimeEnvironmentImpl setAlwaysLooping() {
		this.loopType = LoopType.ALWAYS_LOOP;
		looping = true;
		return this;
	}

	public TimeEnvironmentImpl setDefaultLooping() {
		this.loopType = LoopType.DEFAULT_LOOP;
		looping = animation != null && !animation.isNonLooping();
		return this;
	}

	public TimeEnvironmentImpl setNeverLooping() {
		this.loopType = LoopType.NEVER_LOOP;
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
		if ((live) && (length > 0)) {
//			System.out.println("animationTime: " + animationTime + ", speed: " + animationSpeed);
//			if (looping) {
			if (loopType == LoopType.ALWAYS_LOOP || animation != null && !animation.isNonLooping() && loopType == LoopType.DEFAULT_LOOP) {
//				animationTime = start + (int) ((animationTime - start + (long) (timeSkip * animationSpeed)) % animation.length());
				animationTime = (int) (((animationTime) + (long) (timeSkip * animationSpeed)) % (length));
				globalSeqTime = (int) (globalSeqTime + (long) (timeSkip * animationSpeed));
			} else {
//				if (animationTime >= animation.length()) {
				globalSeqTime = (int) (globalSeqTime + (long) (timeSkip * animationSpeed));
				if (animationTime >= length) {
					live = false;
					globalSeqTime = 0;
				}
				animationTime = Math.min(length, (int) (animationTime + (timeSkip * animationSpeed)));
			}
		}
		return this;
	}

	private TimeEnvironmentImpl updateLastMillis() {
		lastUpdateMillis = System.currentTimeMillis();
		return this;
	}
}
