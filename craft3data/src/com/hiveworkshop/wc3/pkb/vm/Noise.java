package com.hiveworkshop.wc3.pkb.vm;

/** Simplex noise in one to four dimensions with the engine's permutation table. */
public final class Noise {
	private static final int[] PERM = { 0x97, 0xA0, 0x89, 0x5B, 0x5A, 0x0F, 0x83, 0x0D, 0xC9, 0x5F, 0x60, 0x35, 0xC2,
			0xE9, 0x07, 0xE1, 0x8C, 0x24, 0x67, 0x1E, 0x45, 0x8E, 0x08, 0x63, 0x25, 0xF0, 0x15, 0x0A, 0x17, 0xBE, 0x06,
			0x94, 0xF7, 0x78, 0xEA, 0x4B, 0x00, 0x1A, 0xC5, 0x3E, 0x5E, 0xFC, 0xDB, 0xCB, 0x75, 0x23, 0x0B, 0x20, 0x39,
			0xB1, 0x21, 0x58, 0xED, 0x95, 0x38, 0x57, 0xAE, 0x14, 0x7D, 0x88, 0xAB, 0xA8, 0x44, 0xAF, 0x4A, 0xA5, 0x47,
			0x86, 0x8B, 0x30, 0x1B, 0xA6, 0x4D, 0x92, 0x9E, 0xE7, 0x53, 0x6F, 0xE5, 0x7A, 0x3C, 0xD3, 0x85, 0xE6, 0xDC,
			0x69, 0x5C, 0x29, 0x37, 0x2E, 0xF5, 0x28, 0xF4, 0x66, 0x8F, 0x36, 0x41, 0x19, 0x3F, 0xA1, 0x01, 0xD8, 0x50,
			0x49, 0xD1, 0x4C, 0x84, 0xBB, 0xD0, 0x59, 0x12, 0xA9, 0xC8, 0xC4, 0x87, 0x82, 0x74, 0xBC, 0x9F, 0x56, 0xA4,
			0x64, 0x6D, 0xC6, 0xAD, 0xBA, 0x03, 0x40, 0x34, 0xD9, 0xE2, 0xFA, 0x7C, 0x7B, 0x05, 0xCA, 0x26, 0x93, 0x76,
			0x7E, 0xFF, 0x52, 0x55, 0xD4, 0xCF, 0xCE, 0x3B, 0xE3, 0x2F, 0x10, 0x3A, 0x11, 0xB6, 0xBD, 0x1C, 0x2A, 0xDF,
			0xB7, 0xAA, 0xD5, 0x77, 0xF8, 0x98, 0x02, 0x2C, 0x9A, 0xA3, 0x46, 0xDD, 0x99, 0x65, 0x9B, 0xA7, 0x2B, 0xAC,
			0x09, 0x81, 0x16, 0x27, 0xFD, 0x13, 0x62, 0x6C, 0x6E, 0x4F, 0x71, 0xE0, 0xE8, 0xB2, 0xB9, 0x70, 0x68, 0xDA,
			0xF6, 0x61, 0xE4, 0xFB, 0x22, 0xF2, 0xC1, 0xEE, 0xD2, 0x90, 0x0C, 0xBF, 0xB3, 0xA2, 0xF1, 0x51, 0x33, 0x91,
			0xEB, 0xF9, 0x0E, 0xEF, 0x6B, 0x31, 0xC0, 0xD6, 0x1F, 0xB5, 0xC7, 0x6A, 0x9D, 0xB8, 0x54, 0xCC, 0xB0, 0x73,
			0x79, 0x32, 0x2D, 0x7F, 0x04, 0x96, 0xFE, 0x8A, 0xEC, 0xCD, 0x5D, 0xDE, 0x72, 0x43, 0x1D, 0x18, 0x48, 0xF3,
			0x8D, 0x80, 0xC3, 0x4E, 0x42, 0xD7, 0x3D, 0x9C, 0xB4 };

	private Noise() {
	}

	private static int perm(final int i) {
		return PERM[i & 255];
	}

	private static float grad1(final int hash) {
		final float mag = (hash & 7) + 1.0f;
		return ((hash & 8) != 0) ? -mag : mag;
	}

	public static float simplex1(final float x) {
		final float i0 = (float) Math.floor(x);
		final float x0 = x - i0;
		final float x1 = x0 - 1.0f;
		final int u0 = ((int) i0) & 0xFF;
		final float t0 = 1.0f - (x0 * x0);
		final float t1 = 1.0f - (x1 * x1);
		final float t0sqr = t0 * t0;
		final float t1sqr = t1 * t1;
		final float n0 = t0sqr * t0sqr * x0 * grad1(perm(u0));
		final float n1 = t1sqr * t1sqr * x1 * grad1(perm(u0 + 1));
		return 0.39500001f * (n0 + n1);
	}

	private static float grad2(final int hash, final float x, final float y) {
		final int h = hash & 7;
		final float u = (h >= 4) ? y : x;
		final float v = (h >= 4) ? x : y;
		final float su = ((hash & 1) != 0) ? -u : u;
		final float sv = ((hash & 2) != 0) ? -v : v;
		return su + (2.0f * sv);
	}

	private static int fastFloor(final float v) {
		return (v <= 0) ? (((int) v) - 1) : (int) v;
	}

	private static float corner2(final float t, final int hash, final float cx, final float cy) {
		if (t < 0) {
			return 0;
		}
		final float t2 = t * t;
		return t2 * t2 * grad2(hash, cx, cy);
	}

	public static float simplex2(final float x, final float y) {
		final float kF2 = 0.36602539f;
		final float kG2 = 0.21132487f;
		final float kG2x2 = 0.42264974f;
		final float s = (x + y) * kF2;
		final int i = fastFloor(x + s);
		final int j = fastFloor(y + s);
		final float t = (i + j) * kG2;
		final float x0 = x - (i - t);
		final float y0 = y - (j - t);
		final int i1 = (x0 <= y0) ? 0 : 1;
		final int j1 = (x0 <= y0) ? 1 : 0;
		final float x1 = (x0 - i1) + kG2;
		final float y1 = (y0 - j1) + kG2;
		final float x2 = (x0 - 1.0f) + kG2x2;
		final float y2 = (y0 - 1.0f) + kG2x2;
		final int ii = i & 0xFF;
		final int jj = j & 0xFF;
		final float n0 = corner2((0.5f - (x0 * x0)) - (y0 * y0), perm(perm(jj) + ii), x0, y0);
		final float n1 = corner2((0.5f - (x1 * x1)) - (y1 * y1), perm(perm(j1 + jj) + i1 + ii), x1, y1);
		final float n2 = corner2((0.5f - (x2 * x2)) - (y2 * y2), perm(ii + 1 + perm(jj + 1)), x2, y2);
		return 40.0f * ((n0 + n1) + n2);
	}

	private static float grad3(final int hash, final float x, final float y, final float z) {
		final int h = hash & 15;
		final float u = (h < 8) ? x : y;
		final float b = ((h == 12) || (h == 14)) ? x : z;
		final float v = (h < 4) ? y : b;
		final float su = ((h & 1) != 0) ? -u : u;
		final float sv = ((h & 2) != 0) ? -v : v;
		return su + sv;
	}

	private static final int[][] ORDER3 = { { 1, 0, 0, 1, 1, 0 }, { 0, 1, 0, 0, 1, 1 }, { 0, 0, 1, 1, 0, 1 },
			{ 0, 0, 1, 0, 1, 1 }, { 1, 0, 0, 1, 1, 0 }, { 0, 1, 0, 1, 1, 0 }, { 1, 0, 0, 1, 0, 1 },
			{ 0, 0, 1, 0, 1, 1 } };

	public static float simplex3(final float x, final float y, final float z) {
		final float kF3 = 0.33333334f;
		final float kG3 = 0.16666667f;
		final float[] kStep1 = { kG3, -1.0f + kG3 };
		final float[] kStep2 = { kG3 + kG3, -1.0f + (kG3 + kG3) };
		final float kStep3 = -0.5f;
		final float s = ((x + y) + z) * kF3;
		final float fi = (float) Math.floor(x + s);
		final float fj = (float) Math.floor(y + s);
		final float fk = (float) Math.floor(z + s);
		final float t = ((fi + fj) + fk) * kG3;
		final float x0 = (x - fi) + t;
		final float y0 = (y - fj) + t;
		final float z0 = (z - fk) + t;
		final int mask = (x0 < y0 ? 1 : 0) | (y0 < z0 ? 2 : 0) | (z0 < x0 ? 4 : 0);
		final int[] o = ORDER3[mask];
		final int ii = ((int) fi) & 0xFF;
		final int jj = ((int) fj) & 0xFF;
		final int kk = ((int) fk) & 0xFF;
		final float[] cx = { x0, x0 + kStep1[o[0]], x0 + kStep2[o[3]], x0 + kStep3 };
		final float[] cy = { y0, y0 + kStep1[o[1]], y0 + kStep2[o[4]], y0 + kStep3 };
		final float[] cz = { z0, z0 + kStep1[o[2]], z0 + kStep2[o[5]], z0 + kStep3 };
		final int[] h = { perm(perm(perm(kk) + jj) + ii), perm(perm(perm(kk + o[2]) + jj + o[1]) + ii + o[0]),
				perm(perm(perm(kk + o[5]) + jj + o[4]) + ii + o[3]), perm(perm(perm(kk + 1) + 1 + jj) + 1 + ii) };
		float sum = 0;
		for (int c = 0; c < 4; c++) {
			final float raw = ((-0.60000002f + (cx[c] * cx[c])) + (cy[c] * cy[c])) + (cz[c] * cz[c]);
			final float tc = (raw < 0) ? raw : 0;
			final float tc2 = tc * tc;
			sum += tc2 * tc2 * grad3(h[c], cx[c], cy[c], cz[c]);
		}
		return 32.0f * sum;
	}

	private static float grad4(final int hash, final float x, final float y, final float z, final float w) {
		final int h = hash & 31;
		final float u = (h < 24) ? x : y;
		final float v = (h < 16) ? y : z;
		final float t = (h < 8) ? z : w;
		final float su = ((hash & 1) != 0) ? -u : u;
		final float sv = ((hash & 2) != 0) ? -v : v;
		final float st = ((hash & 4) != 0) ? -t : t;
		return (su + sv) + st;
	}

	private static final int[][] ORDER4 = { { 0, 1, 2, 3 }, { 1, 0, 2, 3 }, { 0, 2, 1, 3 }, { 0, 0, 0, 0 },
			{ 0, 1, 3, 2 }, { 1, 0, 3, 2 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 2, 0, 1, 3 },
			{ 1, 2, 0, 3 }, { 2, 1, 0, 3 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
			{ 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 2, 0, 3, 1 },
			{ 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 3, 0, 1, 2 }, { 0, 0, 0, 0 }, { 3, 1, 0, 2 },
			{ 0, 0, 0, 0 }, { 3, 0, 2, 1 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
			{ 0, 3, 1, 2 }, { 0, 0, 0, 0 }, { 0, 2, 3, 1 }, { 0, 0, 0, 0 }, { 0, 3, 2, 1 }, { 0, 0, 0, 0 },
			{ 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 1, 3, 0, 2 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
			{ 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
			{ 1, 2, 3, 0 }, { 2, 1, 3, 0 }, { 1, 3, 2, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 },
			{ 2, 3, 0, 1 }, { 3, 2, 0, 1 }, { 0, 0, 0, 0 }, { 3, 1, 2, 0 }, { 2, 3, 1, 0 }, { 3, 2, 1, 0 } };

	public static float simplex4(final float x, final float y, final float z, final float w) {
		final float kF4 = 0.309017f;
		final float kG4 = 0.1381966f;
		final float kG4x2 = 0.2763932f;
		final float kG4x3 = 0.41458982f;
		final float kG4x4m1 = -0.44721359f;
		final float s = (((x + y) + z) + w) * kF4;
		final float fi = (float) Math.floor(x + s);
		final float fj = (float) Math.floor(y + s);
		final float fk = (float) Math.floor(z + s);
		final float fl = (float) Math.floor(w + s);
		final float t = (((fi + fj) + fk) + fl) * kG4;
		final float[] p0 = { (x - fi) + t, (y - fj) + t, (z - fk) + t, (w - fl) + t };
		final int xc = (p0[0] > p0[1] ? 1 : 0) | (p0[1] > p0[2] ? 2 : 0) | (p0[2] > p0[3] ? 4 : 0)
				| (p0[0] > p0[2] ? 8 : 0) | (p0[0] > p0[3] ? 16 : 0) | (p0[1] > p0[3] ? 32 : 0);
		final int[] rank = ORDER4[xc];
		final int[] base = { ((int) fi) & 0xFF, ((int) fj) & 0xFF, ((int) fk) & 0xFF, ((int) fl) & 0xFF };
		final float[][] c = new float[5][4];
		final int[][] idx = new int[5][4];
		for (int a = 0; a < 4; a++) {
			final int s1 = (rank[a] > 2) ? 1 : 0;
			final int s2 = (rank[a] > 1) ? 1 : 0;
			final int s3 = (rank[a] > 0) ? 1 : 0;
			c[0][a] = p0[a];
			c[1][a] = (p0[a] - s1) + kG4;
			c[2][a] = (p0[a] - s2) + kG4x2;
			c[3][a] = (p0[a] - s3) + kG4x3;
			c[4][a] = p0[a] + kG4x4m1;
			idx[0][a] = base[a];
			idx[1][a] = base[a] + s1;
			idx[2][a] = base[a] + s2;
			idx[3][a] = base[a] + s3;
			idx[4][a] = base[a] + 1;
		}
		float sum = 0;
		for (int k = 0; k < 5; k++) {
			final float tc = 0.60000002f
					- ((((c[k][0] * c[k][0]) + (c[k][1] * c[k][1])) + (c[k][2] * c[k][2])) + (c[k][3] * c[k][3]));
			if (tc < 0) {
				continue;
			}
			final int h = perm(perm(perm(perm(idx[k][3]) + idx[k][2]) + idx[k][1]) + idx[k][0]);
			final float tc2 = tc * tc;
			sum += tc2 * tc2 * grad4(h, c[k][0], c[k][1], c[k][2], c[k][3]);
		}
		return 27.0f * sum;
	}
}
