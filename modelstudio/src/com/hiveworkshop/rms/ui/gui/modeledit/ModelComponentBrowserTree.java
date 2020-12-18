package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.*;

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
		setCellRenderer(getComponentBrowserCellRenderer());
		setFocusable(false);
	}

	private static DefaultTreeModel buildTreeModel(
			final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {

		EditableModel model = modelViewManager.getModel();

		final ChooseableModelRoot modelRoot = new ChooseableModelRoot(modelViewManager, model);
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(modelRoot);

		ChooseableModelComment modelComment = new ChooseableModelComment(modelViewManager, model.getHeader());
//		System.out.println(model.getHeader());
//		System.out.println(model.getName());
//		System.out.println(model.getWrappedDataSource());
		root.add(new DefaultMutableTreeNode(modelComment));

		ChooseableModelHeader modelHeader = new ChooseableModelHeader(modelViewManager, model);
		root.add(new DefaultMutableTreeNode(modelHeader));

		final DefaultMutableTreeNode sequences = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Sequences"));
		for (final Animation item : model.getAnims()) {
			sequences.add(new DefaultMutableTreeNode(new ChooseableAnimationItem(modelViewManager, item)));
		}
		root.add(sequences);

		final DefaultMutableTreeNode globalSequences = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "GlobalSequences"));
		for (int globalSeqId = 0; globalSeqId < model.getGlobalSeqs().size(); globalSeqId++) {
			ChooseableGlobalSequenceItem sequenceItem = new ChooseableGlobalSequenceItem(modelViewManager, model.getGlobalSeq(globalSeqId), globalSeqId);
			globalSequences.add(new DefaultMutableTreeNode(sequenceItem));
		}
		root.add(globalSequences);

		final DefaultMutableTreeNode textures = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Textures"));
		for (final Bitmap item : model.getTextures()) {
			textures.add(new DefaultMutableTreeNode(new ChooseableBitmapItem(modelViewManager, item)));
		}
		root.add(textures);

		final DefaultMutableTreeNode materials = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Materials"));
		for (final Material item : model.getMaterials()) {
			materials.add(new DefaultMutableTreeNode(new ChooseableMaterialItem(modelViewManager, item)));
		}
		root.add(materials);

		final DefaultMutableTreeNode tVertexAnims = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "TVertexAnims"));
		for (final TextureAnim item : model.getTexAnims()) {
			ChooseableTextureAnimItem textureAnimItem = new ChooseableTextureAnimItem(modelViewManager, item);
			tVertexAnims.add(new DefaultMutableTreeNode(textureAnimItem));
		}
		root.add(tVertexAnims);

		final DefaultMutableTreeNode geosets = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Geosets"));
		for (final Geoset item : model.getGeosets()) {
			ChooseableGeosetItem geosetItem = new ChooseableGeosetItem(modelViewManager, item);
			geosets.add(new DefaultMutableTreeNode(geosetItem));
		}
		root.add(geosets);

		final DefaultMutableTreeNode geosetAnims = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "GeosetAnims"));
		for (final GeosetAnim item : model.getGeosetAnims()) {
			ChooseableGeosetAnimItem geosetAnimItem = new ChooseableGeosetAnimItem(modelViewManager, item);
			geosetAnims.add(new DefaultMutableTreeNode(geosetAnimItem));
		}
		root.add(geosetAnims);

		final IdObjectToChooseableElementWrappingConverter converter = new IdObjectToChooseableElementWrappingConverter(
				modelViewManager, undoActionListener, modelStructureChangeListener);

		final Map<IdObject, DefaultMutableTreeNode> nodeToTreeElement = new HashMap<>();
		final Map<IdObject, List<DefaultMutableTreeNode>> nodeToChildrenAwaitingLink = new HashMap<>();

		final DefaultMutableTreeNode nodes = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Nodes"));
		nodeToTreeElement.put(null, nodes);

		for (final IdObject object : model.getIdObjects()) {
			object.apply(converter);
			final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(converter.element);

			nodeToTreeElement.put(object, treeNode);
			IdObject parent = object.getParent();

			if (parent == object) {
				parent = null;
			}
			final DefaultMutableTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode == null) {
				List<DefaultMutableTreeNode> awaitingChildrenList = nodeToChildrenAwaitingLink.computeIfAbsent(parent, k -> new ArrayList<>());
				awaitingChildrenList.add(treeNode);
			} else {
				parentTreeNode.add(treeNode);
			}
			final List<DefaultMutableTreeNode> childrenNeedingLinkToCurrentNode = nodeToChildrenAwaitingLink.get(object);

			if ((childrenNeedingLinkToCurrentNode != null)
					&& childrenNeedingLinkToCurrentNode.size() > 0) {
				for (final DefaultMutableTreeNode child : childrenNeedingLinkToCurrentNode) {
					treeNode.add(child);
				}
			}

		}
		root.add(nodes);

		final DefaultMutableTreeNode cameras = new DefaultMutableTreeNode(getDummyItem(modelViewManager, "Cameras"));
		for (final Camera item : model.getCameras()) {
			cameras.add(new DefaultMutableTreeNode(new ChooseableCameraItem(modelViewManager, item)));
		}
		root.add(cameras);

		if (!model.getFaceEffects().isEmpty()) {
			for (final FaceEffect faceEffect : model.getFaceEffects()) {
				ChooseableFaceEffectsChunkItem effectsChunkItem = new ChooseableFaceEffectsChunkItem(modelViewManager, faceEffect);
				root.add(new DefaultMutableTreeNode(effectsChunkItem));
			}
		}
		if (model.getBindPoseChunk() != null) {
			ChooseableBindPoseChunkItem bindPoseChunkItem = new ChooseableBindPoseChunkItem(modelViewManager, model.getBindPoseChunk());
			root.add(new DefaultMutableTreeNode(bindPoseChunkItem));
		}
		return new DefaultTreeModel(root);
	}

	private static ChooseableDummyItem getDummyItem(ModelViewManager modelViewManager, String sequences) {
		return new ChooseableDummyItem(modelViewManager, sequences);
	}

	public void addSelectListener(final ComponentsPanel componentsPanel) {
		addTreeSelectionListener(e -> {
			final TreePath path = e.getNewLeadSelectionPath();
			boolean selected = false;

			if (path != null) {
				final Object lastPathComponent = path.getLastPathComponent();

				if (lastPathComponent instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;

					if (node.getUserObject() instanceof ChooseableDisplayElement) {
						asElement(node.getUserObject()).select(componentsPanel);
						selected = true;
					}
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
		};
	}

	private ChooseableDisplayElement<?> asElement(final Object userObject) {
		return (ChooseableDisplayElement<?>) userObject;
	}

	public void reloadFromModelView() {
		System.out.println("Reloading ModelComponentBrowserTree");
		SwingUtilities.invokeLater(() -> {
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
				newPathWithNewObjects = getTreePath(nextPathToExpand, currentNode, newPathWithNewObjects);
				pathsToExpand.add(newPathWithNewObjects);
			}
			for (final TreePath path : pathsToExpand) {
				expandPath(path);
			}
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) getModel().getRoot();
			TreePath newSelectionPath = newRootPath;
			if (selectionPath != null) {
				newSelectionPath = getTreePath(selectionPath, currentNode, newSelectionPath);
			}
			setSelectionPath(newSelectionPath); // should also fire listeners
		});
	}

	private TreePath getTreePath(TreePath selectionPath, DefaultMutableTreeNode currentNode, TreePath newSelectionPath) {
		for (int i = 1; i < selectionPath.getPathCount(); i++) {
			final DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) selectionPath
					.getPathComponent(i);
			boolean foundMatchingChild = false;

			for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
				final DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) currentNode.getChildAt(j);

				if (asElement(childAt.getUserObject()).hasSameItem(asElement(pathComponent.getUserObject()))) {
					currentNode = childAt;
					newSelectionPath = newSelectionPath.pathByAddingChild(childAt);
					foundMatchingChild = true;
				}
			}
			if (!foundMatchingChild) {
				break;
			}
		}
		return newSelectionPath;
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

//	final Image attachmentImage = IconUtils.loadNodeImage("/attachment" + template + ".png");
//	final Image eventImage = IconUtils.loadNodeImage("/event" + template + ".png");
//	final Image lightImage = IconUtils.loadNodeImage("/light" + template + ".png");
//	final Image particleImage = IconUtils.loadNodeImage("/particle1" + template + ".png");
//	final Image particle2Image = IconUtils.loadNodeImage("/particle2" + template + ".png");
//	final Image ribbonImage = IconUtils.loadNodeImage("/ribbon" + template + ".png");
//	final Image collisionImage = IconUtils.loadNodeImage("/collision" + template + ".png");

	private static final class ChooseableModelRoot extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon MODEL_ROOT_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("replaceabletextures\\worldeditui\\editor-trigger.blp")));

		public ChooseableModelRoot(final ModelViewManager modelViewManager, final EditableModel item) {
			super(MODEL_ROOT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Model \"" + item.getHeaderName() + "\"";
		}
	}

	private static final class ChooseableModelComment extends ChooseableDisplayElement<ArrayList<String>> {
		private static final ImageIcon COMMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("/comment.png"));

		public ChooseableModelComment(final ModelViewManager modelViewManager, final ArrayList<String> item) {
			super(COMMENT_ICON, modelViewManager, item);
			System.out.println(item);
		}

		@Override
		protected String getName(final ArrayList<String> item, final ModelViewManager modelViewManager) {
			return "Comment";
		}
	}

	private static final class ChooseableModelHeader extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon DATA_ICON = new ImageIcon(RMSIcons.loadNodeImage("/model.png"));

		public ChooseableModelHeader(final ModelViewManager modelViewManager, final EditableModel item) {
			super(DATA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Header";
		}
	}

	private static final class ChooseableAnimationItem extends ChooseableDisplayElement<Animation> {

		private static final ImageIcon ANIMATION_ICON = new ImageIcon(RMSIcons.loadNodeImage("/animation.png"));

		public ChooseableAnimationItem(final ModelViewManager modelViewManager, final Animation item) {
			super(ANIMATION_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Animation item, final ModelViewManager modelViewManager) {
			return "Anim \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableGlobalSequenceItem extends ChooseableDisplayElement<Integer> {
		private static final ImageIcon GLOBAL_SEQ_ICON = new ImageIcon(RMSIcons.loadNodeImage("/globalseq.png"));

		private final int globalSeqId;

		public ChooseableGlobalSequenceItem(final ModelViewManager modelViewManager, final Integer item, final int globalSeqId) {
			super(GLOBAL_SEQ_ICON, modelViewManager, item);
			this.globalSeqId = globalSeqId;
		}

		@Override
		protected String getName(final Integer item, final ModelViewManager modelViewManager) {
			return "GlobalSequence " + globalSeqId + ": Duration " + item;
		}

		@Override
		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (other != null) && (other.getClass() == getClass())
					&& (globalSeqId == ((ChooseableGlobalSequenceItem) other).globalSeqId);
		}
	}

	private static final class ChooseableBitmapItem extends ChooseableDisplayElement<Bitmap> {

		private static final ImageIcon TEXTURE_ICON = new ImageIcon(RMSIcons.loadNodeImage("/bitmap.png"));

		public ChooseableBitmapItem(final ModelViewManager modelViewManager, final Bitmap item) {
			super(TEXTURE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Bitmap item, final ModelViewManager modelViewManager) {
			return "Bitmap \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableMaterialItem extends ChooseableDisplayElement<Material> {

		private static final ImageIcon MATERIAL_ICON = new ImageIcon(RMSIcons.loadNodeImage("/material.png"));

		public ChooseableMaterialItem(final ModelViewManager modelViewManager, final Material item) {
			super(MATERIAL_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Material item, final ModelViewManager modelViewManager) {
			return "Material " + modelViewManager.getModel().getMaterials().indexOf(item);
		}
	}

	private static final class ChooseableTextureAnimItem extends ChooseableDisplayElement<TextureAnim> {

		private static final ImageIcon TEXTURE_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("/textureanim.png"));

		public ChooseableTextureAnimItem(final ModelViewManager modelViewManager, final TextureAnim item) {
			super(TEXTURE_ANIM_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final TextureAnim item, final ModelViewManager modelViewManager) {
			return "TextureAnim " + modelViewManager.getModel().getTexAnims().indexOf(item);
		}
	}

	private static final class ChooseableGeosetAnimItem extends ChooseableDisplayElement<GeosetAnim> {
		private static final ImageIcon GEOSET_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("/geoanim.png"));

		public ChooseableGeosetAnimItem(final ModelViewManager modelViewManager, final GeosetAnim item) {
			super(GEOSET_ANIM_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final GeosetAnim item, final ModelViewManager modelViewManager) {
			return "GeosetAnim " + modelViewManager.getModel().getGeosetAnims().indexOf(item);
		}
	}

	private static final class ChooseableBoneItem extends ChooseableDisplayElement<Bone> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("/bone.png"));

		public ChooseableBoneItem(final ModelViewManager modelViewManager, final Bone item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Bone item, final ModelViewManager modelViewManager) {
			return "Bone \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableHelperItem extends ChooseableDisplayElement<Helper> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("/helperhand.png"));

		public ChooseableHelperItem(final ModelViewManager modelViewManager, final Helper item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Helper item, final ModelViewManager modelViewManager) {
			return "Helper \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableLightItem extends ChooseableDisplayElement<Light> {
		private static final ImageIcon LIGHT_ICON = new ImageIcon(RMSIcons.loadNodeImage("/light.png"));

		public ChooseableLightItem(final ModelViewManager modelViewManager, final Light item) {
			super(LIGHT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Light item, final ModelViewManager modelViewManager) {
			return "Light \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableAttachmentItem extends ChooseableDisplayElement<Attachment> {
		private static final ImageIcon ATTACHMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("/attachment.png"));

		public ChooseableAttachmentItem(final ModelViewManager modelViewManager, final Attachment item) {
			super(ATTACHMENT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Attachment item, final ModelViewManager modelViewManager) {
			return "Attachment \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterItem extends ChooseableDisplayElement<ParticleEmitter> {
		private static final ImageIcon PARTICLE_ICON = new ImageIcon(RMSIcons.loadNodeImage("/particle1.png"));

		public ChooseableParticleEmitterItem(final ModelViewManager modelViewManager, final ParticleEmitter item) {
			super(PARTICLE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitter item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitter2Item extends ChooseableDisplayElement<ParticleEmitter2> {
		private static final ImageIcon PARTICLE2_ICON = new ImageIcon(RMSIcons.loadNodeImage("/particle2.png"));

		public ChooseableParticleEmitter2Item(final ModelViewManager modelViewManager, final ParticleEmitter2 item) {
			super(PARTICLE2_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitter2 item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter2 \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterPopcornItem extends ChooseableDisplayElement<ParticleEmitterPopcorn> {
		private static final ImageIcon POPCORN_ICON = new ImageIcon(RMSIcons.loadNodeImage("/popcorn.png"));

		public ChooseableParticleEmitterPopcornItem(final ModelViewManager modelViewManager, final ParticleEmitterPopcorn item) {
			super(POPCORN_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitterPopcorn item, final ModelViewManager modelViewManager) {
			return "ParticleEmitterPopcorn \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableRibbonEmitterItem extends ChooseableDisplayElement<RibbonEmitter> {
		private static final ImageIcon RIBBON_ICON = new ImageIcon(RMSIcons.loadNodeImage("/ribbon.png"));

		public ChooseableRibbonEmitterItem(final ModelViewManager modelViewManager, final RibbonEmitter item) {
			super(RIBBON_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final RibbonEmitter item, final ModelViewManager modelViewManager) {
			return "RibbonEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableEventObjectItem extends ChooseableDisplayElement<EventObject> {
		private static final ImageIcon EVENT_OBJECT_ICON = new ImageIcon(RMSIcons.loadNodeImage("/event.png"));

		public ChooseableEventObjectItem(final ModelViewManager modelViewManager, final EventObject item) {
			super(EVENT_OBJECT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(EventObject item, ModelViewManager modelViewManager) {
			return null;
		}
	}

	private static final class ChooseableCollisionShapeItem extends ChooseableDisplayElement<CollisionShape> {
		private static final ImageIcon COLLISION_SHAPE_ICON = new ImageIcon(RMSIcons.loadNodeImage("/collision.png"));

		public ChooseableCollisionShapeItem(final ModelViewManager modelViewManager, final CollisionShape item) {
			super(COLLISION_SHAPE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final CollisionShape item, final ModelViewManager modelViewManager) {
			return "CollisionShape \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableCameraItem extends ChooseableDisplayElement<Camera> {
		private static final ImageIcon CAMERA_ICON = new ImageIcon(RMSIcons.loadNodeImage("/camera.png"));

		public ChooseableCameraItem(final ModelViewManager modelViewManager, final Camera item) {
			super(CAMERA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Camera item, final ModelViewManager modelViewManager) {
			return "Camera \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableFaceEffectsChunkItem extends ChooseableDisplayElement<FaceEffect> {
		private static final ImageIcon FACEFX_ICON = new ImageIcon(RMSIcons.loadNodeImage("/fafx.png"));

		public ChooseableFaceEffectsChunkItem(final ModelViewManager modelViewManager, final FaceEffect item) {
			super(FACEFX_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final FaceEffect item, final ModelViewManager modelViewManager) {
			return "FaceFX \"" + item.faceEffectTarget + "\"";
		}
	}

	private static final class ChooseableBindPoseChunkItem extends ChooseableDisplayElement<BindPose> {
		private static final ImageIcon BINDPOSE_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadNodeImage("/bindpos.png")));

		public ChooseableBindPoseChunkItem(final ModelViewManager modelViewManager, final BindPose item) {
			super(BINDPOSE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final BindPose item, final ModelViewManager modelViewManager) {
			return "BindPose";
		}
	}

	private static final class ChooseableDummyItem extends ChooseableDisplayElement<Void> {
		private static final ImageIcon GROUP_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
		private static final ImageIcon GROUP_ICON_EXPANDED = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
		private final String name2;

		public ChooseableDummyItem(final ModelViewManager modelViewManager, final String name) {
			super(GROUP_ICON, modelViewManager, null);
			name2 = name;
		}

		@Override
		protected String getName(final Void item, final ModelViewManager modelViewManager) {
			return name2;
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

	private static final class ChooseableGeosetItem extends ChooseableDisplayElement<Geoset> {
		private static final ImageIcon GEOSET_ITEM_ICON = new ImageIcon(RMSIcons.loadNodeImage("/geoset.png"));

		public ChooseableGeosetItem(final ModelViewManager modelViewManager, final Geoset item) {
			super(GEOSET_ITEM_ICON, modelViewManager, item);
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
			element = new ChooseableBoneItem(modelViewManager, object);
		}

		@Override
		public void light(final Light light) {
			element = new ChooseableLightItem(modelViewManager, light);
		}

		@Override
		public void helper(final Helper object) {
			element = new ChooseableHelperItem(modelViewManager, object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			element = new ChooseableAttachmentItem(modelViewManager, attachment);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			element = new ChooseableParticleEmitterItem(modelViewManager, particleEmitter);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			element = new ChooseableParticleEmitter2Item(modelViewManager, particleEmitter);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			element = new ChooseableParticleEmitterPopcornItem(modelViewManager, popcornFxEmitter);
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			element = new ChooseableRibbonEmitterItem(modelViewManager, particleEmitter);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			element = new ChooseableEventObjectItem(modelViewManager, eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			element = new ChooseableCollisionShapeItem(modelViewManager, collisionShape);
		}

		@Override
		public void camera(final Camera camera) {
			element = new ChooseableCameraItem(modelViewManager, camera);
		}

	}
}
