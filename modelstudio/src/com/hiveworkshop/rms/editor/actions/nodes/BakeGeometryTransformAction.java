package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class BakeGeometryTransformAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Map<GeosetVertex, Vec3[]> vertToLocNormTang = new HashMap<>();
	private final Map<GeosetVertex, Vec3[]> vertToOldLocNormTang = new HashMap<>();
	private final Map<IdObject, Mat4> nodeToWorldMat = new LinkedHashMap<>();
	private final Set<IdObject> topNodes = new LinkedHashSet<>();

	private final Mat4 locMat = new Mat4();
	private final Vec3 translation = new Vec3();
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public BakeGeometryTransformAction(Collection<IdObject> nodeSelection,
	                                   Collection<Geoset> geosets,
	                                   Mat4 rotMat,
	                                   ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		topNodes.addAll(getTopNodes(nodeSelection));
		fillTransformMap(topNodes);
		fillVertexMaps(geosets);
	}

	public BakeGeometryTransformAction(Collection<IdObject> nodeSelection,
	                                   Collection<Geoset> geosets,
	                                   Mat4 rotMat,
	                                   Sequence sequence, int time,
	                                   ModelStructureChangeListener changeListener){
		this(nodeSelection, geosets, rotMat, changeListener);

		List<IdObject> sortedNodes = collectSortedNodes(topNodes);
		Quat quat = new Quat();
		Vec3 trans = new Vec3();
		for (IdObject idObject : sortedNodes){
			AnimFlag<Quat> rotAnimFlag = idObject.getRotationFlag();
			if(rotAnimFlag != null){
				quat.set(rotAnimFlag.interpolateAt(sequence, time));
			} else {
				quat.setIdentity();
			}
			AnimFlag<Vec3> translAnimFlag = idObject.getTranslationFlag();
			if(translAnimFlag != null){
				trans.set(translAnimFlag.interpolateAt(sequence, time));
			} else {
				trans.set(Vec3.ZERO);
			}

			updateTransformMapNode(trans, quat, idObject);
		}
		calcVertexLocs();
	}


	private List<IdObject> getTopNodes(Collection<IdObject> selection) {
		return selection.stream()
				.filter(idObject -> idObject.getParent() == null || !selection.contains(idObject.getParent()))
				.collect(Collectors.toList());
	}

	private void fillTransformMap(Collection<IdObject> topNodes){
		for(IdObject node : topNodes){
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

	private List<IdObject> collectSortedNodes(Collection<IdObject> topNodes){
		List<IdObject> sorted = new ArrayList<>();
		for (IdObject node : topNodes){
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

	public BakeGeometryTransformAction calculateTransform(Vec3 delta, Quat quat, Collection<IdObject> nodes){
		for(IdObject node : nodes) {
			updateTransformMapNode(delta, quat, node);
		}
		calcVertexLocs();
		return this;
	}
	public BakeGeometryTransformAction updateTransform(Vec3 delta, Quat quat, Collection<IdObject> nodes){
		for(IdObject node : nodes) {
			updateTransformMapNode(delta, quat, node);
		}
		calcAndApplyVertexLocs();
		return this;
	}

	public BakeGeometryTransformAction doSetup(){
		applyVerticesTransforms(vertToLocNormTang);
		return this;
	}

	@Override
	public BakeGeometryTransformAction undo() {
		applyVerticesTransforms(vertToOldLocNormTang);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public BakeGeometryTransformAction redo() {
		applyVerticesTransforms(vertToLocNormTang);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	private void applyVerticesTransforms(Map<GeosetVertex, Vec3[]> vertToLocNormTang) {
		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] vec3s = vertToLocNormTang.get(vertex);
			vertex.set(vec3s[0]);
			vertex.setNormal(vec3s[1]);
			if(vertex.getTangent() != null){
				vertex.getTangent().set(vec3s[2]);
			}
		}
	}

	@Override
	public String actionName() {
		return "Bake Geometry Transform";
	}
	private void updateTransformMapNode(Vec3 delta, Quat quat, IdObject node) {
		updateTransform(node, setTranslationHeap(node.getPivotPoint(), delta), quat);
	}

	private void updateTransform(IdObject node, Vec3 transl, Quat rot){
		Mat4 parentMat = nodeToWorldMat.getOrDefault(node.getParent(), Mat4.IDENTITY);
		locMat.fromRotationTranslationScaleOrigin(rot, transl, Vec3.ONE, node.getPivotPoint());
		nodeToWorldMat.get(node).set(parentMat).mul(locMat);
		for(IdObject child : node.getChildrenNodes()){
			updateTransform(child, Vec3.ZERO, Quat.IDENTITY);
		}
	}

	private void calcVertexLocs() {
		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] locNormTan = vertToLocNormTang.get(vertex);
			update(getTransform(vertex), locNormTan);
		}
	}
	private void calcAndApplyVertexLocs() {
		for (GeosetVertex vertex : vertToLocNormTang.keySet()){
			Vec3[] locNormTan = vertToLocNormTang.get(vertex);
			update(getTransform(vertex), locNormTan);

			applyVertTransform(vertex, locNormTan);
		}
	}

	private void applyVertTransform(GeosetVertex vertex, Vec3[] locNormTang) {
		vertex.set(locNormTang[0]);
		vertex.setNormal(locNormTang[1]);
		if(vertex.getTangent() != null){
			vertex.getTangent().set(locNormTang[2]);
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

	private Vec3 setTranslationHeap(Vec3 pivotPoint, Vec3 newDelta) {
		translation.set(pivotPoint)
				.transform(rotMat, 1, true)
				.add(newDelta)
				.transform(invRotMat, 1, true)
				.sub(pivotPoint);

		return translation;
	}
}
