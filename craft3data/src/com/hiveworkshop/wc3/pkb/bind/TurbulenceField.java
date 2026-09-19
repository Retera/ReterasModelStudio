package com.hiveworkshop.wc3.pkb.bind;

/** Procedural curl-noise turbulence, the engine's "Turbulence" sampler with DataSource = Procedural. */
public final class TurbulenceField {
	public static final int GRADIENT_COUNT = 256;
	private static final int MAX_OCTAVES = 24;
	private static final float TAU = 6.2831853071795864f;
	private static final float PI = 3.1415926535897932f;

	private static final int[] FAST_NOISE_PERM = { 151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7,
			225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197,
			62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168,
			68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220,
			105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
			89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226,
			250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189,
			28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129,
			22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242,
			193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181,
			199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29,
			24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180 };

	private static final float[] OFFSET1 = { 31.416f, 47.853001f, 12.793f };
	private static final float[] OFFSET2 = { -233.145f, -113.408f, -185.31f };

	private final SamplerResource.Turbulence params;
	private final float[] rigidBasis = new float[GRADIENT_COUNT];
	private final float[] spinRate = new float[GRADIENT_COUNT];
	private final float[] gradients = new float[GRADIENT_COUNT];
	private final float[] rotated = new float[GRADIENT_COUNT];
	private final float delta = 1.0e-4f;
	private final float dnorm = 5000.0f;

	public TurbulenceField(final SamplerResource.Turbulence params) {
		this.params = params;
		generateBasis(params.seed, params.timeRandomVariation);
		rotateBasis(0, gradients);
	}

	/** Minimal MT19937 (32-bit Mersenne Twister), seeded the way {@code std::mt19937(seed)} is. */
	private static final class Mt19937 {
		private final int[] mt = new int[624];
		private int index;

		Mt19937(final int seed) {
			mt[0] = seed;
			for (int i = 1; i < 624; i++) {
				mt[i] = (1812433253 * (mt[i - 1] ^ (mt[i - 1] >>> 30))) + i;
			}
			index = 624;
		}

		int next() {
			if (index >= 624) {
				for (int i = 0; i < 624; i++) {
					final int y = (mt[i] & 0x80000000) | (mt[(i + 1) % 624] & 0x7fffffff);
					int v = mt[(i + 397) % 624] ^ (y >>> 1);
					if ((y & 1) != 0) {
						v ^= 0x9908b0df;
					}
					mt[i] = v;
				}
				index = 0;
			}
			int y = mt[index++];
			y ^= y >>> 11;
			y ^= (y << 7) & 0x9d2c5680;
			y ^= (y << 15) & 0xefc60000;
			y ^= y >>> 18;
			return y;
		}
	}

	private void generateBasis(final int seed, final float timeRandomVariation) {
		final boolean uniformSpin = timeRandomVariation < 1.0e-5f;
		final Mt19937 mt = new Mt19937(seed);
		for (int i = 0; i < GRADIENT_COUNT; i++) {
			rigidBasis[i] = ((mt.next() >>> 8) * (1.0f / 16777216.0f)) * TAU;
			spinRate[i] = uniformSpin ? TAU
					: TAU + ((((mt.next() >>> 8) * (1.0f / 16777216.0f) * 2.0f) - 1.0f) * timeRandomVariation * PI);
		}
	}

	private void rotateBasis(final float rotation, final float[] out) {
		for (int i = 0; i < GRADIENT_COUNT; i++) {
			out[i] = (float) Math.sin(rigidBasis[i] + (rotation * spinRate[i]));
		}
	}

	private static float fadeT(final float t, final int interp) {
		switch (interp) {
		case 1:
			return t * t * (3.0f - (2.0f * t));
		case 2:
			return ((((6.0f * t) - 15.0f) * t) + 10.0f) * t * t * t;
		default:
			return t;
		}
	}

	private static float corner(final float[] grad, final int xi, final int yi, final int zi) {
		final int hx = FAST_NOISE_PERM[xi & 255];
		final int hy = FAST_NOISE_PERM[(hx + yi) & 255];
		final int h = FAST_NOISE_PERM[(hy + zi) & 255];
		return grad[h & 255];
	}

	private static float coherentNoise(final float px, final float py, final float pz, final float[] grad,
			final int interp) {
		final float fx = (float) Math.floor(px);
		final float fy = (float) Math.floor(py);
		final float fz = (float) Math.floor(pz);
		final int cellX = (int) fx;
		final int cellY = (int) fy;
		final int cellZ = (int) fz;
		final float u = fadeT(px - fx, interp);
		final float v = fadeT(py - fy, interp);
		final float w = fadeT(pz - fz, interp);
		final float c000 = corner(grad, cellX, cellY, cellZ);
		final float c100 = corner(grad, cellX + 1, cellY, cellZ);
		final float c010 = corner(grad, cellX, cellY + 1, cellZ);
		final float c110 = corner(grad, cellX + 1, cellY + 1, cellZ);
		final float c001 = corner(grad, cellX, cellY, cellZ + 1);
		final float c101 = corner(grad, cellX + 1, cellY, cellZ + 1);
		final float c011 = corner(grad, cellX, cellY + 1, cellZ + 1);
		final float c111 = corner(grad, cellX + 1, cellY + 1, cellZ + 1);
		final float x00 = c000 + (u * (c100 - c000));
		final float x10 = c010 + (u * (c110 - c010));
		final float x01 = c001 + (u * (c101 - c001));
		final float x11 = c011 + (u * (c111 - c011));
		final float y0v = x00 + (v * (x10 - x00));
		final float y1v = x01 + (v * (x11 - x01));
		return y0v + (w * (y1v - y0v));
	}

	private final float[] octInvFreq = new float[MAX_OCTAVES];
	private final float[] octAmp = new float[MAX_OCTAVES];

	private int buildOctaves() {
		final SamplerResource.Turbulence t = params;
		float wavelength = t.globalScale * t.wavelength;
		if (!(wavelength > 0) && !(wavelength < 0)) {
			wavelength = 1.0e-7f;
		}
		final int octaves = Math.max(1, t.octaves);
		final float lac = Math.max(t.lacunarity, (float) Math.pow(1.0e-7f / wavelength, 1.0f / octaves));
		final float g = t.gain * t.gainMultiplier;
		float amp = t.globalScale * t.strength;
		if ((lac == 1.0f) || (g == 0.0f)) {
			float ampSum = 0;
			float a = amp;
			for (int i = 0; i < octaves; i++) {
				ampSum += a;
				a *= g;
			}
			octInvFreq[0] = 1.0f / wavelength;
			octAmp[0] = ampSum;
			return 1;
		}
		final int count = Math.min(octaves, MAX_OCTAVES);
		for (int i = 0; i < count; i++) {
			octInvFreq[i] = 1.0f / wavelength;
			octAmp[i] = amp;
			wavelength *= lac;
			amp *= g;
		}
		return count;
	}

	private final float[] g3 = new float[3];

	private void noiseGradient(final float sx, final float sy, final float sz, final float[] grad) {
		final float d = delta;
		final float n = dnorm;
		final int interp = params.interpolator;
		g3[0] = (coherentNoise(sx + d, sy, sz, grad, interp) - coherentNoise(sx - d, sy, sz, grad, interp)) * n;
		g3[1] = (coherentNoise(sx, sy + d, sz, grad, interp) - coherentNoise(sx, sy - d, sz, grad, interp)) * n;
		g3[2] = (coherentNoise(sx, sy, sz + d, grad, interp) - coherentNoise(sx, sy, sz - d, grad, interp)) * n;
	}

	/** Curl-noise velocity at a world position; {@code out} receives x, y, z. */
	public void sampleVelocity(final float wx, final float wy, final float wz, final float time, final float[] out) {
		final float px = wx;
		final float py = wz;
		final float pz = -wy;
		final float rotation = (time * params.timeScale) + params.timeBase;
		float[] grad = gradients;
		if (rotation != 0) {
			rotateBasis(rotation, rotated);
			grad = rotated;
		}
		final int count = buildOctaves();
		float dPsiXx = 0, dPsiXy = 0, dPsiXz = 0;
		float dPsiYx = 0, dPsiYy = 0, dPsiYz = 0;
		float dPsiZx = 0, dPsiZy = 0, dPsiZz = 0;
		for (int i = 0; i < count; i++) {
			final float f = octInvFreq[i];
			final float a = octAmp[i];
			noiseGradient(f * px, f * py, f * pz, grad);
			dPsiXx += a * g3[0];
			dPsiXy += a * g3[1];
			dPsiXz += a * g3[2];
			noiseGradient((f * px) + OFFSET1[0], (f * py) + OFFSET1[1], (f * pz) + OFFSET1[2], grad);
			dPsiYx += a * g3[0];
			dPsiYy += a * g3[1];
			dPsiYz += a * g3[2];
			noiseGradient((f * px) + OFFSET2[0], (f * py) + OFFSET2[1], (f * pz) + OFFSET2[2], grad);
			dPsiZx += a * g3[0];
			dPsiZy += a * g3[1];
			dPsiZz += a * g3[2];
		}
		final float curlX = dPsiZy - dPsiYz;
		final float curlY = dPsiXz - dPsiZx;
		final float curlZ = dPsiYx - dPsiXy;
		out[0] = curlX;
		out[1] = -curlZ;
		out[2] = curlY;
	}
}
