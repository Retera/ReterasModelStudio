package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public final class ModelComponentBrowserTree extends JTree {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private Map<IdObject, DefaultMutableTreeNode> nodeToTreeElement;
	private boolean controlDown = false;

	public ModelComponentBrowserTree(final ModelViewManager modelViewManager,
	                                 final UndoActionListener undoActionListener, final ModelEditorManager modelEditorManager,
	                                 final ModelStructureChangeListener modelStructureChangeListener) {
		super();
		setModel(buildTreeModel(modelViewManager, undoActionListener, modelStructureChangeListener));
//		super(buildTreeModel(modelViewManager, undoActionListener, modelStructureChangeListener));

		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;


		addKeyListener(getKeyAdapter());

		final HighlightOnMouseoverListenerImpl mouseListener = new HighlightOnMouseoverListenerImpl();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addTreeExpansionListener(getExpansionListener());

		setCellRenderer(getComponentBrowserCellRenderer());
//		setFocusable(false);


		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(8);
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL && !controlDown) System.out.println("controll down");
				controlDown = e.isControlDown();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				controlDown = e.isControlDown();
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) System.out.println("controll up");
			}
		};
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

	private DefaultTreeModel buildTreeModel(
			final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {

		EditableModel model = modelViewManager.getModel();

		final DispElements.ChooseableModelRoot modelRoot = new DispElements.ChooseableModelRoot(modelViewManager, model);
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(modelRoot);
		int number = 0;

		DispElements.ChooseableModelComment modelComment = new DispElements.ChooseableModelComment(modelViewManager, model.getHeader());
//		System.out.println(model.getHeader());
//		System.out.println(model.getName());
//		System.out.println(model.getWrappedDataSource());
		root.add(new DefaultMutableTreeNode(modelComment));

		DispElements.ChooseableModelHeader modelHeader = new DispElements.ChooseableModelHeader(modelViewManager, model);
		root.add(new DefaultMutableTreeNode(modelHeader));

		DefaultMutableTreeNode sequences = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Sequences"));
		for (Animation item : model.getAnims()) {
			sequences.add(new DefaultMutableTreeNode(new DispElements.ChooseableAnimationItem(modelViewManager, item)));
		}
		root.add(sequences);

		final DefaultMutableTreeNode globalSequences = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "GlobalSequences"));
		for (int globalSeqId = 0; globalSeqId < model.getGlobalSeqs().size(); globalSeqId++) {
			DispElements.ChooseableGlobalSequenceItem sequenceItem = new DispElements.ChooseableGlobalSequenceItem(modelViewManager, model.getGlobalSeq(globalSeqId), globalSeqId);
			globalSequences.add(new DefaultMutableTreeNode(sequenceItem));
		}
		root.add(globalSequences);

		number = 0;
		final DefaultMutableTreeNode textures = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Textures"));
		for (final Bitmap item : model.getTextures()) {
			textures.add(new DefaultMutableTreeNode(new DispElements.ChooseableBitmapItem(modelViewManager, item, number++)));
		}
		root.add(textures);

		number = 0;
		final DefaultMutableTreeNode materials = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Materials"));
		for (final Material item : model.getMaterials()) {
			materials.add(new DefaultMutableTreeNode(new DispElements.ChooseableMaterialItem(modelViewManager, item, number++)));
		}
		root.add(materials);

		number = 0;
		final DefaultMutableTreeNode tVertexAnims = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "TVertexAnims"));
		for (final TextureAnim item : model.getTexAnims()) {
			DispElements.ChooseableTextureAnimItem textureAnimItem = new DispElements.ChooseableTextureAnimItem(modelViewManager, item, number++);
			tVertexAnims.add(new DefaultMutableTreeNode(textureAnimItem));
		}
		root.add(tVertexAnims);

		number = 0;
		final DefaultMutableTreeNode geosets = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Geosets"));
		for (final Geoset item : model.getGeosets()) {
			DispElements.ChooseableGeosetItem geosetItem = new DispElements.ChooseableGeosetItem(modelViewManager, item, number++);
			geosets.add(new DefaultMutableTreeNode(geosetItem));
		}
		root.add(geosets);

		final DefaultMutableTreeNode geosetAnims = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "GeosetAnims"));
		for (final GeosetAnim item : model.getGeosetAnims()) {
			DispElements.ChooseableGeosetAnimItem geosetAnimItem = new DispElements.ChooseableGeosetAnimItem(modelViewManager, item, number++);
			geosetAnims.add(new DefaultMutableTreeNode(geosetAnimItem));
		}
		root.add(geosetAnims);

//		final DispElements.IdObjectToChooseableElementWrappingConverter converter =
//				new DispElements.IdObjectToChooseableElementWrappingConverter(
//						modelViewManager, undoActionListener, modelStructureChangeListener);


		final DefaultMutableTreeNode nodes = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Nodes"));

//		Map<IdObject, DefaultMutableTreeNode> nodeToTreeElement = new HashMap<>();
		nodeToTreeElement = new HashMap<>();
		nodeToTreeElement.put(null, nodes);

		// Create all treeNodes
		for (final IdObject idObject : model.getIdObjects()) {
//			idObject.apply(converter);
//			final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(converter.getElement());
			ChooseableDisplayElement<?> element = DispElements.getIdObjectElement(modelViewManager, idObject);
			final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(element);

			nodeToTreeElement.put(idObject, treeNode);
		}

		// Link all treeNodes
		for (final IdObject idObject : model.getIdObjects()) {
			IdObject parent = idObject.getParent();

			if (parent == idObject) {
				parent = null;
			}

			final DefaultMutableTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			parentTreeNode.add(nodeToTreeElement.get(idObject));
		}

		root.add(nodes);

		final DefaultMutableTreeNode cameras = new DefaultMutableTreeNode(DispElements.getDummyItem(modelViewManager, "Cameras"));
		for (final Camera item : model.getCameras()) {
			cameras.add(new DefaultMutableTreeNode(new DispElements.ChooseableCameraItem(modelViewManager, item)));
		}
		root.add(cameras);

		number = 0;
		if (!model.getFaceEffects().isEmpty()) {
			for (final FaceEffect faceEffect : model.getFaceEffects()) {
				DispElements.ChooseableFaceEffectsChunkItem effectsChunkItem = new DispElements.ChooseableFaceEffectsChunkItem(modelViewManager, faceEffect, number++);
				root.add(new DefaultMutableTreeNode(effectsChunkItem));
			}
		}
		if (model.getBindPoseChunk() != null) {
			DispElements.ChooseableBindPoseChunkItem bindPoseChunkItem = new DispElements.ChooseableBindPoseChunkItem(modelViewManager, model.getBindPoseChunk());
			root.add(new DefaultMutableTreeNode(bindPoseChunkItem));
		}
		return new DefaultTreeModel(root);
	}

	public void addSelectListener(final ComponentsPanel componentsPanel) {
		addTreeSelectionListener(e -> {
			final TreePath path = e.getNewLeadSelectionPath();
			boolean selected = false;

			if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {

				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

				if (node.getUserObject() instanceof ChooseableDisplayElement) {
					asElement(node.getUserObject()).select(componentsPanel);
					selected = true;
				}
			}
			if (!selected) {
				componentsPanel.selectedBlank();
			}
		});
	}

	private DefaultTreeCellRenderer getComponentBrowserCellRenderer() {
		return new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
			                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				ImageIcon iconOverride = null;
				if (value instanceof DefaultMutableTreeNode) {
					Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
					if (userObject instanceof ChooseableDisplayElement) {
						ImageIcon icon = ((ChooseableDisplayElement<?>) userObject).getIcon(expanded);
						if (icon != null) {
							iconOverride = icon;
						}
					}
				}
				final Component treeCellRendererComponent =
						super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				if (iconOverride != null) {
					setIcon(iconOverride);
				}
				return treeCellRendererComponent;
			}
		};
	}

	private ChooseableDisplayElement<?> asElement(final Object userObject) {
		return (ChooseableDisplayElement<?>) userObject;
	}

	private ChooseableDisplayElement<?> asElement(TreePath path) {
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			ChooseableDisplayElement<?> userObject = asElement(node);
			if (userObject != null) return userObject;
		}
		return null;
	}

	private ChooseableDisplayElement<?> asElement(DefaultMutableTreeNode node) {
		if (node != null) {
			Object userObject = node.getUserObject();
			if (userObject instanceof ChooseableDisplayElement) {
				return (ChooseableDisplayElement<?>) userObject;
			}
		}
		return null;
	}

	public void reloadFromModelView() {
//		System.out.println("Reloading ModelComponentBrowserTree");
		SwingUtilities.invokeLater(() -> {
			TreePath selectionPath = getSelectionPath();
			System.out.println("selectionPath: " + selectionPath);

			TreePath rootPath = new TreePath(getModel().getRoot());
			System.out.println("rootPath: " + rootPath);


			Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
			setModel(buildTreeModel(modelViewManager, undoActionListener, modelStructureChangeListener));

			TreePath newRootPath = new TreePath(getModel().getRoot());
			System.out.println("newRootPath: " + newRootPath);

			final List<TreePath> pathsToExpand = new ArrayList<>();

			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getModel().getRoot();

			while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
				pathsToExpand.add(getTreePath(expandedDescendants.nextElement(), rootNode));
			}
			for (TreePath path : pathsToExpand) {
				expandPath(path);
			}

			TreePath newSelectionPath = newRootPath;
			if (selectionPath != null) {
				System.out.println("selection path not null!");

				ChooseableDisplayElement<?> element = asElement((DefaultMutableTreeNode) selectionPath.getLastPathComponent());
				if (element != null && element.getItem() instanceof IdObject) {
					IdObject idObject = (IdObject) element.getItem();
					if (nodeToTreeElement.containsKey(idObject)) {
						newSelectionPath = new TreePath(nodeToTreeElement.get(idObject).getPath());
						System.out.println("make visible: " + newSelectionPath);
						makeVisible(newSelectionPath);
					}
				} else {
					newSelectionPath = getTreePath(selectionPath, rootNode);
				}
			}

			setSelectionPath(newSelectionPath); // should also fire listeners
		});
	}

	private TreePath getTreePath(TreePath nodePath, DefaultMutableTreeNode rootNode) {
		TreePath compoundPath = new TreePath(rootNode);
		DefaultMutableTreeNode currentNode = rootNode;

		for (int i = 1; i < nodePath.getPathCount() && compoundPath.getPathCount() == i; i++) {

			for (int j = 0; (j < currentNode.getChildCount()); j++) {

				DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) nodePath.getPathComponent(i);
				DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) currentNode.getChildAt(j);

				if (asElement(childAt).hasSameItem(asElement(pathComponent))) {
					currentNode = childAt;
					compoundPath = compoundPath.pathByAddingChild(childAt);
					break;
				}
			}
		}
		return compoundPath;
	}

	private final class HighlightOnMouseoverListenerImpl implements MouseMotionListener, MouseListener {
		private ChooseableDisplayElement<?> lastMouseOverNode = null;

		@Override
		public void mouseMoved(final MouseEvent e) {
//			controlDown = e.isControlDown();
			final TreePath pathForLocation = getPathForLocation(e.getX(), e.getY());
			final ChooseableDisplayElement<?> element;
			if (pathForLocation == null) {
				element = null;
			} else {
				final DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
				element = (ChooseableDisplayElement<?>) lastPathComponent.getUserObject();
			}
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
		public void mouseDragged(final MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
//			controlDown = e.isControlDown();
		}

		@Override
		public void mousePressed(final MouseEvent e) {
//			controlDown = e.isControlDown();
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			if (lastMouseOverNode != null) {
				lastMouseOverNode.mouseExited();
			}
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
//			controlDown = e.isControlDown();
		}
	}

//	final Image attachmentImage = IconUtils.loadNodeImage("/attachment" + template + ".png");
//	final Image eventImage = IconUtils.loadNodeImage("/event" + template + ".png");
//	final Image lightImage = IconUtils.loadNodeImage("/light" + template + ".png");
//	final Image particleImage = IconUtils.loadNodeImage("/particle1" + template + ".png");
//	final Image particle2Image = IconUtils.loadNodeImage("/particle2" + template + ".png");
//	final Image ribbonImage = IconUtils.loadNodeImage("/ribbon" + template + ".png");
//	final Image collisionImage = IconUtils.loadNodeImage("/collision" + template + ".png");

}
