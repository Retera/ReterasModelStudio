package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Vec3;

public class Plane {
	private final Vec3 norm;
	private float dist;

	public Plane() {
		this.norm = new Vec3(Vec3.Z_AXIS);
		this.dist = 1.0f;
	}

	public Plane(Vec3 norm, float dist) {
		this.norm = norm;
		this.dist = dist;
	}

	public Plane(Vec3 norm, Vec3 point) {
		this.norm = norm;
		this.dist = -point.dot(norm);
	}

	public Vec3 getNorm() {
		return norm;
	}

	public float getDist() {
		return dist;
	}

	public Plane set(Vec3 norm, float dist) {
		this.norm.set(norm);
		this.dist = dist;
		return this;
	}

	public Plane set(Plane plane) {
		this.norm.set(plane.norm);
		this.dist = plane.dist;
		return this;
	}

	public Plane set(Vec3 norm, Vec3 point) {
		this.norm.set(norm).normalize();
		this.dist = -point.dot(norm);
		return this;
	}

	public Plane set(float dist) {
		this.dist = dist;
		return this;
	}

	public Plane set(Vec3 norm) {
		this.norm.set(norm).normalize();
		return this;
	}

	public Plane setPoint(Vec3 point) {
		this.dist = -point.dot(norm);
		return this;
	}

	public float getIntersect(Ray ray) {
		float denominatorP = norm.dot(ray.getDir());
		float numeratorP = norm.dot(ray.getPoint()) + dist;
		return -(numeratorP / denominatorP);
	}


	private final Vec3 tempVec = new Vec3();
	public boolean inFrontOf(Vec3 point){
		tempVec.set(point).addScaled(norm, -dist);
		return tempVec.dot(norm)>0;
	}

	public Vec3 getPoint(){
		return tempVec.set(norm).scale(dist);
	}

}
