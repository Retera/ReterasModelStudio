package com.hiveworkshop.wc3.gui.animedit;

import com.hiveworkshop.wc3.gui.animedit.TimeBoundChangeListener.TimeBoundChangeNotifier;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

public class TimeEnvironmentImpl implements AnimatedRenderEnvironment, TimeBoundProvider {
	private int currentTime;
	private int globalSequenceLength = -1;
	private final TimeBoundChangeNotifier notifier = new TimeBoundChangeNotifier();
	private final ControllableTimeBoundProvider controllableTimeBoundProvider = new ControllableTimeBoundProvider(0,
			1000);
	private boolean staticViewMode;

	public void setCurrentTime(final int currentTime) {
		this.currentTime = currentTime;
	}

	public void setStart(final int startTime) {
		controllableTimeBoundProvider.setStart(startTime);

		if (globalSequenceLength == -1) {
			if (currentTime < startTime) {
				currentTime = startTime;
			}
			notifier.timeBoundsChanged(controllableTimeBoundProvider.getStart(),
					controllableTimeBoundProvider.getEnd());
		}
	}

	public void setEnd(final int endTime) {
		controllableTimeBoundProvider.setEnd(endTime);
		if (globalSequenceLength == -1) {
			if (currentTime > endTime) {
				currentTime = endTime;
			}
			notifier.timeBoundsChanged(controllableTimeBoundProvider.getStart(),
					controllableTimeBoundProvider.getEnd());
		}
	}

	public void setBounds(final int startTime, final int endTime) {
		controllableTimeBoundProvider.setStart(startTime);
		controllableTimeBoundProvider.setEnd(endTime);
		globalSequenceLength = -1;
		if (globalSequenceLength == -1) {
			currentTime = 0;
			notifier.timeBoundsChanged(controllableTimeBoundProvider.getStart(),
					controllableTimeBoundProvider.getEnd());
		}
	}

	public void setStaticViewMode(final boolean staticViewMode) {
		this.staticViewMode = staticViewMode;
	}

	public void setGlobalSeq(final int globalSeq) {
		this.globalSequenceLength = globalSeq;
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
	public BasicTimeBoundProvider getCurrentAnimation() {
		if (staticViewMode) {
			return null;
		}
		return controllableTimeBoundProvider;
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
		if (globalSequenceLength == -1) {
			return controllableTimeBoundProvider.getStart();
		}
		return 0;
	}

	@Override
	public int getEnd() {
		if (globalSequenceLength == -1) {
			return controllableTimeBoundProvider.getEnd();
		}
		return globalSequenceLength;
	}

	@Override
	public void addChangeListener(final TimeBoundChangeListener listener) {
		notifier.subscribe(listener);
	}

}
