package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SimplifyKeyframesAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class BakeAndRebindAction implements UndoAction {
	ModelHandler modelHandler;
	EditableModel model;
	Map<Sequence, TreeMap<Integer, Vec3>> seqNewTransKF = new HashMap<>();
	Map<Sequence, TreeMap<Integer, Vec3>> seqNewScaleKF = new HashMap<>();
	Map<Sequence, TreeMap<Integer, Quat>> seqNewRotKF = new HashMap<>();
	ArrayList<Integer> times = new ArrayList<>();
//	TreeSet<Integer> allKF = new TreeSet<>();
//	TreeMap<Integer, IdObject> parentChain1 = new TreeMap<>();
//	TreeMap<Integer, IdObject> parentChain2 = new TreeMap<>();
//
//	Set<Entry<Vec3>> orgTransEntries = new HashSet<>();
//	Set<Entry<Vec3>> orgScaleEntries = new HashSet<>();
//	Set<Entry<Quat>> orgRotEntries = new HashSet<>();

	List<UndoAction> keyframeActions = new ArrayList<>();

	CompoundAction compoundAction;

	IdObject objToRebind;
	//	IdObject oldParent;
	IdObject newParent;

	public BakeAndRebindAction(IdObject objToRebind, IdObject newParent, ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.model = modelHandler.getModel();
		this.objToRebind = objToRebind;
//		this.oldParent = objToRebind.getParent();
		this.newParent = newParent;

//		IdObject commonParent = findCommonParent(objToRebind, newParent);
		RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView());
		TimeEnvironmentImpl timeEnvironment = renderModel.getTimeEnvironment();
		timeEnvironment.setLive(true);
		if (newParent != objToRebind.getParent()) {
			if (objToRebind.getParent() != null) {
				getParentKfTimes(objToRebind.getParent(), newParent);
			}

			//		calcWorldThings(renderModel, timeEnvironment, modelHandler, objToRebind.getParent());
			calculateCompFlags(renderModel, timeEnvironment);
		}
		createSetNewEntriesAction(objToRebind);

	}

//	private void calcWorldThings(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, ModelHandler modelHandler, IdObject idObject){
//		renderModel.refreshFromEditor(null);
//		for(Animation animation : modelHandler.getModel().getAnims()){
//			timeEnvironment.setAnimation(animation);
////			for (Integer i = allKF.ceiling(animation.getStart()); i != null && allKF.floor(animation.getEnd()) != null && i <= allKF.floor(animation.getEnd()); i = allKF.higher(i)){
//			for (Integer i = allKF.ceiling(0); i != null && allKF.floor(animation.getLength()) != null && i <= allKF.floor(animation.getLength()); i = allKF.higher(i)){
//				timeEnvironment.setAnimationTime(i);
//				renderModel.updateNodes(true, false, false);
//				System.out.println("time: " + i + "; Local loc: " + renderModel.getRenderNode(idObject).getLocalLocation() + ", World loc: " + renderModel.getRenderNode(idObject).getWorldLocation());
////				newTransKF.put(i, renderModel.getRenderNode(idObject).getLocalLocation());
////				newScaleKF.put(i, renderModel.getRenderNode(idObject).getLocalScale());
////				newRotKF.put(i, renderModel.getRenderNode(idObject).getLocalRotation());
//				newTransKF.put(i, new Vec3(renderModel.getRenderNode(idObject).getWorldLocation()));
//				newScaleKF.put(i,  new Vec3(renderModel.getRenderNode(idObject).getWorldScale()));
//				newRotKF.put(i, new Quat(renderModel.getRenderNode(idObject).getWorldRotation()));
//			}
//		}
//	}

//	private IdObject findCommonParent(IdObject objToRebind, IdObject newParent){
//		getParentChain(objToRebind, parentChain1);
//		getParentChain(newParent, parentChain2);
//		for (Integer i : parentChain1.keySet()){
//			for (Integer j : parentChain2.keySet()){
//				if (parentChain1.get(i) == parentChain2.get(j)){
//					return parentChain1.get(i);
//				}
//			}
//		}
//		return null;
//	}

	private void getParentKfTimes(IdObject obj, IdObject lastParent) {
		TreeSet<Integer> timeSet = new TreeSet<>();
		for (AnimFlag<?> animFlag : obj.getAnimFlags()) {
			getAllEntries(animFlag, timeSet);
		}
		if (obj.getParent() != lastParent) {
			getParentKfTimes(obj.getParent(), lastParent);
		}
		times.addAll(timeSet);
	}

	private void getAllEntries(AnimFlag<?> flag, TreeSet<Integer> timeSet) {
		if (flag != null) {
			for (Animation animation : modelHandler.getModel().getAnims()) {
				TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(animation);
				if (entryMap != null) {
					timeSet.addAll(entryMap.keySet());
				}
			}
		}
	}

	private void getParentChain(IdObject object, TreeMap<Integer, IdObject> mapToFill) {
		if (object != null) {
			mapToFill.put(mapToFill.size(), object);
			if (object.getParent() != null) {
				getParentChain(object.getParent(), mapToFill);
			}
		}
	}

	private void calculateCompFlags(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment) {
		renderModel.refreshFromEditor();

		for (Animation animation : model.getAnims()) {
			timeEnvironment.setSequence(animation);
			TreeMap<Integer, Vec3> newTransKF = seqNewTransKF.computeIfAbsent(animation, k -> new TreeMap<>());
			TreeMap<Integer, Vec3> newScaleKF = seqNewScaleKF.computeIfAbsent(animation, k -> new TreeMap<>());
			TreeMap<Integer, Quat> newRotKF = seqNewRotKF.computeIfAbsent(animation, k -> new TreeMap<>());

			Vec3 lastNewTrans = null;
			Vec3 lastNewScale = null;
			Quat lastNewRot = null;
//			for (int time = 0; time < animation.getLength(); time++) {
			for (int time : times) {
				timeEnvironment.setAnimationTime(time);
				renderModel.updateNodes(true, false, false);

				RenderNode2 renderNode = renderModel.getRenderNode(objToRebind);
				Vec3 newTrans = new Vec3(renderNode.getPivot()).sub(objToRebind.getPivotPoint());
				newTransKF.put(time, newTrans);
//				if(lastNewTrans != null && newTrans.equalLocs(lastNewTrans)){
//					newTransKF.remove(time);
//				}
//				lastNewTrans = newTrans;

				Vec3 newScale = new Vec3(renderModel.getRenderNode(objToRebind).getWorldScale());
////				newScale.transformInverted(renderModel.getRenderNode(newParent).getWorldMatrix());
				newScaleKF.put(time, newScale);
//				if(newScale.equals(lastNewScale)){
//					newTransKF.remove(time);
//				}
//				lastNewScale = newScale;
//
				Quat newRot = new Quat(renderModel.getRenderNode(objToRebind).getWorldRotation()).normalize();

//				Quat newRot = new Quat(0,0,0,1);
//				newRot.mul(renderModel.getRenderNode(objToRebind).getWorldRotation()).normalize();
				newRotKF.put(time, newRot);
//				if(lastNewRot != null && newRot.equals(lastNewRot)){
//					newTransKF.remove(time);
//				}
//				lastNewRot = newRot;
			}
		}
	}

	@Override
	public UndoAction undo() {
		compoundAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		compoundAction.redo();
		return this;
	}


	@Override
	public String actionName() {
		return "Baked and changed Parent";
	}

	private void createSetNewEntriesAction(IdObject objToRebind) {
//		IdObject obj = objToRebind.getParent();
		IdObject parent = objToRebind.getParent();
		if (parent != null) {
//			Bone obj = (Bone) parent.copy();
//
//			obj.setParent(null);
//
//			UndoAction addBoneAction = new DrawBoneAction(modelHandler.getModelView(), null, obj);
//			keyframeActions.add(addBoneAction);

			List<Sequence> allSequences = model.getAllSequences();

//		Bone bone = new Bone();
			List<AnimFlag<?>> replacementFlags = new ArrayList<>();

			if (!seqNewTransKF.isEmpty()) {
				System.out.println("fixing translation");
//				AnimFlag<Vec3> translationOrg = obj.getTranslationFlag();
				AnimFlag<Vec3> translationOrg = objToRebind.getTranslationFlag();

				AnimFlag<Vec3> translation;
				translation = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
				translation.setInterpType(InterpolationType.LINEAR);
//				if (translationOrg == null) {
//					translation = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
//					translation.setInterpType(InterpolationType.LINEAR);
//				} else {
//					translation = translationOrg.getEmptyCopy();
//				}

				AnimFlag<Vec3> filledFlag = getFilledFlag(allSequences, translation, seqNewTransKF);
				if (filledFlag.size() > 0) {
					if (translationOrg != null) {
						translation.setInterpType(translationOrg.getInterpolationType());
					}
					replacementFlags.add(filledFlag);
				}
			}

			if (!seqNewScaleKF.isEmpty()) {
				System.out.println("fixing scaling");
//				AnimFlag<Vec3> scalingOrg = obj.getScalingFlag();
				AnimFlag<Vec3> scalingOrg = objToRebind.getScalingFlag();

				AnimFlag<Vec3> scaling;
				scaling = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING);
				scaling.setInterpType(InterpolationType.LINEAR);
//				if (scalingOrg == null) {
//					scaling = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING);
//					scaling.setInterpType(InterpolationType.LINEAR);
//				} else {
//					scaling = scalingOrg.getEmptyCopy();
//				}


				AnimFlag<Vec3> filledFlag = getFilledFlag(allSequences, scaling, seqNewScaleKF);
				if (filledFlag.size() > 0) {
//					if (scalingOrg != null){
//						scaling.setInterpType(scalingOrg.getInterpolationType());
//					}
					replacementFlags.add(filledFlag);
				}
			}

			if (!seqNewRotKF.isEmpty()) {
				System.out.println("fixing rotation");
//				AnimFlag<Quat> rotationOrg = obj.getRotationFlag();
				AnimFlag<Quat> rotationOrg = objToRebind.getRotationFlag();

				AnimFlag<Quat> rotation;
				rotation = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
				rotation.setInterpType(InterpolationType.LINEAR);
//				if (rotationOrg == null) {
//					rotation = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
//					rotation.setInterpType(InterpolationType.LINEAR);
//				} else {
//					rotation = rotationOrg.getEmptyCopy();
//				}

				AnimFlag<Quat> filledFlag = getFilledFlag(allSequences, rotation, seqNewRotKF);
				if (filledFlag.size() > 0) {
//					if (rotationOrg != null){
//						rotation.setInterpType(rotationOrg.getInterpolationType());
//					}
					replacementFlags.add(filledFlag);
				}
			}
			if (!replacementFlags.isEmpty()) {
//				UndoAction action = new ReplaceAnimFlagsAction(obj, replacementFlags, null);
				UndoAction action = new ReplaceAnimFlagsAction(objToRebind, replacementFlags, null);
				keyframeActions.add(action);
			}
//			UndoAction newParentAction = new ParentChangeAction(objToRebind, obj, null);
			UndoAction newParentAction = new ParentChangeAction(objToRebind, null, null);
			keyframeActions.add(newParentAction);

		}
		compoundAction = new CompoundAction("Baked and changed Parent", keyframeActions);
	}

	private <Q> AnimFlag<Q> getFilledFlag(List<Sequence> sequences, AnimFlag<Q> animFlag, Map<Sequence, TreeMap<Integer, Q>> seqNewKF) {
		for (Sequence sequence : sequences) {
			TreeMap<Integer, Q> newKFMap = seqNewKF.get(sequence);
			if (newKFMap != null) {
				for (int i : newKFMap.keySet()) {
//					System.out.println("new entry at " + i + ": " + newKFMap.get(i));
					animFlag.addEntry(i, newKFMap.get(i), sequence);
				}
			}
		}
		SimplifyKeyframesAction action = new SimplifyKeyframesAction(Collections.singleton(animFlag), sequences, 0.00001f);
		System.out.println("removed " + action.getNumberOfEntriesToRemove() + " keyframes");
		action.redo();
		return animFlag;
	}
}
