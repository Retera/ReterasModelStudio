package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class FixFacesAction1 implements UndoAction {

	private final Set<Triangle> trianglesToFlip;

	Map<Pair<GeosetVertex, GeosetVertex>, Set<Triangle>> edgeToTris = new HashMap<>();
	Map<Pair<InexactHashVector, InexactHashVector>, TreeSet<Triangle>> locEdgeToTris = new HashMap<>();

	Map<Triangle, Set<Pair<InexactHashVector, InexactHashVector>>> triToLocEdges = new HashMap<>();
	Map<Triangle, Set<Pair<GeosetVertex, GeosetVertex>>> triToEdges = new HashMap<>();

	HashMap<Triangle, Integer[]> fixedTris1 = new HashMap<>();
	HashMap<Triangle, Integer[]> fixedTris2 = new HashMap<>();

	Map<Triangle, Set<Triangle>> triToStrip = new HashMap<>();


	Set<Set<Pair<InexactHashVector, InexactHashVector>>> stripEdges = new HashSet<>();

	public FixFacesAction1(Collection<Triangle> selectedTris){
		System.out.println("selected tris: " + selectedTris.size());

		fillEdgeMaps(selectedTris);
		checkTriangles(new HashSet<>(triToEdges.keySet()));

		collectLocEdgeTris();



		Set<Triangle> notChecked2 = locEdgeToTris.values().stream()
				.filter(tris -> tris.size() == 2)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		checkLocTriangles(notChecked2);

		for(Triangle triangle : fixedTris1.keySet()){
			if(!fixedTris2.containsKey(triangle)){
				fixedTris2.put(triangle, fixedTris1.get(triangle));
			}
		}

		trianglesToFlip = new HashSet<>();
		for(Triangle triangle : fixedTris2.keySet()){
			Integer[] newIndexes = fixedTris2.get(triangle);
			if(newIndexes[1] != 1){
				trianglesToFlip.add(triangle);
			}
		}
	}

	private void collectLocEdgeTris() {
		for(Pair<GeosetVertex, GeosetVertex> edge : edgeToTris.keySet()){
			Set<Triangle> triangles = edgeToTris.get(edge);
			if(triangles.size() == 1){
				Pair<InexactHashVector, InexactHashVector> locEdge = new Pair<>(new InexactHashVector(edge.getFirst()), new InexactHashVector(edge.getSecond()));
				locEdgeToTris.computeIfAbsent(locEdge, k -> new TreeSet<>(Comparator.comparing(Triangle::hashCode)))
						.addAll(triangles);

				edgeToTris.get(edge)
						.forEach(tri -> triToLocEdges.computeIfAbsent(tri, k -> new HashSet<>()).add(locEdge));
			}
		}
	}

	private void checkLocTriangles(Set<Triangle> notChecked2){
		if (!notChecked2.isEmpty()){
			Triangle startPoint = notChecked2.stream().findFirst().get();
			System.out.println("checkLocTriangles, " + notChecked2.size() + " left");
			for(Triangle stripTri : triToStrip.get(startPoint)){
				fixedTris2.put(stripTri, fixedTris1.get(stripTri));
				notChecked2.remove(stripTri);
			}

			Set<Triangle> strip = new LinkedHashSet<>();

			selectLinkedLoc(startPoint, strip);
			notChecked2.removeAll(strip);
			checkLocTriangles(notChecked2);
		}
	}

	private void selectLinkedLoc(Triangle currentTri, Set<Triangle> selection) {
		selection.add(currentTri);
		for(Pair<InexactHashVector, InexactHashVector> locEdge : triToLocEdges.get(currentTri)){
			TreeSet<Triangle> triangles = locEdgeToTris.get(locEdge);
			if(triangles.size() == 2){
				for (Triangle other : triangles) {
					if (!selection.contains(other)) {
						if (!fixedTris2.containsKey(other)) {
							boolean flippOrder = flippOrder(other, currentTri, locEdge, fixedTris2);
							for(Triangle stripTri : triToStrip.get(other)){
								Integer[] newOrder;
								if(flippOrder){
									Integer[] order = fixedTris1.get(stripTri);
									newOrder = new Integer[]{order[0], order[2], order[1]};
								} else {
									newOrder = fixedTris1.get(stripTri);
								}
								fixedTris2.put(stripTri, newOrder);
//								selection.add(stripTri);
							}
						}
						selectLinkedLoc(other, selection);
					}
				}
			}
		}
	}
//	private void checkLocEdges(Set<Pair<InexactHashVector, InexactHashVector>> notChecked2){
//		if (!notChecked2.isEmpty()){
//			Pair<InexactHashVector, InexactHashVector> startEdge = notChecked2.stream().findFirst().get();
//			System.out.println("checkLocTriangles, " + notChecked2.size() + " left");
//			for(Triangle stripTri : triToStrip.get(startPoint)){
//				fixedTris2.put(stripTri, fixedTris1.get(stripTri));
//				notChecked2.remove(stripTri);
//			}
//
//
//			Set<Triangle> strip = new LinkedHashSet<>();
//
//			selectLinkedLoc(startPoint, strip);
//			notChecked2.removeAll(strip);
//			checkLocTriangles(notChecked2);
//		}
//	}

//	private void selectLinkedLoc(Triangle currentTri, Set<Triangle> selection) {
//		selection.add(currentTri);
//		for(Pair<InexactHashVector, InexactHashVector> locEdge : triToLocEdges.get(currentTri)){
//			TreeSet<Triangle> triangles = locEdgeToTris.get(locEdge);
//			if(triangles.size() == 2){
//				for (Triangle other : triangles) {
//					if (!selection.contains(other)) {
//						boolean flippOrder = flippOrder(other, currentTri, locEdge, fixedTris2);
//						for(Triangle stripTri : triToStrip.get(other)){
//							Integer[] newOrder;
//							if(flippOrder){
//								Integer[] order = fixedTris1.get(stripTri);
//								newOrder = new Integer[]{order[0], order[2], order[1]};
//							} else {
//								newOrder = fixedTris1.get(stripTri);
//							}
//							fixedTris2.put(stripTri, newOrder);
//							selection.add(stripTri);
//						}
//						selectLinkedLoc(other, selection);
//					}
//				}
//			}
//		}
//	}

	private boolean flippOrder(Triangle tri, Triangle fixedTri, Pair<InexactHashVector, InexactHashVector> edge, HashMap<Triangle, Integer[]> fixedTris) {
		Integer[] order = fixedTris.get(fixedTri);

		int t_e1 = getIndex(tri, edge.getFirst());
		int t_e2 = (3 + getIndex(tri, edge.getSecond())-t_e1)%3;

		int n_e1 = order[getIndex(fixedTri, edge.getFirst())];
		int n_e2 = (3+order[getIndex(fixedTri, edge.getSecond())]-n_e1)%3;

		return t_e2 == n_e2;
	}

	private int getIndex(Triangle tri, InexactHashVector first) {
		for(int i = 0; i<1; i++){
			if(new InexactHashVector(tri.get(i)).equals(first)){
				return i;
			}
		}
		return 0;
	}

	private void checkTriangles(Set<Triangle> notChecked){
		if (!notChecked.isEmpty()){
			Triangle startPoint = notChecked.stream().findFirst().get();
			System.out.println("checkTriangles, " + notChecked.size() + " left");
			Set<Triangle> strip = new LinkedHashSet<>();
			fixedTris1.put(startPoint, new Integer[]{0, 1, 2});

			selectLinked(startPoint, strip);
			strip.forEach(t -> triToStrip.put(t, strip));
			notChecked.removeAll(strip);
			checkTriangles(notChecked);
		}
	}

	private void selectLinked(Triangle currentTri, Set<Triangle> selection) {
		selection.add(currentTri);
		for (Pair<GeosetVertex, GeosetVertex> edge : triToEdges.get(currentTri)) {
			for (Triangle other : edgeToTris.get(edge)) {
				if (!selection.contains(other)) {
					Integer[] newOrder = getNewOrder(other, currentTri, edge, fixedTris1);
					fixedTris1.put(other, newOrder);
					selectLinked(other, selection);
				}
			}
		}
	}

	private Integer[] getNewOrder(Triangle tri, Triangle fixedTri, Pair<GeosetVertex, GeosetVertex> edge, HashMap<Triangle, Integer[]> fixedTris1) {
		Integer[] order = fixedTris1.get(fixedTri);

		int t_e1 = tri.indexOf(edge.getFirst());
		int t_e2 = (3+ tri.indexOf(edge.getSecond())-t_e1)%3;

		int n_e1 = order[fixedTri.indexOf(edge.getFirst())];
		int n_e2 = (3+order[fixedTri.indexOf(edge.getSecond())]-n_e1)%3;

		Integer[] newOrder;
		if(t_e2 == n_e2){
			newOrder = new Integer[]{0,2, 1};
		} else {
			newOrder = new Integer[]{0, 1, 2};
		}
		return newOrder;
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
		return "fix " + trianglesToFlip.size() + " faces";
	}
}
