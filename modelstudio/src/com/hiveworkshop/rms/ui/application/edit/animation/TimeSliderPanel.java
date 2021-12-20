package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddKeyframeAction3;
import com.hiveworkshop.rms.editor.actions.animation.SlideKeyframeAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.TimeSliderView;
import com.hiveworkshop.rms.ui.application.actionfunctions.TimeSkip;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class TimeSliderPanel extends JPanel implements SelectionListener {
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

	private final TimeSlider timeSlider;
	private final TimeBarPainter timeBarPainter;

	private boolean keyframeModeActive;
	private int tickStep = 300;
	private TimeEnvironmentImpl timeEnvironment;

	private final TimeSliderTimeListener notifier;

	private final JPopupMenu popupMenu;
	private KeyFrame draggingFrame = null;
	private int draggingFrameStartTime = 0;
	private boolean draggingSlider = false;
	private Robot robot;

	private final Cursor slideCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
//	private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private UndoManager undoManager;

	private final JCheckBox allKF;
	private final Timer liveAnimationTimer;
//	private final ProgramPreferences preferences;
	private final GUITheme theme;
	private boolean drawing;
	private final JButton setKeyframe;
	private final JButton setTimeBounds;
	private final JButton playButton;
	private final KeyframeHandler keyframeHandler;

	public TimeSliderPanel(ProgramPreferences preferences) {
//		this.changeListener = ModelStructureChangeListener.changeListener;
//		this.preferences = preferences;
		theme = preferences.getTheme();
		notifier = new TimeSliderTimeListener();
		setLayout(new MigLayout("fill, gap 0, ins 0, aligny top", "3[]3[grow]3", "[]"));
		JPanel buttonPanel = new JPanel(new MigLayout("ins 0"));
		playButton = new JButton(RMSIcons.PLAY);
		playButton.addActionListener(e -> pausePlayAnimation());
//		JButton playButton = new JButton("||>");
		playButton.setPreferredSize(PLAY_BUTTON_DIMENSION);
		playButton.setSize(PLAY_BUTTON_DIMENSION);
		buttonPanel.add(playButton, "spany 2");
		allKF = new JCheckBox("All KF");
		allKF.addActionListener(e -> revalidateKeyframeDisplay());


		setKeyframe = createSetKeyframeButton();
		buttonPanel.add(setKeyframe, "wrap");
		setTimeBounds = TimeSliderView.createSetTimeBoundsButton();
		buttonPanel.add(setTimeBounds, "wrap");

		buttonPanel.add(allKF, "wrap");
		buttonPanel.setOpaque(true);
		add(buttonPanel, "aligny top, shrink");

//		JPanel timelinePanel = new TimeLinePanel_test();
		JPanel timelinePanel = getTimelinePanel();
		timelinePanel.setOpaque(true);
		add(timelinePanel, "growx, growy, aligny top");

		timeSlider = new TimeSlider(timelinePanel);
		timeBarPainter = new TimeBarPainter(timeSlider);

		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));

		popupMenu = new JPopupMenu();

//		copiedKeyframes = new ArrayList<>();

		liveAnimationTimer = new Timer(16, e -> liveAnimationTimerListener());
		MouseAdapter mouseAdapter = getMouseAdapter();
		timelinePanel.addMouseListener(mouseAdapter);
		timelinePanel.addMouseMotionListener(mouseAdapter);
		try {
			robot = new Robot();
		} catch (final AWTException e1) {
			e1.printStackTrace();
		}

		timelinePanel.addComponentListener(getComponentAdapter());

		keyframeHandler = new KeyframeHandler(notifier, timelinePanel);

		TimeSkip.getPlayItem();
		TimeSkip.getFfw1Item();
		TimeSkip.getFfw10Item();
		TimeSkip.getBbw1Item();
		TimeSkip.getBbw10Item();
		TimeSkip.getNextKFItem();
		TimeSkip.getPrevKFItem();
	}

	public static JButton createSetKeyframeButton() {
		JButton setKeyframe;
		setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
		setKeyframe.setMargin(new Insets(0, 0, 0, 0));
		setKeyframe.setToolTipText("Create Keyframe");
		setKeyframe.addActionListener(e -> createKeyframe());
		return setKeyframe;
	}

	public KeyframeHandler getKeyframeHandler() {
		return keyframeHandler;
	}

	private static void createKeyframe() {
		 ModelPanel mpanel = ProgramGlobals.getCurrentModelPanel();
		if (mpanel != null) {
			UndoAction undoAction = new AddKeyframeAction3(mpanel.getModelHandler(), mpanel.getEditorActionType());
			mpanel.getUndoManager().pushAction(undoAction.redo());
			mpanel.repaintSelfAndRelatedChildren();
		}
		ProgramGlobals.getMainPanel().repaintSelfAndChildren();
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				keyframeHandler.slideExistingKeyFramesForResize();
				if (timeEnvironment != null) {
					timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
//					timeChooserRect.x = computeSliderXFromTime();
				}
			}
		};
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			Point lastMousePoint;
			double mouseDownPointX = 0;
			int mouseDragXOffset = 0;
			@Override
			public void mouseReleased(final MouseEvent e) {
				super.mouseReleased(e);
				if (drawing) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						Point mousePoint = e.getPoint();
						if (draggingSlider) {
							timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
							draggingSlider = false;
						} else if (draggingFrame != null && mouseDownPointX != e.getPoint().getX()) {
							int dx = mousePoint.x - mouseDragXOffset;
							applyDraggedFrame(dx);
						}
						checkMouseOver(mousePoint);
					} else if (SwingUtilities.isRightMouseButton(e)) {
						showPopupIfFrameExists(e);
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				mouseDownPointX = e.getPoint().getX();
				if (drawing) {
					lastMousePoint = e.getPoint();
					draggingSlider = timeSlider.onSlide(lastMousePoint);
					if (!draggingSlider) {
						mouseDragXOffset = TimeSliderPanel.this.mousePressed(e, lastMousePoint);
					}
				}
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				super.mouseMoved(e);
				if (drawing) {
					checkMouseOver(e.getPoint());
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				super.mouseDragged(e);
				if (drawing) {
					Point mousePoint = e.getPoint();
					if (draggingSlider) {
						dragTimeSlider((int) (mousePoint.getX() - lastMousePoint.getX()));
					} else if (draggingFrame != null) {
						int newX = mousePoint.x - mouseDragXOffset;
						updateDraggedKeyframe(newX, draggingFrame);
						repaint();
					}
					lastMousePoint = e.getPoint();
				}
			}
		};
	}

	private void dragTimeSlider(int dx) {
		timeSlider.moveSlider(dx);
		int computedTime = (int) (timeSlider.getLocationFraction() * timeEnvironment.getLength());
		if (computedTime != timeEnvironment.getEnvTrackTime()) {
			timeEnvironment.setAnimationTime(computedTime);
			notifier.timeChanged(timeEnvironment.getEnvTrackTime());
		}
		repaint();
	}

	private void showPopupIfFrameExists(MouseEvent mouseEvent) {
		boolean foundFrame = false;
		for (Integer time : keyframeHandler.getTimes()) {
			if (keyframeHandler.getKeyFrame(time).containsPoint(mouseEvent.getPoint())) {
				foundFrame = showTimeSliderPopup3(mouseEvent, time);
			}
		}
		if (!foundFrame && timeSlider.containsPoint(mouseEvent.getPoint())) {
			showTimeSliderPopUp2(mouseEvent);
		}
	}

	private void applyDraggedFrame(int newX) {
		updateDraggedKeyframe(newX, draggingFrame);
		if (undoManager != null && draggingFrameStartTime != draggingFrame.getTime()) {
			undoManager.pushAction(new SlideKeyframeAction(draggingFrameStartTime, draggingFrame.getTime(), draggingFrame.getTimelines(), timeEnvironment.getCurrentSequence(), this::revalidateKeyframeDisplay));
		}
		draggingFrame = null;
		repaint();
	}

	private int mousePressed(MouseEvent mouseEvent, Point lastMousePoint) {
		if (timeSlider.onBackward(lastMousePoint)) {
			timeStep(-1, true);
		} else if (timeSlider.onForward(lastMousePoint)) {
			timeStep(1, true);
		} else {
			if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
				Integer lastMousePoint1 = initDragging(lastMousePoint);
				if (lastMousePoint1 != null) return lastMousePoint1;
			}
		}
		return 0;
	}

	private Integer initDragging(Point lastMousePoint) {
		draggingFrame = keyframeHandler.initDragging(lastMousePoint);
		if(draggingFrame != null) {
			draggingFrameStartTime = draggingFrame.getTime();
//			return (int) (lastMousePoint.getX() - draggingFrame.getRenderRect().x);
			return (int) (lastMousePoint.getX() - draggingFrame.getXPoint());
		}
		return null;
	}

	private boolean showTimeSliderPopup3(MouseEvent mouseEvent, Integer time) {
		popupMenu.removeAll();

		JMenuItem timeIndicator = new JMenuItem("" + time);
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();
		popupMenu.add(getMenuItem("Delete All", e -> keyframeHandler.deleteKeyframes("delete keyframe", time, keyframeHandler.getKeyFrame(time).getObjects())));
		popupMenu.addSeparator();
		popupMenu.add(getMenuItem("Cut", e -> keyframeHandler.cutItem(time)));
		popupMenu.add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(time)));
		popupMenu.add(getMenuItem("Copy Frame (whole model)", e -> keyframeHandler.copyAllKeyframes(time)));
		popupMenu.add(getMenuItem("Paste", e -> keyframeHandler.pasteToAllSelected(time)));

		popupMenu.addSeparator();

		for (IdObject object : keyframeHandler.getKeyFrame(time).getObjects()) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if (flag.hasEntryAt(timeEnvironment.getCurrentSequence(), time)) {
					JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());

					subMenu.add(getMenuItem("Delete", e -> keyframeHandler.deleteKeyframe(flag, time)));
					subMenu.addSeparator();
					subMenu.add(getMenuItem("Cut", e -> keyframeHandler.cutSpecificItem(time, object, flag)));
					subMenu.add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(object, flag, time)));
					subMenu.add(getMenuItem("Paste", e -> keyframeHandler.pasteToSpecificTimeline(time, flag)));

					popupMenu.add(subMenu);
				}
			}
		}
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
		return true;
	}

	private JMenuItem getMenuItem(String text, ActionListener actionListener) {
		JMenuItem deleteSpecificItem = new JMenuItem(text);
		deleteSpecificItem.addActionListener(actionListener);
		return deleteSpecificItem;
	}

	private void showTimeSliderPopUp2(MouseEvent mouseEvent) {
		popupMenu.removeAll();

		JMenuItem timeIndicator = new JMenuItem("" + timeEnvironment.getEnvTrackTime());
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();
		popupMenu.add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(timeEnvironment.getEnvTrackTime())));
		popupMenu.add(getMenuItem("Copy Frame (whole model)", e -> keyframeHandler.copyAllKeyframes(timeEnvironment.getEnvTrackTime())));
		popupMenu.add(getMenuItem("Paste", e -> keyframeHandler.pasteToAllSelected(timeEnvironment.getEnvTrackTime())));
		popupMenu.addSeparator();
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
	}

	private void liveAnimationTimerListener() {
		if (!drawing || timeEnvironment == null || !timeEnvironment.isLive()) {
			return;
		}
		timeEnvironment.updateAnimationTime();
		notifier.timeChanged(timeEnvironment.getEnvTrackTime());
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		repaint();
	}

	public void setModelHandler(ModelHandler modelHandler) {
		keyframeHandler.setModelHandler(modelHandler);
		if(modelHandler != null) {
			this.undoManager = modelHandler.getUndoManager();
			timeEnvironment = modelHandler.getEditTimeEnv();
			timeEnvironment.addChangeListener(this);
			timeBarPainter.setTimeEnvironment(timeEnvironment);
		} else {
			timeBarPainter.setTimeEnvironment(null);
			this.undoManager = null;
		}
	}


	public void checkMouseOver(Point mousePt) {
		if (keyframeHandler.getTimeFromPoint(mousePt) != null) {
			setCursor(slideCursor);
		} else {
			setCursor(null);
		}
		repaint();
	}

	public void jumpToPreviousTime() {
		Integer newTime = keyframeHandler.getPrevFrame();
		setCurrentTime(newTime == null ? 0 : newTime);
	}

	public void jumpToNextTime() {
		Integer newTime = keyframeHandler.getNextFrame();
		setCurrentTime(newTime == null ? 0 : newTime);
	}

	public void jumpFrames(int deltaFrames) {
		int newTime = timeEnvironment.getEnvTrackTime() + deltaFrames;
		if (newTime > timeEnvironment.getLength()) {
			newTime = timeEnvironment.getLength();
		} else if (newTime < 0) {
			newTime = 0;
		}
		setCurrentTime(newTime);
	}


	public void setCurrentTime(int newTime) {
		timeEnvironment.setAnimationTime(newTime);
		notifier.timeChanged(timeEnvironment.getEnvTrackTime());
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		repaint();
	}

	public void timeStep(int step) {
		timeStep(step, false);
	}

	private void timeStep(int step, boolean moveMouse) {
		timeEnvironment.stepAnimationTime(step);
		if (robot != null && moveMouse) {
			int xStart = timeSlider.getX();
			timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());

			int pixelDelta = timeSlider.getX() - xStart;
			robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + pixelDelta, MouseInfo.getPointerInfo().getLocation().y);
		} else {
			timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		}
		notifier.timeChanged(timeEnvironment.getEnvTrackTime());
		repaint();
	}


	public void addListener(Consumer<Integer> listener) {
		notifier.subscribe(listener);
	}

	//	@Override
	public void timeBoundsChanged(int start, int end) {
		liveAnimationTimer.stop();
		playButton.setIcon(RMSIcons.PLAY);
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	public void setKeyframeModeActive(final boolean keyframeModeActive) {
		this.keyframeModeActive = keyframeModeActive;
	}

	public void updateDraggedKeyframe(int newX, KeyFrame draggingFrame) {
		draggingFrame.getRenderRect().x = newX;

		int computedTime = (int) ((newX / (double) timeSlider.getMaxX()) * timeEnvironment.getLength());

		if (computedTime < 0) {
			computedTime = 0;
//			draggingFrame.setFrameX(computedTime);
		} else if (computedTime > timeEnvironment.getLength()) {
			computedTime = timeEnvironment.getLength();
//			draggingFrame.setFrameX(computedTime);
		}
		int oldTime = draggingFrame.getTime();
//		draggingFrame.drag(newX);
		draggingFrame.setTime(computedTime);
		if ((draggingFrame.getTime() != oldTime) && keyframeHandler.getKeyFrame(draggingFrame.getTime()) == null) {
			keyframeHandler.removeFrame(oldTime);
			for (AnimFlag<?> timeline : draggingFrame.getTimelines()) {
				timeline.slideKeyframe(oldTime, draggingFrame.getTime(), timeEnvironment.getCurrentSequence());
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(timeEnvironment.getEnvTrackTime());
			}
//			draggingFrame.setTime(computedTime);
			keyframeHandler.putFrame(draggingFrame.getTime(), draggingFrame);
		}
	}

	@Override
	public void onSelectionChanged(final AbstractSelectionManager newSelection) {
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	// called when user is editing the keyframes and they need to be updated
	public void revalidateKeyframeDisplay() {
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	private JPanel getTimelinePanel() {
		return new JPanel(new MigLayout("fill")) {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if (!drawing || timeEnvironment == null) {
					return;
				}
				int width = getWidth();
				timeBarPainter.drawTimeBar(g, width);
				if (SLIDING_TIME_CHOOSER_WIDTH <= width) {
					// keyframes
					keyframeHandler.drawKeyframeMarkers(g);
					// time label of dragged keyframe
					if (draggingFrame != null) {
						draggingFrame.drawFloatingTime(g);
					}
					// time slider and glass covering current tick
					timeSlider.drawTimeSlider(g, timeEnvironment.getEnvTrackTime());
				}

			}

		};
	}

	public void play() {
		if (liveAnimationTimer.isRunning()) {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
			timeEnvironment.setLive(false);
		} else {
			timeEnvironment.setLive(true);
			liveAnimationTimer.start();
			playButton.setIcon(RMSIcons.PAUSE);
		}
		repaint();
	}

	private void pausePlayAnimation() {
		if (timeEnvironment != null) {
			if (liveAnimationTimer.isRunning()) {
				liveAnimationTimer.stop();
				playButton.setIcon(RMSIcons.PLAY);
				timeEnvironment.setLive(false);
			} else {
				timeEnvironment.setLive(true);
				liveAnimationTimer.start();
				playButton.setIcon(RMSIcons.PAUSE);
			}
		} else {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
		}
		repaint();
	}

	public void setDrawing(final boolean drawing) {
		this.drawing = drawing;
		System.out.println("is drawing: " + drawing);
		for (Component component : getComponents()) {
			component.setEnabled(drawing);
		}
		playButton.setVisible(drawing);
		setKeyframe.setVisible(drawing);
		setTimeBounds.setVisible(drawing);
		allKF.setVisible(drawing);
		repaint();
	}
}
