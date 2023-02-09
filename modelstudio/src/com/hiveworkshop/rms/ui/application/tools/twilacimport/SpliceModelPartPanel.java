package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class SpliceModelPartPanel extends TwiImportPanel {

	Map<IdObject, IdObject> chainMap;
	public SpliceModelPartPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 1][grow 1][grow 0]"));

		add(new JLabel("Part source"), "");
		add(new JLabel("Destination"), "");

		add(donBoneChooserButton, "");
		add(recBoneChooserButton, "");

		add(getBoneOptionPanel(), "spanx, wrap");

		BoneChainMapWizard boneChainMapWizard = new BoneChainMapWizard(this, recModel, donModel);
		JButton mapBonesButton = new JButton("Map Bones!");
		mapBonesButton.addActionListener(e -> mapBones(boneChainMapWizard, donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone(), boneChainDepth));
		add(mapBonesButton, "wrap");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(donBoneChooserButton.getChosenIdObject(), recBoneChooserButton.getChosenIdObject()));
		add(importButton, "");
	}


	private void mapBones(BoneChainMapWizard boneChainMapWizard, Bone donBone, Bone recBone, int boneChainDepth){
		boneChainMapWizard.editMapping(recBone, donBone, boneChainDepth, true);
		chainMap = boneChainMapWizard.fillAndGetChainMap();
	}

	private void doImport(IdObject donBone, IdObject recBone) {
		if (donBone != null) {
			Set<IdObject> chainToImport = new HashSet<>();
			collectChildren(donBone, chainToImport);

			if(chainMap == null){
				chainMap = getChainMap(recBone, recModel, donBone, donModel, boneChainDepth, true); // donating bones to receiving bones
			}

			Set<Bone> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));

			Set<IdObject> extendedImpChain = new HashSet<>(chainToImport);
			Set<Geoset> newGeosets = getNewGeosets(extendedImpChain);

			Map<IdObject, IdObject> parentMap = new HashMap<>();
			for (IdObject bone : extendedImpChain) {
				if (bone.getParent() != null && chainMap.get(bone) != null && chainMap.get(bone.getParent()) == null) {
					parentMap.put(bone.getParent(), chainMap.get(bone).getParent());
				}
			}

			List<IdObject> extraBones = new ArrayList<>();
			if (extendedImpChain.size() != selectedBones.size() && boneOption == BoneOption.importBones || boneOption == BoneOption.importBonesExtra) {
				for (IdObject bone : extendedImpChain) {
					if (chainMap.get(bone) == null) {
						System.out.println("adding bone copy of: " + bone.getName());
						IdObject copy = bone.copy();
						chainMap.put(bone, copy);
						extraBones.add(copy);
					}
				}
			}

			if (boneOption == BoneOption.rebindGeometry) {
				for (IdObject parent : parentMap.keySet()) {
					if (chainMap.get(parent) == null) {
						parentMap.put(parent, parentMap.get(parent));
					}
				}
			}

			boolean removeExtraBones = boneOption == BoneOption.importBones || boneOption == BoneOption.importBonesExtra;
			Set<Material> newMaterials = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				for (GeosetVertex vertex : geoset.getVertices()) {
//					vertex.replaceBones(chainMap, removeExtraBones);
					vertex.replaceBones(chainMap);
				}
				if(!recModel.contains(geoset.getMaterial())){
					newMaterials.add(geoset.getMaterial());
				}
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

			for (IdObject idObject : extraBones) {
				IdObject replParent = null;
				if(idObject.getParent() != null && !extraBones.contains(idObject.getParent())){
					replParent = chainMap.get(idObject.getParent());
					if(replParent != null){
						System.out.println("setting parent of " + idObject.getName() + " to null (" + replParent.getName() + ")");
					}
					idObject.setParent(null);

				}
//				IdObject newIdObject = boneCopyMap.get(idObject);
//				for (AnimFlag<?> animFlag : newIdObject.getAnimFlags()) {
//					AnimFlag<?> oldAnimFlag = idObject.find(animFlag.getName());
//					animFlag.clear();
//
//					for (Sequence recSequence : recToDonSequenceMap.keySet()) {
//						Sequence donSequence = recToDonSequenceMap.get(recSequence);
//						AnimFlagUtils.copyFrom(animFlag, oldAnimFlag, donSequence, recSequence);
//					}
//				}
				undoActions.add(new AddNodeAction(recModel, idObject, null));
				if(replParent != null){
					System.out.println("setting parent of " + idObject.getName() + " to " + replParent.getName() + "");
					undoActions.add(new SetParentAction(idObject, replParent, null));
				}
			}

			for(Material material : newMaterials){
				undoActions.add(new AddMaterialAction(material, recModel, null));
			}

			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.clearMatrices();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes, extraBones), recModelHandler.getModelView(), "", ModelStructureChangeListener.changeListener);
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::geosetsUpdated, addedModelPart, selectionAction).redo());
		}

	}
	private void doImport1(IdObject donBone, IdObject recBone) {
		if (donBone != null) {
			if(chainMap == null){
				chainMap = getChainMap(recBone, recModel, donBone, donModel, boneChainDepth, true); // donating bones to receiving bones
			}

			Set<IdObject> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));

			Map<IdObject, IdObject> parentMap = new HashMap<>();
			for (IdObject bone : selectedBones) {
				if (bone.getParent() != null && chainMap.get(bone) != null && chainMap.get(bone.getParent()) == null) {
					parentMap.put(bone.getParent(), chainMap.get(bone).getParent());
				}
			}

			int selectedBonesSize = selectedBones.size();

			Set<Geoset> newGeosets = getNewGeosets(selectedBones);

			List<IdObject> extraBones = new ArrayList<>();
			if (selectedBones.size() != selectedBonesSize && boneOption == BoneOption.importBones || boneOption == BoneOption.importBonesExtra) {
				for (IdObject bone : selectedBones) {
					if (chainMap.get(bone) == null) {
						System.out.println("adding bone copy of: " + bone.getName());
						IdObject copy = bone.copy();
						chainMap.put(bone, copy);
						extraBones.add(copy);
					}
				}
			}

			if (boneOption == BoneOption.rebindGeometry) {
				for (IdObject parent : parentMap.keySet()) {
					if (chainMap.get(parent) == null) {
						parentMap.put(parent, parentMap.get(parent));
					}
				}
			}

			boolean removeExtraBones = boneOption == BoneOption.importBones || boneOption == BoneOption.importBonesExtra;
			Set<Material> newMaterials = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				for (GeosetVertex vertex : geoset.getVertices()) {
//					vertex.replaceBones(chainMap, removeExtraBones);
					vertex.replaceBones(chainMap);
				}
				if(!recModel.contains(geoset.getMaterial())){
					newMaterials.add(geoset.getMaterial());
				}
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

			for (IdObject idObject : extraBones) {
				IdObject replParent = null;
				if(idObject.getParent() != null && !extraBones.contains(idObject.getParent())){
					replParent = chainMap.get(idObject.getParent());
					if(replParent != null){
						System.out.println("setting parent of " + idObject.getName() + " to null (" + replParent.getName() + ")");
					}
					idObject.setParent(null);

				}
//				IdObject newIdObject = boneCopyMap.get(idObject);
//				for (AnimFlag<?> animFlag : newIdObject.getAnimFlags()) {
//					AnimFlag<?> oldAnimFlag = idObject.find(animFlag.getName());
//					animFlag.clear();
//
//					for (Sequence recSequence : recToDonSequenceMap.keySet()) {
//						Sequence donSequence = recToDonSequenceMap.get(recSequence);
//						AnimFlagUtils.copyFrom(animFlag, oldAnimFlag, donSequence, recSequence);
//					}
//				}
				undoActions.add(new AddNodeAction(recModel, idObject, null));
				if(replParent != null){
					System.out.println("setting parent of " + idObject.getName() + " to " + replParent.getName() + "");
					undoActions.add(new SetParentAction(idObject, replParent, null));
				}
			}

			for(Material material : newMaterials){
				undoActions.add(new AddMaterialAction(material, recModel, null));
			}

			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.clearMatrices();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes, extraBones), recModelHandler.getModelView(), "", ModelStructureChangeListener.changeListener);
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::geosetsUpdated, addedModelPart, selectionAction).redo());
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
}
