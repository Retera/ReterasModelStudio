package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GeosetVertexNodeDeleteOperation1 implements UndoAction {
	private final GeosetVertex vertex;
	private final Map<Integer, Bone> integerBoneMap;
	boolean relink;
	Map<IdObject, IdObject> topParentMap;


	public GeosetVertexNodeDeleteOperation1(GeosetVertex vertex, Set<Bone> bones, boolean relink, Map<IdObject, IdObject> topParentMap) {
		this.vertex = vertex;
		this.relink = relink;
		this.topParentMap = topParentMap;

		integerBoneMap = new TreeMap<>();
		// ToDo SkinBones

		List<Bone> vertexBones = vertex.getBones();
		for (Bone bone : vertexBones) {
			if (bones.contains(bone)) {
				int key = vertexBones.indexOf(bone);
				if (key != -1) {
					integerBoneMap.put(key, bone);
				}
			}
		}
	}

	@Override
	public UndoAction undo() {
		for (Integer i : integerBoneMap.keySet()) {
			Bone oldBone = integerBoneMap.get(i);
			if (relink) {
				IdObject replacedParent = topParentMap.get(oldBone);
				if (replacedParent instanceof Bone) {
					vertex.removeBone((Bone) replacedParent);
				}
			}
			vertex.addBoneAttachment(i, oldBone);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if (relink) {
			for (Integer i : integerBoneMap.keySet()) {
				IdObject potParent = topParentMap.get(integerBoneMap.get(i));
				if (potParent instanceof Bone && !(potParent instanceof Helper)) {
					vertex.removeBone(integerBoneMap.get(i));
					vertex.addBoneAttachment(i, (Bone) potParent);
				}
			}
		}
		vertex.removeBones(integerBoneMap.values());
		return this;
	}

	@Override
	public String actionName() {
		return "remove vertex bone binding";
	}
}
