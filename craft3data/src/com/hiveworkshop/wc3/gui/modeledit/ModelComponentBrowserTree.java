package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.etheller.collections.ArrayList;
import com.etheller.collections.Collection;
import com.etheller.collections.HashMap;
import com.etheller.collections.List;
import com.etheller.collections.Map;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportIconUtils;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.BindPoseChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;
import com.hiveworkshop.wc3.util.IconUtils;

public final class ModelComponentBrowserTree extends JTree {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public ModelComponentBrowserTree(final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener, final ModelEditorManager modelEditorManager,
			final ModelStructureChangeListener modelStructureChangeListener) {
		super(buildTreeModel(modelViewManager, undoActionListener, modelStructureChangeListener));
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		final HighlightOnMouseoverListenerImpl mouseListener = new HighlightOnMouseoverListenerImpl();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
					final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				ImageIcon iconOverride = null;
				if (value instanceof DefaultMutableTreeNode) {
					final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
					if (userObject instanceof ChooseableDisplayElement) {
						final ImageIcon icon = ((ChooseableDisplayElement<?>) userObject).getIcon(expanded);
						if (icon != null) {
							iconOverride = icon;
						}
					}
				}
				final Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, selected,
						expanded, leaf, row, hasFocus);
				if (iconOverride != null) {
					setIcon(iconOverride);
				}
				return treeCellRendererComponent;
			}
		});
//		setFocusable(false);
	}

	public void addSelectListener(final ModelComponentListener selectListener) {
		addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				final TreePath path = e.getNewLeadSelectionPath();
				boolean selected = false;
				if (path != null) {
					final Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof DefaultMutableTreeNode) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;
						if (node.getUserObject() instanceof ChooseableDisplayElement) {
							asElement(node.getUserObject()).select(selectListener);
							selected = true;
						}
					}
				}
				if (!selected) {
					selectListener.selectedBlank();
				}
			}
		});
	}

	public void reloadFromModelView() {
		System.out.println("Reloading ModelComponentBrowserTree");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final TreePath selectionPath = getSelectionPath();
				final TreePath rootPath = new TreePath(getModel().getRoot());
				final Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
				setModel(buildTreeModel(modelViewManager, undoActionListener, modelStructureChangeListener));
				final TreePath newRootPath = new TreePath(getModel().getRoot());
				final List<TreePath> pathsToExpand = new ArrayList<>();
				while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
					final TreePath nextPathToExpand = expandedDescendants.nextElement();
					TreePath newPathWithNewObjects = newRootPath;
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) getModel().getRoot();
					for (int i = 1; i < nextPathToExpand.getPathCount(); i++) {
						final DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) nextPathToExpand
								.getPathComponent(i);
						boolean foundMatchingChild = false;
						for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
							final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) currentNode.getChildAt(j);
							if (asElement(childAt.getUserObject())
									.hasSameItem(asElement(pathComponent.getUserObject()))) {
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
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) getModel().getRoot();
				TreePath newSelectionPath = newRootPath;
				if (selectionPath != null) {
					for (int i = 1; i < selectionPath.getPathCount(); i++) {
						final DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) selectionPath
								.getPathComponent(i);
						boolean foundMatchingChild = false;
						for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
							final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) currentNode.getChildAt(j);
							if (asElement(childAt.getUserObject())
									.hasSameItem(asElement(pathComponent.getUserObject()))) {
								currentNode = childAt;
								newSelectionPath = newSelectionPath.pathByAddingChild(childAt);
								foundMatchingChild = true;
							}
						}
						if (!foundMatchingChild) {
							break;
						}
					}
				}
				setSelectionPath(newSelectionPath); // should also fire listeners
			}

		});
	}

	private ChooseableDisplayElement<?> asElement(final Object userObject) {
		return (ChooseableDisplayElement<?>) userObject;
	}

	private static DefaultTreeModel buildTreeModel(final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ChooseableModelRoot(modelViewManager,
				undoActionListener, modelStructureChangeListener, modelViewManager.getModel()));

		root.add(new DefaultMutableTreeNode(new ChooseableModelComment(modelViewManager, undoActionListener,
				modelStructureChangeListener, modelViewManager.getModel())));
		root.add(new DefaultMutableTreeNode(new ChooseableModelHeader(modelViewManager, undoActionListener,
				modelStructureChangeListener, modelViewManager.getModel())));
		final DefaultMutableTreeNode sequences = new DefaultMutableTreeNode(new ChooseableDummyItem(modelViewManager,
				undoActionListener, modelStructureChangeListener, "Sequences"));
		for (final Animation item : modelViewManager.getModel().getAnims()) {
			sequences.add(new DefaultMutableTreeNode(new ChooseableAnimationItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, item)));
		}
		root.add(sequences);
		final DefaultMutableTreeNode globalSequences = new DefaultMutableTreeNode(new ChooseableDummyItem(
				modelViewManager, undoActionListener, modelStructureChangeListener, "GlobalSequences"));
		for (int globalSeqId = 0; globalSeqId < modelViewManager.getModel().getGlobalSeqs().size(); globalSeqId++) {
			globalSequences.add(new DefaultMutableTreeNode(
					new ChooseableGlobalSequenceItem(modelViewManager, undoActionListener, modelStructureChangeListener,
							modelViewManager.getModel().getGlobalSeq(globalSeqId), globalSeqId)));
		}
		root.add(globalSequences);
		final DefaultMutableTreeNode textures = new DefaultMutableTreeNode(new ChooseableDummyItem(modelViewManager,
				undoActionListener, modelStructureChangeListener, "Textures"));
		for (final Bitmap item : modelViewManager.getModel().getTextures()) {
			textures.add(new DefaultMutableTreeNode(new ChooseableBitmapItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, item)));
		}
		root.add(textures);
		final DefaultMutableTreeNode materials = new DefaultMutableTreeNode(new ChooseableDummyItem(modelViewManager,
				undoActionListener, modelStructureChangeListener, "Materials"));
		for (final Material item : modelViewManager.getModel().getMaterials()) {
			materials.add(new DefaultMutableTreeNode(new ChooseableMaterialItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, item)));
		}
		root.add(materials);
		final DefaultMutableTreeNode tVertexAnims = new DefaultMutableTreeNode(new ChooseableDummyItem(modelViewManager,
				undoActionListener, modelStructureChangeListener, "TVertexAnims"));
		for (final TextureAnim item : modelViewManager.getModel().getTexAnims()) {
			tVertexAnims.add(new DefaultMutableTreeNode(new ChooseableTextureAnimItem(modelViewManager,
					undoActionListener, modelStructureChangeListener, item)));
		}
		root.add(tVertexAnims);
		final DefaultMutableTreeNode geosets = new DefaultMutableTreeNode(
				new ChooseableDummyItem(modelViewManager, undoActionListener, modelStructureChangeListener, "Geosets"));
		for (final Geoset item : modelViewManager.getModel().getGeosets()) {
			geosets.add(new DefaultMutableTreeNode(new ChooseableGeosetItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, item)));
		}
		root.add(geosets);
		final DefaultMutableTreeNode geosetAnims = new DefaultMutableTreeNode(new ChooseableDummyItem(modelViewManager,
				undoActionListener, modelStructureChangeListener, "GeosetAnims"));
		for (final GeosetAnim item : modelViewManager.getModel().getGeosetAnims()) {
			geosetAnims.add(new DefaultMutableTreeNode(new ChooseableGeosetAnimItem(modelViewManager,
					undoActionListener, modelStructureChangeListener, item)));
		}
		root.add(geosetAnims);
//		for (final Bone item : modelViewManager.getModel().sortedIdObjects(Bone.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableBoneItem(modelViewManager, item)));
//		}
//		for (final Light item : modelViewManager.getModel().sortedIdObjects(Light.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableLightItem(modelViewManager, item)));
//		}
//		for (final Helper item : modelViewManager.getModel().sortedIdObjects(Helper.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableHelperItem(modelViewManager, item)));
//		}
//		for (final Attachment item : modelViewManager.getModel().sortedIdObjects(Attachment.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableAttachmentItem(modelViewManager, item)));
//		}
//		for (final ParticleEmitter item : modelViewManager.getModel().sortedIdObjects(ParticleEmitter.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableParticleEmitterItem(modelViewManager, item)));
//		}
//		for (final ParticleEmitter2 item : modelViewManager.getModel().sortedIdObjects(ParticleEmitter2.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableParticleEmitter2Item(modelViewManager, item)));
//		}
//		for (final PopcornFxEmitter item : modelViewManager.getModel().sortedIdObjects(PopcornFxEmitter.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableParticleEmitterPopcornItem(modelViewManager, item)));
//		}
//		for (final RibbonEmitter item : modelViewManager.getModel().sortedIdObjects(RibbonEmitter.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableRibbonEmitterItem(modelViewManager, item)));
//		}
//		for (final EventObject item : modelViewManager.getModel().sortedIdObjects(EventObject.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableEventObjectItem(modelViewManager, item)));
//		}
//		for (final CollisionShape item : modelViewManager.getModel().sortedIdObjects(CollisionShape.class)) {
//			root.add(new DefaultMutableTreeNode(new ChooseableCollisionShapeItem(modelViewManager, item)));
//		}

		final IdObjectToChooseableElementWrappingConverter converter = new IdObjectToChooseableElementWrappingConverter(
				modelViewManager, undoActionListener, modelStructureChangeListener);

		final Map<IdObject, DefaultMutableTreeNode> nodeToTreeElement = new HashMap<>();
		final Map<IdObject, List<DefaultMutableTreeNode>> nodeToChildrenAwaitingLink = new HashMap<>();
		final DefaultMutableTreeNode nodes = new DefaultMutableTreeNode(
				new ChooseableDummyItem(modelViewManager, undoActionListener, modelStructureChangeListener, "Nodes"));
		nodeToTreeElement.put(null, nodes);
		for (final IdObject object : modelViewManager.getModel().getIdObjects()) {
			object.apply(converter);
			final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(converter.element);
			nodeToTreeElement.put(object, treeNode);
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			final DefaultMutableTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode == null) {
				List<DefaultMutableTreeNode> awaitingChildrenList = nodeToChildrenAwaitingLink.get(parent);
				if (awaitingChildrenList == null) {
					awaitingChildrenList = new ArrayList<>();
					nodeToChildrenAwaitingLink.put(parent, awaitingChildrenList);
				}
				awaitingChildrenList.add(treeNode);
			} else {
				parentTreeNode.add(treeNode);
			}
			final List<DefaultMutableTreeNode> childrenNeedingLinkToCurrentNode = nodeToChildrenAwaitingLink
					.get(object);
			if ((childrenNeedingLinkToCurrentNode != null)
					&& !Collection.Util.isEmpty(childrenNeedingLinkToCurrentNode)) {
				for (final DefaultMutableTreeNode child : childrenNeedingLinkToCurrentNode) {
					treeNode.add(child);
				}
			}

		}
		root.add(nodes);

		final DefaultMutableTreeNode cameras = new DefaultMutableTreeNode(
				new ChooseableDummyItem(modelViewManager, undoActionListener, modelStructureChangeListener, "Cameras"));
		for (final Camera item : modelViewManager.getModel().getCameras()) {
			cameras.add(new DefaultMutableTreeNode(new ChooseableCameraItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, item)));
		}
		root.add(cameras);
		if (!modelViewManager.getModel().getFaceEffects().isEmpty()) {
			for (final FaceEffect faceEffect : modelViewManager.getModel().getFaceEffects()) {
				root.add(new DefaultMutableTreeNode(new ChooseableFaceEffectsChunkItem(modelViewManager,
						undoActionListener, modelStructureChangeListener, faceEffect)));
			}
		}
		if (modelViewManager.getModel().getBindPoseChunk() != null) {
			root.add(new DefaultMutableTreeNode(new ChooseableBindPoseChunkItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, modelViewManager.getModel().getBindPoseChunk())));
		}
		final DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);
		return defaultTreeModel;
	}

	private final class HighlightOnMouseoverListenerImpl implements MouseMotionListener, MouseListener {
		private ChooseableDisplayElement<?> lastMouseOverNode = null;

		@Override
		public void mouseMoved(final MouseEvent mouseEvent) {
			final TreePath pathForLocation = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			final ChooseableDisplayElement<?> element;
			if (pathForLocation == null) {
				element = null;
			} else {
				final DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) pathForLocation
						.getLastPathComponent();
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
		}

		@Override
		public void mousePressed(final MouseEvent e) {
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
		}
	}

	private static abstract class ChooseableDisplayElement<T> {
		protected final ModelViewManager modelViewManager;
		private UndoActionListener undoActionListener;
		protected final T item;
		private ImageIcon icon;
		private ModelStructureChangeListener modelStructureChangeListener;

		public ChooseableDisplayElement(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final T item) {
			this(null, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		public ChooseableDisplayElement(final ImageIcon icon, final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final T item) {
			this.modelViewManager = modelViewManager;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
			this.item = item;
			this.icon = icon;
		}

		public void setIcon(final ImageIcon icon) {
			this.icon = icon;
		}

		public void select(final ModelComponentListener listener) {
			select(item, modelViewManager, undoActionListener, modelStructureChangeListener, listener);
		}

		public abstract void mouseEntered();

		public abstract void mouseExited();

		protected abstract void select(T item, ModelViewManager modelViewManager, UndoActionListener undoListener,
				ModelStructureChangeListener modelStructureChangeListener, ModelComponentListener listener);

		@Override
		public String toString() {
			return getName(item, modelViewManager);
		}

		protected abstract String getName(T item, ModelViewManager modelViewManager);

		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (getClass() == other.getClass())
					&& ((other.item == item) || ((item != null) && item.equals(other.item)));
		}

		public ImageIcon getIcon(final boolean expanded) {
			return icon;
		}
	}
//	final Image attachmentImage = IconUtils.loadImage("icons/nodes/attachment" + template + ".png");
//	final Image eventImage = IconUtils.loadImage("icons/nodes/event" + template + ".png");
//	final Image lightImage = IconUtils.loadImage("icons/nodes/light" + template + ".png");
//	final Image particleImage = IconUtils.loadImage("icons/nodes/particle1" + template + ".png");
//	final Image particle2Image = IconUtils.loadImage("icons/nodes/particle2" + template + ".png");
//	final Image ribbonImage = IconUtils.loadImage("icons/nodes/ribbon" + template + ".png");
//	final Image collisionImage = IconUtils.loadImage("icons/nodes/collision" + template + ".png");

	private static final class ChooseableModelRoot extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon MODEL_ROOT_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("replaceabletextures\\worldeditui\\editor-trigger.blp")));

		public ChooseableModelRoot(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final EditableModel item) {
			super(MODEL_ROOT_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final EditableModel item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Model \"" + item.getHeaderName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableModelComment extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon COMMENT_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/comment.png"));

		public ChooseableModelComment(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final EditableModel item) {
			super(COMMENT_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final EditableModel item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selectedHeaderComment(item.getHeader());
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Comment";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableModelHeader extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon DATA_ICON = new ImageIcon(ViewportIconUtils.loadImage("icons/nodes/model.png"));

		public ChooseableModelHeader(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final EditableModel item) {
			super(DATA_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final EditableModel item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selectedHeaderData(item, modelViewManager, undoListener, modelStructureChangeListener);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Header";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableAnimationItem extends ChooseableDisplayElement<Animation> {
		private static final ImageIcon ANIMATION_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/animation.png"));

		public ChooseableAnimationItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Animation item) {
			super(ANIMATION_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Animation item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item, undoListener, modelStructureChangeListener);
		}

		@Override
		protected String getName(final Animation item, final ModelViewManager modelViewManager) {
			return "Anim \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableGlobalSequenceItem extends ChooseableDisplayElement<Integer> {
		private static final ImageIcon GLOBAL_SEQ_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/globalseq.png"));
		private final int globalSeqId;

		public ChooseableGlobalSequenceItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Integer item,
				final int globalSeqId) {
			super(GLOBAL_SEQ_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
			this.globalSeqId = globalSeqId;
		}

		@Override
		protected void select(final Integer item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(modelViewManager.getModel(), item, globalSeqId, undoListener,
					modelStructureChangeListener);
		}

		@Override
		protected String getName(final Integer item, final ModelViewManager modelViewManager) {
			return "GlobalSequence " + globalSeqId + ": Duration " + item;
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

		@Override
		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (other != null) && (other.getClass() == getClass())
					&& (globalSeqId == ((ChooseableGlobalSequenceItem) other).globalSeqId);
		}
	}

	private static final class ChooseableBitmapItem extends ChooseableDisplayElement<Bitmap> {
		private static final ImageIcon TEXTURE_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/bitmap.png"));

		public ChooseableBitmapItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Bitmap item) {
			super(TEXTURE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Bitmap item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item, modelViewManager, undoListener, modelStructureChangeListener);
		}

		@Override
		protected String getName(final Bitmap item, final ModelViewManager modelViewManager) {
			return "Bitmap \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableMaterialItem extends ChooseableDisplayElement<Material> {
		private static final ImageIcon MATERIAL_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/material.png"));

		public ChooseableMaterialItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Material item) {
			super(MATERIAL_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Material item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item, modelViewManager, undoListener, modelStructureChangeListener);
		}

		@Override
		protected String getName(final Material item, final ModelViewManager modelViewManager) {
			return "Material " + modelViewManager.getModel().getMaterials().indexOf(item);
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableTextureAnimItem extends ChooseableDisplayElement<TextureAnim> {
		private static final ImageIcon TEXTURE_ANIM_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/textureanim.png"));

		public ChooseableTextureAnimItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final TextureAnim item) {
			super(TEXTURE_ANIM_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final TextureAnim item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final TextureAnim item, final ModelViewManager modelViewManager) {
			return "TextureAnim " + modelViewManager.getModel().getTexAnims().indexOf(item);
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableGeosetItem extends ChooseableDisplayElement<Geoset> {
		private static final ImageIcon GEOSET_ITEM_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/geoset.png"));

		public ChooseableGeosetItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Geoset item) {
			super(GEOSET_ITEM_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Geoset item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Geoset item, final ModelViewManager modelViewManager) {
			final String numberName = "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
			if ((item.getLevelOfDetailName() != null) && (item.getLevelOfDetailName().length() > 0)) {
				return numberName + ": " + item.getLevelOfDetailName();
			}
			return numberName;
		}

		@Override
		public void mouseEntered() {
			modelViewManager.highlightGeoset(item);
		}

		@Override
		public void mouseExited() {
			modelViewManager.unhighlightGeoset(item);
		}

	}

	private static final class ChooseableGeosetAnimItem extends ChooseableDisplayElement<GeosetAnim> {
		private static final ImageIcon GEOSET_ANIM_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/geoanim.png"));

		public ChooseableGeosetAnimItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final GeosetAnim item) {
			super(GEOSET_ANIM_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final GeosetAnim item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final GeosetAnim item, final ModelViewManager modelViewManager) {
			return "GeosetAnim " + modelViewManager.getModel().getGeosetAnims().indexOf(item);
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableBoneItem extends ChooseableDisplayElement<Bone> {
		private static final ImageIcon BONE_ICON = new ImageIcon(ViewportIconUtils.loadImage("icons/nodes/bone.png"));

		public ChooseableBoneItem(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Bone item) {
			super(BONE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Bone item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Bone item, final ModelViewManager modelViewManager) {
			return "Bone \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableHelperItem extends ChooseableDisplayElement<Helper> {
		private static final ImageIcon BONE_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/helperhand.png"));

		public ChooseableHelperItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Helper item) {
			super(BONE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Helper item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Helper item, final ModelViewManager modelViewManager) {
			return "Helper \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableLightItem extends ChooseableDisplayElement<Light> {
		private static final ImageIcon LIGHT_ICON = new ImageIcon(ViewportIconUtils.loadImage("icons/nodes/light.png"));

		public ChooseableLightItem(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Light item) {
			super(LIGHT_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Light item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Light item, final ModelViewManager modelViewManager) {
			return "Light \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableAttachmentItem extends ChooseableDisplayElement<Attachment> {
		private static final ImageIcon ATTACHMENT_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/attachment.png"));

		public ChooseableAttachmentItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Attachment item) {
			super(ATTACHMENT_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Attachment item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Attachment item, final ModelViewManager modelViewManager) {
			return "Attachment \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableParticleEmitterItem extends ChooseableDisplayElement<ParticleEmitter> {
		private static final ImageIcon PARTICLE_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/particle1.png"));

		public ChooseableParticleEmitterItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final ParticleEmitter item) {
			super(PARTICLE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final ParticleEmitter item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final ParticleEmitter item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableParticleEmitter2Item extends ChooseableDisplayElement<ParticleEmitter2> {
		private static final ImageIcon PARTICLE2_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/particle2.png"));

		public ChooseableParticleEmitter2Item(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final ParticleEmitter2 item) {
			super(PARTICLE2_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final ParticleEmitter2 item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final ParticleEmitter2 item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter2 \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableParticleEmitterPopcornItem
			extends ChooseableDisplayElement<ParticleEmitterPopcorn> {
		private static final ImageIcon POPCORN_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/popcorn.png"));

		public ChooseableParticleEmitterPopcornItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final ParticleEmitterPopcorn item) {
			super(POPCORN_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final ParticleEmitterPopcorn item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final ParticleEmitterPopcorn item, final ModelViewManager modelViewManager) {
			return "ParticleEmitterPopcorn \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableRibbonEmitterItem extends ChooseableDisplayElement<RibbonEmitter> {
		private static final ImageIcon RIBBON_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/ribbon.png"));

		public ChooseableRibbonEmitterItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final RibbonEmitter item) {
			super(RIBBON_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final RibbonEmitter item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final RibbonEmitter item, final ModelViewManager modelViewManager) {
			return "RibbonEmitter \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

	}

	private static final class ChooseableEventObjectItem extends ChooseableDisplayElement<EventObject> {
		private static final ImageIcon EVENT_OBJECT_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/event.png"));

		public ChooseableEventObjectItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final EventObject item) {
			super(EVENT_OBJECT_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final EventObject item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final EventObject item, final ModelViewManager modelViewManager) {
			return "EventObject \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableCollisionShapeItem extends ChooseableDisplayElement<CollisionShape> {
		private static final ImageIcon COLLISION_SHAPE_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/collision.png"));

		public ChooseableCollisionShapeItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final CollisionShape item) {
			super(COLLISION_SHAPE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final CollisionShape item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final CollisionShape item, final ModelViewManager modelViewManager) {
			return "CollisionShape \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableCameraItem extends ChooseableDisplayElement<Camera> {
		private static final ImageIcon CAMERA_ICON = new ImageIcon(
				ViewportIconUtils.loadImage("icons/nodes/camera.png"));

		public ChooseableCameraItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final Camera item) {
			super(CAMERA_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final Camera item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final Camera item, final ModelViewManager modelViewManager) {
			return "Camera \"" + item.getName() + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableFaceEffectsChunkItem extends ChooseableDisplayElement<FaceEffect> {
		private static final ImageIcon FACEFX_ICON = new ImageIcon(ViewportIconUtils.loadImage("icons/nodes/fafx.png"));

		public ChooseableFaceEffectsChunkItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final FaceEffect item) {
			super(FACEFX_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final FaceEffect item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final FaceEffect item, final ModelViewManager modelViewManager) {
			return "FaceFX \"" + item.faceEffectTarget + "\"";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableBindPoseChunkItem extends ChooseableDisplayElement<BindPoseChunk> {
		private static final ImageIcon BINDPOSE_ICON = new ImageIcon(
				IconUtils.worldEditStyleIcon(ViewportIconUtils.loadImage("icons/nodes/bindpos.png")));

		public ChooseableBindPoseChunkItem(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final BindPoseChunk item) {
			super(BINDPOSE_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, item);
		}

		@Override
		protected void select(final BindPoseChunk item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
			listener.selected(item);
		}

		@Override
		protected String getName(final BindPoseChunk item, final ModelViewManager modelViewManager) {
			return "BindPoseChunk";
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}
	}

	private static final class ChooseableDummyItem extends ChooseableDisplayElement<Void> {
		private static final ImageIcon GROUP_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
		private static final ImageIcon GROUP_ICON_EXPANDED = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
		private final String name2;

		public ChooseableDummyItem(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener, final String name) {
			super(GROUP_ICON, modelViewManager, undoActionListener, modelStructureChangeListener, null);
			name2 = name;
		}

		@Override
		protected void select(final Void item, final ModelViewManager modelViewManager,
				final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener,
				final ModelComponentListener listener) {
		}

		@Override
		protected String getName(final Void item, final ModelViewManager modelViewManager) {
			return name2;
		}

		@Override
		public void mouseEntered() {
		}

		@Override
		public void mouseExited() {
		}

		@Override
		public ImageIcon getIcon(final boolean expanded) {
			return expanded ? GROUP_ICON_EXPANDED : GROUP_ICON;
		}

		@Override
		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (other instanceof ChooseableDummyItem) && ((ChooseableDummyItem) other).name2.equals(name2);
		}
	}

	public static interface ModelComponentListener {

		void selectedBlank();

		void selected(EditableModel model);

		void selectedHeaderData(EditableModel model, ModelViewManager modelViewManager, UndoActionListener undoListener,
				ModelStructureChangeListener modelStructureChangeListener);

		void selectedHeaderComment(Iterable<String> comment);

		void selected(Animation animation, UndoActionListener undoListener,
				ModelStructureChangeListener modelStructureChangeListener);

		void selected(EditableModel model, Integer globalSequence, int globalSequenceId, UndoActionListener undoActionListener,
				ModelStructureChangeListener modelStructureChangeListener);

		void selected(Bitmap texture, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
				ModelStructureChangeListener modelStructureChangeListener);

		void selected(Material material, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
				ModelStructureChangeListener modelStructureChangeListener);

		void selected(TextureAnim textureAnim);

		void selected(Geoset geoset);

		void selected(GeosetAnim geosetAnim);

		void selected(Bone object);

		void selected(Light light);

		void selected(Helper object);

		void selected(Attachment attachment);

		void selected(ParticleEmitter particleEmitter);

		void selected(ParticleEmitter2 particleEmitter);

		void selected(ParticleEmitterPopcorn popcornFxEmitter);

		void selected(RibbonEmitter particleEmitter);

		void selected(EventObject eventObject);

		void selected(CollisionShape collisionShape);

		void selected(Camera camera);

		void selected(FaceEffect faceEffectsChunk);

		void selected(BindPoseChunk bindPoseChunk);
	}

	private static final class IdObjectToChooseableElementWrappingConverter implements IdObjectVisitor {
		private final ModelViewManager modelViewManager;
		private ChooseableDisplayElement<?> element;
		private final UndoActionListener undoActionListener;
		private final ModelStructureChangeListener modelStructureChangeListener;

		public IdObjectToChooseableElementWrappingConverter(final ModelViewManager modelViewManager,
				final UndoActionListener undoActionListener,
				final ModelStructureChangeListener modelStructureChangeListener) {
			this.modelViewManager = modelViewManager;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
		}

		@Override
		public void bone(final Bone object) {
			element = new ChooseableBoneItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					object);
		}

		@Override
		public void light(final Light light) {
			element = new ChooseableLightItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					light);
		}

		@Override
		public void helper(final Helper object) {
			element = new ChooseableHelperItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			element = new ChooseableAttachmentItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					attachment);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			element = new ChooseableParticleEmitterItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, particleEmitter);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			element = new ChooseableParticleEmitter2Item(modelViewManager, undoActionListener,
					modelStructureChangeListener, particleEmitter);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			element = new ChooseableParticleEmitterPopcornItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, popcornFxEmitter);
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			element = new ChooseableRibbonEmitterItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, particleEmitter);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			element = new ChooseableEventObjectItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			element = new ChooseableCollisionShapeItem(modelViewManager, undoActionListener,
					modelStructureChangeListener, collisionShape);
		}

		@Override
		public void camera(final Camera camera) {
			element = new ChooseableCameraItem(modelViewManager, undoActionListener, modelStructureChangeListener,
					camera);
		}

	}
}
