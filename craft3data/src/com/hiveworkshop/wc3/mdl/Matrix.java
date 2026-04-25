package com.hiveworkshop.wc3.mdl;
import hiveworkshop.localizationmanager.LocalizationManager;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Vertex motion matrices.
 *
 * Eric Theller 11/10/2011
 */
public class Matrix {
	ArrayList m_boneIds;
	List<Bone> bones;

	public Matrix() {
		m_boneIds = new ArrayList();
	}

	public Matrix(final int id) {
		m_boneIds = new ArrayList();
		m_boneIds.add(id);
	}

	public Matrix(final ArrayList boneIds, final boolean useIds) {
		if (useIds) {
			m_boneIds = boneIds;
		}
		else {
			bones = boneIds;
		}
	}

	public String getName() {
		String out = "";
		if (bones != null) {
			if (bones.size() > 0) {
				out = bones.get(0).getName();
				for (int i = 1; i < bones.size(); i++) {
					out = out + ", " + bones.get(i).getName();
				}
			}
			else {
				out = LocalizationManager.getInstance().get("string.matrix_getname_list");
			}
		}
		else if (m_boneIds != null) {
			if (m_boneIds.size() > 0) {
				out = m_boneIds.get(0).toString();
				for (int i = 1; i < m_boneIds.size(); i++) {
					out = out + ", " + m_boneIds.get(i).toString();
				}
			}
			else {
				out = LocalizationManager.getInstance().get("string.matrix_getname_ids");
			}
		}
		return out;
	}

	long lastPopupTimeHack = 0;

	public void updateIds(final EditableModel mdlr) {
		final int sz1 = bones.size();
		if (m_boneIds == null) {
			m_boneIds = new ArrayList();
		}
		else {
			m_boneIds.clear();
		}
		for (int i = 0; i < bones.size(); i++) {
			final int newId = mdlr.getObjectId(bones.get(i));
			if (newId >= 0) {
				m_boneIds.add(newId);
			}
			else {
				new Exception("exception.matrix_updateids_matrix").printStackTrace();
				if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
					JOptionPane.showMessageDialog(null,
							LocalizationManager.getInstance().get("dialog.matrix_updateids_model"));
					lastPopupTimeHack = System.currentTimeMillis();
				}
			}
		}
		if ((m_boneIds.size() < sz1) || ((sz1 != 0) && (m_boneIds.size() == 0))) {
			new Exception("exception.matrix_updateids_matrix").printStackTrace();
			if ((System.currentTimeMillis() - lastPopupTimeHack) > 2000) {
				JOptionPane.showMessageDialog(null, LocalizationManager.getInstance().get("dialog.matrix_updateids_bad_sizes") + (sz1 - m_boneIds.size())
						+ LocalizationManager.getInstance().get("dialog.matrix_updateids_same_size"));
				lastPopupTimeHack = System.currentTimeMillis();
			}
		}
	}

	public void updateBones(final EditableModel mdlr) {
		if (bones == null) {
			bones = new ArrayList<>();
		}
		else {
			bones.clear();
		}
		for (int i = 0; i < m_boneIds.size(); i++) {
			final Bone b = mdlr.getBone((Integer) m_boneIds.get(i));
			// if( b.getClass() == Helper.class )
			// {
			// JOptionPane.showMessageDialog(null,LocalizationManager.getInstance().get("dialog.matrix_updatebones_attached")+b.getName()+
			// LocalizationManager.getInstance().get("dialog.matrix_updatebones_bad"));
			// }
			if (b != null) {
				bones.add(b);
			}
			else {
//				JOptionPane.showMessageDialog(null, LocalizationManager.getInstance().get("dialog.matrix_updatebones_referencing"));
				System.err.println(LocalizationManager.getInstance().get("dialog.matrix_updatebones_referencing=") + m_boneIds.get(i));
			}
		}
	}

	private Matrix(final List<Bone> newBones) {
		bones = newBones;
	}

	public static Matrix create(final List<Bone> newBones) {
		return new Matrix(new ArrayList<>(newBones));
	}

	public static Matrix fromBoneLinksSimpleSD(final List<GeosetVertexBoneLink> newBones) {
		final List<Bone> bones = new ArrayList<>();
		for (final GeosetVertexBoneLink link : newBones) {
			bones.add(link.bone);
		}
		return new Matrix(bones);
	}

	public Matrix(final int[] boneIds) {
		m_boneIds = new ArrayList();
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
		return (Integer) m_boneIds.get(index);
	}

	public int size() {
		if ((m_boneIds != null) && (m_boneIds.size() > 0)) {
			return m_boneIds.size();
		}
		else if ((bones != null) && (bones.size() > 0)) {
			return bones.size();
		}
		// JOptionPane.showMessageDialog(null,LocalizationManager.getInstance().get("dialog.matrix_size_warning"));
		// System.out.println(LocalizationManager.getInstance().get("dialog.matrix_size_warning"));
		return -1;// bad stuff
	}

	public static Matrix parseText(final String input) {
		final String[] entries = input.split(",");
		Matrix temp = null;
		final int size = entries.length;
		final int[] boneId = new int[size];
		// When { 4, 2 }, splits around commas, it has 3 segments and we
		// only want the first two (the third being "" after ending comma)
		if (size == 1) {
			try {
				boneId[0] = (int) Long.parseLong(entries[0].split("\\{")[1].split("}")[0].split(" ")[1]);
			}
			catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						LocalizationManager.getInstance().get("global.dialog.error") + " \"" + input + "\": " + LocalizationManager.getInstance().get("dialog.matrix_parsetext_interpreted"));
			}
		}
		else {
			try {
				boneId[0] = (int) Long.parseLong(entries[0].split("\\{")[1].split(" ")[1]);
			}
			catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						LocalizationManager.getInstance().get("global.dialog.error") + " \"" + input + "\": " + LocalizationManager.getInstance().get("dialog.matrix_parsetext_interpreted"));
			}
			for (int i = 1; i < size; i++) {
				try {
					boneId[i] = (int) Long.parseLong(entries[i].split(" ")[1]);
				}
				catch (final NumberFormatException e) {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
							LocalizationManager.getInstance().get("global.dialog.error") + " \"" + input + "\": " + LocalizationManager.getInstance().get("dialog.matrix_parsetext_interpreted"));
				}
			}
		}
		if ((boneId != null) && (boneId[0] != -1)) {
			temp = new Matrix(boneId);
		}
		else {
			temp = new Matrix();
		}
		return temp;
	}

	public void printTo(final PrintWriter writer, final int tabHeight) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		if (m_boneIds.size() > 0) {
			writer.print(tabs + LocalizationManager.getInstance().get("println.matrix_printto_matrices") + " { " + m_boneIds.get(0).toString());
		}
		else {
			writer.print(tabs + LocalizationManager.getInstance().get("println.matrix_printto_matrices") + " { -1");
		}
		// writer.print(tabs+LocalizationManager.getInstance().get("println.matrix_printto_matrices") + " { "+bones.get(0).getClass().getName());
		for (int i = 1; i < bones.size(); i++) {
			writer.print(", " + m_boneIds.get(i));
			// writer.print(", "+bones.get(i).getClass().getName());
		}
		writer.println(" },");
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

	public void setBones(final ArrayList<Bone> bones) {
		this.bones = bones;
	}
}
