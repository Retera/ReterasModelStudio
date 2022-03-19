package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class BakeAndRebindActionTwi2 implements UndoAction {
	ModelHandler modelHandler;
	TreeSet<Integer> transKF = new TreeSet<>();
	TreeSet<Integer> scaleKF = new TreeSet<>();
	TreeSet<Integer> rotKF = new TreeSet<>();
	TreeMap<Integer, Vec3> diffTransKF = new TreeMap<>();
	TreeMap<Integer, Vec3> diffScaleKF = new TreeMap<>();
	TreeMap<Integer, Quat> diffRotKF = new TreeMap<>();
	TreeMap<Integer, Quat> diffRotKF2 = new TreeMap<>();
	TreeSet<Integer> allKF = new TreeSet<>();
	Quat rotXaxis90 = new Quat().setFromAxisAngle(Vec3.X_AXIS, (float) (Math.PI/2.0));
	Quat rotXaxis90inv = new Quat(rotXaxis90).invertRotation();

	List<UndoAction> keyframeActions = new ArrayList<>();

	CompoundAction compoundAction;
	IdObject objToRebind;
	IdObject oldParent;
	IdObject newParent;
	IdObject tempNewObject;

	AnimFlag<Vec3> orgTranslationFlag;
	AnimFlag<Vec3> orgScalingFlag;
	AnimFlag<Quat> orgRotationFlag;

	AnimFlag<Vec3> newTranslationFlag;
	AnimFlag<Vec3> newScalingFlag;
	AnimFlag<Quat> newRotationFlag;

	int defCalc = 0;
	int rotCalc = 0;
	public BakeAndRebindActionTwi2(IdObject objToRebind, IdObject newParent, ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		this.objToRebind = objToRebind;
		this.oldParent = objToRebind.getParent();
		this.newParent = newParent;

		orgTranslationFlag = objToRebind.getTranslationFlag();
		orgScalingFlag = objToRebind.getScalingFlag();
		orgRotationFlag = objToRebind.getRotationFlag();

		tempNewObject = objToRebind.copy();
		tempNewObject.setParent(newParent);
		tempNewObject.setName("tempNewObject");
		tempNewObject.remove(MdlUtils.TOKEN_ROTATION);
		tempNewObject.remove(MdlUtils.TOKEN_TRANSLATION);

		modelHandler.getModel().add(tempNewObject); // for testing to be able to see if I'm close to solving this...

		RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView());
		TimeEnvironmentImpl timeEnvironment = renderModel.getTimeEnvironment();

		allKF.addAll(transKF);
		allKF.addAll(scaleKF);
		allKF.addAll(rotKF);

		newScalingFlag = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.LINEAR, null);
		tempNewObject.add(newScalingFlag);
		newTranslationFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.LINEAR, null);
		tempNewObject.add(newTranslationFlag);
		newRotationFlag = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.LINEAR, null);
		tempNewObject.add(newRotationFlag);

		calcWorldThings(renderModel, timeEnvironment, modelHandler);

		tempNewObject.setParent(null);
		modelHandler.getModel().remove(tempNewObject);


		System.out.println("defaulted to first Quat " + defCalc + " of " + rotCalc + " times");
	}


	private void calcWorldThings(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, ModelHandler modelHandler) {
		renderModel.refreshFromEditor(null);


		List<Animation> anims = modelHandler.getModel().getAnims();

//		List<Animation> anims = modelHandler.getModel().getAnims().stream()
//				.filter(a -> a.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase("Walk")
//						|| a.getName().toLowerCase(Locale.ROOT).startsWith("stand")
////						|| a.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase("stand")
//						|| a.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase("Death")
//				)
//				.collect(Collectors.toList());


		// update and set rotation stuff
		calcRotations_v4(renderModel, timeEnvironment, anims);

//		printStuff_v3(renderModel, timeEnvironment, anims);
	}



	private void calcRotations_v4(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, List<Animation> anims) {
		System.out.println("Rotation!");
		Mat4 tempMat = new Mat4();
		Mat4 tempMat2 = new Mat4();

		Vec3 vX = new Vec3(objToRebind.getPivotPoint()).translate(5, 0,0);
		Vec3 vXTemp = new Vec3(vX);
		Vec3 vXTemp1 = new Vec3(vX);
		Vec3 vXTemp2 = new Vec3(vX);
		Vec3 vXTemp3 = new Vec3(vX);
		Vec3 vXOrg = new Vec3(vX);

		for (Animation animation : anims) {
			System.out.println(animation);
			timeEnvironment.setSequence(animation);
			diffRotKF.clear();
			diffRotKF2.clear();
			diffTransKF.clear();
			RenderNode2 orgRenderNode = renderModel.getRenderNode(objToRebind);
			RenderNode2 tempRenderNode = renderModel.getRenderNode(tempNewObject);

			for (int i = 0; i <= animation.getLength(); i++) {
				printKFUpdate(i, animation, "Rot");
				timeEnvironment.setAnimationTime(i);
				renderModel.updateNodes(true, false, false);

				Mat4 parentWorldMatrix = renderModel.getRenderNode(tempNewObject).getParentWorldMatrix();
				Mat4 orgWorldMatrix = renderModel.getRenderNode(objToRebind).getWorldMatrix();
				tempMat.set(parentWorldMatrix).invert().mul(orgWorldMatrix);

				Vec3 pos = getLocationFromMat(tempMat, tempNewObject.getPivotPoint());
				Quat rot = getRotationFromMat(tempMat, renderModel.getRenderNode(tempNewObject).getLocalScale(), i);
				tellInvalid(rot, "Rot1, " + i);


				Quat rot_w_ch = new Quat(renderModel.getRenderNode(objToRebind).getWorldRotation());
				tellInvalid(rot_w_ch, "rot_w_ch1, " + i);
				Quat rot_w_par = new Quat(renderModel.getRenderNode(tempNewObject).getParentWorldRotation());
				tellInvalid(rot_w_par, "rot_w_par1, " + i);

				Quat rot_l_ch = rot_w_par.invertQuat();
				tellInvalid(rot_w_ch, "rot_w_ch2, " + i);
				rot_l_ch.mulLeft(rot_w_ch);//.tellInvalid();
				tellInvalid(rot_w_ch, "rot_w_ch3, " + i);
				rot_l_ch.normalize();

				vXOrg.set(vX).transform(orgRenderNode.getWorldMatrix());
				tempMat2.fromRotationTranslationScaleOrigin(rot, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
				tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
				vXTemp.set(vX).transform(tempMat);

				tempMat2.fromRotationTranslationScaleOrigin(rot_l_ch, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
				tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
				vXTemp1.set(vX).transform(tempMat);


				Quat rotAlt = calcRotAlt(tempMat, tempMat2, vX, vXTemp2, vXTemp3, vXOrg, tempRenderNode, i, pos, rot);


				if(vXTemp2.distance(vXOrg) < vXTemp.distance(vXOrg)
				){
					if(i == 163){
						System.out.println("Added QUAT-rot_l_ch ");
					}
					diffRotKF.put(i, rotAlt);
				} else {
					if(i == 163){
						System.out.println("Added MAT-rot ");
					}

					diffRotKF.put(i, rot);
				}
				if(i == 163){
//						System.out.println("Added MAT-rot " + tooBigDiff(vXTemp.sub(vXOrg), 0.001));
					Quat q1 = new Quat(rot).mul(tempRenderNode.getParentWorldRotation());
					printPairs("MatRot", vXTemp, q1);
					Quat q2 = new Quat(rot_l_ch).mul(tempRenderNode.getParentWorldRotation());
					printPairs("childRot", vXTemp1, q2);
					Quat q3 = new Quat(rotAlt).mul(tempRenderNode.getParentWorldRotation());
					printPairs("RotAlt", vXTemp2, q3);
					printPairs("Org", vXOrg, orgRenderNode.getWorldRotation());
//					System.out.println("(Mat)v1:\t" + vXTemp +  "\n(Mat)v2:\t" + vXTemp1 +  "\n(Quat)v2:\t" + vXTemp2 + "\norg____:\t" + vXOrg);
//					System.out.println("MatRot: " + q1 +  " Mat2Rot: " + q3 +  " QuatRot: " + q2 + " org: " + orgRenderNode.getWorldRotation());
//					System.out.println("MatRot:\t" + q1 + "\nQutRot:\t" + q2 + "\norg:\t" + orgRenderNode.getWorldRotation());

				}


//				if(i == 163){
//					System.out.println("rot_l_ch: " + rot_l_ch + ", rot: " + rot);
//					System.out.println("w_scale: " + renderModel.getRenderNode(tempNewObject).getWorldScale());
//					System.out.println("l_rot: " + renderModel.getRenderNode(tempNewObject).getLocalRotation());
//				}


				diffTransKF.put(i, pos);

			}


			addKeyframes(animation);

			timeEnvironment.setAnimationTime(200);
			renderModel.updateNodes(true, false, false);

			vXOrg.set(vX).transform(orgRenderNode.getWorldMatrix());
			vXTemp.set(vX).transform(tempRenderNode.getWorldMatrix());



			Quat orgWorldRot = orgRenderNode.getWorldRotation();
			Quat tempWorldRot = renderModel.getRenderNode(tempNewObject).getWorldRotation();
			System.out.println("orgWorldRot: " + orgWorldRot + ", tempWorldRot: " + tempWorldRot);
			System.out.println("vXOrg: " + vXOrg + ", vXTemp: " + vXTemp);
			vXOrg.sub(renderModel.getRenderNode(objToRebind).getRenderPivot());
			vXTemp.sub(renderModel.getRenderNode(tempNewObject).getRenderPivot());
			System.out.println("QuatTo (o->t): " + vXOrg.getQuatTo(vXTemp) + ", QuatTo (t->o): " + vXTemp.getQuatTo(vXOrg));
		}
	}

	private void printPairs(String name, Vec3 pos, Quat rot){
		String s = (name + ":                  ").substring(0,16);

		String s1 = (pos + ",                       ").substring(0,40) + "(rot: ";
		System.out.println(s + s1 + rot + ")");
	}

	private void addKeyframes(Animation animation) {
		Quat lastRot = null;
		Integer lastKeyRot = null;
		Vec3 lastLoc = null;
		Integer lastKeyLoc = null;
		for(Integer i : diffRotKF.keySet()){
			Quat quat = diffRotKF.get(i);
			if(quat != null){
				Integer nextKey = diffRotKF.higherKey(i);
				if(lastRot == null || nextKey == null || diffRotKF.get(nextKey) == null){
					newRotationFlag.addEntry(i, quat, animation);
					lastRot = quat;
					lastKeyRot = i;
				} else {
					float t = (i-lastKeyRot)/(float)(nextKey-lastKeyRot);
					Quat interp = Quat.getSlerped(lastRot, diffRotKF.get(nextKey), t);
					Vec3 temp1 = new Vec3(1,0,0);
					Vec3 temp2 = new Vec3(1,0,0);
					temp1.transform(interp);
					temp2.transform(quat);

//						Quat diff = Quat.getSlerped(lastRot, diffRotKF.get(nextKey), t);
//
//						diff.sub(quat);
					Vec3 diff = new Vec3(temp1).sub(temp2);
//						if(i == 200){
//							System.out.println("diff: " + diff);
//						}
					if (tooBigDiff(diff, 0.001)){


						newRotationFlag.addEntry(i, quat, animation);
					}
				}
			}
			Vec3 vec3 = diffTransKF.get(i);
			if(vec3 != null){
				Integer nextKey = diffTransKF.higherKey(i);
//					Vec3 next = diffTransKF.get(nextKey);
				if(lastLoc == null || nextKey == null || diffTransKF.get(nextKey) == null){
					newTranslationFlag.addEntry(i, vec3, animation);
					lastLoc = vec3;
					lastKeyLoc = i;
				} else if(!lastLoc.equalLocs(vec3) || !diffTransKF.get(nextKey).equalLocs(vec3)){
					float t = (i-lastKeyLoc)/(float)(nextKey-lastKeyLoc);
					Vec3 diff = Vec3.getLerped(lastLoc, diffTransKF.get(nextKey), t).sub(vec3);
//						if(i == 200){
//							System.out.println("diff: " + diff);
//						}
					if (tooBigDiff(diff, 0.001)){

						newTranslationFlag.addEntry(i, vec3, animation);
					}
				} else {
//						System.out.println("skipped!");
				}
			}
		}
	}

	private Quat calcRotAlt(Mat4 tempMat, Mat4 tempMat2, Vec3 vX, Vec3 vXTemp2, Vec3 vXTemp3, Vec3 vXOrg, RenderNode2 tempRenderNode, int i, Vec3 pos, Quat rot) {
		rotCalc++;
		Quat rotAlt = new Quat();
		Vec3 vXOrgNorm = new Vec3(vXOrg);

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.X_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.X_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.Y_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.Y_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.Z_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.Z_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_X_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_X_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_Y_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_Y_AXIS))
			return rotAlt;

		rotAlt.set(rot);
		if (testAxisRotThing(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_Z_AXIS)) return rotAlt;

		rotAlt.set(rot);
		if (testAxisStuff2(tempMat, tempMat2, vX, vXTemp2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.NEGATIVE_Z_AXIS))
			return rotAlt;

		rotAlt.set(rot);

		rotAlt = getRotAlt(tempMat, tempMat2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, Vec3.X_AXIS);

		tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
		tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
		vXTemp2.set(vX).transform(tempMat);
		if(i == 163){
		System.out.println("defaulted to first Quat");
		}
		defCalc++;
		return rotAlt;
	}

	private boolean testAxisStuff2(Mat4 tempMat, Mat4 tempMat2, Vec3 vX, Vec3 vXTemp2, Vec3 vXOrg, RenderNode2 tempRenderNode, Vec3 pos, Quat rotAlt, Vec3 vXOrgNorm, Vec3 axis) {
		for (int itt = 0; itt < 10; itt++) {
			rotAlt.set(getRotAlt2(tempMat, tempMat2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, axis));
			tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
			tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
			vXTemp2.set(vX).transform(tempMat);
			if (!tooBigDiff(vXTemp2.sub(vXOrg), 0.001)) {
				tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
				tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
				vXTemp2.set(vX).transform(tempMat);
				return true;
			}
		}
		return false;
	}

	private boolean testAxisRotThing(Mat4 tempMat, Mat4 tempMat2, Vec3 vX, Vec3 vXTemp2, Vec3 vXOrg, RenderNode2 tempRenderNode, Vec3 pos, Quat rotAlt, Vec3 vXOrgNorm, Vec3 axis) {
		for (int itt = 0; itt < 10; itt++) {
			rotAlt.set(getRotAlt(tempMat, tempMat2, vXOrg, tempRenderNode, pos, rotAlt, vXOrgNorm, axis));
			tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
			tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
			vXTemp2.set(vX).transform(tempMat);
			if (!tooBigDiff(vXTemp2.sub(vXOrg), 0.001)) {
				tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
				tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
				vXTemp2.set(vX).transform(tempMat);
				return true;
			}
		}
		return false;
	}

	private Quat getRotAlt(Mat4 tempMat, Mat4 tempMat2, Vec3 vXOrg, RenderNode2 tempRenderNode, Vec3 pos, Quat rotAlt, Vec3 vXOrgNorm, Vec3 axis) {
		tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
		tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2).invert();
		vXOrgNorm.set(vXOrg).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();
//				Vec3 vXTempNorm = new Vec3(vXTemp).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();
//		vXTempNorm.set(vX).sub(objToRebind.getPivotPoint()).normalize();

		rotAlt = axis.getQuatTo(vXOrgNorm).invertRotation().normalize().mulLeft(rotAlt);
		return rotAlt;
	}
	private Quat getRotAlt2(Mat4 tempMat, Mat4 tempMat2, Vec3 vXOrg, RenderNode2 tempRenderNode, Vec3 pos, Quat rotAlt, Vec3 vXOrgNorm, Vec3 axis) {
		tempMat2.fromRotationTranslationScaleOrigin(rotAlt, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
		tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2).invert();
		vXOrgNorm.set(vXOrg).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();
//				Vec3 vXTempNorm = new Vec3(vXTemp).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();
//		vXTempNorm.set(vX).sub(objToRebind.getPivotPoint()).normalize();

		rotAlt = axis.getQuatTo(vXOrgNorm).normalize().mulLeft(rotAlt);
		return rotAlt;
//		return rotAlt.setAsRotBetween(axis, vXOrgNorm).normalize().mul(rotAlt);
	}

	private Quat nudgeRot(RenderModel renderModel, int i){
		Mat4 tempMat = new Mat4();
		Mat4 tempMat2 = new Mat4();
		RenderNode2 orgRenderNode = renderModel.getRenderNode(objToRebind);
		RenderNode2 tempRenderNode = renderModel.getRenderNode(tempNewObject);

//		tempNewObject.getP
		Vec3 parentRenderPivot = tempRenderNode.getParentRenderPivot();
		Vec3 renderPivot = tempRenderNode.getRenderPivot();

		Vec3 vX = new Vec3(objToRebind.getPivotPoint()).translate(1, 0,0);
		Vec3 vXTemp = new Vec3(vX);
		Vec3 vXTempNorm = new Vec3(vXTemp);
		Vec3 vXTemp1 = new Vec3(vX);
		Vec3 vXTemp2 = new Vec3(vX);
		Vec3 vXOrg = new Vec3(vX);
		Vec3 vXOrgNorm = new Vec3(vXOrg);

		Vec3 pos = getLocationFromMat(tempMat, tempNewObject.getPivotPoint());
		Quat rot = getRotationFromMat(tempMat, renderModel.getRenderNode(tempNewObject).getLocalScale(), i);


		Quat rot_w_ch = new Quat(renderModel.getRenderNode(objToRebind).getWorldRotation());
		tellInvalid(rot_w_ch, "rot_w_ch1, " + i);
		Quat rot_w_par = new Quat(renderModel.getRenderNode(tempNewObject).getParentWorldRotation());
		tellInvalid(rot_w_par, "rot_w_par1, " + i);

		Quat rot_l_ch = rot_w_par.invertQuat();
		tellInvalid(rot_w_ch, "rot_w_ch2, " + i);
		rot_l_ch.mulLeft(rot_w_ch);//.tellInvalid();
		tellInvalid(rot_w_ch, "rot_w_ch3, " + i);
		rot_l_ch.normalize();

		vXOrg.set(vX).transform(orgRenderNode.getWorldMatrix());

		tempMat2.fromRotationTranslationScaleOrigin(rot, pos, tempRenderNode.getLocalScale(), objToRebind.getPivotPoint());
		tempMat.set(tempRenderNode.getParentWorldMatrix()).mul(tempMat2);
		vXTemp.set(vX).transform(tempMat);

		tempMat.set(tempRenderNode.getWorldMatrix()).invert();
//		vXOrgNorm.set(vXOrg).sub(renderPivot).normalize();
//		vXTempNorm.set(vXTemp).sub(renderPivot).normalize();
		vXOrgNorm.set(vXOrg).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();
		vXTempNorm.set(vXTemp).transform(tempMat).sub(objToRebind.getPivotPoint()).normalize();

//		vXOrgNorm.getQuatTo(vXTempNorm)

		return vXOrgNorm.getQuatTo(vXTempNorm);
//		return new Quat();
	}


	private boolean tooBigDiff(Vec3 diff, double v) {
		return !(-v < diff.x && diff.x < v
				&& -v < diff.y && diff.y < v
				&& -v < diff.z && diff.z < v
//								&& -0.001 < diff.w && diff.w < 0.001
		);
	}

	public Vec3 getLocationFromMat(Mat4 mat4, Vec3 pivot) {
		Vec3 loc = new Vec3();

		loc.x = mat4.m30 + ((mat4.m00 * pivot.x) + (mat4.m10 * pivot.y) + (mat4.m20 * pivot.z)) - pivot.x;
		loc.y = mat4.m31 + ((mat4.m01 * pivot.x) + (mat4.m11 * pivot.y) + (mat4.m21 * pivot.z)) - pivot.y;
		loc.z = mat4.m32 + ((mat4.m02 * pivot.x) + (mat4.m12 * pivot.y) + (mat4.m22 * pivot.z)) - pivot.z;

		return loc;
	}

	public Quat getRotationFromMat(Mat4 mat4, Vec3 scale, int i) {
		Quat rot = new Quat();
//		float xx = rot.x * rot.x * 2;
//		float xy = rot.x * rot.y * 2;
//		float xz = rot.x * rot.z * 2;
//		float yy = rot.y * rot.y * 2;
//		float yz = rot.y * rot.z * 2;
//		float zz = rot.z * rot.z * 2;
//		float wx = rot.w * rot.x * 2;
//		float wy = rot.w * rot.y * 2;
//		float wz = rot.w * rot.z * 2;
//
//		m00 = (1 - (yy + zz))   * scale.x;
//		m01 = (xy + wz)         * scale.x;
//		m02 = (xz - wy)         * scale.x;
//		m10 = (xy - wz)         * scale.y;
//		m11 = (1 - (xx + zz))   * scale.y;
//		m12 = (yz + wx)         * scale.y;
//		m20 = (xz + wy)         * scale.z;
//		m21 = (yz - wx)         * scale.z;
//		m22 = (1 - (xx + yy))   * scale.z;

//		System.out.println("ugg");

//		float xx = rot.x * rot.x;
//		float xy = rot.x * rot.y;
//		float xz = rot.x * rot.z;
//		float yy = rot.y * rot.y;
//		float yz = rot.y * rot.z;
//		float zz = rot.z * rot.z;
//		float wx = rot.w * rot.x;
//		float wy = rot.w * rot.y;
//		float wz = rot.w * rot.z;
//		float t00 = mat4.m00 / scale.x;
//		float t01 = mat4.m01 / scale.x;
//		float t02 = mat4.m02 / scale.x;
//		float t10 = mat4.m10 / scale.y;
//		float t11 = mat4.m11 / scale.y;
//		float t12 = mat4.m12 / scale.y;
//		float t20 = mat4.m20 / scale.z;
//		float t21 = mat4.m21 / scale.z;
//		float t22 = mat4.m22 / scale.z;
		float t00 = mat4.m00 / scale.x / 2;
		float t01 = mat4.m01 / scale.x / 2;
		float t02 = mat4.m02 / scale.x / 2;
		float t10 = mat4.m10 / scale.y / 2;
		float t11 = mat4.m11 / scale.y / 2;
		float t12 = mat4.m12 / scale.y / 2;
		float t20 = mat4.m20 / scale.z / 2;
		float t21 = mat4.m21 / scale.z / 2;
		float t22 = mat4.m22 / scale.z / 2;


//		t12 = y*z + w*x        ;
//		t21 = y*z - w*x        ;
//		t20 = x*z + w*y        ;
//		t02 = x*z - w*y        ;
//		t01 = x*y + w*z        ;
//		t10 = x*y - w*z        ;
//		t00 = .5 - y*y - z*z  ;
//		t11 = .5 - x*x - z*z  ;
//		t22 = .5 - x*x - y*y  ;


		//z*z = .5 - y*y - t00
		//z*z = .5 - x*x - t11
		//.5 - y*y - t00 = .5 - x*x - t11
		// y*y = x*x + t11 - t00
		// xx = (.5 - t22 - t11 + t00)/2
		// yy = xx + t11 - t00
		// zz = .5 - xx - t11


		//t12-t21 = 2 * w*x
		//t20-t02 = 2 * w*y
		//t01-t10 = 2 * w*z
		// w = t12-t21/2/x


		//t12+t21 = 2 * y*z
		//t20+t02 = 2 * x*z
		//t01+t10 = 2 * x*y
//		float x = (t02 + w*y)/z;
//		float y = (t10 + w*z)/x;
//		float z = (t12 - w*x)/y;

//		float w = (t01 - x*y)/z;




		float xx = (float) ((.5 - t22 - t11 + t00)/2.0);
		float yy = xx + t11 - t00;
		float zz = (float) (.5 - xx - t11);


		float zz1 = (float) ((t22 - t11 + 1 - t00)/2.0);
		float xx1 = 1 - t11 - zz;
		float yy1 = t11 + zz - t22;


		rot.x = (float) Math.sqrt(xx/2.0f);
		rot.y = (float) Math.sqrt(yy/2.0f);
		rot.z = (float) Math.sqrt(zz/2.0f);
		if(xx < 0){
			if(0 <= zz){
				rot.x = (t02 + t20)/2.0f/rot.z;
			} else if(0 <= yy){
				rot.x = (t01 + t10)/2.0f/rot.y;
			}
//		t20 = x*z + w*y        ;
//		t02 = x*z - w*y        ;
//		xxx = x*z + x*z = t02 + t20      ;
//		xxx = 2 * x*z  = t02 + t20      ;
//		xxx = x  = (t02 + t20)/2/z      ;
		}
		if(yy < 0){
			if(0 <= xx){
				rot.y = (t10  + t01)/2/rot.x;
			} else if(0 <= zz){
				rot.y = (t12  + t21)/2/rot.z;
			}

//		t01 = x*y + w*z        ;
//		t10 = x*y - w*z        ;
//		t10  + t01 = x*y + x*y
//		(t10  + t01)/2/rot.x = y
//			rot.y = (t10  + t01)/2/rot.x;


		}
		if(zz < 0){
			if(0 <= xx){
				rot.z = (t02 + t20)/2/rot.x;
			} else if(0 <= yy){
				rot.z = (t12  + t21)/2/rot.y;
			}

//		t01 = x*y + w*z        ;
//		t10 = x*y - w*z        ;
//		t10  + t01 = x*y + x*y
//		(t10  + t01)/2/rot.x = y
//			rot.y = (t10  + t01)/2/rot.x;


		}

		tellInvalid(rot, i + ": ");
		if(Float.isNaN(rot.x)){
			System.out.println(i + ": xx = ((.5 - " + t22 + " - " +  t11 + " + " + t00 + ")/2.0)" + " = " + xx);
			System.out.println(i + ": x = Math.sqrt(" + xx + "/2.0) = " + rot.x + " rot: " + rot);
		}


//		rot.w = (t01 - rot.x*rot.y)/rot.z;
//		rot.w = (t12-t21)/2/rot.x;

//		if(i == 200){
//			System.out.println("rot: \t" + rot);
//		}

		if(i == 200 || true){
			debugPrint("t00: \t" + t00, i);
			debugPrint("t01: \t" + t01, i);
			debugPrint("t02: \t" + t02, i);
			debugPrint("t10: \t" + t10, i);
			debugPrint("t11: \t" + t11, i);
			debugPrint("t12: \t" + t12, i);
			debugPrint("t20: \t" + t20, i);
			debugPrint("t21: \t" + t21, i);
			debugPrint("t22: \t" + t22, i);


			float xy = (t01+t10)/2.0f;
			float xz = (t20+t02)/2.0f;
			float yz = (t12 + t21)/2.0f;
			float wx = (t12 - t21)/2.0f;
			float wy = (t20 - t02)/2.0f;
			float wz = (t01 - t10)/2.0f;
//			float xy = (t01+t10)/2;
//			float xz = (t20+t02)/2;
//			float yz = (t12 + t21)/2;
//			float wx = (t12 - t21)/2;
//			float wy = (t20 - t02)/2;
//			float wz = (t01 - t10)/2;
			debugPrint("x*y = \t" + xy, i);
			debugPrint("x*z = \t" + xz, i);
			debugPrint("y*z = \t" + yz, i);
			debugPrint("w*x = \t" + wx, i);
			debugPrint("w*y = \t" + wy, i);
			debugPrint("w*z = \t" + wz, i);


//			float x_sing_p = rot.x;
//			float y_sing_p = rot.y;
//			float z_sing_p = rot.z;
//			float x_sing_n = -rot.x;
//			float y_sing_n = -rot.y;
//			float z_sing_n = -rot.z;
//			float x_sing_p = 1;
//			float y_sing_p = 1;
//			float z_sing_p = 1;
//			float x_sing_n = -1;
//			float y_sing_n = -1;
//			float z_sing_n = -1;



			float xy_sign = Math.signum(xy);
			float xz_sign = Math.signum(xz);
			float yz_sign = Math.signum(yz);
			float wx_sign = Math.signum(wx);
			float wy_sign = Math.signum(wy);
			float wz_sign = Math.signum(wz);


			debugPrint("rot: \t" + rot, i);
			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
				debugPrint("all pos, w pos/all neg, w neg", i);
			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
				debugPrint("x neg, w pos/y neg, z neg, w neg", i);
				rot.x = -rot.x;
//				rot.y = -rot.y;
//				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
				debugPrint("x neg, y neg, w pos/z neg, w neg", i);
				rot.x = -rot.x;
				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
				debugPrint("x neg, z neg, w pos/y neg, w neg", i);

				rot.x = -rot.x;
				rot.z = -rot.z;
//				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
				debugPrint("y neg, w pos/x neg, z neg, w neg", i);

				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
				debugPrint("y neg, z neg, w pos/x neg, w neg", i);

				rot.y = -rot.y;
				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
				debugPrint("z neg, w pos/x neg, y neg, w neg", i);
				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
				debugPrint("all neg, w pos/all pos, w neg", i);
				rot.x = -rot.x;
				rot.y = -rot.y;
				rot.z = -rot.z;
			}

//			rot.w = (t01 - rot.x*rot.y)/rot.z;
			if(rot.x != 0){

				rot.w = (t12-t21)/2/rot.x;
			}else if(rot.y != 0){
				rot.w = (t20 - t02)/2/rot.y;
//		t20 = x*z + w*y        ;
//		t02 = x*z - w*y        ;
//		t20 - t02 = x*z + w*y - x*z + w*y       ;
//		t20 - t02 = w*y + w*y       ;
//		t20 - t02 = 2 * w*y       ;
//		(t20 - t02)/2/y = w       ;
			} else if (rot.z != 0){
//		t01 = x*y + w*z        ;
//		t10 = x*y - w*z        ;
//		t01 - t10 = w*z + w*z        ;
//		t01 - t10 =  2 * w*z        ;
//		(t01 - t10)/2/z = w        ;
				rot.w = (t01 - t10)/2/rot.z;
			} else {
				rot.w = 1;
			}
			if(Float.isNaN(rot.w)){
				System.out.println(i + ": W = (" + t12 + " - " + t21 + ") / 2 / " + rot.x + " = " + rot.w + " rot: " + rot);
			}
			debugPrint("rot: \t" + rot, i);
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("all pos, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, y neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, z neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("y neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("y neg, z neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("z neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("all neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("all pos, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, y neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, z neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("y neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("y neg, z neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("z neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("all neg, w neg");
//			}




			/////////////
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f){
//				System.out.println("all pos");
//			}
//			if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f){
//				System.out.println("x neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f){
//				System.out.println("x neg, y neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f){
//				System.out.println("x neg, z neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f){
//				System.out.println("y neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f){
//				System.out.println("y neg, z neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f){
//				System.out.println("z neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f){
//				System.out.println("all neg");
//			}

			//////////////////
//			if(xy_sign == Math.signum(x_sing_p * y_sing_p) && xz_sign == Math.signum(x_sing_p*z_sing_p) && yz_sign == Math.signum(y_sing_p*z_sing_p)){
//				System.out.println("all pos");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_p) && xz_sign == Math.signum(x_sing_n*z_sing_p) && yz_sign == Math.signum(y_sing_p*z_sing_p)){
//				System.out.println("x neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_n) && xz_sign == Math.signum(x_sing_n*z_sing_p) && yz_sign == Math.signum(y_sing_n*z_sing_p)){
//				System.out.println("x neg, y neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_p) && xz_sign == Math.signum(x_sing_n*z_sing_n) && yz_sign == Math.signum(y_sing_p*z_sing_n)){
//				System.out.println("x neg, z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p*y_sing_n) && xz_sign == Math.signum(x_sing_p*z_sing_p) && yz_sign == Math.signum(y_sing_n*z_sing_p)){
//				System.out.println("y neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p*y_sing_n) && xz_sign == Math.signum(x_sing_p*z_sing_n) && yz_sign == Math.signum(y_sing_n*z_sing_n)){
//				System.out.println("y neg, z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p * y_sing_p) && xz_sign == Math.signum(x_sing_p*z_sing_n) && yz_sign == Math.signum(y_sing_p*z_sing_n)){
//				System.out.println("z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_n) && xz_sign == Math.signum(x_sing_n*z_sing_n) && yz_sign == Math.signum(y_sing_n*z_sing_n)){
//				System.out.println("all neg");
//			}


		}



		rot.normalize();
		return rot;
	}

	boolean DEBUG = false;
	private void debugPrint(String s, int i) {
		if(DEBUG){
			if(i == 90 || i == 1000){
				System.out.println(i + ": " + s);
			}
		}
	}

	private Quat tellNan(Quat q, String s){

		if (Float.isNaN(q.w)) {
			q.w = 0;
			System.out.println(s + ": W nan");
		}
		return q;
	}

	public Quat tellInvalid(Quat q, String s) {
		if (Float.isNaN(q.x)) {
			q.x = 0;
			System.out.println(s + ": X nan");
		}
		if (Float.isNaN(q.y)) {
			q.y = 0;
			System.out.println(s + ": Y nan");
		}
		if (Float.isNaN(q.z)) {
			q.z = 0;
			System.out.println(s + ": Z nan");
		}
		if (Float.isNaN(q.w)) {
			q.w = 0;
			System.out.println(s + ": W nan");
		}
		if (Float.isInfinite(q.x)) {
			q.x = 1;
			System.out.println(s + ": X inf");
		}
		if (Float.isInfinite(q.y)) {
			q.y = 1;
			System.out.println(s + ": Y inf");
		}
		if (Float.isInfinite(q.z)) {
			q.z = 1;
			System.out.println(s + ": Z inf");
		}
		if (Float.isInfinite(q.w)) {
			q.w = 1;
			System.out.println(s + ": W inf");
		}

		return q;
	}

	public Quat getRotationFromMat2(Mat4 mat4, Vec3 scale, int i) {
		Quat rot = new Quat();
//		float xx = rot.x * rot.x * 2;
//		float xy = rot.x * rot.y * 2;
//		float xz = rot.x * rot.z * 2;
//		float yy = rot.y * rot.y * 2;
//		float yz = rot.y * rot.z * 2;
//		float zz = rot.z * rot.z * 2;
//		float wx = rot.w * rot.x * 2;
//		float wy = rot.w * rot.y * 2;
//		float wz = rot.w * rot.z * 2;
//
//		m00 = (1 - (yy + zz))   * scale.x;
//		m01 = (xy + wz)         * scale.x;
//		m02 = (xz - wy)         * scale.x;
//		m10 = (xy - wz)         * scale.y;
//		m11 = (1 - (xx + zz))   * scale.y;
//		m12 = (yz + wx)         * scale.y;
//		m20 = (xz + wy)         * scale.z;
//		m21 = (yz - wx)         * scale.z;
//		m22 = (1 - (xx + yy))   * scale.z;




		float t00 = mat4.m00 / scale.x;
		float t01 = mat4.m01 / scale.x;
		float t02 = mat4.m02 / scale.x;
		float t10 = mat4.m10 / scale.y;
		float t11 = mat4.m11 / scale.y;
		float t12 = mat4.m12 / scale.y;
		float t20 = mat4.m20 / scale.z;
		float t21 = mat4.m21 / scale.z;
		float t22 = mat4.m22 / scale.z;
//
//		float t00 = mat4.m00 / scale.x / 2;
//		float t01 = mat4.m01 / scale.x / 2;
//		float t02 = mat4.m02 / scale.x / 2;
//		float t10 = mat4.m10 / scale.y / 2;
//		float t11 = mat4.m11 / scale.y / 2;
//		float t12 = mat4.m12 / scale.y / 2;
//		float t20 = mat4.m20 / scale.z / 2;
//		float t21 = mat4.m21 / scale.z / 2;
//		float t22 = mat4.m22 / scale.z / 2;


//
//		t00 = 1 - yy - zz  ;
//		t01 = xy + wz        ;
//		t02 = xz - wy        ;
//		t10 = xy - wz        ;
//		t11 = 1 - xx - zz ;
//		t12 = yz + wx        ;
//		t20 = xz + wy        ;
//		t21 = yz - wx        ;
//		t22 = 1 - xx - yy  ;
//
//		zz = t22 - t11 + yy;
//		xx = 1 - t22 - yy;
//
//		yy = 1 - t00 - zz  ;


//		t00 = (.5 - yy - zz)  ;
//		t01 = (xy + wz)        ;
//		t02 = (xz - wy)        ;
//		t10 = (xy - wz)        ;
//		t11 = (.5 - xx - zz)  ;
//		t12 = (yz + wx)        ;
//		t20 = (xz + wy)        ;
//		t21 = (yz - wx)        ;
//		t22 = (.5 - xx - yy)  ;


//		t00 = .5 - y*y - z*z  ;
//		t01 = x*y + w*z        ;-
//		t02 = x*z - w*y        ;-
//		t10 = x*y - w*z        ;-
//		t11 = .5 - x*x - z*z  ;
//		t12 = y*z + w*x        ;-
//		t20 = x*z + w*y        ;-
//		t21 = y*z - w*x        ;
//		t22 = .5 - x*x - y*y  ;


//		float w = (t01 - x*y)/z;
//		float x = (t02 + w*y)/z;
//		float y = (t10 + w*z)/x;
//		float z = (t12 - w*x)/y;
//
////		t20 = t02 + 2 * w*y;
//		w = (t20-t02)/2/y;
////		t02 = x*z - (t20-t02)/2;
//		x = (t02 + (t20-t02)/2)/z;
//
////		t02 = (t02 + (t20-t02)/2) - w*y;
//
//		t20 = x*z + w*y;
//		t02 = x*z - w*y;
//
//		t12 = y*z + w*x;
//		t21 = y*z - w*x;
//
////		t12 + t21 = 2 *y*z;
//		y = (t12 + t21)/2/z;
//
////		t20 + t02 = 2 *x*z;
//
//
////		t12 = y*z + w*x        ;
////		t21 = y*z - w*x        ;
////		t12 - t21 = w*x + w*x        ;
////		t12 + t21 = 2*y*z      ;
//		y = (t12 + t21)/2/z;
////		z = (t12 + t21)/2/y;
//
////		t20 = x*z + w*y        ;
////		t02 = x*z - w*y        ;
////		t20 - t02 = w*y + w*y        ;
////		t20+t02 = 2*x*z        ;
//		x = (t20+t02)/2/z;
//
//		x = ((t20+t02)/2)/((t12 + t21)/2/y);
//
////		t01 = x*y + w*z        ;
////		t10 = x*y - w*z        ;
////		t01 - t10 = w*z + w*z        ;
////		t01+t10 = 2*x*y        ;
//		y = (t01+t10)/2/x;
//
//
////		t00 = .5 - y*y - z*z  ;
////		t11 = .5 - x*x - z*z   ;
////		t22 = .5 - x*x - y*y   ;




		float zz = (t22 - t11 + 1 - t00)/2;
		float xx = 1 - t11 - zz;
		float yy = t11 + zz - t22;


		rot.x = (float) Math.sqrt(xx/2.0);
		rot.y = (float) Math.sqrt(yy/2.0);
		rot.z = (float) Math.sqrt(zz/2.0);
		rot.w = (t01 - rot.x*rot.y)/rot.z;

//		if(i == 200){
//			System.out.println("rot: \t" + rot);
//		}

		if(i == 200){
			debugPrint("t00: \t" + t00, i);
			debugPrint("t01: \t" + t01, i);
			debugPrint("t02: \t" + t02, i);
			debugPrint("t10: \t" + t10, i);
			debugPrint("t11: \t" + t11, i);
			debugPrint("t12: \t" + t12, i);
			debugPrint("t20: \t" + t20, i);
			debugPrint("t21: \t" + t21, i);
			debugPrint("t22: \t" + t22, i);


			float xy = (t01+t10)/4.0f;
			float xz = (t20+t02)/4.0f;
			float yz = (t12 + t21)/4.0f;
			float wx = (t12 - t21)/4.0f;
			float wy = (t20 - t02)/4.0f;
			float wz = (t01 - t10)/4.0f;
//			float xy = (t01+t10)/2;
//			float xz = (t20+t02)/2;
//			float yz = (t12 + t21)/2;
//			float wx = (t12 - t21)/2;
//			float wy = (t20 - t02)/2;
//			float wz = (t01 - t10)/2;
			debugPrint("x*y = \t" + xy, i);
			debugPrint("x*z = \t" + xz, i);
			debugPrint("y*z = \t" + yz, i);
			debugPrint("w*x = \t" + wx, i);
			debugPrint("w*y = \t" + wy, i);
			debugPrint("w*z = \t" + wz, i);


//			float x_sing_p = rot.x;
//			float y_sing_p = rot.y;
//			float z_sing_p = rot.z;
//			float x_sing_n = -rot.x;
//			float y_sing_n = -rot.y;
//			float z_sing_n = -rot.z;
			float x_sing_p = 1;
			float y_sing_p = 1;
			float z_sing_p = 1;
			float x_sing_n = -1;
			float y_sing_n = -1;
			float z_sing_n = -1;


			float xpyp = Math.signum(x_sing_p * y_sing_p);
			float xy_sign = Math.signum(xy);
			float xz_sign = Math.signum(xz);
			float yz_sign = Math.signum(yz);
			float wx_sign = Math.signum(wx);
			float wy_sign = Math.signum(wy);
			float wz_sign = Math.signum(wz);


			debugPrint("rot: \t" + rot, i);
			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
				debugPrint("all pos, w pos/all neg, w neg", i);
			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
				debugPrint("x neg, w pos/y neg, z neg, w neg", i);
//				rot.x = -rot.x;
				rot.y = -rot.y;
				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
				debugPrint("x neg, y neg, w pos/z neg, w neg", i);
				rot.x = -rot.x;
				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
				debugPrint("x neg, z neg, w pos/y neg, w neg", i);

				rot.x = -rot.x;
				rot.z = -rot.z;
//				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
				debugPrint("y neg, w pos/x neg, z neg, w neg", i);

				rot.y = -rot.y;
			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
				debugPrint("y neg, z neg, w pos/x neg, w neg", i);

				rot.y = -rot.y;
				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
				debugPrint("z neg, w pos/x neg, y neg, w neg", i);
				rot.z = -rot.z;
			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
				debugPrint("all neg, w pos/all pos, w neg", i);
			}

			rot.w = (t01 - rot.x*rot.y)/rot.z;
			debugPrint("rot: \t" + rot, i);
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("all pos, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, y neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, z neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("y neg, w pos");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("y neg, z neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("z neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("all neg, w pos");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("all pos, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("x neg, y neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("x neg, z neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
//				System.out.println("y neg, w neg");
//			} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("y neg, z neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
//				System.out.println("z neg, w neg");
//			} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
//				System.out.println("all neg, w neg");
//			}




			/////////////
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f){
//				System.out.println("all pos");
//			}
//			if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f){
//				System.out.println("x neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f){
//				System.out.println("x neg, y neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f){
//				System.out.println("x neg, z neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f){
//				System.out.println("y neg");
//			}
//			if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f){
//				System.out.println("y neg, z neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f){
//				System.out.println("z neg");
//			}
//			if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f){
//				System.out.println("all neg");
//			}

			//////////////////
//			if(xy_sign == Math.signum(x_sing_p * y_sing_p) && xz_sign == Math.signum(x_sing_p*z_sing_p) && yz_sign == Math.signum(y_sing_p*z_sing_p)){
//				System.out.println("all pos");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_p) && xz_sign == Math.signum(x_sing_n*z_sing_p) && yz_sign == Math.signum(y_sing_p*z_sing_p)){
//				System.out.println("x neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_n) && xz_sign == Math.signum(x_sing_n*z_sing_p) && yz_sign == Math.signum(y_sing_n*z_sing_p)){
//				System.out.println("x neg, y neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_p) && xz_sign == Math.signum(x_sing_n*z_sing_n) && yz_sign == Math.signum(y_sing_p*z_sing_n)){
//				System.out.println("x neg, z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p*y_sing_n) && xz_sign == Math.signum(x_sing_p*z_sing_p) && yz_sign == Math.signum(y_sing_n*z_sing_p)){
//				System.out.println("y neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p*y_sing_n) && xz_sign == Math.signum(x_sing_p*z_sing_n) && yz_sign == Math.signum(y_sing_n*z_sing_n)){
//				System.out.println("y neg, z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_p * y_sing_p) && xz_sign == Math.signum(x_sing_p*z_sing_n) && yz_sign == Math.signum(y_sing_p*z_sing_n)){
//				System.out.println("z neg");
//			}
//			if(xy_sign == Math.signum(x_sing_n*y_sing_n) && xz_sign == Math.signum(x_sing_n*z_sing_n) && yz_sign == Math.signum(y_sing_n*z_sing_n)){
//				System.out.println("all neg");
//			}


		}



		rot.normalize();
		return rot;
	}
	public Quat getRotationFromMat1b(Mat4 mat4, Vec3 scale, int i) {
		Quat rot = new Quat();
//		float xx = rot.x * rot.x * 2;
//		float xy = rot.x * rot.y * 2;
//		float xz = rot.x * rot.z * 2;
//		float yy = rot.y * rot.y * 2;
//		float yz = rot.y * rot.z * 2;
//		float zz = rot.z * rot.z * 2;
//		float wx = rot.w * rot.x * 2;
//		float wy = rot.w * rot.y * 2;
//		float wz = rot.w * rot.z * 2;
//
//		m00 = (1 - (yy + zz))   * scale.x;
//		m01 = (xy + wz)         * scale.x;
//		m02 = (xz - wy)         * scale.x;
//		m10 = (xy - wz)         * scale.y;
//		m11 = (1 - (xx + zz))   * scale.y;
//		m12 = (yz + wx)         * scale.y;
//		m20 = (xz + wy)         * scale.z;
//		m21 = (yz - wx)         * scale.z;
//		m22 = (1 - (xx + yy))   * scale.z;




		float t00 = mat4.m00 / scale.x;
		float t01 = mat4.m01 / scale.x;
		float t02 = mat4.m02 / scale.x;
		float t10 = mat4.m10 / scale.y;
		float t11 = mat4.m11 / scale.y;
		float t12 = mat4.m12 / scale.y;
		float t20 = mat4.m20 / scale.z;
		float t21 = mat4.m21 / scale.z;
		float t22 = mat4.m22 / scale.z;

		if(i == 200){
			debugPrint("t00: \t" + t00, i);
			debugPrint("t01: \t" + t01, i);
			debugPrint("t02: \t" + t02, i);
			debugPrint("t10: \t" + t10, i);
			debugPrint("t11: \t" + t11, i);
			debugPrint("t12: \t" + t12, i);
			debugPrint("t20: \t" + t20, i);
			debugPrint("t21: \t" + t21, i);
			debugPrint("t22: \t" + t22, i);
		}
//
//		t00 = 1 - yy - zz  ;
//		t01 = xy + wz        ;
//		t02 = xz - wy        ;
//		t10 = xy - wz        ;
//		t11 = 1 - xx - zz ;
//		t12 = yz + wx        ;
//		t20 = xz + wy        ;
//		t21 = yz - wx        ;
//		t22 = 1 - xx - yy  ;
//
//		zz = t22 - t11 + yy;
//		xx = 1 - t22 - yy;
//
//		yy = 1 - t00 - zz  ;

//		t00 = 1 - yy*2 - zz*2  ;
//		t01 = xy*2 + wz*2        ;
//		t02 = xz*2 - wy*2        ;
//		t10 = xy*2 - wz*2        ;
//		t11 = 1 - xx*2 - zz*2 ;
//		t12 = yz*2 + wx*2        ;
//		t20 = xz*2 + wy*2        ;
//		t21 = yz*2 - wx*2        ;
//		t22 = 1 - xx*2 - yy*2  ;

//		t00 = (.5 - yy - zz)*2  ;
//		t01 = (xy + wz)*2        ;
//		t02 = (xz - wy)*2        ;
//		t10 = (xy - wz)*2        ;
//		t11 = (.5 - xx - zz)*2 ;
//		t12 = (yz + wx)*2        ;
//		t20 = (xz + wy)*2        ;
//		t21 = (yz - wx)*2        ;
//		t22 = (.5 - xx - yy)*2  ;

		float xy = (t01+t10)/2.0f;
		float xz = (t20+t02)/2.0f;
		float yz = (t12 + t21)/2.0f;
		float wx = (t12 - t21)/2.0f;
		float wy = (t20 - t02)/2.0f;
		float wz = (t01 - t10)/2.0f;

		float zz = (t22 - t11 + 1 - t00)/2;
		float xx = 1 - t11 - zz;
//		float yy = 1 - t00 - zz;
		float yy = t11 + zz - t22;


		rot.x = (float) Math.sqrt(xx/2);
		rot.y = (float) Math.sqrt(yy/2);
		rot.z = (float) Math.sqrt(zz/2);

		float xy_sign = Math.signum(xy);
		float xz_sign = Math.signum(xz);
		float yz_sign = Math.signum(yz);
		float wx_sign = Math.signum(wx);
		float wy_sign = Math.signum(wy);
		float wz_sign = Math.signum(wz);


		debugPrint("rot: \t" + rot, i);
		if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
			debugPrint("all pos, w pos/all neg, w neg", i);
		} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == 1.0f){
			debugPrint("x neg, w pos/y neg, z neg, w neg", i);
//				rot.x = -rot.x;
			rot.y = -rot.y;
			rot.z = -rot.z;
		} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
			debugPrint("x neg, y neg, w pos/z neg, w neg", i);
			rot.x = -rot.x;
			rot.y = -rot.y;
		} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == -1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
			debugPrint("x neg, z neg, w pos/y neg, w neg", i);

			rot.x = -rot.x;
			rot.z = -rot.z;
//				rot.y = -rot.y;
		} else if(xy_sign == -1.0f && xz_sign == 1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == 1.0f){
			debugPrint("y neg, w pos/x neg, z neg, w neg", i);

			rot.y = -rot.y;
		} else if(xy_sign == -1.0f && xz_sign == -1.0f && yz_sign == 1.0f && wx_sign == 1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
			debugPrint("y neg, z neg, w pos/x neg, w neg", i);

			rot.y = -rot.y;
			rot.z = -rot.z;
		} else if(xy_sign == 1.0f && xz_sign == -1.0f && yz_sign == -1.0f && wx_sign == 1.0f && wy_sign == 1.0f && wz_sign == -1.0f){
			debugPrint("z neg, w pos/x neg, y neg, w neg", i);
			rot.z = -rot.z;
		} else if(xy_sign == 1.0f && xz_sign == 1.0f && yz_sign == 1.0f && wx_sign == -1.0f && wy_sign == -1.0f && wz_sign == -1.0f){
			debugPrint("all neg, w pos/all pos, w neg", i);
		}

		rot.w = (t01 - rot.x*rot.y)/rot.z;

		if(i == 200){
			debugPrint("rot: \t" + rot, i);
		}

		rot.normalize();
		return rot;
	}
	public Quat getRotationFromMat1(Mat4 mat4, Vec3 scale, int i) {
		Quat rot = new Quat();
//		float xx = rot.x * rot.x * 2;
//		float xy = rot.x * rot.y * 2;
//		float xz = rot.x * rot.z * 2;
//		float yy = rot.y * rot.y * 2;
//		float yz = rot.y * rot.z * 2;
//		float zz = rot.z * rot.z * 2;
//		float wx = rot.w * rot.x * 2;
//		float wy = rot.w * rot.y * 2;
//		float wz = rot.w * rot.z * 2;
//
//		m00 = (1 - (yy + zz))   * scale.x;
//		m01 = (xy + wz)         * scale.x;
//		m02 = (xz - wy)         * scale.x;
//		m10 = (xy - wz)         * scale.y;
//		m11 = (1 - (xx + zz))   * scale.y;
//		m12 = (yz + wx)         * scale.y;
//		m20 = (xz + wy)         * scale.z;
//		m21 = (yz - wx)         * scale.z;
//		m22 = (1 - (xx + yy))   * scale.z;




		float t00 = mat4.m00 / scale.x;
		float t01 = mat4.m01 / scale.x;
		float t02 = mat4.m02 / scale.x;
		float t10 = mat4.m10 / scale.y;
		float t11 = mat4.m11 / scale.y;
		float t12 = mat4.m12 / scale.y;
		float t20 = mat4.m20 / scale.z;
		float t21 = mat4.m21 / scale.z;
		float t22 = mat4.m22 / scale.z;

		if(i == 200){
			debugPrint("t00: \t" + t00, i);
			debugPrint("t01: \t" + t01, i);
			debugPrint("t02: \t" + t02, i);
			debugPrint("t10: \t" + t10, i);
			debugPrint("t11: \t" + t11, i);
			debugPrint("t12: \t" + t12, i);
			debugPrint("t20: \t" + t20, i);
			debugPrint("t21: \t" + t21, i);
			debugPrint("t22: \t" + t22, i);
		}
//
//		t00 = 1 - yy - zz  ;
//		t01 = xy + wz        ;
//		t02 = xz - wy        ;
//		t10 = xy - wz        ;
//		t11 = 1 - xx - zz ;
//		t12 = yz + wx        ;
//		t20 = xz + wy        ;
//		t21 = yz - wx        ;
//		t22 = 1 - xx - yy  ;
//
//		zz = t22 - t11 + yy;
//		xx = 1 - t22 - yy;
//
//		yy = 1 - t00 - zz  ;

//		t00 = 1 - yy*2 - zz*2  ;
//		t01 = xy*2 + wz*2        ;
//		t02 = xz*2 - wy*2        ;
//		t10 = xy*2 - wz*2        ;
//		t11 = 1 - xx*2 - zz*2 ;
//		t12 = yz*2 + wx*2        ;
//		t20 = xz*2 + wy*2        ;
//		t21 = yz*2 - wx*2        ;
//		t22 = 1 - xx*2 - yy*2  ;

//		t00 = (.5 - yy - zz)*2  ;
//		t01 = (xy + wz)*2        ;
//		t02 = (xz - wy)*2        ;
//		t10 = (xy - wz)*2        ;
//		t11 = (.5 - xx - zz)*2 ;
//		t12 = (yz + wx)*2        ;
//		t20 = (xz + wy)*2        ;
//		t21 = (yz - wx)*2        ;
//		t22 = (.5 - xx - yy)*2  ;



		float zz = (t22 - t11 + 1 - t00)/2;
		float xx = 1 - t11 - zz;
//		float yy = 1 - t00 - zz;
		float yy = t11 + zz - t22;


		rot.x = (float) Math.sqrt(xx/2);
		rot.y = (float) Math.sqrt(yy/2);
		rot.z = (float) Math.sqrt(zz/2);
		rot.w = (t01 - rot.x*rot.y)/rot.z;

		if(i == 200){
			debugPrint("rot: \t" + rot, i);
		}

		rot.normalize();
		return rot;
	}

	private void tempSolveRotMatrix(){
		//https://en.wikipedia.org/wiki/Cubic_equation#General_cubic_formula
		//https://math.stackexchange.com/questions/893984/conversion-of-rotation-matrix-to-quaternion
		//https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
		//https://math.stackexchange.com/questions/237369/given-this-transformation-matrix-how-do-i-decompose-it-into-translation-rotati/3554913

//		//(m00 - k)*((m11 - k)*(m22 - k)-m12*m21)- m01*(m10*(m22-k)-m12*m20)+m02*(m10*m21-(m11-k)*m20)
//
////(m00 - k)*((m11 - k)*(m22 - k)-u0)- m01*(m10*(m22-k)-u1)+m02*(u2-(m11-k)*m20)
//		float u0 = m12*m21;
//		float u1 = m12*m20;
//		float u2 = m10*m21;
//
////((m00 - k)*(m11 - k)*(m22 - k) - (m00 - k)*u0) - (m01*m10*(m22-k)-m01*u1)+(m02*u2-m02*(m11-k)*m20)
//
////(m00 - k)*(m11 - k)*(m22 - k) - m00*u0 + k*u0 - m01*m10*m22 + m01*m10*k + m02*m20*k - m02*m11*m20 + m01*u1 + m02*u2
//
////(m00 - k)*(m11 - k)*(m22 - k) + k*u0 + m01*m10*k + m02*k*m20 + m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20
////(m00*m11*(m22 - k) - k*m11*(m22 - k) - m00*k*(m22 - k) + k*k*(m22 - k)) + k*u0 + m01*m10*k + m02*k*m20 + m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20
//
////m00*m11*m22 - m00*m11*k - m11*m22*k + m11*k*k - m00*k*m22 + m00*k*k + k*k*m22 - k*k*k + k*u0 + m01*m10*k + m02*k*m20 + m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20
//
//// m01*m10*k + m02*m20*k + k*u0 - m00*m11*k - m11*m22*k + m11*k*k - m00*k*m22 + m00*k*k + k*k*m22 - k*k*k + m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20 + m00*m11*m22
//
//
//// (m01*m10 + m02*m20 + u0 - m00*m11 - m11*m22 - m00*m22)*k + (m11 + m00 + m22)*k*k - k*k*k + m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20 + m00*m11*m22
//
//		float uu1 = m01*m10 + m02*m20 + u0 - m00*m11 - m11*m22 - m00*m22;
//		float uu2 = m11 + m00 + m22;
//		float uum = m01*u1 + m02*u2 - m00*u0 - m01*m10*m22 - m02*m11*m20 + m00*m11*m22;
//
//
//// (uu1)*k + (uu2)*k*k - k*k*k + uum
//
//// (uu1)*k + (uu2)*k*k - k*k*k + uum = 0
//
//// ((uu1) + (uu2)*k - k*k)*k  = -uum
//// ((uu1) + ((uu2) - k)*k)*k  = -uum
//
//// (uu1)*k + uum + (uu2)*k*k - k*k*k = 0
//// (uu1)*k + uum + ((uu2) - k)*k*k = 0
//// ((uu1)*k + uum) + ((uu2) - k)*k*k = 0
//
//// uu1*k + uu2*k*k - k*k*k + uum = 0
//
//		d_0 = uu2*uu2 - (-1*uu1)
//		d_1 = 2*uu2*uu2*uu2 - (9*-1*uu2*uu1) + (27 * (-1 * -1) * uum

	}

	private void printKFUpdate(int i, Animation animation, String type) {
		if (i != 0 && i % 5000 == 0) {
			System.out.println("( "+ type + " - " + animation.getName() + " ) KF: " + i);
		}
	}

	private Quat getWorldRotDif(RenderModel renderModel, Animation animation, RenderNode2 orgRenderNode, RenderNode2 tempRenderNode, Vec3 point, int i) {
		renderModel.updateNodes(true, false, false);
		Vec3 vTemp = new Vec3(point);
		Vec3 vOrg = new Vec3(point);

		Vec3 orgWorldLoc = orgRenderNode.getRenderPivot();
		Vec3 tmpWorldLoc = tempRenderNode.getRenderPivot();

		Vec3 orgChWorldLoc = vOrg.transform(orgRenderNode.getWorldMatrix());
		Vec3 tmpChWorldLoc = vTemp.transform(tempRenderNode.getWorldMatrix());

		if(animation.getName().equals("Death") && i == 900){
			System.out.println(animation.getName() + " - ittr: " + i + ",\tlocDiffX: " + orgChWorldLoc.distance(tmpChWorldLoc) + ",\trotDifX: " + orgChWorldLoc.degAngleTo(tmpChWorldLoc));
//						System.out.println(animation.getName() + " - ittr: " + k + " - worldRotDifY-u: " + worldRotDifY);
//						System.out.println(animation.getName() + " - ittr: " + k + " - worldRotDifZ-u: " + worldRotDifZ );
		}



		Mat4 tempMat = new Mat4().set(renderModel.getRenderNode(tempNewObject).getWorldMatrix()).invert();


//		Vec3 orgDirVec = orgChWorldLoc.sub(orgWorldLoc).transform(tempMat).normalize();
//		Vec3 tmpDirVec = tmpChWorldLoc.sub(tmpWorldLoc).transform(tempMat).normalize();
		Vec3 orgDirVec = orgChWorldLoc.sub(orgWorldLoc).normalize();
		Vec3 tmpDirVec = tmpChWorldLoc.sub(tmpWorldLoc).normalize();


		Vec3 middle = tmpDirVec.add(orgDirVec).normalize();

		Quat worldRotDif = orgDirVec.getQuatTo(middle);
//		worldRotDif.transform(tempMat);
//		worldRotDif.normalize();

		Quat value = newRotationFlag.getEntryAt(animation, i).getValue();
		newRotationFlag.addEntry(i, value.mul(worldRotDif), animation);
		return worldRotDif;
	}


	private void printStuff_v3(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, List<Animation> anims) {
		System.out.println("done calculating");
		List<Animation> anims1 = modelHandler.getModel().getAnims().stream().filter(a -> a.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase("Stand Work Gold")).collect(Collectors.toList());
		timeEnvironment.setSequence(anims1.get(0));

		checkAndPrintAt(renderModel, timeEnvironment, 1);

//		checkAndPrintAt(renderModel, timeEnvironment, 120);
		checkAndPrintAt(renderModel, timeEnvironment, 90);

		checkAndPrintAt(renderModel, timeEnvironment, 500);

//		Quat orgChWorldRot1 = renderModel.getRenderNode(tempchild1x).getWorldRotation();
//		Quat tmpChWorldRot1 = renderModel.getRenderNode(tempchild2x).getWorldRotation();
//		System.out.println("orgChWorldRot: " + orgChWorldRot1 + ", tmpChWorldRot: " + tmpChWorldRot1);
//		Vec3 orgChWorldLoc1x = renderModel.getRenderNode(tempchild1x).getRenderPivot();
//		Vec3 tmpChWorldLoc1x = renderModel.getRenderNode(tempchild2x).getRenderPivot();
//		System.out.println("orgChWorldLoc1x: " + orgChWorldLoc1x + ", tmpChWorldLoc1x: " + tmpChWorldLoc1x);
//
//		Vec3 orgChWorldLoc1y = renderModel.getRenderNode(tempchild1y).getRenderPivot();
//		Vec3 tmpChWorldLoc1y = renderModel.getRenderNode(tempchild2y).getRenderPivot();
//		System.out.println("orgChWorldLoc1y: " + orgChWorldLoc1y + ", tmpChWorldLoc1y: " + tmpChWorldLoc1y);
//
//		Vec3 orgChWorldLoc1z = renderModel.getRenderNode(tempchild1z).getRenderPivot();
//		Vec3 tmpChWorldLoc1z = renderModel.getRenderNode(tempchild2z).getRenderPivot();
//		System.out.println("orgChWorldLoc1z: " + orgChWorldLoc1z + ", tmpChWorldLoc1z: " + tmpChWorldLoc1z);
	}

	private void checkAndPrintAt(RenderModel renderModel, TimeEnvironmentImpl timeEnvironment, int i) {
		System.out.println("KF = " + i);
		timeEnvironment.setAnimationTime(i);
		renderModel.updateNodes(true, false, false);

		System.out.println("orgRendPiv: " + renderModel.getRenderNode(objToRebind).getRenderPivot() + ", tempRendPiv: " + renderModel.getRenderNode(tempNewObject).getRenderPivot());
		System.out.println("orgWorldLoc: " + renderModel.getRenderNode(objToRebind).getWorldLocation() + ", tempWorldLoc: " + renderModel.getRenderNode(tempNewObject).getWorldLocation());


		Quat orgWorldRot = renderModel.getRenderNode(objToRebind).getWorldRotation();
		Quat tempWorldRot = renderModel.getRenderNode(tempNewObject).getWorldRotation();
		System.out.println("orgWorldRot: " + orgWorldRot + ", tempWorldRot: " + tempWorldRot);
	}

	@Override
	public UndoAction undo() {
		objToRebind.setParent(oldParent);
		objToRebind.add(orgTranslationFlag);
		objToRebind.add(orgScalingFlag);
		objToRebind.add(orgRotationFlag);

		ModelStructureChangeListener.changeListener.nodesUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		objToRebind.setParent(newParent);
		objToRebind.add(newTranslationFlag);
		objToRebind.add(newScalingFlag);
		objToRebind.add(newRotationFlag);

		ModelStructureChangeListener.changeListener.nodesUpdated();
		return this;
	}


	@Override
	public String actionName() {
		return "Baked and changed Parent";
	}
}
