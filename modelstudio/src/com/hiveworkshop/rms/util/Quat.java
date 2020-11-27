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

	public Quat(final double a, final double b, final double c, final double d) {
		set(a, b, c, d);
	}

	public Quat(final Quat other) {
		set(other);
	}

	public Quat(final Vec3 eulerRotation) {
		set(eulerRotation);
	}

	public void set(final Vec3 eulerRotation) {
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
		x = (float) ((Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2))
				+ (Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2)));
		y = (float) ((Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2))
				- (Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2)));
		z = (float) ((Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2))
				+ (Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2)));
		w = (float) ((Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2))
				- (Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)));

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
	}

	public Quat(final float[] data) {
		set(data);
	}

	public Quat(final Vec3 axis, final float angle) {
		setFromAxisAngle(axis, angle);
	}

	public void setFromAxisAngle(final Vec3 axis, final float angle) {
		setFromAxisAngle(axis.x, axis.y, axis.z, angle);
	}

	public void setFromAxisAngle(final Vec4 axis) {
		setFromAxisAngle(axis.x, axis.y, axis.z, axis.w);
	}

	public void setFromAxisAngle(final float ax, final float ay, final float az, final float angle) {
		final float halfAngle = angle / 2;
		final float sinOfHalfAngle = (float) Math.sin(halfAngle);
		x = ax * sinOfHalfAngle;
		y = ay * sinOfHalfAngle;
		z = az * sinOfHalfAngle;
		w = (float) Math.cos(halfAngle);
	}

	public void setIdentity() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
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

	private Quat slerp(float ax, float ay, float az, float aw, final float t, final Quat out) {
		final float scale0, scale1;
		// calc cosine
		float cosom = (x * ax) + (y * ay) + (z * az) + (w * aw);
		// adjust signs (if necessary)
		if (cosom < 0) {
			cosom = -cosom;
			ax = -ax;
			ay = -ay;
			az = -az;
			aw = -aw;
		}
		// calculate coefficients
		if ((1.0 - cosom) > 0.000001) {
			// standard case (slerp)
			final float omega = (float) Math.acos(cosom);
			final float sinom = (float) Math.sin(omega);

			scale0 = (float) Math.sin((1.0 - t) * omega) / sinom;
			scale1 = (float) Math.sin(t * omega) / sinom;
		} else {
			// "from" and "to" quaternions are very close
			// ... so we can do a linear interpolation
			scale0 = 1.0f - t;
			scale1 = t;
		}

		out.x = (scale0 * x) + (scale1 * ax);
		out.y = (scale0 * y) + (scale1 * ay);
		out.z = (scale0 * z) + (scale1 * az);
		out.w = (scale0 * w) + (scale1 * aw);

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return out;
	}

	public Quat slerp(final Quat a, final float t, final Quat out) {
		return slerp(a.x, a.y, a.z, a.w, t, out);
	}

	public Quat slerp(final Quat a, final float t) {
		return slerp(a, t, this);
	}

	public Quat squad(final Quat outTan, final Quat inTan, final Quat a, final float t, final Quat out) {
        outTan.slerp(inTan, t, out);

		final float x = out.x;
		final float y = out.y;
		final float z = out.z;
        final float w = out.w;
        
		slerp(a, t, out);

		out.slerp(x, y, z, w, 2 * t * (1 - t), out);
		
		return out;
	}

	public Quat squad(final Quat outTan, final Quat inTan, final Quat a, final float t) {
		return squad(outTan, inTan, a, t, this);
	}

	public Quat mul(final Quat a, final Quat out) {
		final float ax = a.x;
		final float ay = a.y;
		final float az = a.z;
		final float aw = a.w;

		final float thisx = x;
		final float thisy = y;
		final float thisz = z;
		final float thisw = w;

		out.x = (thisx * aw) + (thisw * ax) + (thisy * az) - (thisz * ay);
		out.y = (thisy * aw) + (thisw * ay) + (thisz * ax) - (thisx * az);
		out.z = (thisz * aw) + (thisw * az) + (thisx * ay) - (thisy * ax);
		out.w = (thisw * aw) - (thisx * ax) - (thisy * ay) - (thisz * az);

		return out;
	}

	public Quat mul(final Quat a) {
		return mul(a, this);
	}

	public Quat mulInverse(final Quat a, final Quat out) {
		final float ax = a.x;
		final float ay = a.y;
		final float az = a.z;
		final float aw = a.w;
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}
		
		out.x =	((x * aw) - (w * ax) - (y * az) + (z * ay)) * len;
		out.y = ((y * aw) - (w * ay) - (z * ax) + (x * az)) * len;
		out.z = ((z * aw) - (w * az) - (x * ay) + (y * ax)) * len;
		out.w = ((w * aw) + (x * ax) + (y * ay) + (z * az)) * len;

		return out;
	}

	public Quat mulInverse(final Quat a) {
		return mulInverse(a, this);
	}

	public Vec4 transform(final Vec4 a, final Vec4 out) {
        final float ax = a.x;
        final float ay = a.y;
        final float az = a.z;
        final float uvx = y * az - z * ay;
        final float uvy = z * ax - x * az;
        final float uvz = x * ay - y * ax;
        final float uuvx = y * uvz - z * uvy;
        final float uuvy = z * uvx - x * uvz;
        final float uuvz = x * uvy - y * uvx;
		final float w2 = w * 2;

        out.x = ax + (uvx * w2) + (uuvx * 2);
        out.y = ay + (uvy * w2) + (uuvy * 2);
		out.z = az + (uvz * w2) + (uuvz * 2);
		out.w = a.w;

        return out;
	}
	
	public Vec4 transform(final Vec4 a) {
		return transform(a, a);
	}

	public Vec3 transform(final Vec3 a, final Vec3 out) {
        final float ax = a.x;
        final float ay = a.y;
        final float az = a.z;
        final float uvx = y * az - z * ay;
        final float uvy = z * ax - x * az;
        final float uvz = x * ay - y * ax;
        final float uuvx = y * uvz - z * uvy;
        final float uuvy = z * uvx - x * uvz;
        final float uuvz = x * uvy - y * uvx;
		final float w2 = w * 2;

        out.x = ax + (uvx * w2) + (uuvx * 2);
        out.y = ay + (uvy * w2) + (uuvy * 2);
        out.z = az + (uvz * w2) + (uuvz * 2);

        return out;
	}
	
	public Vec3 transform(final Vec3 a) {
		return transform(a, a);
	}
}
