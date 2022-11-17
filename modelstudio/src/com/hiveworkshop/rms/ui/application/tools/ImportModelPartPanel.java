package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
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
		add(donBoneChooserButton, "");
		recBoneChooserButton.setText("Choose Parent");
		add(recBoneChooserButton, "");

		add(getBoneOptionPanel(), "spanx");
		add(getAnimMapPanel(), "spanx, growx, growy");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone()));
		add(importButton, "");
	}

	private void doImport(Bone donBone, Bone recBone) {
		if (donBone != null) {
			Map<IdObject, IdObject> boneCopyMap = getBoneCopyMap(donBone); //obj to copy
			boneCopyMap.get(donBone).setParent(recBone);

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

			for (IdObject idObject : boneCopyMap.keySet()) {
				IdObject newIdObject = boneCopyMap.get(idObject);
				for (AnimFlag<?> animFlag : newIdObject.getAnimFlags()) {
					AnimFlag<?> oldAnimFlag = idObject.find(animFlag.getName());
					animFlag.clear();

					for (Sequence recSequence : recToDonSequenceMap.keySet()) {
						Sequence donSequence = recToDonSequenceMap.get(recSequence);
						AnimFlagUtils.copyFrom(animFlag, oldAnimFlag, donSequence, recSequence);
					}
				}
				undoActions.add(new AddNodeAction(recModel, newIdObject, null));
			}
			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.clearMatrices();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(boneCopyMap.values(), addedVertexes), recModelHandler.getModelView(), "", null);
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
		System.out.println("collectChildren of: " + object.getName());
		for (IdObject child : object.getChildrenNodes()) {
			collectChildren(child, objectSet);
		}
	}

}
