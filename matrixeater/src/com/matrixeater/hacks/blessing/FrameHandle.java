package com.matrixeater.hacks.blessing;

import java.awt.Graphics;
import java.awt.Rectangle;

public interface FrameHandle {
	void paint(Graphics g);

	String getTooltip();

	int getCost();

	String getUbertip();

	Rectangle getBounds();
}
