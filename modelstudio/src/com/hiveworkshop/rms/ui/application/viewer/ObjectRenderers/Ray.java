package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Vec3;

public class Ray {
	private final Vec3 dir;
	private final Vec3 point;

	public Ray() {
		this.dir = new Vec3();
		this.point = new Vec3();
	}

	public Ray(Vec3 dir, Vec3 point) {
		this.dir = dir;
		this.point = point;
	}

	public Vec3 getDir() {
		return dir;
	}

	public Vec3 getPoint() {
		return point;
	}

	public Ray set(Vec3 dir, Vec3 point) {
		this.dir.set(dir);
		this.point.set(point);
		return this;
	}
	public Ray set(Ray ray) {
		this.dir.set(ray.dir);
		this.point.set(ray.point);
		return this;
	}

	public Ray setFromPoints(Vec3 start, Vec3 end) {
		this.dir.set(end).sub(start).normalize();
		this.point.set(start);
		return this;
	}

	public Ray setDir(Vec3 dir) {
		this.dir.set(dir);
		return this;
	}

	public Ray setDirFromEnd(Vec3 end) {
		this.dir.set(end).sub(point).normalize();
		return this;
	}

	public Ray setPoint(Vec3 point) {
		this.point.set(point);
		return this;
	}
}
