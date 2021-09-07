package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class VertexClusterDefinitions {
	private final Map<Geoset, Map<Integer, Set<GeosetVertex>>> geosetClusterIdMap = new HashMap<>();

	public VertexClusterDefinitions() {
	}

	public Set<GeosetVertex> getVertexBundle(GeosetVertex vertex) {
		Map<Integer, Set<GeosetVertex>> idMap = geosetClusterIdMap.computeIfAbsent(vertex.getGeoset(), k -> getConnectedGroups1(vertex.getGeoset()));
		Set<GeosetVertex> geosetVertCluster = getGeosetVertCluster(vertex, idMap);

		if (geosetVertCluster == null) { //
			Map<Integer, Set<GeosetVertex>> idMap2 = getConnectedGroups1(vertex.getGeoset());
			geosetClusterIdMap.put(vertex.getGeoset(), idMap2);
			geosetVertCluster = getGeosetVertCluster(vertex, idMap);
		}
		if (geosetVertCluster == null) {
			return Collections.emptySet();
		}
		return geosetVertCluster;
	}

	private Set<GeosetVertex> getGeosetVertCluster(GeosetVertex vertex, Map<Integer, Set<GeosetVertex>> idMap) {
		for (Integer clusterId : idMap.keySet()) {
			if (idMap.get(clusterId).contains(vertex)) {
				return idMap.get(clusterId);
			}
		}
		return null;
	}


	private Map<Integer, Set<GeosetVertex>> getConnectedGroups1(Geoset geoset) {
		Set<GeosetVertex> allCheckedVerts = new HashSet<>();
		List<Pair<Set<Integer>, Set<GeosetVertex>>> pairList = new ArrayList<>();
		for (GeosetVertex vertex : geoset.getVertices()) {
			if (!allCheckedVerts.contains(vertex)) {
				Pair<Set<Integer>, Set<GeosetVertex>> connectedGroup = getConnectedGroup(vertex);
				pairList.add(connectedGroup);
				allCheckedVerts.addAll(connectedGroup.getSecond());
			}
		}
		Map<Integer, Set<GeosetVertex>> clusterIdMap = new HashMap<>();

		Set<Integer> checkedIndexes = new HashSet<>();
		for (int i = 0; i < pairList.size(); i++) {
			if (!checkedIndexes.contains(i)) {
				checkedIndexes.add(i);
				Pair<Set<Integer>, Set<GeosetVertex>> group1 = pairList.get(i);

				Set<Integer> posIdSet = new HashSet<>(group1.getFirst());
				Set<GeosetVertex> vertIdSet = new HashSet<>(group1.getSecond());

				clusterIdMap.put(clusterIdMap.size(), vertIdSet);

				for (int j = i + 1; j < pairList.size(); j++) {
					Pair<Set<Integer>, Set<GeosetVertex>> group2 = pairList.get(j);
					if (group2.getFirst().stream().anyMatch(posIdSet::contains)) {
						checkedIndexes.add(j);
						posIdSet.addAll(group2.getFirst());
						vertIdSet.addAll(group2.getSecond());
					}
				}
			}
		}
		return clusterIdMap;

	}

	private Pair<Set<Integer>, Set<GeosetVertex>> getConnectedGroup(GeosetVertex vertex) {
		Set<GeosetVertex> connectedVerts = new HashSet<>();
		findConnected(vertex, connectedVerts);
		Set<Integer> posSet = new HashSet<>();
		for (GeosetVertex gv : connectedVerts) {
			posSet.add(gv.getPositionHash());
		}
		return new Pair<>(posSet, connectedVerts);
	}

	private static void findConnected(GeosetVertex currentVertex, Set<GeosetVertex> connected) {
		connected.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!connected.contains(other)) {
					findConnected(other, connected);
				}
			}
		}
	}
}
