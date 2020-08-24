package com.hiveworkshop.rms.editor.model.visitor;

import java.util.List;

import com.hiveworkshop.rms.editor.model.Bone;

public interface TriangleVisitor {
	VertexVisitor vertex(double x, double y, double z, double normalX, double normalY, double normalZ,
			List<Bone> bones);

	VertexVisitor hdVertex(double x, double y, double z, double normalX, double normalY, double normalZ,
			Bone[] skinBones, short[] skinBoneWeights);

	void triangleFinished();

	TriangleVisitor NO_ACTION = new TriangleVisitor() {
		@Override
		public VertexVisitor vertex(final double x, final double y, final double z, final double normalX,
				final double normalY, final double normalZ, final List<Bone> bones) {
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public VertexVisitor hdVertex(final double x, final double y, final double z, final double normalX,
				final double normalY, final double normalZ, final Bone[] skinBones, final short[] skinBoneWeights) {
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public void triangleFinished() {

		}
	};
}
