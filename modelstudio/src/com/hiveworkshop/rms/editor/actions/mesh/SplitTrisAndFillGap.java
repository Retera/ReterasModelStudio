package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SplitTrisAndFillGap implements UndoAction {
	List<Vec3> selection;
	List<Triangle> addedTriangles = new ArrayList<>();
	Set<Triangle> notSelectedEdgeTriangles;
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Set<GeosetVertex> orgEdgeVertices;
	BiMap<GeosetVertex, GeosetVertex> oldToNew = new BiMap<>();
	Set<Pair<GeosetVertex, GeosetVertex>> edges;
	ModelStructureChangeListener changeListener;

	public SplitTrisAndFillGap(Collection<GeosetVertex> selection, ModelStructureChangeListener changeListener) {
		affectedVertices.addAll(selection);
		this.selection = new ArrayList<>(selection);
		this.changeListener = changeListener;

		edges = ModelUtils.getEdges(affectedVertices);
		orgEdgeVertices = collectEdgeVerts(edges);
		notSelectedEdgeTriangles = getNotSelectedEdgeTris(getAllEdgeTris(orgEdgeVertices), affectedVertices);

		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			oldToNew.put(geosetVertex, geosetVertex.deepCopy());
		}
	}

	private Set<GeosetVertex> collectEdgeVerts(Set<Pair<GeosetVertex, GeosetVertex>> edges) {
		Set<GeosetVertex> orgEdgeVertices = new HashSet<>();
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			orgEdgeVertices.add(edge.getFirst());
			orgEdgeVertices.add(edge.getSecond());
		}
		return orgEdgeVertices;
	}

	private Set<Triangle> getAllEdgeTris(Set<GeosetVertex> orgEdgeVertices){
		Set<Triangle> edgeTriangles = new HashSet<>();
		for (GeosetVertex geosetVertex : orgEdgeVertices) {
			edgeTriangles.addAll(geosetVertex.getTriangles());
		}
		return edgeTriangles;
	}

	private Set<Triangle> getNotSelectedEdgeTris(Set<Triangle> edgeTriangles, Set<GeosetVertex> affectedVertices) {
		Set<Triangle> notSelectedEdgeTriangles = new HashSet<>();

		for (Triangle triangle : edgeTriangles) {
			if (!affectedVertices.contains(triangle.get(0))
					|| !affectedVertices.contains(triangle.get(1))
					|| !affectedVertices.contains(triangle.get(2))) {
				notSelectedEdgeTriangles.add(triangle);
			}
		}
		return notSelectedEdgeTriangles;
	}

	private void splitEdge() {
		for(Triangle triangle : notSelectedEdgeTriangles){
			for(int i = 0; i < triangle.getVerts().length; i++){
				GeosetVertex oldVertex = triangle.get(i);
				GeosetVertex newVertex = oldToNew.get(oldVertex);
				if(newVertex != null) {
					triangle.set(i, newVertex);
					newVertex.addTriangle(triangle);
					oldVertex.removeTriangle(triangle);
//					newVertex.getGeoset().add(newVertex);
				}
			}
		}
	}

	private void unSplitEdge() {
		for(Triangle triangle : notSelectedEdgeTriangles){
			for(int i = 0; i < triangle.getVerts().length; i++){
				GeosetVertex newVertex = triangle.get(i);
				GeosetVertex oldVertex = oldToNew.getByValue(newVertex);
				if(oldVertex != null) {
					triangle.set(i, oldVertex);
					oldVertex.addTriangle(triangle);
					newVertex.removeTriangle(triangle);
//					newVertex.getGeoset().remove(newVertex);
				}
			}
		}
	}

	private void fillGap() {
		for (Pair<GeosetVertex, GeosetVertex> edge : edges) {
			GeosetVertex org1 = edge.getFirst();
			GeosetVertex org2 = edge.getSecond();
			GeosetVertex new1 = oldToNew.get(edge.getFirst());
			GeosetVertex new2 = oldToNew.get(edge.getSecond());

			Geoset geoset = org1.getGeoset();
			Triangle tri1 = new Triangle(org1, new1, org2);
			tri1.setGeoset(geoset);
			Triangle tri2 = new Triangle(new1, new2, org2);
			tri2.setGeoset(geoset);
			addedTriangles.add(tri1);
			addedTriangles.add(tri2);
			geoset.add(tri1);
			geoset.add(tri2);
		}
	}

	private void removeGapFill() {
		for(Triangle triangle : addedTriangles){
			for(int i = 0; i<3; i++){
				triangle.get(i).removeTriangle(triangle);
			}
			triangle.getGeoset().remove(triangle);
		}
	}


	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : oldToNew.values()){
			vertex.getGeoset().add(vertex);
		}
		splitEdge();
		fillGap();

		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		removeGapFill();
		unSplitEdge();
		for (GeosetVertex vertex : oldToNew.values()){
			vertex.getGeoset().remove(vertex);
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Extend";
	}
}
