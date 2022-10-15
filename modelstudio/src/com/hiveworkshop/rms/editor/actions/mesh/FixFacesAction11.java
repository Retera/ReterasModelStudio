package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class FixFacesAction11 implements UndoAction {

	private final Set<Triangle> trianglesToFlip;

	BiSetMap<Pair<GeosetVertex, GeosetVertex>, Triangle> edgeTriMap = new BiSetMap<>();

	BiSetMap<Pair<InexactHashVector, InexactHashVector>, Triangle> locEdgeTriMap = new BiSetMap<>();
	Map<Triangle, Set<Pair<InexactHashVector, InexactHashVector>>> triToLocEdges = new HashMap<>();
	Map<Pair<InexactHashVector, InexactHashVector>, TreeSet<Triangle>> locEdgeToTris = new HashMap<>();

	HashMap<Triangle, Integer[]> fixedTris1 = new HashMap<>();
	HashMap<Triangle, Integer[]> fixedTris2 = new HashMap<>();

	Map<Triangle, Set<Triangle>> triToStrip = new HashMap<>();

	Map<Pair<Set<Triangle>, Set<Triangle>>, Set<Pair<InexactHashVector, InexactHashVector>>> stripPairToEdges = new HashMap<>();
	Map<Set<Triangle>, Set<Set<Triangle>>> stripToConnectedStrips = new HashMap<>();

	Set<Set<Pair<InexactHashVector, InexactHashVector>>> stripEdges = new HashSet<>();

	Set<Set<Triangle>> fixedStrips = new HashSet<>();
	int prec = 100;

	public FixFacesAction11(Collection<Triangle> selectedTris){
		System.out.println("selected tris: " + selectedTris.size());

		fillEdgeMaps(selectedTris);
//		checkTriangles(new HashSet<>(triToEdges.keySet()));
		checkTriangles(new HashSet<>(edgeTriMap.getUKeySet()));

		collectLocEdgeTris();

		for(Pair<InexactHashVector, InexactHashVector> locEdge : locEdgeToTris.keySet()){
			TreeSet<Triangle> triangles = locEdgeToTris.get(locEdge);
			Set<Triangle> strip1 = triToStrip.get(triangles.first());
			Set<Triangle> strip2 = triToStrip.get(triangles.last());

			stripPairToEdges.computeIfAbsent(new Pair<>(strip1, strip2), k -> new HashSet<>()).add(locEdge);
			stripToConnectedStrips.computeIfAbsent(strip1, k -> new HashSet<>()).add(strip2);
			stripToConnectedStrips.computeIfAbsent(strip2, k -> new HashSet<>()).add(strip1);
		}

		fixNeighbourStrips(new HashSet<>(stripToConnectedStrips.keySet()), new HashSet<>());


//
//		Set<Triangle> notChecked2 = locEdgeToTris.values().stream()
//				.filter(tris -> tris.size() == 2)
//				.flatMap(Collection::stream)
//				.collect(Collectors.toSet());
//
//		checkLocTriangles(notChecked2);
//
////		Set<Pair<InexactHashVector, InexactHashVector>> notCheckedEdges = locEdgeToTris.keySet().stream()
////				.filter(locEdge -> locEdgeToTris.get(locEdge).size() == 2)
////				.collect(Collectors.toSet());
////		checkLocEdges(notCheckedEdges);




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

	private void fixNeighbourStrips(Set<Set<Triangle>> notFixedStrips, Set<Set<Triangle>> fixedStrips){
		if(!notFixedStrips.isEmpty()){
			System.out.println("fixNeighbourStrips, " + notFixedStrips.size() + " left");
			Set<Triangle> firstStrip = notFixedStrips.stream().findFirst().get();
			for(Triangle triangle : firstStrip){
				fixedTris2.put(triangle, fixedTris1.get(triangle));
			}
			notFixedStrips.remove(firstStrip);
			fixedStrips.add(firstStrip);


			fixLocConnectedStrips(firstStrip, fixedStrips);
			notFixedStrips.removeAll(fixedStrips);
			fixNeighbourStrips(notFixedStrips, fixedStrips);
		}
	}

	private void fixLocConnectedStrips(Set<Triangle> firstStrip, Set<Set<Triangle>> fixedStrips){
		for(Set<Triangle> neighbourStrip : stripToConnectedStrips.get(firstStrip)){
			if(!fixedStrips.contains(neighbourStrip)){
				Set<Pair<InexactHashVector, InexactHashVector>> locEdges = stripPairToEdges.get(new Pair<>(firstStrip, neighbourStrip));

				Pair<InexactHashVector, InexactHashVector> locEdge = locEdges.stream().findFirst().get();
				TreeSet<Triangle> triangles = locEdgeToTris.get(locEdge);
				boolean shouldFlipp;
				if(firstStrip.contains(triangles.first())){
					shouldFlipp = shouldFlippOrder(triangles.last(), triangles.first(), locEdge, fixedTris2, fixedTris1);
				} else {
					shouldFlipp = shouldFlippOrder(triangles.first(), triangles.last(), locEdge, fixedTris2, fixedTris1);
				}
				System.out.println("flip - " + shouldFlipp);
				fixStrip(neighbourStrip, shouldFlipp);
				fixedStrips.add(neighbourStrip);
			} else {
				System.out.println("already fixed!");
			}
		}

		for(Set<Triangle> neighbourStrip : stripToConnectedStrips.get(firstStrip)){
			if(!fixedStrips.containsAll(stripToConnectedStrips.get(neighbourStrip))){
				fixLocConnectedStrips(neighbourStrip, fixedStrips);
			}
		}

	}

//	private void checkLocEdges(Set<Pair<InexactHashVector, InexactHashVector>> notCheckedEdges){
//		if (!notCheckedEdges.isEmpty()){
//			Pair<InexactHashVector, InexactHashVector> startEdge = notCheckedEdges.stream().findFirst().get();
//
//			TreeSet<Triangle> triangles = locEdgeToTris.get(startEdge);
//			Triangle firstTri = triangles.first();
//			Set<Triangle> firstTriStrip = triToStrip.get(firstTri);
//			for (Triangle stripTri : firstTriStrip){
//				fixedTris2.put(stripTri, fixedTris1.get(stripTri));
//			}
//
//			Triangle lastTri = triangles.last();
//			boolean flippOrder = flippOrder(lastTri, firstTri, startEdge, fixedTris2);
//			Set<Triangle> lastTriStrip = triToStrip.get(lastTri);
//			for (Triangle stripTri : lastTriStrip){
//				if(flippOrder){
//					Integer[] order = fixedTris1.get(stripTri);
//					Integer[] newOrder = new Integer[]{order[0], order[2], order[1]};
//					fixedTris2.put(stripTri, newOrder);
//				} else {
//					fixedTris2.put(stripTri, fixedTris1.get(stripTri));
//				}
//			}
//
//
//
//			Set<Pair<InexactHashVector, InexactHashVector>> checkedEdges = new LinkedHashSet<>();
//
//			selectLinkedLocEdge(startEdge, checkedEdges);
//			notCheckedEdges.removeAll(checkedEdges);
//			checkLocEdges(notCheckedEdges);
//		}
//	}
//
//	private void selectLinkedLocEdge(Pair<InexactHashVector, InexactHashVector> currEdge, Set<Pair<InexactHashVector, InexactHashVector>> checkedEdges) {
//		checkedEdges.add(currEdge);
//		for(Triangle triangle : locEdgeToTris.get(currEdge)){
//			for(Pair<InexactHashVector, InexactHashVector> locEdge : triToLocEdges.get(triangle)){
//				if(!checkedEdges.contains(locEdge)) {
//
//				}
//			}
//		}
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
//						selectLinkedLocEdge(other, selection);
//					}
//				}
//			}
//		}
//	}

	private void collectLocEdgeTris() {
		for(Pair<GeosetVertex, GeosetVertex> edge : edgeTriMap.getTKeySet()){
			Set<Triangle> triangles = edgeTriMap.getByT(edge);
			if(triangles.size() == 1){
				Pair<InexactHashVector, InexactHashVector> locEdge = new Pair<>(new InexactHashVector(edge.getFirst(),  prec), new InexactHashVector(edge.getSecond(),  prec));
				locEdgeToTris.computeIfAbsent(locEdge, k -> new TreeSet<>(Comparator.comparing(Triangle::hashCode)))
						.addAll(triangles);

				edgeTriMap.getByT(edge)
						.forEach(tri -> triToLocEdges.computeIfAbsent(tri, k -> new HashSet<>()).add(locEdge));
			}
		}
//		for(Pair<GeosetVertex, GeosetVertex> edge : edgeToTris.keySet()){
//			Set<Triangle> triangles = edgeToTris.get(edge);
//			if(triangles.size() == 1){
//				Pair<InexactHashVector, InexactHashVector> locEdge = new Pair<>(new InexactHashVector(edge.getFirst(),  prec), new InexactHashVector(edge.getSecond(),  prec));
//				locEdgeToTris.computeIfAbsent(locEdge, k -> new TreeSet<>(Comparator.comparing(Triangle::hashCode)))
//						.addAll(triangles);
//
//				edgeToTris.get(edge)
//						.forEach(tri -> triToLocEdges.computeIfAbsent(tri, k -> new HashSet<>()).add(locEdge));
//			}
//		}
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
//		selection.add(currentTri);
		Set<Triangle> currTriStrip = triToStrip.get(currentTri);
		selection.addAll(currTriStrip);
		for(Triangle currStripTri : currTriStrip){
			Set<Pair<InexactHashVector, InexactHashVector>> locEdges = triToLocEdges.get(currStripTri);
			if(locEdges != null){
				for(Pair<InexactHashVector, InexactHashVector> locEdge : locEdges){
					TreeSet<Triangle> locEdgeTriangles = locEdgeToTris.get(locEdge);
					if(locEdgeTriangles.size() == 2){
						for (Triangle other : locEdgeTriangles) {
							if (!fixedTris2.containsKey(other)) {
								boolean flippOrder = shouldFlippOrder(other, currStripTri, locEdge, fixedTris2, fixedTris1);
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
								selectLinkedLoc(other, selection);
							}
						}
					}
				}
			}
		}

//		for(Triangle currStripTri : currTriStrip){
//			Set<Pair<InexactHashVector, InexactHashVector>> locEdges = triToLocEdges.get(currStripTri);
//			if(locEdges != null){
//				for(Pair<InexactHashVector, InexactHashVector> locEdge : locEdges){
//					TreeSet<Triangle> locEdgeTriangles = locEdgeToTris.get(locEdge);
//					if(locEdgeTriangles.size() == 2){
//						for (Triangle other : locEdgeTriangles) {
//							if (!selection.contains(other)) {
//								if (!fixedTris2.containsKey(other)) {
//									boolean flippOrder = flippOrder(other, currStripTri, locEdge, fixedTris2);
//									Set<Triangle> otherStrip = triToStrip.get(other);
//									fixStrip(otherStrip, flippOrder);
////									selection.add(stripTri);
//								}
//								selectLinkedLoc(other, selection);
//							}
//						}
//					}
//				}
//			}
//		}


//		for(Pair<InexactHashVector, InexactHashVector> locEdge : triToLocEdges.get(currentTri)){
//			TreeSet<Triangle> triangles = locEdgeToTris.get(locEdge);
//			if(triangles.size() == 2){
//				for (Triangle other : triangles) {
//					if (!selection.contains(other)) {
//						if (!fixedTris2.containsKey(other)) {
//							boolean flippOrder = flippOrder(other, currentTri, locEdge, fixedTris2);
//							Set<Triangle> otherStrip = triToStrip.get(other);
//							fixStrip(otherStrip, flippOrder);
//
//						}
//						selectLinkedLoc(other, selection);
//					}
//				}
//			}
//		}
	}

	private void fixStrip(Set<Triangle> otherStrip, boolean flippOrder) {
		for(Triangle stripTri : otherStrip){
			Integer[] newOrder;
			if(flippOrder){
				Integer[] order = fixedTris1.get(stripTri);
				newOrder = new Integer[]{order[0], order[2], order[1]};
			} else {
				newOrder = fixedTris1.get(stripTri);
			}
			fixedTris2.put(stripTri, newOrder);
		}
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
		for (Pair<GeosetVertex, GeosetVertex> edge : edgeTriMap.getByU(currentTri)) {
			for (Triangle other : edgeTriMap.getByT(edge)) {
				if (!selection.contains(other)) {
					Integer[] newOrder = getNewOrder(other, currentTri, edge, fixedTris1);
					fixedTris1.put(other, newOrder);
					selectLinked(other, selection);
				}
			}
		}
	}

	private void fillEdgeMaps(Collection<Triangle> selectedTris){
		for(Triangle triangle : selectedTris){
			for(int i = 0; i<3; i++){
				Pair<GeosetVertex, GeosetVertex> edge = new Pair<>(triangle.get(i), triangle.get((i+1)%3));
				edgeTriMap.computeIfAbsent(edge, triangle);
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


	private boolean shouldFlippOrder(Triangle tri, Triangle fixedTri,
	                                 Pair<InexactHashVector, InexactHashVector> edge,
	                                 HashMap<Triangle, Integer[]> fixedTris2,
	                                 HashMap<Triangle, Integer[]> fixedTris1) {
		Integer[] preFixedOrder = fixedTris1.get(tri);
		Integer[] fixedOrder = fixedTris2.get(fixedTri);

		int t_e1 = preFixedOrder[getIndex(tri, edge.getFirst())];
		int t_index = preFixedOrder[getIndex(tri, edge.getSecond())];
		int t_e2 = (3 + t_index -t_e1)%3;

		int n_e1 = fixedOrder[getIndex(fixedTri, edge.getFirst())];
		int n_index = getIndex(fixedTri, edge.getSecond());
		int n_e2 = (3+fixedOrder[n_index]-n_e1)%3;
//		System.out.println("fixedTri: " + t_e1);
		return t_e2 == n_e2;
	}

	private int getIndex(Triangle tri, InexactHashVector first) {
		for(int i = 0; i<1; i++){
			if(new InexactHashVector(tri.get(i), prec).equals(first)){
				return i;
			}
		}
		return 0;
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

	private static class BiSetMap <T, U>{
		Map<T, Set<U>> t_to_u = new HashMap<>();
		Map<U, Set<T>> u_to_t = new HashMap<>();
		BiSetMap(){
		}

		void computeIfAbsent(T value1, U value2){
			t_to_u.computeIfAbsent(value1, k -> new HashSet<>()).add(value2);
			u_to_t.computeIfAbsent(value2, k -> new HashSet<>()).add(value1);
		}

		Set<T> getByU(U value){
			return u_to_t.get(value);
		}
		Set<U> getByT(T value){
			return t_to_u.get(value);
		}

		public Map<T, Set<U>> getTToU() {
			return t_to_u;
		}

		public Map<U, Set<T>> getUToT() {
			return u_to_t;
		}

		Set<U> getUKeySet(){
			return u_to_t.keySet();
		}
		Set<T> getTKeySet(){
			return t_to_u.keySet();
		}
	}
}
