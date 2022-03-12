package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SelectEdge {

	Geoset geoset;
	LinkedList<GeosetVertex> edgeToSelect = new LinkedList<>();

	public SelectEdge(ModelHandler modelHandler){
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
		if(!selectedVertices.isEmpty()) {
			collectEdges(selectedVertices.stream().findFirst().get());
			UndoAction action = new AddSelectionUggAction(new SelectionBundle(edgeToSelect), modelView, ModelStructureChangeListener.getModelStructureChangeListener());
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}


	private void collectEdges(GeosetVertex startVert) {
		geoset = startVert.getGeoset();
		Set<GeosetVertex> addedVerts = new HashSet<>();

		edgeToSelect.add(startVert);
		addedVerts.add(startVert);

		GeosetVertex validNeighbour;
		do {
			validNeighbour = getValidEdgeNeighbour(addedVerts, edgeToSelect.getLast());
			if(validNeighbour != null){
				edgeToSelect.addLast(validNeighbour);
				addedVerts.add(validNeighbour);
			}
		} while (validNeighbour != null);

		do {
			validNeighbour = getValidEdgeNeighbour(addedVerts, edgeToSelect.getFirst());
			if(validNeighbour != null){
				edgeToSelect.addFirst(validNeighbour);
				addedVerts.add(validNeighbour);
			}
		} while (validNeighbour != null);
	}

	private GeosetVertex getValidEdgeNeighbour(Set<GeosetVertex> vertexPool, GeosetVertex vertex) {
		Set<GeosetVertex> visitedNeighbours = new HashSet<>();
		Set<GeosetVertex> validNeighbours = new HashSet<>();

//		Set<GeosetVertex> allNeighbours = new HashSet<>();
//		vertex.getTriangles().forEach(
//				triangle -> Arrays.stream(triangle.getVerts())
//						.filter(v -> v != vertex)
//						.forEach(allNeighbours::add));

//		for(Triangle triangle : vertex.getTriangles()){
//			for(GeosetVertex vert : triangle.getVerts()){
//				if (vert != vertex
//						&& !visitedNeighbours.contains(vert)
//						&& !vertexPool.contains(vert)
////						&& !willBeFullySelected(vertexPool, triangle)
//						&& !sharesTwoTris(vert, vertex)
//						&& !willFullySelectedTri(vertexPool, vert)
//				){
//					validNeighbours.add(vert);
//					visitedNeighbours.add(vert);
//				} else if (vert != vertex && visitedNeighbours.contains(vert)){
//					validNeighbours.remove(vert);
//				}
//			}
//		}
		for(Triangle triangle : vertex.getTriangles()){
			for(GeosetVertex vert : triangle.getVerts()){
				if (!vertexPool.contains(vert)
						&& !sharesTwoTris(vert, vertex)
						&& !willFullySelectedTri(vertexPool, vert)
				) {
					validNeighbours.add(vert);
				}
			}
		}
		if(validNeighbours.isEmpty()){
			return null;
		} else {
			return validNeighbours.stream().findFirst().get();
		}
	}


	private boolean willFullySelectedTri(Set<GeosetVertex> vertexPool, GeosetVertex vertex) {
		for(Triangle triangle: vertex.getTriangles()){
			int selectedVerts = 0;
			for(GeosetVertex vert : triangle.getVerts()){
				if(vertexPool.contains(vert)){
					selectedVerts++;
				}
			}
			if(1 < selectedVerts){
				return true;
			}
		}
		return false;
	}
	private boolean sharesTwoTris(GeosetVertex vert, GeosetVertex vertex) {
		long count = vertex.getTriangles().stream().filter(triangle -> triangle.containsRef(vert)).count();
		return 1 < count;
	}
}
