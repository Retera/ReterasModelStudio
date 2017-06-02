package com.hiveworkshop.assetripper;

public class AssetRipperSettings {
	private final boolean includeInternal;
	private final int flatten;

	public AssetRipperSettings(final boolean includeInternal, final int flatten) {
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
