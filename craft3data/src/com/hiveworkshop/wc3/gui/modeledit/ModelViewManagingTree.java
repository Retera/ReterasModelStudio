package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetComponentVisibilityAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetComponentVisibilityAction.VisibilityTarget;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.v2.ComponentVisibility;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

/**
 * The Outliner: every geoset, node and camera with two toggles per row, an
 * eye (drawn in the viewports) and a check (selectable and editable). Editable
 * implies visible, so turning the eye off also locks the row out of editing,
 * and turning the check on also shows it. Group rows apply to everything
 * beneath them. Every change is one undo action; changes that give or take
 * editability go through the model editor so the selection stays consistent.
 */
public final class ModelViewManagingTree extends JTree {
	private static final int GLYPH_SIZE = 16;
	private static final int GLYPH_COUNT = 2;

	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelEditorManager modelEditorManager;
	private ModelComponentNavigationListener navigationListener = ModelComponentNavigationListener.NONE;

	public ModelViewManagingTree(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager) {
		super(buildTreeModel(modelViewManager));
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelEditorManager = modelEditorManager;
		setToggleClickCount(0);
		setCellRenderer(new OutlinerCellRenderer());
		final HighlightOnMouseoverListenerImpl hoverListener = new HighlightOnMouseoverListenerImpl();
		addMouseMotionListener(hoverListener);
		addMouseListener(hoverListener);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				} else if (SwingUtilities.isLeftMouseButton(e)) {
					handleClick(e);
				}
			}
		});
	}

	public void setNavigationListener(final ModelComponentNavigationListener navigationListener) {
		this.navigationListener = navigationListener == null ? ModelComponentNavigationListener.NONE
				: navigationListener;
	}

	// ---- clicks ----

	private void handleClick(final MouseEvent e) {
		final TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path == null) {
			return;
		}
		final Rectangle bounds = getPathBounds(path);
		final OutlinerNode node = (OutlinerNode) path.getLastPathComponent();
		final int glyph = bounds == null ? -1 : (e.getX() - bounds.x) / GLYPH_SIZE;
		final List<OutlinerElement<?>> targets = node.componentElements();
		if (targets.isEmpty()) {
			return;
		}
		if (glyph == 0) {
			// eye: hide if anything is visible, else show (locked)
			boolean anyVisible = false;
			for (final OutlinerElement<?> element : targets) {
				anyVisible |= element.getVisibility().isVisible();
			}
			applyVisibility(targets, anyVisible ? ComponentVisibility.HIDDEN : ComponentVisibility.VISIBLE);
		} else {
			// check (or the label, as the old checkbox behaved): lock if everything is
			// editable, else make editable
			boolean allEditable = true;
			for (final OutlinerElement<?> element : targets) {
				allEditable &= element.getVisibility().isEditable();
			}
			applyVisibility(targets, allEditable ? ComponentVisibility.VISIBLE : ComponentVisibility.EDITABLE);
		}
	}

	/**
	 * Moves every element to the given state as one undoable step.
	 */
	private void applyVisibility(final List<OutlinerElement<?>> elements, final ComponentVisibility to) {
		final List<OutlinerElement<?>> gainingEditability = new ArrayList<>();
		final List<OutlinerElement<?>> losingEditability = new ArrayList<>();
		final List<OutlinerElement<?>> others = new ArrayList<>();
		for (final OutlinerElement<?> element : elements) {
			final ComponentVisibility from = element.getVisibility();
			if (from == to) {
				continue;
			}
			if (to.isEditable()) {
				gainingEditability.add(element);
			} else if (from.isEditable()) {
				losingEditability.add(element);
			} else {
				others.add(element);
			}
		}
		final Runnable refresh = this::repaint;
		final List<UndoAction> actions = new ArrayList<>();
		if (!gainingEditability.isEmpty()) {
			actions.add(modelEditorManager.getModelEditor()
					.showComponent(new TransitionHandler(gainingEditability, to)));
		}
		if (!losingEditability.isEmpty()) {
			actions.add(modelEditorManager.getModelEditor().hideComponent(
					ListView.Util.of(losingEditability.toArray(new SelectableComponent[0])),
					new TransitionHandler(losingEditability, to), refresh));
		}
		if (!others.isEmpty()) {
			final SetComponentVisibilityAction action = new SetComponentVisibilityAction(others, to, refresh);
			action.redo();
			actions.add(action);
		}
		if (actions.size() == 1) {
			undoActionListener.pushAction(actions.get(0));
		} else if (actions.size() > 1) {
			undoActionListener.pushAction(new CompoundAction(actions.get(0).actionName(),
					ListView.Util.of(actions.toArray(new UndoAction[0]))));
		}
		repaint();
	}

	/**
	 * Adapts a tri-state transition to the two-state contract of the model
	 * editor's show/hide pipeline. When the target state is editable,
	 * makeEditable applies it and makeNotEditable restores each element's old
	 * state; when editability is being taken away it is the other way round.
	 */
	private static final class TransitionHandler implements EditabilityToggleHandler {
		private final List<OutlinerElement<?>> elements;
		private final Map<OutlinerElement<?>, ComponentVisibility> before = new HashMap<>();
		private final ComponentVisibility to;

		TransitionHandler(final List<OutlinerElement<?>> elements, final ComponentVisibility to) {
			this.elements = elements;
			this.to = to;
			for (final OutlinerElement<?> element : elements) {
				before.put(element, element.getVisibility());
			}
		}

		@Override
		public void makeEditable() {
			for (final OutlinerElement<?> element : elements) {
				element.setVisibility(to.isEditable() ? to : before.get(element));
			}
		}

		@Override
		public void makeNotEditable() {
			for (final OutlinerElement<?> element : elements) {
				element.setVisibility(to.isEditable() ? before.get(element) : to);
			}
		}
	}

	// ---- popup ----

	private void showPopup(final MouseEvent e) {
		final TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path == null) {
			return;
		}
		setSelectionPath(path);
		final OutlinerNode node = (OutlinerNode) path.getLastPathComponent();
		final List<OutlinerElement<?>> targets = node.componentElements();
		if (targets.isEmpty()) {
			return;
		}
		final JPopupMenu menu = new JPopupMenu();
		final boolean group = !node.isComponent();
		final String suffix = group ? " (all beneath)" : "";
		menu.add(item("Editable" + suffix, () -> applyVisibility(targets, ComponentVisibility.EDITABLE)));
		menu.add(item("Visible, locked" + suffix, () -> applyVisibility(targets, ComponentVisibility.VISIBLE)));
		menu.add(item("Hidden" + suffix, () -> applyVisibility(targets, ComponentVisibility.HIDDEN)));
		menu.addSeparator();
		final List<OutlinerElement<?>> category = node.categoryElements();
		if (node.isComponent()) {
			menu.add(item("Show Only This", () -> {
				final List<OutlinerElement<?>> rest = new ArrayList<>(category);
				rest.removeAll(targets);
				applyVisibility(rest, ComponentVisibility.HIDDEN);
				applyVisibility(targets, ComponentVisibility.EDITABLE);
			}));
		}
		menu.add(item("Show All", () -> applyVisibility(allComponentElements(), ComponentVisibility.EDITABLE)));
		if (node.isComponent()) {
			final Object item = node.element().item;
			menu.addSeparator();
			menu.add(item("Select in Model Tab", () -> navigationListener.openInModelTab(item)));
			menu.add(item("Open in Tracks", () -> navigationListener.openInTracks(item)));
		}
		menu.show(this, e.getX(), e.getY());
	}

	private static JMenuItem item(final String label, final Runnable action) {
		return new JMenuItem(new AbstractAction(label) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				action.run();
			}
		});
	}

	private List<OutlinerElement<?>> allComponentElements() {
		return ((OutlinerNode) getModel().getRoot()).componentElements();
	}

	// ---- model ----

	public void reloadFromModelView() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final TreePath rootPath = new TreePath(getModel().getRoot());
				final Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
				setModel(buildTreeModel(modelViewManager));
				final TreePath newRootPath = new TreePath(getModel().getRoot());
				final List<TreePath> pathsToExpand = new ArrayList<>();
				while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
					final TreePath nextPathToExpand = expandedDescendants.nextElement();
					TreePath newPathWithNewObjects = newRootPath;
					OutlinerNode currentNode = (OutlinerNode) getModel().getRoot();
					for (int i = 1; i < nextPathToExpand.getPathCount(); i++) {
						final OutlinerNode pathComponent = (OutlinerNode) nextPathToExpand.getPathComponent(i);
						boolean foundMatchingChild = false;
						for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
							final OutlinerNode childAt = (OutlinerNode) currentNode.getChildAt(j);
							if (childAt.element().hasSameItem(pathComponent.element())) {
								currentNode = childAt;
								newPathWithNewObjects = newPathWithNewObjects.pathByAddingChild(childAt);
								foundMatchingChild = true;
							}
						}
						if (!foundMatchingChild) {
							break;
						}
					}
					pathsToExpand.add(newPathWithNewObjects);
				}
				for (final TreePath path : pathsToExpand) {
					expandPath(path);
				}
			}
		});
	}

	/**
	 * Expands to, scrolls to and selects the row showing the given geoset, node
	 * or camera.
	 */
	public void scrollToObject(final Object object) {
		final Object root = getModel().getRoot();
		if (root instanceof OutlinerNode) {
			final TreePath path = findPathByItem((OutlinerNode) root, object);
			if (path != null) {
				if (path.getParentPath() != null) {
					expandPath(path.getParentPath());
				}
				scrollPathToVisible(path);
				setSelectionPath(path);
			}
		}
	}

	private TreePath findPathByItem(final OutlinerNode node, final Object object) {
		if (node.element().item == object) {
			return new TreePath(node.getPath());
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			final TreePath path = findPathByItem((OutlinerNode) node.getChildAt(i), object);
			if (path != null) {
				return path;
			}
		}
		return null;
	}

	private static DefaultTreeModel buildTreeModel(final ModelViewManager modelViewManager) {
		final OutlinerNode root = new OutlinerNode(new ModelElement(modelViewManager));
		final OutlinerNode mesh = new OutlinerNode(new GroupElement(modelViewManager, "Mesh"));
		for (final Geoset geoset : modelViewManager.getModel().getGeosets()) {
			mesh.add(new OutlinerNode(new GeosetElement(modelViewManager, geoset)));
		}
		if (mesh.getChildCount() > 0) {
			root.add(mesh);
		}
		final Map<IdObject, OutlinerNode> nodeToTreeElement = new HashMap<>();
		final Map<IdObject, List<OutlinerNode>> nodeToChildrenAwaitingLink = new HashMap<>();
		final OutlinerNode nodes = new OutlinerNode(new GroupElement(modelViewManager, "Nodes"));
		nodeToTreeElement.put(null, nodes);
		for (final IdObject object : modelViewManager.getModel().getIdObjects()) {
			final OutlinerNode treeNode = new OutlinerNode(new NodeElement(modelViewManager, object));
			nodeToTreeElement.put(object, treeNode);
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			final OutlinerNode parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode == null) {
				List<OutlinerNode> awaiting = nodeToChildrenAwaitingLink.get(parent);
				if (awaiting == null) {
					awaiting = new ArrayList<>();
					nodeToChildrenAwaitingLink.put(parent, awaiting);
				}
				awaiting.add(treeNode);
			} else {
				parentTreeNode.add(treeNode);
			}
			final List<OutlinerNode> childrenNeedingLink = nodeToChildrenAwaitingLink.get(object);
			if (childrenNeedingLink != null) {
				for (final OutlinerNode child : childrenNeedingLink) {
					treeNode.add(child);
				}
			}
		}
		if (nodes.getChildCount() > 0) {
			root.add(nodes);
		}
		final OutlinerNode cameras = new OutlinerNode(new GroupElement(modelViewManager, "Cameras"));
		for (final Camera camera : modelViewManager.getModel().getCameras()) {
			cameras.add(new OutlinerNode(new CameraElement(modelViewManager, camera)));
		}
		if (cameras.getChildCount() > 0) {
			root.add(cameras);
		}
		return new DefaultTreeModel(root);
	}

	// ---- tree nodes ----

	private static final class OutlinerNode extends DefaultMutableTreeNode {
		OutlinerNode(final OutlinerElement<?> element) {
			super(element);
		}

		OutlinerElement<?> element() {
			return (OutlinerElement<?>) getUserObject();
		}

		boolean isComponent() {
			return element().isComponent();
		}

		/** This row's component plus every component beneath it, in tree order. */
		List<OutlinerElement<?>> componentElements() {
			final List<OutlinerElement<?>> result = new ArrayList<>();
			collect(this, result);
			return result;
		}

		private static void collect(final OutlinerNode node, final List<OutlinerElement<?>> into) {
			if (node.isComponent()) {
				into.add(node.element());
			}
			for (int i = 0; i < node.getChildCount(); i++) {
				collect((OutlinerNode) node.getChildAt(i), into);
			}
		}

		/** Every component in the same top-level group (Mesh, Nodes or Cameras). */
		List<OutlinerElement<?>> categoryElements() {
			OutlinerNode top = this;
			while ((top.getParent() != null) && (top.getParent().getParent() != null)) {
				top = (OutlinerNode) top.getParent();
			}
			return top.componentElements();
		}
	}

	// ---- rendering ----

	private enum GlyphState {
		OFF, ON, PARTIAL
	}

	private static GlyphState aggregate(final List<OutlinerElement<?>> elements, final boolean editability) {
		int on = 0;
		for (final OutlinerElement<?> element : elements) {
			final ComponentVisibility state = element.getVisibility();
			if (editability ? state.isEditable() : state.isVisible()) {
				on++;
			}
		}
		if (on == 0) {
			return GlyphState.OFF;
		}
		return on == elements.size() ? GlyphState.ON : GlyphState.PARTIAL;
	}

	private final class OutlinerCellRenderer extends JPanel implements TreeCellRenderer {
		private final GlyphComponent glyphs = new GlyphComponent();
		private final JLabel label = new JLabel();

		OutlinerCellRenderer() {
			super(new BorderLayout(2, 0));
			setOpaque(false);
			add(glyphs, BorderLayout.WEST);
			add(label, BorderLayout.CENTER);
			label.setOpaque(true);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
				final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
			final OutlinerNode node = (OutlinerNode) value;
			final List<OutlinerElement<?>> elements = node.componentElements();
			if (elements.isEmpty()) {
				glyphs.eye = GlyphState.OFF;
				glyphs.check = GlyphState.OFF;
				glyphs.enabled = false;
			} else {
				glyphs.eye = aggregate(elements, false);
				glyphs.check = aggregate(elements, true);
				glyphs.enabled = true;
			}
			label.setText(node.element().toString());
			label.setFont(tree.getFont());
			if (selected) {
				label.setBackground(UIManager.getColor("Tree.selectionBackground"));
				label.setForeground(UIManager.getColor("Tree.selectionForeground"));
			} else {
				label.setBackground(tree.getBackground());
				label.setForeground(tree.getForeground());
			}
			return this;
		}
	}

	private static final class GlyphComponent extends JComponent {
		GlyphState eye = GlyphState.OFF;
		GlyphState check = GlyphState.OFF;
		boolean enabled = true;

		GlyphComponent() {
			setPreferredSize(new Dimension(GLYPH_SIZE * GLYPH_COUNT, GLYPH_SIZE));
			setOpaque(false);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			if (!enabled) {
				return;
			}
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				paintEye(g2, 0, eye);
				paintCheck(g2, GLYPH_SIZE, check);
			} finally {
				g2.dispose();
			}
		}

		private static Color colorFor(final GlyphState state) {
			switch (state) {
			case ON:
				return new Color(30, 30, 30);
			case PARTIAL:
				return new Color(30, 30, 30, 110);
			default:
				return new Color(150, 150, 150, 120);
			}
		}

		private static void paintEye(final Graphics2D g, final int x, final GlyphState state) {
			g.setColor(colorFor(state));
			final int cx = x + (GLYPH_SIZE / 2);
			final int cy = GLYPH_SIZE / 2;
			// almond outline
			g.drawArc(x + 2, cy - 4, GLYPH_SIZE - 4, 8, 0, 180);
			g.drawArc(x + 2, cy - 4, GLYPH_SIZE - 4, 8, 180, 180);
			if (state != GlyphState.OFF) {
				g.fillOval(cx - 2, cy - 2, 5, 5);
			} else {
				// closed eye: a strike through
				g.drawLine(x + 3, cy + 5, x + GLYPH_SIZE - 3, cy - 5);
			}
		}

		private static void paintCheck(final Graphics2D g, final int x, final GlyphState state) {
			g.setColor(colorFor(state == GlyphState.OFF ? GlyphState.OFF : GlyphState.ON));
			g.drawRect(x + 2, 2, GLYPH_SIZE - 5, GLYPH_SIZE - 5);
			if (state == GlyphState.ON) {
				g.drawLine(x + 5, 8, x + 7, 11);
				g.drawLine(x + 7, 11, x + 12, 4);
			} else if (state == GlyphState.PARTIAL) {
				g.setColor(colorFor(GlyphState.PARTIAL));
				g.fillRect(x + 5, 5, GLYPH_SIZE - 10, GLYPH_SIZE - 10);
			}
		}
	}

	// ---- hover highlight ----

	private final class HighlightOnMouseoverListenerImpl extends MouseAdapter {
		private OutlinerElement<?> lastMouseOverNode = null;

		@Override
		public void mouseMoved(final MouseEvent mouseEvent) {
			final TreePath pathForLocation = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			final OutlinerElement<?> element = pathForLocation == null ? null
					: ((OutlinerNode) pathForLocation.getLastPathComponent()).element();
			if (element != lastMouseOverNode) {
				if (lastMouseOverNode != null) {
					lastMouseOverNode.mouseExited();
				}
				if (element != null) {
					element.mouseEntered();
				}
				lastMouseOverNode = element;
			}
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			if (lastMouseOverNode != null) {
				lastMouseOverNode.mouseExited();
				lastMouseOverNode = null;
			}
		}
	}

	// ---- elements ----

	private static abstract class OutlinerElement<T> implements SelectableComponent, VisibilityTarget {
		protected final ModelViewManager modelViewManager;
		protected final T item;

		OutlinerElement(final ModelViewManager modelViewManager, final T item) {
			this.modelViewManager = modelViewManager;
			this.item = item;
		}

		boolean isComponent() {
			return true;
		}

		abstract void mouseEntered();

		abstract void mouseExited();

		protected abstract String getName(T item, ModelViewManager modelViewManager);

		@Override
		public String toString() {
			return getName(item, modelViewManager);
		}

		boolean hasSameItem(final OutlinerElement<?> other) {
			return (getClass() == other.getClass())
					&& ((other.item == item) || ((item != null) && item.equals(other.item)));
		}
	}

	private static final class GeosetElement extends OutlinerElement<Geoset> {
		GeosetElement(final ModelViewManager modelViewManager, final Geoset item) {
			super(modelViewManager, item);
		}

		@Override
		public ComponentVisibility getVisibility() {
			return modelViewManager.getGeosetVisibility(item);
		}

		@Override
		public void setVisibility(final ComponentVisibility state) {
			modelViewManager.setGeosetVisibility(item, state);
		}

		@Override
		protected String getName(final Geoset item, final ModelViewManager modelViewManager) {
			return item.getUIName(modelViewManager.getModel());
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		void mouseEntered() {
			modelViewManager.highlightGeoset(item);
		}

		@Override
		void mouseExited() {
			modelViewManager.unhighlightGeoset(item);
		}
	}

	private static final class NodeElement extends OutlinerElement<IdObject> {
		NodeElement(final ModelViewManager modelViewManager, final IdObject item) {
			super(modelViewManager, item);
		}

		@Override
		public ComponentVisibility getVisibility() {
			return modelViewManager.getIdObjectVisibility(item);
		}

		@Override
		public void setVisibility(final ComponentVisibility state) {
			modelViewManager.setIdObjectVisibility(item, state);
		}

		@Override
		protected String getName(final IdObject item, final ModelViewManager modelViewManager) {
			return item.getClass().getSimpleName() + " \"" + item.getName() + "\"";
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		void mouseEntered() {
			modelViewManager.highlightNode(item);
		}

		@Override
		void mouseExited() {
			modelViewManager.unhighlightNode(item);
		}
	}

	private static final class CameraElement extends OutlinerElement<Camera> {
		CameraElement(final ModelViewManager modelViewManager, final Camera item) {
			super(modelViewManager, item);
		}

		@Override
		public ComponentVisibility getVisibility() {
			return modelViewManager.getCameraVisibility(item);
		}

		@Override
		public void setVisibility(final ComponentVisibility state) {
			modelViewManager.setCameraVisibility(item, state);
		}

		@Override
		protected String getName(final Camera item, final ModelViewManager modelViewManager) {
			return item.getName();
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		void mouseEntered() {
		}

		@Override
		void mouseExited() {
		}
	}

	/** A heading row; its state is the aggregate of the rows beneath it. */
	private static class GroupElement extends OutlinerElement<String> {
		GroupElement(final ModelViewManager modelViewManager, final String name) {
			super(modelViewManager, name);
		}

		@Override
		boolean isComponent() {
			return false;
		}

		@Override
		public ComponentVisibility getVisibility() {
			return ComponentVisibility.HIDDEN;
		}

		@Override
		public void setVisibility(final ComponentVisibility state) {
		}

		@Override
		protected String getName(final String item, final ModelViewManager modelViewManager) {
			return item;
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
		}

		@Override
		void mouseEntered() {
		}

		@Override
		void mouseExited() {
		}
	}

	private static final class ModelElement extends GroupElement {
		ModelElement(final ModelViewManager modelViewManager) {
			super(modelViewManager, "");
		}

		@Override
		protected String getName(final String item, final ModelViewManager modelViewManager) {
			return modelViewManager.getModel().getHeaderName();
		}

		@Override
		boolean hasSameItem(final OutlinerElement<?> other) {
			return other instanceof ModelElement;
		}
	}
}
