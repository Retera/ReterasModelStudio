package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.Bitmap;

public enum HD_Material_Layer {
	DIFFUSE("Diffuse", "Textures\\White.dds"),
	VERTEX("Vertex", "Textures\\normal.dds"),
	ORM("ORM", "Textures\\orm.dds"),
	EMISSIVE("Emissive", "Textures\\Black32.dds"),
	TEAM_COLOR("Team Color", ""),
	REFLECTIONS("Reflections", "ReplaceableTextures\\EnvironmentMap.dds"),
	;

	String layerName;
	Bitmap bitmap;

	HD_Material_Layer(String s, String bitmapPath) {
		layerName = s;
		this.bitmap = getBitmap(bitmapPath);
	}

	public String getLayerName() {
		return layerName;
	}

	public Bitmap getPlaceholderBitmap() {
		return bitmap;
	}

	private Bitmap getBitmap(String s) {
		Bitmap bitmap = new Bitmap(s);
		bitmap.setWrapHeight(true);
		bitmap.setWrapWidth(true);
		return bitmap;
	}
}
