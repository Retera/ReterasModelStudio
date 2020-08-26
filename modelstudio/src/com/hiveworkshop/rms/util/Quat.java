package com.hiveworkshop.rms.util;

/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I
 * wanted a simple object to encompass four quaternion values for rotation (this
 * is how MDLs handle rotating)
 *
 * Eric Theller 3/8/2012
 */
public class Quat {
	public float x = 0, y = 0, z = 0, w = 1;

	public Quat() {

	}

	public Quat(final double a, final double b, final double c, final double d) {
		set(a, b, c, d);
	}

	public Quat(final Quat other) {
		set(other);
	}

	public Quat(final Vector3 eulerRotation) {
		set(eulerRotation);
	}

	public void set(Quat a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
		this.w = a.w;
	}

	public void set(double x, double y, double z, double w) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
		this.w = (float) w;
	}

	public void set(final Vector3 eulerRotation) {
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
		x = data[0];
		y = data[1];
		z = data[2];
		w = data[3];
	}

	public Quat(final Vector3 axis, final float angle) {
		setFromAxisAngle(axis, angle);
	}

	public void setFromAxisAngle(final Vector3 axis, final float angle) {
		setFromAxisAngle(axis.x, axis.y, axis.z, angle);
	}

	public void setFromAxisAngle(final Vector4 axis) {
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

	public Quat normalize(final Quat out) {
		float len = lengthSquared();

		if (len != 0) {
			len = 1 / len;
		}

		out.x = x * len;
		out.y = y * len;
		out.z = z * len;
		out.w = w * len;

		return out;
	}

	public Quat normalize() {
		return normalize(this);
	}

	public double[] toArray() {
		return new double[] { x, y, z, w };
	}

	public float[] toFloatArray() {
		return new float[] { (float) x, (float) y, (float) z, (float) w };
	}

	public Vector3 toEuler() {
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

		return new Vector3(roll, pitch, yaw);
		// return new Vertex(heading, attitude, bank);
		// Now Quaternions can go burn and die.
	}

	public Quat slerp(final Quat end, final float t, final Quat out) {
		float bx = end.x, by = end.y, bz = end.z, bw = end.w;
		final float scale0, scale1;
		// calc cosine
		float cosom = (x * bx) + (y * by) + (z * bz) + (w * bw);
		// adjust signs (if necessary)
		if (cosom < 0) {
			cosom = -cosom;
			bx = -bx;
			by = -by;
			bz = -bz;
			bw = -bw;
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

		out.x = (scale0 * x) + (scale1 * bx);
		out.y = (scale0 * y) + (scale1 * by);
		out.z = (scale0 * z) + (scale1 * bz);
		out.w = (scale0 * w) + (scale1 * bw);

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return out;
	}

	private static final Quat temp1 = new Quat(0, 0, 0, 0);
	private static final Quat temp2 = new Quat(0, 0, 0, 0);

	public static Quat squad(final Quat out, final Quat a,
			final Quat aOutTan, final Quat bInTan, final Quat b,
			final float t) {
		a.slerp(b, t, temp1);
		aOutTan.slerp(bInTan, t, temp2);
		temp1.slerp(temp2, 2 * t * (1 - t), out);
		
		return out;
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + ", " + w + " }";
	}

	public void setCoord(final byte index, final float value) {
		if (!Float.isNaN(value)) {
			switch (index) {
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			case 2:
				z = value;
				break;
			case 3:
				w = value;
				break;
			}
		}
	}

	public float getCoord(final byte index) {
		switch (index) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		case 3:
			return w;
		}
		return 0;
	}

	public Quat mul(final Quat a, final Quat out) {
		float ax = a.x;
		float ay = a.y;
		float az = a.z;
		float aw = a.w;

		out.x = (x * aw) + (w * ax) + (y * az) - (z * ay);
		out.y = (y * aw) + (w * ay) + (z * ax) - (x * az);
		out.z = (z * aw) + (w * az) + (x * ay) - (y * ax);
		out.w = (w * aw) - (x * ax) - (y * ay) - (z * az);

		return out;
	}

	public Quat mul(final Quat a) {
		return mul(a, this);
	}

	public Quat mulInverse(Quat a, Quat out) {
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}
		
		out.x =	((x * a.w) - (w * a.x) - (y * a.z) + (z * a.y)) * len;
		out.y = ((y * a.w) - (w * a.y) - (z * a.x) + (x * a.z)) * len;
		out.z = ((z * a.w) - (w * a.z) - (x * a.y) + (y * a.x)) * len;
		out.w = ((w * a.w) + (x * a.x) + (y * a.y) + (z * a.z)) * len;

		return out;
	}

	public Quat mulInverse(Quat a) {
		return mulInverse(a, this);
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}
}
