package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;

import java.awt.*;

public class TimeBarPainter {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDER_TIME_BUTTON_SIZE = 50;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = SLIDER_TIME_BUTTON_SIZE + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int PLAY_BUTTON_SIZE = 30;
	private static final Dimension PLAY_BUTTON_DIMENSION = new Dimension(PLAY_BUTTON_SIZE, PLAY_BUTTON_SIZE);
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);
	private float tickStep = 300;
	private TimeEnvironmentImpl timeEnvironment;
	private final TimeSlider timeSlider;

	public TimeBarPainter(TimeSlider timeSlider) {
		this.timeSlider = timeSlider;

	}

	public void setTickStep(float tickStep) {
		this.tickStep = tickStep;
	}

	public TimeBarPainter setTimeEnvironment(TimeEnvironmentImpl timeEnvironment) {
		this.timeEnvironment = timeEnvironment;
		return this;
	}


	public void drawTimeBar(Graphics g, int width) {
		drawTimeBarBase(g, width);
		int maxX = width - SLIDING_TIME_CHOOSER_WIDTH;
		if (0 <= maxX) {
			switch (ProgramGlobals.getPrefs().getTheme()) {
				case DARK, HIFI -> g.setColor(Color.WHITE);
				case FOREST_GREEN -> g.setColor(Color.WHITE);
				default -> g.setColor(Color.BLACK);
			}
			// time markers
			drawTimeTicks(g);
		} else {
			g.drawString("No pixels", 0, 16);
		}
	}

	public void drawTimeBarBase(Graphics g, int width) {
		if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLUE.darker());
		}
		g.fillRect(0, 0, width, VERTICAL_SLIDER_HEIGHT);
	}

	public void drawTimeTicks(Graphics g) {
		if (timeEnvironment != null) {
			FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

			final int timeSpan = timeEnvironment.getLength();

//			int numberOfTicks = timeSpan / tickStep;
//			int startOffset = tickStep;

//			// draw first time marker
////			drawMajorTick(g, fontMetrics, 0);
//			// draw even (time%tickStep==0) time markers
//			for (int i = 0; i < numberOfTicks; i++) {
////				int time = startOffset + tickStep * i;
//				int time = tickStep * i;
//
//				boolean majorTick = (i % 2) == 0;
//				if (majorTick) {
//					drawMajorTick(g, fontMetrics, time);
//				} else {
//					drawMinorTick(g, computeXFromTime(time));
//				}
//			}
			boolean majorTick = true;
			for (float time = 0; time < timeSpan; time += tickStep) {

//				majorTick = (i % 2) == 0;
				if (majorTick || tickStep<1) {
					drawMajorTick(g, fontMetrics, (int) time);
				} else {
					drawMinorTick(g, computeXFromTime((int) time));
				}
				majorTick = !majorTick;
			}
			// draw last time marker
			drawMajorTick(g, fontMetrics, timeEnvironment.getLength());
		}
	}

	private void drawMinorTick(Graphics g, int xCoordPixels) {
		((Graphics2D) g).setStroke(WIDTH_1_STROKE);
		int lineEnd = VERTICAL_SLIDER_HEIGHT + (VERTICAL_TICKS_HEIGHT / 2);
		g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
	}

	private void drawMajorTick(Graphics g, FontMetrics fontMetrics, int time) {
		int xCoordPixels = computeXFromTime(time);
		((Graphics2D) g).setStroke(WIDTH_2_STROKE);
		int lineEnd = VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT;
		g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
		String tickLabel = "" + time;
		g.drawString(tickLabel, xCoordPixels - (fontMetrics.stringWidth(tickLabel) / 2), lineEnd + fontMetrics.getAscent());
	}

	private int computeXFromTime(int time) {
		double timeRatio = (time) / (double) (timeEnvironment.getLength());
		return (int) (timeSlider.getMaxX() * timeRatio) + (SIDE_OFFSETS);
	}
}
