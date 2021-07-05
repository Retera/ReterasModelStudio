package com.hiveworkshop.rms.editor.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Vertex motion matrices.
 *
 * Eric Theller 11/10/2011
 */
public class Matrix {
	List<Integer> m_boneIds = new ArrayList<>();
	List<Bone> bones = new ArrayList<>();
	long identityHash = 0;

	public Matrix() {
	}

	public Matrix(final int id) {
		m_boneIds.add(id);
	}

	public Matrix(final Collection<Bone> newBones) {
		bones.addAll(newBones);
		recalculateId();
	}

	public Matrix(Bone newBone) {
		bones.add(newBone);
		recalculateId();
	}

	public Matrix(final int[] boneIds) {
		for (int boneId : boneIds) {
			m_boneIds.add(boneId);
		}
	}

	public String getName() {
		StringBuilder out = new StringBuilder();
		if (bones != null) {
			if (bones.size() > 0) {
				out = new StringBuilder(bones.get(0).getName());
				for (int i = 1; i < bones.size(); i++) {
					out.append(", ").append(bones.get(i).getName());
				}
			} else {
				out = new StringBuilder("Error bad bone list");
			}
		} else if (m_boneIds != null) {
			if (m_boneIds.size() > 0) {
				out = new StringBuilder(m_boneIds.get(0).toString());
				for (int i = 1; i < m_boneIds.size(); i++) {
					out.append(", ").append(m_boneIds.get(i).toString());
				}
			} else {
				out = new StringBuilder("Error bad bone ids");
			}
		}
		return out.toString();
	}

	long lastPopupTimeHack = 0;

	public void updateIds(final EditableModel mdlr) {
//		mdlr.sortIdObjects();
		m_boneIds.clear();
		List<Bone> bonesToRemove = new ArrayList<>();
		for (Bone bone : bones) {
			final int newId = mdlr.getObjectId(bone);
//			System.out.println("new id: " + newId + " for bone: " + bone.getName());
			if (newId >= 0) {
				m_boneIds.add(newId);
			} else {
				bonesToRemove.add(bone);
				new Exception("Matrix error").printStackTrace();
				if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
//					JOptionPane.showMessageDialog(null, "Error: A matrix's bone reference was missing in the model!" + "\nDid you move geometry between models and forget to update bones?");
					System.out.println("Error: A matrix's bone reference was missing in the model!" + "\nDid you move geometry between models and forget to update bones?");
					lastPopupTimeHack = System.currentTimeMillis();
				}
			}
		}
		if ((bones.size() != 0 && m_boneIds.size() == 0) || (m_boneIds.size() < bones.size())) {
			bones.removeAll(bonesToRemove);
			new Exception("Matrix error").printStackTrace();
			if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
				JOptionPane.showMessageDialog(null, "Error: bad sizes in matrix (" + (bones.size() - m_boneIds.size()) + " as difference, should be same size)\n Bad bones was removed from the matrix.");
				System.out.println("Error: bad sizes in matrix (" + (bones.size() - m_boneIds.size()) + " as difference, should be same size)");
				lastPopupTimeHack = System.currentTimeMillis();
			}
			recalculateId();
		}
	}

	public void updateBones(final EditableModel model) {
		bones.clear();
		for (Integer m_boneId : m_boneIds) {
			final Bone b = model.getBone(m_boneId);
			// if( b.getClass() == Helper.class ) { JOptionPane.showMessageDialog(null,"Error: Holy fo shizzle my grizzle! There's geometry attached to Helper "+b.getName()+" and that is very bad!"); }
			if (b != null) {
				bones.add(b);
			} else {
//				JOptionPane.showMessageDialog(null, "Error: A matrix's bone id was not referencing a real bone!");
				System.err.println("Error: A matrix's bone id was not referencing a real bone! " + m_boneId);
			}
		}
		recalculateId();
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
		this.bones.remove(null);
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

	public void addId(final int id) {
		m_boneIds.add(id);
	}

	public void replaceBones(Map<IdObject, IdObject> newBoneMap) {
		bones.replaceAll(b -> (Bone) newBoneMap.get(b));
		recalculateId();
	}

	public int getBoneId(final int index) {
		return m_boneIds.get(index);
	}

	public int getBoneId(final int index, EditableModel model) {
		return model.getObjectId(bones.get(index));
	}

	public int size() {
//		if ((m_boneIds != null) && (m_boneIds.size() > 0)) { return m_boneIds.size(); } else
		if ((bones != null) && (bones.size() > 0)) {
			return bones.size();
		}
		// JOptionPane.showMessageDialog(null,"Warning: A matrix with no contents was used!");
		// System.out.println("Warning: A matrix with no contents was used!");
		return -1;// bad stuff
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
//		for(Bone b : bones){
//			identityHash = identityHash * 31 + b.hashCode();
//		}
		identityHash = bones.hashCode();
	}
}
