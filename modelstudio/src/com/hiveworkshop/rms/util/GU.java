package com.hiveworkshop.rms.util;

import java.awt.*;
import java.util.Arrays;

public class GU {
	public static void fillPolygon(Graphics graphics, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x;
			polygonY[i] = points[i].y;
		}
		graphics.fillPolygon(polygonX, polygonY, points.length);
	}

	public static void fillPolygonAt(Graphics graphics, Point point, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x + point.x;
			polygonY[i] = points[i].y + point.y;
		}
		graphics.fillPolygon(polygonX, polygonY, points.length);
	}

	public static void fillPolygonAt(Graphics graphics, int xOff, int yOff, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x + xOff;
			polygonY[i] = points[i].y + yOff;
		}
		graphics.fillPolygon(polygonX, polygonY, points.length);
	}

	public static void drawPolygon(Graphics graphics, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x;
			polygonY[i] = points[i].y;
		}
		graphics.drawPolygon(polygonX, polygonY, points.length);
	}

	public static void drawPolygon(Graphics graphics, Vec2... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = (int) points[i].x;
			polygonY[i] = (int) points[i].y;
		}
		graphics.drawPolygon(polygonX, polygonY, points.length);
	}

	public static void drawPolygonAt(Graphics graphics, Point point, Point... points) {
		drawPolygonAt(graphics, point.x, point.y, points);
	}

	public static void drawPolygonAt(Graphics graphics, int xOff, int yOff, Point... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = points[i].x + xOff;
			polygonY[i] = points[i].y + yOff;
		}
		graphics.drawPolygon(polygonX, polygonY, points.length);
	}

	public static void drawPolygonAt(Graphics graphics, int xOff, int yOff, Vec2... points) {
		int[] polygonX = new int[points.length];
		int[] polygonY = new int[points.length];

		for (int i = 0; i < points.length; i++) {
			polygonX[i] = (int) points[i].x + xOff;
			polygonY[i] = (int) points[i].y + yOff;
		}
		graphics.drawPolygon(polygonX, polygonY, points.length);
	}

	public static void fillCenteredSquare(Graphics graphics, Point point, int size) {
		fillCenteredSquare(graphics, point.x, point.y, size);
	}

	public static void fillCenteredSquare(Graphics graphics, int x, int y, int size) {
		int offset = size / 2;
		graphics.fillRect(x - offset, (y - offset), size, size);
	}

	public static void drawCenteredSquare(Graphics graphics, Point point, int size) {
		drawCenteredSquare(graphics, point.x, point.y, size);
	}

	public static void drawCenteredSquare(Graphics graphics, int x, int y, int size) {
		int offset = size / 2;
		graphics.drawRect(x - offset, (y - offset), size, size);
	}

	public static void drawLine(Graphics graphics, Point... points) {
		for (int i = 0; i < points.length - 1; i++) {
			graphics.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
		}
	}

	public static void drawLines(Graphics graphics, Point... points) {
		for (int i = 0; i < points.length - 1; i += 2) {
			graphics.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
		}
	}

	public static void drawLines(Graphics graphics, Vec2... points) {
		for (int i = 0; i < points.length - 1; i += 2) {
			graphics.drawLine((int) points[i].x, (int) points[i].y, (int) points[i + 1].x, (int) points[i + 1].y);
		}
	}

	public static void drawLines(Graphics graphics, Vec2[] starts, Vec2[] ends) {
		for (int i = 0; i < starts.length - 1; i++) {
			graphics.drawLine((int) starts[i].x, (int) starts[i].y, (int) ends[i].x, (int) ends[i].y);
		}
	}

	public static void drawPolygonAt(Graphics graphics, Point point, Polygon polygon) {
		polygon.translate(point.x, point.y);
		graphics.drawPolygon(polygon);
		polygon.translate(-point.x, -point.y);
	}

	public static void drawPolygonAt(Graphics graphics, int xOff, int yOff, Polygon polygon) {
		polygon.translate(xOff, yOff);
		graphics.drawPolygon(polygon);
		polygon.translate(-xOff, -yOff);
	}

	public static void fillPolygonAt(Graphics graphics, Point point, Polygon polygon) {
		fillPolygonAt(graphics, point.x, point.y, polygon);
	}

	public static void fillPolygonAt(Graphics graphics, int xOff, int yOff, Polygon polygon) {
		polygon.translate(xOff, yOff);
		graphics.fillPolygon(polygon);
		polygon.translate(-xOff, -yOff);
	}

	public static Polygon getTransPolygon(int xOff, int yOff, Polygon polygon) {
		Polygon p2 = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		p2.translate(xOff, yOff);
		return p2;
	}

	public static Polygon getTransPolygon(Point point, Polygon polygon) {
		Polygon p2 = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		p2.translate(point.x, point.y);
		return p2;
	}

	public static Polygon getRektPoly(Point point1, Point point2) {
		return getRektPoly(point1.x, point1.y, point2.x, point2.y);
	}

	public static Polygon getRektPoly(int x1, int y1, int x2, int y2) {
		Polygon pRek = new Polygon();
		pRek.addPoint(x1, y1);
		pRek.addPoint(x2, y1);
		pRek.addPoint(x2, y2);
		pRek.addPoint(x1, y2);
		return pRek;
	}

	public static Polygon getSymTriPoly(int xb, int yb, int p) {
		Polygon pRek = new Polygon();
		int xp = xb == 0 ? p : 0;
		int yp = yb == 0 ? p : 0;
		System.out.println("-xb: " + -xb + ", -yb: " + -yb);
		pRek.addPoint(-xb, -yb);
		System.out.println("xp: " + xp + ", yp: " + yp);
		pRek.addPoint(xp, yp);
		System.out.println("xb: " + xb + ", yb: " + yb);
		pRek.addPoint(xb, yb);
		System.out.println("pRekt.x: " + Arrays.toString(pRek.xpoints) + ", pRekt.y: " + Arrays.toString(pRek.ypoints));
		Polygon nT = new Polygon();
		nT.addPoint(-5, 0);
		nT.addPoint(0, -18);
		nT.addPoint(5, 0);
		System.out.println("nT.x: " + Arrays.toString(nT.xpoints) + ", nT.y: " + Arrays.toString(nT.ypoints));
		return pRek;
	}
}
