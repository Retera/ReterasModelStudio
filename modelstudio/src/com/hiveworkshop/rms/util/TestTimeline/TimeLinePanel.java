package com.hiveworkshop.rms.util.TestTimeline;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

public class TimeLinePanel extends JPanel {
	int start = 0;
	int end = 30;
	int realStart = -20;
	int realEnd = 50;
	int tickStep = 50;
	int tickTime = 50;
	int currentTime;
	double zoom = 1;
	MigLayout migLayout = new MigLayout("fill, ins 0, gap 0");
	List<TimeListener> timeListeners = new ArrayList<>();
	Color baseBgColor = Color.DARK_GRAY.brighter();
	Color timeMarkerColor = Color.BLUE.brighter();
	Color outOfRangeColor = new Color(0, 0, 0, 60);
	Color majorTickColor = new Color(0, 0, 0, 150);
//	Color endMarkerColor = new Color(0, 0, 0, 200);
//	Color minorTickColor = new Color(0, 0, 0, 60);

	List<KeyFrame> keyFrames = new ArrayList<>();
	TimeBarPainter timeBarPainter;
	public TimeLinePanel() {
//		MigLayout migLayout = new MigLayout("fill, ins 0, gap 0");
		setLayout(migLayout);
		timeBarPainter = new TimeBarPainter(this);

		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				updateTime(getTimeFromX(e.getX()));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				onScroll(e.getWheelRotation() > 0);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				updateTime(getTimeFromX(e.getX()));
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
			}
		};
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		this.addMouseWheelListener(mouseAdapter);
	}

	public TimeLinePanel setKeyframes(List<KeyFrame> keyFrames) {
		for (KeyFrame keyFrame : this.keyFrames) {
			remove(keyFrame);
		}
		this.keyFrames = keyFrames;
		for (KeyFrame keyFrame : keyFrames) {
			add(keyFrame, "pos " + getXFromTime(keyFrame.getTime()) + "px 30px");

			MouseAdapter l = new MouseAdapter() {
				boolean isMouseDown;
				Point clickPoint;
				Point componentOffset;

				public void mousePressed(MouseEvent e) {
					if (!isMouseDown) {
						isMouseDown = true;
						clickPoint = e.getLocationOnScreen();
						componentOffset = e.getPoint();
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (isMouseDown) {
						isMouseDown = false;
						int newRelX = e.getXOnScreen() - TimeLinePanel.this.getLocationOnScreen().x - componentOffset.x;
						int newTime = getTimeFromX(newRelX);
						keyFrame.setTime(newTime);
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					int newRelX = e.getXOnScreen() - TimeLinePanel.this.getLocationOnScreen().x - componentOffset.x;
					keyFrame.setLocation(newRelX, 30);
				}
			};
			keyFrame.addMouseListener(l);
			keyFrame.addMouseMotionListener(l);
		}
		return this;
	}

	private void onScroll(boolean b) {
		Container parent = getParent();
		if (parent != null) {
			if (b) {
				zoom *= 1.2;
			} else {
				zoom /= 1.2;
			}
			if (zoom > 0) {
				if (zoom <= 1) {
					this.setPreferredSize(new Dimension((int) (parent.getWidth() / zoom), getHeight()));
				}
				if (zoom >= 1) {
					updateRealLimits();
				}
			}
		}
		revalidate();
		repaint();
		SwingUtilities.invokeLater(this::updateKeyframes);
	}

	private void updateKeyframes() {
		calculateTickTime();
		for (KeyFrame keyFrame : keyFrames) {
			int xFromTime = getXFromTime(keyFrame.getTime()) - (keyFrame.getWidth() / 2);
//			System.out.println("x from time = "+ keyFrame.getTime() +": " + xFromTime);
			add(keyFrame, "pos " + xFromTime + "px 30px");
		}
		revalidate();
		repaint();
	}

	private void updateRealLimits() {
		int interv = end - start;
		realStart = (int) (start - (interv * 0.08 * zoom));
		realEnd = (int) (end + (interv * 0.08 * zoom));
	}

	public void setCurrentTime(int time) {
		currentTime = time;
//		System.out.println("time: " + currentTime);
		repaint();
	}

	public void addTimeListener(TimeListener timeListener) {
		timeListeners.add(timeListener);
	}

	public void removeTimeListener(TimeListener timeListener) {
		timeListeners.remove(timeListener);
	}

	public void updateTime(int time) {
		currentTime = time;
		for (TimeListener timeListener : timeListeners) {
			timeListener.timeChanged(time);
		}
		repaint();
	}

	public void setLimits(int length) {
		this.start = 0;
		this.end = length;
		updateRealLimits();
//		System.out.println("limits set");
		revalidate();
		repaint();
	}

	private int getTimeFromX(int x) {
		return x * tickTime / tickStep + realStart;
	}

	private int getXFromTime(int time) {
		return ((time - realStart) * tickStep) / tickTime;
	}

	private List<TimeTick> getTicks() {
		List<TimeTick> tickList = new ArrayList<>();
		calculateTickTime();

		int firstTickTime = realStart - realStart % tickTime;

		for (int i = firstTickTime; i <= realEnd; i += tickTime) {
			tickList.add(new TimeTick(i, getXFromTime(i)));
		}
		return tickList;
	}

	private void calculateTickTime() {
		int realTimeSpan = realEnd - realStart;
		int width = Math.max(1, getWidth());
		int minTimePerPix = 45 * realTimeSpan / width;

		tickTime = 1;
		System.out.println("Math.log10(realTimeSpan)+1: " + (Math.log10(realTimeSpan) + 1));
		System.out.println("realTimeSpan: " + realTimeSpan + ", minPix: " + minTimePerPix);

		for (int i = 0; i < Math.log10(realTimeSpan) + 1; i++) {
			double pow = Math.pow(10, i);
			if (pow > minTimePerPix) {
				tickTime = (int) pow;
				System.out.println("pow (i=" + i + "): " + pow);
				break;
			} else if (2 * pow > minTimePerPix) {
				tickTime = 2 * (int) pow;
				System.out.println("pow (i=" + i + "): " + pow);
				break;
			} else if (5 * pow > minTimePerPix) {
				tickTime = 5 * (int) pow;
				System.out.println("pow (i=" + i + "): " + pow);
				break;
			}
		}

		System.out.println("tickTime: " + tickTime);
		double timePerTick = realTimeSpan / (double) tickTime;

		tickStep = (int) (width / timePerTick);
	}


	private void drawTimeMarker(Graphics g, FontMetrics fontMetrics) {
		int realTimeSpan = realEnd - realStart;

		int timePoint = getXFromTime(currentTime);

		String tickLabel = "" + currentTime;
		int stringWidth = fontMetrics.stringWidth(tickLabel);
		int height = fontMetrics.getHeight();
		int xOff = stringWidth / 2;
		int xOffPad = xOff + height / 2;
		g.setColor(timeMarkerColor);
		int halfHeight = height / 2;

		g.drawLine(timePoint, 1, timePoint, getHeight() - 1);

		int hAdj = 5;
		int wAdj = 2;
		int x1 = timePoint - xOffPad - wAdj;
		int x2 = timePoint + xOff - height + wAdj;
		int rad = height + hAdj;
		int y1 = 0;
		int y2 = height - hAdj - 1;
		g.fillArc(x1, y1, rad, rad, 90, 90);
		g.fillArc(x1, y2, rad, rad, 180, 90);
		g.fillArc(x2, y2, rad, rad, 270, 90);
		g.fillArc(x2, y1, rad, rad, 0, 90);

		g.fillRect(x1 + 1, height - hAdj, xOffPad * 2 + 1, height - hAdj);
		g.fillRect(timePoint - xOff, 1, xOff * 2 - 1, height * 2 - 2);

//		System.out.println("height: " + fontMetrics.getHeight() + ", getAscent: " +  fontMetrics.getAscent() + ", width: " + stringWidth);
		int y = halfHeight + fontMetrics.getAscent();
		g.setColor(Color.WHITE);
		g.drawString(tickLabel, timePoint - xOff, y);
	}

//	private void drawTimeMarker2(Graphics g, FontMetrics fontMetrics) {
//		int realTimeSpan = realEnd - realStart;
//		double pixelsPerTime = getWidth() / (double) realTimeSpan;
//		int timePoint = (int) (pixelsPerTime * (currentTime - realStart));
//		String tickLabel = "" + currentTime;
//		int stringWidth = fontMetrics.stringWidth(tickLabel);
//		int height = fontMetrics.getHeight();
//		int xOff = stringWidth / 2;
//		int xOffPad = xOff + height / 2;
//		g.setColor(timeMarkerColor);
//		int padding = (height / 2 + fontMetrics.getAscent()) / 2;
//		int halfHeight = height / 2;
//		int arcRad = height + 2;
//		int x = 70 - (stringWidth / 2);
//		int x2 = 70 - ((stringWidth) / 2) - height / 2;
////		GU.fillPolygonAt(g,timePoint-xOffPad, 0, getMarkerPolygon(fontMetrics, tickLabel));
////		g.fillRoundRect(x-padding-1, 0, stringWidth + padding*2, height *2, padding*2+2, padding*2);
//
//		int hAdj = 5;
//		int wAdj = 2;
////		g.setColor(Color.YELLOW);
//		g.drawLine(timePoint, 1, timePoint, getHeight() - 1);
//		g.fillArc(timePoint - xOffPad - wAdj, 0, height + hAdj, height + hAdj, 90, 90);
////		g.setColor(Color.RED);
//		g.fillArc(timePoint - xOffPad - wAdj, height - hAdj - 1, height + hAdj, height + hAdj, 180, 90);
////		g.setColor(Color.CYAN);
//		g.fillArc(timePoint + xOff - height + wAdj, height - hAdj - 1, height + hAdj, height + hAdj, 270, 90);
////		g.setColor(Color.MAGENTA);
//		g.fillArc(timePoint + xOff - height + wAdj, 0, height + hAdj, height + hAdj, 0, 90);
//		g.fillRect(timePoint - xOffPad - wAdj + 1, height - hAdj, xOffPad * 2 + 1, height - hAdj);
////		g.setColor(Color.GREEN);
//		g.fillRect(timePoint - xOff, 1, xOff * 2 - 1, height * 2 - 2);
////		g.fillRect(0, 5, 5, 5);
//
//		System.out.println("height: " + fontMetrics.getHeight() + ", getAscent: " + fontMetrics.getAscent() + ", width: " + stringWidth);
//		int y = halfHeight + fontMetrics.getAscent();
//		g.setColor(Color.WHITE);
//		g.drawString(tickLabel, timePoint - xOff, y);
//	}
//
//	private void drawTimeTicks(Graphics g, List<TimeTick> timeTicks) {
//		// draw first time marker
//		g.setColor(endMarkerColor);
//		int realTimeSpan = realEnd - realStart;
//		double pixelsPerTime = getWidth() / (double) realTimeSpan;
//		int pixStart = (int) (pixelsPerTime * (start - realStart));
//		drawVerticalLine(g, getXFromTime(start));
////		drawVerticalLine(g, (pixStart));
//		// draw even (time%tickStep==0) time markers
//		g.setColor(minorTickColor);
//		drawVerticalLine(g, timeTicks.get(0).getLoc() - (tickStep / 2));
//		for (TimeTick timeTick : timeTicks) {
//			g.setColor(majorTickColor);
//			drawVerticalLine(g, timeTick.getLoc());
//			g.setColor(minorTickColor);
//			drawVerticalLine(g, timeTick.getLoc() + (tickStep / 2));
//		}
//		// draw last time marker
//		g.setColor(endMarkerColor);
//		int pixEnd = (int) (pixelsPerTime * (end - realStart));
//		drawVerticalLine(g, getXFromTime(end));
//		System.out.println("(in ticks) x from time = " + 30 + ": " + getXFromTime(30));
////		drawVerticalLine(g, (pixEnd));
//	}


//	private void drawVerticalLine(Graphics g, int xCoordPixels) {
//		((Graphics2D) g).setStroke(new BasicStroke(1));
//		g.drawLine(xCoordPixels, 0, xCoordPixels, getHeight());
//	}
//
//	private void drawMajorTick(Graphics g, FontMetrics fontMetrics, int time) {
//		int xCoordPixels = time; //computeXFromTime(time);
//		((Graphics2D) g).setStroke(new BasicStroke(1));
//		int lineEnd = getHeight();
//		g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
////		String tickLabel = "" + time;
////		g.drawString(tickLabel, xCoordPixels - (fontMetrics.stringWidth(tickLabel) / 2), lineEnd + fontMetrics.getAscent());
//	}

	private void drawTimeText(Graphics g, List<TimeTick> tickPoints, FontMetrics fontMetrics) {
		for (TimeTick timeTick : tickPoints) {
			String tickLabel = "" + timeTick.getTime();
			g.drawString(tickLabel, timeTick.getLoc() - (fontMetrics.stringWidth(tickLabel) / 2), fontMetrics.getHeight() / 2 + fontMetrics.getAscent());
		}
	}


//	private void fillWithGrad(Graphics g, int loc) {
////		for(int i = 0; i<nColors-1; i++){
////			Point p1 = new Point(0, 10);
////			Point p2 = new Point(0, 10);
////			((Graphics2D) g).setPaint(new GradientPaint(p1, colors.get(i), p2, colors.get(i+1)));
////		}
//		Color[] colors = {
//				new Color(0, 255, 0, 170),
//				new Color(255, 0, 0, 170),
//				new Color(0, 0, 255, 170),
//				new Color(255, 255, 0, 170),
//		};
//		float[] fractions = new float[colors.length];
//		for (int i = 0; i < colors.length; i++) {
//			fractions[i] = i * 1f / (float) colors.length;
//		}
//
//		int y = 30;
//		int h = 25;
//		int w = 20;
//		LinearGradientPaint multipleGradientPaint = new LinearGradientPaint(w / 2.5f + loc, y, w + loc, y + h, fractions, colors);
////		LinearGradientPaint multipleGradientPaint = new LinearGradientPaint(fractions, colors1, repeat, type, scaleType);
//
////		GradientPaint paint = new GradientPaint();
//		((Graphics2D) g).setPaint(multipleGradientPaint);
//
//		g.fillRoundRect(loc - w / 2, y, w, h, 2, 2);
//	}


	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(baseBgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		// time markers
		List<TimeTick> tickPoints = getTicks();
		timeBarPainter.drawTimeTicks(g, tickPoints);

		// mark out of animation time
		g.setColor(outOfRangeColor);
		g.fillRect(0, 0, getXFromTime(start), getHeight());
		g.fillRect(getXFromTime(end), 0, getWidth() - getXFromTime(end), getHeight());

		FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

		g.setColor(majorTickColor);
		g.fillRect(0, 0, getWidth(), fontMetrics.getHeight() * 2);

		g.setColor(Color.WHITE);
		drawTimeText(g, tickPoints, fontMetrics);

		drawTimeMarker(g, fontMetrics);
	}

	public interface TimeListener {
		void timeChanged(int time);
	}

	private static class TimeTick {
		int time;
		int loc;

		TimeTick(int time, int loc) {
			this.time = time;
			this.loc = loc;
		}

		public int getTime() {
			return time;
		}

		public int getLoc() {
			return loc;
		}
	}

	private static class TimeBarPainter {
		Color baseBgColor = Color.DARK_GRAY.brighter();
		Color timeMarkerColor = Color.BLUE.brighter();
		Color outOfRangeColor = new Color(0, 0, 0, 60);
		Color majorTickColor = new Color(0, 0, 0, 150);
		Color endMarkerColor = new Color(0, 0, 0, 200);
		Color minorTickColor = new Color(0, 0, 0, 60);

		TimeLinePanel timeLinePanel;
		TimeBarPainter(TimeLinePanel timeLinePanel) {
			this.timeLinePanel = timeLinePanel;
		}

		private int getTimeFromX(int x) {
			return x * timeLinePanel.tickTime / timeLinePanel.tickStep + timeLinePanel.realStart;
		}

		private int getXFromTime(int time) {
			return ((time - timeLinePanel.realStart) * timeLinePanel.tickStep) / timeLinePanel.tickTime;
		}
		private void drawTimeTicks(Graphics g, List<TimeTick> timeTicks) {
			// draw first time marker
			g.setColor(endMarkerColor);
			int realTimeSpan = timeLinePanel.realEnd - timeLinePanel.realStart;
			double pixelsPerTime = timeLinePanel.getWidth() / (double) realTimeSpan;
			int pixStart = (int) (pixelsPerTime * (timeLinePanel.start - timeLinePanel.realStart));
			drawVerticalLine(g, getXFromTime(timeLinePanel.start));
//		drawVerticalLine(g, (pixStart));
			// draw even (time%tickStep==0) time markers
			g.setColor(minorTickColor);
			drawVerticalLine(g, timeTicks.get(0).getLoc() - (timeLinePanel.tickStep / 2));
			for (TimeTick timeTick : timeTicks) {
				g.setColor(majorTickColor);
				drawVerticalLine(g, timeTick.getLoc());
				g.setColor(minorTickColor);
				drawVerticalLine(g, timeTick.getLoc() + (timeLinePanel.tickStep / 2));
			}
			// draw last time marker
			g.setColor(endMarkerColor);
			int pixEnd = (int) (pixelsPerTime * (timeLinePanel.end - timeLinePanel.realStart));
			drawVerticalLine(g, getXFromTime(timeLinePanel.end));
			System.out.println("(in ticks) x from time = " + 30 + ": " + getXFromTime(30));
//		drawVerticalLine(g, (pixEnd));
		}


		private void drawVerticalLine(Graphics g, int xCoordPixels) {
			((Graphics2D) g).setStroke(new BasicStroke(1));
			g.drawLine(xCoordPixels, 0, xCoordPixels, timeLinePanel.getHeight());
		}

		private void drawMajorTick(Graphics g, FontMetrics fontMetrics, int time) {
			int xCoordPixels = time; //computeXFromTime(time);
			((Graphics2D) g).setStroke(new BasicStroke(1));
			int lineEnd = timeLinePanel.getHeight();
			g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
//		String tickLabel = "" + time;
//		g.drawString(tickLabel, xCoordPixels - (fontMetrics.stringWidth(tickLabel) / 2), lineEnd + fontMetrics.getAscent());
		}
	}
}
