package ysera.time;

public interface TimeModeListener {
	void openTimeline();

	void openGlobalSequence(int index);

	void setTimeBounds(int intervalStart, int intervalEnd);
}
