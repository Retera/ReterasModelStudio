package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class TwilacsHandWizard {
	private EditableModel handModel;
	private EditableModel recModel;
	private ModelHandler recModelHandler;

	public TwilacsHandWizard(EditableModel handModel, ModelHandler recModelHandler) {
		this.handModel = handModel;
		this.recModelHandler = recModelHandler;
		this.recModel = recModelHandler.getModel();



		List<UndoAction> actions = new ArrayList<>();
		Map<String, Bone> nameToNode = getBoneNameMap(recModel);

		String side_L = "left";
		HandBones hm_L = new HandBones(handModel, side_L);
		HandBones rm_L = new HandBones(recModel, side_L);
		LocRotScale leftHandAdj = getHandAdj(hm_L, rm_L);
		Set<Geoset> leftHandGeos = getNewGeosets(hm_L.allHandBones);
		for (Geoset geoset : leftHandGeos) {
			translateVertices(geoset, nameToNode, leftHandAdj, rm_L);
		}


		String side_R = "right";
		HandBones hm_R = new HandBones(handModel, side_R);
		HandBones rm_R = new HandBones(recModel, side_R);
		LocRotScale rightHandAdj = getHandAdj(hm_R, rm_R);
		Set<Geoset> rightHandGeos = getNewGeosets(hm_R.allHandBones);
		for (Geoset geoset : rightHandGeos) {
			translateVertices(geoset, nameToNode, rightHandAdj, rm_R);
		}


		for (Geoset geoset : leftHandGeos) {
			actions.add(new AddGeosetAction(geoset, recModel, null));
		}
		for (Geoset geoset : rightHandGeos) {
			actions.add(new AddGeosetAction(geoset, recModel, null));
		}

		recModelHandler.getUndoManager().pushAction(new CompoundAction("Splice Hand Action", actions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
	}


	private void translateVertices(Geoset geoset, Map<String, Bone> nameToNode, LocRotScale handAdj, HandBones rm) {
		Vec3 pivotPoint = rm.hand.getPivotPoint();
		for (GeosetVertex vertex : geoset.getVertices()) {
			vertex.add(handAdj.loc).scale(pivotPoint, handAdj.scale).rotate(pivotPoint, handAdj.rot);
			if (vertex.getSkinBones() != null) {
				replaceHDBones((Bone) rm.hand, nameToNode, vertex);
			} else {
				replaceSDBones((Bone) rm.hand, nameToNode, vertex);
			}
		}
	}


	protected Set<Geoset> getNewGeosets(Set<Bone> selectedBones) {
		Set<Bone> extraBones = new HashSet<>();
		Set<Geoset> newGeosets = getCopiedGeosets(selectedBones, extraBones);

		for (Geoset newGeoset : newGeosets) {
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			vertexSet.addAll(getVertexSet(extraBones, newGeoset));

			Set<Triangle> trianglesToRemove = new HashSet<>();
			Set<GeosetVertex> verticesToCull = new HashSet<>();
			for (Triangle triangle : newGeoset.getTriangles()) {
				List<GeosetVertex> triVerts = Arrays.asList(triangle.getVerts());
				if (!vertexSet.containsAll(triVerts)) {
					trianglesToRemove.add(triangle);
					verticesToCull.addAll(triVerts);
				}
			}

			verticesToCull.removeAll(vertexSet);
			newGeoset.remove(verticesToCull);

			trianglesToRemove.forEach(triangle -> newGeoset.remove(triangle.removeFromVerts()));
		}
		return newGeosets;
	}



	private Set<Geoset> getCopiedGeosets(Set<Bone> selectedBones, Set<Bone> extraBones) {
		Set<Geoset> newGeosets = new HashSet<>();
		for (Geoset donGeoset : handModel.getGeosets()) {
			Geoset newGeoset = donGeoset.deepCopy();
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if (!vertexSet.isEmpty()) {
				extraBones.addAll(getExtraBones(vertexSet));

				newGeoset.setParentModel(recModel);
				newGeosets.add(newGeoset);
			}
		}
		return newGeosets;
	}

	private Set<GeosetVertex> getVertexSet(Set<Bone> selectedBones, Geoset newGeoset) {
		Set<GeosetVertex> vertexSet = new HashSet<>();
		for (Bone bone : selectedBones) {
			List<GeosetVertex> vertices = newGeoset.getBoneMap().get(bone);
			if (vertices != null) {
				vertexSet.addAll(vertices);
			}
		}
		return vertexSet;
	}

	private Set<Bone> getExtraBones(Set<GeosetVertex> vertexSet) {
		Set<Bone> extraBones = new HashSet<>();
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getWeight()>0 && skinBone.getBone() != null) {
						extraBones.add(skinBone.getBone());
					}
				}
			} else {
				extraBones.addAll(vertex.getBones());
			}
		}
		return extraBones;
	}

	private Map<String, Bone> getBoneNameMap(EditableModel model) {
		Map<String, Bone> nameToNode = new HashMap<>();
		for (Bone bone : model.getBones()) {
			nameToNode.put(bone.getName(), bone);
		}
		return nameToNode;
	}

	private LocRotScale getHandAdj(HandBones hm, HandBones rm) {
//		IdObject hm_hand = handModel.getObject("bone_hand_left");
//		IdObject hm_tmb = handModel.getObject("L_tmb_01_bind_jnt");
//		IdObject hm_mid = handModel.getObject("L_mid_finger_01_bind_jnt");
//
//		IdObject rm_hand = recModel.getObject("bone_hand_left");
//		IdObject rm_tmb = recModel.getObject("L_tmb_01_bind_jnt");
//		IdObject rm_mid = recModel.getObject("L_mid_finger_01_bind_jnt");

//		HandBones hm = new HandBones(handModel, side);
//		HandBones rm = new HandBones(recModel, side);

		Vec3 locDiff = new Vec3(rm.hand.getPivotPoint()).sub(hm.hand.getPivotPoint());

		float hm_dist = hm.hand.getPivotPoint().distance(hm.mid.getPivotPoint());
		float rm_dist = rm.hand.getPivotPoint().distance(rm.mid.getPivotPoint());
		float scaleDiff = rm_dist/hm_dist;

		Vec3 hm_ind_vec = new Vec3(hm.ind.getPivotPoint()).sub(hm.hand.getPivotPoint()).normalize();
		Vec3 hm_mid_vec = new Vec3(hm.mid.getPivotPoint()).sub(hm.hand.getPivotPoint()).normalize();

		Vec3 rm_ind_vec = new Vec3(rm.ind.getPivotPoint()).sub(rm.hand.getPivotPoint()).normalize();
		Vec3 rm_mid_vec = new Vec3(rm.mid.getPivotPoint()).sub(rm.hand.getPivotPoint()).normalize();

		Quat ind_rot = new Quat().setAsRotBetween(rm_ind_vec, hm_ind_vec);

		Vec3 tempVec = new Vec3(hm_mid_vec);
		tempVec.rotate(Vec3.ZERO, ind_rot);

		Quat mid_rot = new Quat().setAsRotBetween(rm_mid_vec, tempVec);

		Quat totRot = new Quat(ind_rot).mul(mid_rot);

//		Vec3 hm_cross = new Vec3(hm_tmb_vec).cross(hm_mid_vec);
//		Vec3 rm_cross = new Vec3(rm_tmb_vec).cross(rm_mid_vec);
//		Quat totRot = new Quat().setAsRotBetween(rm_cross, hm_cross);

		return new LocRotScale(locDiff, totRot, scaleDiff);
//		return new LocRotScale(locDiff, new Quat(), 1);
	}
	private LocRotScale getHandAdjTmb(HandBones hm, HandBones rm) {
//		IdObject hm_hand = handModel.getObject("bone_hand_left");
//		IdObject hm_tmb = handModel.getObject("L_tmb_01_bind_jnt");
//		IdObject hm_mid = handModel.getObject("L_mid_finger_01_bind_jnt");
//
//		IdObject rm_hand = recModel.getObject("bone_hand_left");
//		IdObject rm_tmb = recModel.getObject("L_tmb_01_bind_jnt");
//		IdObject rm_mid = recModel.getObject("L_mid_finger_01_bind_jnt");

//		HandBones hm = new HandBones(handModel, side);
//		HandBones rm = new HandBones(recModel, side);

		Vec3 locDiff = new Vec3(rm.hand.getPivotPoint()).sub(hm.hand.getPivotPoint());

		float hm_dist = hm.hand.getPivotPoint().distance(hm.mid.getPivotPoint());
		float rm_dist = rm.hand.getPivotPoint().distance(rm.mid.getPivotPoint());
//		float scaleDiff = rm_dist/hm_dist;
		float scaleDiff = 1;

		Vec3 hm_tmb_vec = new Vec3(hm.tmb.getPivotPoint()).sub(hm.hand.getPivotPoint()).normalize();
		Vec3 hm_mid_vec = new Vec3(hm.mid.getPivotPoint()).sub(hm.hand.getPivotPoint()).normalize();

		Vec3 rm_tmb_vec = new Vec3(rm.tmb.getPivotPoint()).sub(rm.hand.getPivotPoint()).normalize();
		Vec3 rm_mid_vec = new Vec3(rm.mid.getPivotPoint()).sub(rm.hand.getPivotPoint()).normalize();

		Quat tmb_rot = new Quat().setAsRotBetween(rm_tmb_vec, hm_tmb_vec);

		Vec3 tempVec = new Vec3(hm_mid_vec);
		tempVec.rotate(Vec3.ZERO, tmb_rot);

		Quat mid_rot = new Quat().setAsRotBetween(rm_mid_vec, tempVec);

		Quat totRot = new Quat(tmb_rot).mul(mid_rot);

//		Vec3 hm_cross = new Vec3(hm_tmb_vec).cross(hm_mid_vec);
//		Vec3 rm_cross = new Vec3(rm_tmb_vec).cross(rm_mid_vec);
//		Quat totRot = new Quat().setAsRotBetween(rm_cross, hm_cross);

		return new LocRotScale(locDiff, totRot, scaleDiff);
	}


	private void replaceSDBones(Bone fallBackBone, Map<String, Bone> nameToNode, GeosetVertex gv) {
		List<Bone> bones = gv.getMatrix().getBones();
		for (int i = 0; i < bones.size(); i++) {
			IdObject bone = bones.get(i);
			if (bone != null) {
				final String boneName = bone.getName();
				Bone replacement = nameToNode.get(boneName);
				int upwardDepth = 0;
				while ((replacement == null) && (bone != null)) {
					bone = bone.getParent();
					upwardDepth++;
					if (bone != null) {
						replacement = nameToNode.get(bone.getName());
					} else {
						replacement = null;
					}
				}
				if (replacement == null) {
					replacement = fallBackBone;
//							throw new IllegalStateException("failed to replace: " + boneName);
				} else {
					while ((0 < upwardDepth) && (replacement.getChildrenNodes().size() == 1)
							&& (replacement.getChildrenNodes().get(0) instanceof Bone)) {
						replacement = (Bone) replacement.getChildrenNodes().get(0);
						upwardDepth--;
					}
				}
				gv.setBone(i, replacement);

			}
		}
	}

	private void replaceHDBones(Bone fallBackBone, Map<String, Bone> nameToNode, GeosetVertex gv) {
		for (int i = 0; i < gv.getSkinBones().length; i++) {
			IdObject bone = gv.getSkinBones()[i].getBone();
			if (bone != null) {
				final String boneName = bone.getName();
				Bone replacement = nameToNode.get(boneName);
				int upwardDepth = 0;
				while ((replacement == null) && (bone != null)) {
					bone = bone.getParent();
					upwardDepth++;
					if (bone != null) {
						replacement = nameToNode.get(bone.getName());
					} else {
						replacement = null;
					}
				}
				if (replacement == null) {
					replacement = fallBackBone;
//							throw new IllegalStateException("failed to replace: " + boneName);
				} else {
					while ((0 < upwardDepth) && (replacement.getChildrenNodes().size() == 1)
							&& (replacement.getChildrenNodes().get(0) instanceof Bone)) {
						replacement = (Bone) replacement.getChildrenNodes().get(0);
						upwardDepth--;
					}
				}
				gv.getSkinBones()[i].setBone(replacement);

			}
		}
	}

	private static class LocRotScale {
		Vec3 loc;
		Quat rot;
		float scale;
		LocRotScale(Vec3 loc, Quat rot, float scale) {
			this.loc = loc;
			this.rot = rot;
			this.scale = scale;
		}
	}

	private static class HandBones {
		IdObject hand;
		IdObject tmb;
		IdObject ind;
		IdObject mid;
		Set<Bone> allHandBones;

		HandBones(EditableModel model, String side) {
			hand = model.getObject("bone_hand_" + side);
			tmb = model.getObject(side.toUpperCase(Locale.ROOT).charAt(0) + "_tmb_01_bind_jnt");
			ind = model.getObject(side.toUpperCase(Locale.ROOT).charAt(0) + "_ind_finger_01_bind_jnt");
			mid = model.getObject(side.toUpperCase(Locale.ROOT).charAt(0) + "_mid_finger_01_bind_jnt");

			allHandBones = new HashSet<>();
			collectAllChildren(hand, allHandBones);
		}



		private void collectAllChildren(IdObject parent, Set<Bone> collected) {
			if (parent instanceof Bone && !(parent instanceof Helper)) {
				collected.add((Bone) parent);
			}
			for (IdObject child : parent.getChildrenNodes()) {
				collectAllChildren(child, collected);
			}
		}
	}
}
