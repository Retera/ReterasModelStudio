package com.hiveworkshop.rms.util;

/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I
 * wanted a simple object to encompass four quaternion values for rotation (this
 * is how MDLs handle rotating)
 *
 * Eric Theller 3/8/2012
 */
public class Quat extends Vec4 {
	public Quat() {
		w = 1;
	}

	public Quat(final double x, final double y, final double z, final double w) {
		set(x, y, z, w);
	}

	public Quat(final Quat other) {
		set(other);
	}

	public Quat(final Vec4 a) {
		set(a);
	}

	public Quat(final Vec3 eulerRotation) {
		set(eulerRotation);
	}

	public static Quat getSlerped(final Quat from, final Quat toward, final float t) {
		return new Quat(from).slerp(toward, t);
	}

	public Quat(final float[] data) {
		set(data);
	}

	public Quat(final Vec3 axis, final float angle) {
		setFromAxisAngle(axis, angle);
	}

	public static Quat getSquad(final Quat from, final Quat toward, final Quat outTan, final Quat inTan, final float t) {
		return new Quat(from).squad(toward, outTan, inTan, t);
	}

	public static Quat getProd(final Quat a, final Quat b) {
		return new Quat(a).mul(b);
	}

	public static Quat getInverseRotation(Quat q) {
		return new Quat(q).invertRotation();
	}

	public Quat set(final Vec3 eulerRotation) {
		// eulerRotation.x = Math.toRadians(eulerRotation.x);
		// eulerRotation.y = Math.toRadians(eulerRotation.y);
		// eulerRotation.z = Math.toRadians(eulerRotation.z);
		// Original Wikipedia equation test

		// double yaw = eulerRotation.z;
		// yaw = Math.PI - yaw;
		// if( yaw > Math.PI )
		// {
		// yaw -= Math.PI;
		// }
		// eulerRotation.z = yaw;
		// eulerRotation.y = -eulerRotation.y;
		double sinX = Math.sin(eulerRotation.x / 2);
		double sinY = Math.sin(eulerRotation.y / 2);
		double sinZ = Math.sin(eulerRotation.z / 2);
		double cosX = Math.cos(eulerRotation.x / 2);
		double cosY = Math.cos(eulerRotation.y / 2);
		double cosZ = Math.cos(eulerRotation.z / 2);
		x = (float) ((cosX * cosY * cosZ) + (sinX * sinY * sinZ));
		y = (float) ((sinX * cosY * cosZ) - (cosX * sinY * sinZ));
		z = (float) ((cosX * sinY * cosZ) + (sinX * cosY * sinZ));
		w = (float) ((cosX * cosY * sinZ) - (sinX * sinY * cosZ));

		if (Math.abs(x) < 1E-15) {
			x = 0;
		}
		if (Math.abs(y) < 1E-15) {
			y = 0;
		}
		if (Math.abs(z) < 1E-15) {
			z = 0;
		}
		if (Math.abs(w) < 1E-15) {
			w = 0;
		}
		// b = -b;
		// c = -c;
		// new test
		// double c1 = Math.cos(eulerRotation.x / 2);
		// double c2 = Math.cos(eulerRotation.y / 2);
		// double c3 = Math.cos(eulerRotation.z / 2);
		// double s1 = Math.sin(eulerRotation.x / 2);
		// double s2 = Math.sin(eulerRotation.y / 2);
		// double s3 = Math.sin(eulerRotation.z / 2);
		//
		// a = c1 * c2 * c3 - s1 * s2 * s3;
		// b = s1 * s2 * c3 + c1 * c2 * s3;
		// c = s1 * c2 * c3 + c1 * s2 * s3;
		// d = c1 * s2 * c3 - s1 * c2 * s3;

		/**
		 * double heading = eulerRotation.x; double attitude = eulerRotation.y; double
		 * bank = eulerRotation.z; double c1 = Math.cos(heading); double s1 =
		 * Math.sin(heading); double c2 = Math.cos(attitude); double s2 =
		 * Math.sin(attitude); double c3 = Math.cos(bank); double s3 = Math.sin(bank); a
		 * = Math.sqrt(1.0 + c1 * c2 + c1*c3 - s1 * s2 * s3 + c2*c3) / 2.0; double w4 =
		 * (4.0 * a); b = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4 ; c = (s1 * c2 + s1 *
		 * c3 + c1 * s2 * s3) / w4 ; d = (-s1 * s3 + c1 * s2 * c3 +s2) / w4 ;
		 */

		// Now Quaternions can go burn and die.
		return this;
	}

	public Vec3 toEuler() {
		// Wikipedia formula
		double roll = (Math.atan2(2.0 * ((x * y) + (z * w)), 1 - (2.0 * ((y * y) + (z * z)))));
		double stuff = (x * z) - (w * y);
		if (stuff > 0.5) {
			stuff = 0.5;
		} else if (stuff < -0.5) {
			stuff = -0.5;
		}
		double pitch = (Math.asin(2.0 * (stuff)));
		final double yaw = (Math.atan2(2.0 * ((x * w) + (y * z)), 1 - (2.0 * ((z * z) + (w * w)))));

		// yaw = Math.PI - yaw;
		// if( yaw > Math.PI )
		// {
		// yaw -= Math.PI;
		// }
		// pitch = -pitch;

		// //Wikipedia formula with some twists
		// double roll = (Math.atan2( 2.0 * ( d * a + b * c ), 1 - 2.0 * ( a * a
		// + b * b ) ) );
		// double pitch = (Math.asin( 2.0 * ( d * b - c * a ) ) );
		// double yaw = (Math.atan2( 2.0 * ( d * c + a * b ), 1 - 2.0 * ( b * b
		// + c * c) ) );

		// //www.eulideanspace.com formula
		// double bank = (Math.atan2( 2.0 * ( a * b - c * d ), 1 - 2.0 * ( b * b
		// + d * d ) ) );
		// double attitude = (Math.asin( 2.0 * ( b * c + d * a ) ) );
		// double heading = (Math.atan2( 2.0 * ( c * d + b * d ), 1 - 2.0 * ( c
		// * c + d * d) ) );
		// //Math.toDegrees
		//
		// if( b * c + d * a == 0.5 )
		// {
		// heading = 2 * Math.atan2(b,a);
		// bank = 0;
		// }
		// else if( b * c + d * a == -0.5 )
		// {
		// heading = - 2 * Math.atan2(b,a);
		// bank = 0;
		// }

		if (((y * z) + (w * x)) == 0.5) {
			roll = 2 * Math.atan2(y, x);
			pitch = 0;
		} else if (((y * z) + (w * x)) == -0.5) {
			roll = -2 * Math.atan2(y, x);
			pitch = 0;
		}

		return new Vec3(roll, pitch, yaw);
		// return new Vertex(heading, attitude, bank);
		// Now Quaternions can go burn and die.
	}

	public Quat setFromAxisAngle(final Vec3 axis, final float angle) {
		return setFromAxisAngle(axis.x, axis.y, axis.z, angle);
	}

	public Quat setFromAxisAngle(final Vec4 axis) {
		return setFromAxisAngle(axis.x, axis.y, axis.z, axis.w);
	}

	public Quat setFromAxisAngle(final float ax, final float ay, final float az, final float angle) {
		final float halfAngle = angle / 2;
		final float sinOfHalfAngle = (float) Math.sin(halfAngle);
		x = ax * sinOfHalfAngle;
		y = ay * sinOfHalfAngle;
		z = az * sinOfHalfAngle;
		w = (float) Math.cos(halfAngle);
		return this;
	}

	public Quat setIdentity() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
		return this;
	}

//	public Quat squad(final Quat outTan, final Quat inTan, final Quat a, final float t, final Quat out) {
//        outTan.slerp(inTan, t, out);
//
//		final float x = out.x;
//		final float y = out.y;
//		final float z = out.z;
//        final float w = out.w;
//
//		slerp(a, t, out);
//
//		out.slerp(x, y, z, w, 2 * t * (1 - t), out);
//
//		return out;
//	}

	private Quat getSlerped(final Quat from, float ax, float ay, float az, float aw, final float t) {
		Quat temp = new Quat(ax, ay, az, aw);
		return new Quat(from).slerp(temp, t);
	}

	private Quat slerp(float ax, float ay, float az, float aw, final float t) {
		Quat temp = new Quat(ax, ay, az, aw);
		return slerp(temp, t);
	}

	public Quat slerp(final Quat toward, final float t) {
		final float scale0, scale1;
		float dir = 1;
		// calc cosine
		float cosOm = (x * toward.x) + (y * toward.y) + (z * toward.z) + (w * toward.w);
		// adjust signs (if necessary)
		if (cosOm < 0) {
			cosOm = -cosOm;
			dir = -1;
//			a.x = -a.x;
//			a.y = -a.y;
//			a.z = -a.z;
//			a.w = -a.w;
		}
		// calculate coefficients
		if ((1.0 - cosOm) > 0.000001) {
			// standard case (slerp)
			final float omega = (float) Math.acos(cosOm);
			final float sinOm = (float) Math.sin(omega);

			scale0 = (float) Math.sin((1.0 - t) * omega) / sinOm;
			scale1 = (float) Math.sin(t * omega) / sinOm;
		} else {
			// "from" and "to" quaternions are very close
			// ... so we can do a linear interpolation
			scale0 = 1.0f - t;
			scale1 = t;
		}
		this.scale(scale0).add(Quat.getScaled(toward, scale1 * dir));
//		x = (scale0 * x) + (scale1 * dir * toward.x);
//		y = (scale0 * y) + (scale1 * dir * toward.y);
//		z = (scale0 * z) + (scale1 * dir * toward.z);
//		w = (scale0 * w) + (scale1 * dir * toward.w);

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return this;
//		return slerp(a, t, this);
	}

	public Quat squad(final Quat toward, final Quat outTan, final Quat inTan, final float t) {
//		outTan.slerp(inTan, t, out);
		Quat temp = getSlerped(outTan, inTan, t);

//		slerp(toward, t, out);

//		Quat temp2 = getSlerped(this, toward, t);
//		Quat temp3 = getSlerped(temp2, temp, 2 * t * (1 - t));
//		return (Quat) set(temp3);

		slerp(toward, t);
		return slerp(temp, 2 * t * (1 - t));
	}

	public Quat mul(final Quat a) {
		float newX = (x * a.w) + (w * a.x) + (y * a.z) - (z * a.y);
		float newY = (y * a.w) + (w * a.y) + (z * a.x) - (x * a.z);
		float newZ = (z * a.w) + (w * a.z) + (x * a.y) - (y * a.x);
		float newW = (w * a.w) - (x * a.x) - (y * a.y) - (z * a.z);
		return (Quat) set(newX, newY, newZ, newW);
	}

	public static Quat getScaledInverted(final Quat a) {
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}
//		float newX = -a.x * len;
//		float newY = -a.y * len;
//		float newZ = -a.z * len;
//		float newW = a.w * len;
//
//		return (Quat) set(newX, newY, newZ, newW);
		return (Quat) new Quat(a).invertRotation().scale(len);
	}

//	public Quat mulLeft(final Quat a) {
//		float newX = (a.x * w) + (a.w * x) + (a.y * z) - (a.z * y);
//		float newY = (a.y * w) + (a.w * y) + (a.z * x) - (a.x * z);
//		float newZ = (a.z * w) + (a.w * z) + (a.x * y) - (a.y * x);
//		float newW = (a.w * w) - (a.x * x) - (a.y * y) - (a.z * z);
//		return (Quat) set(newX, newY, newZ, newW);
//	}

	public Quat mulInverse(final Quat a) {
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}

		float newX = ((x * a.w) - (w * a.x) - (y * a.z) + (z * a.y)) * len;
		float newY = ((y * a.w) - (w * a.y) - (z * a.x) + (x * a.z)) * len;
		float newZ = ((z * a.w) - (w * a.z) - (x * a.y) + (y * a.x)) * len;
		float newW = ((w * a.w) + (x * a.x) + (y * a.y) + (z * a.z)) * len;

		return (Quat) set(newX, newY, newZ, newW);
	}

	public Quat mulLeft(Quat a) {
		return set(getProd(a, this));
	}

	public Quat getInverted() {
		return new Quat(this).invertRotation();
	}

	public Quat invertRotation() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Quat set(Quat q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
		return this;
	}

	public Quat set(Vec4 a) {
		x = a.x;
		y = a.y;
		z = a.z;
		w = a.w;
		return this;
	}

	public static Quat parseQuat(String s) {
		String unbracketed = s.replaceAll("[\\[\\](){}]", "");
		String[] numbers = unbracketed.split(",");
		float num0 = Float.parseFloat(numbers[0].strip());
		float num1 = Float.parseFloat(numbers[1].strip());
		float num2 = Float.parseFloat(numbers[2].strip());
		float num3 = Float.parseFloat(numbers[3].strip());
		return new Quat(num0, num1, num2, num3);
	}
}
