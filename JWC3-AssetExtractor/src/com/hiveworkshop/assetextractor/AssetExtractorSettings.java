package com.hiveworkshop.assetextractor;

public class AssetExtractorSettings {
	public static final int FLATTEN_BY_RETAIN_ICON_AND_TEXTURE_PATHS = 3;
	public static final int FLATTEN_BY_RETAIN_ALL_PATHS = 0;

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
