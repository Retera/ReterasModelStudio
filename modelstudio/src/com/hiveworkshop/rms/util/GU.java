package com.hiveworkshop.rms.util;

import java.awt.*;

public class GU {
	public static void fillPolygon(Graphics2D graphics2D, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x;
			polygonY[i] = points[i].y;
		}
		graphics2D.fillPolygon(polygonX, polygonY, points.length);
	}

	public static void drawPolygon(Graphics2D graphics2D, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x;
			polygonY[i] = points[i].y;
		}
		graphics2D.drawPolygon(polygonX, polygonY, points.length);
	}

	public static void drawSquare(Graphics2D graphics2D, Point point, int size) {
		int offset = size / 2;
		graphics2D.fillRect(point.x - offset, (point.y - offset), size, size);
	}
}
