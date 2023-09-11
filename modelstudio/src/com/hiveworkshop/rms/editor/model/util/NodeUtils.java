package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.*;

public class NodeUtils {


	public static Set<IdObject> collectChildren(IdObject idObject){
		return collectChildren(Collections.singleton(idObject), null, true);
	}

	public static Set<IdObject> collectChildren(IdObject parent, Set<IdObject> children, boolean includeParents){
		return collectChildren(Collections.singleton(parent), children, includeParents);
	}


	public static Set<IdObject> collectChildren(Collection<IdObject> parents, Set<IdObject> children, boolean includeParents){
		if(children == null){
			children = new LinkedHashSet<>();
		}
		for(IdObject parent : parents){
			if(includeParents){
				children.add(parent);
			}
			for(IdObject child : parent.getChildrenNodes()){
				collectChildren(child, children);
			}
		}
		return children;
	}

	private static void collectChildren(IdObject idObject, Set<IdObject> children){
		children.add(idObject);
		for (IdObject child : idObject.getChildrenNodes()) {
			collectChildren(child, children);
		}
	}

	public static boolean isValidHierarchy(IdObject idObject, IdObject newParent){
		IdObject tempParent = newParent;
		for(int i = 0; i < 5000 && tempParent != null; i++){
			if(tempParent == idObject){
				return false;
			}
			tempParent = tempParent.getParent();
		}
		return tempParent == null;
	}

	public static List<IdObject> getNodeChainTo(IdObject idObject){
		return getNodeChainTo(idObject, true, true);
	}

	public static List<IdObject> getNodeChainTo(IdObject idObject, boolean topFirst, boolean include){
		List<IdObject> tempNodes = new ArrayList<>();
		if(include) tempNodes.add(idObject);
		IdObject tempParent = idObject.getParent();
		for(int i = 0; i < 5000 && tempParent.getParent() != null; i++){
			if(tempParent == idObject){
				break;
			}
			if(topFirst) tempNodes.add(0, tempParent);
			else  tempNodes.add(tempParent);
			tempParent = tempParent.getParent();
		}
		return tempNodes;
	}

	public static Set<IdObject> getProblematicNodes(Collection<IdObject> nodesToCheck){
		Set<IdObject> validNodes = new LinkedHashSet<>();
		Set<IdObject> inValidNodes = new LinkedHashSet<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for(IdObject node : nodesToCheck){
			if(!validNodes.contains(node) && !inValidNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				for(int i = 0; i < maxItr && tempParent != null && !tempNodes.contains(tempParent) && !validNodes.contains(tempParent); i++){
					tempNodes.add(tempParent);
					tempParent = tempParent.getParent();
				}
				if(tempParent == null || validNodes.contains(tempParent)){
					validNodes.addAll(tempNodes);
				} else {
					inValidNodes.addAll(tempNodes);
				}
				tempNodes.clear();
			}
		}
		return inValidNodes;
	}
	public static List<List<IdObject>> getNodeCircles1(Collection<IdObject> nodesToCheck){
		Set<IdObject> validNodes = new LinkedHashSet<>();
		List<List<IdObject>> nodeCircles = new ArrayList<>();
		Set<IdObject> inValidNodes = new LinkedHashSet<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for(IdObject node : nodesToCheck){
			if(!validNodes.contains(node) && !inValidNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				for (int i = 0; i < maxItr && tempParent != null && !tempNodes.contains(tempParent) && !validNodes.contains(tempParent); i++) {
					tempNodes.add(tempParent);
					tempParent = tempParent.getParent();
				}
				if(tempParent == null || validNodes.contains(tempParent)){
					validNodes.addAll(tempNodes);
				} else {
					inValidNodes.addAll(tempNodes);
					for (IdObject tempNode : tempNodes) {
						int maxItr2 = nodesToCheck.size()+2;
						IdObject tempP2 = tempNode.getParent();
						for (int i = 0; i < maxItr2 && tempP2 != tempNode; i++) {
							tempP2 = tempP2.getParent();
						}

						if (tempP2 == tempNode) {
							tempP2 = tempNode.getParent();
							List<IdObject> circle = new ArrayList<>();
							circle.add(tempNode);
							for (int i = 0; i < maxItr2 && tempP2 != tempNode; i++) {
								circle.add(tempP2);
								tempP2 = tempP2.getParent();
							}
							nodeCircles.add(circle);
						}
					}
				}
				tempNodes.clear();
			}
		}
		return nodeCircles;
	}
	public static List<List<IdObject>> getNodeCircles(Collection<IdObject> nodesToCheck){
		Set<IdObject> checkedNodes = new LinkedHashSet<>();
		List<List<IdObject>> nodeCircles = new ArrayList<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for(IdObject node : nodesToCheck){
			if(!checkedNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				for (int i = 0; i < maxItr && tempParent != null && !tempNodes.contains(tempParent) && !checkedNodes.contains(tempParent); i++) {
					tempNodes.add(tempParent);
					tempParent = tempParent.getParent();
				}
				if(tempParent != null && !checkedNodes.contains(tempParent)){
					for (IdObject tempNode : tempNodes) {
						int maxItr2 = nodesToCheck.size()+2;
						IdObject tempP2 = tempNode.getParent();
						for (int i = 0; i < maxItr2 && tempP2 != tempNode; i++) {
							tempP2 = tempP2.getParent();
						}

						if (tempP2 == tempNode) {
							tempP2 = tempNode.getParent();
							List<IdObject> circle = new ArrayList<>();
							circle.add(tempNode);
							for (int i = 0; i < maxItr2 && tempP2 != tempNode; i++) {
								circle.add(tempP2);
								tempP2 = tempP2.getParent();
							}
							nodeCircles.add(circle);
							break;
						}
					}
				}
				checkedNodes.addAll(tempNodes);
				tempNodes.clear();
			}
		}
		return nodeCircles;
	}

	public static Set<IdObject> getProblematicNodes2(Collection<IdObject> nodesToCheck){
		Set<IdObject> validNodes = new LinkedHashSet<>();
		Set<IdObject> inValidNodes = new LinkedHashSet<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for (IdObject node : nodesToCheck) {
			IdObject tempPar = node.getParent();
			for (int i = 0; i < maxItr; i++) {
				if (tempPar == node) {
					inValidNodes.add(tempPar);
					break;
				} else if (tempPar == null || validNodes.contains(tempPar)) {
					validNodes.add(node);
					break;
				}
				tempPar = tempPar.getParent();
			}
		}
		return inValidNodes;
	}
	public static Set<IdObject> getProblematicNodes3(Collection<IdObject> nodesToCheck){
		Set<IdObject> validNodes = new LinkedHashSet<>();
		Set<IdObject> inValidNodes = new LinkedHashSet<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for (IdObject node : nodesToCheck) {
			if(!validNodes.contains(node) && !inValidNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				for (int i = 0; i < maxItr && tempParent != null && tempParent != node && !validNodes.contains(tempParent); i++) {
					tempNodes.add(tempParent);
					tempParent = tempParent.getParent();
				}
				if (tempParent == node) {
					inValidNodes.add(tempParent);
				} else if (tempParent == null || validNodes.contains(tempParent)) {
					validNodes.addAll(tempNodes);
				}

				tempNodes.clear();
			}
		}
		return inValidNodes;
	}
	public static Set<IdObject> getProblematicNodes4(Collection<IdObject> nodesToCheck){
		Set<IdObject> validNodes = new LinkedHashSet<>();
		Set<IdObject> hasInvalidParent = new LinkedHashSet<>();
		Set<IdObject> invalidNodes = new LinkedHashSet<>();

		int maxItr = nodesToCheck.size()+2;
		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for (IdObject node : nodesToCheck) {
			if(!validNodes.contains(node) && !invalidNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				IdObject firstInvalidParent = null;
				for (int i = 0; i < maxItr
						&& tempParent != null
						&& tempParent != node
						&& !validNodes.contains(tempParent)
						&& !hasInvalidParent.contains(tempParent); i++) {
					tempNodes.add(tempParent);
					if (invalidNodes.contains(tempParent)) {
						if(firstInvalidParent == null) {
							firstInvalidParent = tempParent;
						} else if (tempParent == firstInvalidParent) {
							break;
						}
					}
					tempParent = tempParent.getParent();
				}
				if (tempParent == node) {
					invalidNodes.add(tempParent);
				} else if (tempParent == null || validNodes.contains(tempParent)) {
//				validNodes.add(node);
					validNodes.addAll(tempNodes);
				} else if (invalidNodes.contains(tempParent)) {
					hasInvalidParent.add(node);
				}

				tempNodes.clear();
			}
		}
		return invalidNodes;
	}

	public static IdObject getTopNode(IdObject idObject){
		if(idObject.getParent() == null) return idObject;
		return getTopNode(idObject.getParent());
	}

	public static IdObject getTopNode2(IdObject idObject){
		return getTopNode2(idObject, 5000);
	}

	public static IdObject getTopNode2(IdObject idObject, int maxNodes){
		IdObject topNodeSafe = getTopNodeSafe(idObject, maxNodes);
		if (topNodeSafe == null) {
			throw new RuntimeException("Node hierarchy loop");
		}
		return topNodeSafe;
	}

	public static IdObject getTopNodeSafe(IdObject idObject){
		return getTopNodeSafe(idObject, 5000);
	}

	public static IdObject getTopNodeSafe(IdObject idObject, int maxNodes){
		if(idObject.getParent() == null) return idObject;
		IdObject tempParent = idObject.getParent();
		for(int i = 0; i < maxNodes && tempParent.getParent() != null; i++){
			if(tempParent == idObject){
				return null;
			}
			tempParent = tempParent.getParent();
		}
		return getTopNode(idObject.getParent());
	}
}
