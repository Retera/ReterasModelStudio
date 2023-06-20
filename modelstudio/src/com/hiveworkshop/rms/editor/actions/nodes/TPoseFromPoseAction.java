package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class TPoseFromPoseAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Map<IdObject, Vec3> nodeToNewPivot = new HashMap<>();
	private final Map<IdObject, Vec3> nodeToOldPivot = new HashMap<>();
	private final Map<IdObject, Vec3AnimFlag> nodeToNewTrans = new HashMap<>();
	private final Map<IdObject, Vec3AnimFlag> nodeToOldTrans = new HashMap<>();
	private final Map<IdObject, QuatAnimFlag> nodeToNewRot = new HashMap<>();
	private final Map<IdObject, QuatAnimFlag> nodeToOldRot = new HashMap<>();
	private final BakeGeometryTransformAction bakeGeometryTransformAction;

	private final Vec3 tempDelta = new Vec3();
	private final Quat quat = new Quat();

	public TPoseFromPoseAction(EditableModel model, Sequence sequence, int time, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		System.out.println("seq: " + sequence.getName() + ", at " + time);

		List<IdObject> idObjects = collectSortedNodes(model.getIdObjects());
		Collections.reverse(idObjects);

		Map<IdObject, Mat4> nodeTransforms = new LinkedHashMap<>();
		Vec3 translation = new Vec3();
		Vec3 axis = new Vec3();
		Quat rotation = new Quat();

		for (IdObject node : idObjects) {
			nodeToOldPivot.put(node, new Vec3(node.getPivotPoint()));
			nodeToNewPivot.put(node, new Vec3(node.getPivotPoint()));

			AnimFlag<Vec3> transFlag = node.getTranslationFlag();
			if (transFlag != null) {
				nodeToOldTrans.put(node, (Vec3AnimFlag) transFlag);
				AnimFlag<Vec3> vec3AnimFlag = transFlag.deepCopy();

				nodeToNewTrans.put(node, (Vec3AnimFlag) vec3AnimFlag);
				translation.set(vec3AnimFlag.interpolateAt(sequence, time));
				moveNode(node, translation);
			} else {
				translation.set(Vec3.ZERO);
			}

			AnimFlag<Quat> rotFlag = node.getRotationFlag();
			if (rotFlag != null) {
				nodeToOldRot.put(node, (QuatAnimFlag) rotFlag);
				AnimFlag<Quat> quatAnimFlag = rotFlag.deepCopy();
				nodeToNewRot.put(node, (QuatAnimFlag) quatAnimFlag);

				rotation.set(quatAnimFlag.interpolateAt(sequence, time));
				float angle = rotation.getAxisAngle();
				axis.setAsAxis(rotation);
				rotateNode(node, angle, axis);
			} else {
				rotation.set(Quat.IDENTITY);
			}

			nodeTransforms.put(node, new Mat4().fromRotationTranslationScaleOrigin(rotation, translation, Vec3.ONE, node.getPivotPoint()));
		}

		Collections.reverse(idObjects);

		Mat4 tempMat = new Mat4();
		for (IdObject node : idObjects) {
			Mat4 patentMat = nodeTransforms.get(node);
			for (IdObject child : node.getChildrenNodes()) {
				Mat4 childMat = nodeTransforms.get(child);
				tempMat.set(patentMat).mul(childMat);
				childMat.set(tempMat);
			}
		}

		bakeGeometryTransformAction = new BakeGeometryTransformAction(idObjects, model.getGeosets(), Mat4.IDENTITY, null);
		bakeGeometryTransformAction.setTransforms(nodeTransforms);

	}

	private List<IdObject> collectSortedNodes(Collection<IdObject> idObjects) {
		List<IdObject> parentNodes = idObjects.stream()
				.filter(idObject -> idObject.getParent() == null)
				.collect(Collectors.toList());
		List<IdObject> sorted = new ArrayList<>();
		for (IdObject node : parentNodes) {
			collectSortedChildNodes(node, sorted);
		}
		return sorted;
	}
	private void collectSortedChildNodes(IdObject node, List<IdObject> sorted){
		sorted.add(node);
		for (IdObject child : node.getChildrenNodes()) {
			collectSortedChildNodes(child, sorted);
		}
	}


	@Override
	public UndoAction undo() {
		for (IdObject idObject : nodeToOldTrans.keySet()) {
			idObject.add(nodeToOldTrans.get(idObject));
		}
		for (IdObject idObject : nodeToOldRot.keySet()){
			idObject.add(nodeToOldRot.get(idObject));
		}
		for (IdObject idObject : nodeToOldPivot.keySet()) {
			idObject.setPivotPoint(nodeToOldPivot.get(idObject));
		}

		bakeGeometryTransformAction.undo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (IdObject idObject : nodeToNewTrans.keySet()) {
			idObject.add(nodeToNewTrans.get(idObject));
		}
		for (IdObject idObject : nodeToNewRot.keySet()) {
			idObject.add(nodeToNewRot.get(idObject));
		}
		for (IdObject idObject : nodeToNewPivot.keySet()) {
			idObject.setPivotPoint(nodeToNewPivot.get(idObject));
		}

		bakeGeometryTransformAction.redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}


	@Override
	public String actionName() {
		return "Bake T-Pose";
	}


	private void moveNode(IdObject node, Vec3 delta){
		nodeToNewPivot.get(node).add(delta);
		for (IdObject child : node.getChildrenNodes()) {
			moveChildren(child, delta);
		}
		tempDelta.set(Vec3.ZERO).sub(delta);

		AnimFlag<Vec3> newTranslation = nodeToNewTrans.get(node);
		if (newTranslation != null) {
			moveTranslations(tempDelta, newTranslation);
		}
	}

	private void moveChildren(IdObject child, Vec3 delta){
		nodeToNewPivot.get(child).add(delta);
		for (IdObject grandchild : child.getChildrenNodes()) {
			moveChildren(grandchild,delta);
		}
	}

	private void rotateNode(IdObject node, double radians, Vec3 axis) {
		Vec3 newPivot = nodeToNewPivot.get(node);
		quat.setFromAxisAngle(axis, (float) radians);
		for (IdObject child : node.getChildrenNodes()) {
			rotChildren(child, newPivot, quat);
		}

		AnimFlag<Quat> newRotation = nodeToNewRot.get(node);

		quat.setFromAxisAngle(axis, (float) -radians);
		if (newRotation != null) {
			rotRotationsQuatMul(quat, newRotation);
		}
	}

	private void rotChildren(IdObject child, Vec3 center, Quat quat) {
		nodeToNewPivot.get(child).rotate(center, quat);

		AnimFlag<Vec3> newChildTranslation = nodeToNewTrans.get(child);
		if (newChildTranslation != null) {
			rotTranslation(quat, newChildTranslation);
		}
		AnimFlag<Quat> newChildRotation = nodeToNewRot.get(child);
		if (newChildRotation != null) {
			rotRotationsRotAxis(quat, newChildRotation);
		}
		for (IdObject grandchild : child.getChildrenNodes()) {
			rotChildren(grandchild, center, quat);
		}

	}

	private void rotTranslation(Quat quat, AnimFlag<Vec3> newTranslation) {
		for (TreeMap<Integer, Entry<Vec3>> entryMap : newTranslation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Vec3> entry : entryMap.values()) {
					entry.getValue().rotate(Vec3.ZERO, quat);
					if (newTranslation.tans()) {
						entry.getInTan().rotate(Vec3.ZERO, quat);
						entry.getOutTan().rotate(Vec3.ZERO, quat);
					}
				}
			}
		}
	}

	private void moveTranslations(Vec3 dist, AnimFlag<Vec3> newTranslation) {
		for (TreeMap<Integer, Entry<Vec3>> entryMap : newTranslation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Vec3> entry : entryMap.values()) {
					entry.getValue().add(dist);
					if (newTranslation.tans()) {
						entry.getInTan().add(dist);
						entry.getOutTan().add(dist);
					}
				}
			}
		}
	}

	private void rotRotationsRotAxis(Quat quat, AnimFlag<Quat> newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
					entry.getValue().rotateAxis(quat);
					if (newRotation.tans()) {
						entry.getInTan().rotateAxis(quat);
						entry.getOutTan().rotateAxis(quat);
					}
				}
			}
		}
	}

	private void rotRotationsQuatMul(Quat quat, AnimFlag<Quat> newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
					entry.getValue().mul(quat);
					if (newRotation.tans()) {
						entry.getInTan().mul(quat);
						entry.getOutTan().mul(quat);
					}
				}
			}
		}
	}
}
