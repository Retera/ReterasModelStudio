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
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class ImportModelPartPanel extends TwiImportPanel {

	public ImportModelPartPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 0][grow 0][grow][grow 0]"));

		add(new JLabel("Part source"), "");
		add(new JLabel("Parent"), "");

		recBoneButtonText = "Choose Parent";
		add(getButton(donBoneButtonText, this::chooseDonBone, donModel), "");
		add(getButton(recBoneButtonText, this::chooseRecBone, recModel), "");
		add(getBoneOptionPanel(), "spanx");
		add(getAnimMapPanel(), "spanx, growx, growy");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(chosenDonBone, chosenRecBone));
		add(importButton, "");
	}

	private void doImport(Bone donBone, Bone recBone) {
		if (donBone != null) {
			Map<IdObject, IdObject> boneCopyMap = getBoneCopyMap(donBone); //obj to copy
			boneCopyMap.get(donBone).setParent(recBone);
//			Set<Bone> selectedBones = new HashSet<>();
//			boneCopyMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));

			Set<Bone> selectedBones = boneCopyMap.keySet().stream()
					.filter(idObject -> idObject instanceof Bone)
					.map(obj -> (Bone)obj)
					.collect(Collectors.toSet());
			int selectedBonesSize = selectedBones.size();

			Set<Geoset> newGeosets = getNewGeosets(selectedBones);

			if(selectedBones.size() != selectedBonesSize){
				for (Bone bone : selectedBones){
					boneCopyMap.computeIfAbsent(bone, k -> bone.copy());
				}
			}
			for(Geoset geoset : newGeosets){
				for(GeosetVertex vertex : geoset.getVertices()){
					vertex.replaceBones(boneCopyMap);
				}
			}

			Map<Sequence, Sequence> recToDonSequenceMap = getRecToDonSequenceMap();

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();
//			for (IdObject newIdObject : boneCopyMap.values()) {
//				for (AnimFlag<?> animFlag : newIdObject.getAnimFlags()) {
//					for (AnimShell donAnimShell : donAnimations) {
//						if (!recToDonSequenceMap.containsValue(donAnimShell.getAnim())) {
//							animFlag.deleteAnim(donAnimShell.getAnim());
//						}
//					}
//					for (Sequence recSequence : recToDonSequenceMap.keySet()) {
//						Sequence donSequence = recToDonSequenceMap.get(recSequence);
//						animFlag.copyFrom(animFlag, donSequence, recSequence);
//					}
//				}
//				undoActions.add(new AddNodeAction(recModel, newIdObject, null));
//			}
			for (IdObject idObject : boneCopyMap.keySet()) {
				IdObject newIdObject = boneCopyMap.get(idObject);
				for (AnimFlag<?> animFlag : newIdObject.getAnimFlags()) {
					AnimFlag<?> oldAnimFlag = idObject.find(animFlag.getName());
					animFlag.clear();
//					for (AnimShell donAnimShell : donAnimations) {
//						if (!recToDonSequenceMap.containsValue(donAnimShell.getAnim())) {
//							animFlag.deleteAnim(donAnimShell.getAnim());
//						}
//					}
					for (Sequence recSequence : recToDonSequenceMap.keySet()) {
						Sequence donSequence = recToDonSequenceMap.get(recSequence);
						animFlag.copyFrom(oldAnimFlag, donSequence, recSequence);
					}
				}
				undoActions.add(new AddNodeAction(recModel, newIdObject, null));
			}
			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.getMatrices().clear();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(boneCopyMap.values(), addedVertexes), recModelHandler.getModelView(), "");
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::nodesUpdated, addedModelPart, selectionAction).redo());
		}

	}

	private Map<IdObject, IdObject> getBoneCopyMap(IdObject donBone){
		Set<IdObject> chosenDonObjects = new HashSet<>();
		collectChildren(donBone, chosenDonObjects);
		Map<IdObject, IdObject> boneCopyMap = new HashMap<>();
		for (IdObject idObject : chosenDonObjects){
			boneCopyMap.put(idObject, idObject.copy());
		}
		for (IdObject newIdObject : boneCopyMap.values()){
			if(newIdObject.getParent() != null){
				newIdObject.setParent(boneCopyMap.get(newIdObject.getParent()));
			}
		}

		return boneCopyMap;
	}

	private void collectChildren(IdObject object, Set<IdObject> objectSet) {
		objectSet.add(object);
		System.out.println(object.getName());
		for (IdObject child : object.getChildrenNodes()) {
			collectChildren(child, objectSet);
		}
	}

//	private Set<GeosetVertex> getVertexSet(Set<Bone> selectedBones) {
//		Set<GeosetVertex> vertexList = new HashSet<>();
//		for (Geoset geoset : donModel.getGeosets()) {
//			for (Bone bone : selectedBones) {
//				List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
//				if (vertices != null) {
//					vertexList.addAll(vertices);
//				}
//			}
//		}
//		return vertexList;
//	}
//
//	private Set<Geoset> getNewGeosets(Set<Bone> selectedBones) {
//		Set<Geoset> newGeosets = new HashSet<>();
//		for (Geoset geoset : donModel.getGeosets()) {
//			Set<GeosetVertex> vertexList = new HashSet<>();
//			for (Bone bone : selectedBones) {
//				List<GeosetVertex> vertices = geoset.getBoneMap().get(bone);
//				if (vertices != null) {
//					vertexList.addAll(vertices);
//				}
//			}
//			if (!vertexList.isEmpty()) {
//				for (GeosetVertex vertex : vertexList) {
//					if (vertex.getSkinBones() != null) {
//						for (SkinBone skinBone : vertex.getSkinBones()) {
//							if (skinBone != null && skinBone.getBone() != null) {
//								skinBone.setBone(null);
//							}
//						}
//					} else {
//						System.out.println("Vert bones bf: " + vertex.getMatrix().size());
//						Set<Bone> vertBones = new HashSet<>(vertex.getBones());
//						vertBones.removeAll(selectedBones);
//						vertex.removeBones(vertBones);
//						System.out.println("Vert bones ressss: " + vertex.getMatrix().size());
//
//					}
//				}
//				Set<Triangle> trianglesToRemove = new HashSet<>();
//				for (Triangle triangle : geoset.getTriangles()) {
//					if (!containsAll(triangle, vertexList)) {
//						trianglesToRemove.add(triangle);
//					}
//				}
//				Set<GeosetVertex> verticesToCull = new HashSet<>();
//				for (Triangle triangle : trianglesToRemove) {
//					verticesToCull.add(triangle.get(0));
//					verticesToCull.add(triangle.get(1));
//					verticesToCull.add(triangle.get(2));
//					geoset.removeExtended(triangle);
//				}
//				verticesToCull.removeAll(vertexList);
//				geoset.remove(verticesToCull);
//				geoset.setParentModel(recModel);
//				newGeosets.add(geoset);
//			}
//
//		}
//		return newGeosets;
//	}
//
//	private boolean containsAll(Triangle triangle, Set<GeosetVertex> vertexSet) {
//		return vertexSet.contains(triangle.get(0))
//				&& vertexSet.contains(triangle.get(1))
//				&& vertexSet.contains(triangle.get(2));
//	}

}
