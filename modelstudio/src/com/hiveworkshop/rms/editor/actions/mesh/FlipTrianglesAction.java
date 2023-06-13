package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class FlipTrianglesAction implements UndoAction {
	private final List<Triangle> oldTriangles = new ArrayList<>();
	private final List<Triangle> newTriangles = new ArrayList<>();
	private final Vec3 tempV = new Vec3();
	private final Vec3 tempN = new Vec3();

	public FlipTrianglesAction(Set<Triangle> selectedTriangles){
		if (!selectedTriangles.isEmpty()) {
			List<Set<Triangle>> strips = getStrips(selectedTriangles);
			Vec3 tempOrgN = new Vec3();
			for (Set<Triangle> strip : strips){
				List<Triangle[]> triPairs = getTriPairs(strip);
				for (Triangle[] triPair : triPairs){
					GeosetVertex[][] edges = getEdges(triPair[0], triPair[1]);
					if (edges != null) {
						oldTriangles.add(triPair[0]);
						oldTriangles.add(triPair[1]);
						tempOrgN.set(getTriNormal(triPair[0]));
						Triangle[] newTris = getNewTris(edges[0], edges[1], tempOrgN, triPair[0].getGeoset());
						newTriangles.add(newTris[0]);
						newTriangles.add(newTris[1]);
					}
				}
			}
		}
	}

	public int getNewTriCount(){
		return newTriangles.size();
	}

	private Triangle[] getNewTris(GeosetVertex[] commonEdge, GeosetVertex[] newEdge, Vec3 orgTriNorm, Geoset geoset){
		Triangle tri1 = new Triangle(newEdge[0], commonEdge[1], newEdge[1], geoset);
		Triangle tri2 = new Triangle(newEdge[0], newEdge[1], commonEdge[0], geoset);
		// ensure the new triangle are facing the same way as the old ones
		if (orgTriNorm.dot(getTriNormal(tri1)) < 0) tri1.flip(false);
		if (orgTriNorm.dot(getTriNormal(tri2)) < 0) tri2.flip(false);
		return new Triangle[] {tri1, tri2};
	}

	private Vec3 getTriNormal(Triangle triangle){
		tempN.set(triangle.get(1)).sub(triangle.get(0));
		tempV.set(triangle.get(2)).sub(triangle.get(1));
		return tempN.cross(tempV);
	}

	private List<Triangle[]> getTriPairs(Set<Triangle> strip){
		List<Triangle[]> triPairs = new ArrayList<>();
		Map<Triangle, Set<Triangle>> triToNeighbours = getTriToNeighbours(strip);

		for (int i = 0; i < (strip.size() * 10) && !triToNeighbours.isEmpty(); i++) {
			Triangle tri1 = getTriOnEdgeOrFirst(triToNeighbours);
			if (tri1 != null){
				Triangle tri2 = getFirstNeighbour(triToNeighbours, tri1);

				removeFromOthers(triToNeighbours, tri1);
				removeFromOthers(triToNeighbours, tri2);

				if(tri2 != null){
					triPairs.add(new Triangle[]{tri1, tri2});
				}
			}
		}
		return triPairs;
	}

	private Triangle getTriOnEdgeOrFirst(Map<Triangle, Set<Triangle>> triToNeighbour) {
		for (Triangle triangle : triToNeighbour.keySet()){
			if(triToNeighbour.get(triangle).size() == 1){
				return triangle;
			}
		}
		return triToNeighbour.keySet().stream().findFirst().orElse(null);
	}

	private Triangle getFirstNeighbour(Map<Triangle, Set<Triangle>> triToNeighbours, Triangle currTri) {
		Set<Triangle> currNeighbours = triToNeighbours.get(currTri);
		if(currNeighbours != null && !currNeighbours.isEmpty()){
			return currNeighbours.stream().findFirst().get();
		}
		return null;
	}

	private void removeFromOthers(Map<Triangle, Set<Triangle>> triToNeighbours, Triangle currTri) {
		Set<Triangle> currNeighbours2 = triToNeighbours.remove(currTri);
		if(currNeighbours2 != null){
			for(Triangle t : currNeighbours2){
				triToNeighbours.get(t).remove(currTri);
			}
		}
	}

	private Map<Triangle, Set<Triangle>> getTriToNeighbours(Set<Triangle> strip){
		Map<Triangle, Set<Triangle>> triToNeighbours = new LinkedHashMap<>();
		for(Triangle tri : strip){
			Set<Triangle> neighbours = new HashSet<>();
			for (GeosetVertex vertex : tri.getVerts()) {
				for (Triangle neighbourTri : vertex.getTriangles()){
					if(neighbourTri != tri && strip.contains(neighbourTri) && shareOneEdge(tri, neighbourTri)){
						neighbours.add(neighbourTri);
					}
				}
			}
			triToNeighbours.put(tri, neighbours);
		}
		return triToNeighbours;
	}


	private List<Set<Triangle>> getStrips(Set<Triangle> selectedTris){
		Set<Triangle> sortedTris = new HashSet<>();
		List<Set<Triangle>> triStrips = new ArrayList<>();
		for(Triangle tri : selectedTris){
			if(!sortedTris.contains(tri)){
				Set<Triangle> currentStrip = new LinkedHashSet<>();
				collectConnected(tri, currentStrip, sortedTris, selectedTris);
				triStrips.add(currentStrip);
			}
		}
		return triStrips;
	}

	private void collectConnected(Triangle triToCheck, Set<Triangle> currentStrip, Set<Triangle> sortedTris, Set<Triangle> selectedTris){
		currentStrip.add(triToCheck);
		sortedTris.add(triToCheck);
		for (GeosetVertex vertex : triToCheck.getVerts()){
			for (Triangle neighbourTri : vertex.getTriangles()){
				if(neighbourTri != triToCheck
						&& !sortedTris.contains(neighbourTri)
						&& selectedTris.contains(neighbourTri)
						&& shareOneEdge(triToCheck, neighbourTri)) {
					collectConnected(neighbourTri, currentStrip, sortedTris, selectedTris);
				}
			}
		}
	}

	private boolean shareOneEdge(Triangle tri1, Triangle tri2){
		int commonVerts = 0;
		for(GeosetVertex vertex : tri2.getVerts()){
			if(tri1.containsRef(vertex)){
				commonVerts++;
			}
		}
		return commonVerts == 2;
	}

	private GeosetVertex[][] getEdges(Triangle tri1, Triangle tri2) {
		GeosetVertex[] commonEdge = new GeosetVertex[2];
		GeosetVertex[] newEdge = new GeosetVertex[2];
		int ceI = 0;
		int neI = 0;
		for(GeosetVertex geosetVertex : tri1.getVerts()){
			if (tri2.containsRef(geosetVertex)){
				commonEdge[ceI++] = geosetVertex;
			} else {
				newEdge[neI++] = geosetVertex;
			}
		}

		for(GeosetVertex geosetVertex : tri2.getVerts()){
			if (!tri1.containsRef(geosetVertex)){
				newEdge[neI++] = geosetVertex;
				break;
			}
		}

		if(ceI == 2 && neI == 2){
			return new GeosetVertex[][]{commonEdge, newEdge};
		}

		return null;
	}

	private GeosetVertex[][] getEdges2(Triangle tri1, Triangle tri2) {
		GeosetVertex[] commonEdge = new GeosetVertex[2];
		GeosetVertex[] newEdge = new GeosetVertex[2];
		int ceI = 0;
		int index = 3;
		for(GeosetVertex geosetVertex : tri1.getVerts()){
			int i = tri2.indexOf(geosetVertex);
			if (i != -1){
				commonEdge[ceI++] = geosetVertex;
				index -= i;
			} else {
				newEdge[0] = geosetVertex;
			}
		}
		newEdge[1] = tri2.get(index);

		if(ceI == 2){
			return new GeosetVertex[][]{commonEdge, newEdge};
		}

		return null;
	}

	@Override
	public FlipTrianglesAction undo() {
		for (Triangle triangle : newTriangles) {
			triangle.getGeoset().remove(triangle);
			for (GeosetVertex geosetVertex : triangle.getVerts()) {
				geosetVertex.removeTriangle(triangle);
			}
		}
		for (Triangle t : oldTriangles) {
			t.getGeoset().addTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.addTriangle(t);
			}
		}
		return this;
	}

	@Override
	public FlipTrianglesAction redo() {
		for (Triangle t : oldTriangles) {
			t.getGeoset().removeTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.removeTriangle(t);
			}
		}

		for (Triangle triangle : newTriangles) {
			triangle.getGeoset().add(triangle);
			for (GeosetVertex geosetVertex : triangle.getVerts()) {
				if (!geosetVertex.hasTriangle(triangle)) {
					geosetVertex.addTriangle(triangle);
				}
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Flip Triangles";
	}
}
