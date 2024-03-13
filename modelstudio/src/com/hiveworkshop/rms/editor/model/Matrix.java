package com.hiveworkshop.rms.editor.model;

import java.util.*;

/**
 * Vertex motion matrices.
 *
 * Eric Theller 11/10/2011
 */
public class Matrix {
	private List<Bone> bones = new ArrayList<>();
	private long identityHash = 0;

	public Matrix() {
	}

	public Matrix(final Collection<Bone> newBones) {
		bones.addAll(newBones);
		recalculateId();
	}

	public Matrix(Bone newBone) {
		bones.add(newBone);
		recalculateId();
	}

	public String getName() {
		if (bones != null) {
			if (bones.isEmpty()) {
				return "Error bad bone list";
			} else {
				String[] names = bones.stream().map(IdObject::getName).toArray(String[]::new);
				return String.join(", ", names);
			}
		}
		return "";
	}

	public void cureBones(List<Bone> modelBones) {
		bones.removeIf(b -> !modelBones.contains(b));
	}


	public Bone get(int i) {
		return bones.get(i);
	}

	public void add(final Bone bone) {
		if (bone != null) {
			bones.add(bone);
			recalculateId();
		}
	}

	public void add(int i, final Bone bone) {
		if (bone != null) {
			bones.add(i, bone);
			recalculateId();
		}
	}

	public void set(int i, final Bone bone) {
		if (bone != null) {
			bones.set(i, bone);
			recalculateId();
		}
	}

	public void addAll(Collection<Bone> bones) {
		this.bones.addAll(bones);
		this.bones.removeIf(Objects::isNull);
		recalculateId();
	}

	public void remove(int i) {
		bones.remove(i);
		recalculateId();
	}

	public void remove(final Bone bone) {
		bones.remove(bone);
		recalculateId();
	}

	public void removeAll(Collection<Bone> bones) {
		this.bones.removeAll(bones);
		recalculateId();
	}

	public void clear() {
		bones.clear();
		recalculateId();
	}

	public void replaceBones(Map<IdObject, IdObject> newBoneMap) {
		bones.replaceAll(b -> (Bone) newBoneMap.get(b));
		bones.removeIf(Objects::isNull);
		recalculateId();
	}

	public void replaceBones(Map<IdObject, IdObject> newBoneMap, boolean removeIfNotInMap) {
		bones.replaceAll(b -> removeIfNotInMap || newBoneMap.get(b) != null ? (Bone) newBoneMap.get(b) : b);
		bones.removeIf(Objects::isNull);
		recalculateId();
	}
	public void replaceBone(Bone oldBone, Bone newBone, boolean removeIfNewIsNull) {
		if(newBone != null){
			int i = bones.indexOf(oldBone);
			bones.set(i, newBone);
		} else if(removeIfNewIsNull){
			bones.remove(oldBone);
		}
		recalculateId();
	}

	public int size() {
		return bones != null ? bones.size() : -1;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Matrix) {
			return identityHash == ((Matrix) other).identityHash;
		}
		return false;
	}

	public boolean isEmpty() {
		return bones == null || bones.isEmpty();
	}

	public List<Bone> getBones() {
		return bones;
	}

	public void setBones(final List<Bone> bones) {
		this.bones = bones;
		recalculateId();
	}

	private void recalculateId() {
		identityHash = bones.hashCode();
	}

	@Override
	public int hashCode() {
		return bones.hashCode();
	}
}
