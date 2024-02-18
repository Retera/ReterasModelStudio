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
		this.dist = point.dot(norm);
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
		this.dist = point.dot(norm);
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
	public Plane setFrom(Vec3... tri) {
		norm.set(tri[1]).sub(tri[0]);
		tempVec.set(tri[2]).sub(tri[1]);
		norm.cross(tempVec).normalize();
//		setPoint(tri[0]);
		this.dist = tri[0].dot(norm);
		return this;
	}
	public Plane setPoint(Vec3 point) {
		this.dist = point.dot(norm);
		return this;
	}

	public float getIntersectBad(Ray ray) {
		// I can't remember where this came from, but it seems to be wrong...
		float numeratorP = norm.dot(ray.getPoint()) + dist;
		float denominatorP = norm.dot(ray.getDir());
		return -(numeratorP / denominatorP);
	}

	public float getIntersect(Ray ray) {
		float projection = getPoint().sub(ray.getPoint()).dot(norm);
		float normProj = ray.getDir().dot(norm);
		return projection / normProj;
	}
	public Vec3 getIntersectP(Ray ray) {
		float rayDist = getIntersect(ray);
		return tempVec.set(ray.getPoint()).addScaled(ray.getDir(), rayDist);
	}

	private final Vec3 tempVec = new Vec3();
	public boolean inFrontOf(Vec3 point){
		tempVec.set(point).addScaled(norm, -dist);
		return tempVec.dot(norm) > 0;
	}
	public boolean inFrontOf(Vec3 point, float margin){
		tempVec.set(point).addScaled(norm, -dist);
		return (tempVec.dot(norm)+margin) > 0;
	}
	public float normDistTo(Vec3 point){
		tempVec.set(point).addScaled(norm, -dist);
		return tempVec.dot(norm);
	}

	public boolean pointOnPlane(Vec3 v) {
		tempVec.set(v).addScaled(norm, -dist);
		float dot = tempVec.dot(norm);
		return Math.abs(dot) < Float.MIN_VALUE*10f;
	}

	public Vec3 getPoint(){
		return tempVec.set(norm).scale(dist);
	}

}
