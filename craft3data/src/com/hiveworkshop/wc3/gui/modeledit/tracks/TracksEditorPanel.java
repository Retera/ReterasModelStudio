package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.animedit.FixedTimeEnvironment;
import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SetKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SetTrackGlobalSequenceAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SetTrackInterpolationAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SlideKeyframeByIndexAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.ReversedAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.gui.modeledit.util.EditingHotkeys;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;

/**
 * The Tracks view: a tree of animated components and their tracks on the
 * left, a timeline of their keys in the middle, and a keyframe inspector on
 * the right.
 * <p>
 * Left-drag on empty space rubber-band selects; left-drag on a selected key
 * slides the selection; middle-drag slides too. Right-click opens the track
 * menu (insert, copy, paste, duplicate, delete, interpolation, global
 * sequence). Double-click a key to edit it in the inspector. Clicking the
 * ruler strip at the bottom moves the shared playhead.
 */
public class TracksEditorPanel extends JPanel {
	private static final float TIME_SCALE_SETTING_DIVISOR = 1000.0f;
	private static final int ROW_HEIGHT = 16;
	private static final int RULER_HEIGHT = 16;
	private static final int KEY_HALF_WIDTH = 4;

	private final ModelComponentAnimFlagTree modelComponentAnimFlagTree;
	private final JSlider scaleSlider;
	private int lastScale;
	private final JTextField mouseTimeField;
	private final TracksEditorTimelinePanel timelinePanel;
	private final KeyframeInspectorPanel inspector;
	private final ModelViewManager modelViewManager;

	public TracksEditorPanel(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager, final ToolbarButtonGroup<SelectionMode> modeNotifier,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.modelViewManager = modelViewManager;
		modelComponentAnimFlagTree = new ModelComponentAnimFlagTree(modelViewManager, undoActionListener,
				modelEditorManager, modelStructureChangeListener);
		setLayout(new BorderLayout());
		scaleSlider = new JSlider(1, 3000, 500);
		lastScale = scaleSlider.getValue();
		mouseTimeField = new JTextField(35);
		inspector = new KeyframeInspectorPanel(undoActionListener, this::repaint);
		inspector.setModel(modelViewManager.getModel());
		inspector.setPreferredSize(new Dimension(290, 200));
		timelinePanel = new TracksEditorTimelinePanel(modelComponentAnimFlagTree, modelViewManager, scaleSlider,
				undoActionListener, modelStructureChangeListener, inspector);
		modeNotifier.addToolbarButtonListener(timelinePanel);
		final JScrollPane pane = new JScrollPane(timelinePanel);
		pane.setRowHeaderView(modelComponentAnimFlagTree);
		final TimeRulerPanel ruler = new TimeRulerPanel(timelinePanel);
		pane.setColumnHeaderView(ruler);
		timelinePanel.ruler = ruler;
		scaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				timelinePanel.setPreferredSize(
						new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));
				pane.setViewportView(timelinePanel);
				ruler.setPreferredSize(new Dimension(getMaxX(modelViewManager), RULER_HEIGHT + 4));
				pane.setColumnHeaderView(ruler);
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
				mouseTimeField.setText(Integer.toString(timelinePanel.timeFromX(e.getX())));
				timelinePanel.mouseMoved(e.getX(), e.getY());
				timelinePanel.repaint();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (timelinePanel.slidingKeys && (timelinePanel.mouseDragStart != null)) {
					final int delta = timelinePanel.timeFromX(e.getX())
							- timelinePanel.timeFromX(timelinePanel.mouseDragStart.x);
					mouseTimeField.setText(delta > 0 ? "+" + delta : Integer.toString(delta));
				} else {
					mouseTimeField.setText(Integer.toString(timelinePanel.timeFromX(e.getX())));
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

			@Override
			public void mouseClicked(final MouseEvent e) {
				timelinePanel.mouseClicked(e);
			}
		});
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
				pane.repaint();
			}
		});
		timelinePanel.setPreferredSize(
				new Dimension(getMaxX(modelViewManager), modelComponentAnimFlagTree.getPreferredSize().height));
		ruler.setPreferredSize(new Dimension(getMaxX(modelViewManager), RULER_HEIGHT + 4));
		EditingHotkeys.installDelete(this, timelinePanel::deleteKeyframes);
		EditingHotkeys.installClipboard(this, timelinePanel::cutSelected, timelinePanel::copySelected,
				() -> timelinePanel.pasteAt(timelinePanel.playheadOrHoverTime(), null));
		add(controlsPanel, BorderLayout.BEFORE_FIRST_LINE);
		add(pane, BorderLayout.CENTER);
		add(inspector, BorderLayout.EAST);
	}

	/**
	 * Shares the animation playhead: the timeline draws the environment's
	 * current time and clicking the ruler hands the chosen time to
	 * {@code playheadSetter} (normally the time slider), which propagates it
	 * back to the environment and the viewports.
	 */
	public void setTimeEnvironment(final TimeEnvironmentImpl environment, final IntConsumer playheadSetter) {
		timelinePanel.timeEnvironment = environment;
		timelinePanel.playheadSetter = playheadSetter;
		repaint();
	}

	/**
	 * Selects the row for a component (a node, layer, camera... or one of its
	 * tracks) and scrolls it into view. Used by "Open in Tracks".
	 */
	public void selectObject(final Object object) {
		modelComponentAnimFlagTree.selectObject(object);
		final TreePath path = modelComponentAnimFlagTree.getSelectionPath();
		if (path != null) {
			modelComponentAnimFlagTree.expandPath(path);
			modelComponentAnimFlagTree.scrollPathToVisible(path);
		}
		repaint();
	}

	public void reloadFromModelView() {
		modelComponentAnimFlagTree.reloadFromModelView();
		inspector.setModel(modelViewManager.getModel());
		SwingUtilities.invokeLater(timelinePanel::pruneSelection);
	}

	private int getMaxX(final ModelViewManager modelViewManager) {
		int maxTime = 0;
		for (final Animation anim : modelViewManager.getModel().getAnims()) {
			maxTime = Math.max(maxTime, anim.getIntervalEnd());
		}
		for (final Integer globalSeq : modelViewManager.getModel().getGlobalSeqs()) {
			maxTime = Math.max(maxTime, globalSeq);
		}
		maxTime += 1000;
		return (int) (maxTime * (scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR));
	}

	/**
	 * The time ruler, kept in the scroll pane's column header so it stays visible
	 * however tall the track list gets. Click or drag on it to move the shared
	 * playhead.
	 */
	private static final class TimeRulerPanel extends JPanel {
		private final TracksEditorTimelinePanel timeline;

		TimeRulerPanel(final TracksEditorTimelinePanel timeline) {
			this.timeline = timeline;
			setPreferredSize(new Dimension(100, RULER_HEIGHT + 4));
			final MouseAdapter mouse = new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					seek(e);
				}

				@Override
				public void mouseDragged(final MouseEvent e) {
					seek(e);
				}

				private void seek(final MouseEvent e) {
					if (timeline.playheadSetter != null) {
						timeline.playheadSetter.accept(Math.max(0, timeline.timeFromX(e.getX())));
					}
				}
			};
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;
			g2.setColor(getBackground());
			g2.fill3DRect(0, 0, getWidth(), getHeight(), true);
			int tickSize = 1;
			final double scale = timeline.scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR;
			final int minimumTickSize = (int) Math.max(1, 25 / scale);
			while (tickSize < minimumTickSize) {
				tickSize *= 10;
			}
			final Rectangle clip = g2.getClipBounds();
			final int minTick = (int) Math.ceil(clip.x / scale / tickSize);
			final int maxTick = (int) Math.floor((clip.x + clip.width) / scale / tickSize);
			for (int tick = minTick; tick <= maxTick; tick++) {
				final int tickTime = tick * tickSize;
				final int tickX = timeline.xFromTime(tickTime);
				g2.setColor(getForeground());
				g2.drawLine(tickX, getHeight() - 6, tickX, getHeight());
				g2.setColor(Color.GRAY);
				g2.drawString(Integer.toString(tickTime), tickX + 2, getHeight() - 7);
			}
			for (final Animation anim : timeline.modelView.getModel().getAnims()) {
				g2.setColor(Color.RED.darker());
				g2.drawLine(timeline.xFromTime(anim.getStart()), 0, timeline.xFromTime(anim.getStart()), getHeight());
				g2.drawLine(timeline.xFromTime(anim.getEnd()), 0, timeline.xFromTime(anim.getEnd()), getHeight());
			}
			if (timeline.timeEnvironment != null) {
				final int x = timeline.xFromTime(timeline.playheadTime());
				g2.setColor(new Color(255, 220, 0));
				g2.fillPolygon(new int[] { x - 6, x + 6, x }, new int[] { 0, 0, getHeight() - 1 }, 3);
				g2.setColor(Color.BLACK);
				g2.drawPolygon(new int[] { x - 6, x + 6, x }, new int[] { 0, 0, getHeight() - 1 }, 3);
			}
		}
	}

	/** One tree row that shows a track. */
	private static final class TrackRow {
		final int row;
		final AnimFlag track;
		final TimelineContainer container;

		TrackRow(final int row, final AnimFlag track, final TimelineContainer container) {
			this.row = row;
			this.track = track;
			this.container = container;
		}
	}

	private static final class TracksEditorTimelinePanel extends JPanel
			implements ToolbarButtonListener<SelectionMode> {
		private final GradientPaint keyframePaint;
		private final GradientPaint keyframePaintBlue;
		private final GradientPaint keyframePaintTeal;
		private final GradientPaint keyframePaintRed;
		private final GradientPaint keyframePaintHighlight;
		private final ModelComponentAnimFlagTree tree;
		private final ModelView modelView;
		private final JSlider scaleSlider;
		private final Rectangle recycleClipRect = new Rectangle();
		private final UndoActionListener undoActionListener;
		private final ModelStructureChangeListener modelStructureChangeListener;
		private final KeyframeInspectorPanel inspector;
		private int mouseHoverX;
		private int mouseHoverY;
		private Point mouseDragStart = null;
		private final Point mouseDragEnd = new Point();
		private boolean slidingKeys = false;
		private boolean popupPending = false;
		private final Map<AnimFlag, Set<Integer>> selectedTrackToIndices = new HashMap<>();
		private SelectionMode selectionType = SelectionMode.SELECT;
		private TimeEnvironmentImpl timeEnvironment;
		private IntConsumer playheadSetter;
		private TimeRulerPanel ruler;

		@Override
		public void repaint() {
			super.repaint();
			if (ruler != null) {
				ruler.repaint();
			}
		}

		TracksEditorTimelinePanel(final ModelComponentAnimFlagTree tree, final ModelView modelView,
				final JSlider scaleSlider, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener,
				final KeyframeInspectorPanel inspector) {
			this.tree = tree;
			this.modelView = modelView;
			this.scaleSlider = scaleSlider;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
			this.inspector = inspector;
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

		// ---- geometry shared by hit testing and painting ----

		int xFromTime(final int time) {
			return (int) (time * (scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR));
		}

		int timeFromX(final int x) {
			return (int) Math.round(x / (scaleSlider.getValue() / TIME_SCALE_SETTING_DIVISOR));
		}

		private Rectangle keyRect(final int row, final int time) {
			return new Rectangle(xFromTime(time) - KEY_HALF_WIDTH, ROW_HEIGHT * row, KEY_HALF_WIDTH * 2, ROW_HEIGHT);
		}

		/** Every tree row that carries a track, top to bottom. */
		private List<TrackRow> trackRows() {
			final List<TrackRow> rows = new ArrayList<>();
			final int rowCount = tree.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				final TreePath path = tree.getPathForRow(i);
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				final Object userObject = node.getUserObject();
				if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
					final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject).getFlag();
					TimelineContainer container = null;
					final Object parentUserObject = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
					if (parentUserObject instanceof ModelComponentAnimFlagTree.ChooseableDisplayElement) {
						final Object item = ((ModelComponentAnimFlagTree.ChooseableDisplayElement<?>) parentUserObject)
								.getItem();
						if (item instanceof TimelineContainer) {
							container = (TimelineContainer) item;
						}
					}
					rows.add(new TrackRow(i, track, container));
				}
			}
			return rows;
		}

		private TrackRow trackRowAt(final int y) {
			final int row = y / ROW_HEIGHT;
			for (final TrackRow trackRow : trackRows()) {
				if (trackRow.row == row) {
					return trackRow;
				}
			}
			return null;
		}

		private KeyframeRef keyAt(final Point point) {
			final TrackRow row = trackRowAt(point.y);
			if (row == null) {
				return null;
			}
			final ArrayList<Integer> times = row.track.getTimes();
			for (int j = times.size() - 1; j >= 0; j--) {
				if (keyRect(row.row, times.get(j)).contains(point)) {
					return new KeyframeRef(row.container, row.track, j);
				}
			}
			return null;
		}

		int playheadOrHoverTime() {
			if (timeEnvironment != null) {
				return playheadTime();
			}
			return Math.max(0, timeFromX(mouseHoverX));
		}

		private int playheadTime() {
			if (timeEnvironment.getGlobalSeq() != null) {
				return timeEnvironment.getAnimationTime();
			}
			return timeEnvironment.getStart() + timeEnvironment.getAnimationTime();
		}

		// ---- selection ----

		private boolean isSelected(final AnimFlag track, final int index) {
			final Set<Integer> indices = selectedTrackToIndices.get(track);
			return (indices != null) && indices.contains(index);
		}

		private void select(final KeyframeRef ref, final boolean add) {
			if (!add) {
				selectedTrackToIndices.clear();
			}
			Set<Integer> indices = selectedTrackToIndices.get(ref.track);
			if (indices == null) {
				indices = new HashSet<>();
				selectedTrackToIndices.put(ref.track, indices);
			}
			indices.add(ref.index);
			selectionChanged();
		}

		private List<KeyframeRef> selectedRefs() {
			final List<KeyframeRef> refs = new ArrayList<>();
			for (final TrackRow row : trackRows()) {
				final Set<Integer> indices = selectedTrackToIndices.get(row.track);
				if (indices == null) {
					continue;
				}
				final List<Integer> sorted = new ArrayList<>(indices);
				java.util.Collections.sort(sorted);
				for (final Integer index : sorted) {
					if (index < row.track.getTimes().size()) {
						refs.add(new KeyframeRef(row.container, row.track, index));
					}
				}
			}
			return refs;
		}

		/** Drops selection indices that no longer exist (after deletes or reloads). */
		void pruneSelection() {
			boolean changed = false;
			for (final Map.Entry<AnimFlag, Set<Integer>> entry : new ArrayList<>(selectedTrackToIndices.entrySet())) {
				final int size = entry.getKey().getTimes().size();
				changed |= entry.getValue().removeIf(index -> index >= size);
				if (entry.getValue().isEmpty()) {
					selectedTrackToIndices.remove(entry.getKey());
					changed = true;
				}
			}
			if (changed) {
				selectionChanged();
			}
			repaint();
		}

		private void selectionChanged() {
			inspector.setSelection(selectedRefs());
			repaint();
		}

		// ---- mouse ----

		void mousePressed(final MouseEvent e) {
			if (mouseDragStart != null) {
				return;
			}
			mouseDragStart = e.getPoint();
			mouseDragEnd.setLocation(mouseDragStart);
			popupPending = false;
			slidingKeys = false;
			if (SwingUtilities.isRightMouseButton(e)) {
				popupPending = true;
				final KeyframeRef key = keyAt(e.getPoint());
				if ((key != null) && !isSelected(key.track, key.index)) {
					select(key, false);
				}
				return;
			}
			final KeyframeRef key = keyAt(e.getPoint());
			if (SwingUtilities.isMiddleMouseButton(e)) {
				slidingKeys = !selectedTrackToIndices.isEmpty();
				return;
			}
			if (key != null) {
				if (!isSelected(key.track, key.index)) {
					select(key, e.isShiftDown() || e.isControlDown() || (selectionType == SelectionMode.ADD));
				}
				slidingKeys = true;
			}
		}

		void mouseReleased(final MouseEvent e) {
			if (mouseDragStart == null) {
				return;
			}
			final boolean moved = mouseDragStart.distance(e.getPoint()) > 3;
			if (popupPending) {
				popupPending = false;
				mouseDragStart = null;
				if (!moved) {
					showPopup(e.getPoint());
				}
				return;
			}
			if (slidingKeys) {
				final int timeDelta = timeFromX(mouseDragEnd.x) - timeFromX(mouseDragStart.x);
				if ((timeDelta != 0) && moved) {
					final List<UndoAction> actions = new ArrayList<>();
					final Runnable repainter = this::repaint;
					for (final Map.Entry<AnimFlag, Set<Integer>> entry : selectedTrackToIndices.entrySet()) {
						for (final Integer index : entry.getValue()) {
							actions.add(new SlideKeyframeByIndexAction(entry.getKey(), index, timeDelta, repainter));
						}
					}
					pushCompound("Slide Keyframe(s)", actions, true);
					inspector.setSelection(selectedRefs());
				}
			} else {
				rubberBandSelect();
			}
			mouseDragStart = null;
		}

		void mouseClicked(final MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
				final KeyframeRef key = keyAt(e.getPoint());
				if (key != null) {
					select(key, false);
					inspector.focusValue();
				}
			}
		}

		private void rubberBandSelect() {
			final Rectangle dragArea = new Rectangle(Math.min(mouseDragStart.x, mouseDragEnd.x),
					Math.min(mouseDragStart.y, mouseDragEnd.y), Math.max(1, Math.abs(mouseDragStart.x - mouseDragEnd.x)),
					Math.max(1, Math.abs(mouseDragStart.y - mouseDragEnd.y)));
			if (selectionType == SelectionMode.SELECT) {
				selectedTrackToIndices.clear();
			}
			for (final TrackRow row : trackRows()) {
				final ArrayList<Integer> times = row.track.getTimes();
				Set<Integer> indices = selectedTrackToIndices.get(row.track);
				for (int j = 0; j < times.size(); j++) {
					if (!dragArea.intersects(keyRect(row.row, times.get(j)))) {
						continue;
					}
					if (selectionType == SelectionMode.DESELECT) {
						if (indices != null) {
							indices.remove(j);
							if (indices.isEmpty()) {
								selectedTrackToIndices.remove(row.track);
								indices = null;
							}
						}
					} else {
						if (indices == null) {
							indices = new HashSet<>();
							selectedTrackToIndices.put(row.track, indices);
						}
						indices.add(j);
					}
				}
			}
			selectionChanged();
		}

		void mouseDragged(final int x, final int y) {
			if (mouseDragStart == null) {
				mouseDragStart = new Point(x, y);
			}
			mouseDragEnd.setLocation(x, y);
		}

		void mouseMoved(final int mouseHoverX, final int mouseHoverY) {
			this.mouseHoverX = mouseHoverX;
			this.mouseHoverY = mouseHoverY;
			mouseDragStart = null;
		}

		// ---- editing operations ----

		private void pushCompound(final String name, final List<UndoAction> actions, final boolean redo) {
			if (actions.isEmpty()) {
				return;
			}
			final UndoAction action = actions.size() == 1 ? actions.get(0)
					: new CompoundAction(name, ListView.Util.of(actions.toArray(new UndoAction[0])));
			if (redo) {
				action.redo();
			}
			undoActionListener.pushAction(action);
			repaint();
		}

		void deleteKeyframes() {
			final List<UndoAction> actions = new ArrayList<>();
			for (final KeyframeRef ref : selectedRefs()) {
				if (ref.container == null) {
					continue;
				}
				final AddKeyframeAction add = ref.track.tans()
						? new AddKeyframeAction(ref.container, ref.track, ref.time(), ref.value(), ref.inTan(),
								ref.outTan(), modelStructureChangeListener)
						: new AddKeyframeAction(ref.container, ref.track, ref.time(), ref.value(),
								modelStructureChangeListener);
				actions.add(new ReversedAction("delete keyframe", add));
			}
			selectedTrackToIndices.clear();
			pushCompound("Delete Keyframe(s)", actions, true);
			selectionChanged();
		}

		void copySelected() {
			TrackKeyClipboard.copy(selectedRefs());
		}

		void cutSelected() {
			copySelected();
			deleteKeyframes();
		}

		/**
		 * Pastes the clipboard so its earliest key lands at {@code time}. Keys go
		 * back to the track they came from; a key already at the target time is
		 * overwritten, otherwise one is added.
		 */
		void pasteAt(final int time, final TrackRow targetRow) {
			if (TrackKeyClipboard.isEmpty()) {
				return;
			}
			final List<UndoAction> actions = new ArrayList<>();
			final List<TrackRow> rows = trackRows();
			final boolean singleTrackPaste = (targetRow != null) && sameTrackForAll(TrackKeyClipboard.getEntries());
			for (final TrackKeyClipboard.Entry entry : TrackKeyClipboard.getEntries()) {
				final AnimFlag track = singleTrackPaste ? targetRow.track : entry.track;
				TimelineContainer container = singleTrackPaste ? targetRow.container : entry.container;
				boolean trackInModel = false;
				for (final TrackRow row : rows) {
					if (row.track == track) {
						trackInModel = true;
						if (container == null) {
							container = row.container;
						}
					}
				}
				if (!trackInModel || (container == null)) {
					continue;
				}
				if (!valueFits(track, entry.value)) {
					continue;
				}
				final int targetTime = time + (entry.time - TrackKeyClipboard.getBaseTime());
				actions.add(writeKeyAction(container, track, targetTime, AnimFlag.cloneValue(entry.value),
						AnimFlag.cloneValue(entry.inTan), AnimFlag.cloneValue(entry.outTan)));
			}
			pushCompound("Paste Keyframe(s)", actions, true);
		}

		private static boolean sameTrackForAll(final List<TrackKeyClipboard.Entry> entries) {
			for (final TrackKeyClipboard.Entry entry : entries) {
				if (entry.track != entries.get(0).track) {
					return false;
				}
			}
			return true;
		}

		private static boolean valueFits(final AnimFlag track, final Object value) {
			if (track.size() == 0) {
				return true;
			}
			return track.getValues().get(0).getClass() == value.getClass();
		}

		/** Set the key at {@code time} if one exists there, otherwise add one. */
		private UndoAction writeKeyAction(final TimelineContainer container, final AnimFlag track, final int time,
				final Object value, final Object inTan, final Object outTan) {
			final int existing = track.getTimes().indexOf(time);
			final Object in = track.tans() ? (inTan == null ? AnimFlag.cloneValue(value) : inTan) : null;
			final Object out = track.tans() ? (outTan == null ? AnimFlag.cloneValue(value) : outTan) : null;
			if (existing >= 0) {
				final Object oldValue = AnimFlag.cloneValue(track.getValues().get(existing));
				if (track.tans()) {
					return new SetKeyframeAction(container, track, time, value, in, out, oldValue,
							AnimFlag.cloneValue(track.getInTans().get(existing)),
							AnimFlag.cloneValue(track.getOutTans().get(existing)), this::repaint);
				}
				return new SetKeyframeAction(container, track, time, value, oldValue, this::repaint);
			}
			if (track.tans()) {
				return new AddKeyframeAction(container, track, time, value, in, out, modelStructureChangeListener);
			}
			return new AddKeyframeAction(container, track, time, value, modelStructureChangeListener);
		}

		/** Adds a key at {@code time} whose value is the track's value there. */
		void insertKeyAt(final TrackRow row, final int time) {
			if ((row == null) || (row.container == null) || (time < 0)) {
				return;
			}
			if (row.track.getTimes().contains(time)) {
				return;
			}
			final Object value = AnimFlag
					.cloneValue(row.track.interpolateAt(new FixedTimeEnvironment(modelView.getModel(), time)));
			final List<UndoAction> actions = new ArrayList<>();
			actions.add(writeKeyAction(row.container, row.track, time, value, null, null));
			pushCompound("Insert Keyframe", actions, true);
			final int index = row.track.getTimes().indexOf(time);
			if (index >= 0) {
				select(new KeyframeRef(row.container, row.track, index), false);
			}
		}

		/** Copies each selected key one millisecond later (a hold key). */
		void duplicateSelected() {
			final List<UndoAction> actions = new ArrayList<>();
			for (final KeyframeRef ref : selectedRefs()) {
				if (ref.container == null) {
					continue;
				}
				final int target = ref.time() + 1;
				if (ref.track.getTimes().contains(target)) {
					continue;
				}
				actions.add(writeKeyAction(ref.container, ref.track, target, AnimFlag.cloneValue(ref.value()),
						AnimFlag.cloneValue(ref.inTan()), AnimFlag.cloneValue(ref.outTan())));
			}
			pushCompound("Duplicate Keyframe(s)", actions, true);
		}

		void setInterpolation(final AnimFlag track, final InterpolationType type) {
			final SetTrackInterpolationAction action = new SetTrackInterpolationAction(track, type, this::repaint);
			action.redo();
			undoActionListener.pushAction(action);
			inspector.setSelection(selectedRefs());
		}

		@SuppressWarnings({ "deprecation", "removal" })
		void convertToGlobalSequence(final AnimFlag track) {
			int length = 1000;
			for (final Integer time : track.getTimes()) {
				length = Math.max(length, time);
			}
			final Integer sequence = new Integer(length);
			final SetTrackGlobalSequenceAction action = new SetTrackGlobalSequenceAction(modelView.getModel(), track,
					sequence, true, modelStructureChangeListener);
			action.redo();
			undoActionListener.pushAction(action);
			inspector.setSelection(selectedRefs());
		}

		void detachGlobalSequence(final AnimFlag track) {
			final SetTrackGlobalSequenceAction action = new SetTrackGlobalSequenceAction(modelView.getModel(), track,
					null, false, modelStructureChangeListener);
			action.redo();
			undoActionListener.pushAction(action);
			inspector.setSelection(selectedRefs());
		}

		void selectAllInTrack(final TrackRow row) {
			selectedTrackToIndices.clear();
			final Set<Integer> indices = new HashSet<>();
			for (int j = 0; j < row.track.getTimes().size(); j++) {
				indices.add(j);
			}
			selectedTrackToIndices.put(row.track, indices);
			selectionChanged();
		}

		// ---- popup ----

		private void showPopup(final Point point) {
			final TrackRow row = trackRowAt(point.y);
			final int time = Math.max(0, timeFromX(point.x));
			final KeyframeRef key = keyAt(point);
			final boolean haveSelection = !selectedTrackToIndices.isEmpty();
			final JPopupMenu menu = new JPopupMenu();
			if (row != null) {
				menu.add(item("Insert Key at " + time, () -> insertKeyAt(row, time)));
			}
			final JMenuItem copy = item("Copy", this::copySelected);
			copy.setEnabled(haveSelection);
			menu.add(copy);
			final JMenuItem cut = item("Cut", this::cutSelected);
			cut.setEnabled(haveSelection);
			menu.add(cut);
			final JMenuItem paste = item("Paste at " + time, () -> pasteAt(time, row));
			paste.setEnabled(!TrackKeyClipboard.isEmpty());
			menu.add(paste);
			final JMenuItem duplicate = item("Duplicate (hold key +1 ms)", this::duplicateSelected);
			duplicate.setEnabled(haveSelection);
			menu.add(duplicate);
			final JMenuItem delete = item("Delete", this::deleteKeyframes);
			delete.setEnabled(haveSelection);
			menu.add(delete);
			if (row != null) {
				menu.addSeparator();
				menu.add(item("Select All Keys in Track", () -> selectAllInTrack(row)));
				final JMenu interpolation = new JMenu("Interpolation (" + row.track.getInterpTypeAsEnum() + ")");
				for (final InterpolationType type : InterpolationType.values()) {
					final JMenuItem entry = item(type.toString(), () -> setInterpolation(row.track, type));
					entry.setEnabled(type != row.track.getInterpTypeAsEnum());
					interpolation.add(entry);
				}
				menu.add(interpolation);
				if (row.track.hasGlobalSeq()) {
					menu.add(item("Detach from Global Sequence " + row.track.getGlobalSeq(),
							() -> detachGlobalSequence(row.track)));
				} else {
					menu.add(item("Convert to Global Sequence", () -> convertToGlobalSequence(row.track)));
				}
			}
			if (key != null) {
				menu.addSeparator();
				menu.add(item("Edit in Inspector", () -> {
					select(key, false);
					inspector.focusValue();
				}));
			}
			menu.show(this, point.x, point.y);
		}

		private static JMenuItem item(final String label, final Runnable action) {
			return new JMenuItem(new AbstractAction(label) {
				@Override
				public void actionPerformed(final ActionEvent e) {
					action.run();
				}
			});
		}

		// ---- painting ----

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;
			final int rowCount = tree.getRowCount();
			boolean mousedOverAnythingEditable = false;
			final Map<Integer, TrackRow> rowsByIndex = new HashMap<>();
			for (final TrackRow row : trackRows()) {
				rowsByIndex.put(row.row, row);
			}
			for (int i = 0; i < rowCount; i++) {
				final TrackRow trackRow = rowsByIndex.get(i);
				if (trackRow != null) {
					mousedOverAnythingEditable |= paintTrackRow(g2, trackRow);
					continue;
				}
				final TreePath pathForRow = tree.getPathForRow(i);
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
				final Object userObject = node.getUserObject();
				g.setColor(Color.GRAY);
				g.drawRect(0, ROW_HEIGHT * i, getWidth(), ROW_HEIGHT - 1);
				if (userObject instanceof ModelComponentAnimFlagTree.ChooseableModelRoot) {
					for (final Animation anim : modelView.getModel().getAnims()) {
						final int xEnd = xFromTime(anim.getStart());
						final int xStart = xFromTime(anim.getEnd());
						g.setColor(Color.RED.darker());
						g.drawLine(xStart, 0, xStart, getHeight());
						g.drawLine(xEnd, 0, xEnd, getHeight());
						g.setColor(Color.BLACK);
						final String animName = "\"" + anim.getName() + "\"";
						g.drawString(animName, xStart, ROW_HEIGHT * i + (ROW_HEIGHT + g.getFont().getSize()) / 2);
						g.drawString(animName, xEnd, ROW_HEIGHT * i + (ROW_HEIGHT + g.getFont().getSize()) / 2);
					}
				}
			}
			paintPlayhead(g2);
			if (mouseDragStart != null) {
				if (slidingKeys) {
					paintSlidePreview(g2);
				} else if (!popupPending) {
					g.setColor(Color.RED);
					g.drawRect(Math.min(mouseDragStart.x, mouseDragEnd.x), Math.min(mouseDragStart.y, mouseDragEnd.y),
							Math.abs(mouseDragStart.x - mouseDragEnd.x), Math.abs(mouseDragStart.y - mouseDragEnd.y));
				}
			} else {
				g.setColor(Color.GRAY);
				g.drawLine(mouseHoverX, 0, mouseHoverX, getHeight());
			}
			setCursor(mousedOverAnythingEditable ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : null);
		}

		/** @return true when the mouse hovers a selected key */
		private boolean paintTrackRow(final Graphics2D g, final TrackRow trackRow) {
			final int i = trackRow.row;
			final AnimFlag track = trackRow.track;
			final Set<Integer> selectedIndices = selectedTrackToIndices.get(track);
			final String name = track.getName();
			final boolean afHideShow = "Alpha".equals(name) || "Visibility".equals(name);
			final boolean afColor = "Color".equals(name) || name.endsWith("Color");
			final boolean translation = "Translation".equals(name);
			final boolean rotation = "Rotation".equals(name);
			final boolean scaling = "Scaling".equals(name);
			boolean mousedOverSelected = false;
			g.setColor(Color.BLACK);
			g.fillRect(0, ROW_HEIGHT * i, getWidth(), ROW_HEIGHT - 1);
			g.setColor(Color.GRAY);
			g.drawRect(0, ROW_HEIGHT * i, getWidth(), ROW_HEIGHT - 1);
			final ArrayList<Integer> times = track.getTimes();
			Object lastValue = null;
			int lastEndX = 0;
			final InterpolationType interpTypeAsEnum = track.getInterpTypeAsEnum();
			for (int j = 0; j < times.size(); j++) {
				final Integer time = times.get(j);
				final int x = xFromTime(time);
				final Rectangle rect = keyRect(i, time);
				final boolean mouseOver = rect.contains(mouseHoverX, mouseHoverY);
				final boolean selected = (selectedIndices != null) && selectedIndices.contains(j);
				mousedOverSelected |= mouseOver && selected;
				final Object value = track.getValues().get(j);
				// span bar between the previous key and this one for alpha and color
				if (afHideShow && (value instanceof Number)) {
					final float now = ((Number) value).floatValue();
					final float before = lastValue instanceof Number ? ((Number) lastValue).floatValue()
							: ((Number) track.getIdentity()).floatValue();
					if (interpTypeAsEnum == InterpolationType.DONT_INTERP) {
						g.setColor(new Color(0f, 1f, 1f, clamp01(before)));
					} else {
						g.setPaint(new GradientPaint(lastEndX, 0, new Color(0f, clamp01(before), clamp01(before), clamp01(before)),
								x - KEY_HALF_WIDTH, 0, new Color(0f, clamp01(now), clamp01(now), clamp01(now))));
					}
					g.fillRect(lastEndX, ROW_HEIGHT * i + ROW_HEIGHT / 4, x - KEY_HALF_WIDTH - lastEndX, ROW_HEIGHT / 2);
					lastValue = value;
				} else if (afColor && (value instanceof Vertex)) {
					final Color now = toColor((Vertex) value);
					final Color before = lastValue instanceof Vertex ? toColor((Vertex) lastValue)
							: toColor((Vertex) track.getIdentity());
					if (interpTypeAsEnum == InterpolationType.DONT_INTERP) {
						g.setColor(before);
					} else {
						g.setPaint(new GradientPaint(lastEndX, 0, before, x - KEY_HALF_WIDTH, 0, now));
					}
					g.fillRect(lastEndX, ROW_HEIGHT * i + ROW_HEIGHT / 4, x - KEY_HALF_WIDTH - lastEndX, ROW_HEIGHT / 2);
					lastValue = value;
				}
				// the key pill
				if (selected) {
					g.setPaint(keyframePaintHighlight);
				} else if (afHideShow && (value instanceof Number)) {
					final float v = ((Number) value).floatValue();
					if (v < 1.0f) {
						g.setColor(new Color(0f, 1f, 1f, clamp01(v)));
					} else {
						g.setPaint(keyframePaintTeal);
					}
				} else if (afColor && (value instanceof Vertex)) {
					g.setColor(toColor((Vertex) value));
				} else if (scaling) {
					g.setPaint(keyframePaintRed);
				} else if (translation) {
					g.setPaint(keyframePaintBlue);
				} else {
					g.setPaint(keyframePaint);
				}
				g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
				if (rotation && (value instanceof QuaternionRotation)) {
					paintRotationGlyph(g, (QuaternionRotation) value, rect);
				}
				Color outline = Color.GREEN;
				if (afHideShow) {
					outline = Color.CYAN;
				} else if (afColor) {
					outline = Color.GRAY;
				} else if (scaling) {
					outline = Color.ORANGE;
				} else if (translation) {
					outline = Color.BLUE;
				}
				g.setColor(mouseOver ? Color.WHITE : selected ? Color.RED : outline);
				g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
				lastEndX = x + KEY_HALF_WIDTH;
			}
			return mousedOverSelected;
		}

		/**
		 * A short needle whose angle is the rotation angle around the key's axis,
		 * so a row of rotation keys reads as a sequence of angles instead of
		 * identical pills.
		 */
		private static void paintRotationGlyph(final Graphics2D g, final QuaternionRotation q, final Rectangle rect) {
			final double angle = q.getAngleAroundAxis();
			final Vertex axis = q.getAxisOfRotation();
			final double signed = (axis != null) && (axis.z < 0) ? -angle : angle;
			final double cx = rect.getCenterX();
			final double cy = rect.getCenterY();
			final double r = rect.height / 2.0 - 1;
			final Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(0, 60, 0));
			g.setStroke(new BasicStroke(1.5f));
			g.drawLine((int) Math.round(cx), (int) Math.round(cy), (int) Math.round(cx + (r * Math.sin(signed))),
					(int) Math.round(cy - (r * Math.cos(signed))));
			g.setStroke(new BasicStroke(1f));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
		}

		private static float clamp01(final float f) {
			return Math.max(0f, Math.min(1f, f));
		}

		private static Color toColor(final Vertex v) {
			return new Color(clamp01((float) v.x), clamp01((float) v.y), clamp01((float) v.z));
		}

		private void paintPlayhead(final Graphics2D g) {
			if (timeEnvironment == null) {
				return;
			}
			final int x = xFromTime(playheadTime());
			g.setColor(new Color(255, 220, 0));
			g.drawLine(x, 0, x, getHeight());
		}

		private void paintSlidePreview(final Graphics2D g) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			final int timeDelta = timeFromX(mouseDragEnd.x) - timeFromX(mouseDragStart.x);
			for (final TrackRow row : trackRows()) {
				final Set<Integer> indices = selectedTrackToIndices.get(row.track);
				if (indices == null) {
					continue;
				}
				for (final Integer index : indices) {
					if (index >= row.track.getTimes().size()) {
						continue;
					}
					final int newTime = row.track.getTimes().get(index) + timeDelta;
					final Rectangle rect = keyRect(row.row, newTime);
					g.setPaint(keyframePaintHighlight);
					g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
					g.setColor(Color.RED);
					g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
					g.setColor(Color.WHITE);
					g.drawString(Integer.toString(newTime), rect.x + rect.width, rect.y + ROW_HEIGHT - 4);
				}
			}
		}

		@Override
		public void typeChanged(final SelectionMode newType) {
			this.selectionType = newType;
		}
	}
}
