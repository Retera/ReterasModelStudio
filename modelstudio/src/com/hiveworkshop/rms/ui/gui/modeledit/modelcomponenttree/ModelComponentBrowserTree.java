package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public final class ModelComponentBrowserTree extends JTree {
	private final ModelHandler modelHandler;
	private Map<IdObject, DefaultMutableTreeNode> nodeToTreeElement;
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
		basicTreeUI.setRightChildIndent(8);


		componentsPanel = new ComponentsPanel(modelHandler);
		addSelectListener(componentsPanel);
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

	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {

		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.MODEL_ROOT, modelView, model).setNameFunc(() -> "Model \"" + model.getHeaderName() + "\""));

		root.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.COMMENT, modelView, model.getHeader()).setNameFunc(() -> "Comment")));

		root.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.HEADER, modelView, model).setNameFunc(() -> "Header")));

		DefaultMutableTreeNode sequences = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.ANIMATION, modelView, "Sequences").setNameFunc(() -> "Sequences"));
		root.add(sequences);
		for (Animation item : model.getAnims()) {
			sequences.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.ANIMATION, modelView, item).setNameFunc(() -> item.getName())));
		}

		DefaultMutableTreeNode globalSequences = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.GLOBAL_SEQ, modelView, "GlobalSequences").setNameFunc(() -> "GlobalSequences"));
		root.add(globalSequences);
		for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
			globalSequences.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.GLOBAL_SEQ, modelView, globalSeq).setNameFunc(() -> "GlobalSequence " + model.getGlobalSeqId(globalSeq) + ": Duration " + globalSeq.getLength())));
		}

		DefaultMutableTreeNode textures = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.TEXTURE, modelView, "Textures").setNameFunc(() -> "Textures"));
		root.add(textures);
		int number = 0;
		for (Bitmap item : model.getTextures()) {
			textures.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.TEXTURE, modelView, item, number++).setNameFunc(() -> item.getName())));
		}

		DefaultMutableTreeNode materials = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.MATERIAL, modelView, "Materials").setNameFunc(() -> "Materials"));
		root.add(materials);
		number = 0;
		for (Material item : model.getMaterials()) {
			int number1 = number;
			materials.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.MATERIAL, modelView, item, number++).setNameFunc(() -> "# " + number1 + " " + item.getName())));
		}

		DefaultMutableTreeNode textureAnims = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.TEXTURE_ANIM, modelView, "TextureAnims").setNameFunc(() -> "TextureAnims"));
		root.add(textureAnims);
		number = 0;
		for (TextureAnim item : model.getTexAnims()) {
			textureAnims.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.TEXTURE_ANIM, modelView, item, number++).setNameFunc(() -> "# " + model.getTexAnims().indexOf(item))));
		}

		DefaultMutableTreeNode geosets = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.GEOSET_ITEM, modelView, "Geosets").setNameFunc(() -> "Geosets"));
		root.add(geosets);
		number = 0;
		for (Geoset item : model.getGeosets()) {
			ChoosableDisplayElement<Geoset> geosetItem2 = new ChoosableDisplayElement<>(DisplayElementType.GEOSET_ITEM, modelView, item, number++).setNameFunc(() -> item.getName());

			geosets.add(new DefaultMutableTreeNode(geosetItem2));
		}

//		DefaultMutableTreeNode geosetAnims = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.GEOSET_ANIM, modelView, "GeosetAnims").setNameFunc(() -> "GeosetAnims"));
//		for (GeosetAnim item : model.getGeosetAnims()) {
//			geosetAnims.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.GEOSET_ANIM, modelView, item).setNameFunc(() -> "# " + model.getGeosets().indexOf(item.getGeoset()))));
//		}
//		root.add(geosetAnims);


		DefaultMutableTreeNode nodes = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.NODES, modelView, "Nodes").setNameFunc(() -> "Nodes"));
		root.add(nodes);

		nodeToTreeElement = new HashMap<>();
		nodeToTreeElement.put(null, nodes);

		// Create all treeNodes
		for (IdObject idObject : model.getIdObjects()) {
			ChoosableDisplayElement<?> element = getIdObjectElement(modelView, idObject);
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(element);

			nodeToTreeElement.put(idObject, treeNode);
		}

		// Link all treeNodes
		for (IdObject idObject : model.getIdObjects()) {
			IdObject parent = idObject.getParent();

			DefaultMutableTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			parentTreeNode.add(nodeToTreeElement.get(idObject));
		}

		DefaultMutableTreeNode cameras = new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.CAMERA, modelView, null).setNameFunc(() -> "Cameras"));
		root.add(cameras);
		for (Camera item : model.getCameras()) {
			cameras.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.CAMERA, modelView, item).setNameFunc(() -> "Camera \"" + item.getName() + "\"")));
		}

		number = 0;
		if (!model.getFaceEffects().isEmpty()) {
			for (FaceEffect faceEffect : model.getFaceEffects()) {
				root.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.FACEFX, modelView, faceEffect, number++).setNameFunc(() -> DisplayElementType.FACEFX.getName() + " \"" + faceEffect.getFaceEffectTarget() + "\"")));
			}
		}
		if (model.getBindPoseChunk() != null) {
			root.add(new DefaultMutableTreeNode(new ChoosableDisplayElement<>(DisplayElementType.BINDPOSE, modelView, model.getBindPoseChunk()).setNameFunc(() -> "BindPose")));
		}
		return new DefaultTreeModel(root);
	}

	public void addSelectListener(ComponentsPanel componentsPanel) {
		addTreeSelectionListener(e -> {
			TreePath path = e.getNewLeadSelectionPath();
			boolean selected = false;

			if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

				if (node.getUserObject() instanceof ChoosableDisplayElement) {
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
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {
				ImageIcon iconOverride = null;
				if (value instanceof DefaultMutableTreeNode) {
					Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
					if (userObject instanceof ChoosableDisplayElement) {
						ImageIcon icon = ((ChoosableDisplayElement<?>) userObject).getIcon(expanded);
						if (icon != null) {
							iconOverride = icon;
						}
					}
				}
				Component treeCellRendererComponent =
						super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				if (iconOverride != null) {
					setIcon(iconOverride);
				}
				return treeCellRendererComponent;
			}
		};
	}

	private ChoosableDisplayElement<?> asElement(Object userObject) {
		return (ChoosableDisplayElement<?>) userObject;
	}

	private ChoosableDisplayElement<?> asElement(TreePath path) {
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			ChoosableDisplayElement<?> userObject = asElement(node);
			if (userObject != null) return userObject;
		}
		return null;
	}

	private ChoosableDisplayElement<?> asElement(DefaultMutableTreeNode node) {
		if (node != null) {
			Object userObject = node.getUserObject();
			if (userObject instanceof ChoosableDisplayElement) {
				return (ChoosableDisplayElement<?>) userObject;
			}
		}
		return null;
	}

	public ModelComponentBrowserTree reloadFromModelView() {
//		System.out.println("Reloading ModelComponentBrowserTree");
		SwingUtilities.invokeLater(() -> {
			TreePath selectionPath = getSelectionPath();
			System.out.println("selectionPath: " + selectionPath);

			TreePath rootPath = new TreePath(getModel().getRoot());
			System.out.println("rootPath: " + rootPath);


			Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
			setModel(buildTreeModel(modelHandler));

			TreePath newRootPath = new TreePath(getModel().getRoot());
			System.out.println("newRootPath: " + newRootPath);

			List<TreePath> pathsToExpand = new ArrayList<>();

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

				ChoosableDisplayElement<?> element = asElement((DefaultMutableTreeNode) selectionPath.getLastPathComponent());
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
		return this;
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


	public static ChoosableDisplayElement<?> getIdObjectElement(ModelView modelView, IdObject idObject) {
		return switch (idObject.getClass().getSimpleName()) {
			case "Helper" -> new ChoosableDisplayElement<>(DisplayElementType.HELPER, modelView, idObject).setNameFunc(() -> DisplayElementType.HELPER.getName() + " \"" + idObject.getName() + "\"");
			case "Bone" -> new ChoosableDisplayElement<>(DisplayElementType.BONE, modelView, idObject).setNameFunc(() -> DisplayElementType.BONE.getName() + " \"" + idObject.getName() + "\"");
			case "Light" -> new ChoosableDisplayElement<>(DisplayElementType.LIGHT, modelView, idObject).setNameFunc(() -> DisplayElementType.LIGHT.getName() + " \"" + idObject.getName() + "\"");
			case "Attachment" -> new ChoosableDisplayElement<>(DisplayElementType.ATTACHMENT, modelView, idObject).setNameFunc(() -> DisplayElementType.ATTACHMENT.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitter" -> new ChoosableDisplayElement<>(DisplayElementType.PARTICLE, modelView, idObject).setNameFunc(() -> DisplayElementType.PARTICLE.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitter2" -> new ChoosableDisplayElement<>(DisplayElementType.PARTICLE2, modelView, idObject).setNameFunc(() -> DisplayElementType.PARTICLE2.getName() + " \"" + idObject.getName() + "\"");
			case "ParticleEmitterPopcorn" -> new ChoosableDisplayElement<>(DisplayElementType.POPCORN, modelView, idObject).setNameFunc(() -> DisplayElementType.POPCORN.getName() + " \"" + idObject.getName() + "\"");
			case "RibbonEmitter" -> new ChoosableDisplayElement<>(DisplayElementType.RIBBON, modelView, idObject).setNameFunc(() -> DisplayElementType.RIBBON.getName() + " \"" + idObject.getName() + "\"");
			case "EventObject" -> new ChoosableDisplayElement<>(DisplayElementType.EVENT_OBJECT, modelView, idObject).setNameFunc(() -> DisplayElementType.EVENT_OBJECT.getName() + " \"" + idObject.getName() + "\"");
			case "CollisionShape" -> new ChoosableDisplayElement<>(DisplayElementType.COLLISION_SHAPE, modelView, idObject).setNameFunc(() -> DisplayElementType.COLLISION_SHAPE.getName() + " \"" + idObject.getName() + "\"");
			default -> null;
		};
	}


	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("mouseExited");
				modelHandler.getModelView().higthlight(null);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath pathForLocation = getPathForLocation(e.getX(), e.getY());
				if (pathForLocation == null) {
					modelHandler.getModelView().higthlight(null);
				} else {
					DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
					ChoosableDisplayElement<?> element = (ChoosableDisplayElement<?>) lastPathComponent.getUserObject();
					if (element != null) {
						modelHandler.getModelView().higthlight(element.getItem());
					}
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
