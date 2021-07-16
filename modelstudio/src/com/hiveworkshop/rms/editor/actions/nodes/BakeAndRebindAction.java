package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawBoneAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ReplaceAnimFlagsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class BakeAndRebindAction implements UndoAction {
	ModelHandler modelHandler;
	TreeSet<Integer> transKF = new TreeSet<>();
	TreeSet<Integer> scaleKF = new TreeSet<>();
	TreeSet<Integer> rotKF = new TreeSet<>();
	TreeMap<Integer, Vec3> newTransKF = new TreeMap<>();
	TreeMap<Integer, Vec3> newScaleKF = new TreeMap<>();
	TreeMap<Integer, Quat> newRotKF = new TreeMap<>();
	TreeSet<Integer> allKF = new TreeSet<>();
	TreeMap<Integer, IdObject> parentChain1 = new TreeMap<>();
	TreeMap<Integer, IdObject> parentChain2 = new TreeMap<>();

	Set<Entry<Vec3>> orgTransEntries = new HashSet<>();
	Set<Entry<Vec3>> orgScaleEntries = new HashSet<>();
	Set<Entry<Quat>> orgRotEntries = new HashSet<>();

	List<UndoAction> keyframeActions = new ArrayList<>();

	CompoundAction compoundAction;

	public BakeAndRebindAction(IdObject objToRebind, IdObject newParent, ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		IdObject commonParent = findCommonParent(objToRebind.getParent(), newParent);
		TimeEnvironmentImpl timeEnvironment = new TimeEnvironmentImpl(0, 1);
		RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView(), timeEnvironment);
		getParentKfTimes(objToRebind.getParent(), newParent);

		allKF.addAll(transKF);
		allKF.addAll(scaleKF);
		allKF.addAll(rotKF);

		calculateCompFlags();

//		calcWorldThings(renderModel, timeEnvironment, modelHandler, objToRebind.getParent());
		createSetNewEntriesAction(objToRebind);

	}


	private void calcWorldThings(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, ModelHandler modelHandler, IdObject idObject) {
		renderModel.refreshFromEditor(null);
		for (Animation animation : modelHandler.getModel().getAnims()) {
			timeEnvironment.setAnimation(animation);
			for (Integer i = allKF.ceiling(animation.getStart()); i != null && allKF.floor(animation.getEnd()) != null && i <= allKF.floor(animation.getEnd()); i = allKF.higher(i)) {
				timeEnvironment.setAnimationTime(i);
				renderModel.updateNodes(true, false, false);
				System.out.println("time: " + i + "; Local loc: " + renderModel.getRenderNode(idObject).getLocalLocation() + ", World loc: " + renderModel.getRenderNode(idObject).getWorldLocation());
//				newTransKF.put(i, renderModel.getRenderNode(idObject).getLocalLocation());
//				newScaleKF.put(i, renderModel.getRenderNode(idObject).getLocalScale());
//				newRotKF.put(i, renderModel.getRenderNode(idObject).getLocalRotation());
				newTransKF.put(i, new Vec3(renderModel.getRenderNode(idObject).getWorldLocation()));
				newScaleKF.put(i, new Vec3(renderModel.getRenderNode(idObject).getWorldScale()));
				newRotKF.put(i, new Quat(renderModel.getRenderNode(idObject).getWorldRotation()));
			}
		}
	}

	private IdObject findCommonParent(IdObject objToRebind, IdObject newParent) {
		getParentChain(objToRebind, parentChain1);
		getParentChain(newParent, parentChain2);
		for (Integer i : parentChain1.keySet()) {
			for (Integer j : parentChain2.keySet()) {
				if (parentChain1.get(i) == parentChain2.get(j)) {
					return parentChain1.get(i);
				}
			}
		}
		return null;
	}

	//	private void calculateCompFlags(IdObject objToRebind, IdObject commonParent){
	private void calculateCompFlags() {
		TimeEnvironmentImpl timeEnvironment = new TimeEnvironmentImpl();
		for (Integer i : parentChain1.descendingMap().keySet()) {
			IdObject idObject = parentChain1.get(i);
			for (Animation animation : modelHandler.getModel().getAnims()) {
				timeEnvironment.setAnimation(animation);
				SortedSet<Integer> animTimes = allKF.subSet(animation.getStart(), animation.getEnd());
				for (Integer t : animTimes) {
					timeEnvironment.setAnimationTime(t);
					AnimFlag<?> translation = idObject.find(MdlUtils.TOKEN_TRANSLATION);
					if (translation != null) {
						Vec3 vec3 = newTransKF.computeIfAbsent(t, v -> new Vec3(0, 0, 0));
						vec3.add(((Vec3AnimFlag) translation).interpolateAt(timeEnvironment));
//						Vec3 value = ((Vec3AnimFlag) translation).interpolateAt(timeEnvironment);
//						newTransKF.put(t, value);
					}
					AnimFlag<?> scaling = idObject.find(MdlUtils.TOKEN_SCALING);
					if (scaling != null) {
						Vec3 vec3 = newScaleKF.computeIfAbsent(t, v -> new Vec3(1, 1, 1));
						vec3.multiply(((Vec3AnimFlag) scaling).interpolateAt(timeEnvironment));
//						Vec3 value = ((Vec3AnimFlag) scaling).interpolateAt(timeEnvironment);
//						newScaleKF.put(t, value);
					}
					AnimFlag<?> rotation = idObject.find(MdlUtils.TOKEN_ROTATION);
					if (rotation != null) {
						Quat quat = newRotKF.computeIfAbsent(t, v -> new Quat(0, 0, 0, 1));
						quat.mul(((QuatAnimFlag) rotation).interpolateAt(timeEnvironment));
//						Quat value = ((QuatAnimFlag) rotation).interpolateAt(timeEnvironment);
//						newRotKF.put(t, value);
					}
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

	private void saveOrg(IdObject obj) {
		AnimFlag<?> translation = obj.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			orgTransEntries.addAll(((Vec3AnimFlag) translation).getEntryMap().values());
		}
		AnimFlag<?> scaling = obj.find(MdlUtils.TOKEN_SCALING);
		if (scaling != null) {
			orgScaleEntries.addAll(((Vec3AnimFlag) scaling).getEntryMap().values());
		}
		AnimFlag<?> rotation = obj.find(MdlUtils.TOKEN_ROTATION);
		if (rotation != null) {
			orgRotEntries.addAll(((QuatAnimFlag) rotation).getEntryMap().values());
		}
	}

	private void getParentKfTimes(IdObject obj, IdObject lastParent) {
		AnimFlag<?> translation = obj.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			transKF.addAll(translation.getEntryMap().keySet());
		}
		AnimFlag<?> scaling = obj.find(MdlUtils.TOKEN_SCALING);
		if (scaling != null) {
			scaleKF.addAll(scaling.getEntryMap().keySet());
		}
		AnimFlag<?> rotation = obj.find(MdlUtils.TOKEN_ROTATION);
		if (rotation != null) {
			rotKF.addAll(rotation.getEntryMap().keySet());
		}
		if (obj.getParent() != lastParent) {
			getParentKfTimes(obj.getParent(), lastParent);
		}
	}

	private void calculateAntiCompFlags(IdObject newParent, IdObject commonParent) {

	}

//	private void ugg(){
//		for(IdObject idObject : parentChain1.values()){
//			Vec3 localLocation = new Vec3(0, 0, 0);
//			Quat localRotation = new Quat(0, 0, 0, 1);
//			Vec3 localScale = new Vec3(1, 1, 1);
//
//			// Translation
//			Vec3AnimFlag timelineTrans = (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_TRANSLATION);
//
//			if (timelineTrans != null) {
//				Vec3 renderTranslation = timelineTrans.interpolateAt(animatedRenderEnvironment);
//				if (renderTranslation != null) {
//					localLocation.set(renderTranslation);
//				}
//			}
//
//			// Rotation
//			QuatAnimFlag timelineRot = (QuatAnimFlag) idObject.find(MdlUtils.TOKEN_ROTATION);
//			if (timelineRot != null) {
//				Quat renderRotation = timelineRot.interpolateAt(animatedRenderEnvironment);
//				if (renderRotation != null) {
//					localRotation.set(renderRotation);
//				}
//			}
//
//			// Scale
//			Vec3AnimFlag timeline = (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_SCALING);
//			if (timeline != null) {
//				Vec3 renderScale = timeline.interpolateAt(animatedRenderEnvironment);
//				if (renderScale != null) {
//					localScale.set(renderScale);
//				}
//			}
//
//			node.setTransformation(localLocation, localRotation, localScale);
//		}
//	}

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


	private void createSetNewEntriesAction(IdObject objToRebind) {
//		IdObject obj = objToRebind.getParent();
		Bone obj = (Bone) objToRebind.getParent().copy();
		obj.setParent(null);

		UndoAction addBoneAction = new DrawBoneAction(modelHandler.getModelView(), null, obj);
		keyframeActions.add(addBoneAction);

//		Bone bone = new Bone();
		List<AnimFlag<?>> replacementFlags = new ArrayList<>();
		if (!newTransKF.isEmpty()) {
			System.out.println("fixing translation");
			AnimFlag<?> translationOrg = obj.find(MdlUtils.TOKEN_TRANSLATION);
			Vec3AnimFlag translation;
			if (translationOrg == null) {
				translation = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
				UndoAction action = new AddAnimFlagAction(obj, translation, null);
				keyframeActions.add(action);
			} else {
				translation = (Vec3AnimFlag) translationOrg.getEmptyCopy();
			}

			for (int i : newTransKF.keySet()) {
				System.out.println("new entry at " + i + ": " + newTransKF.get(i));
				translation.addEntry(i, newTransKF.get(i));
			}
			replacementFlags.add(translation);
		}

		if (!newScaleKF.isEmpty()) {
			System.out.println("fixing scaling");
			AnimFlag<?> scalingOrg = obj.find(MdlUtils.TOKEN_SCALING);
			Vec3AnimFlag scaling;
			if (scalingOrg == null) {
				scaling = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING);
				UndoAction action = new AddAnimFlagAction(obj, scaling, null);
				keyframeActions.add(action);
			} else {
				scaling = (Vec3AnimFlag) scalingOrg.getEmptyCopy();
			}

			for (int i : newScaleKF.keySet()) {
				scaling.addEntry(i, newScaleKF.get(i));
			}
			replacementFlags.add(scaling);
		}

		if (!newRotKF.isEmpty()) {
			System.out.println("fixing rotation");
			AnimFlag<?> rotationOrg = obj.find(MdlUtils.TOKEN_ROTATION);
			QuatAnimFlag rotation;
			if (rotationOrg == null) {
				rotation = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
				UndoAction action = new AddAnimFlagAction(obj, rotation, null);
				keyframeActions.add(action);
			} else {
				rotation = (QuatAnimFlag) rotationOrg.getEmptyCopy();
			}

			for (int i : newRotKF.keySet()) {
				rotation.addEntry(i, newRotKF.get(i));
			}
			replacementFlags.add(rotation);
		}
		if (!replacementFlags.isEmpty()) {
			UndoAction action = new ReplaceAnimFlagsAction(obj, replacementFlags, null);
			keyframeActions.add(action);
		}
		UndoAction newParentAction = new ParentChangeAction(objToRebind, obj, null);
		keyframeActions.add(newParentAction);

		compoundAction = new CompoundAction("Baked and changed Parent", keyframeActions);
	}


	@Override
	public String actionName() {
		return "Baked and changed Parent";
	}
}
