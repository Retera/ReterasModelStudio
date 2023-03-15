package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class RecalculateTangentsAction implements UndoAction {
	private final List<Vec4> oldTangents;
	private final List<Vec4> newTangents;
	private final List<GeosetVertex> affectedVertices;
	private int zeroAreaUVTris = 0;

	public RecalculateTangentsAction(Collection<GeosetVertex> affectedVertices) {
		this.affectedVertices = new ArrayList<>(affectedVertices);
		this.oldTangents = new ArrayList<>();
		this.newTangents = new ArrayList<>();
		for (GeosetVertex vertex : affectedVertices) {
			if(vertex.getTangent() != null){
				this.oldTangents.add(new Vec4(vertex.getTangent()));
			} else {
				this.oldTangents.add(null);
			}
		}
		recalculateTangents2(this.affectedVertices);
	}

	@Override
	public String actionName() {
		return "Recalculate Tangents";
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i < affectedVertices.size(); i++) {
			affectedVertices.get(i).setTangent(oldTangents.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (int i = 0; i < affectedVertices.size(); i++) {
			affectedVertices.get(i).setTangent(newTangents.get(i));
		}
		return this;
	}

	public int getZeroAreaUVTris() {
		return zeroAreaUVTris;
	}

	// copied from
	// https://github.com/TaylorMouse/MaxScripts/blob/master/Warcraft%203%20Reforged/GriffonStudios/GriffonStudios_Warcraft_3_Reforged_Export.ms#L169
	// and edited
	// https://gamedev.stackexchange.com/questions/68612/how-to-compute-tangent-and-bitangent-vectors
	public void recalculateTangents2(List<GeosetVertex> affectedVertices) {

		Set<Triangle> triangles = new HashSet<>();
		for(GeosetVertex vertex : affectedVertices){
			triangles.addAll(vertex.getTriangles());
		}


		Vec3 edge1 = new Vec3();
		Vec3 edge2 = new Vec3();
		Vec3 edge1Dir = new Vec3();
		Vec3 edge2Dir = new Vec3();
		Vec3 ssDir = new Vec3();
		Vec3 ttDir = new Vec3();
		Vec2 uvEdge1 = new Vec2();
		Vec2 uvEdge2 = new Vec2();
		Map<GeosetVertex, Vec3> vertexSMap = new HashMap<>();
		Map<GeosetVertex, Vec3> vertexTMap = new HashMap<>();

		for (Triangle triangle : triangles) {
			GeosetVertex v1 = triangle.get(0);
			GeosetVertex v2 = triangle.get(1);
			GeosetVertex v3 = triangle.get(2);

			edge1.set(v2).sub(v1);
			edge2.set(v3).sub(v1);

			Vec2 w1 = v1.getTVertex(0);
			Vec2 w2 = v2.getTVertex(0);
			Vec2 w3 = v3.getTVertex(0);

			uvEdge1.set(w2).sub(w1);
			uvEdge2.set(w3).sub(w1);


			double tVertWeight = (uvEdge1.x * uvEdge2.y) - (uvEdge2.x * uvEdge1.y);
			if (tVertWeight == 0) {
				tVertWeight = 0.00000001;
				zeroAreaUVTris++;
			}

			double r = 1.0 / tVertWeight;

			edge1Dir.set(edge1).scale(uvEdge2.y);
			edge2Dir.set(edge2).scale(uvEdge1.y);
			ssDir.set(edge1Dir).sub(edge2Dir).scale((float) r);

			edge1Dir.set(edge1).scale(uvEdge2.x);
			edge2Dir.set(edge2).scale(uvEdge1.x);
			ttDir.set(edge2Dir).sub(edge1Dir).scale((float) r);

			vertexSMap.computeIfAbsent(triangle.get(0), k -> new Vec3()).add(ssDir);
			vertexSMap.computeIfAbsent(triangle.get(1), k -> new Vec3()).add(ssDir);
			vertexSMap.computeIfAbsent(triangle.get(2), k -> new Vec3()).add(ssDir);

			vertexTMap.computeIfAbsent(triangle.get(0), k -> new Vec3()).add(ttDir);
			vertexTMap.computeIfAbsent(triangle.get(1), k -> new Vec3()).add(ttDir);
			vertexTMap.computeIfAbsent(triangle.get(2), k -> new Vec3()).add(ttDir);
		}

		Vec3 tempNorm = new Vec3();
		for(GeosetVertex vertex : affectedVertices){
			Vec4 tangent = new Vec4();
			Vec3 triSAcc = vertexSMap.getOrDefault(vertex, Vec3.Z_AXIS);
			Vec3 normal = vertex.getNormal();

			tangent.set(triSAcc).addScaled(normal, normal.dot(triSAcc)).normalize();

			tempNorm.set(normal).cross(triSAcc);

			Vec3 triTAcc = vertexTMap.getOrDefault(vertex, Vec3.Z_AXIS);
			tangent.w = tempNorm.dot(triTAcc) < 0.0f ? -1.0f : 1.0f;

			newTangents.add(tangent);
		}
	}
}
