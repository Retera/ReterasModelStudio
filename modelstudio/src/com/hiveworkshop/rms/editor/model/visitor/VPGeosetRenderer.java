package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public class VPGeosetRenderer {
	int index;
	private Vec2[] triV2 = new Vec2[3];
	private Vec2[] normalV2 = new Vec2[3];
	private ProgramPreferences programPreferences;
	private CoordinateSystem coordinateSystem;
	private Graphics2D graphics;
	private Geoset geoset;
	boolean isAnimated;
	private RenderModel renderModel;

	public VPGeosetRenderer() {
	}

	public VPGeosetRenderer reset(Graphics2D graphics,
	                              ProgramPreferences programPreferences,
	                              CoordinateSystem coordinateSystem,
	                              RenderModel renderModel,
	                              Geoset geoset, boolean isAnimated) {
		this.isAnimated = isAnimated;
		this.geoset = geoset;
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	//	@Override
	public void beginTriangle(boolean isHD) {
		renderGeosetTries(geoset, isHD);
	}

	private void renderGeosetTries(Geoset geoset, boolean isHD) {
		for (Triangle triangle : geoset.getTriangles()) {
			index = 0;
			for (GeosetVertex vertex : triangle.getVerts()) {
				vertex(vertex, isHD);
			}
			triangleFinished();
		}
	}

	public void vertex(GeosetVertex vertex, Boolean isHd) {
		Vec3 vertexSumHeap = vertex;
		Vec3 normal = vertex.getNormal();
		Vec3 normalSumHeap = normal;
		if (isAnimated) {
			List<Bone> bones1 = vertex.getBoneAttachments();
			Mat4 bonesMatrixSumHeap;
			if (isHd) {
				bonesMatrixSumHeap = ModelUtils.processHdBones(renderModel, vertex.getSkinBones());
			} else {
				bonesMatrixSumHeap = ModelUtils.processSdBones(renderModel, bones1);
			}
			vertexSumHeap = Vec3.getTransformed(vertex, bonesMatrixSumHeap);
			if (normal != null) {
				normalSumHeap = Vec3.getTransformed(normal, bonesMatrixSumHeap);
				normalSumHeap.normalize();
			}

		}

		fillPointArrays(vertexSumHeap, normalSumHeap);
	}

	public void triangleFinished() {
		GU.drawPolygon(graphics, triV2);
		if (programPreferences.showNormals()) {
			Color triangleColor = graphics.getColor();
			graphics.setColor(programPreferences.getNormalsColor());

			GU.drawLines(graphics, triV2, normalV2);

			graphics.setColor(triangleColor);
		}

	}

	public void fillPointArrays(Vec3 vertex, Vec3 normal) {

		Vec2 vert2 = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex);
		triV2[index] = vert2;

		if (programPreferences.showNormals() && normal != null) {
			Vec3 normalPoint = Vec3.getScaled(normal, (float) (12 / coordinateSystem.getZoom())).add(vertex);

			normalV2[index] = CoordSysUtils.convertToViewVec2(coordinateSystem, normalPoint);
		}
		index++;
	}

}