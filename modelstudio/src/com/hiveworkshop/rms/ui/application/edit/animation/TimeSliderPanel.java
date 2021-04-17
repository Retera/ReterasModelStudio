package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.TimeSliderView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderTimeListener.TimeSliderTimeNotifier;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.SetKeyframeAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.SlideKeyframeAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.ReversedAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
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
import java.util.stream.Collectors;

public class TimeSliderPanel extends JPanel implements TimeBoundChangeListener, SelectionListener {
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
	private final TimeBoundProvider timeBoundProvider;
	private int start, end = 30;
	private int currentTime = 0;
	int tickStep = 25;
	JPanel timelinePanel;
	private double dx = 0;

	private final Rectangle timeChooserRect;

	private Point lastMousePoint;
	private boolean draggingSlider = false;
	private Robot robot;

	private final TimeSliderTimeNotifier notifier;

	private SelectionManager<IdObject> nodeSelectionManager;
	private final GradientPaint keyframePaint;
	private final GradientPaint keyframePaintBlue;
	private final GradientPaint keyframePaintRed;
	private final Map<Integer, KeyFrame> timeToKey = new LinkedHashMap<>();

	private final JPopupMenu popupMenu;
	private KeyFrame mouseOverFrame = null;
	private KeyFrame draggingFrame = null;
	private int draggingFrameStartTime = 0;
	private int mouseDragXOffset = 0;

	private final Cursor slideCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
	private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private UndoActionListener undoManager;

	private final List<CopiedKeyFrame> copiedKeyframes;
	private boolean useAllCopiedKeyframes = false;
	private final ModelStructureChangeListener structureChangeListener;
	private ModelView modelView;
	private final JCheckBox allKF;
	private TimeEnvironmentImpl timeEnvironmentImpl;
	private final Timer liveAnimationTimer;
	private final ProgramPreferences preferences;
	private final GUITheme theme;
	private boolean drawing;
	private double mouseDownPointX = 0;
	final JButton setKeyframe;
	final JButton setTimeBounds;
	JButton playButton;
	private EditableModel model;
	MainPanel mainPanel;

//	TimeLine timeLine;

	//	public TimeSliderPanel(
//			final TimeBoundProvider timeBoundProvider,
//			final ModelStructureChangeListener structureChangeListener,
//			final ProgramPreferences preferences) {
//	public TimeSliderPanel(MainPanel mainPanel) {
	public TimeSliderPanel(MainPanel mainPanel,
	                       final TimeBoundProvider timeBoundProvider,
	                       final ModelStructureChangeListener structureChangeListener,
	                       final ProgramPreferences preferences) {
		this.mainPanel = mainPanel;
		//mainPanel.animatedRenderEnvironment, mainPanel.modelStructureChangeListener, mainPanel.prefs
		this.timeBoundProvider = timeBoundProvider;
		this.structureChangeListener = structureChangeListener;
		this.preferences = preferences;
		theme = preferences.getTheme();
		notifier = new TimeSliderTimeNotifier();
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


		setKeyframe = createSetKeyframeButton(mainPanel);
		buttonPanel.add(setKeyframe, "wrap");
		setTimeBounds = TimeSliderView.createSetTimeBoundsButton(mainPanel);
		buttonPanel.add(setTimeBounds, "wrap");

		buttonPanel.add(allKF, "wrap");
		buttonPanel.setOpaque(true);
//		buttonPanel.setBackground(Color.magenta);
//		add(buttonPanel, "cell 0 0, shrink, gap 0, pad -5");
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

		timeBoundProvider.addChangeListener(this);

		start = timeBoundProvider.getStart();
		end = timeBoundProvider.getEnd();

		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));

		timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);

		popupMenu = new JPopupMenu();

		copiedKeyframes = new ArrayList<>();

		liveAnimationTimer = new Timer(16, e -> liveAnimationTimerListener());
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
	}

	public static JButton createSetKeyframeButton(MainPanel mainPanel) {
		final JButton setKeyframe;
		setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
		setKeyframe.setMargin(new Insets(0, 0, 0, 0));
		setKeyframe.setToolTipText("Create Keyframe");
		setKeyframe.addActionListener(e -> createKeyframe(mainPanel));
		return setKeyframe;
	}

	private static void createKeyframe(MainPanel mainPanel) {
		final ModelPanel mpanel = mainPanel.currentModelPanel();
		if (mpanel != null) {
			mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().createKeyframe(mainPanel.actionType));
		}
		MainPanel.repaintSelfAndChildren(mainPanel);
		mpanel.repaintSelfAndRelatedChildren();
	}

	public void setModel(EditableModel model) {
		this.model = model;
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				slideExistingKeyFramesForResize();
				timeChooserRect.x = computeSliderXFromTime();
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
					final Point mousePoint = mouseEvent.getPoint();
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
					for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
						if (timeAndKey.getValue().renderRect.contains(mouseEvent.getPoint())) {
							foundFrame = showTimeSliderPopup(mouseEvent, timeAndKey, structureChangeListener);
						}
					}
					if (!foundFrame && timeChooserRect.contains(mouseEvent.getPoint())) {
						showTimeSliderPopUp2(mouseEvent);
					}
				}
			}

			@Override
			public void mousePressed(final MouseEvent mouseEvent) {
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
				final Point mousePoint = e.getPoint();
				if (draggingSlider) {
					dx = mousePoint.getX() - lastMousePoint.getX();
					timeChooserRect.x += (int) dx;
					final int maxXPosition = timelinePanel.getWidth() - timeChooserRect.width;
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
				for (final KeyFrame frame : timeToKey.values()) {
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

	private boolean showTimeSliderPopup(MouseEvent mouseEvent, Map.Entry<Integer, KeyFrame> timeAndKey, ModelStructureChangeListener structureChangeListener) {
		popupMenu.removeAll();

		final JMenuItem timeIndicator = new JMenuItem("" + timeAndKey.getKey());
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();

		final JMenuItem deleteAll = new JMenuItem("Delete All");
		deleteAll.addActionListener(e -> deleteKeyframes("delete keyframe", structureChangeListener, timeAndKey.getKey(), timeAndKey.getValue().objects));
		popupMenu.add(deleteAll);
		popupMenu.addSeparator();

		final JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.addActionListener(e -> cutItem(timeAndKey, structureChangeListener));
		popupMenu.add(cutItem);

		final JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(e -> copyKeyframes(structureChangeListener, timeAndKey.getKey()));
		popupMenu.add(copyItem);

		final JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
		copyFrameItem.addActionListener(e -> copyAllKeyframes(timeAndKey.getKey()));
		popupMenu.add(copyFrameItem);

		final JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(e -> pasteToAllSelected(structureChangeListener, timeAndKey.getKey()));
		popupMenu.add(pasteItem);

		popupMenu.addSeparator();

		for (final IdObject object : timeAndKey.getValue().objects) {
			for (final AnimFlag<?> flag : object.getAnimFlags()) {
				final int flooredTimeIndex = flag.floorIndex(timeAndKey.getKey());
				if ((flooredTimeIndex != -1) && (flooredTimeIndex < flag.getTimes().size()) && flag.getTimes().get(flooredTimeIndex).equals(timeAndKey.getKey())) {
					final JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
					popupMenu.add(subMenu);

					final JMenuItem deleteSpecificItem = new JMenuItem("Delete");
					deleteSpecificItem.addActionListener(e -> deleteKeyframe("delete keyframe", structureChangeListener, object, flag, timeAndKey.getKey()));
					subMenu.add(deleteSpecificItem);
					subMenu.addSeparator();

					final JMenuItem cutSpecificItem = new JMenuItem("Cut");
					cutSpecificItem.addActionListener(e -> cutSpecificItem(timeAndKey, structureChangeListener, object, flag));
					subMenu.add(cutSpecificItem);

					final JMenuItem copySpecificItem = new JMenuItem("Copy");
					copySpecificItem.addActionListener(e -> copyKeyframes(structureChangeListener, object, flag, timeAndKey.getKey()));
					subMenu.add(copySpecificItem);

					final JMenuItem pasteSpecificItem = new JMenuItem("Paste");
					pasteSpecificItem.addActionListener(e -> pasteToSpecificTimeline(structureChangeListener, timeAndKey, flag));
					subMenu.add(pasteSpecificItem);
				}
			}
		}
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
		return true;
	}

	private void showTimeSliderPopUp2(MouseEvent mouseEvent) {
		popupMenu.removeAll();

		final JMenuItem timeIndicator = new JMenuItem("" + currentTime);
		timeIndicator.setEnabled(false);
		popupMenu.add(timeIndicator);
		popupMenu.addSeparator();

		final JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(e -> copyKeyframes(structureChangeListener, currentTime));
		popupMenu.add(copyItem);

		final JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
		copyFrameItem.addActionListener(e -> copyAllKeyframes(currentTime));
		popupMenu.add(copyFrameItem);

		final JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(e -> pasteToAllSelected(structureChangeListener, currentTime));
		popupMenu.add(pasteItem);

		popupMenu.addSeparator();
		popupMenu.show(TimeSliderPanel.this, mouseEvent.getX(), mouseEvent.getY());
	}

	private void cutSpecificItem(Map.Entry<Integer, KeyFrame> timeAndKey, ModelStructureChangeListener structureChangeListener, IdObject object, AnimFlag<?> flag) {
		copyKeyframes(structureChangeListener, object, flag, timeAndKey.getKey());
		deleteKeyframe("cut keyframe", structureChangeListener, object, flag, timeAndKey.getKey());
	}

	private void cutItem(Map.Entry<Integer, KeyFrame> timeAndKey, ModelStructureChangeListener structureChangeListener) {
		copyKeyframes(structureChangeListener, timeAndKey.getKey());
		deleteKeyframes("cut keyframe", structureChangeListener, timeAndKey.getKey(), timeAndKey.getValue().objects);
	}

	private void liveAnimationTimerListener() {
		if (!drawing) {
			return;
		}
		if (currentTime == end) {
			currentTime = start;
			timeChooserRect.x = computeSliderXFromTime();
			notifier.timeChanged(currentTime);
			repaint();
		} else {
			jumpFrames(16);
		}
	}

	public void deleteSelectedKeyframes() {
		final KeyFrame keyFrame = timeToKey.get(currentTime);
		if (keyFrame != null) {
			deleteKeyframes("delete keyframe", structureChangeListener, currentTime, keyFrame.objects);
		}
		revalidateKeyframeDisplay();
	}

	private void deleteKeyframes(final String actionName, final ModelStructureChangeListener structureChangeListener, final int trackTime, final Collection<IdObject> objects) {
		final List<UndoAction> actions = new ArrayList<>();
		for (final IdObject object : objects) {
			for (final AnimFlag<?> flag : object.getAnimFlags()) {
				final int flooredTimeIndex = flag.floorIndex(trackTime);

				final ReversedAction deleteFrameAction = getDeleteAction(actionName, structureChangeListener, object, flag, trackTime, flooredTimeIndex);
				if (deleteFrameAction != null) {
					actions.add(deleteFrameAction);
				}
			}
		}
		// TODO build one action for performance, so that the structure change notifier is not called N times, where N is the number of selected timelines
		final CompoundAction action = new CompoundAction(actionName, actions);
		action.redo();
		undoManager.pushAction(action);
	}

	private void deleteKeyframe(final String actionName, final ModelStructureChangeListener structureChangeListener, final IdObject object, final AnimFlag<?> flag, final int trackTime) {
		final int flooredTimeIndex = flag.floorIndex(trackTime);
		final ReversedAction deleteFrameAction = getDeleteAction(actionName, structureChangeListener, object, flag, trackTime, flooredTimeIndex);
		if (deleteFrameAction != null) {
			deleteFrameAction.redo();
			undoManager.pushAction(deleteFrameAction);
		}
	}

	private ReversedAction getDeleteAction(String actionName, ModelStructureChangeListener structureChangeListener, IdObject object, AnimFlag<?> flag, int trackTime, int flooredTimeIndex) {
		final ReversedAction deleteFrameAction;
		if ((flooredTimeIndex != -1) && (flooredTimeIndex < flag.getTimes().size()) && (flag.getTimes().get(flooredTimeIndex).equals(trackTime))) {
			// I'm going to cheat a little bit.
			// When this saves the list of keyframe values to put back if we CTRL+Z
			// to the "undo stack", it will store the memory references directly.
			// This makes the assumption that we can't graphically edit
			// deleted keyframes, and I'm pretty certain that should be true.
			// (Copy&Paste cannot use this optimization, and must create deep copies
			// of the keyframe values)
//			if (flag.tans()) {
//				deleteFrameAction = new ReversedAction(actionName, new AddKeyframeAction(object, flag, trackTime, flag.getValues().get(flooredTimeIndex), flag.getInTans().get(flooredTimeIndex), flag.getOutTans().get(flooredTimeIndex), structureChangeListener));
//			} else {
//				deleteFrameAction = new ReversedAction(actionName, new AddKeyframeAction(object, flag, trackTime, flag.getValues().get(flooredTimeIndex), structureChangeListener));
//			}
			deleteFrameAction = new ReversedAction(actionName, new AddKeyframeAction(object, flag, flag.getEntry(flooredTimeIndex), structureChangeListener));
		} else {
			deleteFrameAction = null;
		}
		return deleteFrameAction;
	}

	public void jumpToPreviousTime() {
		final List<Integer> validTimes = getTimes().stream().filter(t -> t < currentTime).collect(Collectors.toList());
		if (validTimes.isEmpty()) {
			setCurrentTime(start);
		} else {
			setCurrentTime(validTimes.get(validTimes.size() - 1));
		}
	}

	public void jumpToNextTime() {
		final List<Integer> validTimes = getTimes().stream().filter(t -> t > currentTime).collect(Collectors.toList());
		if (validTimes.isEmpty()) {
			setCurrentTime(start);
		} else {
			setCurrentTime(validTimes.get(0));
		}
	}


	private List<Integer> getTimes() {
		final List<Integer> times = new ArrayList<>(timeToKey.keySet());
		Collections.sort(times);
		return times;
	}

	public void setCurrentTime(final int newTime) {
//		System.out.println("setCurrentTime");
		currentTime = newTime;
		notifier.timeChanged(currentTime);
		final int maxXPosition = timelinePanel.getWidth() - timeChooserRect.width;
		timeChooserRect.x = computeSliderXFromTime();
		if (timeChooserRect.x > maxXPosition) {
			timeChooserRect.x = maxXPosition;
		} else if (timeChooserRect.x < 0) {
			timeChooserRect.x = 0;
		}
		repaint();
	}

	public void jumpFrames(final int deltaFrames) {
		int newTime = currentTime + deltaFrames;
		if (newTime > end) {
			newTime = end;
		} else if (newTime < start) {
			newTime = start;
		}
		setCurrentTime(newTime);
		repaint();
	}

	private void copyKeyframes(final ModelStructureChangeListener structureChangeListener, final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		for (final IdObject object : getSelectionToUse()) {
			for (final AnimFlag<?> flag : object.getAnimFlags()) {
				final Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
				if (((flag.getGlobalSeq() == null) && (currentEditorGlobalSeq == null)) || ((currentEditorGlobalSeq != null) && currentEditorGlobalSeq.equals(flag.getGlobalSeq()))) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	private void copyKeyframes(final ModelStructureChangeListener structureChangeListener, final IdObject object, final AnimFlag<?> flag, final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		copuKeyframes(object, flag, trackTime);
	}

	private void copuKeyframes(IdObject object, AnimFlag<?> flag, int trackTime) {
		final int flooredTimeIndex = flag.floorIndex(trackTime);
		if ((flooredTimeIndex != -1) && (flooredTimeIndex < flag.getTimes().size()) && (flag.getTimes().get(flooredTimeIndex).equals(trackTime))) {
			final Object value = flag.getValues().get(flooredTimeIndex);
			copiedKeyframes.add(new CopiedKeyFrame(object, flag, new AnimFlag.Entry(flag.getEntry(flooredTimeIndex))));
//			if (flag.tans()) {
//				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), AnimFlag.cloneValue(flag.getInTans().get(flooredTimeIndex)), AnimFlag.cloneValue(flag.getOutTans().get(flooredTimeIndex))));
//			} else {
//				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
//			}
		} else {
			final Object value = flag.interpolateAt(timeEnvironmentImpl);
			if (flag.tans()) {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), AnimFlag.cloneValue(value), AnimFlag.cloneValue(value)));
			} else {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
			}
		}
	}

	private void copyAllKeyframes(final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = true;
		for (final IdObject object : modelView.getModel().getIdObjects()) {
			for (final AnimFlag<?> flag : object.getAnimFlags()) {
				final Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
				if (((flag.getGlobalSeq() == null) && (currentEditorGlobalSeq == null)) || ((currentEditorGlobalSeq != null) && currentEditorGlobalSeq.equals(flag.getGlobalSeq()))) {
					copuKeyframes(object, flag, trackTime);
				}
			}
		}
	}

	public void setTickStep(int tickStep) {
		this.tickStep = tickStep;
	}

	public void setUndoManager(final UndoActionListener undoManager, final TimeEnvironmentImpl timeEnvironmentImpl) {
		this.undoManager = undoManager;
		this.timeEnvironmentImpl = timeEnvironmentImpl;
	}

	public void setModelView(final ModelView modelView) {
		this.modelView = modelView;
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
			timeStep(-1);
		}
		repaint();
	}

	private void stepForwards() {
		if (currentTime < end) {
			timeStep(1);
		}
		repaint();
	}

	private void timeStep(int step) {
		currentTime += step;
		int pixelDelta = timeChooserRect.x;
		timeChooserRect.x = computeSliderXFromTime();
		pixelDelta = timeChooserRect.x - pixelDelta;
		if (robot != null) {
			robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + pixelDelta, MouseInfo.getPointerInfo().getLocation().y);
		}
		notifier.timeChanged(currentTime);
	}

	private int computeTimeFromSlider() {
		final int pixelCenter = timeChooserRect.x;
		final int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		final double locationRatio = pixelCenter / (double) widthMinusOffsets;
		return (int) (locationRatio * (end - start)) + start;
	}

	private int computeTimeFromX(final int x) {
		final int pixelCenter = x - (SIDE_OFFSETS);
		final int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		final double locationRatio = pixelCenter / (double) widthMinusOffsets;
//		System.out.println("time from X " + x + ": " + ((locationRatio * (end - start)) + start));
		return (int) (locationRatio * (end - start)) + start;
	}

	private int computeSliderXFromTime() {
		final int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		final double timeRatio = (currentTime - start) / (double) (end - start);
		return (int) (widthMinusOffsets * timeRatio);
	}

	private int computeXFromTime(final int time) {
		final int widthMinusOffsets = timelinePanel.getWidth() - (SIDE_OFFSETS * 2);
		final double timeRatio = (time - start) / (double) (end - start);
//		System.out.println("new x: " + ((widthMinusOffsets * timeRatio) + (SIDE_OFFSETS)) + " for time " + time);
		return (int) (widthMinusOffsets * timeRatio) + (SIDE_OFFSETS);
	}

	public boolean sliderContainsPoint(final Point mousePoint) {
		return (mousePoint.getY() < (timeChooserRect.y + timeChooserRect.height))
				&& (mousePoint.getX() > (timeChooserRect.x + SLIDER_SIDE_BUTTON_SIZE))
				&& (mousePoint.getX() < ((timeChooserRect.x + timeChooserRect.width) - SLIDER_SIDE_BUTTON_SIZE));
	}

	public void addListener(final TimeSliderTimeListener listener) {
		notifier.subscribe(listener);
	}

//	@Override
//	protected void paintComponent(final Graphics g) {
//		super.paintComponent(g);
//		if (!drawing) {
//			return;
//		}
//		final int width = getWidth();
//
//		drawTimeBar(g, width);
//
//		final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
//
//		final int widthMinusOffsets = width - (SIDE_OFFSETS * 2);
//		if (widthMinusOffsets < 0) {
//			g.drawString("No pixels", 0, 16);
//			return;
//		}
//		switch (theme) {
//			case DARK, HIFI -> g.setColor(Color.WHITE);
//			case FOREST_GREEN -> g.setColor(Color.WHITE);
//			default -> g.setColor(Color.BLACK);
//		}
//		// time markers
//		drawTimeTicks(g, fontMetrics);
//
//		// keyframes
//		drawKeyframeMarkers(g);
//		// time label of dragged keyframe
//		getDragKeyframeFloatingTime(g);
//
//		// time slider
//		drawTimeSlider(g, fontMetrics);
//		// glass covering current tick
//		drawCurrentKeyframeMarker(g);
//
//		final Image playImage = liveAnimationTimer.isRunning() ? RMSIcons.PAUSE.getImage() : RMSIcons.PLAY.getImage();
//		g.drawImage(playImage, 0, VERTICAL_SLIDER_HEIGHT + 4, playImage.getWidth(null) / 2, playImage.getWidth(null) / 2, null);
//	}

	private void drawCurrentKeyframeMarker(Graphics g) {
		g.setColor(GLASS_TICK_COVER_COLOR);
		final int currentTimePixelX = computeXFromTime(currentTime);
		g.fillRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
		g.setColor(GLASS_TICK_COVER_BORDER_COLOR);
		g.drawRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
	}

	private void drawKeyframeMarkers(Graphics g) {
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			final int currentTimePixelX = computeXFromTime(timeAndKey.getKey());
			final boolean mouseOver = timeAndKey.getValue() == mouseOverFrame;
			boolean translation = false, rotation = false, scaling = false, other = false;
			for (final AnimFlag<?> af : timeAndKey.getValue().timelines) {
				final boolean afTranslation = "Translation".equals(af.getName());
				translation |= afTranslation;
				final boolean afRotation = "Rotation".equals(af.getName());
				rotation |= afRotation;
				final boolean afScaling = "Scaling".equals(af.getName());
				scaling |= afScaling;
				other |= !(afTranslation || afRotation || afScaling);
			}
			if (scaling) {
				((Graphics2D) g).setPaint(keyframePaintRed);
			} else if (rotation) {
				((Graphics2D) g).setPaint(keyframePaint);
			} else if (translation) {
				((Graphics2D) g).setPaint(keyframePaintBlue);
			} else {
				((Graphics2D) g).setPaint(keyframePaint);
			}
			g.fillRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
			Color color = Color.GREEN;
			if (scaling) {
				color = Color.ORANGE;
			} else if (rotation) {
			} else if (translation) {
				color = Color.BLUE;
			}
			g.setColor(mouseOver ? Color.RED : color);
			g.drawRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);

		}
	}

	private void getDragKeyframeFloatingTime(Graphics g) {
		if (draggingFrame != null) {
			g.setColor(Color.WHITE);
			int draggingFrameTime = draggingFrame.time;
			if (draggingFrameTime > end) {
				draggingFrameTime = end;
			} else if (draggingFrameTime < start) {
				draggingFrameTime = start;
			}
			g.drawString(Integer.toString(draggingFrameTime), draggingFrame.renderRect.x - draggingFrame.renderRect.width, VERTICAL_SLIDER_HEIGHT);
		}
	}

	public void updateDraggedKeyframe(final Point mousePoint) {
		draggingFrame.renderRect.x = mousePoint.x - mouseDragXOffset;

		int computedTime = computeTimeFromX(draggingFrame.getXPoint());

		if (computedTime < start) {
			computedTime = start;
			draggingFrame.setFrameX(computedTime);
		} else if (computedTime > end) {
			computedTime = end;
			draggingFrame.setFrameX(computedTime);
		}
		if ((computedTime != draggingFrame.time) && !timeToKey.containsKey(computedTime)) {
			timeToKey.remove(draggingFrame.time);
			for (final AnimFlag<?> timeline : draggingFrame.timelines) {
				timeline.slideKeyframe(draggingFrame.time, computedTime);
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(currentTime);
			}
			draggingFrame.time = computedTime;
			timeToKey.put(draggingFrame.time, draggingFrame);
		}
	}


	private void drawTimeTicks(Graphics g, FontMetrics fontMetrics) {
		final int timeSpan = end - start;
		int numberOfTicks = timeSpan / tickStep;
		int startOffset = tickStep - (start % tickStep);

		// draw first time marker
		drawMajorTick(g, fontMetrics, start);
		// draw even (time%tickStep==0) time markers
		for (int i = 0; i < numberOfTicks; i++) {
			int time = start + startOffset + tickStep * i;

			final boolean majorTick = (i % 2) == 0;
			if (majorTick) {
				drawMajorTick(g, fontMetrics, time);
			} else {
				drawMinorTick(g, computeXFromTime(time));
			}
		}
		// draw last time marker
		drawMajorTick(g, fontMetrics, end);
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
		String timeChooserLabel = currentTime + "";
		int y = (timeChooserRect.y + ((timeChooserRect.height + fontMetrics.getAscent()) / 2)) - 1;
		g.drawString(timeChooserLabel, timeChooserRect.x + ((timeChooserRect.width - fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
		timeChooserLabel = "<";
		g.drawString(timeChooserLabel, timeChooserRect.x + ((SLIDER_SIDE_BUTTON_SIZE - fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
		timeChooserLabel = ">";
		g.drawString(timeChooserLabel, (timeChooserRect.x + timeChooserRect.width) - ((SLIDER_SIDE_BUTTON_SIZE + fontMetrics.stringWidth(timeChooserLabel)) / 2), y);
	}

	@Override
	public void timeBoundsChanged(final int start, final int end) {
		liveAnimationTimer.stop();
		playButton.setIcon(RMSIcons.PLAY);
		this.start = start;
		this.end = end;
		currentTime = start;
		timeChooserRect.x = computeSliderXFromTime();
		updateKeyframeDisplay();
//		timeLine.repaint();
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
			final Iterable<IdObject> selection = getSelectionToUse();
			for (final IdObject object : selection) {
				for (final AnimFlag<?> flag : object.getAnimFlags()) {
					if (((flag.getGlobalSeq() == null) && (timeEnvironmentImpl.getGlobalSeq() == null)) || ((timeEnvironmentImpl.getGlobalSeq() != null) && timeEnvironmentImpl.getGlobalSeq().equals(flag.getGlobalSeq()))) {
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
	}

	public Collection<IdObject> getSelectionToUse() {
		if ((modelView == null) || (modelView.getModel() == null)) {
			return Collections.emptySet();
		}
		return allKF.isSelected() ? modelView.getModel().getIdObjects()
				: nodeSelectionManager.getSelection();
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

	private void pasteToAllSelected(final ModelStructureChangeListener structureChangeListener, final int trackTime) {
		final List<UndoAction> actions = new ArrayList<>();
		final int mouseClickAnimationTime = trackTime;// computeTimeFromX(e.getX());
		for (final CopiedKeyFrame frame : copiedKeyframes) {
			if (getSelectionToUse().contains(frame.node) || useAllCopiedKeyframes) {
				UndoAction action = getUndoAction(structureChangeListener, mouseClickAnimationTime, frame);
				actions.add(action);
			}
		}
		undoManager.pushAction(new CompoundAction("paste keyframe", actions));
		revalidateKeyframeDisplay();
	}

	private void pasteToSpecificTimeline(final ModelStructureChangeListener structureChangeListener,
	                                     final Map.Entry<Integer, KeyFrame> timeAndKey, final AnimFlag<?> flag) {
		boolean foundCopiedMatch = false;
		final int mouseClickAnimationTime = timeAndKey.getKey();// computeTimeFromX(e.getX());
		for (final CopiedKeyFrame frame : copiedKeyframes) {
			if (frame.sourceTimeline == flag) {
				// only paste to selected nodes
				UndoAction action = getUndoAction(structureChangeListener, mouseClickAnimationTime, frame);
				undoManager.pushAction(action);
				foundCopiedMatch = true;
				break;
			}
		}
		if (!foundCopiedMatch) {
			JOptionPane.showMessageDialog(TimeSliderPanel.this,
					"Tell Retera to code in the ability to paste cross-node data!");
		}
		revalidateKeyframeDisplay();
	}

	private UndoAction getUndoAction(ModelStructureChangeListener structureChangeListener, int mouseClickAnimationTime, CopiedKeyFrame frame) {
		// only paste to selected nodes
		UndoAction action;
		AnimFlag<?> sourceTimeline = frame.sourceTimeline;
		final int flooredTimeIndex = sourceTimeline.floorIndex(mouseClickAnimationTime);
		AnimFlag.Entry newEntry = new AnimFlag.Entry(frame.entry);
//		final Object newValue = AnimFlag.cloneValue(frame.value);
		// tans might be null
//		final Object newInTan = AnimFlag.cloneValue(frame.inTan);
//		final Object newOutTan = AnimFlag.cloneValue(frame.outTan);
		if ((flooredTimeIndex != -1) && (flooredTimeIndex < sourceTimeline.getTimes().size()) && (sourceTimeline.getTimes().get(flooredTimeIndex).equals(mouseClickAnimationTime))) {
//			if (sourceTimeline.tans()) {
//				final Object oldValue = sourceTimeline.valueAt(mouseClickAnimationTime);
//				final Object oldInTan = sourceTimeline.valueAt(mouseClickAnimationTime);
//				final Object oldOutTan = sourceTimeline.valueAt(mouseClickAnimationTime);
//				sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
//				action = new SetKeyframeAction(frame.node, sourceTimeline, mouseClickAnimationTime, newValue, newInTan, newOutTan, oldValue, oldInTan, oldOutTan, () -> {
//					// TODO this is a hack to refresh screen while dragging
//					notifier.timeChanged(currentTime);
//				});
//			} else {
//				final Object oldValue = sourceTimeline.valueAt(mouseClickAnimationTime);
//				sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue);
//				action = new SetKeyframeAction(frame.node, sourceTimeline, mouseClickAnimationTime, newValue, oldValue, () -> {
//					// TODO this is a hack to refresh screen while dragging
//					notifier.timeChanged(currentTime);
//				});
//			}
			newEntry.time = mouseClickAnimationTime;
			sourceTimeline.setEntry(mouseClickAnimationTime, newEntry);
			action = new SetKeyframeAction(frame.node, sourceTimeline, newEntry, () -> {
				// TODO this is a hack to refresh screen while dragging
				notifier.timeChanged(currentTime);
			});
		} else {
//			if (sourceTimeline.tans()) {
//				sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
//				action = new AddKeyframeAction(frame.node, sourceTimeline, mouseClickAnimationTime, newValue, newInTan, newOutTan, structureChangeListener);
//			} else {
//				sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue);
//				action = new AddKeyframeAction(frame.node, sourceTimeline, mouseClickAnimationTime, newValue, structureChangeListener);
//			}
			newEntry.time = mouseClickAnimationTime;
			sourceTimeline.addKeyframe(newEntry);
			action = new AddKeyframeAction(frame.node, sourceTimeline, newEntry, structureChangeListener);
		}
		return action;
	}

	private JPanel getTimelinePanel() {
		return new JPanel(new MigLayout("fill")) {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if (!drawing) {
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
				final int currentTimePixelX = computeXFromTime(currentTime);
				g.fillRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
				g.setColor(GLASS_TICK_COVER_BORDER_COLOR);
				g.drawRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
			}

			private void drawKeyframeMarkers(Graphics g) {
				for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
					final int currentTimePixelX = computeXFromTime(timeAndKey.getKey());
					final boolean mouseOver = timeAndKey.getValue() == mouseOverFrame;
					boolean translation = false, rotation = false, scaling = false, other = false;
					for (final AnimFlag<?> af : timeAndKey.getValue().timelines) {
						final boolean afTranslation = "Translation".equals(af.getName());
						translation |= afTranslation;
						final boolean afRotation = "Rotation".equals(af.getName());
						rotation |= afRotation;
						final boolean afScaling = "Scaling".equals(af.getName());
						scaling |= afScaling;
						other |= !(afTranslation || afRotation || afScaling);
					}
					if (scaling) {
						((Graphics2D) g).setPaint(keyframePaintRed);
					} else if (rotation) {
						((Graphics2D) g).setPaint(keyframePaint);
					} else if (translation) {
						((Graphics2D) g).setPaint(keyframePaintBlue);
					} else {
						((Graphics2D) g).setPaint(keyframePaint);
					}
					g.fillRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);
					Color color = Color.GREEN;
					if (scaling) {
						color = Color.ORANGE;
					} else if (rotation) {
					} else if (translation) {
						color = Color.BLUE;
					}
					g.setColor(mouseOver ? Color.RED : color);
					g.drawRoundRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT, 2, 2);

				}
			}

			private void getDragKeyframeFloatingTime(Graphics g) {
				if (draggingFrame != null) {
					g.setColor(Color.WHITE);
					int draggingFrameTime = draggingFrame.time;
					if (draggingFrameTime > end) {
						draggingFrameTime = end;
					} else if (draggingFrameTime < start) {
						draggingFrameTime = start;
					}
					g.drawString(Integer.toString(draggingFrameTime), draggingFrame.renderRect.x - draggingFrame.renderRect.width, VERTICAL_SLIDER_HEIGHT);
				}
			}
		};
	}

	private static final class CopiedKeyFrame {
		private final TimelineContainer node;
		private final AnimFlag<?> sourceTimeline;
		private final Object value;
		private final Object inTan;
		private final Object outTan;
		private final AnimFlag.Entry<?> entry;

		public CopiedKeyFrame(final TimelineContainer node, final AnimFlag<?> sourceTimeline, final Object value,
		                      final Object inTan, final Object outTan) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			this.value = value;
			this.inTan = inTan;
			this.outTan = outTan;
			entry = new AnimFlag.Entry(0, value, inTan, outTan);
		}

		public CopiedKeyFrame(TimelineContainer node, AnimFlag<?> sourceTimeline, AnimFlag.Entry<?> entry) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			this.entry = entry;
			this.value = entry.value;
			this.inTan = entry.inTan;
			this.outTan = entry.outTan;
		}
	}

	// to be called externally
	public void copy() {
		copyKeyframes(structureChangeListener, currentTime);
	}

	public void cut() {
		copyKeyframes(structureChangeListener, currentTime);
		final KeyFrame keyFrame = timeToKey.get(currentTime);
		if (keyFrame != null) {
			deleteKeyframes("cut keyframe", structureChangeListener, currentTime, keyFrame.objects);
		}
		revalidateKeyframeDisplay();
	}

	public void paste() {
		pasteToAllSelected(structureChangeListener, currentTime);
	}

	public void play() {
		if (liveAnimationTimer.isRunning()) {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
		} else {
			liveAnimationTimer.start();
			playButton.setIcon(RMSIcons.PAUSE);
		}
		repaint();
	}

	private void pausePlayAnimation() {
		if (liveAnimationTimer.isRunning()) {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
		} else {
			liveAnimationTimer.start();
			playButton.setIcon(RMSIcons.PAUSE);
		}
		repaint();
	}

	public void setDrawing(final boolean drawing) {
		this.drawing = drawing;
		for (final Component component : getComponents()) {
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

		private KeyFrame(final int time) {
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
