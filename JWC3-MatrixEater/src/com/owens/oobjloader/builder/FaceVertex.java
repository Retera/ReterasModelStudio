package com.owens.oobjloader.builder;

public class FaceVertex {

    int index = -1;
    public VertexGeometric v = null;
    public VertexTexture t = null;
    public VertexNormal n = null;

    @Override
	public String toString() {
        return v + "|" + n + "|" + t;
    }
}