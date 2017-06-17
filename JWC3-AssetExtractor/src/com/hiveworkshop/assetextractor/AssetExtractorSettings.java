package com.hiveworkshop.assetextractor;

public class AssetExtractorSettings {
	private final boolean includeInternal;
	private final int flatten;

	public AssetExtractorSettings(final boolean includeInternal, final int flatten) {
		this.includeInternal = includeInternal;
		this.flatten = flatten;
	}

	public boolean isIncludeInternal() {
		return includeInternal;
	}

	public int getFlatten() {
		return flatten;
	}
}
