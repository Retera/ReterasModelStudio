package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Pair;

import java.util.*;

public class VertexClusterDefinitions {
	private final Map<Geoset, Map<Integer, Set<GeosetVertex>>> geosetClusterIdMap = new HashMap<>();

	public VertexClusterDefinitions(Collection<GeosetVertex> geosetVertices) {
		Set<Geoset> geosets = new HashSet<>();
		geosetVertices.forEach(geosetVertex -> geosets.add(geosetVertex.getGeoset()));
		for(Geoset geoset : geosets){
			geosetClusterIdMap.put(geoset, getConnectedGroups1(geoset));
		}
	}

	public Set<GeosetVertex> getVertexBundle(GeosetVertex vertex) {
		return getGeosetVertCluster2(vertex, geosetClusterIdMap.get(vertex.getGeoset()));
	}
	private Set<GeosetVertex> getGeosetVertCluster2(GeosetVertex vertex, Map<Integer, Set<GeosetVertex>> idMap) {
		for (Integer clusterId : idMap.keySet()) {
			if (idMap.get(clusterId).contains(vertex)) {
				return idMap.get(clusterId);
			}
		}
		return Collections.emptySet();
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
//		System.out.println("pairlist size: " + pairList.size());
		if(300<pairList.size()){
			pairList = getPrematchedPairList(pairList);
//			System.out.println("\t new pairlist size: " + pairList.size());
		}
		return getClusterIdMap(pairList);

	}

	private Map<Integer, Set<GeosetVertex>> getClusterIdMap(List<Pair<Set<Integer>, Set<GeosetVertex>>> pairList) {
		Map<Integer, Set<GeosetVertex>> clusterIdMap = new HashMap<>();

		for (int i = pairList.size()-1; 0 <= i; i--) {
			Pair<Set<Integer>, Set<GeosetVertex>> group1 = pairList.remove(i);

			Set<Integer> cluster_locationHashes = new HashSet<>(group1.getFirst());
			Set<GeosetVertex> cluster_verts = new HashSet<>(group1.getSecond());

			for (int j = pairList.size()-1; 0 <= j; j--) {

				Pair<Set<Integer>, Set<GeosetVertex>> group2 = pairList.get(j);

				Set<Integer> locationHashes_j = group2.getFirst();
				Set<GeosetVertex> verts_j = group2.getSecond();

				if (locationHashes_j.stream().anyMatch(cluster_locationHashes::contains)) {
					cluster_locationHashes.addAll(locationHashes_j);
					cluster_verts.addAll(verts_j);
					pairList.remove(j);
					j = pairList.size();
					i--;
				}
			}
			clusterIdMap.put(clusterIdMap.size(), cluster_verts);
		}
		return clusterIdMap;
	}

	private Map<Integer, Set<GeosetVertex>> getConnectedGroups22(Geoset geoset) {
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

		getPrematchedPairList(pairList);
		return clusterIdMap;

	}

	private List<Pair<Set<Integer>, Set<GeosetVertex>>> getPrematchedPairList(List<Pair<Set<Integer>, Set<GeosetVertex>>> pairList) {
		List<Pair<Set<Integer>, Set<GeosetVertex>>> pairList2 = new ArrayList<>();
		for (int i = pairList.size()-1; 0 <= i; i--) {
			Pair<Set<Integer>, Set<GeosetVertex>> group1 = pairList.remove(i);

			Set<Integer> cluster_locationHashes = group1.getFirst();
			Set<GeosetVertex> cluster_verts = group1.getSecond();

			for (int j = pairList.size()-1; 0 <= j; j--) {

				Pair<Set<Integer>, Set<GeosetVertex>> group2 = pairList.get(j);

				Set<Integer> locationHashes_j = group2.getFirst();
				Set<GeosetVertex> verts_j = group2.getSecond();

				if (locationHashes_j.stream().anyMatch(cluster_locationHashes::contains)) {
					cluster_locationHashes.addAll(locationHashes_j);
					cluster_verts.addAll(verts_j);
					pairList.remove(j);
					i--;
				}
			}
			pairList2.add(new Pair<>(cluster_locationHashes, cluster_verts));
		}
		return pairList2;
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
			for (GeosetVertex other : tri.getVerts()) {
				if (!connected.contains(other)) {
					findConnected(other, connected);
				}
			}
		}
	}
}
