package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class RotateNodesTPoseAction2 extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final ModelStructureChangeListener changeListener;
	private final List<RotateNodeTPoseAction2> rotNodeActions = new ArrayList<>();
	private double radians;
	private final Vec3 tempAxis = new Vec3();
	private final Vec3 realAxis = new Vec3();
	private final Mat4 tempMat = new Mat4();
	private final Map<GeosetVertex, Vec3[]> vertToLocNormTang = new HashMap<>();
	private final Map<GeosetVertex, Vec3[]> vertToOldLocNormTang = new HashMap<>();
	private final Map<IdObject, Mat4> nodeToWorldMat = new LinkedHashMap<>();
	private final Set<IdObject> topNodes = new LinkedHashSet<>();

	public RotateNodesTPoseAction2(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
//	                               Collection<CameraNode> camSelection,
	                               RenderModel editorRenderModel,
	                               Vec3 center, Vec3 axis, double radians,
	                               Mat4 rotMat,
	                               ModelStructureChangeListener changeListener){
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = changeListener;
		tempAxis.set(axis).transform(rotMat, 1, true);
		tempMat.set(rotMat).invert();
		realAxis.set(axis).transform(tempMat, 1, true);
		System.out.println("realAxis: " + realAxis + ", tempAxis: " + tempAxis + ", axis: " + axis);

		topNodes.addAll(getTopNodes(nodeSelection));
		fillTransformMap(topNodes);
		if(radians != 0.0){
			tempQuat.setFromAxisAngle(realAxis, (float) -radians);
			updateTransformMap(Vec3.ZERO, tempQuat);
		}

		fillVertexMaps(editorRenderModel.getModel().getGeosets());
		calcVertexLocs();
		for (IdObject node2 : nodeSelection) {
			rotNodeActions.add(new RotateNodeTPoseAction2(node2, realAxis, radians, center, rotMat, null));
		}
	}
	public RotateNodesTPoseAction2(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
	                               Collection<CameraNode> camSelection,
	                               RenderModel editorRenderModel,
	                               Vec3 center, Vec3 axis,
	                               Mat4 rotMat){
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.changeListener = null;
		tempMat.set(rotMat).invert();
		tempAxis.set(axis).transform(tempMat, 1, true);
		realAxis.set(axis).transform(tempMat, 1, true);
		System.out.println("realAxis: " + realAxis + ", tempAxis: " + tempAxis + ", axis: " + axis);

		topNodes.addAll(getTopNodes(nodeSelection));
		fillTransformMap(topNodes);
		if(radians != 0.0){
			tempQuat.setFromAxisAngle(realAxis, (float) -radians);
			updateTransformMap(Vec3.ZERO, tempQuat);
		}
		fillVertexMaps(editorRenderModel.getModel().getGeosets());
		calcVertexLocs();
		for (IdObject node2 : nodeSelection) {
//			rotNodeActions.add(new RotateNodeTPoseAction2(node2, axis, 0, center, rotMat, null));
			rotNodeActions.add(new RotateNodeTPoseAction2(node2, realAxis, 0, center, rotMat, null));
		}
	}
	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	private void fillTransformMap(Collection<IdObject> topNodes){
		for(IdObject node : topNodes){
			System.out.println("topNode: " + node.getName());
			fillTransformMap(node);
		}
	}
	private void fillTransformMap(IdObject node){
		nodeToWorldMat.put(node, new Mat4());
		for(IdObject child : node.getChildrenNodes()){
			fillTransformMap(child);
		}
	}
	private void fillVertexMaps(Collection<Geoset> geosets) {
		for (Geoset geoset : geosets) {
			for (GeosetVertex vertex : geoset.getVertices()){
				for (Bone bone : vertex.getAllBones()){
					if(nodeToWorldMat.containsKey(bone)){
						Vec3 tang = vertex.getTang() == null ? new Vec3(Vec3.Z_AXIS) : vertex.getTang().getVec3();
						Vec3[] oldLocNormTan = new Vec3[] {new Vec3(vertex), new Vec3(vertex.getNormal()), tang};
						vertToOldLocNormTang.put(vertex, oldLocNormTan);

						Vec3[] locNormTan = new Vec3[] {new Vec3(vertex), new Vec3(vertex.getNormal()), new Vec3(tang)};
						vertToLocNormTang.put(vertex, locNormTan);
						break;
					}
				}
			}
		}
	}

	public RotateNodesTPoseAction2 doSetup() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		calcAndApplyVertexLocs();
		for(RotateNodeTPoseAction2 action : rotNodeActions){
			action.doSetup();
		}
		return this;
	}

	public RotateNodesTPoseAction2 updateRotation(double radians){
		this.radians += radians;
		tempQuat.setFromAxisAngle(realAxis, (float) -radians);
		updateTransformMap(Vec3.ZERO, tempQuat);
		calcAndApplyVertexLocs();
		for(RotateNodeTPoseAction2 action : rotNodeActions){
			action.updateRotation(radians);
		}
		return this;
	}
	public RotateNodesTPoseAction2 setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		tempQuat.setFromAxisAngle(realAxis, (float) -rotDiff);
		updateTransformMap(Vec3.ZERO, tempQuat);
		calcAndApplyVertexLocs();
		for(RotateNodeTPoseAction2 action : rotNodeActions){
			action.updateRotation(rotDiff);
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction2 undo() {
		for (GeosetVertex vertex : vertToOldLocNormTang.keySet()){
			Vec3[] vec3s = vertToOldLocNormTang.get(vertex);
			vertex.set(vec3s[0]);
			vertex.setNormal(vec3s[1]);
			if(vertex.getTangent() != null){
				vertex.getTangent().set(vec3s[2]);
			}
		}
		for(RotateNodeTPoseAction2 action : rotNodeActions){
			action.undo();
		}
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.undo();
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodesTPoseAction2 redo() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		for(RotateNodeTPoseAction2 action : rotNodeActions){
			action.redo();
		}

		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] vec3s = vertToLocNormTang.get(vertex);
			vertex.set(vec3s[0]);
			vertex.setNormal(vec3s[1]);
			if(vertex.getTangent() != null){
				vertex.getTangent().set(vec3s[2]);
			}
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate " + "node.getName()";
	}



	Mat4 locMat = new Mat4();
	Quat tempQuat = new Quat();

	private void updateTransformMap(Vec3 transl, Quat quat){
		for(IdObject node : topNodes){
			updateTransform(node, transl, quat);
		}
	}
	private void updateTransform(IdObject node, Vec3 transl, Quat quat){
		Mat4 parentMat = nodeToWorldMat.getOrDefault(node.getParent(), Mat4.IDENTITY);
		locMat.fromRotationTranslationScaleOrigin(quat, transl, Vec3.ONE, node.getPivotPoint());
		nodeToWorldMat.get(node).set(parentMat).mul(locMat);
		for(IdObject child : node.getChildrenNodes()){
			updateTransform(child, Vec3.ZERO, Quat.IDENTITY);
		}
	}


	private void calcVertexLocs() {
		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] locNormTan = vertToLocNormTang.get(vertex);
//			Vec3[] ogrLocNormTan = vertToOldLocNormTang.get(vertex);
//			update(getTransform(vertex), ogrLocNormTan, locNormTan);
			update(getTransform(vertex), locNormTan);
		}
	}
	private void calcAndApplyVertexLocs() {
		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] locNormTan = vertToLocNormTang.get(vertex);
//			Vec3[] ogrLocNormTan = vertToOldLocNormTang.get(vertex);
//			update(getTransform(vertex), ogrLocNormTan, locNormTan);
			update(getTransform(vertex), locNormTan);

			applyVertTransform(vertex, locNormTan);
		}
	}

	private void applyVertTransform(GeosetVertex vertex, Vec3[] locNormTan) {
		vertex.set(locNormTan[0]);
		vertex.setNormal(locNormTan[1]);
		if(vertex.getTangent() != null){
			vertex.getTangent().set(locNormTan[2]);
		}
	}

	private void update(Mat4 mat4, Vec3[] orgLocNormTan, Vec3[] locNormTan) {
		locNormTan[0].set(orgLocNormTan[0]);
		locNormTan[1].set(orgLocNormTan[1]);
		locNormTan[2].set(orgLocNormTan[2]);
		if (mat4 != null) {
			locNormTan[0].transform(mat4);
			locNormTan[1].transform(0, mat4).normalize();
			locNormTan[2].transform(0, mat4).normalize();
		}
	}
	private void update(Mat4 mat4, Vec3[] locNormTan) {
		if (mat4 != null) {
			locNormTan[0].transform(mat4);
			locNormTan[1].transform(0, mat4).normalize();
			locNormTan[2].transform(0, mat4).normalize();
		}
	}

	private Mat4 getTransform(GeosetVertex vertex) {
		if (vertex.getSkinBones() != null) {
			return processHdBones(vertex.getSkinBones());
		} else {
			return processSdBones(vertex.getMatrix().getBones());
		}
	}

	Mat4 matrixSumHeap = new Mat4();
	public Mat4 processHdBones(SkinBone[] skinBones) {
		boolean foundValidBones = false;
		matrixSumHeap.setZero();

		for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
			SkinBone skinBone = skinBones[boneIndex];
			if (skinBone != null && skinBone.getBone() != null) {
				Bone bone = skinBone.getBone();
				foundValidBones = true;
				Mat4 worldMatrix = nodeToWorldMat.getOrDefault(bone, Mat4.IDENTITY);
				matrixSumHeap.addScaled(worldMatrix, skinBone.getWeightFraction());
			}
		}
		if (!foundValidBones) {
			matrixSumHeap.setIdentity();
		}
		return matrixSumHeap;
	}

	public Mat4 processSdBones(List<Bone> bones) {
		matrixSumHeap.setZero();
		if (bones.size() > 0) {
			for (Bone bone : bones) {
				Mat4 worldMatrix = nodeToWorldMat.getOrDefault(bone, Mat4.IDENTITY);
				matrixSumHeap.add(worldMatrix);
			}
			return matrixSumHeap.uniformScale(1f / bones.size());
		}
		return matrixSumHeap.setIdentity();
	}
}
