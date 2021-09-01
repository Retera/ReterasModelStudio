package com.hiveworkshop.rms.editor.model.util;

public enum HD_Material_Layer {
	DIFFUSE("Diffuse"),
	VERTEX("Vertex"),
	ORM("ORM"),
	EMISSIVE("Emissive"),
	TEAM_COLOR("Team Color"),
	REFLECTIONS("Reflections"),
	;

	String layerName;

	HD_Material_Layer(String s) {
		layerName = s;
	}

	public String getLayerName() {
		return layerName;
	}
}
