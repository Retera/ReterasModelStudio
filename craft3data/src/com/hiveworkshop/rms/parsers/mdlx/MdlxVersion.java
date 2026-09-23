package com.hiveworkshop.rms.parsers.mdlx;

/**
 * Format version gates for the Warcraft III: Reforged 3.0 ("Forsaken Kingdom", September 2026) layouts, derived
 * from the 14,956 stock models of patch 3.0.0 (all FormatVersion 1800, plus two 1600/1700 environment models) and
 * from a FormatVersion 1300 MDL written by Blizzard's Maya exporter (the version their content pipeline uses
 * before the game compiles it to 1800 MDX). Everything from 1300 upwards is treated as the new format; the
 * binary layouts of 1300-1500 MDX have never been observed, only the MDL text form of 1300.
 */
public final class MdlxVersion {
	public static final int FORSAKEN_KINGDOM_MIN = 1300;

	private MdlxVersion() {
	}

	public static boolean isForsakenKingdom(final int version) {
		return FORSAKEN_KINGDOM_MIN <= version;
	}

	/** GEOS SKIN elements are uint16 (4 bone indices + 4 weights per vertex) instead of uint8. */
	public static boolean hasSkin16(final int version) {
		return isForsakenKingdom(version);
	}

	/** LITE gains a ShadowIntensity float after AmbIntensity (MDX 1200+). */
	public static boolean hasLightShadowIntensity(final int version) {
		return 1200 <= version;
	}

	/** LITE gains a ShadowCasting uint32 after the type and ShadowCastingStart/End floats after ShadowIntensity. */
	public static boolean hasLightShadowCasting(final int version) {
		return isForsakenKingdom(version);
	}

	/** LITE gains QuadraticFalloff, LinearFalloff and Damping floats after ShadowCastingEnd (MDX 1600+). */
	public static boolean hasLightFalloff(final int version) {
		return 1600 <= version;
	}

	/**
	 * CAMS stores a header byte in the top 8 bits of each camera's inclusive size (always 3 in stock data) and
	 * carries the depth-of-field tracks IDUF (focus distance), ELAF (focal length) and PTSF (f-stop).
	 */
	public static boolean hasCameraDepthOfField(final int version) {
		return isForsakenKingdom(version);
	}

	/** The DILG ("Glider") ray-picking whitelist chunk that follows BPOS. */
	public static boolean hasGlider(final int version) {
		return isForsakenKingdom(version);
	}

	/**
	 * MDL text is written in the game's own dialect: a per-layer {@code Shader "name",} line, every texture slot
	 * bound with {@code static TextureID id <= slot,} and animated slots as {@code TextureID n <= slot { ... }}.
	 */
	public static boolean writesGameMdlDialect(final int version) {
		return isForsakenKingdom(version);
	}
}
