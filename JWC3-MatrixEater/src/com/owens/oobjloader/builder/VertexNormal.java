package com.owens.oobjloader.builder;

public class VertexNormal {
    public float x = 0;
    public float y = 0;
    public float z = 0;

    public void add(final float x, final float y, final float z) {
	this.x += x;
	this.y += y;
	this.z += z;
    }

    public VertexNormal(final float x, final float y, final float z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    @Override
	public String toString() {
	if(null == this) {
		return "null";
	} else {
		return x+","+y+","+z;
	}
    }
}