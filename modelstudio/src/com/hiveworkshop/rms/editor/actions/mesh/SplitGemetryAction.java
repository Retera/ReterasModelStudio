package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;

import java.util.*;

public final class SplitGemetryAction implements UndoAction {

	private final ModelStructureChangeListener modelStructureChangeListener;
	List<GeosetVertex> affectedVertices = new ArrayList<>();
	BiMap<GeosetVertex, GeosetVertex> oldToNew = new BiMap<>();

	Set<Triangle> fullySelectedTris;
	Set<Triangle> notSelectedTris;
	Set<GeosetVertex> edgeVerts;


	public SplitGemetryAction(EditableModel model,
	                          ModelStructureChangeListener modelStructureChangeListener,
	                          ModelView modelView) {
		this.modelStructureChangeListener = modelStructureChangeListener;

		affectedVertices.addAll(modelView.getSelectedVertices());

		fullySelectedTris = modelView.getSelectedTriangles();
		notSelectedTris = new HashSet<>();
		edgeVerts = new HashSet<>();

		for(GeosetVertex vertex : affectedVertices){
			for(Triangle triangle : vertex.getTriangles()){
				if (!fullySelectedTris.contains(triangle)){
					notSelectedTris.add(triangle);
					edgeVerts.add(vertex);
				}
			}
		}

		for(GeosetVertex vertex : edgeVerts){
			oldToNew.put(vertex, vertex.deepCopy());
		}
	}


	public SplitGemetryAction(Collection<GeosetVertex> affectedVertices,
	                          ModelStructureChangeListener modelStructureChangeListener) {

		this.modelStructureChangeListener = modelStructureChangeListener;

		this.affectedVertices.addAll(affectedVertices);

		fullySelectedTris = getFullySelectedTris(affectedVertices);
		notSelectedTris = new HashSet<>();
		edgeVerts = new HashSet<>();

		for(GeosetVertex vertex : affectedVertices){
			for(Triangle triangle : vertex.getTriangles()){
				if (!fullySelectedTris.contains(triangle)){
					notSelectedTris.add(triangle);
					edgeVerts.add(vertex);
				}
			}
		}

		for(GeosetVertex vertex : edgeVerts){
			oldToNew.put(vertex, vertex.deepCopy());
		}
	}

	private Set<Triangle> getFullySelectedTris(Collection<GeosetVertex> vertices){
		Set<GeosetVertex> verts = new HashSet<>(vertices);
		Set<Triangle> fullySelectedTris = new HashSet<>();
		for(GeosetVertex vertex : verts){
			for(Triangle triangle : vertex.getTriangles()){
				if (verts.contains(triangle.get(0))
						&& verts.contains(triangle.get(1))
						&& verts.contains(triangle.get(2))){
					fullySelectedTris.add(triangle);
				}
			}
		}
		return fullySelectedTris;
	}

	private void splitGeoset(){
		for(Triangle triangle : notSelectedTris){
			for(int i = 0; i<triangle.getVerts().length; i++){
				GeosetVertex oldVertex = triangle.get(i);
				GeosetVertex newVertex = oldToNew.get(oldVertex);
				if (newVertex != null){
					oldVertex.removeTriangle(triangle);
					triangle.set(i, newVertex);
					newVertex.addTriangle(triangle);
				}
			}
		}

		for(GeosetVertex vertex : oldToNew.values()){
			Geoset geoset = vertex.getGeoset();
			geoset.add(vertex);
			vertex.setGeoset(geoset);
		}
	}

	private void unSplitGeoset(){
		for(Triangle triangle : notSelectedTris){
			for(int i = 0; i<triangle.getVerts().length; i++){
				GeosetVertex newVertex = triangle.get(i);
				GeosetVertex oldVertex = oldToNew.getByValue(newVertex);
				if (oldVertex != null){
					newVertex.removeTriangle(triangle);
					triangle.set(i, newVertex);
					oldVertex.addTriangle(triangle);
				}
			}
		}
		for(GeosetVertex vertex : oldToNew.values()){
			Geoset geoset = vertex.getGeoset();
			geoset.remove(vertex);
			vertex.setGeoset(null);
		}
	}

	@Override
	public UndoAction undo() {
		unSplitGeoset();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		splitGeoset();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "split geoset";
	}


}
