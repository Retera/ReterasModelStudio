package com.hiveworkshop.wc3.gui.animedit;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.animedit.TimeSliderTimeListener.TimeSliderTimeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SlideKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.IdObject;

public class TimeSliderPanel extends JPanel implements TimeBoundChangeListener, SelectionListener {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = 100 + SLIDER_SIDE_BUTTON_SIZE * 2;
	private static final int VERTICAL_TICKS_HEIGHT = 18;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);

	private boolean keyframeModeActive;
	private final TimeBoundProvider timeBoundProvider;
	private int start, end = 30;
	private int currentTime = 0;

	private final Rectangle timeChooserRect;

	private Point lastMousePoint;
	private boolean draggingSlider = false;
	private Robot robot;

	private final TimeSliderTimeNotifier notifier;

	private SelectionManager<IdObject> nodeSelectionManager;
	private final GradientPaint keyframePaint;
	private final Map<Integer, KeyFrame> timeToKey = new LinkedHashMap<>();

	private final JPopupMenu popupMenu;
	private KeyFrame mouseOverFrame = null;
	private KeyFrame draggingFrame = null;
	private int draggingFrameStartTime = 0;
	private int mouseDragXOffset = 0;

	private final Cursor slideCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
	private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private UndoActionListener undoManager;

	public TimeSliderPanel(final TimeBoundProvider timeBoundProvider) {
		this.timeBoundProvider = timeBoundProvider;
		this.notifier = new TimeSliderTimeNotifier();
		add(Box.createVerticalStrut(VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		timeBoundProvider.addChangeListener(this);
		start = timeBoundProvider.getStart();
		end = timeBoundProvider.getEnd();
		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));
		timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);
		this.popupMenu = new JPopupMenu();
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				final Point mousePoint = e.getPoint();
				if (draggingSlider) {
					timeChooserRect.x = computeSliderXFromTime();
					draggingSlider = false;
				} else if (draggingFrame != null) {
					updateDraggedKeyframe(mousePoint);
					draggingFrame.renderRect.x = computeXFromTime(draggingFrame.time);
					if (undoManager != null) {
						undoManager.pushAction(new SlideKeyframeAction(draggingFrameStartTime, draggingFrame.time,
								draggingFrame.timelines));
					}
					draggingFrame = null;
					repaint();
				}
				checkMouseOver(mousePoint);
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
					} else {
						if (SwingUtilities.isLeftMouseButton(e)) {
							for (final KeyFrame frame : timeToKey.values()) {
								if (frame.renderRect.contains(lastMousePoint)) {
									draggingFrame = frame;
									draggingFrameStartTime = frame.time;
									mouseDragXOffset = (int) (lastMousePoint.getX() - frame.renderRect.x);
									break;
								}
							}
						}
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
				if (SwingUtilities.isRightMouseButton(e)) {
					for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
						if (timeAndKey.getValue().renderRect.contains(e.getPoint())) {
							popupMenu.removeAll();
							popupMenu.add(new JMenuItem("Delete All"));
							popupMenu.addSeparator();
							popupMenu.add(new JMenuItem("Cut"));
							popupMenu.add(new JMenuItem("Copy"));
							popupMenu.add(new JMenuItem("Paste"));
							popupMenu.addSeparator();
							for (final IdObject object : timeAndKey.getValue().objects) {
								for (final AnimFlag flag : object.getAnimFlags()) {
									final int flooredTimeIndex = flag.floorIndex(timeAndKey.getKey());
									if (flooredTimeIndex < flag.getTimes().size()
											&& flag.getTimes().get(flooredTimeIndex) == timeAndKey.getKey()) {
										final JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
										popupMenu.add(subMenu);
										subMenu.add(new JMenuItem("Delete All"));
										subMenu.addSeparator();
										subMenu.add(new JMenuItem("Cut"));
										subMenu.add(new JMenuItem("Copy"));
										subMenu.add(new JMenuItem("Paste"));
									}
								}
							}
							popupMenu.show(TimeSliderPanel.this, e.getX(), e.getY());
						}
					}
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				checkMouseOver(e.getPoint());
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
						notifier.timeChanged(currentTime);
					}
					repaint();
				} else if (draggingFrame != null) {
					updateDraggedKeyframe(mousePoint);
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

		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(final ComponentEvent e) {

			}

			@Override
			public void componentResized(final ComponentEvent e) {
				slideExistingKeyFramesForResize();
				timeChooserRect.x = computeSliderXFromTime();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {

			}

			@Override
			public void componentHidden(final ComponentEvent e) {

			}
		});
		keyframePaint = new GradientPaint(new Point(0, 10), new Color(200, 255, 200), new Point(0, getHeight()),
				new Color(100, 255, 100), true);
	}

	public void setUndoManager(final UndoActionListener undoManager) {
		this.undoManager = undoManager;
	}

	public void updateDraggedKeyframe(final Point mousePoint) {
		final double dx = mousePoint.getX() - lastMousePoint.getX();
		draggingFrame.renderRect.x = mousePoint.x - mouseDragXOffset;
		final int maxXPosition = getWidth() - SIDE_OFFSETS - draggingFrame.renderRect.width;
		int computedTime = computeTimeFromX(draggingFrame.renderRect.x + draggingFrame.renderRect.width / 2);
		if (computedTime < start) {
			computedTime = start;
			draggingFrame.renderRect.x = computeXFromTime(start);
		} else if (computedTime > end) {
			computedTime = end;
			draggingFrame.renderRect.x = computeXFromTime(end);
		}
		if (computedTime != draggingFrame.time && !timeToKey.containsKey(computedTime)) {
			timeToKey.remove(draggingFrame.time);
			for (final AnimFlag timeline : draggingFrame.timelines) {
				timeline.slideKeyframe(draggingFrame.time, computedTime);
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(currentTime);
			}
			draggingFrame.time = computedTime;
			timeToKey.put(draggingFrame.time, draggingFrame);
		}
	}

	public void checkMouseOver(final Point mousePt) {
		KeyFrame newMouseOver = null;
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			if (timeAndKey.getValue().renderRect.contains(mousePt)) {
				newMouseOver = timeAndKey.getValue();
			}
		}
		if (newMouseOver != mouseOverFrame) {
			mouseOverFrame = newMouseOver;
			if (mouseOverFrame != null) {
				setCursor(slideCursor);
			} else {
				setCursor(null);
			}
			repaint();
		}
	}

	public void setNodeSelectionManager(final SelectionManager<IdObject> nodeSelectionManager) {
		if (this.nodeSelectionManager != nodeSelectionManager) {
			if (this.nodeSelectionManager != null) {
				this.nodeSelectionManager.removeSelectionListener(this);
			}
			this.nodeSelectionManager = nodeSelectionManager;
			if (this.nodeSelectionManager != null) {
				this.nodeSelectionManager.addSelectionListener(this);
			}
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
			notifier.timeChanged(currentTime);
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
			notifier.timeChanged(currentTime);
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
		final double timeRatio = (currentTime - start) / (double) (end - start);
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		return (int) (widthMinusOffsets * timeRatio);
	}

	private int computeXFromTime(final int time) {
		final double timeRatio = (time - start) / (double) (end - start);
		final int widthMinusOffsets = getWidth() - SIDE_OFFSETS * 2;
		return (int) (widthMinusOffsets * timeRatio) + timeChooserRect.width / 2;
	}

	public boolean sliderContainsPoint(final Point mousePoint) {
		return mousePoint.getY() < timeChooserRect.y + timeChooserRect.height
				&& mousePoint.getX() > timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE
				&& mousePoint.getX() < timeChooserRect.x + timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE;
	}

	public void addListener(final TimeSliderTimeListener listener) {
		notifier.subscribe(listener);
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

		// keyframes
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			final int currentTimePixelX = computeXFromTime(timeAndKey.getKey());
			final boolean mouseOver = timeAndKey.getValue() == mouseOverFrame;
			((Graphics2D) g).setPaint(keyframePaint);
			g.fillRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
			g.setColor(mouseOver ? Color.RED : Color.GREEN);
			g.drawRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);

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
		currentTime = start;
		// if (currentTime < start) {
		// currentTime = start;
		// }
		// if (currentTime > end) {
		// currentTime = end;
		// }
		timeChooserRect.x = computeSliderXFromTime();
		updateKeyframeDisplay();
		repaint();
	}

	public void setKeyframeModeActive(final boolean keyframeModeActive) {
		this.keyframeModeActive = keyframeModeActive;
	}

	public int getCurrentTime() {
		return currentTime;
	}

	private void slideExistingKeyFramesForResize() {
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			timeAndKey.getValue().reposition();
		}
	}

	private void updateKeyframeDisplay() {
		timeToKey.clear();
		if (nodeSelectionManager != null) {
			final Set<IdObject> selection = nodeSelectionManager.getSelection();
			for (final IdObject object : selection) {
				for (final AnimFlag flag : object.getAnimFlags()) {
					final int flagStartIndex = flag.ceilIndex(start);
					final int endFlagIndex = flag.floorIndex(end);
					if (flag.size() > 0) {
						for (int flagIndex = flagStartIndex; flagIndex <= endFlagIndex; flagIndex++) {
							final Integer time = flag.getTimes().get(flagIndex);
							KeyFrame keyFrame = timeToKey.get(time);
							if (keyFrame == null) {
								keyFrame = new KeyFrame(time);
								timeToKey.put(time, keyFrame);
							}
							keyFrame.objects.add(object);
							keyFrame.timelines.add(flag);
						}
					}
				}
			}
		}
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		updateKeyframeDisplay();
		repaint();
	}

	// called when user is editing the keyframes and they need to be updated
	public void revalidateKeyframeDisplay() {
		updateKeyframeDisplay();
		repaint();
	}

	private final class KeyFrame {
		private int time;
		private final Set<IdObject> objects = new HashSet<>();
		private final List<AnimFlag> timelines = new ArrayList<>();
		private final Rectangle renderRect;

		public KeyFrame(final int time) {
			this.time = time;
			final int currentTimePixelX = computeXFromTime(time);
			this.renderRect = new Rectangle(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
		}

		public void reposition() {
			renderRect.x = computeXFromTime(time) - 4;
		}
	}
}
