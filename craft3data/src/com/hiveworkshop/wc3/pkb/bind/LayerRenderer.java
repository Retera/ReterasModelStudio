package com.hiveworkshop.wc3.pkb.bind;

/** One renderer of a layer as read from {@code CLayerCompileCacheRenderer}. */
public final class LayerRenderer {
	public static final int CLASS_BILLBOARD = 0;
	public static final int CLASS_RIBBON = 1;
	public static final int CLASS_MESH = 2;
	public static final int CLASS_LIGHT = 3;

	public static final int BLEND_ADD = 0;
	public static final int BLEND_NO_ALPHA_ADD = 1;
	public static final int BLEND_BLEND = 2;
	public static final int BLEND_BLEND_ADD = 3;
	public static final int BLEND_OPAQUE = 4;
	public static final int BLEND_ALPHA_KEY = 5;

	public static final class ParticleInput {
		public int semantic;
		public int indexInStorage;
		public String additionalFieldName = "";
	}

	public int rendererClass = CLASS_BILLBOARD;
	public boolean isAtlas;
	public boolean hasAlphaLut;
	public boolean isDistortion;
	public boolean hasFlipUVs;
	public boolean hasCustomTextureU;
	public boolean hasEnableSize2D;
	public boolean isRenderingEnabled = true;
	public boolean hasTransparent;
	public boolean isLit;
	public boolean hasSoftParticles;
	public int billboardingMode;
	public int transparentSortMode;
	public int atlasBlending;
	public String alphaRemapMapPath = "";
	public boolean textureFlipU;
	public boolean textureFlipV;
	public boolean textureRotateTexture;
	public String diffuseTexturePath = "";
	public int atlasSubDivX;
	public int atlasSubDivY;
	public int blendMode = BLEND_OPAQUE;
	public ParticleInput[] particleInputs = new ParticleInput[0];

	public boolean hasAssetInputBindings() {
		return particleInputs.length > 0;
	}

	/**
	 * The diffuse texture path relative to the game data root, with the
	 * {@code _HD.w3mod/} style mod prefix removed and backslashes, so the usual
	 * texture loader (with its .tif/.blp to .dds fallback) can find it.
	 */
	public String gameTexturePath() {
		if (diffuseTexturePath.isEmpty()) {
			return "";
		}
		String path = diffuseTexturePath.replace('/', '\\');
		final int mod = path.toLowerCase().lastIndexOf(".w3mod\\");
		if (mod >= 0) {
			path = path.substring(mod + ".w3mod\\".length());
		}
		return path;
	}
}
