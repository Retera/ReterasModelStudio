package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class SimsVert {
	private int index;
	private boolean isBlendVert = false;
	private Vec3 pos;
	private Vec3 norm;
	private SimsTexVert texVert;
	private float scaleFactor = 20.0F;

	public SimsVert(int index, Vec3 pos, Vec3 norm) {
		this.index = index;
		this.pos = pos;
		this.norm = norm;
	}

	public SimsVert(int index, boolean isBlendVert, Vec3 pos, Vec3 norm) {
		this.index = index;
		this.isBlendVert = isBlendVert;
		this.pos = pos;
		this.norm = norm;
	}

	public SimsVert(int index, LineReaderThingi lineReaderThingi) {
		this.index = index;
		float[] data = lineReaderThingi.readFloats();
		this.pos = (new Vec3(data[0], data[1], data[2])).scale(this.scaleFactor);
		this.norm = new Vec3(data[3], data[4], data[5]);
	}

	public SimsVert(int index, boolean isBlendVert, LineReaderThingi lineReaderThingi) {
		this.index = index;
		this.isBlendVert = isBlendVert;
		float[] data = lineReaderThingi.readFloats();
		this.pos = (new Vec3(data[0], data[1], data[2])).scale(this.scaleFactor);
		this.norm = new Vec3(data[3], data[4], data[5]);
	}

	public int getIndex() {
		return this.index;
	}

	public SimsVert setIndex(int index) {
		this.index = index;
		return this;
	}

	public boolean isBlendVert() {
		return this.isBlendVert;
	}

	public SimsVert setBlendVert(boolean blendVert) {
		this.isBlendVert = blendVert;
		return this;
	}

	public SimsTexVert getTexVert() {
		return this.texVert;
	}

	public SimsVert setTexVert(SimsTexVert texVert) {
		this.texVert = texVert;
		return this;
	}

	public Vec3 getPos() {
		return this.pos;
	}

	public SimsVert setPos(Vec3 pos) {
		this.pos = pos;
		return this;
	}

	public Vec3 getNorm() {
		return this.norm;
	}

	public SimsVert setNorm(Vec3 norm) {
		this.norm = norm;
		return this;
	}

	public GeosetVertex createVertex() {
		GeosetVertex vertex = new GeosetVertex(this.pos, this.norm);
		if (this.texVert != null) {
			vertex.addTVertex(this.texVert.getPoint());
		} else {
			vertex.addTVertex(new Vec2());
		}

		return vertex;
	}

	public String asString() {
		float var10000 = this.pos.x / this.scaleFactor;
		return "" + var10000 + " " + this.pos.y / this.scaleFactor + " " + this.pos.z / this.scaleFactor + " " + this.norm.x + " " + this.norm.y + " " + this.norm.z;
	}
}
