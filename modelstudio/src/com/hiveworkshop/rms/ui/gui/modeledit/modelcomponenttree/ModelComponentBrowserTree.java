package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public final class ModelComponentBrowserTree extends JTree {
	private final ModelHandler modelHandler;
	private Map<IdObject, ModelTreeNode<?>> nodeToTreeElement;
	private boolean controlDown = false;
	private final ComponentsPanel componentsPanel;

	public ModelComponentBrowserTree(ModelHandler modelHandler) {
		super();
		this.modelHandler = modelHandler;
		setModel(buildTreeModel(modelHandler));

		addKeyListener(getKeyAdapter());

		MouseAdapter mouseListener = getMouseAdapter();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addTreeExpansionListener(getExpansionListener());

		setCellRenderer(getComponentBrowserCellRenderer());

		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);


		componentsPanel = new ComponentsPanel(modelHandler);
		addTreeSelectionListener(e -> onTreeSelection(componentsPanel, e));
	}

	public ComponentsPanel getComponentsPanel() {
		return componentsPanel;
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
			boolean isExpanding = false;

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if (controlDown && (!isExpanding)) {
					isExpanding = true;
					if (event.getPath().getLastPathComponent() instanceof ModelTreeNode<?> lastPathComponent) {
						expandAllChildren(lastPathComponent, event.getPath(), true);
					}
					isExpanding = false;
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				if (controlDown && (!isExpanding)) {
					isExpanding = true;
					if (event.getPath().getLastPathComponent() instanceof ModelTreeNode<?> lastPathComponent) {
						expandAllChildren(lastPathComponent, event.getPath(), false);
					}
					isExpanding = false;
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

	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();

		ModelTreeNode<?> root = new ModelTreeNode<>(DisplayElementType.MODEL_ROOT, modelView, model, () -> "Model \"" + model.getHeaderName() + "\"");
		root.add(new ModelTreeNode<>(DisplayElementType.COMMENT, modelView, model.getComments(), () -> "Comment"));
		root.add(new ModelTreeNode<>(DisplayElementType.HEADER, modelView, model, () -> "Header"));

		root.add(getSequencesNode(model, modelView));
		root.add(getGlobalSequences(model, modelView));
		root.add(getTextures(model, modelView));
		root.add(getMaterials(model, modelView));
		root.add(getTextureAnims(model, modelView));
		root.add(getGeosets(model, modelView));
		root.add(getNodes(model, modelView));
		root.add(getCameras(model, modelView));

		int number = 0;
		if (!model.getFaceEffects().isEmpty()) {
			for (FaceEffect faceEffect : model.getFaceEffects()) {
				root.add(new ModelTreeNode<>(DisplayElementType.FACEFX, modelView, faceEffect, () -> DisplayElementType.FACEFX.getName() + " \"" + faceEffect.getFaceEffectTarget() + "\"", number++));
			}
		}
		return new DefaultTreeModel(root);
	}

	private ModelTreeNode<?> getCameras(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> cameras = new ModelTreeNode<>(new ChoosableDisplayElement<>(DisplayElementType.CAMERA, modelView, "Cameras"));
		for (Camera item : model.getCameras()) {
			cameras.add(new ModelTreeNode<>(DisplayElementType.CAMERA, modelView, item, model.getId(item)));
		}
		return cameras;
	}

	private ModelTreeNode<?> getNodes(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> nodes = new ModelTreeNode<>(DisplayElementType.NODES, modelView, "Nodes");

		nodeToTreeElement = new HashMap<>();
		nodeToTreeElement.put(null, nodes);

		// Create all treeNodes
		for (IdObject idObject : model.getIdObjects()) {
			ModelTreeNode<?> treeNode = new ModelTreeNode<>(getIdObjectElement(modelView, idObject));

			nodeToTreeElement.put(idObject, treeNode);
		}

		// Link all treeNodes
		for (IdObject idObject : model.getIdObjects()) {
			IdObject parent = idObject.getParent();

			ModelTreeNode<?> parentTreeNode = nodeToTreeElement.get(parent);
			parentTreeNode.add(nodeToTreeElement.get(idObject));
		}
		return nodes;
	}

	private ModelTreeNode<?> getGeosets(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> geosets = new ModelTreeNode<>(DisplayElementType.GEOSET_ITEM, modelView, "Geosets");
		for (Geoset item : model.getGeosets()) {
			geosets.add(new ModelTreeNode<>(DisplayElementType.GEOSET_ITEM, modelView, item));
		}
		return geosets;
	}

	private ModelTreeNode<?> getTextureAnims(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> textureAnims = new ModelTreeNode<>(DisplayElementType.TEXTURE_ANIM, modelView, "TextureAnims");
		for (int i = 0; i < model.getTexAnims().size(); i++) {
			textureAnims.add(new ModelTreeNode<>(DisplayElementType.TEXTURE_ANIM, modelView, model.getTexAnim(i), i));
		}
		return textureAnims;
	}

	private ModelTreeNode<?> getMaterials(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> materials = new ModelTreeNode<>(DisplayElementType.MATERIAL, modelView, "Materials");
		for (int i = 0; i < model.getMaterials().size(); i++) {
			materials.add(new ModelTreeNode<>(DisplayElementType.MATERIAL, modelView, model.getMaterial(i), i));
		}
		return materials;
	}

	private ModelTreeNode<?> getTextures(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> textures = new ModelTreeNode<>(DisplayElementType.TEXTURE, modelView, "Textures");
		for (int i = 0; i < model.getTextures().size(); i++) {
			textures.add(new ModelTreeNode<>(DisplayElementType.TEXTURE, modelView, model.getTexture(i), i));
		}
		return textures;
	}

	private ModelTreeNode<?> getGlobalSequences(EditableModel model, ModelView modelView) {
		ModelTreeNode<?> globalSequences = new ModelTreeNode<>(DisplayElementType.GLOBAL_SEQ, modelView, "GlobalSequences");
		for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
			globalSequences.add(new ModelTreeNode<>(DisplayElementType.GLOBAL_SEQ, modelView, globalSeq, model.getId(globalSeq)));
		}
		return globalSequences;
	}

	private ModelTreeNode<?> getSequencesNode(EditableModel model, ModelView modelView) {
//		ModelTreeNode<?> sequences = new ModelTreeNode<>(DisplayElementType.ANIMATION, modelView, "Sequences (" + model.getAnims().size() + ")");
		ModelTreeNode<?> sequences = new ModelTreeNode<>(DisplayElementType.ANIMATION, modelView, "Sequences");
		for (int i = 0; i < model.getAnims().size(); i++) {
			sequences.add(new ModelTreeNode<>(DisplayElementType.ANIMATION, modelView, model.getAnim(i), i));
		}
		return sequences;
	}

	private void onTreeSelection(ComponentsPanel componentsPanel, TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		if (path != null && path.getLastPathComponent() instanceof ModelTreeNode<?> node) {
			node.select(componentsPanel);
		} else {
			componentsPanel.selectedBlank();
		}
	}

	private DefaultTreeCellRenderer getComponentBrowserCellRenderer() {
		return new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				if (value instanceof ModelTreeNode treeNode) {
					ImageIcon iconOverride = treeNode.getIcon(expanded);
					if (iconOverride != null) {
						setIcon(iconOverride);
					}
				}
				return treeCellRendererComponent;
			}
		};
	}

	public ModelComponentBrowserTree reloadFromModelView() {
//		System.out.println("Reloading ModelComponentBrowserTree");
		SwingUtilities.invokeLater(() -> {
			TreePath oldSelectionPath = getSelectionPath();
			TreePath rootPath = new TreePath(getModel().getRoot());
			Enumeration<TreePath> oldExpandedDescendants = getExpandedDescendants(rootPath);
//			System.out.println("oldSelectionPath: " + oldSelectionPath);
//			System.out.println("rootPath: " + rootPath);
			
			setModel(buildTreeModel(modelHandler));
			ModelTreeNode<?> rootNode = (ModelTreeNode<?>) getModel().getRoot();
			TreePath newRootPath = new TreePath(rootNode);
//			System.out.println("newRootPath: " + newRootPath);

			List<TreePath> pathsToExpand = new ArrayList<>();
			while (oldExpandedDescendants != null && oldExpandedDescendants.hasMoreElements()) {
				pathsToExpand.add(getNewTreePathForOldPath(oldExpandedDescendants.nextElement(), rootNode));
			}
			for (TreePath path : pathsToExpand) {
				expandPath(path);
			}

			TreePath newSelectionPath = newRootPath;
			if (oldSelectionPath != null) {
				if (oldSelectionPath.getLastPathComponent() instanceof ModelTreeNode<?> node
						&& node.getItem() instanceof IdObject idObject) {
					if (nodeToTreeElement.containsKey(idObject)) {
						newSelectionPath = new TreePath(nodeToTreeElement.get(idObject).getPath());
//						System.out.println("make visible: " + newSelectionPath);
						makeVisible(newSelectionPath);
					}
				} else {
					newSelectionPath = getNewTreePathForOldPath(oldSelectionPath, rootNode);
				}
			}

			setSelectionPath(newSelectionPath); // should also fire listeners
		});
		return this;
	}

	private TreePath getNewTreePathForOldPath(TreePath oldNodePath, ModelTreeNode<?> rootNode) {
		TreePath compoundPath = new TreePath(rootNode);
		ModelTreeNode<?> currentNode = rootNode;

		for (int i = 1; i < oldNodePath.getPathCount(); i++) {
			Object pathComponent = oldNodePath.getPathComponent(i);
			if (pathComponent instanceof ModelTreeNode<?> oldNode){
				currentNode = currentNode.findChildWithSameItem(oldNode);
				if (currentNode != null) {
					compoundPath = compoundPath.pathByAddingChild(currentNode);
				} else {
					break;
				}
			}
		}
		return compoundPath;
	}

	public static ChoosableDisplayElement<?> getIdObjectElement(ModelView modelView, IdObject idObject) {
		return switch (idObject.getClass().getSimpleName()) {
			case "Helper" -> new ChoosableDisplayElement<>(DisplayElementType.HELPER, modelView, idObject, () -> DisplayElementType.HELPER.getName() + " \"" + idObject.getName() + "\"");
			case "Bone" -> new ChoosableDisplayElement<>(DisplayElementType.BONE, modelView, idObject, () -> DisplayElementType.BONE.getName() + " \"" + idObject.getName() + "\"");
			case "Light" -> new ChoosableDisplayElement<>(DisplayElementType.LIGHT, modelView, idObject, () -> DisplayElementType.LIGHT.getName() + " \"" + idObject.getName() + "\"");
			case "Attachment" -> new ChoosableDisplayElement<>(DisplayElementType.ATTACHMENT, modelView, idObject, () -> DisplayElementType.ATTACHMENT.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitter" -> new ChoosableDisplayElement<>(DisplayElementType.PARTICLE, modelView, idObject, () -> DisplayElementType.PARTICLE.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitter2" -> new ChoosableDisplayElement<>(DisplayElementType.PARTICLE2, modelView, idObject, () -> DisplayElementType.PARTICLE2.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitterPopcorn" -> new ChoosableDisplayElement<>(DisplayElementType.POPCORN, modelView, idObject, () -> DisplayElementType.POPCORN.getName() + " \"" + idObject.getName() + "\"");
			case "RibbonEmitter" -> new ChoosableDisplayElement<>(DisplayElementType.RIBBON, modelView, idObject, () -> DisplayElementType.RIBBON.getName() + " \"" + idObject.getName() + "\"");
			case "EventObject" -> new ChoosableDisplayElement<>(DisplayElementType.EVENT_OBJECT, modelView, idObject, () -> DisplayElementType.EVENT_OBJECT.getName() + " \"" + idObject.getName() + "\"");
			case "CollisionShape" -> new ChoosableDisplayElement<>(DisplayElementType.COLLISION_SHAPE, modelView, idObject, () -> DisplayElementType.COLLISION_SHAPE.getName() + " \"" + idObject.getName() + "\"");
			default -> null;
		};
	}


	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseExited(final MouseEvent e) {
//				System.out.println("[MCBT]mouseExited");
				modelHandler.getModelView().higthlight(null);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath pathForLocation = getPathForLocation(e.getX(), e.getY());
				if (pathForLocation == null) {
					modelHandler.getModelView().higthlight(null);
				} else if (pathForLocation.getLastPathComponent() instanceof ModelTreeNode<?> lastPathComponent) {
					modelHandler.getModelView().higthlight(lastPathComponent.getItem());
				}
			}
		};
	}
//	Image attachmentImage = IconUtils.loadNodeImage("/attachment" + template + ".png");
//	Image eventImage = IconUtils.loadNodeImage("/event" + template + ".png");
//	Image lightImage = IconUtils.loadNodeImage("/light" + template + ".png");
//	Image particleImage = IconUtils.loadNodeImage("/particle1" + template + ".png");
//	Image particle2Image = IconUtils.loadNodeImage("/particle2" + template + ".png");
//	Image ribbonImage = IconUtils.loadNodeImage("/ribbon" + template + ".png");
//	Image collisionImage = IconUtils.loadNodeImage("/collision" + template + ".png");

}
