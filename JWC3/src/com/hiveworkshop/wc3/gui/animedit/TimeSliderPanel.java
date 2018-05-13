package com.hiveworkshop.wc3.gui.animedit;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.JPanel;

public class TimeSliderPanel extends JPanel implements TimeBoundChangeListener {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = 100 + SLIDER_SIDE_BUTTON_SIZE * 2;
	private static final int VERTICAL_TICKS_HEIGHT = 18;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(2);

	private boolean keyframeModeActive;
	private final TimeBoundProvider timeBoundProvider;
	private int start, end = 30;
	private int currentTime = 0;

	private final Rectangle timeChooserRect;

	private Point lastMousePoint;
	private boolean draggingSlider = false;
	private Robot robot;

	public TimeSliderPanel(final TimeBoundProvider timeBoundProvider) {
		this.timeBoundProvider = timeBoundProvider;
		add(Box.createVerticalStrut(VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		timeBoundProvider.addChangeListener(this);
		start = timeBoundProvider.getStart();
		end = timeBoundProvider.getEnd();
		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));
		timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (draggingSlider) {
					timeChooserRect.x = computeSliderXFromTime();
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				lastMousePoint = e.getPoint();
				draggingSlider = sliderContainsPoint(lastMousePoint);
				if (!draggingSlider) {
					if (lastMousePoint.x > timeChooserRect.x
							&& lastMousePoint.x < timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE
							&& lastMousePoint.y < timeChooserRect.y + timeChooserRect.height) {
						stepBackwards();
					} else if (lastMousePoint.x > timeChooserRect.x + timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE
							&& lastMousePoint.x < timeChooserRect.x + timeChooserRect.width
							&& lastMousePoint.y < timeChooserRect.y + timeChooserRect.height) {
						stepForwards();
					}
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {

			}

			@Override
			public void mouseEntered(final MouseEvent e) {

			}

			@Override
			public void mouseClicked(final MouseEvent e) {

			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(final MouseEvent e) {
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				final Point mousePoint = e.getPoint();
				if (draggingSlider) {
					final double dx = mousePoint.getX() - lastMousePoint.getX();
					timeChooserRect.x += (int) dx;
					final int maxXPosition = getWidth() - timeChooserRect.width;
					if (timeChooserRect.x > maxXPosition) {
						timeChooserRect.x = maxXPosition;
					} else if (timeChooserRect.x < 0) {
						timeChooserRect.x = 0;
					}
					final int computedTime = computeTimeFromSlider();
					if (computedTime != currentTime) {
						currentTime = computedTime;
					}
					repaint();
				}
				lastMousePoint = e.getPoint();
			}
		});
		try {
			robot = new Robot();
		} catch (final AWTException e1) {
			e1.printStackTrace();
		}
	}

	private void stepBackwards() {
		if (currentTime > start) {
			currentTime--;
			int pixelDelta = timeChooserRect.x;
			timeChooserRect.x = computeSliderXFromTime();
			pixelDelta = timeChooserRect.x - pixelDelta;
			if (robot != null) {
				robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + pixelDelta,
						MouseInfo.getPointerInfo().getLocation().y);
			}
		}
		repaint();
	}

	private void stepForwards() {
		if (currentTime < end) {
			currentTime++;
			int pixelDelta = timeChooserRect.x;
			timeChooserRect.x = computeSliderXFromTime();
			pixelDelta = timeChooserRect.x - pixelDelta;
			if (robot != null) {
				robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + pixelDelta,
						MouseInfo.getPointerInfo().getLocation().y);
			}
		}
		repaint();
	}

	private int computeTimeFromSlider() {
		final int pixelCenter = timeChooserRect.x;
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		final double timeRatio = pixelCenter / (double) widthMinusOffsets;
		final int computedTime = (int) (timeRatio * (end - start)) + start;
		return computedTime;
	}

	private int computeTimeFromX(final int x) {
		final int pixelCenter = x - timeChooserRect.width / 2;
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		final double timeRatio = pixelCenter / (double) widthMinusOffsets;
		final int computedTime = (int) (timeRatio * (end - start)) + start;
		return computedTime;
	}

	private int computeSliderXFromTime() {
		final double timeRatio = currentTime / (double) (end - start);
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		return (int) (widthMinusOffsets * timeRatio);
	}

	private int computeXFromTime(final int time) {
		final double timeRatio = time / (double) (end - start);
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		return (int) (widthMinusOffsets * timeRatio) + timeChooserRect.width / 2;
	}

	public boolean sliderContainsPoint(final Point mousePoint) {
		return mousePoint.getY() < timeChooserRect.y + timeChooserRect.height
				&& mousePoint.getX() > timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE
				&& mousePoint.getX() < timeChooserRect.x + timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final int width = getWidth();
		if (keyframeModeActive) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLUE.darker());
		}
		g.fillRect(0, 0, width, VERTICAL_SLIDER_HEIGHT);
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.y,
				timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE * 2, timeChooserRect.height, true);
		g.fill3DRect(timeChooserRect.x, timeChooserRect.y, SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.height, true);
		g.fill3DRect(timeChooserRect.x + timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.y,
				SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.height, true);
		final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
		g.setColor(getForeground());
		String timeChooserLabel = currentTime + " / " + end;
		g.drawString(timeChooserLabel,
				timeChooserRect.x + (timeChooserRect.width - fontMetrics.stringWidth(timeChooserLabel)) / 2,
				timeChooserRect.y + (timeChooserRect.height + fontMetrics.getAscent()) / 2 - 1);
		timeChooserLabel = "<";
		g.drawString(timeChooserLabel,
				timeChooserRect.x + (SLIDER_SIDE_BUTTON_SIZE - fontMetrics.stringWidth(timeChooserLabel)) / 2,
				timeChooserRect.y + (timeChooserRect.height + fontMetrics.getAscent()) / 2 - 1);
		timeChooserLabel = ">";
		g.drawString(timeChooserLabel,
				timeChooserRect.x + timeChooserRect.width
						- (SLIDER_SIDE_BUTTON_SIZE + fontMetrics.stringWidth(timeChooserLabel)) / 2,
				timeChooserRect.y + (timeChooserRect.height + fontMetrics.getAscent()) / 2 - 1);
		final int widthMinusOffsets = width - SIDE_OFFSETS * 2;
		if (widthMinusOffsets < 0) {
			g.drawString("No pixels", 0, 16);
			return;
		}
		final int timeSpan = end - start;
		final int tickWidthPixels = widthMinusOffsets / 30;
		final int tickWidthTime = timeSpan / 30;
		for (int i = 0; i <= 30; i++) {
			final int xCoordPixels = SIDE_OFFSETS + i * tickWidthPixels;
			final boolean majorTick = i % 2 == 0;
			if (majorTick) {
				((Graphics2D) g).setStroke(WIDTH_2_STROKE);
				final int lineEnd = getHeight() - fontMetrics.getAscent();
				g.drawLine(xCoordPixels, VERTICAL_SLIDER_HEIGHT, xCoordPixels, lineEnd);
				final String tickLabel = "" + computeTimeFromX(xCoordPixels);
				g.drawString(tickLabel, xCoordPixels - fontMetrics.stringWidth(tickLabel) / 2,
						lineEnd + fontMetrics.getAscent());
			} else {
				((Graphics2D) g).setStroke(WIDTH_1_STROKE);
				final int lineEnd = VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT / 2;
				g.drawLine(xCoordPixels, VERTICAL_SLIDER_HEIGHT, xCoordPixels, lineEnd);
			}
		}
		// glass covering current tick
		g.setColor(GLASS_TICK_COVER_COLOR);
		final int currentTimePixelX = computeXFromTime(currentTime);
		g.fillRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
		g.setColor(GLASS_TICK_COVER_BORDER_COLOR);
		g.drawRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
	}

	@Override
	public void timeBoundsChanged(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	public void setKeyframeModeActive(final boolean keyframeModeActive) {
		this.keyframeModeActive = keyframeModeActive;
	}
}
