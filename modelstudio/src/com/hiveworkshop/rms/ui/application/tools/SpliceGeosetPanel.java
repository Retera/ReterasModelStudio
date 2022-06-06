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
import java.util.function.Consumer;

public class SpliceGeosetPanel extends TwiImportPanel {
	Geoset chosenDonGeo = null;
	List<Geoset> chosenDonGeos = new ArrayList<>();
	String donGeosetButtonText = "Choose Geoset";
	GeosetChooser geosetChooser;

	public SpliceGeosetPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 1][grow 1][grow 0]"));

		add(new JLabel("Part source"), "");
//		add(new JLabel("Destination"), "");
		add(new JLabel(""), "");
		geosetChooser = new GeosetChooser(donModel);

//		add(getButton(donGeosetButtonText, this::chooseDonGeoset, donModel), "wrap");
		add(getButton(donGeosetButtonText, this::chooseDonGeosets, donModel), "wrap");
//		add(getButton(recBoneButtonText, this::chooseRecBone, recModel), "");

//		add(getBoneOptionPanel(), "spanx, wrap");
		add(new JPanel(), "spanx, wrap");

		JButton importButton = new JButton("Import!");
//		importButton.addActionListener(e -> doImport(chosenDonGeo));
		importButton.addActionListener(e -> doImport(chosenDonGeos));
		add(importButton, "");
	}


	protected JButton getButton(String text, Consumer<JButton> buttonConsumer, EditableModel model) {
		JButton button = new JButton(text);
		if(model != null){
//			button.setIcon(iconHandler.getImageIcon(null, model));
			button.setIcon(iconHandler.getImageIcon(model));
		}
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}

	protected void chooseDonGeosets(JButton chooseGeoset) {
		chosenDonGeos = geosetChooser.chooseGeosets(chosenDonGeos, this);
		if (!chosenDonGeos.isEmpty() && chosenDonGeos.get(0) != null) {
			if(chosenDonGeos.size() == 1){
				chooseGeoset.setText(chosenDonGeos.get(0).getName());
			} else {
				chooseGeoset.setText(chosenDonGeos.size() + " Geosets");
			}
		} else {
			chooseGeoset.setText(donGeosetButtonText);
		}
		chooseGeoset.setIcon(iconHandler.getImageIcon(new HashSet<>(chosenDonGeos), donModel));
		repaint();
	}

//	protected void chooseDonGeoset(JButton chooseGeoset) {
//		chosenDonGeo = geosetChooser.chooseGeoset(chosenDonGeo, this); // ToDo Allow multiple geosets?
//		if (chosenDonGeo != null) {
//			chooseGeoset.setText(chosenDonGeo.getName());
//		} else {
//			chooseGeoset.setText(donGeosetButtonText);
//		}
//		chooseGeoset.setIcon(iconHandler.getImageIcon(chosenDonGeo, donModel));
//		repaint();
//	}

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

			for (GeosetVertex vertex : newGeoset.getVertices()) {
				vertex.replaceBones(chainMap);
			}

			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();

			newGeoset.getMatrices().clear();
			Set<GeosetVertex> addedVertexes = new HashSet<>(newGeoset.getVertices());
			AddGeosetAction addGeosetAction = new AddGeosetAction(newGeoset, recModel, ModelStructureChangeListener.changeListener);
			undoActions.add(addGeosetAction);

			CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

			SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes), recModelHandler.getModelView(), "", null);
			recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::nodesUpdated, addedModelPart, selectionAction).redo());
		}

	}

	private void doImport(List<Geoset> donGeosets) {
		if (!donGeosets.isEmpty()) {
			Set<Bone> geosetsBoneSets = new HashSet<>();
			List<Geoset> newGeosets = new ArrayList<>();
			for(Geoset geoset : donGeosets){
				geosetsBoneSets.addAll(geoset.getBoneMap().keySet());
				newGeosets.add(geoset.deepCopy());
			}

			System.out.println("total bones: " + geosetsBoneSets.size());
			Set<Bone> topLevelBones = getTopLevelBones(geosetsBoneSets);
			System.out.println("top level bones: " + topLevelBones.size());

			List<IdObject> bones = new ArrayList<>(recModel.getBones());

			Map<IdObject, IdObject> chainMap = new HashMap<>();
			fillBoneChainMap(chainMap, bones, recModel, new ArrayList<>(topLevelBones), donModel, true);
//			for (Bone bone : topLevelBones){
////				Bone recBone = recBoneChooser.chooseBone(null, this);
//				Bone recBone = getABone(bone);
//				chainMap.putAll(getChainMap(recBone, recModel, bone, donModel, boneChainDepth, true));
//			}
//			Map<IdObject, IdObject> chainMap = getChainMap(recBone, recModel, donGeoset, donModel, boneChainDepth, true); // donating bones to receiving bones

			Set<Bone> selectedBones = new HashSet<>();
			chainMap.keySet().stream().filter(idObject -> idObject instanceof Bone).forEach(idObject -> selectedBones.add((Bone) idObject));


			//todo imported stuff needs to get new Animations applied!
			List<UndoAction> undoActions = new ArrayList<>();
			Set<GeosetVertex> addedVertexes = new HashSet<>();

			for(Geoset newGeoset : newGeosets){
				for (GeosetVertex vertex : newGeoset.getVertices()) {
					vertex.replaceBones(chainMap);
				}
				newGeoset.getMatrices().clear();
				addedVertexes.addAll(newGeoset.getVertices());
				AddGeosetAction addGeosetAction = new AddGeosetAction(newGeoset, recModel, ModelStructureChangeListener.changeListener);
				undoActions.add(addGeosetAction);
			}

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
			IdObject mapFromChild = mapFromChilds.get(0);
			IdObject mapToChild = mapToChilds.get(0);
			if (isBones(mapFromChild, mapToChild)) {
				boneChainMap.put(mapFromChild, mapToChild);
				fillBoneChainMap(boneChainMap, mapToChild.getChildrenNodes(), mapToModel, mapFromChild.getChildrenNodes(), mapFromModel, presentParent);
			} else if (mapFromChild.getClass() == mapToChild.getClass()) {
				boneChainMap.put(mapFromChild, mapToChild);
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

//	private Bone getABone(Bone bone) {
//		JPanel panel = new JPanel(new MigLayout());
//		panel.add(new JLabel(iconHandler.getImageIcon(bone, donModel)));
//		panel.add(new JLabel(bone.getName()));
//		if (bone.getParent() != null) {
//			panel.add(new JLabel(" (" + bone.getParent().getName() + ")"));
//		}
//		panel.add(getButton(recBoneButtonText, b -> chooseRecBone1(b), recModel));
//
//		JOptionPane.showMessageDialog(this, panel, "Choose Bone", JOptionPane.PLAIN_MESSAGE);
//		return chosenRecBone;
//	}
//
//
//	protected Bone chooseRecBone1(JButton chooseBone) {
//		chosenRecBone = recBoneChooser.chooseBone(null, this);
//		if (chosenRecBone != null) {
//			chooseBone.setText(chosenRecBone.getName());
//		} else {
//			chooseBone.setText(recBoneButtonText);
//		}
//		chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, recModel));
//		repaint();
//		return chosenRecBone;
//	}

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

}
