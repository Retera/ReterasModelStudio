package com.hiveworkshop.wc3.pkb.vm;

import com.hiveworkshop.wc3.pkb.bind.SamplerResource;
import com.hiveworkshop.wc3.pkb.bind.SamplerResource.Shape;
import com.hiveworkshop.wc3.pkb.sim.FastRand;

/**
 * Primitive shape sampling and geometry queries (box, sphere, ellipsoid,
 * cylinder, capsule, cone). Mesh shapes have no data in this port and every
 * query on them reports "unhandled".
 */
public final class ShapeSampling {
	private static final float PI = 3.14159265358979323846f;
	private static final float TWO_PI = 6.28318530717958647692f;
	private static final int[] WRAP_LOOKUP = { 1, 2, 0, 1 };
	private static final float[] UNIT_BOX_FACE_LIMITS = { -5.0f / 6.0f, -0.5f, -1.0f / 6.0f, 1.0f / 6.0f,
			1.0f / 6.0f, 0.5f, 5.0f / 6.0f };

	private ShapeSampling() {
	}

	// ------------------------------------------------------------------ helpers

	public static float halfToUnitBits(final int half, final float bias) {
		final int bits = ((half & 0x7FFF) << 8) | Bank.ONE_F32_BITS;
		return Float.intBitsToFloat(bits) - bias;
	}

	private static int unitToHalf(final float v) {
		final float maxOne = Float.intBitsToFloat(0x3FFFFFFF);
		final float one = Math.min(v + 1.0f, maxOne);
		final int bits = Float.floatToRawIntBits(one);
		return (bits & 0x00FFFF00) >>> 8;
	}

	private static int packHalves(final float low, final float high) {
		return (unitToHalf(high) << 16) | unitToHalf(low);
	}

	private static float clamp(final float v, final float lo, final float hi) {
		return v < lo ? lo : (v > hi ? hi : v);
	}

	private static boolean hasRotation(final Shape sh) {
		return (sh.eulerOrientation[0] != 0) || (sh.eulerOrientation[1] != 0) || (sh.eulerOrientation[2] != 0);
	}

	/** Rotates a local direction by the shape's euler orientation (Y and Z swapped around the rotation). */
	public static void rotateDirection(final Shape sh, final float x, final float y, final float z,
			final float[] out) {
		if (!hasRotation(sh)) {
			out[0] = x;
			out[1] = y;
			out[2] = z;
			return;
		}
		final float cx = (float) Math.cos(sh.eulerOrientation[0]);
		final float sx = (float) Math.sin(sh.eulerOrientation[0]);
		final float cy = (float) Math.cos(sh.eulerOrientation[1]);
		final float sy = (float) Math.sin(sh.eulerOrientation[1]);
		final float cz = (float) Math.cos(sh.eulerOrientation[2]);
		final float sz = (float) Math.sin(sh.eulerOrientation[2]);
		final float ix = x;
		final float iy = z;
		final float iz = -y;
		final float y1 = (cx * iy) - (sx * iz);
		final float z1 = (sx * iy) + (cx * iz);
		final float x2 = (cy * ix) + (sy * z1);
		final float z2 = (-sy * ix) + (cy * z1);
		final float x3 = (cz * x2) - (sz * y1);
		final float y3 = (sz * x2) + (cz * y1);
		out[0] = x3;
		out[1] = -z2;
		out[2] = y3;
	}

	public static void unrotateDirection(final Shape sh, final float x, final float y, final float z,
			final float[] out) {
		if (!hasRotation(sh)) {
			out[0] = x;
			out[1] = y;
			out[2] = z;
			return;
		}
		final float cx = (float) Math.cos(sh.eulerOrientation[0]);
		final float sx = (float) Math.sin(sh.eulerOrientation[0]);
		final float cy = (float) Math.cos(sh.eulerOrientation[1]);
		final float sy = (float) Math.sin(sh.eulerOrientation[1]);
		final float cz = (float) Math.cos(sh.eulerOrientation[2]);
		final float sz = (float) Math.sin(sh.eulerOrientation[2]);
		final float ix = x;
		final float iy = z;
		final float iz = -y;
		final float x1 = (cz * ix) + (sz * iy);
		final float y1 = (-sz * ix) + (cz * iy);
		final float x2 = (cy * x1) - (sy * iz);
		final float z2 = (sy * x1) + (cy * iz);
		final float y3 = (cx * y1) + (sx * z2);
		final float z3 = (-sx * y1) + (cx * z2);
		out[0] = x2;
		out[1] = -z3;
		out[2] = y3;
	}

	public static void worldToLocal(final Shape sh, final float[] p, final float[] out) {
		float x = p[0];
		float y = p[1];
		float z = p[2];
		if (sh.transformTranslate) {
			x -= sh.position[0];
			y -= sh.position[1];
			z -= sh.position[2];
		}
		if (!sh.transformRotate) {
			out[0] = x;
			out[1] = y;
			out[2] = z;
			return;
		}
		unrotateDirection(sh, x, y, z, out);
	}

	public static void localToWorld(final Shape sh, final float[] p, final float[] out) {
		float x = p[0];
		float y = p[1];
		float z = p[2];
		if (sh.transformRotate) {
			rotateDirection(sh, x, y, z, out);
			x = out[0];
			y = out[1];
			z = out[2];
		}
		if (sh.transformTranslate) {
			x += sh.position[0];
			y += sh.position[1];
			z += sh.position[2];
		}
		out[0] = x;
		out[1] = y;
		out[2] = z;
	}

	/** The engine's samplePosition post-transform: ellipsoid scale, rotation, translation. */
	public static void applyShapeTrs(final Shape sh, final float lx, final float ly, final float lz,
			final RegisterValue out) {
		final boolean scaled = sh.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID;
		float sx = scaled ? lx * sh.nonUniformScale[0] : lx;
		float sy = scaled ? ly * sh.nonUniformScale[1] : ly;
		float sz = scaled ? lz * sh.nonUniformScale[2] : lz;
		if (sh.transformRotate && hasRotation(sh)) {
			final float cx = (float) Math.cos(sh.eulerOrientation[0]);
			final float sxn = (float) Math.sin(sh.eulerOrientation[0]);
			final float cy = (float) Math.cos(sh.eulerOrientation[1]);
			final float syn = (float) Math.sin(sh.eulerOrientation[1]);
			final float cz = (float) Math.cos(sh.eulerOrientation[2]);
			final float szn = (float) Math.sin(sh.eulerOrientation[2]);
			final float ix = sx;
			final float iy = sz;
			final float iz = -sy;
			final float x1 = ix;
			final float y1 = (cx * iy) - (sxn * iz);
			final float z1 = (sxn * iy) + (cx * iz);
			final float x2 = (cy * x1) + (syn * z1);
			final float y2 = y1;
			final float z2 = (-syn * x1) + (cy * z1);
			final float x3 = (cz * x2) - (szn * y2);
			final float y3 = (szn * x2) + (cz * y2);
			final float z3 = z2;
			sx = x3;
			sy = -z3;
			sz = y3;
		}
		if (sh.transformTranslate) {
			sx += sh.position[0];
			sy += sh.position[1];
			sz += sh.position[2];
		}
		out.setFloat3(sx, sy, sz);
	}

	// ------------------------------------------------------------------ random sampling

	/**
	 * Draws a random point on/in the shape (or evaluates the parametric
	 * coordinates {@code pc} when given). Returns false for mesh shapes.
	 */
	public static boolean samplePosition(final Shape sh, final FastRand rng, final float[] pc, final RegisterValue out) {
		final boolean volume = sh.dimensionality == SamplerResource.DIM_VOLUME;
		if (sh.type == SamplerResource.SHAPE_MESH) {
			return false;
		}
		final float[] p = new float[3];
		if (pc != null) {
			if (!positionFromPCoords(sh, pc, volume, p)) {
				return false;
			}
			applyShapeTrs(sh, p[0], p[1], p[2], out);
			return true;
		}
		switch (sh.type) {
		case SamplerResource.SHAPE_CAPSULE: {
			final float outerR = sh.radius;
			final float innerR = sh.innerRadius;
			final float height = sh.height;
			float cylPart;
			float sphPart;
			if (volume) {
				cylPart = PI * Math.max(0, (outerR * outerR) - (innerR * innerR)) * height;
				sphPart = (4.0f / 3.0f) * PI * Math.max(0, (outerR * outerR * outerR) - (innerR * innerR * innerR));
			} else {
				cylPart = TWO_PI * (outerR + innerR) * height;
				sphPart = 2.0f * TWO_PI * ((outerR * outerR) + (innerR > 0 ? innerR * innerR : 0));
			}
			final float total = cylPart + sphPart;
			if ((total > 0) && ((rng.unit() * total) < cylPart)) {
				if (volume) {
					cylinderVolumePoint(rng, outerR, innerR, height, p);
				} else {
					cylinderSurfacePoint(rng, outerR, innerR, height, p);
				}
			} else {
				if (volume) {
					sphereVolumePoint(rng, outerR, innerR, p);
				} else {
					sphereSurfacePoint(rng, outerR, innerR, p);
				}
				p[2] += (p[2] >= 0 ? 0.5f : -0.5f) * height;
			}
			applyShapeTrs(sh, p[0], p[1], p[2], out);
			return true;
		}
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			if (volume) {
				sphereVolumePoint(rng, sh.radius, sh.innerRadius, p);
			} else {
				sphereSurfacePoint(rng, sh.radius, sh.innerRadius, p);
			}
			if ((sh.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID) && sh.hemisphere && (p[2] < 0)) {
				p[2] = -p[2];
			}
			applyShapeTrs(sh, p[0], p[1], p[2], out);
			return true;
		}
		case SamplerResource.SHAPE_BOX: {
			final float[] dim = sh.boxDimensions;
			final float ex = dim[0] * 0.5f;
			final float ey = dim[1] * 0.5f;
			final float ez = dim[2] * 0.5f;
			final float r0 = (rng.unit() * 2.0f) - 1.0f;
			final float r1 = (rng.unit() * 2.0f) - 1.0f;
			final float r2 = (rng.unit() * 2.0f) - 1.0f;
			final float surf0 = dim[1] * dim[2];
			final float surf1 = dim[2] * dim[0];
			final float surf2 = dim[0] * dim[1];
			final float total = surf0 + surf1 + surf2;
			if (volume || (total == 0)) {
				applyShapeTrs(sh, r0 * ex, r1 * ey, r2 * ez, out);
				return true;
			}
			boxSurfacePoint(new float[] { ex, ey, ez }, surf0, surf1, total, r0, r1, r2, p);
			applyShapeTrs(sh, p[0], p[1], p[2], out);
			return true;
		}
		case SamplerResource.SHAPE_CYLINDER: {
			if (volume) {
				cylinderVolumePoint(rng, sh.radius, sh.innerRadius, sh.height, p);
			} else {
				cylinderSurfacePoint(rng, sh.radius, sh.innerRadius, sh.height, p);
			}
			applyShapeTrs(sh, p[0], p[1], p[2], out);
			return true;
		}
		case SamplerResource.SHAPE_CONE: {
			final float radius = sh.radius;
			final float height = sh.height;
			final float angle = rng.unit() * TWO_PI;
			if (volume) {
				final float v = (float) Math.cbrt(rng.unit());
				final float r = radius * v * (float) Math.sqrt(rng.unit());
				applyShapeTrs(sh, r * (float) Math.sin(angle), r * (float) Math.cos(angle), height * (1.0f - v), out);
				return true;
			}
			final float s = (float) Math.sqrt(rng.unit());
			applyShapeTrs(sh, radius * s * (float) Math.sin(angle), radius * s * (float) Math.cos(angle),
					height * (1.0f - s), out);
			return true;
		}
		default:
			return false;
		}
	}

	private static void boxSurfacePoint(final float[] extent, final float surf0, final float surf1, final float total,
			final float r0, final float r1, final float r2, final float[] p) {
		final int r2Bits = Float.floatToRawIntBits(r2);
		final int signFlip = r2Bits & 0x80000000;
		final int surfBits = r2Bits & 0x7FFFFFFF;
		final float limit0 = surf0 / total;
		final float limit1 = (surf0 + surf1) / total;
		final int limit0Bits = Float.floatToRawIntBits(limit0);
		final int limit1Bits = Float.floatToRawIntBits(limit1);
		final int id0 = ((limit1Bits - surfBits) >>> 31) + ((limit0Bits - surfBits) >>> 31);
		final int id1 = WRAP_LOOKUP[id0];
		final int id2 = WRAP_LOOKUP[id0 + 1];
		p[0] = p[1] = p[2] = 0;
		p[id0] = Float.intBitsToFloat(Float.floatToRawIntBits(extent[id0]) ^ signFlip);
		p[id1] = r0 * extent[id1];
		p[id2] = r1 * extent[id2];
	}

	private static void sphereVolumePoint(final FastRand rng, final float outerR, final float innerR,
			final float[] p) {
		final float angle = rng.unit() * TWO_PI;
		final int packed = rng.advance();
		final float cosTheta = 1.0f - (2.0f * halfToUnitBits(packed, 1.0f));
		final float sinTheta = (float) Math.sqrt(Math.max(0, 1.0f - (cosTheta * cosTheta)));
		final float r3min = innerR * innerR * innerR;
		final float r3max = outerR * outerR * outerR;
		final float r = (float) Math.cbrt(r3min + ((r3max - r3min) * halfToUnitBits(packed >>> 16, 1.0f)));
		p[0] = r * sinTheta * (float) Math.cos(angle);
		p[1] = r * sinTheta * (float) Math.sin(angle);
		p[2] = r * cosTheta;
	}

	private static void sphereSurfacePoint(final FastRand rng, final float outerR, final float innerR,
			final float[] p) {
		final float angle = rng.unit() * TWO_PI;
		final int packed = rng.advance();
		final float cosTheta = 1.0f - (2.0f * halfToUnitBits(packed, 1.0f));
		final float sinTheta = (float) Math.sqrt(Math.max(0, 1.0f - (cosTheta * cosTheta)));
		float shell = outerR;
		if (innerR > 0) {
			final float outerA = outerR * outerR;
			final float total = outerA + (innerR * innerR);
			if ((total > 0) && ((halfToUnitBits(packed >>> 16, 1.0f) * total) >= outerA)) {
				shell = innerR;
			}
		}
		p[0] = shell * sinTheta * (float) Math.cos(angle);
		p[1] = shell * sinTheta * (float) Math.sin(angle);
		p[2] = shell * cosTheta;
	}

	private static float cylinderRadius(final float outerR, final float innerR, final float u) {
		final float expo = 0.5f * ((outerR > 0 ? innerR / outerR : 0) + 1.0f);
		return ((float) Math.pow(u, expo) * (outerR - innerR)) + innerR;
	}

	private static void cylinderVolumePoint(final FastRand rng, final float outerR, final float innerR,
			final float height, final float[] p) {
		final float angle = rng.unit() * TWO_PI;
		final int packed = rng.advance();
		final float h = halfToUnitBits(packed, 1.5f) * height;
		final float r = cylinderRadius(outerR, innerR, halfToUnitBits(packed >>> 16, 1.0f));
		p[0] = r * (float) Math.sin(angle);
		p[1] = r * (float) Math.cos(angle);
		p[2] = h;
	}

	private static void cylinderSurfacePoint(final FastRand rng, final float outerR, final float innerR,
			final float height, final float[] p) {
		final float angle = rng.unit() * TWO_PI;
		float shell = outerR;
		if ((innerR > 0) && ((outerR + innerR) > 0)) {
			final float innerSurfaceSelector = innerR / (outerR + innerR);
			shell = rng.unit() > innerSurfaceSelector ? outerR : innerR;
		}
		final float h = (height * rng.unit()) - (0.5f * height);
		p[0] = shell * (float) Math.sin(angle);
		p[1] = shell * (float) Math.cos(angle);
		p[2] = h;
	}

	// ------------------------------------------------------------------ parametric coordinates

	public static boolean positionFromPCoords(final Shape sh, final float[] pc, final boolean volume,
			final float[] outPos) {
		final float r = sh.radius;
		final float ir = sh.innerRadius;
		final float h = sh.height;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX: {
			final float[] extent = { sh.boxDimensions[0] * 0.5f, sh.boxDimensions[1] * 0.5f,
					sh.boxDimensions[2] * 0.5f };
			if (volume) {
				outPos[0] = pc[0] * extent[0];
				outPos[1] = pc[1] * extent[1];
				outPos[2] = pc[2] * extent[2];
				return true;
			}
			final float[] d = sh.boxDimensions;
			final float surf0 = d[1] * d[2];
			final float surf1 = d[2] * d[0];
			final float surf2 = d[0] * d[1];
			final float total = surf0 + surf1 + surf2;
			if (total == 0) {
				outPos[0] = pc[0] * extent[0];
				outPos[1] = pc[1] * extent[1];
				outPos[2] = pc[2] * extent[2];
				return true;
			}
			boxSurfacePoint(extent, surf0, surf1, total, pc[0], pc[1], pc[2], outPos);
			return true;
		}
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final int packed = Float.floatToRawIntBits(pc[1]);
			final float cosTheta = 1.0f - (2.0f * halfToUnitBits(packed, 1.0f));
			final float sinTheta = (float) Math.sqrt(Math.max(0, 1.0f - (cosTheta * cosTheta)));
			float shell = r;
			if (volume) {
				final float r3min = ir * ir * ir;
				final float r3max = r * r * r;
				shell = (float) Math.cbrt(r3min + ((r3max - r3min) * halfToUnitBits(packed >>> 16, 1.0f)));
			} else if (ir > 0) {
				final float outerA = r * r;
				final float total = outerA + (ir * ir);
				if ((total > 0) && ((halfToUnitBits(packed >>> 16, 1.0f) * total) >= outerA)) {
					shell = ir;
				}
			}
			outPos[0] = shell * sinTheta * pc[0];
			outPos[1] = shell * sinTheta * pc[2];
			outPos[2] = shell * cosTheta;
			if ((sh.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID) && sh.hemisphere && (outPos[2] < 0)) {
				outPos[2] = -outPos[2];
			}
			return true;
		}
		case SamplerResource.SHAPE_CYLINDER: {
			final int packed = Float.floatToRawIntBits(pc[1]);
			final float z = halfToUnitBits(packed, 1.5f) * h;
			float rad;
			if (volume) {
				final float expo = 0.5f * ((r > 0 ? ir / r : 0) + 1.0f);
				rad = ((float) Math.pow(halfToUnitBits(packed >>> 16, 1.0f), expo) * (r - ir)) + ir;
			} else {
				rad = r;
				if ((ir > 0) && ((r + ir) > 0) && (halfToUnitBits(packed >>> 16, 1.0f) <= (ir / (r + ir)))) {
					rad = ir;
				}
			}
			outPos[0] = rad * pc[0];
			outPos[1] = rad * pc[2];
			outPos[2] = z;
			return true;
		}
		case SamplerResource.SHAPE_CAPSULE: {
			final int packed = Float.floatToRawIntBits(pc[1]);
			final float z = halfToUnitBits(packed, 1.5f) * h;
			final float u = halfToUnitBits(packed >>> 16, 1.0f);
			final float rad = volume ? (float) Math.sqrt(Math.max(0, (ir * ir) + (((r * r) - (ir * ir)) * u))) : r;
			outPos[0] = rad * pc[0];
			outPos[1] = rad * pc[2];
			outPos[2] = z;
			return true;
		}
		case SamplerResource.SHAPE_CONE: {
			if (volume) {
				outPos[0] = r * pc[0];
				outPos[1] = r * pc[2];
				outPos[2] = h * pc[1];
				return true;
			}
			final float s = 1.0f - pc[1];
			outPos[0] = r * s * pc[0];
			outPos[1] = r * s * pc[2];
			outPos[2] = h * pc[1];
			return true;
		}
		default:
			return false;
		}
	}

	private static void writePCoords(final RegisterValue out, final float x, final float y, final float z) {
		out.clear();
		out.componentCount = 3;
		out.typeBank = Bank.INT3;
		out.lanes[0] = x;
		out.lanes[1] = y;
		out.lanes[2] = z;
	}

	public static boolean samplePCoords(final Shape sh, final FastRand rng, final RegisterValue out) {
		final boolean volume = sh.dimensionality == SamplerResource.DIM_VOLUME;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX:
			writePCoords(out, (rng.unit() * 2.0f) - 1.0f, (rng.unit() * 2.0f) - 1.0f, (rng.unit() * 2.0f) - 1.0f);
			return true;
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID:
		case SamplerResource.SHAPE_CYLINDER:
		case SamplerResource.SHAPE_CAPSULE: {
			final float angle = rng.unit() * TWO_PI;
			final int packed = rng.advance();
			writePCoords(out, (float) Math.sin(angle), Float.intBitsToFloat(packed), (float) Math.cos(angle));
			return true;
		}
		case SamplerResource.SHAPE_CONE: {
			if (volume) {
				final float angle = rng.unit() * TWO_PI;
				final float v = (float) Math.cbrt(rng.unit());
				final float t = (float) Math.sqrt(rng.unit()) * v;
				writePCoords(out, t * (float) Math.sin(angle), 1.0f - v, t * (float) Math.cos(angle));
			} else {
				final float u = rng.unit();
				final float angle = rng.unit() * TWO_PI;
				writePCoords(out, (float) Math.sin(angle), 1.0f - (float) Math.sqrt(u), (float) Math.cos(angle));
			}
			return true;
		}
		default:
			return false;
		}
	}

	private static float[] boxFaceLimits(final float[] dim) {
		final float lr = dim[1] * dim[2];
		final float tb = dim[2] * dim[0];
		final float fb = dim[0] * dim[1];
		final float total = lr + tb + fb;
		if (total <= 0) {
			return new float[] { -1.0f, -0.5f, -0.1f, 0.1f, 0.1f, 0.5f, 1.0f };
		}
		final float midX = (lr * 0.5f) / total;
		final float midY = (lr + (tb * 0.5f)) / total;
		final float midZ = (lr + tb + (fb * 0.5f)) / total;
		return new float[] { -midZ, -midY, -midX, midX, midX, midY, midZ };
	}

	public static boolean projectPCoordsPrimitive(final Shape sh, final float[] local, final RegisterValue out) {
		final float[] p = new float[3];
		project(sh, local, p);
		final float rad = (float) Math.sqrt((p[0] * p[0]) + (p[1] * p[1]));
		final float invRad = rad > 1e-20f ? 1.0f / rad : 0;
		final float sinA = rad > 1e-20f ? p[0] * invRad : 0;
		final float cosA = rad > 1e-20f ? p[1] * invRad : 1;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX: {
			final float[] e = { sh.boxDimensions[0] * 0.5f, sh.boxDimensions[1] * 0.5f, sh.boxDimensions[2] * 0.5f };
			int axis = 0;
			float best = e[0] > 0 ? Math.abs(Math.abs(p[0]) - e[0]) : 1e30f;
			for (int c = 1; c < 3; c++) {
				final float d = e[c] > 0 ? Math.abs(Math.abs(p[c]) - e[c]) : 1e30f;
				if (d < best) {
					best = d;
					axis = c;
				}
			}
			final int id1 = WRAP_LOOKUP[axis];
			final int id2 = WRAP_LOOKUP[axis + 1];
			final float[] limits = boxFaceLimits(sh.boxDimensions);
			final float limit = limits[p[axis] < 0 ? (2 - axis) : (3 + axis)];
			writePCoords(out, e[id1] > 0 ? p[id1] / e[id1] : 0, e[id2] > 0 ? p[id2] / e[id2] : 0, limit);
			return true;
		}
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final float len = (float) Math.sqrt((p[0] * p[0]) + (p[1] * p[1]) + (p[2] * p[2]));
			final float cosTheta = len > 1e-20f ? p[2] / len : 1;
			final int packed = packHalves((1.0f - cosTheta) * 0.5f, 0);
			final float sinTheta = (float) Math.sqrt(Math.max(0, 1.0f - (cosTheta * cosTheta)));
			writePCoords(out, len > 1e-20f ? p[0] / ((len * sinTheta) + 1e-20f) : 0, Float.intBitsToFloat(packed),
					len > 1e-20f ? p[1] / ((len * sinTheta) + 1e-20f) : 1);
			return true;
		}
		case SamplerResource.SHAPE_CYLINDER:
		case SamplerResource.SHAPE_CAPSULE: {
			final float heightFrac = sh.height > 0 ? (p[2] / sh.height) + 0.5f : 0.5f;
			final float radialFrac = sh.radius > 0 ? clamp(rad / sh.radius, 0, 1) : 0;
			final int packed = packHalves(clamp(heightFrac, 0, 1), radialFrac);
			writePCoords(out, sinA, Float.intBitsToFloat(packed), cosA);
			return true;
		}
		case SamplerResource.SHAPE_CONE: {
			final float heightFrac = sh.height > 0 ? clamp(p[2] / sh.height, 0, 1) : 0;
			writePCoords(out, sinA, heightFrac, cosA);
			return true;
		}
		default:
			return false;
		}
	}

	/** shape.buildPCoords&lt;Shape&gt;[Regular](a0, a1, a2). */
	public static boolean buildPCoords(final String symbol, final float a0, final float a1, final float a2,
			final int valueArgCount, final RegisterValue out) {
		final String prefix = "shape.buildPCoords";
		if (symbol.length() <= prefix.length()) {
			return false;
		}
		final String which = symbol.substring(prefix.length());
		final boolean regular = which.endsWith("Regular");
		final String shapeName = regular ? which.substring(0, which.length() - 7) : which;
		float g0 = a0;
		float g1 = a1;
		if (regular) {
			final float count = a1 > 0 ? a1 : 1;
			g0 = a0 / count;
			g1 = Interpreter.fmod(a0 * 0.6180339887f, 1.0f);
		}
		if (shapeName.startsWith("Box")) {
			if (regular) {
				writePCoords(out, (g0 * 2.0f) - 1.0f, (g1 * 2.0f) - 1.0f,
						UNIT_BOX_FACE_LIMITS[Math.max(0, Math.min(6, (((int) a0) % 6) + 1))]);
			} else {
				final int faceId = (int) a0;
				writePCoords(out, (a1 * 2.0f) - 1.0f, (a2 * 2.0f) - 1.0f,
						UNIT_BOX_FACE_LIMITS[Math.max(0, Math.min(6, faceId + 3))]);
			}
		} else if (shapeName.startsWith("Sphere")) {
			final float angle = (regular ? g0 : a0) * TWO_PI;
			final int packed = packHalves(regular ? g1 : a1, regular ? 1.0f : a2);
			writePCoords(out, (float) Math.cos(angle), Float.intBitsToFloat(packed), (float) Math.sin(angle));
		} else if (shapeName.startsWith("Cylinder") || shapeName.startsWith("Capsule")) {
			final float height = regular ? g0 : a0;
			final float theta = regular ? g1 : a1;
			final float radial = regular ? 1.0f : a2;
			final float angle = theta * TWO_PI;
			final int packed = packHalves(height, radial);
			writePCoords(out, (float) Math.sin(angle), Float.intBitsToFloat(packed), (float) Math.cos(angle));
		} else if (shapeName.startsWith("Cone")) {
			final boolean volumeForm = !regular && (valueArgCount >= 3);
			final float heightParam = regular ? g0 : a0;
			final float theta = regular ? g1 : a1;
			final float angle = theta * TWO_PI;
			if (volumeForm) {
				final float v = (float) Math.cbrt(clamp(heightParam, 0, 1));
				final float t = (float) Math.sqrt(clamp(a2, 0, 1)) * v;
				writePCoords(out, t * (float) Math.sin(angle), 1.0f - v, t * (float) Math.cos(angle));
			} else {
				writePCoords(out, (float) Math.sin(angle), 1.0f - (float) Math.sqrt(clamp(heightParam, 0, 1)),
						(float) Math.cos(angle));
			}
		} else {
			return false;
		}
		return true;
	}

	public static boolean sampleSurfacePCoordsFromUV(final Shape sh, final float u, final float v,
			final RegisterValue out) {
		if (sh.type == SamplerResource.SHAPE_MESH) {
			return false;
		}
		final float angle = u * TWO_PI;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX:
			writePCoords(out, (u * 2.0f) - 1.0f, (v * 2.0f) - 1.0f, UNIT_BOX_FACE_LIMITS[5]);
			return true;
		case SamplerResource.SHAPE_CONE:
			writePCoords(out, (float) Math.sin(angle), clamp(v, 0, 1), (float) Math.cos(angle));
			return true;
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final int packed = packHalves(clamp(v, 0, 1), 1.0f);
			writePCoords(out, (float) Math.cos(angle), Float.intBitsToFloat(packed), (float) Math.sin(angle));
			return true;
		}
		default: {
			final int packed = packHalves(clamp(v, 0, 1), 1.0f);
			writePCoords(out, (float) Math.sin(angle), Float.intBitsToFloat(packed), (float) Math.cos(angle));
			return true;
		}
		}
	}

	// ------------------------------------------------------------------ getters

	public static float surface(final Shape sh) {
		final float r = sh.radius;
		final float ir = sh.innerRadius;
		final float h = sh.height;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX: {
			final float[] d = sh.boxDimensions;
			return 2.0f * ((d[0] * (d[1] + d[2])) + (d[1] * d[2]));
		}
		case SamplerResource.SHAPE_SPHERE: {
			float s = 4.0f * PI * r * r;
			if (ir > 0) {
				s += 4.0f * PI * ir * ir;
			}
			return s;
		}
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final float cutPlane = sh.hemisphere ? 0.5f : 1.0f;
			float s = 4.0f * PI * r * r * cutPlane;
			if (ir > 0) {
				s += 4.0f * PI * ir * ir * cutPlane;
			}
			return s;
		}
		case SamplerResource.SHAPE_CYLINDER:
			return 2.0f * PI * ((r * (h + r)) + (ir * (h - ir)));
		case SamplerResource.SHAPE_CAPSULE:
			return (2.0f * PI * (r + ir) * h) + (4.0f * PI * r * r) + (ir > 0 ? 4.0f * PI * ir * ir : 0);
		case SamplerResource.SHAPE_CONE:
			return PI * r * ((float) Math.sqrt((r * r) + (h * h)) + r);
		default:
			return 0;
		}
	}

	public static float volume(final Shape sh) {
		final float r = sh.radius;
		final float ir = sh.innerRadius;
		final float h = sh.height;
		switch (sh.type) {
		case SamplerResource.SHAPE_BOX: {
			final float[] d = sh.boxDimensions;
			return d[0] * d[1] * d[2];
		}
		case SamplerResource.SHAPE_SPHERE:
			return sphereVol(r) - (ir > 0 ? sphereVol(ir) : 0);
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final float cutPlane = sh.hemisphere ? 0.5f : 1.0f;
			final float prod = sh.nonUniformScale[0] * sh.nonUniformScale[1] * sh.nonUniformScale[2];
			final float v = sphereVol(r) - (ir > 0 ? sphereVol(ir) : 0);
			return v * cutPlane * prod;
		}
		case SamplerResource.SHAPE_CYLINDER:
			return cylVol(r, h) - (ir > 0 ? cylVol(ir, h) : 0);
		case SamplerResource.SHAPE_CAPSULE:
			return (cylVol(r, h) - (ir > 0 ? cylVol(ir, h) : 0) + sphereVol(r)) - (ir > 0 ? sphereVol(ir) : 0);
		case SamplerResource.SHAPE_CONE:
			return (1.0f / 3.0f) * PI * r * r * h;
		default:
			return 0;
		}
	}

	private static float sphereVol(final float rad) {
		return (4.0f / 3.0f) * PI * rad * rad * rad;
	}

	private static float cylVol(final float rad, final float hh) {
		return PI * rad * rad * hh;
	}

	public static void axisColumn(final Shape sh, final int axis, final float[] out) {
		rotateDirection(sh, axis == 0 ? 1 : 0, axis == 1 ? 1 : 0, axis == 2 ? 1 : 0, out);
	}

	/** Column and sign for axisLeft, axisRight, axisDown, axisUp, axisBackward, axisForward. */
	public static final int[] ABSOLUTE_AXIS_COLUMN = { 0, 0, 2, 2, 1, 1 };
	public static final float[] ABSOLUTE_AXIS_SIGN = { -1, 1, -1, 1, -1, 1 };
	public static final String[] AXIS_NAMES = { "axisLeft", "axisRight", "axisDown", "axisUp", "axisBackward",
			"axisForward" };

	/** Shape getter by symbol; returns false when the symbol is not a getter or needs mesh data. */
	public static boolean getter(final Shape sh, final String symbol, final RegisterValue out) {
		final float[] v = new float[3];
		switch (symbol) {
		case "surface":
			out.setScalar(surface(sh));
			return true;
		case "volume":
			out.setScalar(volume(sh));
			return true;
		case "radius":
			out.setScalar(sh.radius);
			return true;
		case "innerRadius":
			out.setScalar(sh.innerRadius);
			return true;
		case "height":
			out.setScalar(sh.height);
			return true;
		case "vertexCount":
		case "triangleCount":
		case "tetraCount":
			writeInt(out, 0);
			return true;
		case "meshScale":
			out.setFloat3(sh.meshScale[0], sh.meshScale[1], sh.meshScale[2]);
			return true;
		case "boxDim":
			out.setFloat3(sh.boxDimensions[0], sh.boxDimensions[1], sh.boxDimensions[2]);
			return true;
		case "type":
			writeInt(out, sh.type);
			return true;
		case "position":
			out.setFloat3(sh.position[0], sh.position[1], sh.position[2]);
			return true;
		case "axisSide":
			axisColumn(sh, 0, v);
			out.setFloat3(v[0], v[1], v[2]);
			return true;
		case "axisVertical":
			axisColumn(sh, 2, v);
			out.setFloat3(v[0], v[1], v[2]);
			return true;
		case "axisDepth":
			axisColumn(sh, 1, v);
			out.setFloat3(v[0], v[1], v[2]);
			return true;
		default:
			for (int i = 0; i < 6; i++) {
				if (symbol.equals(AXIS_NAMES[i])) {
					axisColumn(sh, ABSOLUTE_AXIS_COLUMN[i], v);
					final float s = ABSOLUTE_AXIS_SIGN[i];
					out.setFloat3(v[0] * s, v[1] * s, v[2] * s);
					return true;
				}
			}
			return false;
		}
	}

	public static boolean isGetterName(final String symbol) {
		switch (symbol) {
		case "surface":
		case "volume":
		case "radius":
		case "innerRadius":
		case "height":
		case "vertexCount":
		case "triangleCount":
		case "tetraCount":
		case "meshScale":
		case "boxDim":
		case "type":
		case "position":
		case "axisSide":
		case "axisVertical":
		case "axisDepth":
		case "axisLeft":
		case "axisRight":
		case "axisDown":
		case "axisUp":
		case "axisBackward":
		case "axisForward":
			return true;
		default:
			return false;
		}
	}

	private static void writeInt(final RegisterValue out, final int v) {
		out.clear();
		out.componentCount = 1;
		out.typeBank = Bank.PTR;
		out.setLaneInt(0, v);
	}

	// ------------------------------------------------------------------ geometry queries (shape_geometry.cpp)

	private static float length3(final float[] p) {
		return (float) Math.sqrt((p[0] * p[0]) + (p[1] * p[1]) + (p[2] * p[2]));
	}

	private static float radialLength(final float[] p) {
		return (float) Math.sqrt((p[0] * p[0]) + (p[1] * p[1]));
	}

	private static float boxField(final float[] p, final float ex, final float ey, final float ez) {
		final float qx = Math.abs(p[0]) - ex;
		final float qy = Math.abs(p[1]) - ey;
		final float qz = Math.abs(p[2]) - ez;
		final float ox = Math.max(qx, 0);
		final float oy = Math.max(qy, 0);
		final float oz = Math.max(qz, 0);
		final float inside = Math.min(Math.max(qx, Math.max(qy, qz)), 0);
		return (float) Math.sqrt((ox * ox) + (oy * oy) + (oz * oz)) + inside;
	}

	private static float cylinderSolidField(final float[] p, final float r, final float h) {
		final float dr = radialLength(p) - r;
		final float dz = Math.abs(p[2]) - (0.5f * h);
		final float outside = (float) Math
				.sqrt((Math.max(dr, 0) * Math.max(dr, 0)) + (Math.max(dz, 0) * Math.max(dz, 0)));
		return outside + Math.min(Math.max(dr, dz), 0);
	}

	private static float capsuleSolidField(final float[] p, final float r, final float h) {
		final float half = 0.5f * h;
		final float dz = clamp(p[2], -half, half);
		final float qz = p[2] - dz;
		return (float) Math.sqrt((p[0] * p[0]) + (p[1] * p[1]) + (qz * qz)) - r;
	}

	private static float coneSolidField(final float[] p, final float r, final float h) {
		if ((r <= 0) || (h <= 0)) {
			return length3(p);
		}
		final float q0 = radialLength(p);
		final float q1 = p[2];
		final float ex = -r;
		final float ey = h;
		final float w0 = q0 - r;
		final float w1 = q1;
		final float e2 = (ex * ex) + (ey * ey);
		final float t = clamp(((w0 * ex) + (w1 * ey)) / e2, 0, 1);
		final float s0 = w0 - (ex * t);
		final float s1 = w1 - (ey * t);
		final float slant = (float) Math.sqrt((s0 * s0) + (s1 * s1));
		final float b0 = Math.max(q0 - r, 0);
		final float b1 = q1;
		final float base = (float) Math.sqrt((b0 * b0) + (b1 * b1));
		final float dist = Math.min(slant, base);
		final boolean inside = (q1 >= 0) && (q1 <= h) && (((q0 * h) + (q1 * r)) <= (r * h));
		return inside ? -dist : dist;
	}

	private static float solidField(final Shape shape, final float[] p, final float radius) {
		switch (shape.type) {
		case SamplerResource.SHAPE_BOX:
			return boxField(p, shape.boxDimensions[0] * 0.5f, shape.boxDimensions[1] * 0.5f,
					shape.boxDimensions[2] * 0.5f);
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID:
			return length3(p) - radius;
		case SamplerResource.SHAPE_CYLINDER:
			return cylinderSolidField(p, radius, shape.height);
		case SamplerResource.SHAPE_CAPSULE:
			return capsuleSolidField(p, radius, shape.height);
		case SamplerResource.SHAPE_CONE:
			return coneSolidField(p, radius, shape.height);
		default:
			return length3(p);
		}
	}

	public static boolean contains(final Shape shape, final float[] local) {
		if (shape.type == SamplerResource.SHAPE_MESH) {
			return false;
		}
		if (solidField(shape, local, shape.radius) >= 0) {
			return false;
		}
		if ((shape.innerRadius > 0) && (solidField(shape, local, shape.innerRadius) <= 0)) {
			return false;
		}
		if ((shape.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID) && shape.hemisphere && (local[2] < 0)) {
			return false;
		}
		return true;
	}

	public static float distanceField(final Shape shape, final float[] local) {
		if (shape.type == SamplerResource.SHAPE_MESH) {
			return 0;
		}
		if ((shape.type == SamplerResource.SHAPE_SPHERE) || (shape.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID)) {
			final float d = length3(local);
			final float outer = d - shape.radius;
			return shape.innerRadius > 0 ? Math.max(outer, shape.innerRadius - d) : outer;
		}
		final float outer = solidField(shape, local, shape.radius);
		if (shape.innerRadius <= 0) {
			return outer;
		}
		return Math.max(outer, -solidField(shape, local, shape.innerRadius));
	}

	private static void rescale(final float[] p, final float target, final float[] out) {
		final float len = length3(p);
		if (len <= 1e-20f) {
			out[0] = target;
			out[1] = 0;
			out[2] = 0;
			return;
		}
		final float k = target / len;
		out[0] = p[0] * k;
		out[1] = p[1] * k;
		out[2] = p[2] * k;
	}

	private static void rescaleRadial(final float[] p, final float target, final float z, final float[] out) {
		final float len = radialLength(p);
		if (len <= 1e-20f) {
			out[0] = target;
			out[1] = 0;
			out[2] = z;
			return;
		}
		final float k = target / len;
		out[0] = p[0] * k;
		out[1] = p[1] * k;
		out[2] = z;
	}

	public static void project(final Shape shape, final float[] local, final float[] out) {
		final float r = shape.radius;
		final float ir = shape.innerRadius;
		final float h = shape.height;
		switch (shape.type) {
		case SamplerResource.SHAPE_SPHERE:
		case SamplerResource.SHAPE_COMPLEX_ELLIPSOID: {
			final float d = length3(local);
			final float shell = ((ir > 0) && (Math.abs(d - ir) < Math.abs(d - r))) ? ir : r;
			rescale(local, shell, out);
			if ((shape.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID) && shape.hemisphere && (out[2] < 0)) {
				out[2] = -out[2];
			}
			return;
		}
		case SamplerResource.SHAPE_BOX: {
			final float[] e = { shape.boxDimensions[0] * 0.5f, shape.boxDimensions[1] * 0.5f,
					shape.boxDimensions[2] * 0.5f };
			out[0] = clamp(local[0], -e[0], e[0]);
			out[1] = clamp(local[1], -e[1], e[1]);
			out[2] = clamp(local[2], -e[2], e[2]);
			if ((out[0] == local[0]) && (out[1] == local[1]) && (out[2] == local[2])) {
				int best = 0;
				float bestGap = e[0] - Math.abs(out[0]);
				for (int c = 1; c < 3; c++) {
					final float gap = e[c] - Math.abs(out[c]);
					if (gap < bestGap) {
						bestGap = gap;
						best = c;
					}
				}
				out[best] = out[best] < 0 ? -e[best] : e[best];
			}
			return;
		}
		case SamplerResource.SHAPE_CYLINDER: {
			final float half = 0.5f * h;
			final float rad = radialLength(local);
			final float shell = ((ir > 0) && (Math.abs(rad - ir) < Math.abs(rad - r))) ? ir : r;
			final float z = clamp(local[2], -half, half);
			if ((Math.abs(local[2]) > half) && (rad < r) && (rad > ir)) {
				out[0] = local[0];
				out[1] = local[1];
				out[2] = local[2] < 0 ? -half : half;
				return;
			}
			rescaleRadial(local, shell, z, out);
			return;
		}
		case SamplerResource.SHAPE_CAPSULE: {
			final float half = 0.5f * h;
			final float zc = clamp(local[2], -half, half);
			final float[] axisToP = { local[0], local[1], local[2] - zc };
			final float len = length3(axisToP);
			final float shell = ((ir > 0) && (Math.abs(len - ir) < Math.abs(len - r))) ? ir : r;
			rescale(axisToP, shell, out);
			out[2] += zc;
			return;
		}
		case SamplerResource.SHAPE_CONE: {
			if ((r <= 0) || (h <= 0)) {
				out[0] = out[1] = out[2] = 0;
				return;
			}
			final float q0 = radialLength(local);
			final float q1 = local[2];
			final float ex = -r;
			final float ey = h;
			final float t = clamp((((q0 - r) * ex) + (q1 * ey)) / ((ex * ex) + (ey * ey)), 0, 1);
			final float slantR = r + (ex * t);
			final float slantZ = ey * t;
			final float slantD = ((q0 - slantR) * (q0 - slantR)) + ((q1 - slantZ) * (q1 - slantZ));
			final float baseR = Math.min(q0, r);
			final float baseD = ((q0 - baseR) * (q0 - baseR)) + (q1 * q1);
			if (baseD < slantD) {
				rescaleRadial(local, baseR, 0, out);
			} else {
				rescaleRadial(local, slantR, slantZ, out);
			}
			return;
		}
		default:
			out[0] = local[0];
			out[1] = local[1];
			out[2] = local[2];
		}
	}

	public static void surfaceNormal(final Shape shape, final float[] local, final float[] out) {
		if (shape.type == SamplerResource.SHAPE_MESH) {
			out[0] = 0;
			out[1] = 0;
			out[2] = 1;
			return;
		}
		final float scale = Math.max(Math.max(Math.max(shape.radius, shape.height), Math.max(shape.boxDimensions[0],
				shape.boxDimensions[1])), Math.max(shape.boxDimensions[2], 1.0f));
		final float e = scale * 1e-3f;
		final float dx = distanceField(shape, new float[] { local[0] + e, local[1], local[2] })
				- distanceField(shape, new float[] { local[0] - e, local[1], local[2] });
		final float dy = distanceField(shape, new float[] { local[0], local[1] + e, local[2] })
				- distanceField(shape, new float[] { local[0], local[1] - e, local[2] });
		final float dz = distanceField(shape, new float[] { local[0], local[1], local[2] + e })
				- distanceField(shape, new float[] { local[0], local[1], local[2] - e });
		final float len = (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		if (len <= 1e-20f) {
			out[0] = 0;
			out[1] = 0;
			out[2] = 1;
			return;
		}
		out[0] = dx / len;
		out[1] = dy / len;
		out[2] = dz / len;
	}

	/** Ray march against the shape; returns the hit parameter in [0,1] or NaN when there is no hit. */
	public static float intersect(final Shape shape, final float[] origin, final float[] direction, final float length,
			final boolean twoSided) {
		if (shape.type == SamplerResource.SHAPE_MESH) {
			return Float.NaN;
		}
		final float[] seg = { direction[0] * length, direction[1] * length, direction[2] * length };
		final float segLen = length3(seg);
		if (segLen <= 0) {
			return Float.NaN;
		}
		final boolean solidSphere = ((shape.type == SamplerResource.SHAPE_SPHERE)
				|| (shape.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID)) && (shape.innerRadius <= 0)
				&& !((shape.type == SamplerResource.SHAPE_COMPLEX_ELLIPSOID) && shape.hemisphere);
		if (solidSphere) {
			final float a = (seg[0] * seg[0]) + (seg[1] * seg[1]) + (seg[2] * seg[2]);
			final float b = 2.0f * ((origin[0] * seg[0]) + (origin[1] * seg[1]) + (origin[2] * seg[2]));
			final float c = ((origin[0] * origin[0]) + (origin[1] * origin[1]) + (origin[2] * origin[2]))
					- (shape.radius * shape.radius);
			final float disc = (b * b) - (4.0f * a * c);
			if (disc < 0) {
				return Float.NaN;
			}
			final float sq = (float) Math.sqrt(disc);
			final float t0 = (-b - sq) / (2.0f * a);
			final float t1 = (-b + sq) / (2.0f * a);
			final float t = (t0 >= 0) ? t0 : (twoSided ? t1 : -1);
			if ((t < 0) || (t > 1)) {
				return Float.NaN;
			}
			return t;
		}
		final boolean startInside = contains(shape, origin);
		if (startInside && !twoSided) {
			return Float.NaN;
		}
		float t = 0;
		final float[] p = new float[3];
		for (int step = 0; step < 192; step++) {
			p[0] = origin[0] + (seg[0] * t);
			p[1] = origin[1] + (seg[1] * t);
			p[2] = origin[2] + (seg[2] * t);
			float d = distanceField(shape, p);
			if (startInside) {
				d = -d;
			}
			final float tol = Math.max(1e-5f, segLen * 1e-5f);
			if (d < tol) {
				return t <= 1 ? t : Float.NaN;
			}
			t += d / segLen;
			if (t > 1) {
				return Float.NaN;
			}
		}
		return Float.NaN;
	}
}
