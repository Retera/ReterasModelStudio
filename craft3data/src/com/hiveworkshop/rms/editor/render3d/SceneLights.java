package com.hiveworkshop.rms.editor.render3d;

/**
 * The lights a viewport hands to the shader pipelines for one frame: the
 * model's own Light nodes (omni, directional, ambient) in world space, or a
 * request for the fixed legacy lighting when the user turned model lights off
 * or the model has none.
 */
public final class SceneLights {
	public static final int MAX_LIGHTS = 8;
	public static final int TYPE_OMNI = 0;
	public static final int TYPE_DIRECTIONAL = 1;
	public static final int TYPE_AMBIENT = 2;

	/** Fixed viewer lighting (the pre-existing canned light). */
	public static final int MODE_LEGACY = 0;
	/** Light nodes of the model. */
	public static final int MODE_MODEL = 1;

	public int mode = MODE_LEGACY;
	public int count;
	public final int[] type = new int[MAX_LIGHTS];
	public final float[] position = new float[MAX_LIGHTS * 3];
	public final float[] direction = new float[MAX_LIGHTS * 3];
	public final float[] color = new float[MAX_LIGHTS * 3];
	public final float[] intensity = new float[MAX_LIGHTS];
	public final float[] ambientColor = new float[MAX_LIGHTS * 3];
	public final float[] ambientIntensity = new float[MAX_LIGHTS];
	public final float[] attenuationStart = new float[MAX_LIGHTS];
	public final float[] attenuationEnd = new float[MAX_LIGHTS];
	/** Reforged falloff exp(-damping d^2) / (1 + linear d + quadratic d^2); defaults are the game's for old lights. */
	public final float[] quadraticFalloff = new float[MAX_LIGHTS];
	public final float[] linearFalloff = new float[MAX_LIGHTS];
	public final float[] damping = new float[MAX_LIGHTS];
	public final float[] shadowIntensity = new float[MAX_LIGHTS];

	public void clear() {
		count = 0;
	}

	public boolean isFull() {
		return count >= MAX_LIGHTS;
	}

	/** Appends a light; returns false when the table is full. Colours are RGB in 0..1. */
	public boolean add(final int lightType, final float px, final float py, final float pz, final float dx,
			final float dy, final float dz, final float r, final float g, final float b, final float lightIntensity,
			final float ar, final float ag, final float ab, final float lightAmbientIntensity, final float attStart,
			final float attEnd) {
		if (count >= MAX_LIGHTS) {
			return false;
		}
		final int i = count++;
		type[i] = lightType;
		position[i * 3] = px;
		position[(i * 3) + 1] = py;
		position[(i * 3) + 2] = pz;
		direction[i * 3] = dx;
		direction[(i * 3) + 1] = dy;
		direction[(i * 3) + 2] = dz;
		color[i * 3] = r;
		color[(i * 3) + 1] = g;
		color[(i * 3) + 2] = b;
		intensity[i] = lightIntensity;
		ambientColor[i * 3] = ar;
		ambientColor[(i * 3) + 1] = ag;
		ambientColor[(i * 3) + 2] = ab;
		ambientIntensity[i] = lightAmbientIntensity;
		attenuationStart[i] = attStart;
		attenuationEnd[i] = attEnd;
		quadraticFalloff[i] = 0.0005f;
		linearFalloff[i] = 0;
		damping[i] = 0.00001f;
		shadowIntensity[i] = 0.4f;
		return true;
	}

	/** Sets the Reforged falloff of the light added last. */
	public void setLastFalloff(final float quadratic, final float linear, final float exponential,
			final float lightShadowIntensity) {
		if (count == 0) {
			return;
		}
		final int i = count - 1;
		quadraticFalloff[i] = quadratic;
		linearFalloff[i] = linear;
		damping[i] = exponential;
		shadowIntensity[i] = lightShadowIntensity;
	}

	public void copyFrom(final SceneLights other) {
		mode = other.mode;
		count = other.count;
		System.arraycopy(other.type, 0, type, 0, MAX_LIGHTS);
		System.arraycopy(other.position, 0, position, 0, position.length);
		System.arraycopy(other.direction, 0, direction, 0, direction.length);
		System.arraycopy(other.color, 0, color, 0, color.length);
		System.arraycopy(other.intensity, 0, intensity, 0, MAX_LIGHTS);
		System.arraycopy(other.ambientColor, 0, ambientColor, 0, ambientColor.length);
		System.arraycopy(other.ambientIntensity, 0, ambientIntensity, 0, MAX_LIGHTS);
		System.arraycopy(other.attenuationStart, 0, attenuationStart, 0, MAX_LIGHTS);
		System.arraycopy(other.attenuationEnd, 0, attenuationEnd, 0, MAX_LIGHTS);
		System.arraycopy(other.quadraticFalloff, 0, quadraticFalloff, 0, MAX_LIGHTS);
		System.arraycopy(other.linearFalloff, 0, linearFalloff, 0, MAX_LIGHTS);
		System.arraycopy(other.damping, 0, damping, 0, MAX_LIGHTS);
		System.arraycopy(other.shadowIntensity, 0, shadowIntensity, 0, MAX_LIGHTS);
	}
}
