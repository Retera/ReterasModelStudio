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
	private final Sequence sequence;
	private final int time;
	private final BakeGeometryTransformAction geometryTransformAction;

	public TPoseFromPoseAction(EditableModel model, Sequence sequence, int time, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.time = time;
		System.out.println("seq: " + sequence.getName() + ", at " + time);

		List<IdObject> idObjects = collectSortedNodes(model.getIdObjects());
		for(IdObject node : idObjects){
			nodeToOldPivot.put(node, new Vec3(node.getPivotPoint()));
			nodeToNewPivot.put(node, new Vec3(node.getPivotPoint()));

			AnimFlag<Vec3> transFlag = node.getTranslationFlag();
			if (transFlag != null) {
				nodeToOldTrans.put(node, (Vec3AnimFlag) transFlag);
				nodeToNewTrans.put(node, (Vec3AnimFlag) transFlag.deepCopy());
			}

			AnimFlag<Quat> rotFlag = node.getRotationFlag();
			if (rotFlag != null) {
				nodeToOldRot.put(node, (QuatAnimFlag) rotFlag);
				nodeToNewRot.put(node, (QuatAnimFlag) rotFlag.deepCopy());
			}
		}
		processTransforms(idObjects);

		geometryTransformAction = new BakeGeometryTransformAction(idObjects, model.getGeosets(), Mat4.IDENTITY, sequence, time, null);
	}

	private List<IdObject> getParentNodes(Collection<IdObject> idObjects){
		return idObjects.stream().filter(idObject -> idObject.getParent() == null).collect(Collectors.toList());
	}

	private List<IdObject> collectSortedNodes(Collection<IdObject> idObjects){
		List<IdObject> parentNodes = getParentNodes(idObjects);
		List<IdObject> sorted = new ArrayList<>();
		for (IdObject node : parentNodes){
			collectSortedChildNodes(node, sorted);
		}
		return sorted;
	}
	private void collectSortedChildNodes(IdObject node, List<IdObject> sorted){
		sorted.add(node);
		for (IdObject child : node.getChildrenNodes()){
			collectSortedChildNodes(child, sorted);
		}
	}

	private void processTransforms(List<IdObject> idObjects){
		for (IdObject idObject : idObjects){
			processNodeRotTransforms(idObject);
			processNodeTranslTransforms(idObject);
		}
	}

	Quat quat = new Quat();
	Vec3 axis = new Vec3();
	Quat tempQuat = new Quat();
	Vec3 trans = new Vec3();
	Vec3 tempTrans = new Vec3();

	private void processNodeRotTransforms(IdObject idObject){
		QuatAnimFlag quatAnimFlag = nodeToNewRot.get(idObject);

		if(quatAnimFlag != null && quatAnimFlag.size(sequence) != 0) {
			quat.set(quatAnimFlag.interpolateAt(sequence, time));
			axis.setAsAxis(quat).normalize();

			tempQuat.setFromAxisAngle(axis, quat.getAxisAngle()).normalize();
			processChildNodesRot(idObject, nodeToNewPivot.get(idObject), tempQuat);

			tempQuat.setIdentity().mulInverse(quat).normalize();
			double radians = tempQuat.getAxisAngle();

			tempQuat.setFromAxisAngle(axis, (float) -radians);

			rotRotations(tempQuat, quatAnimFlag);
		} else {
			quat.setIdentity();
		}
	}
	private void processNodeTranslTransforms(IdObject idObject){
		Vec3AnimFlag vec3AnimFlag = nodeToNewTrans.get(idObject);

		if(vec3AnimFlag != null && vec3AnimFlag.size(sequence) != 0){
			trans.set(vec3AnimFlag.interpolateAt(sequence, time));

			nodeToNewPivot.get(idObject).add(trans);
			processChildNodesTrans(idObject, trans);
			tempTrans.set(Vec3.ZERO).sub(trans);
			moveTranslations(tempTrans, vec3AnimFlag);

		} else {
			trans.set(Vec3.ZERO);
		}
	}

	private void moveTranslations(Vec3 dist, Vec3AnimFlag newTranslation) {
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

	private void processChildNodesTrans(IdObject idObject, Vec3 trans) {
		for (IdObject child : idObject.getChildrenNodes()){
			nodeToNewPivot.get(child).add(trans);
			processChildNodesTrans(child, trans);
		}
	}

	private void processChildNodesRot(IdObject idObject, Vec3 pivot, Quat quat) {
		for (IdObject child : idObject.getChildrenNodes()){
			nodeToNewPivot.get(child).rotate(pivot, quat);
			if(nodeToNewTrans.containsKey(child)){
				trans.set(nodeToNewTrans.get(child).interpolateAt(sequence, time));
				tempTrans.set(trans).transform(tempQuat);
				rotTranslations(quat, nodeToNewTrans.get(child));
			}
			if(nodeToNewRot.containsKey(child)){
				rotChildRotation(quat, nodeToNewRot.get(child));
			}
			processChildNodesRot(child, pivot, quat);
		}
	}

	Vec3 tempAxis = new Vec3();
	private void rotChildRotation(Quat quat, QuatAnimFlag newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
					float axisAngle = entry.getValue().getAxisAngle();
					tempAxis.rotate(Vec3.ZERO, quat);
					entry.getValue().setFromAxisAngle(tempAxis, axisAngle);

					if (newRotation.tans()) {
						tempAxis.setAsAxis(entry.getInTan());
						axisAngle = entry.getInTan().getAxisAngle();
						tempAxis.rotate(Vec3.ZERO, quat);
						entry.getInTan().setFromAxisAngle(tempAxis, axisAngle);

						tempAxis.setAsAxis(entry.getOutTan());
						axisAngle = entry.getOutTan().getAxisAngle();
						tempAxis.rotate(Vec3.ZERO, quat);
						entry.getOutTan().setFromAxisAngle(tempAxis, axisAngle);
					}
				}

			}
		}
	}


	private void rotTranslations(Quat quat, Vec3AnimFlag newTranslation) {
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
	private void rotRotations(Quat quat, QuatAnimFlag newRotation) {
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

	private Vec3 getCompAxis(Quat quat) {
		Vec3 axis = quat.getAxis();
		if(Math.abs(axis.dot(Vec3.X_AXIS)) < Math.abs(axis.dot(Vec3.Z_AXIS))){
			return axis.cross(Vec3.X_AXIS);
		} else {
			return axis.cross(Vec3.Z_AXIS);
		}
	}


	@Override
	public UndoAction undo() {
		for(IdObject idObject : nodeToOldTrans.keySet()){
			idObject.add(nodeToOldTrans.get(idObject));
		}
		for(IdObject idObject : nodeToOldRot.keySet()){
			idObject.add(nodeToOldRot.get(idObject));
		}
		for(IdObject idObject : nodeToOldPivot.keySet()){
			idObject.setPivotPoint(nodeToOldPivot.get(idObject));
		}

		geometryTransformAction.undo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(IdObject idObject : nodeToNewTrans.keySet()){
			idObject.add(nodeToNewTrans.get(idObject));
		}
		for(IdObject idObject : nodeToNewRot.keySet()){
			idObject.add(nodeToNewRot.get(idObject));
		}
		for(IdObject idObject : nodeToNewPivot.keySet()){
			idObject.setPivotPoint(nodeToNewPivot.get(idObject));
		}

		geometryTransformAction.redo();

		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}


	@Override
	public String actionName() {
		return "Bake T-Pose";
	}
}
