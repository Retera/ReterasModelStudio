package com.hiveworkshop.rms.editor.model;

import java.util.Objects;

public class SkinBone {
	short weight;
	Bone bone;

	SkinBone() {
	}

	SkinBone(SkinBone skinBone) {
		this.weight = skinBone.weight;
		this.bone = skinBone.bone;
	}

	public SkinBone(short weight, Bone bone) {
		this.weight = weight;
		this.bone = bone;
	}

	public SkinBone(short weight) {
		this.weight = weight;
		this.bone = null;
	}

	SkinBone(Bone bone) {
		this.weight = 0;
		this.bone = bone;
	}

	public SkinBone set(short weight, Bone bone) {
		this.bone = bone;
		this.weight = weight;
		return this;
	}

	public Bone getBone() {
		return bone;
	}

	public SkinBone setBone(Bone bone) {
		this.bone = bone;
		return this;
	}

	public short getWeight() {
		return weight;
	}
	public float getWeightFraction() {
		return weight/255f;
	}

	public SkinBone setWeight(short weight) {
		this.weight = weight;
		return this;
	}

	int getBoneId(EditableModel model) {
		return model.getObjectId(bone);
	}

	public SkinBone copy() {
		return new SkinBone(weight, bone);
	}

	public boolean equals(SkinBone otherSkinBone) {
//            return weight == otherSkinBone.weight && bone.equals(otherSkinBone.bone);
		return weight == otherSkinBone.weight && bone == otherSkinBone.bone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SkinBone skinBone = (SkinBone) o;
		return weight == skinBone.weight && Objects.equals(bone, skinBone.bone);
	}

	@Override
	public int hashCode() {
		return Objects.hash(weight, bone);
	}
}
