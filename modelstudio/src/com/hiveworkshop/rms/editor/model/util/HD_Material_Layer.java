package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.Bitmap;

public enum HD_Material_Layer {
	DIFFUSE     ("Diffuse",     "Textures\\White.dds",                      0),
	VERTEX      ("Vertex",      "Textures\\normal.dds",                     0),
	ORM         ("ORM",         "Textures\\orm.dds",                        0),
	EMISSIVE    ("Emissive",    "Textures\\Black32.dds",                    0),
	TEAM_COLOR  ("Team Color",  "",                                         1),
	REFLECTIONS ("Reflections", "ReplaceableTextures\\EnvironmentMap.dds",  0),
	;

	final String layerName;
	final Bitmap bitmap;

	HD_Material_Layer(String layerName, String bitmapPath, int replaceableID) {
		this.layerName = layerName;
		this.bitmap = getBitmap(bitmapPath, replaceableID);
	}

	public String getLayerName() {
		return layerName;
	}

	public Bitmap getPlaceholderBitmap() {
		return bitmap;
	}

	private Bitmap getBitmap(String s, int replaceableID) {
		Bitmap bitmap = new Bitmap(s, replaceableID);
		bitmap.setWrapHeight(true);
		bitmap.setWrapWidth(true);
		return bitmap;
	}
}
