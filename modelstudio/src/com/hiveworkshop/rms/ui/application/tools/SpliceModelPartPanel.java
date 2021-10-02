package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
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

	public SpliceModelPartPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 1][grow 1][grow 0]"));

		add(new JLabel("Part source"), "");
		add(new JLabel("Destination"), "");

		add(getButton(donBoneButtonText, this::chooseDonBone, donModel), "");
		add(getButton(recBoneButtonText, this::chooseRecBone, recModel), "");

		add(getBoneOptionPanel(), "spanx, wrap");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(chosenDonBone, chosenRecBone));
		add(importButton, "");
	}

	private void doImport(Bone donBone, Bone recBone) {
		if (donBone != null) {
//			Map<IdObject, IdObject> chainMap = getChainMapReverse(donBone, recBone, boneChainDepth); // donating bones to receiving bones
			Map<IdObject, IdObject> chainMap = getChainMap(recBone, recModel, donBone, donModel, boneChainDepth, true); // donating bones to receiving bones

			Set<Bone> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));
			Set<Geoset> newGeosets = getNewGeosets(selectedBones);
			for(Geoset geoset : newGeosets){
				for(GeosetVertex vertex : geoset.getVertices()){
					vertex.replaceBones(chainMap);
				}
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

			Set<GeosetVertex> addedVertexes = new HashSet<>();
			for (Geoset geoset : newGeosets) {
				geoset.getMatrices().clear();
				addedVertexes.addAll(geoset.getVertices());
				undoActions.add(new AddGeosetAction(geoset, recModel, null));
			}
			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes), recModelHandler.getModelView(), "");
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
