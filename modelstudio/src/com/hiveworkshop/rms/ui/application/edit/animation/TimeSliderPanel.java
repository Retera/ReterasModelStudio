package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddKeyframeAction3;
import com.hiveworkshop.rms.editor.actions.animation.SlideKeyframeAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.TimeSliderView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class TimeSliderPanel extends JPanel implements SelectionListener {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = 50 + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int PLAY_BUTTON_SIZE = 30;
	private static final Dimension PLAY_BUTTON_DIMENSION = new Dimension(PLAY_BUTTON_SIZE, PLAY_BUTTON_SIZE);
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);

	private boolean keyframeModeActive;
	int tickStep = 300;
	private TimeEnvironmentImpl timeEnvironment;
	JPanel timelinePanel;
	private double dx = 0;

	private final Rectangle timeChooserRect;

	private Point lastMousePoint;
	private boolean draggingSlider = false;
	private Robot robot;

	private final TimeSliderTimeListener notifier;

	//	private SelectionManager<IdObject> nodeSelectionManager;
	private final GradientPaint kfPaintRot = new GradientPaint(new Point(0, 10), new Color(100, 255, 100), new Point(0, getHeight()), new Color(0, 255, 0), true);
	private final GradientPaint kfPaintTrans = new GradientPaint(new Point(0, 10), new Color(100, 100, 255), new Point(0, getHeight()), new Color(0, 0, 255), true);
	private final GradientPaint kfPaintScale = new GradientPaint(new Point(0, 10), new Color(255, 150, 100), new Point(0, getHeight()), new Color(255, 100, 0), true);

	private final GradientPaint kfPaintTransRot = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(0, 0, 255), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(0, 255, 0), true);
	private final GradientPaint kfPaintTransScale = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(0, 0, 255), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(255, 100, 0), true);
	private final GradientPaint kfPaintRotScale = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(0, 255, 0), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(255, 100, 0), true);

	private final GradientPaint kfPaintTransTrans = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(0, 0, 255), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(0, 0, 255), true);
	private final GradientPaint kfPaintRotRot = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(0, 255, 0), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(0, 255, 0), true);
	private final GradientPaint kfPaintScaleScale = new GradientPaint(new Point(0, VERTICAL_SLIDER_HEIGHT), new Color(255, 100, 0), new Point(0, VERTICAL_TICKS_HEIGHT + VERTICAL_SLIDER_HEIGHT), new Color(255, 100, 0), true);


	private final GradientPaint keyframePaint;
	private final GradientPaint keyframePaintBlue;
	private final GradientPaint keyframePaintRed;
	private final TreeMap<Integer, KeyFrame> timeToKey = new TreeMap<>();

	private final JPopupMenu popupMenu;
	private KeyFrame mouseOverFrame = null;
	private KeyFrame draggingFrame = null;
	private int draggingFrameStartTime = 0;
	private int mouseDragXOffset = 0;

	private final Cursor slideCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
	private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private UndoManager undoManager;

	private final List<CopiedKeyFrame<?>> copiedKeyframes;
	private boolean useAllCopiedKeyframes = false;
	private final ModelStructureChangeListener structureChangeListener;
	private ModelHandler modelHandler;
	private final JCheckBox allKF;
	private final Timer liveAnimationTimer;
	private final ProgramPreferences preferences;
	private final GUITheme theme;
	private boolean drawing;
	private double mouseDownPointX = 0;
	final JButton setKeyframe;
	final JButton setTimeBounds;
	JButton playButton;
	private EditableModel model;
//	MainPanel mainPanel;
	KeyframeHandler keyframeHandler;

	public TimeSliderPanel(ProgramPreferences preferences) {
		this.structureChangeListener = ModelStructureChangeListener.changeListener;
		this.preferences = preferences;
		theme = preferences.getTheme();
//		notifier = new TimeSliderTimeNotifier();
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


//		JPanel timelinePanel = new JPanel(new MigLayout("debug"));
		timelinePanel = getTimelinePanel();
//		timelinePanel.add(Box.createVerticalStrut(VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		timelinePanel.setOpaque(true);
//		timelinePanel.setBackground(Color.cyan);
//		add(timelinePanel, "cell 1 0, grow x, gap 0, pad 0");

		add(timelinePanel, "growx, growy, aligny top");

//		timeLine = new TimeLine(preferences);
//		add(timeLine, "grow x, growy, gap 0, pad -5");


//		add(Box.createVerticalStrut(VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));

//		setMaximumSize(new Dimension(Integer.MAX_VALUE, VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT + 9999));

//		start = timeEnvironment.getStart();
//		end = timeEnvironment.getEnd();

		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));

		timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);

		popupMenu = new JPopupMenu();

		copiedKeyframes = new ArrayList<>();

		liveAnimationTimer = new Timer(30, e -> liveAnimationTimerListener());
		timelinePanel.addMouseListener(getMouseAdapter());

//		timelinePanel.addMouseMotionListener(getMouseMotionListener());
		timelinePanel.addMouseMotionListener(getMouseAdapter());
		try {
			robot = new Robot();
		} catch (final AWTException e1) {
			e1.printStackTrace();
		}

		timelinePanel.addComponentListener(getComponentAdapter());

		keyframePaint = new GradientPaint(new Point(0, 10), new Color(200, 255, 200), new Point(0, getHeight()), new Color(100, 255, 100), true);
		keyframePaintBlue = new GradientPaint(new Point(0, 10), new Color(200, 200, 255), new Point(0, getHeight()), new Color(100, 100, 255), true);
		keyframePaintRed = new GradientPaint(new Point(0, 10), new Color(255, 200, 200), new Point(0, getHeight()), new Color(255, 100, 100), true);

		keyframeHandler = new KeyframeHandler(notifier, timelinePanel);
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
//			UndoAction undoAction = mpanel.getModelEditorManager().getModelEditor().createKeyframe(mainPanel.actionTypeGroup.getActiveButtonType());

//			UndoAction undoAction = new AddKeyframeAction2(mpanel.getModelHandler(), mainPanel.actionTypeGroup.getActiveButtonType());
			UndoAction undoAction = new AddKeyframeAction3(mpanel.getModelHandler(), mpanel.getEditorActionType());
			mpanel.getUndoManager().pushAction(undoAction.redo());
			mpanel.repaintSelfAndRelatedChildren();
		}
		ProgramGlobals.getMainPanel().repaintSelfAndChildren();
	}

	public void setModel(EditableModel model) {
		this.model = model;
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				slideExistingKeyFramesForResize();
				if (timeEnvironment != null) {
					timeChooserRect.x = computeSliderXFromTime();
				}
			}
		};
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent mouseEvent) {
				if (!drawing) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
					Point mousePoint = mouseEvent.getPoint();
					if (draggingSlider) {
						timeChooserRect.x = computeSliderXFromTime();
						draggingSlider = false;
					} else if (draggingFrame != null && mouseDownPointX != mouseEvent.getPoint().getX()) {
//					} else if (draggingFrame != null ){
						updateDraggedKeyframe(mousePoint);
						draggingFrame.setFrameX(draggingFrame.time);
						if (undoManager != null) {
							if (draggingFrameStartTime != draggingFrame.time) {
								undoManager.pushAction(new SlideKeyframeAction(draggingFrameStartTime, draggingFrame.time, draggingFrame.timelines, () -> revalidateKeyframeDisplay()));
							}
						}
						draggingFrame = null;
						repaint();
					}
					checkMouseOver(mousePoint);
				} else if (SwingUtilities.isRightMouseButton(mouseEvent)) {
					boolean foundFrame = false;
					for (Integer time : timeToKey.keySet()) {
						if (timeToKey.get(time).renderRect.contains(mouseEvent.getPoint())) {
							foundFrame = showTimeSliderPopup3(mouseEvent, time);
						}
					}
//					for (Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
//						if (timeAndKey.getValue().renderRect.contains(mouseEvent.getPoint())) {
//							foundFrame = showTimeSliderPopup(mouseEvent, timeAndKey, structureChangeListener);
//						}
//					}
					if (!foundFrame && timeChooserRect.contains(mouseEvent.getPoint())) {
						showTimeSliderPopUp2(mouseEvent);
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				mouseDownPointX = mouseEvent.getPoint().getX();
				if (!drawing) {
					return;
				}
				lastMousePoint = mouseEvent.getPoint();
				draggingSlider = sliderContainsPoint(lastMousePoint);
				if (!draggingSlider) {
					mouseNotDraged(mouseEvent);
				}
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (!drawing) {
					return;
				}
				checkMouseOver(e.getPoint());
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (!drawing) {
					return;
				}
				Point mousePoint = e.getPoint();
				if (draggingSlider) {
//					System.out.println("is dragging Slider!");
					dx = mousePoint.getX() - lastMousePoint.getX();
					timeChooserRect.x += (int) dx;
					int maxXPosition = timelinePanel.getWidth() - timeChooserRect.width;
					if (timeChooserRect.x > maxXPosition) {
						timeChooserRect.x = maxXPosition;
					} else if (timeChooserRect.x < 0) {
						timeChooserRect.x = 0;
					}
					int computedTime = computeTimeFromSlider();
					if (computedTime != timeEnvironment.getAnimationTime()) {
//						timeEnvironment.setCurrentTime(computedTime);
						timeEnvironment.setAnimationTime(computedTime);
						notifier.timeChanged(timeEnvironment.getAnimationTime());
					}
					repaint();
				} else if (draggingFrame != null) {
					dx = mousePoint.getX() - lastMousePoint.getX();
					updateDraggedKeyframe(mousePoint);
					repaint();
				}
				lastMousePoint = e.getPoint();
			}
		};
	}

	private void mouseNotDraged(MouseEvent mouseEvent) {
		if (isMouseOnBackward()) {
			stepBackwards();
		} else if (isMouseOnForward()) {
			stepForwards();
		} else {
//			boolean foundMatch = false;
			if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
				for (KeyFrame frame : timeToKey.values()) {
					if (frame.renderRect.contains(lastMousePoint)) {
						draggingFrame = frame;
						draggingFrameStartTime = frame.time;
						mouseDragXOffset = (int) (lastMousePoint.getX() - frame.renderRect.x);
//						foundMatch = true;
						break;
					}
				}
			}
//			if (!foundMatch) {
////				if (isMouseOnPlayButton()) {
//////					pausePlayAnimation();
////				}
//			}
		}
	}

	private boolean isMouseOnBackward() {
		return lastMousePoint.x > timeChooserRect.x
				&& lastMousePoint.x < (timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE)
				&& lastMousePoint.y < (timeChooserRect.y + timeChooserRect.height);
	}

	private boolean isMouseOnForward() {
		return lastMousePoint.x > (timeChooserRect.x + timeChooserRect.width - SLIDER_SIDE_BUTTON_SIZE)
				&& lastMousePoint.x < (timeChooserRect.x + timeChooserRect.width)
				&& lastMousePoint.y < (timeChooserRect.y + timeChooserRect.height);
	}

	private boolean isMouseOnPlayButton() {
		return (lastMousePoint.x < (RMSIcons.PLAY.getIconWidth() / 2))
				&& (lastMousePoint.y > (VERTICAL_SLIDER_HEIGHT + 4))
				&& (lastMousePoint.y < (VERTICAL_SLIDER_HEIGHT + 4 + (RMSIcons.PLAY.getIconHeight() / 2)));
	}

//	private boolean showTimeSliderPopup(MouseEvent mouseEvent, Map.Entry<Integer, KeyFrame> timeAndKey) {
//		popupMenu.removeAll();
//
//		JMenuItem timeIndicator = new JMenuItem("" + timeAndKey.getKey());
//		timeIndicator.setEnabled(false);
//		popupMenu.add(timeIndicator);
//		popupMenu.addSeparator();
//
//		JMenuItem deleteAll = new JMenuItem("Delete All");
//		deleteAll.addActionListener(e -> deleteKeyframes("delete keyframe", timeAndKey.getKey(), timeAndKey.getValue().objects));
//		popupMenu.add(deleteAll);
//		popupMenu.addSeparator();
//
//		JMenuItem cutItem = new JMenuItem("Cut");
//		cutItem.addActionListener(e -> cutItem(timeAndKey));
//		popupMenu.add(cutItem);
//
//		JMenuItem copyItem = new JMenuItem("Copy");
//		copyItem.addActionListener(e -> copyKeyframes(timeAndKey.getKey()));
//		popupMenu.add(copyItem);
//
//		JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
//		copyFrameItem.addActionListener(e -> copyAllKeyframes(timeAndKey.getKey()));
//		popupMenu.add(copyFrameItem);
//
//		JMenuItem pasteItem = new JMenuItem("Paste");
//		pasteItem.addActionListener(e -> pasteToAllSelected(timeAndKey.getKey()));
//		popupMenu.add(pasteItem);
//
//		popupMenu.addSeparator();
//
//		for (IdObject object : timeAndKey.getValue().objects) {
//			for (AnimFlag<?> flag : object.getAnimFlags()) {
//				if (flag.hasEntryAt(timeAndKey.getKey())) {
//					JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
//					popupMenu.add(subMenu);
//
//					JMenuItem deleteSpecificItem = new JMenuItem("Delete");
//					deleteSpecificItem.addActionListener(e -> deleteKeyframe(flag, timeAndKey.getKey()));
//					subMenu.add(deleteSpecificItem);
//					subMenu.addSeparator();
//
//					JMenuItem cutSpecificItem = new JMenuItem("Cut");
//					cutSpecificItem.addActionListener(e -> cutSpecificItem(timeAndKey, object, flag));
//					subMenu.add(cutSpecificItem);
//
//					JMenuItem copySpecificItem = new JMenuItem("Copy");
//					copySpecificItem.addActionListener(e -> copyKeyframes(object, flag, timeAndKey.getKey()));
//					subMenu.add(copySpecificItem);
//
//					JMenuItem pasteSpecificItem = new JMenuItem("Paste");
//					pasteSpecificItem.addActionListener(e -> pasteToSpecificTimeline(timeAndKey, flag));
//					subMenu.add(pasteSpecificItem);
//				}
//			}
//		}
//		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
//		return true;
//	}
	private boolean showTimeSliderPopup3(MouseEvent mouseEvent, Integer time) {
		popupMenu.removeAll();

		JMenuItem timeIndicator = new JMenuItem("" + time);
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();

		JMenuItem deleteAll = new JMenuItem("Delete All");
		deleteAll.addActionListener(e -> keyframeHandler.deleteKeyframes("delete keyframe", time, timeToKey.get(time).objects));
		popupMenu.add(deleteAll);
		popupMenu.addSeparator();

		JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.addActionListener(e -> keyframeHandler.cutItem(time));
		popupMenu.add(cutItem);

		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(e -> keyframeHandler.copyKeyframes(time));
		popupMenu.add(copyItem);

		JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
		copyFrameItem.addActionListener(e -> keyframeHandler.copyAllKeyframes(time));
		popupMenu.add(copyFrameItem);

		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(e -> keyframeHandler.pasteToAllSelected(time));
		popupMenu.add(pasteItem);

		popupMenu.addSeparator();

		for (IdObject object : timeToKey.get(time).objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				if (flag.hasEntryAt(time)) {
					JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
					popupMenu.add(subMenu);

					JMenuItem deleteSpecificItem = new JMenuItem("Delete");
					deleteSpecificItem.addActionListener(e -> keyframeHandler.deleteKeyframe(flag, time));
					subMenu.add(deleteSpecificItem);
					subMenu.addSeparator();

					JMenuItem cutSpecificItem = new JMenuItem("Cut");
					cutSpecificItem.addActionListener(e -> keyframeHandler.cutSpecificItem(time, object, flag));
					subMenu.add(cutSpecificItem);

					JMenuItem copySpecificItem = new JMenuItem("Copy");
					copySpecificItem.addActionListener(e -> keyframeHandler.copyKeyframes(object, flag, time));
					subMenu.add(copySpecificItem);

					JMenuItem pasteSpecificItem = new JMenuItem("Paste");
					pasteSpecificItem.addActionListener(e -> keyframeHandler.pasteToSpecificTimeline(time, flag));
					subMenu.add(pasteSpecificItem);
				}
			}
		}
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
		return true;
	}

	private void showTimeSliderPopUp2(MouseEvent mouseEvent) {
		popupMenu.removeAll();

		JMenuItem timeIndicator = new JMenuItem("" + timeEnvironment.getAnimationTime());
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();

		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(e -> keyframeHandler.copyKeyframes(timeEnvironment.getAnimationTime()));
		popupMenu.add(copyItem);

		JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
		copyFrameItem.addActionListener(e -> keyframeHandler.copyAllKeyframes(timeEnvironment.getAnimationTime()));
		popupMenu.add(copyFrameItem);

		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(e -> keyframeHandler.pasteToAllSelected(timeEnvironment.getAnimationTime()));
		popupMenu.add(pasteItem);

		popupMenu.addSeparator();
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
	}

	private void liveAnimationTimerListener() {
		if (!drawing || timeEnvironment == null || !timeEnvironment.isLive()) {
			return;
		}
		timeEnvironment.updateAnimationTime();
		notifier.timeChanged(timeEnvironment.getAnimationTime());
		timeChooserRect.x = computeSliderXFromTime();
		repaint();

//		if (timeEnvironment.getAnimationTime() == timeEnvironment.getEnd()) {
//			timeEnvironment.setAnimationTime(timeEnvironment.getStart());
//			timeChooserRect.x = computeSliderXFromTime();
//			notifier.timeChanged(timeEnvironment.getAnimationTime());
//			repaint();
//		} else {
//			jumpFrames(16);
//		}
	}


	public void setTickStep(int tickStep) {
		this.tickStep = tickStep;
	}

	public void setModelHandler(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		keyframeHandler.setModelHandler(modelHandler);
		if(modelHandler != null) {
			this.undoManager = modelHandler.getUndoManager();
			timeEnvironment = modelHandler.getEditTimeEnv();
			timeEnvironment.addChangeListener(this);
		} else {
			this.undoManager = null;
		}
	}


	public void checkMouseOver(Point mousePt) {
		KeyFrame newMouseOver = getKeyFrame(mousePt);
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

	private KeyFrame getKeyFrame(Point mousePt) {
		KeyFrame newMouseOver = null;
		for (KeyFrame key : timeToKey.values()) {
			if (key.renderRect.contains(mousePt)) {
				newMouseOver = key;
			}
		}
		return newMouseOver;
	}

//	public void setNodeSelectionManager(final SelectionManager<IdObject> nodeSelectionManager) {
////		if (this.nodeSelectionManager != nodeSelectionManager) {
////			if (this.nodeSelectionManager != null) {
////				this.nodeSelectionManager.removeSelectionListener(this);
////			}
////			this.nodeSelectionManager = nodeSelectionManager;
////			if (this.nodeSelectionManager != null) {
////				this.nodeSelectionManager.addSelectionListener(this);
////			}
////		}
//	}


	public void jumpToPreviousTime() {
		Integer newTime = timeToKey.lowerKey(timeEnvironment.getAnimationTime());
		setCurrentTime(newTime == null ? timeEnvironment.getStart() : newTime);
	}

	public void jumpToNextTime() {
		Integer newTime = timeToKey.higherKey(timeEnvironment.getAnimationTime());
		setCurrentTime(newTime == null ? timeEnvironment.getStart() : newTime);
	}

	public void jumpFrames(int deltaFrames) {
		int newTime = timeEnvironment.getAnimationTime() + deltaFrames;
		if (newTime > timeEnvironment.getEnd()) {
			newTime = timeEnvironment.getEnd();
		} else if (newTime < timeEnvironment.getStart()) {
			newTime = timeEnvironment.getStart();
		}
		setCurrentTime(newTime);
	}
	private void stepBackwards() {
		if (timeEnvironment.getAnimationTime() > timeEnvironment.getStart()) {
			timeStep(-1);
		}
		repaint();
	}

	private void stepForwards() {
		if (timeEnvironment.getAnimationTime() < timeEnvironment.getEnd()) {
			timeStep(1);
		}
		repaint();
	}


	public void setCurrentTime(int newTime) {
//		System.out.println("setCurrentTime");
		timeEnvironment.setAnimationTime(newTime);
//		currentTime = newTime;
		notifier.timeChanged(timeEnvironment.getAnimationTime());
		int maxXPosition = timelinePanel.getWidth() - timeChooserRect.width;
		timeChooserRect.x = computeSliderXFromTime();
		if (timeChooserRect.x > maxXPosition) {
			timeChooserRect.x = maxXPosition;
		} else if (timeChooserRect.x < 0) {
			timeChooserRect.x = 0;
		}
		repaint();
	}
	private void timeStep(int step) {
		timeEnvironment.stepAnimationTime(step);
//		currentTime += step;
		int pixelDelta = timeChooserRect.x;
		timeChooserRect.x = computeSliderXFromTime();
		pixelDelta = timeChooserRect.x - pixelDelta;
		if (robot != null) {
			robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + pixelDelta, MouseInfo.getPointerInfo().getLocation().y);
		}
		notifier.timeChanged(timeEnvironment.getAnimationTime());
	}

	private int computeTimeFromSlider() {
		int pixelCenter = timeChooserRect.x;
		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		double locationRatio = pixelCenter / (double) widthMinusOffsets;
		return (int) (locationRatio * (timeEnvironment.getEnd() - timeEnvironment.getStart())) + timeEnvironment.getStart();
	}

	private int computeTimeFromX(final int x) {
		int pixelCenter = x - (SIDE_OFFSETS);
		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		double locationRatio = pixelCenter / (double) widthMinusOffsets;
//		System.out.println("time from X " + x + ": " + ((locationRatio * (end - start)) + start));
		return (int) (locationRatio * (timeEnvironment.getEnd() - timeEnvironment.getStart())) + timeEnvironment.getStart();
	}

	private int computeSliderXFromTime() {
		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		double timeRatio = (timeEnvironment.getAnimationTime() - timeEnvironment.getStart()) / (double) (timeEnvironment.getEnd() - timeEnvironment.getStart());
		return (int) (widthMinusOffsets * timeRatio);
	}

	private int computeXFromTime(int time) {
		int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		double timeRatio = (time - timeEnvironment.getStart()) / (double) (timeEnvironment.getEnd() - timeEnvironment.getStart());
//		System.out.println("new x: " + ((widthMinusOffsets * timeRatio) + (SIDE_OFFSETS)) + " for time " + time);
		return (int) (widthMinusOffsets * timeRatio) + (SIDE_OFFSETS);
	}

	public boolean sliderContainsPoint(Point mousePoint) {
		return (mousePoint.getY() < (timeChooserRect.y + timeChooserRect.height))
				&& (mousePoint.getX() > (timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE))
				&& (mousePoint.getX() < ((timeChooserRect.x + timeChooserRect.width) - SLIDER_SIDE_BUTTON_SIZE));
	}

	public void addListener(Consumer<Integer> listener) {
		notifier.subscribe(listener);
	}


	private void drawTimeTicks(Graphics g, FontMetrics fontMetrics) {
		final int timeSpan = timeEnvironment.getEnd() - timeEnvironment.getStart();
		int numberOfTicks = timeSpan / tickStep;
		int startOffset = tickStep - (timeEnvironment.getStart() % tickStep);

		// draw first time marker
		drawMajorTick(g, fontMetrics, timeEnvironment.getStart());
		// draw even (time%tickStep==0) time markers
		for (int i = 0; i < numberOfTicks; i++) {
			int time = timeEnvironment.getStart() + startOffset + tickStep * i;

			final boolean majorTick = (i % 2) == 0;
			if (majorTick) {
				drawMajorTick(g, fontMetrics, time);
			} else {
				drawMinorTick(g, computeXFromTime(time));
			}
		}
		// draw last time marker
		drawMajorTick(g, fontMetrics, timeEnvironment.getEnd());
	}

	private void drawMinorTick(Graphics g, int xCoordPixels) {
		((Graphics2D) g).setStroke(WIDTH_1_STROKE);
		final int lineEnd = VERTICAL_SLIDER_HEIGHT + (VERTICAL_TICKS_HEIGHT / 2);
		g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
	}

	private void drawMajorTick(Graphics g, FontMetrics fontMetrics, int time) {
		int xCoordPixels = computeXFromTime(time);
		((Graphics2D) g).setStroke(WIDTH_2_STROKE);
		final int lineEnd = VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT;
		g.drawLine(xCoordPixels, 0, xCoordPixels, lineEnd);
		String tickLabel = "" + time;
		g.drawString(tickLabel, xCoordPixels - (fontMetrics.stringWidth(tickLabel) / 2), lineEnd + fontMetrics.getAscent());
	}

	private void drawTimeBar(Graphics g, int width) {
		if (keyframeModeActive) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLUE.darker());
		}
		g.fillRect(0, 0, width, VERTICAL_SLIDER_HEIGHT);
	}

	private void drawTimeSlider(Graphics g, final FontMetrics fontMetrics) {
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.y, timeChooserRect.width - (SLIDER_SIDE_BUTTON_SIZE * 2), timeChooserRect.height, true);
		g.fill3DRect(timeChooserRect.x, timeChooserRect.y, SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.height, true);
		g.fill3DRect((timeChooserRect.x + timeChooserRect.width) - SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.y, SLIDER_SIDE_BUTTON_SIZE, timeChooserRect.height, true);
		g.setColor(getForeground());
//		String timeChooserLabel = currentTime + " / " + end;
		String timeChooserLabel = timeEnvironment.getAnimationTime() + "";
		int y = (timeChooserRect.y + ((timeChooserRect.height + fontMetrics.getAscent()) / 2)) - 1;
		g.drawString(timeChooserLabel, timeChooserRect.x + ((timeChooserRect.width - fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
		timeChooserLabel = "<";
		g.drawString(timeChooserLabel, timeChooserRect.x + ((SLIDER_SIDE_BUTTON_SIZE - fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
		timeChooserLabel = ">";
		g.drawString(timeChooserLabel, (timeChooserRect.x + timeChooserRect.width) - ((SLIDER_SIDE_BUTTON_SIZE + fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
	}

	//	@Override
	public void timeBoundsChanged(int start, int end) {
		liveAnimationTimer.stop();
		playButton.setIcon(RMSIcons.PLAY);
		timeChooserRect.x = computeSliderXFromTime();
		updateKeyframeDisplay();
//		timeLine.repaint();
		repaint();
	}

	public void setKeyframeModeActive(final boolean keyframeModeActive) {
		this.keyframeModeActive = keyframeModeActive;
	}

	private void slideExistingKeyFramesForResize() {
		for (KeyFrame key : timeToKey.values()) {
			key.reposition();
		}
	}

	public void updateDraggedKeyframe(Point mousePoint) {
		draggingFrame.renderRect.x = mousePoint.x - mouseDragXOffset;

		int computedTime = computeTimeFromX(draggingFrame.getXPoint());

		if (computedTime < timeEnvironment.getStart()) {
			computedTime = timeEnvironment.getStart();
			draggingFrame.setFrameX(computedTime);
		} else if (computedTime > timeEnvironment.getEnd()) {
			computedTime = timeEnvironment.getEnd();
			draggingFrame.setFrameX(computedTime);
		}
		if ((computedTime != draggingFrame.time) && !timeToKey.containsKey(computedTime)) {
			timeToKey.remove(draggingFrame.time);
			for (AnimFlag<?> timeline : draggingFrame.timelines) {
				timeline.slideKeyframe(draggingFrame.time, computedTime);
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(timeEnvironment.getAnimationTime());
			}
			draggingFrame.time = computedTime;
			timeToKey.put(draggingFrame.time, draggingFrame);
		}
	}

	private void updateKeyframeDisplay() {
		timeToKey.clear();
//		if (nodeSelectionManager != null) {
		if (true) {
			Iterable<IdObject> selection = getSelectionToUse();
			for (IdObject object : selection) {
				for (AnimFlag<?> flag : object.getAnimFlags()) {
					if (((flag.getGlobalSeqLength() == null) && (timeEnvironment.getGlobalSeq() == null))
							|| ((timeEnvironment.getGlobalSeq() != null) && timeEnvironment.getGlobalSeq().equals(flag.getGlobalSeqLength()))) {
						if (flag.size() > 0) {
							TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap();
							Integer startTime = entryMap.ceilingKey(timeEnvironment.getStart());
							Integer endTime = entryMap.floorKey(timeEnvironment.getEnd());
							if(endTime == null) endTime = startTime;
							for (Integer time = startTime; time != null && time <= endTime; time = entryMap.higherKey(time)) {
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
	}

	public Collection<IdObject> getSelectionToUse() {
		if ((modelHandler == null) || (modelHandler.getModel() == null)) {
			return Collections.emptySet();
		}
//		return allKF.isSelected() ? modelHandler.getModel().getIdObjects() : nodeSelectionManager.getSelection();
		return allKF.isSelected() ? modelHandler.getModel().getIdObjects() : modelHandler.getModelView().getSelectedIdObjects();
	}

	@Override
	public void onSelectionChanged(final AbstractSelectionManager newSelection) {
		updateKeyframeDisplay();
		repaint();
	}

	// called when user is editing the keyframes and they need to be updated
	public void revalidateKeyframeDisplay() {
		updateKeyframeDisplay();
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
				final int width = getWidth();

				drawTimeBar(g, width);

				final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

				final int widthMinusOffsets = width - (SIDE_OFFSETS * 2);
				if (widthMinusOffsets < 0) {
					g.drawString("No pixels", 0, 16);
					return;
				}
				switch (theme) {
					case DARK, HIFI -> g.setColor(Color.WHITE);
					case FOREST_GREEN -> g.setColor(Color.WHITE);
					default -> g.setColor(Color.BLACK);
				}
				// time markers
				drawTimeTicks(g, fontMetrics);

				// keyframes
				drawKeyframeMarkers(g);
				// time label of dragged keyframe
				getDragKeyframeFloatingTime(g);

				// time slider
				drawTimeSlider(g, fontMetrics);
				// glass covering current tick
				drawCurrentKeyframeMarker(g);

//				final Image playImage = liveAnimationTimer.isRunning() ? RMSIcons.PAUSE.getImage() : RMSIcons.PLAY.getImage();
//				g.drawImage(playImage, 0, VERTICAL_SLIDER_HEIGHT + 4, playImage.getWidth(null) / 2, playImage.getWidth(null) / 2, null);
			}

			private void drawCurrentKeyframeMarker(Graphics g) {
				g.setColor(GLASS_TICK_COVER_COLOR);
				final int currentTimePixelX = computeXFromTime(timeEnvironment.getAnimationTime());
				g.fillRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
				g.setColor(GLASS_TICK_COVER_BORDER_COLOR);
				g.drawRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
			}

			private void drawKeyframeMarkers(Graphics g) {
				for (Integer time : timeToKey.keySet()) {
					int currentTimePixelX = computeXFromTime(time);
					boolean mouseOver = timeToKey.get(time) == mouseOverFrame;
					boolean translation = false, rotation = false, scaling = false, other = false;
					for (AnimFlag<?> af : timeToKey.get(time).timelines) {
						boolean afTranslation = af.getName().equals(MdlUtils.TOKEN_TRANSLATION);
						translation = (afTranslation || translation);
						boolean afRotation = af.getName().equals(MdlUtils.TOKEN_ROTATION);
						rotation = (afRotation || rotation);
						boolean afScaling = af.getName().equals(MdlUtils.TOKEN_SCALING);
						scaling = (afScaling || scaling);
						other |= !(afTranslation || afRotation || afScaling);
//						boolean afTranslation = af.getName().equals(MdlUtils.TOKEN_TRANSLATION);
//						translation |= afTranslation;
//						boolean afRotation = af.getName().equals(MdlUtils.TOKEN_ROTATION);
//						rotation |= afRotation;
//						boolean afScaling = af.getName().equals(MdlUtils.TOKEN_SCALING);
//						scaling |= afScaling;
//						other |= !(afTranslation || afRotation || afScaling);
					}
//					if (scaling) {
//						((Graphics2D) g).setPaint(keyframePaintRed);
//					} else if (rotation) {
//						((Graphics2D) g).setPaint(keyframePaint);
//					} else if (translation) {
//						((Graphics2D) g).setPaint(keyframePaintBlue);
//					} else {
//						((Graphics2D) g).setPaint(keyframePaint);
//					}
					((Graphics2D) g).setPaint(getFillPaint(translation, rotation, scaling));
					g.fillRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
//					Color color = Color.GREEN;
//					if (scaling) {
//						color = Color.ORANGE;
//					} else if (rotation) {
//					} else if (translation) {
//						color = Color.BLUE;
//					}
//					g.setColor(mouseOver ? Color.RED : color);
					if(mouseOver){
						g.setColor(Color.RED);
					} else {
						((Graphics2D) g).setPaint(getEdgePaint(translation, rotation, scaling));
					}
					g.drawRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);

				}
			}

			private void getDragKeyframeFloatingTime(Graphics g) {
				if (draggingFrame != null) {
					g.setColor(Color.WHITE);
					int draggingFrameTime = draggingFrame.time;
					if (draggingFrameTime > timeEnvironment.getEnd()) {
						draggingFrameTime = timeEnvironment.getEnd();
					} else if (draggingFrameTime < timeEnvironment.getStart()) {
						draggingFrameTime = timeEnvironment.getStart();
					}
					g.drawString(Integer.toString(draggingFrameTime), draggingFrame.renderRect.x - draggingFrame.renderRect.width, VERTICAL_SLIDER_HEIGHT);
				}
			}
		};
	}

	private GradientPaint getFillPaint(boolean trans, boolean rot, boolean scale) {
		if (trans && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION) {
			return kfPaintTrans;
		} else if (rot && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {
			return kfPaintRot;
		} else if (scale && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.SCALING) {
			return kfPaintScale;
		} else {
			if (trans){
				return kfPaintTrans;
			} else if (rot) {
				return kfPaintRot;
			} else if (scale) {
				return kfPaintScale;
			} else {
				return kfPaintTrans;
			}
		}
	}

	private GradientPaint getEdgePaint(boolean trans, boolean rot, boolean scale) {
		if (trans && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.TRANSLATION) {
			if(rot && scale){
				return kfPaintRotScale;
			}else if (rot){
				return kfPaintRotRot;
			} else if (scale){
				return kfPaintScaleScale;
			} else {
				return kfPaintTransTrans;
			}
		} else if (rot && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.ROTATION) {
			if(trans && scale){
				return kfPaintTransScale;
			}else if (trans){
				return kfPaintTransTrans;
			} else if (scale){
				return kfPaintScaleScale;
			} else {
				return kfPaintRotRot;
			}
		} else if (scale && ProgramGlobals.getEditorActionType() == ModelEditorActionType3.SCALING) {
			if(trans && rot){
				return kfPaintTransRot;
			}else if (trans){
				return kfPaintTransTrans;
			} else if (rot){
				return kfPaintRotRot;
			} else {
				return kfPaintScaleScale;
			}
		} else {
			if (trans) {
				if(rot && scale){
					return kfPaintRotScale;
				}else if (rot){
					return kfPaintRotRot;
				} else if (scale){
					return kfPaintScaleScale;
				} else {
					return kfPaintTransTrans;
				}
			} else if (rot) {
				if (scale){
					return kfPaintScaleScale;
				} else {
					return kfPaintRotRot;
				}
			} else if (scale) {
					return kfPaintScaleScale;
			} else {
				return kfPaintTransTrans;
			}
		}
	}

	private static final class CopiedKeyFrame<T> {
		private final TimelineContainer node;
		private final AnimFlag<T> sourceTimeline;
		private final Entry<T> entry;

		public CopiedKeyFrame(TimelineContainer node, AnimFlag<T> sourceTimeline, Entry<T> entry) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			this.entry = entry;
		}
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

	public final class KeyFrame {
		private int time;
		private final Set<IdObject> objects = new HashSet<>();
		private final List<AnimFlag<?>> timelines = new ArrayList<>();
		private final Rectangle renderRect;
		private int width = 8;

		private KeyFrame(int time) {
			this.time = time;
			final int currentTimePixelX = computeXFromTime(time);
			renderRect = new Rectangle(currentTimePixelX - width / 2, VERTICAL_SLIDER_HEIGHT, width, VERTICAL_TICKS_HEIGHT);
		}

		protected void reposition() {
			renderRect.x = computeXFromTime(time) - 4;
		}

		protected int getXPoint() {
			return renderRect.x + width / 2;
		}

		protected void setFrameX(int time) {
			renderRect.x = computeXFromTime(time) - width / 2;
		}
	}
}
