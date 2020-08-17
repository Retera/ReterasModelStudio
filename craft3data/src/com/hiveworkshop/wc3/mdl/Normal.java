package com.hiveworkshop.wc3.mdl;

public class Normal extends Vertex {
	public Normal(final double x, final double y, final double z) {
		super(x, y, z);
	}

	public Normal(final Normal oldNorm) {
		super(oldNorm.x, oldNorm.y, oldNorm.z);
	}

	public void inverse() {
		x = -x;
		y = -y;
		z = -z;
	}
}