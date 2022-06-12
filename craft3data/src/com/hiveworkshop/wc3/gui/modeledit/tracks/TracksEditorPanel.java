package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SlideKeyframeByIndexAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.ReversedAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;

public class TracksEditorPanel extends JPanel {
	private static final float TIME_SCALE_SETTING_DIVISOR = 1000.0f;
	private final ModelComponentAnimFlagTree modelComponentAnimFlagTree;
	private final JSlider scaleSlider;
	private final JTextField mouseTimeField;
	private int lastScale;

	public TracksEditorPanel(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager, final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener) {
		modelComponentAnimFlagTree = new ModelComponentAnimFlagTree(modelViewManager, undoActionListener,
				modelEditorManager, modelStructureChangeListener);
		setLayout(new BorderLayout());

		scaleSlider = new JSlider(1, 3000, 500);
		lastScale = scaleSlider.getValue();
		mouseTimeField = new JTextField(35);
		final TracksEditorTimelinePanel timelinePanel = new TracksEditorTimelinePanel(modelComponentAnimFlagTree,
				modelViewManager, scaleSlider, undoActionListener, modelStructureChangeListener);
		modeNotifier.addToolbarButtonListener(timelinePanel);
		final JScrollPane pane = new JScrollPane(timelinePanel);
		pane.setRowHeaderView(modelComponentAnimFlagTree);
		scaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				timelinePanel.setPreferredSize(
						new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));
				pane.setViewportView(timelinePanel);
				final int newScale = scaleSlider.getValue();
				pane.getHorizontalScrollBar().setValue(pane.getHorizontalScrollBar().getValue() * newScale / lastScale);
				pane.repaint();
				lastScale = newScale;
			}
		});

		final JPanel controlsPanel = new JPanel();
		mouseTimeField.setEditable(false);
		controlsPanel.add(new JLabel("Scale:"));
		controlsPanel.add(scaleSlider);
		controlsPanel.add(new JLabel("Mouse:"));
		controlsPanel.add(mouseTimeField);
		timelinePanel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(final MouseEvent e) {
				final double scale = scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR;
				mouseTimeField.setText(Double.toString(e.getX() / scale));
				timelinePanel.mouseMoved(e.getX(), e.getY());
				timelinePanel.repaint();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (timelinePanel.slidingKeys) {
					final double scale = scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR;
					final int delta = (int) ((e.getX() - timelinePanel.mouseDragStart.x) / scale);
					mouseTimeField.setText(delta > 0 ? "+" + delta : Integer.toString(delta));
				} else {
					mouseTimeField.setText("");
				}
				timelinePanel.mouseDragged(e.getX(), e.getY());
				timelinePanel.repaint();
				timelinePanel.requestFocus();
			}
		});
		timelinePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				timelinePanel.mouseReleased(e);
				timelinePanel.repaint();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				timelinePanel.mousePressed(e);
				timelinePanel.repaint();
				timelinePanel.requestFocus();
			}
		});
		add(controlsPanel, BorderLayout.BEFORE_FIRST_LINE);
		add(pane, BorderLayout.CENTER);
//		JTable table = new JTable();
//		new TreeList<>(null, null, null)
//		TreeTableSupport support = TreeTableSupport.install(table, null, 0);
//		support.
		modelComponentAnimFlagTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				timelinePanel.setPreferredSize(
						new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));
				pane.setViewportView(timelinePanel);
				pane.repaint();
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				timelinePanel.setPreferredSize(
						new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));
				pane.setViewportView(timelinePanel);
				pane.repaint();
			}
		});
		modelComponentAnimFlagTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				timelinePanel.repaint();
			}
		});
		timelinePanel.setPreferredSize(
				new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));

		final ActionMap map = getActionMap();
		map.put("Delete", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				timelinePanel.deleteKeyframes();
			}
		});
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "Delete");
	}

	private static final class TracksEditorTimelinePanel extends JPanel
			implements ToolbarButtonListener<SelectionMode> {
		private final GradientPaint keyframePaint;
		private final GradientPaint keyframePaintBlue;
		private final GradientPaint keyframePaintTeal;
		private final GradientPaint keyframePaintHighlight;
		private final GradientPaint keyframePaintRed;
		private final ModelComponentAnimFlagTree tree;
		private final ModelView modelView;
		private final JSlider scaleSlider;
		private final Rectangle recycleClipRect = new Rectangle();
		private int mouseHoverX;
		private Point mouseDragStart = null;
		private final Point mouseDragEnd = new Point();
		private boolean slidingKeys = false;
		private final Map<AnimFlag, Set<Integer>> selectedTrackToIndices = new HashMap<>();
		private int mouseHoverY;
		private final UndoActionListener undoActionListener;
		private final ModelStructureChangeListener modelStructureChangeListener;
		private SelectionMode selectionType;

		public TracksEditorTimelinePanel(final ModelComponentAnimFlagTree tree, final ModelView modelView,
				final JSlider scaleSlider, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener) {
			this.tree = tree;
			this.modelView = modelView;
			this.scaleSlider = scaleSlider;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
			keyframePaint = new GradientPaint(new Point(0, 10), new Color(200, 255, 200), new Point(0, getHeight()),
					new Color(100, 255, 100), true);
			keyframePaintBlue = new GradientPaint(new Point(0, 10), new Color(200, 200, 255), new Point(0, getHeight()),
					new Color(100, 100, 255), true);
			keyframePaintTeal = new GradientPaint(new Point(0, 10), new Color(200, 255, 255), new Point(0, getHeight()),
					new Color(100, 255, 255), true);
			keyframePaintRed = new GradientPaint(new Point(0, 10), new Color(255, 200, 200), new Point(0, getHeight()),
					new Color(255, 100, 100), true);
			keyframePaintHighlight = new GradientPaint(new Point(0, 10), new Color(255, 0, 0),
					new Point(0, getHeight()), new Color(0, 0, 0), true);

			setFocusable(true);
		}

		public void deleteKeyframes() {
			final com.etheller.collections.ArrayList<UndoAction> actions = new com.etheller.collections.ArrayList();

			final int rowCount = tree.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				final TreePath pathForRow = tree.getPathForRow(i);
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
				final Object userObject = node.getUserObject();
				if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
					final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject).getFlag();
					final Set<Integer> selectedIndices = selectedTrackToIndices.get(track);

					final Object parentTreeNodeUserObject = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
					if (parentTreeNodeUserObject instanceof ModelComponentAnimFlagTree.ChooseableDisplayElement) {
						final Object editableModelComponentObject = ((ModelComponentAnimFlagTree.ChooseableDisplayElement) parentTreeNodeUserObject)
								.getItem();
						if (editableModelComponentObject instanceof TimelineContainer) {
							final TimelineContainer container = (TimelineContainer) editableModelComponentObject;

							if (selectedIndices != null) {
								final ArrayList<Integer> times = track.getTimes();
								for (final int index : selectedIndices) {
									final Integer time = times.get(index);

									final ReversedAction deleteFrameAction;
									// NOTE: this comment is copied from TimeSliderPanel, and the code is mostly
									// copied,
									// maybe later they can become one function for easier code maintenance:
									//
									// I'm going to cheat a little bit.
									// When this saves in the "undo stack" the list of keyframe values
									// to put back if we CTRL+Z, it will store the memory references
									// directly. This makes the assumption that we can't graphically edit
									// deleted keyframes, and I'm pretty certain that should be true.
									// (Copy&Paste cannot use this optimization, and must create deep copies
									// of the keyframe values)
									if (track.tans()) {
										deleteFrameAction = new ReversedAction("delete keyframe",
												new AddKeyframeAction(container, track, time,
														track.getValues().get(index), track.getInTans().get(index),
														track.getOutTans().get(index), modelStructureChangeListener));
									} else {
										deleteFrameAction = new ReversedAction("delete keyframe",
												new AddKeyframeAction(container, track, time,
														track.getValues().get(index), modelStructureChangeListener));
									}
									actions.add(deleteFrameAction);
								}
							}
						}
					}

				}

			}

			final CompoundAction compoundAction = new CompoundAction("Delete Keyframe(s)", actions);
			compoundAction.redo();
			undoActionListener.pushAction(compoundAction);
			repaint();
			selectedTrackToIndices.clear();

		}

		public void mousePressed(final MouseEvent e) {
			if (mouseDragStart == null) {
				mouseDragStart = e.getPoint();
				mouseDragEnd.setLocation(mouseDragStart);

				if (SwingUtilities.isRightMouseButton(e)) {
					slidingKeys = true;
				} else {
					slidingKeys = false;
					final int rowHeight = 16;// tree.getLastRendererRowHeight();
					final int rowCount = tree.getRowCount();
					for (int i = 0; i < rowCount; i++) {
						final TreePath pathForRow = tree.getPathForRow(i);
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
						final Object userObject = node.getUserObject();
						if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
							final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject)
									.getFlag();

							final ArrayList<Integer> times = track.getTimes();
							final Set<Integer> selectedIndices = selectedTrackToIndices.get(track);
							if (selectedIndices != null) {
								for (int j = 0; j < times.size(); j++) {
									final Integer time = times.get(j);

									final int currentTimePixelX = computeXFromTime(time);

									// TODO the keyframeRectangle computation needs to match what is rendered --
									// ideal code in the future
									// would have code sharing between this and the render function, in order to
									// have less copies of the same idea
									// for programmers to maintain
									final Rectangle keyframeRectangle = new Rectangle(currentTimePixelX - 4,
											rowHeight * i, 8, rowHeight);

									if (keyframeRectangle.contains(mouseDragStart) && selectedIndices.contains(j)) {
										slidingKeys = true;
									}
								}
							}
						}

					}
				}
			}
		}

		public void mouseReleased(final MouseEvent e) {
			if (mouseDragStart != null) {
				if (slidingKeys) {
					final int xDelta = mouseDragEnd.x - mouseDragStart.x;
					final double scale = scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR;
					final int timeDelta = (int) (xDelta / scale);

					final com.etheller.collections.ArrayList<SlideKeyframeByIndexAction> actions = new com.etheller.collections.ArrayList();
					final Runnable repainter = new Runnable() {
						@Override
						public void run() {
							repaint();
						}
					};
					for (final Map.Entry<AnimFlag, Set<Integer>> trackToIndices : selectedTrackToIndices.entrySet()) {
						final AnimFlag track = trackToIndices.getKey();
						final Set<Integer> selectedIndices = trackToIndices.getValue();
						for (final Integer index : selectedIndices) {
							actions.add(new SlideKeyframeByIndexAction(track, index, timeDelta, repainter));
						}
					}
					final CompoundAction compoundAction = new CompoundAction("Slide Keyframe(s)", actions);
					compoundAction.redo();
					undoActionListener.pushAction(compoundAction);
				} else {
					// select everything in there
					final int rowHeight = 16;// tree.getLastRendererRowHeight();
					final int rowCount = tree.getRowCount();
					final int dragMinX = Math.min(mouseDragStart.x, mouseDragEnd.x);
					final int dragMinY = Math.min(mouseDragStart.y, mouseDragEnd.y);
					final Rectangle dragArea = new Rectangle(dragMinX, dragMinY,
							Math.abs(mouseDragStart.x - mouseDragEnd.x), Math.abs(mouseDragStart.y - mouseDragEnd.y));
					if (dragArea.width == 0) {
						dragArea.width = 1;
					}
					if (dragArea.height == 0) {
						dragArea.height = 1;
					}
					if (selectionType == SelectionMode.SELECT) {
						selectedTrackToIndices.clear();
					}
					for (int i = 0; i < rowCount; i++) {
						final TreePath pathForRow = tree.getPathForRow(i);
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
						final Object userObject = node.getUserObject();
						if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
							final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject)
									.getFlag();

							final ArrayList<Integer> times = track.getTimes();
							Set<Integer> selectedIndices = selectedTrackToIndices.get(track);
							for (int j = 0; j < times.size(); j++) {
								final Integer time = times.get(j);

								final int currentTimePixelX = computeXFromTime(time);

								// TODO the keyframeRectangle computation needs to match what is rendered --
								// ideal code in the future
								// would have code sharing between this and the render function, in order to
								// have less copies of the same idea
								// for programmers to maintain
								final Rectangle keyframeRectangle = new Rectangle(currentTimePixelX - 4, rowHeight * i,
										8, rowHeight);

								if (dragArea.intersects(keyframeRectangle)) {
									if (selectionType == SelectionMode.DESELECT) {
										if (selectedIndices != null) {
											selectedIndices.remove(j);
											if (selectedIndices.isEmpty()) {
												selectedTrackToIndices.remove(track);
												selectedIndices = null;
											}
										}

									} else {
										if (selectedIndices == null) {
											selectedIndices = new HashSet<>();
											selectedTrackToIndices.put(track, selectedIndices);
										}
										selectedIndices.add(j);
									}
								}
							}
						}

					}
				}
			}
			mouseDragStart = null;
		}

		public void mouseDragged(final int x, final int y) {
			if (mouseDragStart == null) {
				final Point dragStartingPoint = new Point(x, y);
				mouseDragStart = dragStartingPoint;
			}
			mouseDragEnd.setLocation(x, y);
		}

		public void mouseMoved(final int mouseHoverX, final int mouseHoverY) {
			this.mouseHoverX = mouseHoverX;
			this.mouseHoverY = mouseHoverY;
			mouseDragStart = null;
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final int rowHeight = 16;// tree.getLastRendererRowHeight();
			final int rowCount = tree.getRowCount();
			boolean mousedOverAnythingEditable = false;
			for (int i = 0; i < rowCount; i++) {
				final TreePath pathForRow = tree.getPathForRow(i);
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
				final Object userObject = node.getUserObject();
				if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
					final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject).getFlag();
					final Set<Integer> selectedIndices = selectedTrackToIndices.get(track);
					final boolean afVisbility = "Visibility".equals(track.getName());
					final boolean afAlpha = "Alpha".equals(track.getName());
					final boolean afHideShow = afAlpha || afVisbility;
					final boolean afColor = "Color".equals(track.getName());
					boolean translation = false, rotation = false, scaling = false, other = false;
					final boolean afTranslation = "Translation".equals(track.getName());
					translation |= afTranslation;
					final boolean afRotation = "Rotation".equals(track.getName());
					rotation |= afRotation;
					final boolean afScaling = "Scaling".equals(track.getName());
					scaling |= afScaling;
					other |= !(afTranslation || afRotation || afScaling);

					g.setColor(Color.BLACK);
					g.fillRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					final ArrayList<Integer> times = track.getTimes();
					Object lastValue = null;
					int lastEndX = 0;
					final InterpolationType interpTypeAsEnum = track.getInterpTypeAsEnum();
					for (int j = 0; j < times.size(); j++) {
						final Integer time = times.get(j);

						final int currentTimePixelX = computeXFromTime(time);
						final boolean mouseOver = mouseHoverX >= currentTimePixelX - 4
								&& mouseHoverX < currentTimePixelX + 4 && mouseHoverY >= rowHeight * i
								&& mouseHoverY < rowHeight * (i + 1);// timeAndKey.getValue() == mouseOverFrame;
						final boolean selected = selectedIndices != null && selectedIndices.contains(j);
						if (mouseOver && selected) {
							mousedOverAnythingEditable = true;
						}
						if (selected) {
							((Graphics2D) g).setPaint(keyframePaintHighlight);
						} else if (afHideShow) {
							final Object value = track.getValues().get(j);
							if (value instanceof Number) {
								final float afHideShowNewValue = ((Number) value).floatValue();
								if (afHideShowNewValue < 1.0f) {
									g.setColor(new Color(0f, 1f, 1f, afHideShowNewValue));
								} else {
									((Graphics2D) g).setPaint(keyframePaintTeal);
								}
							}
						} else if (afColor) {
							final Object value = track.getValues().get(j);
							if (value instanceof Vertex) {
								final Vertex colorData = (Vertex) value;
								g.setColor(new Color((float) colorData.x, (float) colorData.y, (float) colorData.z));
							} else {
								((Graphics2D) g).setPaint(keyframePaint);
							}

						} else if (scaling) {
							((Graphics2D) g).setPaint(keyframePaintRed);
						} else if (rotation) {
							((Graphics2D) g).setPaint(keyframePaint);
						} else if (translation) {
							((Graphics2D) g).setPaint(keyframePaintBlue);
						} else {
							((Graphics2D) g).setPaint(keyframePaint);
						}
						g.fillRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
						Color color = Color.GREEN;
						if (afHideShow) {
							color = Color.CYAN;

							final Object value = track.getValues().get(j);
							if (value instanceof Number) {
								float afHideShowLastValue;
								if (lastValue instanceof Number) {
									afHideShowLastValue = ((Number) lastValue).floatValue();
								} else {
									afHideShowLastValue = ((Number) track.getIdentity()).floatValue();
								}
								final float afHideShowNewValue = ((Number) value).floatValue();
								if (interpTypeAsEnum == InterpolationType.DONT_INTERP) {
									g.setColor(new Color(0f, 1f, 1f, afHideShowLastValue));
								} else {
									((Graphics2D) g).setPaint(new GradientPaint(lastEndX, 0,
											new Color(0f, afHideShowLastValue, afHideShowLastValue,
													afHideShowLastValue),
											currentTimePixelX - 4, 0,
											new Color(0f, afHideShowNewValue, afHideShowNewValue, afHideShowNewValue)));
								}
								g.fillRect(lastEndX, rowHeight * i + rowHeight / 4, currentTimePixelX - 4 - lastEndX,
										rowHeight / 2);
								lastValue = value;
							}
						} else if (afColor) {
							color = Color.GRAY;

							final Object value = track.getValues().get(j);
							if (value instanceof Vertex) {
								Color afHideShowLastValue;
								if (lastValue instanceof Vertex) {
									final Vertex colorData = (Vertex) lastValue;
									afHideShowLastValue = new Color((float) colorData.x, (float) colorData.y,
											(float) colorData.z);
								} else {
									final Object identity = track.getIdentity();
									final Vertex colorData = (Vertex) identity;
									afHideShowLastValue = new Color((float) colorData.x, (float) colorData.y,
											(float) colorData.z);
								}
								final Vertex colorData = (Vertex) value;
								final Color afHideShowNewValue = new Color((float) colorData.x, (float) colorData.y,
										(float) colorData.z);
								if (interpTypeAsEnum == InterpolationType.DONT_INTERP) {
									g.setColor(afHideShowLastValue);
								} else {
									((Graphics2D) g).setPaint(new GradientPaint(lastEndX, 0, afHideShowLastValue,
											currentTimePixelX - 4, 0, afHideShowNewValue));
								}
								g.fillRect(lastEndX, rowHeight * i + rowHeight / 4, currentTimePixelX - 4 - lastEndX,
										rowHeight / 2);
								lastValue = value;
							}
						} else if (scaling) {
							color = Color.ORANGE;
						} else if (rotation) {
						} else if (translation) {
							color = Color.BLUE;
						}
						g.setColor(mouseOver ? Color.WHITE : selected ? Color.RED : color);
						g.drawRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
						lastEndX = currentTimePixelX + 4;
					}
				} else if (userObject instanceof ModelComponentAnimFlagTree.ChooseableModelRoot) {
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					for (final Animation anim : modelView.getModel().getAnims()) {
						final int xEnd = computeXFromTime(anim.getStart());
						final int xStart = computeXFromTime(anim.getEnd());
						g.setColor(Color.RED.darker());
						g.drawLine(xStart, 0, xStart, getHeight());
						g.drawLine(xEnd, 0, xEnd, getHeight());
						g.setColor(Color.BLACK);
						final String animName = "\"" + anim.getName() + "\"";
						g.drawString(animName, xStart, rowHeight * i + (rowHeight + g.getFont().getSize()) / 2);
						g.drawString(animName, xEnd, rowHeight * i + (rowHeight + g.getFont().getSize()) / 2);
					}
				} else {
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
				}

			}
			g.setColor(getBackground());
			g.fill3DRect(0, getHeight() - 16, getWidth(), 16, true);
			int tickSize = 1;
			final double scale = scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR;
			final int minimumTickSize = (int) Math.max(1, 25 / scale);
			while (tickSize < minimumTickSize) {
				tickSize *= 10;
			}
			g.getClipBounds(recycleClipRect);
			final int minTick = (int) Math.ceil(recycleClipRect.x / scale / tickSize);
			final int maxTick = (int) Math.floor((recycleClipRect.x + recycleClipRect.width) / scale / tickSize);
			final Color foreground = getForeground();
			final Color brighterForeground = Color.GRAY;
			for (int tick = minTick; tick <= maxTick; tick++) {
				final int tickTime = tick * tickSize;
				final int tickX = computeXFromTime(tickTime);
				g.setColor(foreground);
				g.drawLine(tickX, getHeight() - 16, tickX, getHeight());
				g.setColor(brighterForeground);
				g.drawString(Integer.toString(tickTime), tickX, getHeight() - 6);
			}

			if (mouseDragStart != null) {
				if (slidingKeys) {
					((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.5f));
					for (int i = 0; i < rowCount; i++) {
						final TreePath pathForRow = tree.getPathForRow(i);
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
						final Object userObject = node.getUserObject();
						if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
							final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject)
									.getFlag();
							final Set<Integer> selectedIndices = selectedTrackToIndices.get(track);
							if (selectedIndices != null) {
								for (final Integer index : selectedIndices) {
									final Integer time = track.getTimes().get(index);
									final int xDelta = mouseDragEnd.x - mouseDragStart.x;
									final int timeDelta = (int) (xDelta / scale);

									final int newTime = time + timeDelta;
									final int currentTimePixelX = computeXFromTime(newTime);
									((Graphics2D) g).setPaint(keyframePaintHighlight);
									g.fillRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
									g.setColor(Color.RED);
									g.drawRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
									g.setColor(Color.WHITE);
									g.drawString(Integer.toString(newTime), currentTimePixelX,
											rowHeight * i + rowHeight - 6);
								}
							}
						}
					}

				} else {
					g.setColor(Color.RED);
					final int dragMinX = Math.min(mouseDragStart.x, mouseDragEnd.x);
					final int dragMinY = Math.min(mouseDragStart.y, mouseDragEnd.y);
					g.drawRect(dragMinX, dragMinY, Math.abs(mouseDragStart.x - mouseDragEnd.x),
							Math.abs(mouseDragStart.y - mouseDragEnd.y));
				}
			} else {
				g.setColor(Color.GRAY);
				g.drawLine(mouseHoverX, 0, mouseHoverX, getHeight());
			}

			if (mousedOverAnythingEditable) {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				setCursor(null);
			}
		}

		private int computeXFromTime(final int time) {
//		final double timeRatio = (time - start) / (double) (end - start);
//		final int widthMinusOffsets = getWidth() - (SIDE_OFFSETS * 2);
//		return (int) (widthMinusOffsets * timeRatio) + (timeChooserRect.width / 2);
			return (int) (time * (scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR));
		}

		@Override
		public void typeChanged(final SelectionMode newType) {
			this.selectionType = newType;
		}
	}

	public void reloadFromModelView() {
		modelComponentAnimFlagTree.reloadFromModelView();
	}

	private int getMaxX(final ModelViewManager modelViewManager) {
		int maxTime = 0;
		for (final Animation anim : modelViewManager.getModel().getAnims()) {
			if (anim.getIntervalEnd() > maxTime) {
				maxTime = anim.getIntervalEnd();
			}
		}
		for (final Integer globalSeq : modelViewManager.getModel().getGlobalSeqs()) {
			if (globalSeq > maxTime) {
				maxTime = globalSeq;
			}
		}
		maxTime += 1000;
		final int maxX = (int) (maxTime * (scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR));
		return maxX;
	}
}
