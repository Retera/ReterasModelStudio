package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class FixFaceStripAction implements UndoAction {

	private final Set<Triangle> trianglesToFlip;

	private BiSetMap<Pair<GeosetVertex, GeosetVertex>, Triangle> edgeTriMap;
	private HashMap<Triangle, Integer[]> fixedTris1 = new HashMap<>();

	public FixFaceStripAction(Collection<Triangle> selectedTris){
		System.out.println("selected tris: " + selectedTris.size());

		edgeTriMap = getEdgeMap(selectedTris);
		checkTriangles(new HashSet<>(edgeTriMap.getUKeySet()), fixedTris1);


		trianglesToFlip = new HashSet<>();
		for(Triangle triangle : fixedTris1.keySet()){
			Integer[] newIndexes = fixedTris1.get(triangle);
			if(newIndexes[1] != 1){
				trianglesToFlip.add(triangle);
			}
		}
	}

	private void checkTriangles(Set<Triangle> notChecked, HashMap<Triangle, Integer[]> fixedTris1){
		if (!notChecked.isEmpty()){
			Triangle startPoint = notChecked.stream().findFirst().get();
			System.out.println("checkTriangles, " + notChecked.size() + " left");
			fixedTris1.put(startPoint, new Integer[]{0, 1, 2});

			fixLinked(startPoint, new Integer[]{0, 1, 2}, fixedTris1);
			notChecked.removeAll(fixedTris1.keySet());
			checkTriangles(notChecked, fixedTris1);
		}
	}

	private void fixLinked(Triangle currentTri, Integer[] newOrder, HashMap<Triangle, Integer[]> fixedTris1) {
		fixedTris1.put(currentTri, newOrder);
		for (Pair<GeosetVertex, GeosetVertex> edge : edgeTriMap.getByU(currentTri)) {
			for (Triangle other : edgeTriMap.getByT(edge)) {
				if (!fixedTris1.containsKey(other)) {
					fixLinked(other, getNewOrder(other, currentTri, edge, fixedTris1), fixedTris1);
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


	private BiSetMap<Pair<GeosetVertex, GeosetVertex>, Triangle> getEdgeMap(Collection<Triangle> selectedTris){
		BiSetMap<Pair<GeosetVertex, GeosetVertex>, Triangle> edgeTriMap = new BiSetMap<>();
		for(Triangle triangle : selectedTris){
			for(int i = 0; i<3; i++){
				Pair<GeosetVertex, GeosetVertex> edge = new Pair<>(triangle.get(i), triangle.get((i+1)%3));
				edgeTriMap.computeIfAbsent(edge, triangle);
			}
		}
		return edgeTriMap;
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
