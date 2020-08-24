package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Vertex motion matrices.
 *
 * Eric Theller 11/10/2011
 */
public class Matrix {
	List<Integer> m_boneIds;
	List<Bone> bones;

	public Matrix() {
		m_boneIds = new ArrayList<>();
	}

	public Matrix(final int id) {
		m_boneIds = new ArrayList<>();
		m_boneIds.add(id);
	}

	public String getName() {
		String out = "";
		if (bones != null) {
			if (bones.size() > 0) {
				out = bones.get(0).getName();
				for (int i = 1; i < bones.size(); i++) {
					out = out + ", " + bones.get(i).getName();
				}
			} else {
				out = "Error bad bone list";
			}
		} else if (m_boneIds != null) {
			if (m_boneIds.size() > 0) {
				out = m_boneIds.get(0).toString();
				for (int i = 1; i < m_boneIds.size(); i++) {
					out = out + ", " + m_boneIds.get(i).toString();
				}
			} else {
				out = "Error bad bone ids";
			}
		}
		return out;
	}

	long lastPopupTimeHack = 0;

	public void updateIds(final EditableModel mdlr) {
		final int sz1 = bones.size();
		if (m_boneIds == null) {
			m_boneIds = new ArrayList<>();
		} else {
			m_boneIds.clear();
		}
		for (int i = 0; i < bones.size(); i++) {
			final int newId = mdlr.getObjectId(bones.get(i));
			if (newId >= 0) {
				m_boneIds.add(newId);
			} else {
				new Exception("Matrix error").printStackTrace();
				if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
					JOptionPane.showMessageDialog(null,
							"Error: A matrix's bone reference was missing in the model!\nDid you move geometry between models and forget to update bones?");
					lastPopupTimeHack = System.currentTimeMillis();
				}
			}
		}
		if ((m_boneIds.size() < sz1) || ((sz1 != 0) && (m_boneIds.size() == 0))) {
			new Exception("Matrix error").printStackTrace();
			if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
				JOptionPane.showMessageDialog(null, "Error: bad sizes in matrix (" + (sz1 - m_boneIds.size())
						+ " as difference, should be same size)");
				lastPopupTimeHack = System.currentTimeMillis();
			}
		}
	}

	public void updateBones(final EditableModel mdlr) {
		if (bones == null) {
			bones = new ArrayList<>();
		} else {
			bones.clear();
		}
		for (int i = 0; i < m_boneIds.size(); i++) {
			final Bone b = mdlr.getBone(m_boneIds.get(i));
			// if( b.getClass() == Helper.class )
			// {
			// JOptionPane.showMessageDialog(null,"Error: Holy fo shizzle my
			// grizzle! There's geometry attached to Helper "+b.getName()+" and
			// that is very bad!");
			// }
			if (b != null) {
				bones.add(b);
			} else {
//				JOptionPane.showMessageDialog(null, "Error: A matrix's bone id was not referencing a real bone!");
				System.err.println("Error: A matrix's bone id was not referencing a real bone! " + m_boneIds.get(i));
			}
		}
	}

	public Matrix(final List<Bone> newBones) {
		bones = new ArrayList<>(newBones);
	}

	public Matrix(final int[] boneIds) {
		m_boneIds = new ArrayList<>();
		for (int i = 0; i < boneIds.length; i++) {
			m_boneIds.add(boneIds[i]);
		}
	}

	public void add(final Bone b) {
		bones.add(b);
	}

	public void addId(final int id) {
		m_boneIds.add(id);
	}

	public int getBoneId(final int index) {
		return m_boneIds.get(index);
	}

	public int size() {
		if ((m_boneIds != null) && (m_boneIds.size() > 0)) {
			return m_boneIds.size();
		} else if ((bones != null) && (bones.size() > 0)) {
			return bones.size();
		}
		// JOptionPane.showMessageDialog(null,"Warning: A matrix with no
		// contents was used!");
		// System.out.println("Warning: A matrix with no contents was used!");
		return -1;// bad stuff
	}

	public boolean equals(final Matrix other) {
		if (other.size() != size()) {
			return false;
		}
		boolean same = true;
		for (int i = 0; (i < size()) && same; i++) {
			if (bones.get(i) != other.bones.get(i)) {
				same = false;
			}
		}
		return same;
	}

	public List<Bone> getBones() {
		return bones;
	}

	public void setBones(final List<Bone> bones) {
		this.bones = bones;
	}
}
