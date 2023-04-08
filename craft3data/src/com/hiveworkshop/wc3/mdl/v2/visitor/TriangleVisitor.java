package com.hiveworkshop.wc3.mdl.v2.visitor;

import java.util.List;

import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;

public interface TriangleVisitor {
	VertexVisitor vertex(double x, double y, double z, double normalX, double normalY, double normalZ,
			List<GeosetVertexBoneLink> bones);

	void triangleFinished();

	TriangleVisitor NO_ACTION = new TriangleVisitor() {
		@Override
		public VertexVisitor vertex(final double x, final double y, final double z, final double normalX,
				final double normalY, final double normalZ, final List<GeosetVertexBoneLink> bones) {
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public void triangleFinished() {

		}
	};
}
