package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Debug;

import java.util.*;

public final class SplitGeosetAction implements UndoAction {

	private final ModelStructureChangeListener modelStructureChangeListener;
	private final EditableModel model;

	private final BiMap<Geoset, Geoset> oldGeoToNewGeo = new BiMap<>();
	private final List<GeosetVertex> affectedVertices = new ArrayList<>();
	private final BiMap<GeosetVertex, GeosetVertex> oldToNew = new BiMap<>();

	private final Set<Triangle> fullySelectedTris = new HashSet<>();
	private final Set<Triangle> notSelectedTris = new HashSet<>();

	public SplitGeosetAction(EditableModel model,
	                         Collection<GeosetVertex> affectedVertices,
	                         ModelStructureChangeListener changeListener) {
		this.model = model;
		this.modelStructureChangeListener = changeListener;

		this.affectedVertices.addAll(affectedVertices);

		Set<GeosetVertex> verts = new HashSet<>(affectedVertices);
		for(GeosetVertex vertex : verts){
			for(Triangle triangle : vertex.getTriangles()){
				if (verts.contains(triangle.get(0))
						&& verts.contains(triangle.get(1))
						&& verts.contains(triangle.get(2))){
					fullySelectedTris.add(triangle);
				}
			}
		}

		Set<GeosetVertex> edgeVerts = new HashSet<>();
		for(GeosetVertex vertex : affectedVertices){
			for(Triangle triangle : vertex.getTriangles()){
				if (!fullySelectedTris.contains(triangle)){
					notSelectedTris.add(triangle);
					edgeVerts.add(vertex);
				}
			}
			oldGeoToNewGeo.computeIfAbsent(vertex.getGeoset(), k -> vertex.getGeoset().emptyCopy());
		}

		for(GeosetVertex vertex : edgeVerts){
			oldToNew.put(vertex, vertex.deepCopy());
		}
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

		for(GeosetVertex vertex : affectedVertices){
			Geoset oldGeoset = vertex.getGeoset();
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset);
			oldGeoset.remove(vertex);
			newGeoset.add(vertex);
			vertex.setGeoset(newGeoset);
		}
		for(GeosetVertex vertex : oldToNew.values()){
			Geoset geoset = vertex.getGeoset();
			geoset.add(vertex);
			vertex.setGeoset(geoset);
		}

		for(Triangle triangle : fullySelectedTris){
			Geoset oldGeoset = triangle.getGeoset();
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset);
			oldGeoset.remove(triangle);
			newGeoset.add(triangle);
			triangle.setGeoset(newGeoset);
		}

		for (Geoset geoset : oldGeoToNewGeo.values()){
			model.add(geoset);
		}
		for (Geoset geoset : oldGeoToNewGeo.keys()){
			if(geoset.getVertices().isEmpty()){
				model.remove(geoset);
			}
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

		for(GeosetVertex vertex : affectedVertices){
			Geoset newGeoset = vertex.getGeoset();
			Geoset oldGeoset = oldGeoToNewGeo.getByValue(newGeoset);
			newGeoset.remove(vertex);
			oldGeoset.add(vertex);
			vertex.setGeoset(oldGeoset);
		}

		for(Triangle triangle : fullySelectedTris){
			Geoset newGeoset = triangle.getGeoset();
			Geoset oldGeoset = oldGeoToNewGeo.getByValue(newGeoset);
			newGeoset.remove(triangle);
			oldGeoset.add(triangle);
			triangle.setGeoset(oldGeoset);
		}
		for (Geoset geoset : oldGeoToNewGeo.values()){
			model.remove(geoset);
		}
		for (Geoset geoset : oldGeoToNewGeo.keys()){
			if(!model.contains(geoset)){
				model.add(geoset);
			}
		}
	}

	private void checkGeosets(){
		for (Geoset geoset1 : oldGeoToNewGeo.keys()){
			print("checking geosets!");
			Geoset geoset2 = oldGeoToNewGeo.get(geoset1);
			checkGeosets(geoset1, geoset2);
			checkGeosets(geoset2, geoset1);
		}
	}

	private void checkGeosets(Geoset geoset1, Geoset geoset2) {
		Set<GeosetVertex> vertices1 = new HashSet<>(geoset1.getVertices());
		for(GeosetVertex vertex : geoset2.getVertices()){
			if(vertices1.contains(vertex)){
				print("vertex in two geos!!!!!");
			}
			for(Triangle triangle : vertex.getTriangles()){
				if (geoset1.contains(triangle)){
					if (geoset2.contains(triangle)){
						print("Triangle in both geosets!");
					} else {
						print("Triangle in wrong geoset!");
					}
				} else if (!geoset2.contains(triangle)){
					print("Triangle not in any geoset!");
				}
				if (!triangle.containsRef(vertex)){
					print("vertex not part of triangle!");
				}
			}
		}

		for(Triangle triangle : geoset1.getTriangles()){
			for(GeosetVertex vertex : triangle.getVerts()){
				if (!vertex.hasTriangle(triangle)){
					print("vertex does not own triangle!");
				}
			}
		}
	}

	private void print(String s) {
		Debug.print(s);
	}


	@Override
	public SplitGeosetAction undo() {
		unSplitGeoset();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public SplitGeosetAction redo() {
		splitGeoset();
		checkGeosets();
		modelStructureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "Split Geoset";
	}


}
