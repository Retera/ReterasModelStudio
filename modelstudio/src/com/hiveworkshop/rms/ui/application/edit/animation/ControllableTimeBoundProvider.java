package com.hiveworkshop.rms.ui.application.edit.animation;

public class ControllableTimeBoundProvider implements TimeBoundProvider {
	private int start, end;
	private final TimeBoundChangeListener.TimeBoundChangeNotifier notifier = new TimeBoundChangeListener.TimeBoundChangeNotifier();

	public ControllableTimeBoundProvider(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	public void setStart(final int start) {
		this.start = start;
		notifier.timeBoundsChanged(start, end);
	}

	public void setEnd(final int end) {
		this.end = end;
		notifier.timeBoundsChanged(start, end);
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public void addChangeListener(final TimeBoundChangeListener listener) {
		notifier.subscribe(listener);
	}

}
