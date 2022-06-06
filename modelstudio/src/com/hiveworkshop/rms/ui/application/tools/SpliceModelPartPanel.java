package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
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

		BoneChainMapWizard boneChainMapWizard = new BoneChainMapWizard(this, donModel, recModel);
		JButton mapBonesButton = new JButton("Map Bones!");
		mapBonesButton.addActionListener(e -> mapBones(boneChainMapWizard, donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone(), boneChainDepth));
		add(mapBonesButton, "wrap");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone()));
		add(importButton, "");
	}


	private void mapBones(BoneChainMapWizard boneChainMapWizard, Bone donBone, Bone recBone, int boneChainDepth){
		boneChainMapWizard.editMapping(donBone, recBone, boneChainDepth, true);
		chainMap = boneChainMapWizard.getChainMap();
	}

	private void doImport(Bone donBone, Bone recBone) {
		if (donBone != null) {
			if(chainMap == null){
				chainMap = getChainMap(recBone, recModel, donBone, donModel, boneChainDepth, true); // donating bones to receiving bones
			}

			Set<Bone> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));
			int selectedBonesSize = selectedBones.size();

			Set<Geoset> newGeosets = getNewGeosets(selectedBones);

			List<Bone> extraBones = new ArrayList<>();
			if (selectedBones.size() != selectedBonesSize) {
				for (Bone bone : selectedBones) {
					if (chainMap.get(bone) == null) {
						Bone copy = bone.copy();
						chainMap.put(bone, copy);
						extraBones.add(copy);
					}
				}
			}

			boolean removeExtraBones = boneOption == BoneOption.importBones || boneOption == BoneOption.importBonesExtra;
			for (Geoset geoset : newGeosets) {
				for (GeosetVertex vertex : geoset.getVertices()) {
//					vertex.replaceBones(chainMap, removeExtraBones);
					vertex.replaceBones(chainMap);
				}
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

			for (IdObject idObject : extraBones) {
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
			}


			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.getMatrices().clear();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes), recModelHandler.getModelView(), "", null);
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
}
