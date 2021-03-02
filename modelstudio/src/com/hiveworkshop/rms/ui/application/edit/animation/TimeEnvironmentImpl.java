package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

public class TimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	private final ControllableTimeBoundProvider ctrlTimeBProv = new ControllableTimeBoundProvider(0, 1000);
	int FRAMES_PER_UPDATE = 1000 / 60;
	private int currentTime;
	private int globalSequenceLength = -1;
	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();
	private int start;
	private boolean staticViewMode;
	private int end;
	protected float animationSpeed = 1f;

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
		ctrlTimeBProv.setStart(startTime);
		ctrlTimeBProv.setEnd(endTime);
		globalSequenceLength = -1;
		if (globalSequenceLength == -1) {
			currentTime = 0;
			notifier.timeBoundsChanged(ctrlTimeBProv.getStart(),
					ctrlTimeBProv.getEnd());
		}
	}

	@Override
	public int getStart() {
		if (globalSequenceLength == -1) {
			return ctrlTimeBProv.getStart();
		}
		return 0;
	}

	public void setStart(final int startTime) {
		ctrlTimeBProv.setStart(startTime);

		if (globalSequenceLength == -1) {
			currentTime = Math.min(startTime, currentTime);

			notifier.timeBoundsChanged(ctrlTimeBProv.getStart(),
					ctrlTimeBProv.getEnd());
		}
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
	public int getAnimationTime() {
		if (globalSequenceLength == -1) {
			return currentTime;
		}
		return 0;
	}

	@Override
	public TimeBoundProvider getCurrentAnimation() {
		if (staticViewMode) {
			return null;
		}
		return ctrlTimeBProv;
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
			return ctrlTimeBProv.getEnd();
		}
		return globalSequenceLength;
	}

	public void setEnd(final int endTime) {
		ctrlTimeBProv.setEnd(endTime);
		if (globalSequenceLength == -1) {
			currentTime = Math.min(endTime, currentTime);
			notifier.timeBoundsChanged(ctrlTimeBProv.getStart(),
					ctrlTimeBProv.getEnd());
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
