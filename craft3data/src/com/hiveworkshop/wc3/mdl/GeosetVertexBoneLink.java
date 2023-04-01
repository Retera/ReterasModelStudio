package com.hiveworkshop.wc3.mdl;

public class GeosetVertexBoneLink {
	private byte weight;
	private Bone bone;

	public GeosetVertexBoneLink(final byte weight, final Bone bone) {
		this.weight = weight;
		this.bone = bone;
	}

	public byte getWeight() {
		return weight;
	}

	public void setWeight(final byte weight) {
		this.weight = weight;
	}

	public Bone getBone() {
		return bone;
	}

	public void setBone(final Bone bone) {
		this.bone = bone;
	}
}
