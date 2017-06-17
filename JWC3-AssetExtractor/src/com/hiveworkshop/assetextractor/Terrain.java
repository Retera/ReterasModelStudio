package com.hiveworkshop.assetextractor;

import com.hiveworkshop.wc3.units.Element;

public final class Terrain {
	private String id;
	private String name;
	private final String texturePath;
	private final Element terrainElement;

	public Terrain(final String id, final String name, final String texturePath, final Element terrainElement) {
		this.id = id;
		this.name = name;
		this.texturePath = texturePath;
		this.terrainElement = terrainElement;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getTexturePath() {
		return texturePath;
	}

	public Element getTerrainElement() {
		return terrainElement;
	}

	@Override
	public String toString() {
		return name;
	}
}
