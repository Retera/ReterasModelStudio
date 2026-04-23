package com.hiveworkshop.wc3.mdl;

import javax.swing.JOptionPane;

/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I
 * wanted a simple object to encompass four quaternion values for rotation (this
 * is how MDLs handle rotating)
 *
 * Eric Theller 3/8/2012
 */
public class QuaternionRotation {
	public double a, b, c, d;

	public QuaternionRotation(final double a, final double b, final double c, final double d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public QuaternionRotation(final QuaternionRotation other) {
		this.a = other.a;
		this.b = other.b;
		this.c = other.c;
		this.d = other.d;
	}

	public QuaternionRotation(final Vertex eulerRotation) {
		set(eulerRotation);
	}

	public void set(final Vertex eulerRotation) {
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
		a = Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
				+ Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
		b = Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
				- Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
		c = Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
				+ Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
		d = Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2)
				- Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2);

		if (Math.abs(a) < 1E-15) {
			a = 0;
		}
		if (Math.abs(b) < 1E-15) {
			b = 0;
		}
		if (Math.abs(c) < 1E-15) {
			c = 0;
		}
		if (Math.abs(d) < 1E-15) {
			d = 0;
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
		a = data[0];
		b = data[1];
		c = data[2];
		d = data[3];
	}

	public QuaternionRotation(final Vertex axis, final double angle) {
		set(axis, angle);
	}

	public void set(final Vertex axis, final double angle) {
		final double halfAngle = angle / 2;
		final double sinOfHalfAngle = Math.sin(halfAngle);
		a = axis.x * sinOfHalfAngle;
		b = axis.y * sinOfHalfAngle;
		c = axis.z * sinOfHalfAngle;
		d = Math.cos(halfAngle);
	}

	public void normalize() {
		final double sumSq = Math.sqrt(a * a + b * b + c * c + d * d);
		a /= sumSq;
		b /= sumSq;
		c /= sumSq;
		d /= sumSq;
	}

	public double[] toArray() {
		return new double[] { a, b, c, d };
	}

	public float[] toFloatArray() {
		return new float[] { (float) a, (float) b, (float) c, (float) d };
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getC() {
		return c;
	}

	public double getD() {
		return d;
	}

	public Vertex getAxisOfRotation() {
		final double sqrt = Math.sqrt(1 - d * d);
		if (sqrt == 0) {
			return new Vertex(0, 0, 0);
		}
		return new Vertex(a / sqrt, b / sqrt, c / sqrt);
	}

	public double getAngleAroundAxis() {
		return 2 * Math.acos(d);
	}

	public Vertex toEuler() {
		// Wikipedia formula
		double roll = Math.atan2(2.0 * (a * b + c * d), 1 - 2.0 * (b * b + c * c));
		double stuff = a * c - d * b;
		if (stuff > 0.5) {
			stuff = 0.5;
		} else if (stuff < -0.5) {
			stuff = -0.5;
		}
		double pitch = Math.asin(2.0 * stuff);
		final double yaw = Math.atan2(2.0 * (a * d + b * c), 1 - 2.0 * (c * c + d * d));

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

		if (b * c + d * a == 0.5) {
			roll = 2 * Math.atan2(b, a);
			pitch = 0;
		} else if (b * c + d * a == -0.5) {
			roll = -2 * Math.atan2(b, a);
			pitch = 0;
		}

		return new Vertex(roll, pitch, yaw);
		// return new Vertex(heading, attitude, bank);
		// Now Quaternions can go burn and die.
	}

	public Vertex applyToVertex(final Vertex originOfRotation, final Vertex target) {
		final QuaternionRotation vector = new QuaternionRotation(0, target.x - originOfRotation.x,
				target.y - originOfRotation.y, target.z - originOfRotation.z);
		final QuaternionRotation conjugate = conjugate();
		final QuaternionRotation result = hamiltonianProduct(vector).hamiltonianProduct(conjugate);
		final Vertex resultInPosition = new Vertex(originOfRotation.x + result.b, originOfRotation.y + result.c,
				originOfRotation.z + result.d);
		return resultInPosition;
	}

	public QuaternionRotation hamiltonianProduct(final QuaternionRotation other) {
		return new QuaternionRotation(other.a * a - other.b * b - other.c * c - other.d * d,
				other.a * b + other.b * a - other.c * d + other.d * c,
				other.a * c + other.b * d + other.c * a - other.d * b,
				other.a * d - other.b * c + other.c * b + other.d * a);
	}

	public QuaternionRotation conjugate() {
		return new QuaternionRotation(a, -b, -c, -d);
	}

	public static QuaternionRotation parseText(final String input) {
		final String[] entries = input.split(",");
		QuaternionRotation temp = null;
		double a = 0;
		double b = 0;
		double c = 0;
		double d = 0;
		final String[] str = entries[0].split("\\{");
		try {
			a = Double.parseDouble(str[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: QuaternionRotation coordinates could not be interpreted.");
		}
		try {
			b = Double.parseDouble(entries[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: QuaternionRotation coordinates could not be interpreted.");
		}
		try {
			c = Double.parseDouble(entries[2]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: QuaternionRotation coordinates could not be interpreted.");
		}
		try {
			d = Double.parseDouble(entries[3].split("}")[0]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: QuaternionRotation coordinates could not be interpreted.");
		}
		temp = new QuaternionRotation(a, b, c, d);
		return temp;
	}

	public static QuaternionRotation ghostwolfNlerp(final QuaternionRotation out,
			final QuaternionRotation startingValue, final QuaternionRotation endingValue,
			final float interpolationFactor) {
		final double ax = startingValue.a, ay = startingValue.b, az = startingValue.c, aw = startingValue.d;
		final double bx = endingValue.a, by = endingValue.b, bz = endingValue.c, bw = endingValue.d;
		final float inverseFactor = 1 - interpolationFactor;
		final double x1 = inverseFactor * ax;
		final double y1 = inverseFactor * ay;
		final double z1 = inverseFactor * az;
		final double w1 = inverseFactor * aw;
		final double x2 = interpolationFactor * bx;
		final double y2 = interpolationFactor * by;
		final double z2 = interpolationFactor * bz;
		final double w2 = interpolationFactor * bw;

		// Dot product
		if (ax * bx + ay * by + az * bz + aw * bw < 0) {
			out.a = x1 - x2;
			out.b = y1 - y2;
			out.c = z1 - z2;
			out.d = w1 - w2;
		} else {
			out.a = x1 + x2;
			out.b = y1 + y2;
			out.c = z1 + z2;
			out.d = w1 + w2;
		}

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return out;
	}

	public static QuaternionRotation slerp(final QuaternionRotation out, final QuaternionRotation startingValue,
			final QuaternionRotation endingValue, final float interpolationFactor) {
		final double ax = startingValue.a, ay = startingValue.b, az = startingValue.c, aw = startingValue.d;
		double bx = endingValue.a, by = endingValue.b, bz = endingValue.c, bw = endingValue.d;
		final double omega;
		double cosom;
		final double sinom, scale0, scale1;
		// calc cosine
		cosom = ax * bx + ay * by + az * bz + aw * bw;
		// adjust signs (if necessary)
		if (cosom < 0) {
			cosom = -cosom;
			bx = -bx;
			by = -by;
			bz = -bz;
			bw = -bw;
		}
		// calculate coefficients
		if (1.0 - cosom > 0.000001) {
			// standard case (slerp)
			omega = Math.acos(cosom);
			sinom = Math.sin(omega);
			scale0 = Math.sin((1.0 - interpolationFactor) * omega) / sinom;
			scale1 = Math.sin(interpolationFactor * omega) / sinom;
		} else {
			// "from" and "to" quaternions are very close
			// ... so we can do a linear interpolation
			scale0 = 1.0 - interpolationFactor;
			scale1 = interpolationFactor;
		}

		out.a = scale0 * ax + scale1 * bx;
		out.b = scale0 * ay + scale1 * by;
		out.c = scale0 * az + scale1 * bz;
		out.d = scale0 * aw + scale1 * bw;

		// Super slow and generally not needed.
		// quat.normalize(out, out);
		return out;
	}

	private static QuaternionRotation temp1 = new QuaternionRotation(0, 0, 0, 0);
	private static QuaternionRotation temp2 = new QuaternionRotation(0, 0, 0, 0);

	public static QuaternionRotation ghostwolfNquad(final QuaternionRotation out, final QuaternionRotation a,
			final QuaternionRotation aOutTan, final QuaternionRotation bInTan, final QuaternionRotation b,
			final float t) {
		ghostwolfNlerp(temp1, a, b, t);
		ghostwolfNlerp(temp2, aOutTan, bInTan, t);
		ghostwolfNlerp(out, temp1, temp2, 2 * t * (1 - t));
		return out;
	}

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
		return "{ " + MDLReader.doubleToString(a) + ", " + MDLReader.doubleToString(b) + ", "
				+ MDLReader.doubleToString(c) + ", " + MDLReader.doubleToString(d) + " }";
	}

	public static void main(final String[] args) {
		QuaternionRotation rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		Vertex euler = rot.toEuler();
		euler.x = -euler.x;
		Vertex eulerRotation = new Vertex(euler);

		eulerRotation.x = Math.toDegrees(eulerRotation.x);
		eulerRotation.y = Math.toDegrees(eulerRotation.y);
		eulerRotation.z = Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		euler = rot.toEuler();
		euler.y = -euler.y;
		eulerRotation = new Vertex(euler);

		eulerRotation.x = Math.toDegrees(eulerRotation.x);
		eulerRotation.y = Math.toDegrees(eulerRotation.y);
		eulerRotation.z = Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		rot = new QuaternionRotation(0.241689, 0.152046, -0.372562, 0.882987);
		euler = rot.toEuler();
		euler.z = -euler.z;
		eulerRotation = new Vertex(euler);

		eulerRotation.x = Math.toDegrees(eulerRotation.x);
		eulerRotation.y = Math.toDegrees(eulerRotation.y);
		eulerRotation.z = Math.toDegrees(eulerRotation.z);
		System.out.println(rot);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));

		System.out.println();
		euler = new Vertex(Math.PI * (20.0 / 90.0), 0, 0);
		euler.x = -euler.x;
		eulerRotation = new Vertex(euler);

		eulerRotation.x = Math.toDegrees(eulerRotation.x);
		eulerRotation.y = Math.toDegrees(eulerRotation.y);
		eulerRotation.z = Math.toDegrees(eulerRotation.z);
		System.out.println(eulerRotation);
		System.out.println(new QuaternionRotation(euler));
		System.out.println();
		System.out.println(new QuaternionRotation(0, 0, 0.707107, 0.707107).toEuler());
		System.out.println(new QuaternionRotation(0.707107, 0, 0, 0.707107).toEuler());
		System.out.println(
				new QuaternionRotation(4.329780281177466E-017, 0.707107, 4.329780281177466E-017, 0.707107).toEuler());

		System.out.println("Time for a spin!");
		System.out.println(new QuaternionRotation(0.7071203316249954, 0.0, 0.7071203316249954, 0.0)
				.applyToVertex(new Vertex(0, 0, 0), new Vertex(1, 0, 0)));
	}

	public void setCoord(final byte index, final double value) {
		if (!Double.isNaN(value)) {
			switch (index) {
			case 0:
				a = value;
				break;
			case 1:
				b = value;
				break;
			case 2:
				c = value;
				break;
			case 3:
				d = value;
				break;
			}
		}
	}

	public double getCoord(final byte index) {
		switch (index) {
		case 0:
			return a;
		case 1:
			return b;
		case 2:
			return c;
		case 3:
			return d;
		}
		return 0;
	}

	public boolean equalContents(QuaternionRotation v) {
		return a == v.a && b == v.b && c == v.c && d == v.d;
	}

	public boolean roughlyEqualContents(QuaternionRotation v) {
		return Math.abs(a - v.a) <= Double.MIN_VALUE && Math.abs(b - v.b) <= Double.MIN_VALUE
				&& Math.abs(c - v.c) <= Double.MIN_VALUE && Math.abs(d - v.d) <= Double.MIN_VALUE;
	}
}
