package com.hiveworkshop.wc3.mdl;
import hiveworkshop.localizationmanager.LocalizationManager;

public enum LayerShader {
	SD,
	HD;

	private static final LayerShader[] VALUES = values();

	public static LayerShader fromId(final int id) {
		if ((id < 0) || (id >= VALUES.length)) {
			throw new IllegalArgumentException(LocalizationManager.getInstance().get("exception.layershader_layershader") + id);
		}
		return VALUES[id];
	}
}
