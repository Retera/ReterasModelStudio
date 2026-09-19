package com.hiveworkshop.wc3.pkb.bind;

/**
 * A sampler a layer can address by name: a curve, a primitive shape, an event
 * stream or a procedural turbulence field. Mesh, texture and external vector
 * field samplers are recognised but carry no data (they sample as missing).
 */
public final class SamplerResource {
	public static final int KIND_UNKNOWN = 0;
	public static final int KIND_CURVE = 1;
	public static final int KIND_SHAPE = 2;
	public static final int KIND_EVENT_STREAM = 3;
	public static final int KIND_TURBULENCE = 4;
	public static final int KIND_TEXTURE = 5;

	public static final int SHAPE_BOX = 0;
	public static final int SHAPE_SPHERE = 1;
	public static final int SHAPE_COMPLEX_ELLIPSOID = 2;
	public static final int SHAPE_CYLINDER = 3;
	public static final int SHAPE_CAPSULE = 4;
	public static final int SHAPE_CONE = 5;
	public static final int SHAPE_MESH = 6;

	public static final int DIM_VERTEX = 1;
	public static final int DIM_SURFACE = 2;
	public static final int DIM_VOLUME = 3;

	public static final class Curve {
		public float[] times = new float[0];
		public float[] values = new float[0];
		public float[] tangents = new float[0];
		public int components = 1;
		public int interpolator;
		public boolean looped;
		public boolean isProbabilityCurve;
		public float[] cdfTimes = new float[0];
		public float[] cdfValues = new float[0];

		public boolean hasHermiteTangents() {
			return (tangents.length > 0) && (tangents.length >= (2 * values.length));
		}

		/**
		 * Evaluates the curve at {@code t} into {@code out[0..outLen)}, returning the
		 * number of meaningful components (0 when the curve is unusable).
		 */
		public int evalVec(final float t, final float[] out, final int outLen) {
			if ((out == null) || (outLen == 0)) {
				return 0;
			}
			final int comps = components;
			if ((comps == 0) || (comps > 4) || (times.length == 0)) {
				return 0;
			}
			if (values.length < (times.length * comps)) {
				return 0;
			}
			final int n = times.length;
			if (t <= times[0]) {
				writeKey(0, out, outLen);
				return Math.min(outLen, comps);
			}
			if (t >= times[n - 1]) {
				writeKey(n - 1, out, outLen);
				return Math.min(outLen, comps);
			}
			int i = n - 1;
			float u = 0;
			boolean inside = false;
			for (int k = 1; k < n; k++) {
				final float t1 = times[k];
				if (t < t1) {
					final float t0 = times[k - 1];
					final float span = t1 - t0;
					u = span > 0 ? (t - t0) / span : 0;
					i = k;
					inside = true;
					break;
				}
			}
			if (!inside) {
				writeKey(n - 1, out, outLen);
				return Math.min(outLen, comps);
			}
			final int base0 = (i - 1) * comps;
			final int base1 = i * comps;
			final boolean hermite = hasHermiteTangents();
			final int mOutBase = ((2 * (i - 1)) + 1) * comps;
			final int mInBase = (2 * i) * comps;
			final float u2 = u * u;
			final float u3 = u2 * u;
			final float h00 = ((2 * u3) - (3 * u2)) + 1;
			final float h10 = (u3 - (2 * u2)) + u;
			final float h01 = (-2 * u3) + (3 * u2);
			final float h11 = u3 - u2;
			for (int lane = 0; lane < outLen; lane++) {
				if (lane >= comps) {
					out[lane] = 0;
					continue;
				}
				final float v0 = values[base0 + lane];
				final float v1 = values[base1 + lane];
				if (hermite) {
					final float m0Out = tangents[mOutBase + lane];
					final float m1In = tangents[mInBase + lane];
					out[lane] = (h00 * v0) + (h10 * m0Out) + (h01 * v1) + (h11 * m1In);
				} else {
					out[lane] = v0 + ((v1 - v0) * u);
				}
			}
			return Math.min(outLen, comps);
		}

		private void writeKey(final int key, final float[] out, final int outLen) {
			final int base = key * components;
			for (int lane = 0; lane < outLen; lane++) {
				out[lane] = lane < components ? values[base + lane] : 0;
			}
		}

		public float evalCdf(final float t, final float defaultValue) {
			if ((cdfTimes.length == 0) || (cdfValues.length < cdfTimes.length)) {
				return defaultValue;
			}
			final int n = cdfTimes.length;
			if (t <= cdfTimes[0]) {
				return cdfValues[0];
			}
			if (t >= cdfTimes[n - 1]) {
				return cdfValues[n - 1];
			}
			for (int i = 1; i < n; i++) {
				final float t1 = cdfTimes[i];
				if (t < t1) {
					final float t0 = cdfTimes[i - 1];
					final float span = t1 - t0;
					final float u = span > 0 ? (t - t0) / span : 0;
					final float v0 = cdfValues[i - 1];
					final float v1 = cdfValues[i];
					return v0 + ((v1 - v0) * u);
				}
			}
			return cdfValues[n - 1];
		}
	}

	public static final class Shape {
		public int type = SHAPE_BOX;
		public int dimensionality = DIM_SURFACE;
		public final float[] boxDimensions = { 0.5f, 0.5f, 0.5f };
		public float radius;
		public float innerRadius;
		public float height;
		public boolean hemisphere;
		public final float[] position = new float[3];
		public final float[] eulerOrientation = new float[3];
		public final float[] nonUniformScale = { 1, 1, 1 };
		public boolean transformTranslate;
		public boolean transformRotate;
		public String meshResource = "";
		public final float[] meshScale = { 1, 1, 1 };
		public int meshSamplingMode = 1;
	}

	public static final class EventStream {
		public float[] times = new float[0];
	}

	public static final class Turbulence {
		public boolean external;
		public float strength = 0.1f;
		public float wavelength = 0.5f;
		public float globalScale = 1f;
		public float lacunarity = 0.5f;
		public float gain = 0.5f;
		public float gainMultiplier = 1f;
		public int octaves = 2;
		public int interpolator = 1;
		public int seed = 1114229502;
		public float timeScale;
		public float timeBase;
		public float timeRandomVariation = 0.5f;
	}

	public String name = "";
	public int kind = KIND_UNKNOWN;
	public final Curve curve = new Curve();
	public final Shape shape = new Shape();
	public final EventStream eventStream = new EventStream();
	public final Turbulence turbulence = new Turbulence();
	public int textureScriptOutputType = 4;
	/** Built lazily by the binder for procedural turbulence samplers. */
	public TurbulenceField turbulenceField;

	public static SamplerResource findByName(final SamplerResource[] samplers, final String name) {
		if ((samplers == null) || (name == null)) {
			return null;
		}
		for (final SamplerResource s : samplers) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name + ":" + kind;
	}
}
