package com.hiveworkshop.rms.util;

/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I
 * wanted a simple object to encompass four quaternion values for rotation (this
 * is how MDLs handle rotating)
 *
 * Eric Theller 3/8/2012
 */
public class QuaternionRotation {
	public float x = 0, y = 0, z = 0, w = 1;

	public QuaternionRotation() {

	}

	public QuaternionRotation(final double a, final double b, final double c, final double d) {
		set(a, b, c, d);
	}

	public QuaternionRotation(final QuaternionRotation other) {
		set(other);
	}

	public QuaternionRotation(final Vertex3 eulerRotation) {
		set(eulerRotation);
	}

	public void set(QuaternionRotation a) {
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

	public void set(final Vertex3 eulerRotation) {
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

	public QuaternionRotation(final float[] data) {
		x = data[0];
		y = data[1];
		z = data[2];
		w = data[3];
	}

	public QuaternionRotation(final Vertex3 axis, final float angle) {
		setFromAxisAngle(axis, angle);
	}

	public void setFromAxisAngle(final Vertex3 axis, final float angle) {
		setFromAxisAngle(axis.x, axis.y, axis.z, angle);
	}

	public void setFromAxisAngle(final Vertex4 axis) {
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

	public void normalize() {
		final double sumSq = Math.sqrt((x * x) + (y * y) + (z * z) + (w * w));
		x /= sumSq;
		y /= sumSq;
		z /= sumSq;
		w /= sumSq;
	}

	public double[] toArray() {
		return new double[] { x, y, z, w };
	}

	public float[] toFloatArray() {
		return new float[] { (float) x, (float) y, (float) z, (float) w };
	}

	public double getA() {
		return x;
	}

	public double getB() {
		return y;
	}

	public double getC() {
		return z;
	}

	public double getD() {
		return w;
	}

	public Vertex3 getAxisOfRotation() {
		final double sqrt = Math.sqrt(1 - (w * w));
		if (sqrt == 0) {
			return new Vertex3(0, 0, 0);
		}
		return new Vertex3(x / sqrt, y / sqrt, z / sqrt);
	}

	public double getAngleAroundAxis() {
		return 2 * Math.acos(w);
	}

	public Vertex3 toEuler() {
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

		return new Vertex3(roll, pitch, yaw);
		// return new Vertex(heading, attitude, bank);
		// Now Quaternions can go burn and die.
	}

	public Vertex3 applyToVertex(final Vertex3 originOfRotation, final Vertex3 target) {
		final QuaternionRotation vector = new QuaternionRotation(0, target.x - originOfRotation.x,
				target.y - originOfRotation.y, target.z - originOfRotation.z);
		final QuaternionRotation conjugate = conjugate();
		final QuaternionRotation result = hamiltonianProduct(vector).hamiltonianProduct(conjugate);
		final Vertex3 resultInPosition = new Vertex3(originOfRotation.x + result.y, originOfRotation.y + result.z,
				originOfRotation.z + result.w);
		return resultInPosition;
	}

	public QuaternionRotation hamiltonianProduct(final QuaternionRotation other) {
		return new QuaternionRotation((other.x * x) - (other.y * y) - (other.z * z) - (other.w * w),
				(((other.x * y) + (other.y * x)) - (other.z * w)) + (other.w * z),
				((other.x * z) + (other.y * w) + (other.z * x)) - (other.w * y),
				((other.x * w) - (other.y * z)) + (other.z * y) + (other.w * x));
	}

	public QuaternionRotation conjugate() {
		return new QuaternionRotation(x, -y, -z, -w);
	}

	public static QuaternionRotation slerp(final QuaternionRotation out, final QuaternionRotation startingValue,
			final QuaternionRotation endingValue, final float interpolationFactor) {
		final float ax = startingValue.x, ay = startingValue.y, az = startingValue.z, aw = startingValue.w;
		float bx = endingValue.x, by = endingValue.y, bz = endingValue.z, bw = endingValue.w;
		final float omega;
		float cosom;
		final float sinom, scale0, scale1;
		// calc cosine
		cosom = (ax * bx) + (ay * by) + (az * bz) + (aw * bw);
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
			omega = (float) Math.acos(cosom);
			sinom = (float) Math.sin(omega);
			scale0 = (float) Math.sin((1.0 - interpolationFactor) * omega) / sinom;
			scale1 = (float) Math.sin(interpolationFactor * omega) / sinom;
		} else {
			// "from" and "to" quaternions are very close
			// ... so we can do a linear interpolation
			scale0 = 1.0f - interpolationFactor;
			scale1 = interpolationFactor;
		}

		out.x = (scale0 * ax) + (scale1 * bx);
		out.y = (scale0 * ay) + (scale1 * by);
		out.z = (scale0 * az) + (scale1 * bz);
		out.w = (scale0 * aw) + (scale1 * bw);

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return out;
	}

	private static final QuaternionRotation temp1 = new QuaternionRotation(0, 0, 0, 0);
	private static final QuaternionRotation temp2 = new QuaternionRotation(0, 0, 0, 0);

	public static QuaternionRotation ghostwolfSquad(final QuaternionRotation out, final QuaternionRotation a,
			final QuaternionRotation aOutTan, final QuaternionRotation bInTan, final QuaternionRotation b,
			final float t) {
		slerp(temp1, a, b, t);
		slerp(temp2, aOutTan, bInTan, t);
		slerp(out, temp1, temp2, 2 * t * (1 - t));
		return out;
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + ", " + w + " }";
	}

	public static void main(final String[] args) {
		QuaternionRotation rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		Vertex3 euler = rot.toEuler();
		euler.x = -euler.x;
		Vertex3 eulerRotation = new Vertex3(euler);

		eulerRotation.x = (float) Math.toDegrees(eulerRotation.x);
		eulerRotation.y = (float) Math.toDegrees(eulerRotation.y);
		eulerRotation.z = (float) Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		euler = rot.toEuler();
		euler.y = -euler.y;
		eulerRotation = new Vertex3(euler);

		eulerRotation.x = (float) Math.toDegrees(eulerRotation.x);
		eulerRotation.y = (float) Math.toDegrees(eulerRotation.y);
		eulerRotation.z = (float) Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		euler = rot.toEuler();
		euler.z = -euler.z;
		eulerRotation = new Vertex3(euler);

		eulerRotation.x = (float) Math.toDegrees(eulerRotation.x);
		eulerRotation.y = (float) Math.toDegrees(eulerRotation.y);
		eulerRotation.z = (float) Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		euler = new Vertex3(Math.PI * (20.0 / 90.0), 0, 0);
		euler.x = -euler.x;
		eulerRotation = new Vertex3(euler);

		eulerRotation.x = (float) Math.toDegrees(eulerRotation.x);
		eulerRotation.y = (float) Math.toDegrees(eulerRotation.y);
		eulerRotation.z = (float) Math.toDegrees(eulerRotation.z);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));
		System.out.println();
		System.out.println(new QuaternionRotation(0, 0, 0.707107, 0.707107).toEuler());
		System.out.println(new QuaternionRotation(0.707107, 0, 0, 0.707107).toEuler());
		System.out.println(
				new QuaternionRotation(4.329780281177466E-017, 0.707107, 4.329780281177466E-017, 0.707107).toEuler());

		System.out.println("Time for a spin!");
		System.out.println(new QuaternionRotation(0.7071203316249954, 0.0, 0.7071203316249954, 0.0)
				.applyToVertex(new Vertex3(0, 0, 0), new Vertex3(1, 0, 0)));
	}

	public void setCoord(final byte index, final float value) {
		if (!Double.isNaN(value)) {
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

	public double getCoord(final byte index) {
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

	public static QuaternionRotation mul(QuaternionRotation a, QuaternionRotation b, QuaternionRotation out) {
		float ax = (float) a.x;
		float ay = (float) a.y;
		float az = (float) a.z;
		float aw = (float) a.w;
		float bx = (float) b.x;
		float by = (float) b.y;
		float bz = (float) b.z;
		float bw = (float) b.w;

		out.x = (ax * bw) + (aw * bx) + (ay * bz) - (az * by);
		out.y = (ay * bw) + (aw * by) + (az * bx) - (ax * bz);
		out.z = (az * bw) + (aw * bz) + (ax * by) - (ay * bx);
		out.w = (aw * bw) - (ax * bx) - (ay * by) - (az * bz);

		return out;
	}

	public static QuaternionRotation mulInverse(QuaternionRotation a, QuaternionRotation b, QuaternionRotation out) {
		float len = b.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}
		
		out.x =	(a.x * b.w - a.w * b.x - a.y * b.z + a.z * b.y) * len;
		out.y = (a.y * b.w - a.w * b.y - a.z * b.x + a.x * b.z) * len;
		out.z = (a.z * b.w - a.w * b.z - a.x * b.y + a.y * b.x) * len;
		out.w = (a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z) * len;

		return out;
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}
}
