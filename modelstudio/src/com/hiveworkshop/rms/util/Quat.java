package com.hiveworkshop.rms.util;

/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I
 * wanted a simple object to encompass four quaternion values for rotation (this
 * is how MDLs handle rotating)
 *
 * Eric Theller 3/8/2012
 */
public class Quat extends Vec4 {
	public static final Quat IDENTITY = new Quat(0,0,0,1);
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

	public Quat(final float[] data) {
		set(data);
	}

	public Quat(final Vec3 axis, final float angle) {
		setFromAxisAngle(axis, angle);
	}

	public static Quat getSquad(final Quat from, final Quat toward, final Quat outTan, final Quat inTan, final float t) {
		return new Quat(from).squad(toward, outTan, inTan, t);
	}

	public static Quat getSlerped(final Quat from, final Quat toward, final float t) {
		return new Quat(from).slerp(toward, t);
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
		// if( yaw > Math.PI ) {
		// yaw -= Math.PI; }
		// eulerRotation.z = yaw;
		// eulerRotation.y = -eulerRotation.y;
		double sinX = Math.sin(eulerRotation.x / 2.0);
		double sinY = Math.sin(eulerRotation.y / 2.0);
		double sinZ = Math.sin(eulerRotation.z / 2.0);
		double cosX = Math.cos(eulerRotation.x / 2.0);
		double cosY = Math.cos(eulerRotation.y / 2.0);
		double cosZ = Math.cos(eulerRotation.z / 2.0);
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

		/**
		 * double heading = eulerRotation.x; double attitude = eulerRotation.y;
		 * double bank = eulerRotation.z; double c1 = Math.cos(heading);
		 * double s1 = Math.sin(heading); double c2 = Math.cos(attitude);
		 * double s2 = Math.sin(attitude); double c3 = Math.cos(bank);
		 * double s3 = Math.sin(bank); a = Math.sqrt(1.0 + c1 * c2 + c1*c3 - s1 * s2 * s3 + c2*c3) / 2.0;
		 * double w4 = (4.0 * a); b = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4 ;
		 * c = (s1 * c2 + s1 * c3 + c1 * s2 * s3) / w4 ; d = (-s1 * s3 + c1 * s2 * c3 +s2) / w4 ;
		 */

		// Now Quaternions can go burn and die.
		return this;
	}

	public Vec3 wikiToEuler(){
		// roll (x-axis rotation)
		double sinr_cosp = 2 * (w * x + y * z);
		double cosr_cosp = 1 - 2 * (x * x + y * y);
		double roll = Math.atan2(sinr_cosp, cosr_cosp);

		// pitch (y-axis rotation)
		double sinp = 2 * (w * y - z * x);
		// use 90 degrees if out of range
		double pitch = Math.abs(sinp) >= 1 ? Math.copySign(Math.PI / 2.0, sinp) : Math.asin(sinp);


		// yaw (z-axis rotation)
		double siny_cosp = 2 * (w * z + x * y);
		double cosy_cosp = 1 - 2 * (y * y + z * z);
		double yaw = Math.atan2(siny_cosp, cosy_cosp);

		return new Vec3(roll, pitch, yaw);
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
		double yaw = (Math.atan2(2.0 * ((x * w) + (y * z)), 1 - (2.0 * ((z * z) + (w * w)))));

		// yaw = Math.PI - yaw;
		// if( yaw > Math.PI ) {
		// yaw -= Math.PI; }
		// pitch = -pitch;

		// //Wikipedia formula with some twists
		// double roll = (Math.atan2( 2.0 * ( d * a + b * c ), 1 - 2.0 * ( a * a + b * b ) ) );
		// double pitch = (Math.asin( 2.0 * ( d * b - c * a ) ) );
		// double yaw = (Math.atan2( 2.0 * ( d * c + a * b ), 1 - 2.0 * ( b * b + c * c) ) );



		// //www.eulideanspace.com formula
		// double bank = (Math.atan2( 2.0 * ( a * b - c * d ), 1 - 2.0 * ( b * b + d * d ) ) );
		// double attitude = (Math.asin( 2.0 * ( b * c + d * a ) ) );
		// double heading = (Math.atan2( 2.0 * ( c * d + b * d ), 1 - 2.0 * ( c * c + d * d) ) );
		// //Math.toDegrees
		// if( b * c + d * a == 0.5 ) {
		// heading = 2 * Math.atan2(b,a);
		// bank = 0; } else if( b * c + d * a == -0.5 ) {
		// heading = - 2 * Math.atan2(b,a);
		// bank = 0; }

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

	public Quat squad1(final Quat toward, final Quat outTan, final Quat inTan, final float t) {
//		Quat temp = getSlerped(outTan, inTan, t);
		Quat temp = new Quat(outTan).slerp(inTan, t);

		slerp(toward, t);
		return slerp(temp, 2 * t * (1 - t));
	}

	public Quat squad(final Quat toward, final Quat outTan, final Quat inTan, final float t) {

		float scale0;
		float scale1;
		float dir = 1;

		// calc cosine
		float cosOm = (x * toward.x) + (y * toward.y) + (z * toward.z) + (w * toward.w);

		// adjust signs (if necessary)
		if (cosOm < 0) {
			cosOm = -cosOm;
			dir = -1;
		}
		// calculate coefficients
		if ((1.0 - cosOm) > 0.000001) {
			// standard case (slerp)
			float omega = (float) Math.acos(cosOm);
			float sinOm = (float) Math.sin(omega);

			scale0 = (float) Math.sin((1.0 - t) * omega) / sinOm;
			scale1 = (float) Math.sin(t * omega) / sinOm;
		} else {
			// if "from" and "to" quaternions are very close we can do a linear interpolation
			scale0 = 1.0f - t;
			scale1 = t;
		}
		this.scale(scale0).addScaled(toward, scale1 * dir);
		float lengthSquared = lengthSquared();
		if (.99 > lengthSquared || lengthSquared > 1.01) {
			float size_adj = 1.0f / (float) Math.sqrt(lengthSquared);
			this.scale(size_adj);
		}


//		Quat temp = new Quat(outTan);
		float scale0_inT;
		float scale1_inT;
		float dir_inT = 1;

		// calc cosine
		float cosOm_inT = (outTan.x * inTan.x) + (outTan.y * inTan.y) + (outTan.z * inTan.z) + (outTan.w * inTan.w);

		// adjust signs (if necessary)
		if (cosOm_inT < 0) {
			cosOm_inT = -cosOm_inT;
			dir_inT = -1;
		}
		// calculate coefficients
		if ((1.0 - cosOm_inT) > 0.000001) {
			// standard case (slerp)
			float omega = (float) Math.acos(cosOm_inT);
			float sinOm = (float) Math.sin(omega);

			scale0_inT = (float) Math.sin((1.0 - t) * omega) / sinOm;
			scale1_inT = (float) Math.sin(t * omega) / sinOm;
		} else {
			// if "from" and "to" quaternions are very close we can do a linear interpolation
			scale0_inT = 1.0f - t;
			scale1_inT = t;
		}
		float tempX = outTan.x * scale0_inT + inTan.x * scale1_inT * dir_inT;
		float tempY = outTan.y * scale0_inT + inTan.y * scale1_inT * dir_inT;
		float tempZ = outTan.z * scale0_inT + inTan.z * scale1_inT * dir_inT;
		float tempW = outTan.w * scale0_inT + inTan.w * scale1_inT * dir_inT;
//		temp.scale(scale0_inT).addScaled(inTan, scale1_inT * dir_inT);
//		float lengthSquared_inT = temp.lengthSquared();
		float lengthSquared_inT = tempX * tempX + tempY * tempY + tempZ * tempZ + tempW * tempW;
		if (.9 > lengthSquared_inT || lengthSquared_inT > 1.1) {
			float size_adj = 1.0f / (float) Math.sqrt(lengthSquared_inT);
//			temp.scale(size_adj);
			tempX *= size_adj;
			tempY *= size_adj;
			tempZ *= size_adj;
			tempW *= size_adj;
		}

		float lastT = 2 * t * (1 - t);
		float scale0_last;
		float scale1_last;
		float dir_last = 1;

		// calc cosine
//		float cosOm_last = (x * temp.x) + (y * temp.y) + (z * temp.z) + (w * temp.w);
		float cosOm_last = (x * tempX) + (y * tempY) + (z * tempZ) + (w * tempW);

		// adjust signs (if necessary)
		if (cosOm_last < 0) {
			cosOm_last = -cosOm_last;
			dir_last = -1;
		}
		// calculate coefficients
		if ((1.0 - cosOm_last) > 0.000001) {
			// standard case (slerp)
			float omega = (float) Math.acos(cosOm_last);
			float sinOm = (float) Math.sin(omega);

			scale0_last = (float) Math.sin((1.0 - lastT) * omega) / sinOm;
			scale1_last = (float) Math.sin(lastT * omega) / sinOm;
		} else {
			// if "from" and "to" quaternions are very close we can do a linear interpolation
			scale0_last = 1.0f - lastT;
			scale1_last = lastT;
		}
//		this.scale(scale0_last).addScaled(temp, scale1_last * dir_last);
		this.scale(scale0_last);
		x += tempX * scale1_last * dir_last;
		y += tempY * scale1_last * dir_last;
		z += tempZ * scale1_last * dir_last;
		w += tempW * scale1_last * dir_last;
		float lengthSquared_last = lengthSquared();
		if (.9 > lengthSquared_last || lengthSquared_last > 1.1) {
			float size_adj = 1.0f / (float) Math.sqrt(lengthSquared_last);
			this.scale(size_adj);
		}
		return this;
	}


	public Quat slerp(Quat toward, float t) {
		float scale0;
		float scale1;
		float dir = 1;

		// calc cosine
		float cosOm = (x * toward.x) + (y * toward.y) + (z * toward.z) + (w * toward.w);

		// adjust signs (if necessary)
		if (cosOm < 0) {
			cosOm = -cosOm;
			dir = -1;
		}
		// calculate coefficients
		if ((1.0 - cosOm) > 0.000001) {
			// standard case (slerp)
			float omega = (float) Math.acos(cosOm);
			float sinOm = (float) Math.sin(omega);

			scale0 = (float) Math.sin((1.0 - t) * omega) / sinOm;
			scale1 = (float) Math.sin(t * omega) / sinOm;
		} else {
			// if "from" and "to" quaternions are very close we can do a linear interpolation
			scale0 = 1.0f - t;
			scale1 = t;
		}
		this.scale(scale0).addScaled(toward, scale1 * dir);
		float lengthSquared = lengthSquared();
		if (.99 > lengthSquared || lengthSquared > 1.01) {
			float size_adj = 1.0f / (float) Math.sqrt(lengthSquared);
			this.scale(size_adj);
		}
		return this;
	}

	public static Quat getScaledInverted(final Quat a) {
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1 / len;
		}
		return (Quat) new Quat(a).invertRotation().scale(len);
	}


	public Quat mul(final Quat a) {
		float newX = (x * a.w) + (w * a.x) + (y * a.z) - (z * a.y);
		float newY = (y * a.w) + (w * a.y) + (z * a.x) - (x * a.z);
		float newZ = (z * a.w) + (w * a.z) + (x * a.y) - (y * a.x);
		float newW = (w * a.w) - (x * a.x) - (y * a.y) - (z * a.z);
		return (Quat) set(newX, newY, newZ, newW);
	}
	public Quat rotX(float rad) {
		float ax = (float) Math.sin(rad / 2.0);
		float aw = (float) Math.cos(rad / 2.0);
		float newX = (x * aw) + (w * ax);
		float newY = (y * aw) + (z * ax);
		float newZ = (z * aw) - (y * ax);
		float newW = (w * aw) - (x * ax);
		return (Quat) set(newX, newY, newZ, newW);
	}
	public Quat rotY(float rad) {
		float ay = (float) Math.sin(rad / 2.0);
		float aw = (float) Math.cos(rad / 2.0);
		float newX = (x * aw) - (z * ay);
		float newY = (y * aw) + (w * ay);
		float newZ = (z * aw) + (x * ay);
		float newW = (w * aw) - (y * ay);
		return (Quat) set(newX, newY, newZ, newW);
	}
	public Quat rotZ(float rad) {
		float az = (float) Math.sin(rad / 2.0);
		float aw = (float) Math.cos(rad / 2.0);
		float newX = (x * aw) + (y * az);
		float newY = (y * aw) - (x * az);
		float newZ = (z * aw) + (w * az);
		float newW = (w * aw) - (z * az);
		return (Quat) set(newX, newY, newZ, newW);
	}
//	public Quat setFromAxisAngle(float angle) {
//		float ax = 0;
//		float ay = 0;
//		float az = 0;
//		x = ax * (float) Math.sin(angle / 2.0);
//		y = ay * (float) Math.sin(angle / 2.0);
//		z = az * (float) Math.sin(angle / 2.0);
//		w = (float) Math.cos(angle / 2.0);
//		return this;
//	}

	public Quat mulLeft(Quat quat) {
		float newX = (quat.x * w) + (quat.w * x) + (quat.y * z) - (quat.z * y);
		float newY = (quat.y * w) + (quat.w * y) + (quat.z * x) - (quat.x * z);
		float newZ = (quat.z * w) + (quat.w * z) + (quat.x * y) - (quat.y * x);
		float newW = (quat.w * w) - (quat.x * x) - (quat.y * y) - (quat.z * z);
		return (Quat) set(newX, newY, newZ, newW);
	}

	public Quat mulInverse(final Quat a) {
		float len = a.lengthSquared();

		if (len > 0) {
			len = 1.0f / len;
		}

		float newX = ((x * a.w) - (w * a.x) - (y * a.z) + (z * a.y)) * len;
		float newY = ((y * a.w) - (w * a.y) - (z * a.x) + (x * a.z)) * len;
		float newZ = ((z * a.w) - (w * a.z) - (x * a.y) + (y * a.x)) * len;
		float newW = ((w * a.w) + (x * a.x) + (y * a.y) + (z * a.z)) * len;

		return (Quat) set(newX, newY, newZ, newW);
	}

	public Quat invertQuat() {
		float len = lengthSquared();

		if (len > 0) {
			len = 1.0f / len;
		}
		return (Quat) invertRotation().scale(len);
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
	public Quat invertRotation2() {
		w = -w;
		return this;
	}

	public Quat setAsRotBetween(Vec3 vec1, Vec3 vec2) {
//		Vec3 aNorm = new Vec3(vec1).normalize();
//		Vec3 bNorm = new Vec3(vec2).normalize();
		double angle = vec1.radAngleTo(vec2);

//		return setFromAxisAngle(cross, (float) angle).normalize().invertRotation();
//		Vec3 cross = new Vec3(aNorm).cross(bNorm).normalize();
		float lenA = vec1.length();
		float lenB = vec2.length();
		float scaleF = (lenB * lenA);

		if (scaleF == 0) {
			scaleF = 1;
		}
		float axisX = ((vec1.y * vec2.z) - (vec2.y * vec1.z)) / scaleF;
		float axisY = ((vec2.x * vec1.z) - (vec1.x * vec2.z)) / scaleF;
		float axisZ = ((vec1.x * vec2.y) - (vec2.x * vec1.y)) / scaleF;

		return setFromAxisAngle(axisX, axisY, axisZ, (float) angle).normalize().invertRotation();
	}

	public Quat rotationBetweenVectors(Vec3 start, Vec3 dest){
		float cosTheta = start.dotNorm(dest);
		Vec3 rotationAxis;

		if (cosTheta < -1 + 0.001f){
			// special case when vectors in opposite directions:
			// there is no "ideal" rotation axis
			// So guess one; any will do as long as it's perpendicular to start
			rotationAxis = new Vec3(0.0f, 0.0f, 1.0f).crossNorm(start);
			if (rotationAxis.length() < 0.01 ) // bad luck, they were parallel, try again!
				rotationAxis.set(1.0f, 0.0f, 0.0f).crossNorm(start);

			rotationAxis.normalize();
			return setFromAxisAngle(rotationAxis, (float) Math.toRadians(180));
		}

		rotationAxis = start.cross(dest);

		float s = (float) Math.sqrt( (1+cosTheta)*2 );
		float invs = 1 / s;
		set(rotationAxis.x * invs, rotationAxis.y * invs, rotationAxis.z * invs, s * 0.5f );
		return this;

	}

	public Quat setFromAxisAngle(final Vec3 axis, final float angle) {
		return setFromAxisAngle(axis.x, axis.y, axis.z, angle);
	}

	public Quat setFromAxisAngle(final Vec4 axis) {
		return setFromAxisAngle(axis.x, axis.y, axis.z, axis.w);
	}

	public Quat setFromAxisAngle(float ax, float ay, float az, float angle) {
		double halfAngle = angle / 2.0;
		float sinOfHalfAngle = (float) Math.sin(halfAngle);
		x = ax * sinOfHalfAngle;
		y = ay * sinOfHalfAngle;
		z = az * sinOfHalfAngle;
		w = (float) Math.cos(halfAngle);
		return this;
	}

	public Quat setFromAxisAngle2(float ax, float ay, float az, float angle) {
		float sinOfHalfAngle = (float) Math.sin(angle / 2.0);
		x = ax * sinOfHalfAngle;
		y = ay * sinOfHalfAngle;
		z = az * sinOfHalfAngle;
		w = (float) Math.cos(angle / 2.0);
		return this;
	}
	public Vec4 toAxisWithAngle() {
		float angle = (float) Math.acos(MathUtils.clamp(w, -1f, 1f)) * 2;
		float sinOfHalfAngle = (float) Math.sin(angle / 2.0);
		float ax = x / sinOfHalfAngle;
		float ay = y / sinOfHalfAngle;
		float az = z / sinOfHalfAngle;
		return new Vec4(ax, ay, az, angle);
	}
	public Vec3 getAxis() {
		float angle = (float) Math.acos(MathUtils.clamp(w, -1f, 1f)) * 2;
		float sinOfHalfAngle = (float) Math.sin(angle / 2.0);
		float ax = x / sinOfHalfAngle;
		float ay = y / sinOfHalfAngle;
		float az = z / sinOfHalfAngle;
		return new Vec3(ax, ay, az);
	}
	public float getAxisAngle() {
		return (float) Math.acos(MathUtils.clamp(w, -1f, 1f)) * 2;
	}

	public Quat setIdentity() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
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

	public boolean equals(Object v) {
		if(v instanceof Quat){
			Quat q = (Quat) v;
			float sign = Math.copySign(1f, w*q.w);
			return (x == sign*q.x) && (y == sign*q.y) && (z == sign*q.z) && (w == sign*q.w);
		}
		if(v instanceof Vec4){
			return equals((Vec4) v);
		}
		return false;
	}

	@Override
	public Quat normalize() {
		float len = lengthSquared();

		if (len > 0) {
			len = 1.0f / (float) Math.sqrt(len);
			return (Quat) scale(len);
		}
		return (Quat) set(0, 0, 0, 1);
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


	public Quat validate() {
		if (Float.isNaN(this.x)) {
			this.x = 0;
		}
		if (Float.isNaN(this.y)) {
			this.y = 0;
		}
		if (Float.isNaN(this.z)) {
			this.z = 0;
		}
		if (Float.isNaN(this.w)) {
			this.w = 0;
		}
		if (Float.isInfinite(this.x)) {
			this.x = 1;
		}
		if (Float.isInfinite(this.y)) {
			this.y = 1;
		}
		if (Float.isInfinite(this.z)) {
			this.z = 1;
		}
		if (Float.isInfinite(this.w)) {
			this.w = 1;
		}

		return this;
	}

	public Quat setFromMatrix(Mat4 m, Quat q) {
		return q.setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
	}
	public Quat setFromMatrix(Mat4 m) {
		return setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
	}


	private Quat setFromMat(float m00, float m01, float m02, float m10,
	                              float m11, float m12, float m20, float m21, float m22) {

		float s;
		float tr = m00 + m11 + m22;
		if (tr >= 0.0) {
			s = (float) Math.sqrt(tr + 1.0);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else {
			float max = Math.max(Math.max(m00, m11), m22);
			if (max == m00) {
				s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m01 + m10) * s;
				z = (m20 + m02) * s;
				w = (m21 - m12) * s;
			} else if (max == m11) {
				s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m12 + m21) * s;
				x = (m01 + m10) * s;
				w = (m02 - m20) * s;
			} else {
				s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m20 + m02) * s;
				y = (m12 + m21) * s;
				w = (m10 - m01) * s;
			}
		}
		return this;
	}
	public Quat setFromMat(Mat4 m) {
		return setFromUnnormalized(m);
//		float s;
//		float tr = m.m00 + m.m11 + m.m22;
//		if (tr >= 0.0) {
//			s = (float) Math.sqrt(tr + 1.0);
//			w = s * 0.5f;
//			s = 0.5f / s;
//			x = (m.m21 - m.m12) * s;
//			y = (m.m02 - m.m20) * s;
//			z = (m.m10 - m.m01) * s;
//		} else {
//			float max = Math.max(Math.max(m.m00, m.m11), m.m22);
//			if (max == m.m00) {
//				s = (float) Math.sqrt(m.m00 - (m.m11 + m.m22) + 1.0);
//				x = s * 0.5f;
//				s = 0.5f / s;
//				y = (m.m01 + m.m10) * s;
//				z = (m.m20 + m.m02) * s;
//				w = (m.m21 - m.m12) * s;
//			} else if (max == m.m11) {
//				s = (float) Math.sqrt(m.m11 - (m.m22 + m.m00) + 1.0);
//				y = s * 0.5f;
//				s = 0.5f / s;
//				z = (m.m12 + m.m21) * s;
//				x = (m.m01 + m.m10) * s;
//				w = (m.m02 - m.m20) * s;
//			} else {
//				s = (float) Math.sqrt(m.m22 - (m.m00 + m.m11) + 1.0);
//				z = s * 0.5f;
//				s = 0.5f / s;
//				x = (m.m20 + m.m02) * s;
//				y = (m.m12 + m.m21) * s;
//				w = (m.m10 - m.m01) * s;
//			}
//		}
//		return this;
	}


	public Quat setFromUnnormalized(Mat4 mat4) {
		return setFromUnnormalized(mat4.m00, mat4.m01, mat4.m02, mat4.m10, mat4.m11, mat4.m12, mat4.m20, mat4.m21, mat4.m22);
	}
	public Quat setFromUnnormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		float nm00 = m00, nm01 = m01, nm02 = m02;
		float nm10 = m10, nm11 = m11, nm12 = m12;
		float nm20 = m20, nm21 = m21, nm22 = m22;
		float lenX = invsqrt(m00 * m00 + m01 * m01 + m02 * m02);
		float lenY = invsqrt(m10 * m10 + m11 * m11 + m12 * m12);
		float lenZ = invsqrt(m20 * m20 + m21 * m21 + m22 * m22);
		nm00 *= lenX; nm01 *= lenX; nm02 *= lenX;
		nm10 *= lenY; nm11 *= lenY; nm12 *= lenY;
		nm20 *= lenZ; nm21 *= lenZ; nm22 *= lenZ;
		return setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22);
	}

	public static float invsqrt(float r) {
		return 1.0f / (float) Math.sqrt(r);
	}
	public Quat setFromNormalized(Mat4 mat4) {
		return setFromNormalized(mat4.m00, mat4.m01, mat4.m02, mat4.m10, mat4.m11, mat4.m12, mat4.m20, mat4.m21, mat4.m22);
	}
	public Quat setFromNormalized(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		float s;
		float tr = m00 + m11 + m22;
		if (tr >= 0.0f) {
			s = (float) Math.sqrt(tr + 1.0f);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m12 - m21) * s;
			y = (m20 - m02) * s;
			z = (m01 - m10) * s;
		} else {
			if (m00 >= m11 && m00 >= m22) {
				s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0f);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m10 + m01) * s;
				z = (m02 + m20) * s;
				w = (m12 - m21) * s;
			} else if (m11 > m22) {
				s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0f);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m21 + m12) * s;
				x = (m10 + m01) * s;
				w = (m20 - m02) * s;
			} else {
				s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0f);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m02 + m20) * s;
				y = (m21 + m12) * s;
				w = (m01 - m10) * s;
			}
		}
		return this;
	}
}
