package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SplitGeosetAction;
import com.hiveworkshop.rms.editor.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.rms.editor.actions.tools.SetHdSkinAction;
import com.hiveworkshop.rms.editor.actions.tools.SetMatrixAction3;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.CreateFace;
import com.hiveworkshop.rms.ui.application.actionfunctions.RigSelection;
import com.hiveworkshop.rms.ui.application.actionfunctions.ViewSkinning;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.MatrixPopup;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.SkinPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

public class ViewportPopupMenu extends JPopupMenu {
	ViewportAxis[] axises = {
			new ViewportAxis("Front", (byte) 1, (byte) 2),
			new ViewportAxis("Left", (byte) -1, (byte) 2),
			new ViewportAxis("Back", (byte) -2, (byte) 2),
			new ViewportAxis("Right", (byte) 0, (byte) 2),
			new ViewportAxis("Top", (byte) 1, (byte) -1),
			new ViewportAxis("Bottom", (byte) 1, (byte) 0),
			new ViewportAxis("Top90", (byte) 0, (byte) 1),
			new ViewportAxis("Top180", (byte) -2, (byte) 0),
			new ViewportAxis("Top270", (byte) -1, (byte) -2),
			new ViewportAxis("Bottom90", (byte) -1, (byte) 1),
			new ViewportAxis("Bottom180", (byte) -2, (byte) -1),
			new ViewportAxis("Bottom270", (byte) 0, (byte) -2),
	};

	Component parent;
	ModelEditorManager modelEditorManager;
	ModelHandler modelHandler;

	public ViewportPopupMenu(PerspectiveViewport perspectiveViewport, Component parent, ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		this.parent = parent;
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;

		JMenu viewMenu = new JMenu("View");
		add(viewMenu);

		addMenuItem("Front", e -> changeViewportAxis(axises[0]), viewMenu);
		addMenuItem("Back", e -> changeViewportAxis(axises[2]), viewMenu);
		addMenuItem("Top", e -> changeViewportAxis(axises[4]), viewMenu);
		addMenuItem("Bottom", e -> changeViewportAxis(axises[5]), viewMenu);
		addMenuItem("Left", e -> changeViewportAxis(axises[1]), viewMenu);
		addMenuItem("Right", e -> changeViewportAxis(axises[3]), viewMenu);
//		addMenuItem("Front" , new ChangeViewportAxisAction("Front" , (byte)  1, (byte)  2), viewMenu);
//		addMenuItem("Back"  , new ChangeViewportAxisAction("Back"  , (byte) -2, (byte)  2), viewMenu);
//		addMenuItem("Top"   , new ChangeViewportAxisAction("Top"   , (byte)  1, (byte) -1), viewMenu);
//		addMenuItem("Bottom", new ChangeViewportAxisAction("Bottom", (byte)  1, (byte)  0), viewMenu);
//		addMenuItem("Left"  , new ChangeViewportAxisAction("Left"  , (byte) -1, (byte)  2), viewMenu);
//		addMenuItem("Right" , new ChangeViewportAxisAction("Right" , (byte)  0, (byte)  2), viewMenu);

		JMenu meshMenu = new JMenu("Mesh");
		add(meshMenu);

		JMenuItem createFace = new JMenuItem("Create Face");
		createFace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		createFace.addActionListener(e -> createFace());
		meshMenu.add(createFace);

		addMenuItem("Split Geoset and Add Team Color", e -> addTeamColor(modelHandler), meshMenu);
		addMenuItem("Split Geoset", e -> splitGeoset(modelHandler), meshMenu);

		JMenu editMenu = new JMenu("Edit");
		add(editMenu);

		addMenuItem("Translation Type-in", e -> manualMove(this.parent), editMenu);
		addMenuItem("Rotate Type-in", e -> manualRotate(this.parent), editMenu);
		addMenuItem("Position Type-in", e -> manualSet(this.parent), editMenu);
		addMenuItem("Scale Type-in", e -> manualScale(this.parent), editMenu);

		JMenu matrixMenu = new JMenu("Rig");
		add(matrixMenu);

		addMenuItem("Selected Mesh to Selected Nodes", e -> RigSelection.doRig(modelHandler), matrixMenu);
		addMenuItem("Re-assign Matrix", e -> reAssignMatrix(this.parent), matrixMenu);
		addMenuItem("View Matrix", e -> ViewSkinning.viewMatrices(modelHandler), matrixMenu);
		addMenuItem("Re-assign HD Skin", e -> reAssignSkinning(this.parent), matrixMenu);
		addMenuItem("View HD Skin", e -> ViewSkinning.viewHDSkinning(modelHandler), matrixMenu);
		addMenuItem("View Skinning", e -> ViewSkinning.viewSkinning(modelHandler), matrixMenu);

		JMenu nodeMenu = new JMenu("Node");
		add(nodeMenu);

		addMenuItem("Set Parent", e -> setParent(this.parent), nodeMenu);
		addMenuItem("Auto-Center Bone(s)", e -> modelHandler.getUndoManager().pushAction(autoCenterSelectedBones(modelHandler.getModelView())), nodeMenu);
		addMenuItem("Rename Nodes", e -> renameNodes(this.parent), nodeMenu);
		addMenuItem("Append Bone Suffix", e -> appendBoneSuffix(this.parent), nodeMenu);
	}

//	public Viewport getViewport() {
//		return viewport;
//	}

	public UndoAction autoCenterSelectedBones(ModelView modelView) {
		Set<IdObject> selBones = new HashSet<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(b)) {
				selBones.add(b);
			}
		}

		Map<Geoset, Map<Bone, List<GeosetVertex>>> geosetBoneMaps = new HashMap<>();
		for (Geoset geo : modelView.getModel().getGeosets()) {
			geosetBoneMaps.put(geo, geo.getBoneMap());
		}

		Map<Bone, Vec3> boneToOldPosition = new HashMap<>();
		for (IdObject obj : selBones) {
			if (obj instanceof Bone) {
				Bone bone = (Bone) obj;
				List<GeosetVertex> childVerts = new ArrayList<>();
				for (Geoset geo : modelView.getModel().getGeosets()) {
					List<GeosetVertex> vertices = geosetBoneMaps.get(geo).get(bone);
					if (vertices != null) {
						childVerts.addAll(vertices);
					}
				}
				if (childVerts.size() > 0) {
					Vec3 pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vec3(pivotPoint));
					pivotPoint.set(Vec3.centerOfGroup(childVerts));
				}
			}
		}
		return new AutoCenterBonesAction(boneToOldPosition);
	}

	public void splitGeoset(ModelHandler modelHandler) {
		UndoAction splitGeosetAction = new SplitGeosetAction(
				modelHandler.getModel(),
				modelHandler.getModelView().getSelectedVertices(),
				ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(splitGeosetAction.redo());
	}


	public void addTeamColor(ModelHandler modelHandler) {
		if(modelHandler != null){
			ModelView modelView = modelHandler.getModelView();
			UndoAction action = new TeamColorAddAction(modelView.getSelectedVertices(), modelView, ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}



	static void addMenuItem(String itemText, ActionListener actionListener, JMenu menu) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);
	}

	//	void createFace(Viewport viewport) {
	void createFace() {
		// todo make this work with CameraHandler
		try {
//			if (viewport != null) {
//				UndoAction faceFromSelection = ModelEditActions.createFaceFromSelection(modelHandler.getModelView(), viewport.getFacingVector());
//				modelHandler.getUndoManager().pushAction(faceFromSelection.redo());
//			}
			UndoAction faceFromSelection = CreateFace.createFaceFromSelection(modelHandler.getModelView(), null);
			if (faceFromSelection != null) {
				modelHandler.getUndoManager().pushAction(faceFromSelection.redo());
			}
		} catch (final FaceCreationException exc) {
//			JOptionPane.showMessageDialog(viewport, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	void reAssignMatrix(Component parent) {
		MatrixPopup matrixPopup = new MatrixPopup(modelHandler);
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(parent, matrixPopup, "Rebuild Matrix", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			Set<GeosetVertex> selectedVertices = modelHandler.getModelView().getSelectedVertices();
			modelHandler.getUndoManager()
					.pushAction(new SetMatrixAction3(selectedVertices, matrixPopup.getNewBoneList(), matrixPopup.getBonesNotInAll(), ModelStructureChangeListener.changeListener).redo());
		}
	}

	void reAssignSkinning(Component parent) {
		SkinPopup skinPopup = new SkinPopup(modelHandler.getModelView());
		String[] words = {"Accept", "Cancel"};
		int i = JOptionPane.showOptionDialog(parent, skinPopup, "Rebuild Skin", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, words, words[1]);
		if (i == 0) {
			SetHdSkinAction hdSkinAction = new SetHdSkinAction(modelHandler.getModelView().getSelectedVertices(), skinPopup.getBones(), skinPopup.getSkinWeights(), ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(hdSkinAction.redo());
		}
	}

	void appendBoneSuffix(Component parent) {
		if (!modelHandler.getModelView().getSelectedIdObjects().isEmpty()) {
			String suffix = JOptionPane.showInputDialog(parent, "Enter bone suffix:");
			if (suffix != null && !suffix.isBlank() && !modelHandler.getModelView().getSelectedIdObjects().isEmpty()) {
				List<UndoAction> actions = new ArrayList<>();
				for (IdObject node : modelHandler.getModelView().getSelectedIdObjects()) {
					actions.add(new NameChangeAction(node, node.getName() + suffix, null));
				}
				modelHandler.getUndoManager().pushAction(new CompoundAction("add selected bone suffix", actions, ModelStructureChangeListener.changeListener::nodesUpdated).redo());
			}
		} else {
			JOptionPane.showMessageDialog(parent, "No node(s) selected");
		}
	}

	void renameNodes(Component parent) {
		Set<IdObject> selectedIdObjects = modelHandler.getModelView().getSelectedIdObjects();
		if (selectedIdObjects.size() == 0) {
			JOptionPane.showMessageDialog(parent, "No node is selected");
		} else {
			Map<IdObject, String> nodeToNewName = new HashMap<>();
			ModelIconHandler modelIconHandler = new ModelIconHandler();

			JPanel renamePanel = new JPanel(new MigLayout());
			for(IdObject idObject : selectedIdObjects) {
				nodeToNewName.put(idObject, idObject.getName());
				renamePanel.add(new JLabel(idObject.getName(), modelIconHandler.getImageIcon(idObject, modelHandler.getModel()), SwingConstants.LEFT));
				renamePanel.add(new TwiTextField(idObject.getName(), 24, s -> nodeToNewName.put(idObject, s)), "wrap");
			}

			JScrollPane scrollPane = new JScrollPane(renamePanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);

			int panelSize = Math.min(renamePanel.getPreferredSize().height+5, ScreenInfo.getSmallWindow().height);
			JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow][]", "[grow," + panelSize + "]"));
			panel.add(scrollPane, "growx, growy");
			if(ScreenInfo.getSmallWindow().height<=panelSize){
				panel.add(scrollPane.getVerticalScrollBar(), "growy");
			} else {
				// this makes sure that the vertical scrollbar won't become visible without blocking scrolling
				new JPanel().add(scrollPane.getVerticalScrollBar());
			}
			int option = JOptionPane.showConfirmDialog(parent, panel, "Rename Nodes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

			if(option == JOptionPane.OK_OPTION){
				List<UndoAction> undoActions = new ArrayList<>();
				for (IdObject idObject : nodeToNewName.keySet()){
					String newName = nodeToNewName.get(idObject);
					if(!newName.isBlank() && !idObject.getName().equals(newName)){
						undoActions.add(new NameChangeAction(idObject, newName, null));
					}
				}
				if (undoActions.size() == 1){
					modelHandler.getUndoManager().pushAction(new CompoundAction(undoActions.get(0).actionName(), undoActions, ModelStructureChangeListener.changeListener::nodeHierarchyChanged).redo());
				} else if(!undoActions.isEmpty()) {
					modelHandler.getUndoManager().pushAction(new CompoundAction("Rename " + undoActions.size() + " Nodes", undoActions, ModelStructureChangeListener.changeListener::nodeHierarchyChanged).redo());
				}
			}
		}

	}

	void setParent(Component parent) {
		class NodeShell {
			IdObject node;

			public NodeShell(IdObject node) {
				this.node = node;
			}

			public IdObject getNode() {
				return node;
			}

			@Override
			public String toString() {
				if (node == null) {
					return "(No parent)";
				}
				return node.getName();
			}
		}

		List<IdObject> idObjects = modelHandler.getModel().getIdObjects();
		NodeShell[] nodeOptions = new NodeShell[idObjects.size() + 1];
		nodeOptions[0] = new NodeShell(null);
		NodeShell defaultChoice = nodeOptions[0];
		for (int i = 0; i < idObjects.size(); i++) {
			IdObject node = idObjects.get(i);
			nodeOptions[i + 1] = new NodeShell(node);
		}
		NodeShell result = (NodeShell) JOptionPane.showInputDialog(parent, "Choose a parent node", "Set Parent Node", JOptionPane.PLAIN_MESSAGE, null, nodeOptions, defaultChoice);
		if (result != null) {
			Set<IdObject> selectedIdObjects = modelHandler.getModelView().getSelectedIdObjects();
			modelHandler.getUndoManager()
					.pushAction(new SetParentAction(selectedIdObjects, result.getNode(), ModelStructureChangeListener.changeListener).redo());
		}
	}

	void manualMove(Component parent) {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Move X:", "Move Y:", "Move Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel());

		int x = JOptionPane.showConfirmDialog(parent, inputPanel, "Manual Translation", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}

		if (!modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().translate(spinners.getValue(), new Mat4()).redo());
		}
	}

	void manualRotate(Component parent) {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Rotate X degrees (around axis facing front):", "Rotate Y degrees (around axis facing left):", "Rotate Z degrees (around axis facing up):");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel());

		int x = JOptionPane.showConfirmDialog(parent, inputPanel, "Manual Rotation", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}

		if (!modelHandler.getModelView().isEmpty()) {
			Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().rotate(selectionCenter, spinners.getValue(), new Mat4()).redo());
		}
	}

	void manualSet(Component parent) {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "New Position X:", "New Position Y:", "New Position Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel());
		int x = JOptionPane.showConfirmDialog(parent, inputPanel, "Manual Position", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		if (!modelHandler.getModelView().isEmpty()) {
			Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager()
					.pushAction(modelEditorManager.getModelEditor().setPosition(selectionCenter, spinners.getValue()).redo());
		}
	}

	void manualScale(Component parent) {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(1, 1, 1), "Scale X:", "Scale Y:", "Scale Z:");
		inputPanel.add(spinners.spinnerPanel(), "wrap");
		JCheckBox customOrigin = new JCheckBox("Custom Scaling Origin");
		inputPanel.add(customOrigin, "wrap");

//		Vec3 selectionCenter = modelEditorManager.getModelEditor().getSelectionCenter();
		Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
		if (Double.isNaN(selectionCenter.x)) {
			selectionCenter = new Vec3(0, 0, 0);
		}
		Vec3SpinnerArray centerSpinners = new Vec3SpinnerArray(selectionCenter, "Center X:", "Center Y:", "Center Z:");
		inputPanel.add(centerSpinners.spinnerPanel());
		centerSpinners.setEnabled(false);
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));

		int x = JOptionPane.showConfirmDialog(parent, inputPanel, "Manual Scaling", JOptionPane.OK_CANCEL_OPTION);
		if (x != JOptionPane.OK_OPTION) {
			return;
		}
		Vec3 center = selectionCenter;
		if (customOrigin.isSelected()) {
			center = centerSpinners.getValue();
		}
		if (!modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager()
					.pushAction(modelEditorManager.getModelEditor().scale(center, spinners.getValue(), new Mat4()).redo());
		}
	}

	private void changeViewportAxis(ViewportAxis axis) {
//		viewport.setViewportAxises(axis.name, axis.dim1, axis.dim2);
	}

	private class ViewportAxis {
		private final String name;
		private final byte dim1;
		private final byte dim2;

		public ViewportAxis(String name, byte dim1, byte dim2) {
			this.name = name;
			this.dim1 = dim1;
			this.dim2 = dim2;
		}
	}
}
