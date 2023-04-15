package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class FixFacesAction implements UndoAction {

	private final Set<Triangle> trianglesToFlip;

	private final Map<Pair<GeosetVertex, GeosetVertex>, Set<Triangle>> edgeToTris = new HashMap<>();
	private final Map<Triangle, Set<Pair<GeosetVertex, GeosetVertex>>> triToEdges = new HashMap<>();

	private final Map<Triangle, Set<Pair<InexactHashVector, InexactHashVector>>> triToLocEdges = new HashMap<>();
	private final Map<Pair<InexactHashVector, InexactHashVector>, TreeSet<Triangle>> locEdgeToTris = new HashMap<>();

	private final HashMap<Triangle, Integer[]> fixedInStripTris = new HashMap<>();
	private final HashMap<Triangle, Integer[]> fixedBetwStripsTris = new HashMap<>();

	private final Map<Triangle, Set<Triangle>> triToStrip = new HashMap<>();


	private final int prec = 10000;

	public FixFacesAction(Collection<Triangle> selectedTris){
		fillEdgeMaps(selectedTris);
		fixTriangles(new HashSet<>(triToEdges.keySet()));

		collectLocEdgeTris();

		Set<Triangle> notChecked2 = locEdgeToTris.values().stream()
				.filter(tris -> tris.size() == 2)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		fixNeighbourStrips(notChecked2);


		for(Triangle triangle : fixedInStripTris.keySet()){
			if(!fixedBetwStripsTris.containsKey(triangle)){
				fixedBetwStripsTris.put(triangle, fixedInStripTris.get(triangle));
			}
		}

		trianglesToFlip = new HashSet<>();
		for(Triangle triangle : fixedBetwStripsTris.keySet()){
			Integer[] newIndexes = fixedBetwStripsTris.get(triangle);
			if(newIndexes[1] != 1){
				trianglesToFlip.add(triangle);
			}
		}
	}

	private void collectLocEdgeTris() {
		for(Pair<GeosetVertex, GeosetVertex> edge : edgeToTris.keySet()){
			Set<Triangle> triangles = edgeToTris.get(edge);
			if(triangles.size() == 1){
				Pair<InexactHashVector, InexactHashVector> locEdge = new Pair<>(new InexactHashVector(edge.getFirst(),  prec), new InexactHashVector(edge.getSecond(),  prec));
				locEdgeToTris.computeIfAbsent(locEdge, k -> new TreeSet<>(Comparator.comparing(Triangle::hashCode)))
						.addAll(triangles);

				edgeToTris.get(edge)
						.forEach(tri -> triToLocEdges.computeIfAbsent(tri, k -> new HashSet<>()).add(locEdge));
			}
		}
	}

	private void fixNeighbourStrips(Set<Triangle> notChecked2){
		if (!notChecked2.isEmpty()){
			Triangle startPoint = notChecked2.stream().findFirst().get();
			for(Triangle stripTri : triToStrip.get(startPoint)){
				fixedBetwStripsTris.put(stripTri, fixedInStripTris.get(stripTri));
				notChecked2.remove(stripTri);
			}

			Set<Triangle> strip = new LinkedHashSet<>();

			fixLocLinkedStrips(startPoint, strip);
			notChecked2.removeAll(strip);
			fixNeighbourStrips(notChecked2);
		}
	}

	private void fixLocLinkedStrips(Triangle currentTri, Set<Triangle> selection) {
		Set<Triangle> currTriStrip = triToStrip.get(currentTri);
		selection.addAll(currTriStrip);
		for(Triangle currStripTri : currTriStrip){
			Set<Pair<InexactHashVector, InexactHashVector>> locEdges = triToLocEdges.get(currStripTri);
			if(locEdges != null){
				for(Pair<InexactHashVector, InexactHashVector> locEdge : locEdges){
					TreeSet<Triangle> locEdgeTriangles = locEdgeToTris.get(locEdge);
					if(locEdgeTriangles.size() == 2){
						for (Triangle other : locEdgeTriangles) {
							if (!fixedBetwStripsTris.containsKey(other)) {
								boolean flippOrder = shouldFlipOrder(locEdge, other, fixedInStripTris, currStripTri, fixedBetwStripsTris);
								Set<Triangle> otherStrip = triToStrip.get(other);
								fixStrip(otherStrip, flippOrder);
							}
						}
					}
				}
			}
		}
		for(Triangle currStripTri : currTriStrip){
			Set<Pair<InexactHashVector, InexactHashVector>> locEdges = triToLocEdges.get(currStripTri);
			if(locEdges != null){
				for(Pair<InexactHashVector, InexactHashVector> locEdge : locEdges){
					TreeSet<Triangle> locEdgeTriangles = locEdgeToTris.get(locEdge);
					if(locEdgeTriangles.size() == 2){
						for (Triangle other : locEdgeTriangles) {
							if (!selection.contains(other)) {
								fixLocLinkedStrips(other, selection);
							}
						}
					}
				}
			}
		}
	}

	private void fixStrip(Set<Triangle> otherStrip, boolean flippOrder) {
		for(Triangle stripTri : otherStrip){
			Integer[] newOrder;
			if(flippOrder){
				Integer[] order = fixedInStripTris.get(stripTri);
				newOrder = new Integer[]{order[0], order[2], order[1]};
			} else {
				newOrder = fixedInStripTris.get(stripTri);
			}
			fixedBetwStripsTris.put(stripTri, newOrder);
		}

//		if(flippOrder){
//			for(Triangle stripTri : otherStrip){
//				Integer[] order = fixedInStripTris.get(stripTri);
//				fixedBetwStripsTris.put(stripTri, new Integer[]{order[0], order[2], order[1]});
//			}
//		} else {
//			for(Triangle stripTri : otherStrip){
//				fixedBetwStripsTris.put(stripTri, fixedInStripTris.get(stripTri));
//			}
//		}
	}

	private void fixTriangles(Set<Triangle> notChecked){
		if (!notChecked.isEmpty()){
			Triangle startPoint = notChecked.stream().findFirst().get();
			Set<Triangle> strip = new LinkedHashSet<>();
			fixedInStripTris.put(startPoint, new Integer[]{0, 1, 2});

			fixLinkedTris(startPoint, strip);
			strip.forEach(t -> triToStrip.put(t, strip));
			notChecked.removeAll(strip);
			fixTriangles(notChecked);
		}
	}

	private void fixLinkedTris(Triangle currentTri, Set<Triangle> selection) {
		selection.add(currentTri);
		for (Pair<GeosetVertex, GeosetVertex> edge : triToEdges.get(currentTri)) {
			for (Triangle other : edgeToTris.get(edge)) {
				if (!selection.contains(other)) {
					Integer[] newOrder = getNewOrder(other, currentTri, edge, fixedInStripTris);
					fixedInStripTris.put(other, newOrder);
					fixLinkedTris(other, selection);
				}
			}
		}
	}


	private void fillEdgeMaps(Collection<Triangle> selectedTris){
		for(Triangle triangle : selectedTris){
			for(int i = 0; i<3; i++){
				Pair<GeosetVertex, GeosetVertex> edge = new Pair<>(triangle.get(i), triangle.get((i+1)%3));
				edgeToTris.computeIfAbsent(edge, k -> new HashSet<>()).add(triangle);
				triToEdges.computeIfAbsent(triangle, k -> new HashSet<>()).add(edge);
			}
		}
	}


	@Override
	public UndoAction undo() {
		for(Triangle triangle : trianglesToFlip){
			triangle.flip(true);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(Triangle triangle : trianglesToFlip){
			triangle.flip(true);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Flipped " + trianglesToFlip.size() + " Faces";
	}


	private Integer[] getNewOrder(Triangle tri,
	                              Triangle fixedTri,
	                              Pair<GeosetVertex, GeosetVertex> edge,
	                              HashMap<Triangle, Integer[]> fixedInStripTris) {

		int t_e1 = tri.indexOf(edge.getFirst());
		int t_e2 = (3+ tri.indexOf(edge.getSecond())-t_e1)%3;

		Integer[] order = fixedInStripTris.get(fixedTri);
		int n_e1 = order[fixedTri.indexOf(edge.getFirst())];
		int n_e2 = (3+order[fixedTri.indexOf(edge.getSecond())]-n_e1)%3;

		if(t_e2 == n_e2){
			return new Integer[]{0,2, 1};
		} else {
			return new Integer[]{0, 1, 2};
		}
	}
	private boolean shouldFlipOrder(Pair<GeosetVertex, GeosetVertex> edge,
	                                Triangle tri,
	                                Triangle fixedTri,
	                                HashMap<Triangle, Integer[]> fixedInStripTris) {

		int t_e1 = tri.indexOf(edge.getFirst());
		int t_e2 = (3 + tri.indexOf(edge.getSecond())-t_e1) % 3;

		Integer[] order = fixedInStripTris.get(fixedTri);
		int n_e1 = order[fixedTri.indexOf(edge.getFirst())];
		int n_e2 = (3 + order[fixedTri.indexOf(edge.getSecond())]-n_e1) % 3;

		return t_e2 == n_e2;
	}


	private boolean shouldFlipOrder(Pair<InexactHashVector, InexactHashVector> edge,
	                                Triangle tri,
	                                HashMap<Triangle, Integer[]> fixedTris1,
	                                Triangle fixedTri,
	                                HashMap<Triangle, Integer[]> fixedTris2) {
		Integer[] t_order = fixedTris1.get(fixedTri);
		int t_i1 = getIndex(tri, edge.getFirst());
		int t_i2 = getIndex(tri, edge.getSecond());
		int t_e1 = t_order[t_i1];
		int t_e2 = (3 + t_order[t_i2]-t_e1)%3;

		Integer[] fixed_tri_order = fixedTris2.get(fixedTri);
		int n_i1 = getIndex(fixedTri, edge.getFirst());
		int n_i2 = getIndex(fixedTri, edge.getSecond());
		int n_e1 = fixed_tri_order[n_i1];
		int n_e2 = (3+fixed_tri_order[n_i2]-n_e1)%3;

		return t_e2 == n_e2;
	}

	private int getIndex(Triangle tri, InexactHashVector locVert) {
		for(int i = 0; i<3; i++){
			if(new InexactHashVector(tri.get(i), prec).equals(locVert)){
				return i;
			}
		}
		return 2;
	}
}
