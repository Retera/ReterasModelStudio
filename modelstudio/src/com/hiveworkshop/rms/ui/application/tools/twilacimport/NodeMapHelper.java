package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.*;

public class NodeMapHelper {
	private final String[] typeStrings = new String[]{"bone", "geo", "helper", "node"};
	private final LinkedHashMap<IdObject, IdObject> boneChainMap = new LinkedHashMap<>();
//	private final NodeDepthMap nodeDepthMap = new NodeDepthMap(0);
	private final NodeDepthMap nodeDepthMap = new NodeDepthMap(-1);
	private boolean presentParent;
	private boolean checkHelperChilds;
	private boolean isGeometryMode;
	private boolean allowTopLevelMapping;
	private boolean autoValidateChain = true;

	private Set<Class<?>> classSet;

	public NodeMapHelper () {
		nodeDepthMap.setLink(null, null);
	}


	public void setLink(int depth, IdObject fromNode, IdObject toNode) {
		nodeDepthMap.setLink(depth, fromNode, toNode);
	}


	public IdObject getMappedNode(int depth, IdObject fromNode) {
		return nodeDepthMap.getMappedNode(depth, fromNode);
	}
	public boolean hasMappedNode(int depth, IdObject fromNode) {
		return nodeDepthMap.hasMappedNode(depth, fromNode);
	}

	public LinkedHashMap<IdObject, IdObject> getBoneChainMap() {
		return boneChainMap;
	}

	public LinkedHashMap<IdObject, IdObject> getBoneChainMap(int depth) {
		return nodeDepthMap.getBoneChainMap(depth);
	}

	public boolean isNodeMapped(IdObject node) {
		return nodeDepthMap.getBoneChainMap().isEmpty() || !nodeDepthMap.getBoneChainMap().containsKey(node);
	}

	public void fillChainMap(int depth) {
		for (int i = 0; i < depth; i++) {
			LinkedHashMap<IdObject, IdObject> chainMap = nodeDepthMap.getBoneChainMap(i);
			if (chainMap != null) {
				boneChainMap.putAll(chainMap);
				System.out.println("added " + chainMap.size() + " mappings");
			} else {
				break;
			}
		}
		System.out.println(boneChainMap.size() + " mappings found!");
	}

	public Map<IdObject, IdObject> fillAndGetChainMap(int depth) {
		System.out.println("FILL AND GET MAP!");
		fillChainMap(depth);
		System.out.println("MAP SIZE: " + boneChainMap.size());
		return boneChainMap;
	}

	public Map<IdObject, Map<IdObject, List<IdObject>>> getMappingOptions(int depth) {
		Map<IdObject, Map<IdObject, List<IdObject>>> parentToChildOptions = new HashMap<>();

		LinkedHashMap<IdObject, IdObject> chainMap = nodeDepthMap.getDepthMap(depth).getBoneChainMap();
		for (IdObject idObject : chainMap.keySet()) {
			Map<IdObject, List<IdObject>> childOptionMap = parentToChildOptions.computeIfAbsent(idObject.getParent(), k -> new HashMap<>());
			IdObject parentMapping = chainMap.get(idObject);
			if (parentMapping != null) {
				for (IdObject child : idObject.getChildrenNodes()) {
					childOptionMap.put(child, getValidNodesList(child, parentMapping.getChildrenNodes(), depth+1));
				}
			}
		}

		return parentToChildOptions;
	}

	public void prefillMap(int currDepth, int depth, List<IdObject> mapFromNodes, List<IdObject> mapToNodes) {
		if (currDepth == 0) {
//			List<IdObject> mapFromNodes = getAllTopLevelNodes(mapFromModel);
//			List<IdObject> mapToNodes = getAllTopLevelNodes(mapToModel);
			prefillCurrDepth(mapFromNodes, mapToNodes, currDepth);
//			System.out.println("added Top Level Nodes");
		}
		while (currDepth<depth) {
			Map<IdObject, IdObject> boneChainSubMap = nodeDepthMap.getBoneChainMap(currDepth);
			currDepth++;
			if (boneChainSubMap != null) {
				for (IdObject mapFromIdObject : boneChainSubMap.keySet()) {
					IdObject mapToIdObject = boneChainSubMap.get(mapFromIdObject);
					if (mapToIdObject != null && isBones(mapFromIdObject, mapToIdObject)) {
						prefillCurrDepth(mapFromIdObject.getChildrenNodes(), mapToIdObject.getChildrenNodes(), currDepth);

					}
				}
			} else {
				break;
			}
		}

	}

	public void prefillCurrDepth(List<IdObject> mapFromChilds, List<IdObject> mapToChilds, int currDepth) {
		Map<IdObject, List<IdObject>> nodeToPosNodes = getCandidateListMap(mapToChilds, mapFromChilds, currDepth);
		System.out.println("Prefilling at " + currDepth);

		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);

			IdObject betsMatch = posMapToNodes.size() == 2 ? posMapToNodes.get(1) : findBetsMatch(mapFromNode, posMapToNodes);

			nodeDepthMap.setLink(currDepth, mapFromNode, betsMatch);

			String bestName = betsMatch == null ? "None" : betsMatch.getName();
			System.out.println("mapped " + mapFromNode.getName() + " to " + bestName);
		}
	}

	public void prefillCurrDepth(Map<IdObject, List<IdObject>> nodeToPosNodes, int currDepth) {
		System.out.println("Prefilling at " + currDepth);
		for (IdObject mapFromNode : nodeToPosNodes.keySet()) {
			List<IdObject> posMapToNodes = nodeToPosNodes.get(mapFromNode);

			IdObject betsMatch = posMapToNodes.size() == 2 ? posMapToNodes.get(1) : findBetsMatch(mapFromNode, posMapToNodes);
			nodeDepthMap.setLink(currDepth, mapFromNode, betsMatch);

			String bestName = betsMatch == null ? "None" : betsMatch.getName();
			System.out.println("mapped " + mapFromNode.getName() + " to " + bestName);
		}
	}

	public Map<IdObject, List<IdObject>> getCandidateListMap(List<IdObject> idObjectsForComboBox, List<IdObject> idObjectsForPanel, int currDepth) {
		Map<IdObject, List<IdObject>> candidateListMap = new HashMap<>();

		if (!idObjectsForComboBox.isEmpty()) {
			for (IdObject idObjectForPanel : idObjectsForPanel) {
				if (classSet == null || classSet.contains(idObjectForPanel.getClass())) {
					candidateListMap.put(idObjectForPanel, getValidNodesList(idObjectForPanel, idObjectsForComboBox, currDepth));
				}
			}
		}
		return candidateListMap;
	}

	private List<IdObject> getValidNodesList(IdObject idObjectDest, List<IdObject> idObjectsForComboBox, int currDepth) {
		List<IdObject> validObjects = new ArrayList<>();
		validObjects.add(null);

		if (presentParent && !idObjectsForComboBox.isEmpty() && idObjectsForComboBox.get(0).getParent() != null) {
			validObjects.add(idObjectsForComboBox.get(0).getParent());
		}

		idObjectsForComboBox.stream()
				.filter(idObject -> isValidCandidate(idObjectDest, idObject))
				.forEach(validObjects::add);

		if (checkHelperChilds) {
			validObjects.addAll(fetchSuitableChildBones2(idObjectDest, idObjectsForComboBox));
		}


		if (nodeDepthMap.hasMappedNode(currDepth, idObjectDest)) {
			IdObject mappedNode = nodeDepthMap.getMappedNode(currDepth, idObjectDest);
			if (autoValidateChain && !validObjects.contains(mappedNode)) {
				nodeDepthMap.removeLink(currDepth, idObjectDest);
			}
		}

		return validObjects;
	}

//	private List<IdObject> fetchSuitibleChildBones(IdObject idObject, List<IdObject> nodesToCheck) {
//		List<IdObject> nodes =  new ArrayList<>();
//		if (idObject instanceof Bone || idObject instanceof  Helper) {
//			for (IdObject node : nodesToCheck) {
//				if (node instanceof Helper) {
//					for (IdObject child : node.getChildrenNodes()) {
//						if (child instanceof Bone) {
//							nodes.add(child);
//						}
//					}
//				}
//			}
//		}
//
//		return nodes;
//	}

	private List<IdObject> fetchSuitableChildBones2(IdObject idObject, List<IdObject> nodesToCheck) {
		List<IdObject> nodes =  new ArrayList<>();
		if (idObject instanceof Bone || idObject instanceof  Helper) {
			for (IdObject node : nodesToCheck) {
				if (node instanceof Helper || node instanceof Bone) {
					for (IdObject child : node.getChildrenNodes()) {
						if (classSet == null || classSet.contains(child.getClass())) {
							nodes.add(child);
						}
					}
				}
			}
		}

		return nodes;
	}

	private boolean isValidCandidate(IdObject idObjectDest, IdObject idObject) {
//		if (isGeometryMode) {
//			return sameClass(idObjectDest, idObject);
//		}
		return isBones(idObject, idObjectDest) || sameClass(idObjectDest, idObject) || idObjectDest instanceof Helper && idObject instanceof Bone ||  idObjectDest instanceof Bone && idObject instanceof Helper;
	}

	private boolean sameClass(IdObject idObjectDest, IdObject idObject) {
		return idObject.getClass() == idObjectDest.getClass();
	}

	protected boolean isBones(IdObject idObject1, IdObject idObject2) {
		return idObject1 instanceof Bone && idObject2 instanceof Bone;
	}

	public IdObject findBetsMatch(IdObject objToMatch, List<IdObject> validObjects) {
		IdObject sameNameObject = null;
		int lastMatch = 20;
		String matchName = objToMatch.getName();
		for (IdObject idObject : validObjects) {
			if (idObject != null) {
				String name = idObject.getName();
				int comp = Math.abs(name.compareTo(matchName));
				if (comp == 0
						|| comp < lastMatch && isMatch(matchName, name)
						|| sameNameObject == null && isSomeWhatClose(matchName, name)) {
					sameNameObject = idObject;
					lastMatch = comp;
				}
			}
		}
		return sameNameObject;
	}

	/**
	 * splits the strings on "_" and checks if the sub-strings matches up till
	 * min(a_split.length, b_split.length)
	 */
	private boolean isMatch(String destName, String name) {
		String[] namsSplit = name.split("_");
		String[] destSplit = destName.split("_");
		for (int i = 0; i < namsSplit.length && i < destSplit.length; i++) {
			if (!namsSplit[i].equals(destSplit[i])) {
				return false;
			}
		}
		return true;
	}
	private boolean isSomeWhatClose(String destName, String name) {
		destName = replaceAllTypeStrings(destName);
		name = replaceAllTypeStrings(name);

		return name.startsWith(destName) || destName.startsWith(name);
	}

	private String replaceAllTypeStrings(String s) {
		s = s.toLowerCase();
		for (String stupid : typeStrings) {
			s = s.replaceAll(stupid, "TEMP");
		}
//		s = s.replaceAll("TEMP", "").replaceAll("_", "");
		s = s.replaceAll("(TEMP)|_", "");
		return s;
	}

	public boolean isAllowTopLevelMapping() {
		return allowTopLevelMapping;
	}

	public NodeMapHelper setPresentParent(boolean presentParent) {
		this.presentParent = presentParent;
		return this;
	}

	public NodeMapHelper setAllowTopLevelMapping(boolean allowTopLevelMapping) {
		this.allowTopLevelMapping = allowTopLevelMapping;
		return this;
	}

	public NodeMapHelper setIsGeometryMode(boolean isGeometryMode) {
		this.isGeometryMode = isGeometryMode;
		return this;
	}

	public NodeMapHelper setCheckHelperBones(boolean checkHelperChilds) {
		this.checkHelperChilds = checkHelperChilds;
		return this;
	}
	public NodeMapHelper setClassSet(Set<Class<?>> classSet) {
		this.classSet = classSet;
		return this;
	}
}
