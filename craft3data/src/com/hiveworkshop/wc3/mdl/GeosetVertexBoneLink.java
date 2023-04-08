package com.hiveworkshop.wc3.mdl;

public class GeosetVertexBoneLink {
	public short weight;
	public Bone bone;

	public GeosetVertexBoneLink(final short weight, final Bone bone) {
		this.weight = weight;
		this.bone = bone;
	}

	public short getWeight() {
		return weight;
	}

	public void setWeight(final short weight) {
		this.weight = weight;
	}

	public Bone getBone() {
		return bone;
	}

	public void setBone(final Bone bone) {
		this.bone = bone;
	}
}
