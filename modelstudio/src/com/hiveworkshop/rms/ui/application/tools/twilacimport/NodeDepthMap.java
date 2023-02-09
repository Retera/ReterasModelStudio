package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.LinkedHashMap;

public class NodeDepthMap {
	private final int depth;
	private NodeDepthMap nextMap;
	private final LinkedHashMap<IdObject, IdObject> boneChainMap = new LinkedHashMap<>();

	public NodeDepthMap(int depth){
		this.depth = depth;
	}

	public void setLink(IdObject fromNode, IdObject toNode){
		boneChainMap.put(fromNode, toNode);
		if(nextMap == null){
			nextMap = new NodeDepthMap(depth + 1);
		}
	}

	public void setLink(int depth, IdObject fromNode, IdObject toNode){
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			String fn_name = fromNode != null ? fromNode.getName() : "Null";
			String tn_name = toNode != null ? toNode.getName() : "Null";
			System.out.println("addded " + fn_name + " - " + tn_name + " to map at depth: " + depth + "! (depthMap: " + depthMap + ")");
			depthMap.setLink(fromNode, toNode);
		}
	}

	public void removeLink(IdObject fromNode){
		boneChainMap.remove(fromNode);
	}
	public void removeLink(int depth, IdObject fromNode){
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			depthMap.removeLink(fromNode);
		}
	}

	public void clear(){
		boneChainMap.clear();
		nextMap = null;
	}
	public void clear(int depth){
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			depthMap.clear();
		}
	}

	public LinkedHashMap<IdObject, IdObject> getBoneChainMap() {
		return boneChainMap;
	}

	public LinkedHashMap<IdObject, IdObject> getBoneChainMap(int depth) {
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			return depthMap.getBoneChainMap();
		}
		return null;
	}

	public NodeDepthMap getNextDepthMap() {
		return nextMap;
	}
	public NodeDepthMap getDepthMap(int depth) {
		if(depth == this.depth) return this;
		if(depth > this.depth && nextMap != null){
			return nextMap.getDepthMap(depth);
		}
		return null;
	}

	public IdObject getMappedNode(IdObject fromNode) {
		return boneChainMap.get(fromNode);
	}

	public IdObject getMappedNode(int depth, IdObject fromNode) {
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			return depthMap.getBoneChainMap().get(fromNode);
		}
		return null;
	}
	public boolean hasMappedNode(int depth, IdObject fromNode) {
		NodeDepthMap depthMap = getDepthMap(depth);
		if(depthMap != null && depthMap.depth == depth){
			return depthMap.getBoneChainMap().containsKey(fromNode);
		}
		return false;
	}
}
