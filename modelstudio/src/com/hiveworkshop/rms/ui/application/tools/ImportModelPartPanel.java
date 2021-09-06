package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.*;

public class ImportModelPartPanel extends JPanel {
	EditableModel donModel;
	EditableModel recModel;
	ModelHandler recModelHandler;
	BoneChooser boneChooser;


	AnimListCellRenderer donRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> donAnimations = new IterableListModel<>();
	JList<AnimShell> donAnimList = new JList<>(donAnimations);
	JScrollPane donAnimPane = new JScrollPane(donAnimList);

	AnimListCellRenderer recRenderer = new AnimListCellRenderer(true); //ToDo: make a new renderer and only use Animation
	IterableListModel<AnimShell> recAnimations = new IterableListModel<>();
	JList<AnimShell> recAnimList = new JList<>(recAnimations);
	JScrollPane recAnimPane = new JScrollPane(recAnimList);

	BoneShellListCellRenderer donBoneRenderer;
	IterableListModel<BoneShell> donBones = new IterableListModel<>();
	JList<BoneShell> donBoneList = new JList<>(donBones);
	JScrollPane donBonePane = new JScrollPane(donBoneList);
	Bone choosenBone = null;

	public ImportModelPartPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(new MigLayout());
		this.donModel = donModel;
		this.recModel = recModelHandler.getModel();
		this.recModelHandler = recModelHandler;
		boneChooser = new BoneChooser(donModel);

		fillLists(donModel, recModel);

		JButton chooseBone = new JButton("Choose Bone");
//		chooseBone.addActionListener(e -> showStartBonePanel(chooseBone));
		chooseBone.addActionListener(e -> chooseParent(chooseBone));
		add(chooseBone, "wrap");
		add(getAnimMapPanel(), "wrap");

		JButton importButton = new JButton("Import!");
//		importButton.addActionListener(e -> doImport(donBoneList.getSelectedValue()));
		importButton.addActionListener(e -> doImport(choosenBone));
		add(importButton, "wrap");
	}

	private void chooseParent(JButton chooseBone) {
		choosenBone = boneChooser.chooseBone(choosenBone, this);
		if (choosenBone != null) {
			chooseBone.setText(choosenBone.getName());
		} else {
			chooseBone.setText("Choose Bone");
		}
		repaint();
	}

	private void fillLists(EditableModel donModel, EditableModel recModel) {
		for (IdObject object : donModel.getIdObjects()) {
			if (object instanceof Bone) {
				donBones.addElement(new BoneShell((Bone) object, true));
			}
		}
		donBoneRenderer = new BoneShellListCellRenderer(recModel, donModel);
		for (Animation animation : donModel.getAnims()) {
			donAnimations.addElement(new AnimShell(animation));
		}
		donAnimList.setCellRenderer(donRenderer);
		for (Animation animation : recModel.getAnims()) {
			recAnimations.addElement(new AnimShell(animation));
		}
		recAnimList.setCellRenderer(recRenderer);
	}

	private JPanel getAnimMapPanel() {
		JPanel animMapPanel = new JPanel(new MigLayout());
		animMapPanel.add(donAnimPane, "spany");
		donAnimList.addListSelectionListener(e -> donAnimationSelectionChanged(e));
		recAnimList.addListSelectionListener(e -> recAnimationSelectionChanged(e));
		JPanel resPanel = new JPanel(new MigLayout());
		resPanel.add(new JCheckBox("import", true), "wrap");
		resPanel.add(new JLabel("Map to:"), "wrap");
		resPanel.add(recAnimPane, "wrap");
		animMapPanel.add(resPanel);
		return animMapPanel;
	}

	private void donAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && donAnimList.getSelectedValue() != null) {
			recAnimList.setSelectedValue(donAnimList.getSelectedValue().getImportAnimShell(), true);
		}
	}

	private void recAnimationSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() && donAnimList.getSelectedValue() != null) {
			donAnimList.getSelectedValue().setImportAnimShell(recAnimList.getSelectedValue());
		}
	}

	private JPanel getStartBonePanel() {
		JPanel boneChooserPanelPanel = new JPanel(new MigLayout());
		return boneChooserPanelPanel;
	}

	private BoneShell showStartBonePanel(JButton button) {
		BoneShell beforeBone = donBoneList.getSelectedValue();
		JPanel boneChooserPanelPanel = new JPanel(new MigLayout());
		boneChooserPanelPanel.add(new JLabel("Choose first bone/helper in bone chain to be imported"), "wrap");
		boneChooserPanelPanel.add(donBonePane, "wrap");
		int opt = JOptionPane.showConfirmDialog(this, boneChooserPanelPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION);
		if (opt == JOptionPane.OK_OPTION) {
			BoneShell boneShell = donBoneList.getSelectedValue();
			if (boneShell != null) {
				button.setText(boneShell.getName());
			} else {
				button.setText("Choose Bone");
			}
			return boneShell;
		}
		return beforeBone;
	}

	private void doImport(Bone bone) {
		if (bone != null) {
			bone.setParent(null);
			Set<IdObject> selectedObjects = new HashSet<>();
			addToList(bone, selectedObjects);
			Set<Bone> selectedBones = new HashSet<>();
			selectedObjects.stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));
//		Set<GeosetVertex> vertexSet = getVertexSet(selectedBones);
			Set<Geoset> newGeosets = getNewGeosets(selectedBones);
			Map<Sequence, Sequence> sequenceSequenceMap = new HashMap<>();
			for (AnimShell animShell : donAnimations) {
				if (animShell.getImportAnimShell() != null) {
					sequenceSequenceMap.put(animShell.getAnim(), animShell.getImportAnimShell().getAnim());
				}
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();
			System.out.println(selectedObjects.size());
			for (IdObject idObject : selectedObjects) {
				for (AnimFlag<?> animFlag : idObject.getAnimFlags()) {

//					for (Sequence sequence : animFlag.getAnimMap().keySet()){
					for (AnimShell animShell : donAnimations) {
						Sequence sequence = animShell.getAnim();
						if (sequenceSequenceMap.containsKey(sequence)) {
							animFlag.copyFrom(animFlag, sequence, sequenceSequenceMap.get(sequence));
						} else {
							animFlag.deleteAnim(sequence);
						}
					}
//					for (Sequence sequence : sequenceSequenceMap.keySet()){
//						animFlag.copyFrom(animFlag, sequence, sequenceSequenceMap.get(sequence));
//					}
				}
				undoActions.add(new AddNodeAction(recModel, idObject, null));
			}
			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.getMatrices().clear();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
//			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, () -> recModelHandler.getModelView().updateElements());
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(selectedObjects, addedVertexes), recModelHandler.getModelView(), "");
//		undoActions.add(selectionAction);
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::nodesUpdated, addedModelPart, selectionAction).redo());
		}

	}

	private void addToList(IdObject object, Set<IdObject> objectSet) {
		objectSet.add(object);
		System.out.println(object.getName());
		for (IdObject child : object.getChildrenNodes()) {
			addToList(child, objectSet);
		}
	}

	private Set<GeosetVertex> getVertexSet(Set<Bone> selectedBones) {
		Set<GeosetVertex> vertexList = new HashSet<>();
		for (Geoset geoset : donModel.getGeosets()) {
			for (Bone bone : selectedBones) {
				List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
				if (vertices != null) {
					vertexList.addAll(vertices);
				}
			}
		}
		return vertexList;
	}

	private Set<Geoset> getNewGeosets(Set<Bone> selectedBones) {
		Set<Geoset> newGeosets = new HashSet<>();
		for (Geoset geoset : donModel.getGeosets()) {
			Set<GeosetVertex> vertexList = new HashSet<>();
			for (Bone bone : selectedBones) {
				List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
				if (vertices != null) {
					vertexList.addAll(vertices);
				}
			}
			if (!vertexList.isEmpty()) {
				for (GeosetVertex vertex : vertexList) {
					if (vertex.getSkinBones() != null) {
						for (GeosetVertex.SkinBone skinBone : vertex.getSkinBones()) {
							if (skinBone != null && skinBone.getBone() != null) {
								skinBone.setBone(null);
							}
						}
					} else {
						System.out.println("Vert bones bf: " + vertex.getMatrix().size());
						Set<Bone> vertBones = new HashSet<>(vertex.getBones());
						vertBones.removeAll(selectedBones);
						vertex.removeBones(vertBones);
						System.out.println("Vert bones ressss: " + vertex.getMatrix().size());

					}
				}
				Set<Triangle> trianglesToRemove = new HashSet<>();
				for (Triangle triangle : geoset.getTriangles()) {
					if (!containsAll(triangle, vertexList)) {
						trianglesToRemove.add(triangle);
					}
				}
				Set<GeosetVertex> verticesToCull = new HashSet<>();
				for (Triangle triangle : trianglesToRemove) {
					verticesToCull.add(triangle.get(0));
					verticesToCull.add(triangle.get(1));
					verticesToCull.add(triangle.get(2));
					geoset.removeExtended(triangle);
				}
				verticesToCull.removeAll(vertexList);
				geoset.remove(verticesToCull);
				geoset.setParentModel(recModel);
				newGeosets.add(geoset);
			}

		}
		return newGeosets;
	}

	private boolean containsAll(Triangle triangle, Set<GeosetVertex> vertexSet) {
		return vertexSet.contains(triangle.get(0))
				&& vertexSet.contains(triangle.get(1))
				&& vertexSet.contains(triangle.get(2));
	}

}
