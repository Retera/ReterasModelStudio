package com.owens.oobjloader.builder;

public class VertexGeometric {

    public float x = 0;
    public float y = 0;
    public float z = 0;

    public VertexGeometric(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
	public String toString() {
        if (null == this) {
            return "null";
        } else {
            return x + "," + y + "," + z;
        }
    }
}