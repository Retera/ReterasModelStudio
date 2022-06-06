package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.ReplaceSequenceTransformations;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Pair;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class ImportBoneChainAnimationPanel extends TwiImportPanel {

	Map<IdObject, IdObject> chainMap;

	public ImportBoneChainAnimationPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(donModel, recModelHandler);
		setLayout(new MigLayout("fill, wrap 2", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 0][grow 0][grow][grow 0]"));

		add(new JLabel("Motion source"), "");
		add(new JLabel("Destination"), "");

		add(donBoneChooserButton, "");
		add(recBoneChooserButton, "");

		add(new JLabel("Bone Chain Depth: "), "split 2, spanx");
		add(new IntEditorJSpinner(boneChainDepth, boneChainDepth, (i) -> boneChainDepth = i), "");

		add(getAnimMapPanel(), "spanx, growx, growy");

		BoneChainMapWizard boneChainMapWizard = new BoneChainMapWizard(this, donModel, recModel);
		JButton mapBonesButton = new JButton("Map Bones!");
		mapBonesButton.addActionListener(e -> mapBones(boneChainMapWizard, donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone(), boneChainDepth));
		add(mapBonesButton, "wrap");

		JButton importButton = new JButton("Import!");
		importButton.addActionListener(e -> doImport(donBoneChooserButton.getChosenBone(), recBoneChooserButton.getChosenBone(), boneChainDepth));
		add(importButton, "");
	}

	private void mapBones(BoneChainMapWizard boneChainMapWizard, Bone donBone, Bone recBone, int boneChainDepth){
		boneChainMapWizard.editMapping(donBone, recBone, boneChainDepth, false);
		chainMap = boneChainMapWizard.getChainMap();
	}

	private void doImport(Bone donBone, Bone recBone, int boneChainDepth) {
		if (donBone != null && recBone != null) {
			if(chainMap == null){
				chainMap = getChainMap(donBone, donModel, recBone, recModel, boneChainDepth, false); // receiving bones to donating bones
			}
			System.out.println("success so far!! (filled bone chain map successfully with " + chainMap.size() + " bones)");

			Map<Sequence, Sequence> recToDonSequenceMap = getRecToDonSequenceMap(); // receiving animations to donating animations

			doActualImport(recToDonSequenceMap, chainMap);
		}
	}

	private void doActualImport(Map<Sequence, Sequence> recToDonSequenceMap, Map<IdObject, IdObject> chainMap) {
		List<UndoAction> undoActions = new ArrayList<>();
		for(Sequence recSeq : recToDonSequenceMap.keySet()){
			if(!recModel.contains(recSeq)){
				undoActions.add(new AddSequenceAction(recModel, recSeq, null));
			}
		}

		System.out.println(chainMap.size());

		for (IdObject recIdObject : chainMap.keySet()) {
			IdObject donIdObject = chainMap.get(recIdObject);
			List<Pair<AnimFlag<?>, AnimFlag<?>>> flagPairs = getFlagPairs(recIdObject, donIdObject);

			for (Pair<AnimFlag<?>, AnimFlag<?>> flagPair : flagPairs) {
				AnimFlag<?> recAnimFlag = flagPair.getFirst();
				AnimFlag<?> donAnimFlag = flagPair.getSecond();

				if (recAnimFlag instanceof IntAnimFlag && donAnimFlag instanceof IntAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (IntAnimFlag) recAnimFlag, (IntAnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof FloatAnimFlag && donAnimFlag instanceof FloatAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (FloatAnimFlag) recAnimFlag, (FloatAnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof Vec3AnimFlag && donAnimFlag instanceof Vec3AnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (Vec3AnimFlag) recAnimFlag, (Vec3AnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof QuatAnimFlag && donAnimFlag instanceof QuatAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (QuatAnimFlag) recAnimFlag, (QuatAnimFlag) donAnimFlag);
				} else if (recAnimFlag == null){
					AnimFlag<?> newAnimFlag = donAnimFlag.getEmptyCopy();
					for (Sequence sequence : recToDonSequenceMap.keySet()) {
						AnimFlagUtils.copyFrom(newAnimFlag, donAnimFlag, recToDonSequenceMap.get(sequence), sequence);
					}
					undoActions.add(new AddAnimFlagAction<>(recIdObject, newAnimFlag, null));
				} else if (donAnimFlag == null){
					for (Sequence sequence : recToDonSequenceMap.keySet()) {
						undoActions.add(new RemoveFlagEntryMapAction<>(recAnimFlag, sequence, null));
					}
				}
			}
		}

		CompoundAction importSubAnimation = new CompoundAction("import sub animation", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);
		recModelHandler.getUndoManager().pushAction(importSubAnimation.redo());
		System.out.println("Done Importing! :O");
	}

	private List<Pair<AnimFlag<?>, AnimFlag<?>>> getFlagPairs(IdObject recIdObject, IdObject donIdObject){
		List<Pair<AnimFlag<?>, AnimFlag<?>>> pairList = new ArrayList<>();
		if(recIdObject != null && donIdObject != null){
			ArrayList<AnimFlag<?>> recAnimFlags = recIdObject.getAnimFlags();
			ArrayList<AnimFlag<?>> donAnimFlags = donIdObject.getAnimFlags();

			Set<AnimFlag<?>> matchedFlags = new HashSet<>();
			for (AnimFlag<?> recAnimFlag : recAnimFlags){
				AnimFlag<?> donAnimFlag = donIdObject.find(recAnimFlag.getName());
				pairList.add(new Pair<>(recAnimFlag, donAnimFlag));
				matchedFlags.add(donAnimFlag);
			}

			for (AnimFlag<?> donAnimFlag : donAnimFlags){
				if(!matchedFlags.contains(donAnimFlag)){
					pairList.add(new Pair<>(null, donAnimFlag));
				}
			}
		}
		return pairList;
	}

	private <Q> void addUndoActions(Map<Sequence, Sequence> recToDonSequenceMap, List<UndoAction> undoActions, AnimFlag<Q> recAnimFlag, AnimFlag<Q> donAnimFlag) {
		for (Sequence recSequence : recToDonSequenceMap.keySet()) {
			Sequence donSequence = recToDonSequenceMap.get(recSequence);
			undoActions.add(new ReplaceSequenceTransformations<>(donAnimFlag, recAnimFlag, donSequence, recSequence, null));
		}
	}

	private void tryAutoMatchStuff(String regexReplace, List<IdObject> recChilds, List<IdObject> donChilds) {
		Map<IdObject, ArrayList<IdObject>> tempMap = new HashMap<>();
		List<IdObject> recNotBoneChilds = recChilds.stream().filter(object -> !(object instanceof Bone)).collect(Collectors.toList());
		List<IdObject> donNotBoneChilds = donChilds.stream().filter(object -> !(object instanceof Bone)).collect(Collectors.toList());

		List<IdObject> donBoneChilds = donChilds.stream().filter(object -> object instanceof Bone).collect(Collectors.toList());
		List<IdObject> recBoneChilds = recChilds.stream().filter(object -> object instanceof Bone).collect(Collectors.toList());
		for (IdObject tempDonBone : donBoneChilds) {
			List<IdObject> kindaMatchingList = tempMap.computeIfAbsent(tempDonBone, k -> new ArrayList<>() {});
			List<IdObject> nameMatchingList = new ArrayList<>();
			List<IdObject> nameAlmostMatchingList = new ArrayList<>();
			List<IdObject> sameNumChildsList = new ArrayList<>();
			for (IdObject tempRecBone : recBoneChilds) {
				if (tempRecBone.getName().equals(tempDonBone.getName())) {
					nameMatchingList.add(tempRecBone);
				} else if (tempRecBone.getName().toLowerCase(Locale.ROOT).replaceAll(regexReplace, "").equals(tempDonBone.getName().toLowerCase(Locale.ROOT).replaceAll(regexReplace, ""))) {
					nameAlmostMatchingList.add(tempRecBone);
				} else if (tempRecBone.getChildrenNodes().size() == tempDonBone.getChildrenNodes().size()){
					sameNumChildsList.add(tempRecBone);
				}
			}
			kindaMatchingList.addAll(nameMatchingList);
			kindaMatchingList.addAll(nameAlmostMatchingList);
			kindaMatchingList.addAll(sameNumChildsList);
		}
	}


	private void addToList(IdObject object, Set<IdObject> objectSet) {
		objectSet.add(object);
		System.out.println(object.getName());
		for (IdObject child : object.getChildrenNodes()) {
			addToList(child, objectSet);
		}
	}

}
