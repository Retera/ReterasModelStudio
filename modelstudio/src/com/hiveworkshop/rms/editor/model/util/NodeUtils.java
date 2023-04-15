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
			return children;
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
		return true;
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

		Set<IdObject> tempNodes = new LinkedHashSet<>();
		for(IdObject node : nodesToCheck){
			if(!validNodes.contains(node) && !inValidNodes.contains(node)){
				tempNodes.add(node);
				IdObject tempParent = node.getParent();
				for(int i = 0; i < 10000 && tempParent != null && !tempNodes.contains(tempParent) && !validNodes.contains(tempParent); i++){
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

	public static IdObject getTopNode(IdObject idObject){
		if(idObject.getParent() == null) return idObject;
		return getTopNode(idObject.getParent());
	}

	public static IdObject getTopNode2(IdObject idObject){
		if(idObject.getParent() == null) return idObject;
		IdObject tempParent = idObject.getParent();
		for(int i = 0; i < 5000 && tempParent.getParent() != null; i++){
			if(tempParent == idObject){
				throw new RuntimeException("Node hierarchy loop");
			}
			tempParent = tempParent.getParent();
		}
		return getTopNode(idObject.getParent());
	}

	public static IdObject getTopNodeSafe(IdObject idObject){
		if(idObject.getParent() == null) return idObject;
		IdObject tempParent = idObject.getParent();
		for(int i = 0; i < 5000 && tempParent.getParent() != null; i++){
			if(tempParent == idObject){
				return null;
			}
			tempParent = tempParent.getParent();
		}
		return getTopNode(idObject.getParent());
	}
}
