package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public interface TriangleVisitor {

	TriangleVisitor NO_ACTION = new TriangleVisitor() {

		@Override
		public VertexVisitor vertex(Vec3 vert, Vec3 normal, final List<Bone> bones) {
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public VertexVisitor hdVertex(Vec3 vert, Vec3 normal, final Bone[] skinBones, final short[] skinBoneWeights) {
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public void triangleFinished() {
		}
	};

	void triangleFinished();

	VertexVisitor vertex(Vec3 vert, Vec3 normal, List<Bone> bones);

	VertexVisitor hdVertex(Vec3 vert, Vec3 normal, Bone[] skinBones, short[] skinBoneWeights);
}
