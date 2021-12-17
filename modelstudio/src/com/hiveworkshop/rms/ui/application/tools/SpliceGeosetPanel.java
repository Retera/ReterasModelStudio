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

public class SpliceGeosetPanel extends TwiImportPanel {
	Geoset chosenDonGeo = null;
	String donGeosetButtonText = "Choose Geoset";
	GeosetChooser geosetChooser;

	public SpliceGeosetPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 1][grow 1][grow 0]"));

		add(new JLabel("Part source"), "");
		add(new JLabel("Destination"), "");
		geosetChooser = new GeosetChooser(donModel);

		add(getButton(donGeosetButtonText, this::chooseDonGeoset, donModel), "wrap");
//		add(getButton(recBoneButtonText, this::chooseRecBone, recModel), "");

		add(getBoneOptionPanel(), "spanx, wrap");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(chosenDonGeo));
		add(importButton, "");
	}


	protected void chooseDonGeoset(JButton chooseGeoset) {
		chosenDonGeo = geosetChooser.chooseBone(chosenDonGeo, this);
		if (chosenDonGeo != null) {
			chooseGeoset.setText(chosenDonGeo.getName());
//			chooseGeoset.setIcon(iconHandler.getImageIcon(chosenDonBone, donModel));
		} else {
			chooseGeoset.setText(donGeosetButtonText);
//			chooseGeoset.setIcon(null);
		}
		chooseGeoset.setIcon(iconHandler.getImageIcon(chosenDonBone, donModel));
		repaint();
	}

	private void doImport(Geoset donGeoset) {
		if (donGeoset != null) {
			Geoset newGeoset = donGeoset.deepCopy();
//			Map<IdObject, IdObject> chainMap = getChainMapReverse(donGeoset, recBone, boneChainDepth); // donating bones to receiving bones
			Map<IdObject, IdObject> chainMap = new HashMap<>();
			Map<Bone, List<GeosetVertex>> boneMap = donGeoset.getBoneMap();
			System.out.println("total bones: " + boneMap.size());
			Set<Bone> topLevelBones = getTopLevelBones(boneMap.keySet());
			System.out.println("top level bones: " + topLevelBones.size());
//			Map<IdObject, IdObject> boneChainMap = new HashMap<>();
			List<IdObject> bones = new ArrayList<>(recModel.getBones());
			fillBoneChainMap(chainMap, bones, recModel, new ArrayList<>(topLevelBones), donModel, true);
//			for (Bone bone : topLevelBones){
////				Bone recBone = recBoneChooser.chooseBone(null, this);
//				Bone recBone = getABone(bone);
//				chainMap.putAll(getChainMap(recBone, recModel, bone, donModel, boneChainDepth, true));
//			}
//			Map<IdObject, IdObject> chainMap = getChainMap(recBone, recModel, donGeoset, donModel, boneChainDepth, true); // donating bones to receiving bones

			Set<Bone> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));
//			Set<Geoset> newGeosets = getNewGeosets(selectedBones);
//			for(Geoset geoset : newGeosets){
//				for(GeosetVertex vertex : geoset.getVertices()){
//					vertex.replaceBones(chainMap);
//				}
//			}
			for (GeosetVertex vertex : newGeoset.getVertices()) {
				vertex.replaceBones(chainMap);
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

//			for (Geoset geoset : newGeosets) {
//				geoset.getMatrices().clear();
//				addedVertexes.addAll(geoset.getVertices());
//				undoActions.add(new AddGeosetAction(geoset, recModel, null));
//			}
			newGeoset.getMatrices().clear();
			Set<GeosetVertex> addedVertexes = new HashSet<>(newGeoset.getVertices());
			AddGeosetAction addGeosetAction = new AddGeosetAction(newGeoset, recModel, ModelStructureChangeListener.changeListener);
			undoActions.add(addGeosetAction);

			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes), recModelHandler.getModelView(), "", null);
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::nodesUpdated, addedModelPart, selectionAction).redo());
		}

	}


	protected void fillBoneChainMap(Map<IdObject, IdObject> boneChainMap,
	                                List<IdObject> mapToChilds, EditableModel mapToModel,
	                                List<IdObject> mapFromChilds, EditableModel mapFromModel,
	                                boolean presentParent) {
//		List<IdObject> mapFromChilds = mapFromBone.getChildrenNodes();
//		List<IdObject> mapToChilds = mapToBone.getChildrenNodes();
		if (mapFromChilds.size() == 1 && mapToChilds.size() == 1) {
			if (isBones(mapFromChilds.get(0), mapToChilds.get(0))) {
				boneChainMap.put(mapFromChilds.get(0), mapToChilds.get(0));
				fillBoneChainMap(boneChainMap, mapToChilds.get(0).getChildrenNodes(), mapToModel, mapFromChilds.get(0).getChildrenNodes(), mapFromModel, presentParent);
			} else if (mapFromChilds.get(0).getClass() == mapToChilds.get(0).getClass()) {
				boneChainMap.put(mapFromChilds.get(0), mapToChilds.get(0));
			}
		} else if (!(mapFromChilds.isEmpty() || mapToChilds.isEmpty())) {
			Map<IdObject, IdObject> boneChainSubMap = getBoneChainSubMap(mapToChilds, mapToModel, mapFromChilds, mapFromModel, presentParent);
			boneChainMap.putAll(boneChainSubMap);
			for (IdObject idObject : boneChainSubMap.keySet()) {
				if (boneChainSubMap.get(idObject) != null && isBones(idObject, boneChainSubMap.get(idObject))) {
					fillBoneChainMap(boneChainMap, boneChainSubMap.get(idObject).getChildrenNodes(), mapToModel, idObject.getChildrenNodes(), mapFromModel, presentParent);
				}
			}
		}

	}

	private Bone getABone(Bone bone) {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel(iconHandler.getImageIcon(bone, donModel)));
		panel.add(new JLabel(bone.getName()));
		if (bone.getParent() != null) {
			panel.add(new JLabel(" (" + bone.getParent().getName() + ")"));
		}
		panel.add(getButton(recBoneButtonText, b -> chooseRecBone1(b), recModel));

		JOptionPane.showMessageDialog(this, panel, "Choose Bone", JOptionPane.PLAIN_MESSAGE);
		return chosenRecBone;
	}


	protected Bone chooseRecBone1(JButton chooseBone) {
		chosenRecBone = recBoneChooser.chooseBone(null, this);
		if (chosenRecBone != null) {
			chooseBone.setText(chosenRecBone.getName());
//			chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, recModel));
		} else {
			chooseBone.setText(recBoneButtonText);
//			chooseBone.setIcon(null);
		}
		chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, recModel));
		repaint();
		return chosenRecBone;
	}

	private Set<Bone> getTopLevelBones(Set<Bone> bones) {
		Set<Bone> topLevelBones = new HashSet<>();
		System.out.println("looking through " + bones.size() + " bones");
		for (Bone bone : bones) {
//			if(bone.getParent() instanceof )
			topLevelBones.add(getLastParentInSet(bone, bone, bones));
		}
		return topLevelBones;
	}

	private Bone getLastParentInSet(Bone bone, Bone lastFoundParent, Set<Bone> bones) {
		if (bones.contains(bone)) {
			lastFoundParent = bone;
		}
		if (bone.getParent() instanceof Helper) {
			return getLastParentInSet((Bone) bone.getParent(), lastFoundParent, bones);
		}
		if (bone.getParent() instanceof Bone && bones.contains((Bone) bone.getParent())) {
			return getLastParentInSet((Bone) bone.getParent(), lastFoundParent, bones);
		}
		return lastFoundParent;
	}

//	private Map<IdObject, IdObject> getBoneCopyMap(IdObject donBone){
//		Set<IdObject> chosenDonObjects = new HashSet<>();
//		collectChildren(donBone, chosenDonObjects);
//		Map<IdObject, IdObject> boneCopyMap = new HashMap<>();
//		for (IdObject idObject : chosenDonObjects){
//			boneCopyMap.put(idObject, idObject.copy());
//		}
//		for (IdObject newIdObject : boneCopyMap.values()){
//			if(newIdObject.getParent() != null){
//				newIdObject.setParent(boneCopyMap.get(newIdObject.getParent()));
//			}
//		}
//
//		return boneCopyMap;
//	}
//
//	private void collectChildren(IdObject object, Set<IdObject> objectSet) {
//		objectSet.add(object);
//		System.out.println(object.getName());
//		for (IdObject child : object.getChildrenNodes()) {
//			collectChildren(child, objectSet);
//		}
//	}
}
