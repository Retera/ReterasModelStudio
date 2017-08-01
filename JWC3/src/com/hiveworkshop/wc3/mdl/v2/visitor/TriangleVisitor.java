package com.hiveworkshop.wc3.mdl.v2.visitor;

import java.util.List;

import com.hiveworkshop.wc3.mdl.Bone;

public interface TriangleVisitor {
	VertexVisitor vertex(double x, double y, double z, double normalX, double normalY, double normalZ,
			List<Bone> bones);

	void triangleFinished();
}
