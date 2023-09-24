package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.BiMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class SimModel {
	Map<Integer, SimsBone> boneTreeMap = new LinkedHashMap<>();
	TreeMap<Integer, SimsFace> faceIndexMap = new TreeMap<>();
	TreeMap<Integer, SimsTexVert> textureIndexMap = new TreeMap<>();
	TreeMap<Integer, int[]> weightMap = new TreeMap<>();
	TreeMap<Integer, SimsVert> vertIndexMap = new TreeMap<>();
	TreeMap<Integer, SimsVert> blendVertIndexMap = new TreeMap<>();
	TreeMap<Integer, Integer> blendDataMap = new TreeMap<>();
	BiMap<SimsVert, GeosetVertex> newVertMap = new BiMap<>();
	BiMap<SimsVert, GeosetVertex> newBlendVertMap = new BiMap<>();
	BiMap<SimsVert, Bone> newBlendBoneMap = new BiMap<>();
	BiMap<SimsFace, Triangle> newFaceMap = new BiMap<>();
	String name;
	String texture;

	public SimModel() {
	}

	public SimModel setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public SimModel setTexture(String texture) {
		this.texture = texture;
		return this;
	}

	public String getTexture() {
		return this.texture;
	}

	public SimModel addBone(SimsBone value) {
		this.boneTreeMap.put(value.getIndex(), value);
		return this;
	}

	public SimModel addFaceIndex(int i, SimsFace value) {
		this.faceIndexMap.put(i, value);
		return this;
	}

	public SimModel addUV(SimsTexVert value) {
		this.textureIndexMap.put(value.getIndex(), value);
		return this;
	}

	public SimModel addWeight(int i, int[] value) {
		this.weightMap.put(i, value);
		return this;
	}

	public SimModel addVertex(SimsVert value) {
		if (value.isBlendVert()) {
			this.blendVertIndexMap.put(value.getIndex(), value);
		} else {
			this.vertIndexMap.put(value.getIndex(), value);
			value.setTexVert(this.textureIndexMap.get(value.getIndex()));
		}

		return this;
	}

	public SimModel addBlendVertex(SimsVert value) {
		this.blendVertIndexMap.put(value.getIndex(), value);
		return this;
	}

	public SimModel addBlendData(int i, int value) {
		this.blendDataMap.put(i, value);
		return this;
	}

	public SimModel fillMaps() {

		for (SimsVert simsVert : this.vertIndexMap.values()) {
			GeosetVertex vertex = simsVert.createVertex();
			this.newVertMap.put(simsVert, vertex);
		}
//
//		for (SimsVert vert : this.blendVertIndexMap.values()) {
//			GeosetVertex vertex = vert.createVertex();
//			this.newVertMap.put(vert, vertex);
//		}

//		for(int i : blendVertIndexMap.keySet()){
//			SimsVert vert = blendVertIndexMap.get(i);
//
//		}
		for (SimsVert vert : this.blendVertIndexMap.values()) {
			Bone blendVert = new Bone("BlendVertex" + vert.getIndex());
			blendVert.setPivotPoint(vert.getPos());
			newBlendBoneMap.put(vert, blendVert);
			GeosetVertex vertex = vert.createVertex();
			this.newBlendVertMap.put(vert, vertex);
		}

		for (SimsFace face : this.faceIndexMap.values()) {
			SimsVert simsVert0 = this.vertIndexMap.get(face.getVert0());
			SimsVert simsVert1 = this.vertIndexMap.get(face.getVert1());
			SimsVert simsVert2 = this.vertIndexMap.get(face.getVert2());
			GeosetVertex vertex0 = this.newVertMap.get(simsVert0);
			GeosetVertex vertex1 = this.newVertMap.get(simsVert1);
			GeosetVertex vertex2 = this.newVertMap.get(simsVert2);
			this.newFaceMap.put(face, new Triangle(vertex0, vertex1, vertex2).addToVerts());
		}

		return this;
	}

	public GeosetVertex getVertex(int i) {
		return this.newVertMap.get(this.vertIndexMap.get(i));
	}

	public GeosetVertex getBlendVertex(int i) {
		return this.newVertMap.get(this.blendVertIndexMap.get(i));
	}

	public BiMap<SimsVert, GeosetVertex> getVertMap() {
		return this.newVertMap;
	}

	public BiMap<SimsVert, GeosetVertex> getBlendVertMap() {
		return this.newBlendVertMap;
	}
	public BiMap<SimsVert, Bone> getBlendBoneMap() {
		return this.newBlendBoneMap;
	}

	public BiMap<SimsFace, Triangle> getFaceMap() {
		return this.newFaceMap;
	}

	public Map<Integer, SimsBone> getBoneTreeMap() {
		return this.boneTreeMap;
	}

	public SimModel updateFromEditor() {

		for (SimsVert simsVert : this.newVertMap.keys()) {
			GeosetVertex vertex = this.newVertMap.get(simsVert);
			simsVert.setPos(vertex).setNorm(vertex.getNormal());
			if (simsVert.getTexVert() != null) {
				simsVert.getTexVert().setPoint(vertex.getTVertex(0));
			}
		}

		return this;
	}

	public String getNewModelString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name).append("\n");
		sb.append(this.texture).append("\n");
		sb.append(this.boneTreeMap.size()).append("\n");

		for (SimsBone bone : this.boneTreeMap.values()) {
			sb.append(bone.getName()).append("\n");
		}

		sb.append(this.faceIndexMap.size()).append("\n");

		for (SimsFace face : this.faceIndexMap.values()) {
			sb.append(face.asString()).append("\n");
		}

		sb.append(this.boneTreeMap.size()).append("\n");

		for (SimsBone bone : this.boneTreeMap.values()) {
			sb.append(bone.bindingString()).append("\n");
		}

		sb.append(this.textureIndexMap.size()).append("\n");

		for (SimsTexVert tv : this.textureIndexMap.values()) {
			sb.append(tv.asString()).append("\n");
		}

		sb.append(this.weightMap.size()).append("\n");

		for (int[] ints : this.weightMap.values()) {
			sb.append(ints[0]).append(" ").append(ints[1]).append("\n");
		}

		sb.append(this.vertIndexMap.size() + this.blendVertIndexMap.size()).append("\n");

		for (SimsVert simsVert : this.vertIndexMap.values()) {
			sb.append(simsVert.asString()).append("\n");
		}

		for (SimsVert simsVert : this.blendVertIndexMap.values()) {
			sb.append(simsVert.asString()).append("\n");
		}

		return sb.toString();
	}

	public TreeMap<Integer, SimsTexVert> getTextureIndexMap() {
		return this.textureIndexMap;
	}
}
