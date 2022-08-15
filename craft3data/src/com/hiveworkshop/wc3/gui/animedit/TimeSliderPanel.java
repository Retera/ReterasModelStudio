package com.hiveworkshop.wc3.gui.animedit;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.hiveworkshop.wc3.gui.GUITheme;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.TimeSliderPanel.KeyFrame;
import com.hiveworkshop.wc3.gui.animedit.TimeSliderTimeListener.TimeSliderTimeNotifier;
import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SetKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SlideKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.ReversedAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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

	public TimeSliderPanel(final TimeBoundProvider timeBoundProvider,
			final ModelStructureChangeListener structureChangeListener, final ProgramPreferences preferences) {
		this.timeBoundProvider = timeBoundProvider;
		this.structureChangeListener = structureChangeListener;
		this.preferences = preferences;
		theme = preferences.getTheme();
		this.notifier = new TimeSliderTimeNotifier();
		add(Box.createVerticalStrut(VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, VERTICAL_SLIDER_HEIGHT + VERTICAL_TICKS_HEIGHT + 9999));
		timeBoundProvider.addChangeListener(this);
		start = timeBoundProvider.getStart();
		end = timeBoundProvider.getEnd();
		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));
		timeChooserRect = new Rectangle(0, 0, SLIDING_TIME_CHOOSER_WIDTH, VERTICAL_SLIDER_HEIGHT);
		this.popupMenu = new JPopupMenu();
		this.copiedKeyframes = new ArrayList<>();

		liveAnimationTimer = new Timer(16, new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
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
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (!drawing) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e)) {
					final Point mousePoint = e.getPoint();
					if (draggingSlider) {
						timeChooserRect.x = computeSliderXFromTime();
						draggingSlider = false;
					} else if (draggingFrame != null) {
						updateDraggedKeyframe(mousePoint);
						draggingFrame.renderRect.x = computeXFromTime(draggingFrame.time);
						if (undoManager != null) {
							if (draggingFrameStartTime != draggingFrame.time) {
								undoManager.pushAction(new SlideKeyframeAction(draggingFrameStartTime,
										draggingFrame.time, draggingFrame.timelines, new Runnable() {
											@Override
											public void run() {
												revalidateKeyframeDisplay();
											}
										}));
							}
						}
						draggingFrame = null;
						repaint();
					}
					checkMouseOver(mousePoint);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					boolean foundFrame = false;
					for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
						if (timeAndKey.getValue().renderRect.contains(e.getPoint())) {
							popupMenu.removeAll();
							final JMenuItem timeIndicator = new JMenuItem("" + timeAndKey.getKey());
							timeIndicator.setEnabled(false);
							popupMenu.add(timeIndicator);
							popupMenu.addSeparator();
							final JMenuItem deleteAll = new JMenuItem("Delete All");
							deleteAll.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									deleteKeyframes("delete keyframe", structureChangeListener, timeAndKey.getKey(),
											timeAndKey.getValue().objects);
								}
							});
							popupMenu.add(deleteAll);
							popupMenu.addSeparator();
							final JMenuItem cutItem = new JMenuItem("Cut");
							cutItem.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(final ActionEvent e) {
									copyKeyframes(structureChangeListener, timeAndKey.getKey());
									deleteKeyframes("cut keyframe", structureChangeListener, timeAndKey.getKey(),
											timeAndKey.getValue().objects);
								}
							});
							popupMenu.add(cutItem);
							final JMenuItem copyItem = new JMenuItem("Copy");
							copyItem.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									copyKeyframes(structureChangeListener, timeAndKey.getKey());
								}
							});
							popupMenu.add(copyItem);
							final JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
							copyFrameItem.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent e) {
									copyAllKeyframes(timeAndKey.getKey());
								}
							});
							popupMenu.add(copyFrameItem);
							final JMenuItem pasteItem = new JMenuItem("Paste");
							pasteItem.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(final ActionEvent ae) {
									pasteToAllSelected(structureChangeListener, timeAndKey.getKey());
								}
							});
							popupMenu.add(pasteItem);
							popupMenu.addSeparator();
							for (final IdObject object : timeAndKey.getValue().objects) {
								for (final AnimFlag flag : object.getAnimFlags()) {
									final int flooredTimeIndex = flag.floorIndex(timeAndKey.getKey());
									if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
											&& flag.getTimes().get(flooredTimeIndex).equals(timeAndKey.getKey())) {
										final JMenu subMenu = new JMenu(object.getName() + ": " + flag.getName());
										popupMenu.add(subMenu);
										final JMenuItem deleteSpecificItem = new JMenuItem("Delete");
										deleteSpecificItem.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(final ActionEvent e) {
												deleteKeyframe("delete keyframe", structureChangeListener, object, flag,
														timeAndKey.getKey());
											}
										});
										subMenu.add(deleteSpecificItem);
										subMenu.addSeparator();
										final JMenuItem cutSpecificItem = new JMenuItem("Cut");
										cutSpecificItem.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(final ActionEvent e) {
												copyKeyframes(structureChangeListener, object, flag,
														timeAndKey.getKey());
												deleteKeyframe("cut keyframe", structureChangeListener, object, flag,
														timeAndKey.getKey());
											}
										});
										subMenu.add(cutSpecificItem);
										final JMenuItem copySpecificItem = new JMenuItem("Copy");
										copySpecificItem.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(final ActionEvent e) {
												copyKeyframes(structureChangeListener, object, flag,
														timeAndKey.getKey());
											}
										});
										subMenu.add(copySpecificItem);
										final JMenuItem pasteSpecificItem = new JMenuItem("Paste");
										pasteSpecificItem.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(final ActionEvent e) {
												pasteToSpecificTimeline(structureChangeListener, timeAndKey, flag);
											}
										});
										subMenu.add(pasteSpecificItem);
									}
								}
							}
							popupMenu.show(TimeSliderPanel.this, e.getX(), e.getY());
							foundFrame = true;
						}
					}
					if (!foundFrame && timeChooserRect.contains(e.getPoint())) {
						popupMenu.removeAll();
						final JMenuItem timeIndicator = new JMenuItem("" + currentTime);
						timeIndicator.setEnabled(false);
						popupMenu.add(timeIndicator);
						popupMenu.addSeparator();
						final JMenuItem copyItem = new JMenuItem("Copy");
						copyItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								copyKeyframes(structureChangeListener, currentTime);
							}
						});
						popupMenu.add(copyItem);
						final JMenuItem copyFrameItem = new JMenuItem("Copy Frame (whole model)");
						copyFrameItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								copyAllKeyframes(currentTime);
							}
						});
						popupMenu.add(copyFrameItem);
						final JMenuItem pasteItem = new JMenuItem("Paste");
						pasteItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent ae) {
								pasteToAllSelected(structureChangeListener, currentTime);
							}
						});
						popupMenu.add(pasteItem);
						popupMenu.addSeparator();
						popupMenu.show(TimeSliderPanel.this, e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (!drawing) {
					return;
				}
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
						boolean foundMatch = false;
						if (SwingUtilities.isLeftMouseButton(e)) {
							for (final KeyFrame frame : timeToKey.values()) {
								if (frame.renderRect.contains(lastMousePoint)) {
									draggingFrame = frame;
									draggingFrameStartTime = frame.time;
									mouseDragXOffset = (int) (lastMousePoint.getX() - frame.renderRect.x);
									foundMatch = true;
									break;
								}
							}
						}
						if (!foundMatch) {
							if (lastMousePoint.x < RMSIcons.PLAY.getIconWidth() / 2
									&& lastMousePoint.y > VERTICAL_SLIDER_HEIGHT + 4
									&& lastMousePoint.y < VERTICAL_SLIDER_HEIGHT + 4
											+ RMSIcons.PLAY.getIconHeight() / 2) {
								if (liveAnimationTimer.isRunning()) {
									liveAnimationTimer.stop();
								} else {
									liveAnimationTimer.start();
								}
								repaint();
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
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

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
		keyframePaintBlue = new GradientPaint(new Point(0, 10), new Color(200, 200, 255), new Point(0, getHeight()),
				new Color(100, 100, 255), true);
		keyframePaintRed = new GradientPaint(new Point(0, 10), new Color(255, 200, 200), new Point(0, getHeight()),
				new Color(255, 100, 100), true);

		allKF = new JCheckBox("All KF");
		allKF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				revalidateKeyframeDisplay();
			}
		});
		add(allKF);
		final GroupLayout layout = new GroupLayout(this);
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(VERTICAL_SLIDER_HEIGHT).addGap(4)
				.addGroup(layout.createParallelGroup().addComponent(allKF)));
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(24)
				.addGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addComponent(allKF))));
		setLayout(layout);

		final ActionMap map = getActionMap();
		map.put("Delete", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				deleteSelectedKeyframes();
			}
		});
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
	}

	public void deleteSelectedKeyframes() {
		final KeyFrame keyFrame = timeToKey.get(currentTime);
		if (keyFrame != null) {
			deleteKeyframes("delete keyframe", structureChangeListener, currentTime, keyFrame.objects);
		}
		revalidateKeyframeDisplay();
	}

	private void deleteKeyframes(final String actionName, final ModelStructureChangeListener structureChangeListener,
			final int trackTime, final Collection<IdObject> objects) {
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		for (final IdObject object : objects) {
			for (final AnimFlag flag : object.getAnimFlags()) {
				final int flooredTimeIndex = flag.floorIndex(trackTime);
				if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
						&& flag.getTimes().get(flooredTimeIndex) == trackTime) {
					final ReversedAction deleteFrameAction;
					// I'm going to cheat a little bit.
					// When this saves in the "undo stack" the list of keyframe values
					// to put back if we CTRL+Z, it will store the memory references
					// directly. This makes the assumption that we can't graphically edit
					// deleted keyframes, and I'm pretty certain that should be true.
					// (Copy&Paste cannot use this optimization, and must create deep copies
					// of the keyframe values)
					if (flag.tans()) {
						deleteFrameAction = new ReversedAction(actionName,
								new AddKeyframeAction(object, flag, trackTime, flag.getValues().get(flooredTimeIndex),
										flag.getInTans().get(flooredTimeIndex), flag.getOutTans().get(flooredTimeIndex),
										structureChangeListener));
					} else {
						deleteFrameAction = new ReversedAction(actionName, new AddKeyframeAction(object, flag,
								trackTime, flag.getValues().get(flooredTimeIndex), structureChangeListener));
					}
					actions.add(deleteFrameAction);
				}
			}
		}
		// TODO build one action for performance, so that the structure change notifier
		// is not called N times, where N
		// is the number of selected timelines
		final CompoundAction action = new CompoundAction(actionName, actions);
		action.redo();
		undoManager.pushAction(action);
	}

	private void deleteKeyframe(final String actionName, final ModelStructureChangeListener structureChangeListener,
			final IdObject object, final AnimFlag flag, final int trackTime) {
		final int flooredTimeIndex = flag.floorIndex(trackTime);
		if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
				&& flag.getTimes().get(flooredTimeIndex) == trackTime) {
			final ReversedAction deleteFrameAction;
			// I'm going to cheat a little bit.
			// When this saves in the "undo stack" the list of keyframe values
			// to put back if we CTRL+Z, it will store the memory references
			// directly. This makes the assumption that we can't graphically edit
			// deleted keyframes, and I'm pretty certain that should be true.
			// (Copy&Paste cannot use this optimization, and must create deep copies
			// of the keyframe values)
			if (flag.tans()) {
				deleteFrameAction = new ReversedAction(actionName,
						new AddKeyframeAction(object, flag, trackTime, flag.getValues().get(flooredTimeIndex),
								flag.getInTans().get(flooredTimeIndex), flag.getOutTans().get(flooredTimeIndex),
								structureChangeListener));
			} else {
				deleteFrameAction = new ReversedAction(actionName, new AddKeyframeAction(object, flag, trackTime,
						flag.getValues().get(flooredTimeIndex), structureChangeListener));
			}
			deleteFrameAction.redo();
			undoManager.pushAction(deleteFrameAction);
		}
	}

	public void jumpLeft() {
		int lastTime = 0;
		final List<Integer> times = new ArrayList<>();
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			final Integer time = timeAndKey.getKey();
			times.add(time);
		}
		Collections.sort(times);
		boolean foundMatch = false;
		for (final Integer time : times) {
			if (time >= currentTime) {
				if (lastTime > end) {
					lastTime = end;
				} else if (lastTime < start) {
					lastTime = start;
				}
				setCurrentTime(lastTime);
				foundMatch = true;
				break;
			}
			lastTime = time;
		}
		if (!foundMatch) {
			if (times.size() > 0) {
				final int jumpTime = Math.max(start, times.get(times.size() - 1));
				setCurrentTime(jumpTime);
			}
		}
	}

	public void setCurrentTime(final int lastTime) {
		currentTime = lastTime;
		notifier.timeChanged(currentTime);
		final int maxXPosition = getWidth() - timeChooserRect.width;
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

	public void jumpRight() {
		final List<Integer> times = new ArrayList<>();
		for (final Map.Entry<Integer, KeyFrame> timeAndKey : timeToKey.entrySet()) {
			final Integer time = timeAndKey.getKey();
			times.add(time);
		}
		boolean foundMatch = false;
		Collections.sort(times);
		for (Integer time : times) {
			if (time > currentTime) {
				if (time > end) {
					time = end;
				} else if (time < start) {
					time = start;
				}
				currentTime = time;
				notifier.timeChanged(currentTime);
				final int maxXPosition = getWidth() - timeChooserRect.width;
				timeChooserRect.x = computeSliderXFromTime();
				if (timeChooserRect.x > maxXPosition) {
					timeChooserRect.x = maxXPosition;
				} else if (timeChooserRect.x < 0) {
					timeChooserRect.x = 0;
				}
				repaint();
				foundMatch = true;
				break;
			}
		}
		if (!foundMatch) {
			if (times.size() > 0) {
				setCurrentTime(end);
			}
		}
	}

	private void copyKeyframes(final ModelStructureChangeListener structureChangeListener, final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		for (final IdObject object : getSelectionToUse()) {
			for (final AnimFlag flag : object.getAnimFlags()) {
				final Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
				if (flag.getGlobalSeq() == null && currentEditorGlobalSeq == null
						|| currentEditorGlobalSeq != null && currentEditorGlobalSeq.equals(flag.getGlobalSeq())) {
					final int flooredTimeIndex = flag.floorIndex(trackTime);
					if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
							&& flag.getTimes().get(flooredTimeIndex) == trackTime) {
						final Object value = flag.getValues().get(flooredTimeIndex);
						if (flag.tans()) {
							copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
									AnimFlag.cloneValue(flag.getInTans().get(flooredTimeIndex)),
									AnimFlag.cloneValue(flag.getOutTans().get(flooredTimeIndex))));
						} else {
							copiedKeyframes
									.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
						}
					} else {
						final Object value = flag.interpolateAt(timeEnvironmentImpl);
						if (flag.tans()) {
							copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
									AnimFlag.cloneValue(value), AnimFlag.cloneValue(value)));
						} else {
							copiedKeyframes
									.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
						}
					}
				}
			}
		}
	}

	private void copyKeyframes(final ModelStructureChangeListener structureChangeListener, final IdObject object,
			final AnimFlag flag, final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = false;
		final int flooredTimeIndex = flag.floorIndex(trackTime);
		if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
				&& flag.getTimes().get(flooredTimeIndex) == trackTime) {
			final Object value = flag.getValues().get(flooredTimeIndex);
			if (flag.tans()) {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
						AnimFlag.cloneValue(flag.getInTans().get(flooredTimeIndex)),
						AnimFlag.cloneValue(flag.getOutTans().get(flooredTimeIndex))));
			} else {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
			}
		} else {
			final Object value = flag.interpolateAt(timeEnvironmentImpl);
			if (flag.tans()) {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
						AnimFlag.cloneValue(value), AnimFlag.cloneValue(value)));
			} else {
				copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
			}
		}
	}

	private void copyAllKeyframes(final int trackTime) {
		copiedKeyframes.clear();
		useAllCopiedKeyframes = true;
		for (final IdObject object : modelView.getModel().getIdObjects()) {
			for (final AnimFlag flag : object.getAnimFlags()) {
				final Integer currentEditorGlobalSeq = timeEnvironmentImpl.getGlobalSeq();
				if (flag.getGlobalSeq() == null && currentEditorGlobalSeq == null
						|| currentEditorGlobalSeq != null && currentEditorGlobalSeq.equals(flag.getGlobalSeq())) {
					final int flooredTimeIndex = flag.floorIndex(trackTime);
					if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
							&& flag.getTimes().get(flooredTimeIndex) == trackTime) {
						final Object value = flag.getValues().get(flooredTimeIndex);
						if (flag.tans()) {
							copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
									AnimFlag.cloneValue(flag.getInTans().get(flooredTimeIndex)),
									AnimFlag.cloneValue(flag.getOutTans().get(flooredTimeIndex))));
						} else {
							copiedKeyframes
									.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
						}
					} else {
						final Object value = flag.interpolateAt(timeEnvironmentImpl);
						if (flag.tans()) {
							copiedKeyframes.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value),
									AnimFlag.cloneValue(value), AnimFlag.cloneValue(value)));
						} else {
							copiedKeyframes
									.add(new CopiedKeyFrame(object, flag, AnimFlag.cloneValue(value), null, null));
						}
					}
				}
			}
		}
	}

	public void setUndoManager(final UndoActionListener undoManager, final TimeEnvironmentImpl timeEnvironmentImpl) {
		this.undoManager = undoManager;
		this.timeEnvironmentImpl = timeEnvironmentImpl;
	}

	public void setModelView(final ModelView modelView) {
		this.modelView = modelView;
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
		if (!drawing) {
			return;
		}
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
		switch (theme) {
		case DARK:
		case HIFI:
			g.setColor(Color.WHITE);
			break;
		case FOREST_GREEN:
			g.setColor(Color.WHITE);
			break;
		default:
			g.setColor(Color.BLACK);
			break;

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
			boolean translation = false, rotation = false, scaling = false, other = false;
			for (final AnimFlag af : timeAndKey.getValue().timelines) {
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
		if (draggingFrame != null) {
			g.setColor(Color.WHITE);
			int draggingFrameTime = computeTimeFromX(draggingFrame.renderRect.x + draggingFrame.renderRect.width / 2);
			if (draggingFrameTime > end) {
				draggingFrameTime = end;
			} else if (draggingFrameTime < start) {
				draggingFrameTime = start;
			}
			g.drawString(Integer.toString(draggingFrameTime),
					draggingFrame.renderRect.x + draggingFrame.renderRect.width, VERTICAL_SLIDER_HEIGHT);
		}

		// glass covering current tick
		g.setColor(GLASS_TICK_COVER_COLOR);
		final int currentTimePixelX = computeXFromTime(currentTime);
		g.fillRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
		g.setColor(GLASS_TICK_COVER_BORDER_COLOR);
		g.drawRect(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);

		final Image playImage = liveAnimationTimer.isRunning() ? RMSIcons.PAUSE.getImage() : RMSIcons.PLAY.getImage();
		g.drawImage(playImage, 0, VERTICAL_SLIDER_HEIGHT + 4, playImage.getWidth(null) / 2,
				playImage.getWidth(null) / 2, null);
	}

	@Override
	public void timeBoundsChanged(final int start, final int end) {
		liveAnimationTimer.stop();
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
			final Iterable<IdObject> selection = getSelectionToUse();
			for (final IdObject object : selection) {
				for (final AnimFlag flag : object.getAnimFlags()) {
					if (flag.getGlobalSeq() == null && timeEnvironmentImpl.getGlobalSeq() == null
							|| timeEnvironmentImpl.getGlobalSeq() != null
									&& timeEnvironmentImpl.getGlobalSeq().equals(flag.getGlobalSeq())) {
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
		if (modelView == null || modelView.getModel() == null) {
			return Collections.emptySet();
		}
		final Collection<IdObject> selection = allKF.isSelected() ? modelView.getModel().getIdObjects()
				: nodeSelectionManager.getSelection();
		return selection;
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
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		for (final CopiedKeyFrame frame : copiedKeyframes) {
			if (getSelectionToUse().contains(frame.node) || useAllCopiedKeyframes) {
				// only paste to selected nodes
				final int mouseClickAnimationTime = trackTime;// computeTimeFromX(e.getX());
				final int flooredTimeIndex = frame.sourceTimeline.floorIndex(mouseClickAnimationTime);
				final Object newValue = AnimFlag.cloneValue(frame.value);
				// tans might be null
				final Object newInTan = AnimFlag.cloneValue(frame.inTan);
				final Object newOutTan = AnimFlag.cloneValue(frame.outTan);
				if (flooredTimeIndex != -1 && flooredTimeIndex < frame.sourceTimeline.getTimes().size()
						&& frame.sourceTimeline.getTimes().get(flooredTimeIndex) == mouseClickAnimationTime) {
					if (frame.sourceTimeline.tans()) {
						final Object oldValue = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						final Object oldInTan = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						final Object oldOutTan = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						frame.sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
						actions.add(new SetKeyframeAction(frame.node, frame.sourceTimeline, mouseClickAnimationTime,
								newValue, newInTan, newOutTan, oldValue, oldInTan, oldOutTan, new Runnable() {
									@Override
									public void run() {
										// TODO this is a hack to refresh screen while
										// dragging
										notifier.timeChanged(currentTime);
									}
								}));
					} else {
						final Object oldValue = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						frame.sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue);
						actions.add(new SetKeyframeAction(frame.node, frame.sourceTimeline, mouseClickAnimationTime,
								newValue, oldValue, new Runnable() {
									@Override
									public void run() {
										// TODO this is a hack to refresh screen while
										// dragging
										notifier.timeChanged(currentTime);
									}
								}));
					}
				} else {
					if (frame.sourceTimeline.tans()) {
						frame.sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
						actions.add(new AddKeyframeAction(frame.node, frame.sourceTimeline, mouseClickAnimationTime,
								newValue, newInTan, newOutTan, structureChangeListener));
					} else {
						frame.sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue);
						actions.add(new AddKeyframeAction(frame.node, frame.sourceTimeline, mouseClickAnimationTime,
								newValue, structureChangeListener));
					}
				}
			}
		}
		undoManager.pushAction(new CompoundAction("paste keyframe", actions));
		revalidateKeyframeDisplay();
	}

	private void pasteToSpecificTimeline(final ModelStructureChangeListener structureChangeListener,
			final Map.Entry<Integer, KeyFrame> timeAndKey, final AnimFlag flag) {
		boolean foundCopiedMatch = false;
		for (final CopiedKeyFrame frame : copiedKeyframes) {
			if (frame.sourceTimeline == flag) {
				// only paste to selected nodes
				final int mouseClickAnimationTime = timeAndKey.getKey();// computeTimeFromX(e.getX());

				final int flooredTimeIndex = flag.floorIndex(mouseClickAnimationTime);
				final Object newValue = AnimFlag.cloneValue(frame.value);
				// tans might be null
				final Object newInTan = AnimFlag.cloneValue(frame.inTan);
				final Object newOutTan = AnimFlag.cloneValue(frame.outTan);
				if (flooredTimeIndex != -1 && flooredTimeIndex < flag.getTimes().size()
						&& flag.getTimes().get(flooredTimeIndex) == mouseClickAnimationTime) {
					if (frame.sourceTimeline.tans()) {
						final Object oldValue = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						final Object oldInTan = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						final Object oldOutTan = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						frame.sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
						undoManager.pushAction(
								new SetKeyframeAction(frame.node, frame.sourceTimeline, mouseClickAnimationTime,
										newValue, newInTan, newOutTan, oldValue, oldInTan, oldOutTan, new Runnable() {
											@Override
											public void run() {
												// TODO this is a hack to refresh screen
												// while dragging
												notifier.timeChanged(currentTime);
											}
										}));
					} else {
						final Object oldValue = frame.sourceTimeline.valueAt(mouseClickAnimationTime);
						frame.sourceTimeline.setKeyframe(mouseClickAnimationTime, newValue);
						undoManager.pushAction(new SetKeyframeAction(frame.node, frame.sourceTimeline,
								mouseClickAnimationTime, newValue, oldValue, new Runnable() {
									@Override
									public void run() {
										// TODO this is a hack to refresh screen
										// while dragging
										notifier.timeChanged(currentTime);
									}
								}));
					}
				} else {
					if (frame.sourceTimeline.tans()) {
						frame.sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue, newInTan, newOutTan);
						undoManager.pushAction(new AddKeyframeAction(frame.node, frame.sourceTimeline,
								mouseClickAnimationTime, newValue, newInTan, newOutTan, structureChangeListener));
					} else {
						frame.sourceTimeline.addKeyframe(mouseClickAnimationTime, newValue);
						undoManager.pushAction(new AddKeyframeAction(frame.node, frame.sourceTimeline,
								mouseClickAnimationTime, newValue, structureChangeListener));
					}
				}

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

	public final class KeyFrame {
		private int time;
		private final Set<IdObject> objects = new HashSet<>();
		private final List<AnimFlag> timelines = new ArrayList<>();
		private final Rectangle renderRect;

		private KeyFrame(final int time) {
			this.time = time;
			final int currentTimePixelX = computeXFromTime(time);
			this.renderRect = new Rectangle(currentTimePixelX - 4, VERTICAL_SLIDER_HEIGHT, 8, VERTICAL_TICKS_HEIGHT);
		}

		protected void reposition() {
			renderRect.x = computeXFromTime(time) - 4;
		}
	}

	private static final class CopiedKeyFrame {
		private final TimelineContainer node;
		private final AnimFlag sourceTimeline;
		private final Object value;
		private final Object inTan;
		private final Object outTan;

		public CopiedKeyFrame(final TimelineContainer node, final AnimFlag sourceTimeline, final Object value,
				final Object inTan, final Object outTan) {
			this.node = node;
			this.sourceTimeline = sourceTimeline;
			this.value = value;
			this.inTan = inTan;
			this.outTan = outTan;
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
		} else {
			liveAnimationTimer.start();
		}
	}

	public void setDrawing(final boolean drawing) {
		this.drawing = drawing;
		for (final Component component : this.getComponents()) {
			component.setEnabled(drawing);
		}
		repaint();
	}
}
