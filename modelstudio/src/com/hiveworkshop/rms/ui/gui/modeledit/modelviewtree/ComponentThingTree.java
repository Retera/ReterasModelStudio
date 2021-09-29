package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTreeNode;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

/**
 * This code is taken from a Stack Overflow post:
 * https://stackoverflow.com/questions/21847411/java-swing-need-a-good-quality-developed-jtree-with-checkboxes
 *
 * Thanks to the creators!
 */
public class ComponentThingTree extends JTree {

	private static final long serialVersionUID = -4194122328392241790L;

	// Defining a new event type for the checking mechanism and preparing event-handling mechanism
	protected EventListenerList listenerList = new EventListenerList();
	ComponentTreeNode<EditableModel> root;
	ComponentTreeNode<Geoset> meshes;
	ComponentTreeNode<IdObject> nodes;
//	ComponentTreeNode<Camera> cameras;

	HashSet<TreeNode> checkedPaths = new HashSet<>();
	private boolean controlDown = false;
	protected ModelHandler modelHandler;
	protected ModelView modelView;
	protected UndoManager undoManager;

	private final Map<IdObject, ComponentTreeNode<IdObject>> nodeToTreeElement = new HashMap<>();

	public ComponentThingTree() {
		super();
		// Disabling toggling by double-click
		setToggleClickCount(0);
		setOpaque(false);
		setEditable(true);

		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);

//		addMouseListener(getMouseListener());
		addKeyListener(getKeyAdapter());
		addTreeExpansionListener(getExpansionListener());
	}

	public ComponentThingTree setModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			this.modelView = modelHandler.getModelView();
			this.undoManager = modelHandler.getUndoManager();

		} else {
			this.modelView = null;
			this.undoManager = null;
		}
		uggugg();
		System.out.println("ComponentThingTree#setModel: buildTreeModel");
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
		System.out.println("ComponentThingTree#setModel: setTreeModel");
		setModel(treeModel);
		TreeCellRenderer cellRenderer = new TreeCellRenderer() {
			//			JPanel panel = new JPanel(new MigLayout("gap 0, ins 0"));
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				if (value instanceof ComponentTreeNode) {
//					panel.add(((ComponentTreeNode<?>) value).getTreeRenderComponent());
//					JButton button = new JButton("ugg");
//					button.addActionListener(e -> System.out.println("UGG!"));
//					panel.add(button);
//					return panel;
					return ((ComponentTreeNode<?>) value).getTreeRenderComponent();
				}
				return new JLabel("ugg");
			}
		};
		setCellRenderer(cellRenderer);
//		setCellEditor(getCellEditor1());
		setCellEditor(new ButtonCellEditor());
		revalidate();
		repaint();
		return this;
	}

	private void uggugg() {
		root = new ComponentTreeNode<>(modelHandler, modelHandler.getModel());
		meshes = new ComponentTreeNode<>(modelHandler, new Geoset());
		meshes.setVisible1(true);
		meshes.setEditable1(true);
		nodes = new ComponentTreeNode<>(modelHandler, new Helper());
		nodes.setVisible1(true);
		nodes.setEditable1(true);
//		cameras = new ComponentTreeNode<>(modelView, new Camera());
	}

	public ComponentThingTree reloadTree() {
		TreePath rootPath = new TreePath(getModel().getRoot());
		Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
		System.out.println("ComponentThingTree#setModel: setTreeModel");
		setModel(treeModel);

		expandTree(expandedDescendants);
		return this;
	}

	private void expandTree(Enumeration<TreePath> expandedDescendants) {
		TreePath newRootPath = new TreePath(getModel().getRoot());
		List<TreePath> pathsToExpand = new ArrayList<>();

		while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
			TreePath nextPathToExpand = expandedDescendants.nextElement();

			TreePath newPathWithNewObjects = newRootPath;
			ComponentTreeNode<?> currentNode = (ComponentTreeNode<?>) getModel().getRoot();
			newPathWithNewObjects = getTreePath(nextPathToExpand, newPathWithNewObjects, currentNode);

			pathsToExpand.add(newPathWithNewObjects);
		}
		for (final TreePath path : pathsToExpand) {
			expandPath(path);
		}
	}

	private TreePath getTreePath(TreePath nextPathToExpand, TreePath newPathWithNewObjects, ComponentTreeNode<?> currentNode) {
		for (int i = 1; i < nextPathToExpand.getPathCount(); i++) {
			ComponentTreeNode<?> pathComponent = (ComponentTreeNode<?>) nextPathToExpand.getPathComponent(i);
			boolean foundMatchingChild = false;
			for (int j = 0; j < currentNode.getChildCount() && foundMatchingChild == false; j++) {
				ComponentTreeNode<?> childAt = (ComponentTreeNode<?>) currentNode.getChildAt(j);
				if (childAt.getItem() == pathComponent.getItem()) {
					currentNode = childAt;
					newPathWithNewObjects = newPathWithNewObjects.pathByAddingChild(childAt);
					foundMatchingChild = true;
				}
			}
			if (foundMatchingChild == false) {
				break;
			}
		}
		return newPathWithNewObjects;
	}


	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {
		root.removeAllChildren();
		meshes.removeAllChildren();
		nodes.removeAllChildren();
//		cameras.removeAllChildren();

		if (modelHandler != null) {
			buildGeosetTree(modelHandler);
			if (meshes.getChildCount() > 0) {
				root.add(meshes);
			}

			buildNodeTree(modelHandler);
			if (nodes.getChildCount() > 0) {
				root.add(nodes);
			}

//			buildCameraTree(modelHandler);
//			if (cameras.getChildCount() > 0) {
//				root.add(cameras);
//			}
		}

		return new DefaultTreeModel(root);
	}


	private void buildNodeTree(ModelHandler modelHandler) {
		updateNodeToElement(modelHandler);

		for (IdObject object : modelHandler.getModel().getIdObjects()) {
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			ComponentTreeNode<IdObject> parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode != null) {
				parentTreeNode.add(nodeToTreeElement.get(object));
			}
		}
	}

	private void buildGeosetTree(ModelHandler modelHandler) {
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			meshes.add(new ComponentTreeNode<>(modelHandler, geoset));
		}
	}

	private void updateNodeToElement(ModelHandler modelHandler) {
		nodeToTreeElement.clear();

		nodeToTreeElement.put(null, nodes);

		EditableModel model = modelHandler.getModel();
		for (IdObject object : model.getIdObjects()) {
			nodeToTreeElement.put(object, new ComponentTreeNode<>(modelHandler, object));

		}
	}


	private TreeExpansionListener getExpansionListener() {
		return new TreeExpansionListener() {
			boolean isSt = false;

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if (controlDown && (!isSt)) {
					isSt = true;
					if (event.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
						expandAllChildren(lastPathComponent, event.getPath(), true);
					}
					isSt = false;
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				if (controlDown && (!isSt)) {
					isSt = true;
					if (event.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
						expandAllChildren(lastPathComponent, event.getPath(), false);
					}
					isSt = false;
				}
			}
		};
	}

	private void expandAllChildren(TreeNode node, TreePath path, boolean expand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			expandAllChildren(child, path.pathByAddingChild(child), expand);
		}
		if (expand) {
			expandPath(path);
		} else {
			collapsePath(path);
		}
	}

	private MouseListener getMouseListener() {
		// Calling checking mechanism on mouse click
		return new MouseAdapter() {
			SelectionBundle newSelection;

			@Override
			public void mouseClicked(final MouseEvent e) {
				super.mousePressed(e);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				super.mousePressed(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				super.mousePressed(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);
				int modifiersEx = e.getModifiersEx();
				if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0) {
					newSelection = new SelectionBundle();
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				super.mousePressed(e);
				System.out.println("CT: mouse released over : " + getPathForLocation(e.getX(), e.getY()));
				System.out.println("CT: mouse released over : " + e.getSource() + ", consumed: " + e.isConsumed());
				if (e.getSource() instanceof ComponentTreeNode) {
					ComponentTreeNode<?> source = (ComponentTreeNode<?>) e.getSource();
					Object userObject = source.getUserObject();
					if (userObject instanceof IdObject) {
						IdObject userObject1 = (IdObject) userObject;
						newSelection = new SelectionBundle(Collections.singleton(userObject1));
					}
//					((ComponentTreeNode<?>)e.getSource()).

				}
				int modifiersEx = e.getModifiersEx();
				if ((ProgramGlobals.getPrefs().getSelectMouseButton() & modifiersEx) > 0 && newSelection != null) {
					Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
					Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();

					if (modifiersEx == addSelectModifier) {
//						SelectionMode.ADD;
						if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
							undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView).redo());
						}
					} else if (modifiersEx == removeSelectModifier) {
//						SelectionMode.DESELECT;
						if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
							undoManager.pushAction(new RemoveSelectionUggAction(newSelection, modelView).redo());
						}
					} else {
//						SelectionMode.SELECT;
						if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
							undoManager.pushAction(new SetSelectionUggAction(newSelection, modelView).redo());
						}
					}
				}
				newSelection = null;
			}
		};
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL && !controlDown) System.out.println("controll down");
				controlDown = e.isControlDown();
				super.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				controlDown = e.isControlDown();
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) System.out.println("controll up");
				super.keyReleased(e);
			}
		};
	}


	protected void checkSubTree(final TreePath tp, int depth, final boolean check) {
		final JCheckBoxTreeNode cn = (JCheckBoxTreeNode) (tp.getLastPathComponent());
		cn.setChecked(check);
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();

		if (depth == -1) {
			depth = node.getDepth() + 1;
		}

		int thisDepth = depth - 1;
		for (int i = 0; i < node.getChildCount() && depth > 0; i++) {
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), thisDepth, check);
		}

		if (check) {
			checkedPaths.add(cn);
		} else {
			checkedPaths.remove(cn);
		}
	}

	private TreeCellEditor getCellEditor1() {
		return new TreeCellEditor() {
			@Override
			public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
				if (value instanceof ComponentTreeNode) {
//					panel.add(((ComponentTreeNode<?>) value).getTreeRenderComponent());
//					JButton button = new JButton("ugg");
//					button.addActionListener(e -> System.out.println("UGG!"));
//					panel.add(button);
//					return panel;
					return ((ComponentTreeNode<?>) value).getTreeRenderComponent();
				}
				return new JLabel("ugg");
			}

			@Override
			public Object getCellEditorValue() {
				System.out.println("editorCellValue");
				return null;
			}

			@Override
			public boolean isCellEditable(EventObject anEvent) {
				return true;
			}

			@Override
			public boolean shouldSelectCell(EventObject anEvent) {
				return true;
			}

			@Override
			public boolean stopCellEditing() {
//				fireEditingStopped();
				return true;
			}

			@Override
			public void cancelCellEditing() {
//				fireEditingCanceled();
			}

			@Override
			public void addCellEditorListener(CellEditorListener l) {
				listenerList.add(CellEditorListener.class, l);
			}

			@Override
			public void removeCellEditorListener(CellEditorListener l) {
				listenerList.remove(CellEditorListener.class, l);
			}
		};
	}

	public static class ButtonCellEditor extends AbstractCellEditor implements TreeCellEditor {
		public ButtonCellEditor() {
		}

		@Override
		public Object getCellEditorValue() {
			System.out.println("ButtonCellEditor#getCellEditorValue");
			return "";
//			return value.toString();
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			boolean cellEditable = super.isCellEditable(anEvent);
			System.out.println("ButtonCellEditor#isCellEditable: " + cellEditable);
			return cellEditable;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			boolean b = super.shouldSelectCell(anEvent);
			System.out.println("ButtonCellEditor#shouldSelectCell: " + b + " event: " + anEvent);
			return b;
		}

		@Override
		public boolean stopCellEditing() {
			boolean b = super.stopCellEditing();
			System.out.println("ButtonCellEditor#stopCellEditing: " + b);
//				fireEditingStopped();
			return b;
		}

		@Override
		public void cancelCellEditing() {
			System.out.println("ButtonCellEditor#cancelCellEditing");
			super.cancelCellEditing();
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			System.out.println("ButtonCellEditor#getTreeCellEditorComponent");
			if (value instanceof ComponentTreeNode) {
				System.out.println("returning renderComp");
				return ((ComponentTreeNode<?>) value).getTreeRenderComponent();
			}
			return null;
		}

	}

}