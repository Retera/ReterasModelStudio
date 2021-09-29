package com.hiveworkshop.rms.ui.application.edit.animation;

import javax.swing.*;
import java.awt.*;

public class TimeSlider {
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDER_TIME_BUTTON_SIZE = 50;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = SLIDER_TIME_BUTTON_SIZE + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private Color forgroundC = Color.WHITE;
//	private TimeEnvironmentImpl timeEnvironment;
	JPanel timelinePanel;
	private int x = 0;

	private Rectangle timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);
	private Rectangle timeRect =        new Rectangle(0, 0, SLIDER_TIME_BUTTON_SIZE, VERTICAL_SLIDER_HEIGHT);
	private Rectangle backRect =        new Rectangle(0, 0, SLIDER_SIDE_BUTTON_SIZE, VERTICAL_SLIDER_HEIGHT);
	private Rectangle forwardRect =     new Rectangle(0, 0, SLIDER_SIDE_BUTTON_SIZE, VERTICAL_SLIDER_HEIGHT);

	public TimeSlider(JPanel timelinePanel){
		this.timelinePanel = timelinePanel;
	}

	public void drawTimeSlider(Graphics g, FontMetrics fontMetrics, int time) {
		drawRaisedRect(g, fontMetrics, backRect, "<");
		drawRaisedRect(g, fontMetrics, timeRect, time + "");
		drawRaisedRect(g, fontMetrics, forwardRect, ">");
	}

	private void drawRaisedRect(Graphics g, FontMetrics fontMetrics, Rectangle rect, String text) {
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(rect.x, rect.y, rect.width, rect.height, true);
//		g.setColor(getForeground());
		g.setColor(forgroundC);

		int y = (rect.y + ((rect.height + fontMetrics.getAscent()) / 2)) - 1;
		int x1 = rect.x + ((rect.width - fontMetrics.stringWidth(text)) / 2);
		g.drawString(text, x1, y);
	}

	public TimeSlider updateX(int x){
		this.x = x;

		int backRectOffset = x;
		int timeRectOffset = SLIDER_SIDE_BUTTON_SIZE + x;
		int forwardRectOffset = SLIDER_SIDE_BUTTON_SIZE + SLIDER_TIME_BUTTON_SIZE + x;
		backRect.x = backRectOffset;
		timeRect.x = timeRectOffset;
		forwardRect.x = forwardRectOffset;
		return this;
	}

	public int getX(){
		return x;
	}
	public int getCenterX(){
		return x + SLIDER_SIDE_BUTTON_SIZE + SLIDER_TIME_BUTTON_SIZE/2;
	}

	public TimeSlider setFromTimeFraction(double fraction){
		x = (int) (getMaxX() * fraction);
		updateX(x);
		return this;
	}

	public int getMaxX() {
		return timelinePanel.getWidth() - SLIDING_TIME_CHOOSER_WIDTH;
	}

	public double getLocationFraction(){
		return x / (double) getMaxX();
	}

	public TimeSlider moveSlider(int dx){
		x += dx;
		x = Math.min(getMaxX(), x);
		x = Math.max(0, x);
		updateX(x);
		return this;
	}

	public boolean containsPoint(Point point){
		return backRect.contains(point) || timeRect.contains(point) || forwardRect.contains(point);
	}
	public boolean onBackward(Point point){
		return backRect.contains(point);
	}
	public boolean onForward(Point point){
		return forwardRect.contains(point);
	}
	public boolean onSlide(Point point){
		return timeRect.contains(point);
	}
}
