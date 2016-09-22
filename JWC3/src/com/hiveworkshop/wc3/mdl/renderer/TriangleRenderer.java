package com.hiveworkshop.wc3.mdl.renderer;

import java.util.List;

import com.hiveworkshop.wc3.mdl.Bone;

public interface TriangleRenderer {
	VertexRenderer renderVertex(double x, double y, double z, double normalX, double normalY, double normalZ,
			List<Bone> bones);

	void triangleFinished();
}
