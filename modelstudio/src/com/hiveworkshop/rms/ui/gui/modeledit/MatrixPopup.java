package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ParentToggleRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * The panel to handle re-assigning Matrices.
 *
 * Eric Theller 6/11/2012
 */

public class MatrixPopup extends JPanel implements ActionListener, ListSelectionListener, ChangeListener {
	JLabel title;

	// New refs
	JLabel newRefsLabel;
	public DefaultListModel<BoneShell> newRefs;
	JList<BoneShell> newRefsList;
	JScrollPane newRefsPane;
	JButton removeNewRef;
	JButton moveUp;
	JButton moveDown;

	// Bones (all available -- NEW AND OLD)
	JLabel bonesLabel;
	DefaultListModel<BoneShell> bones;
	JList<BoneShell> bonesList;
	JScrollPane bonesPane;
	JButton useBone;

	JCheckBox displayParents = new JCheckBox("Display parents", false);

	EditableModel model;

	public MatrixPopup(final EditableModel model) {
		this.model = model;
		final ModelView disp = new ModelViewManager(model);
		final ParentToggleRenderer renderer = new ParentToggleRenderer(displayParents, disp, null);
		displayParents.addChangeListener(this);

		bonesLabel = new JLabel("Bones");
		buildBonesList();
		// Built before oldBoneRefs, so that the MatrixShells can default to
		// using New Refs with the same name as their first bone
		bonesList = new JList<>(bones);
		bonesList.setCellRenderer(renderer);
		bonesPane = new JScrollPane(bonesList);
		bonesPane.setPreferredSize(new Dimension(400, 500));

		useBone = new JButton("Use Bone(s)", ImportPanel.greenArrowIcon);
		useBone.addActionListener(this);

		newRefsLabel = new JLabel("New Refs");
		newRefs = new DefaultListModel<>();
		newRefsList = new JList<>(newRefs);
		newRefsList.setCellRenderer(renderer);
		newRefsPane = new JScrollPane(newRefsList);
		newRefsPane.setPreferredSize(new Dimension(400, 500));

		removeNewRef = new JButton("Remove", ImportPanel.redXIcon);
		removeNewRef.addActionListener(this);
		moveUp = new JButton(ImportPanel.moveUpIcon);
		moveUp.addActionListener(this);
		moveDown = new JButton(ImportPanel.moveDownIcon);
		moveDown.addActionListener(this);

		buildLayout();

		refreshNewRefsList();
	}

	public void buildLayout() {
		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(displayParents)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(newRefsLabel)
								.addComponent(newRefsPane)
								.addComponent(removeNewRef))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(moveUp)
								.addComponent(moveDown))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(bonesLabel)
								.addComponent(bonesPane)
								.addComponent(useBone))));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(displayParents).addGap(10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(newRefsLabel)
						.addComponent(bonesLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(newRefsPane)
						.addGroup(layout.createSequentialGroup()
								.addComponent(moveUp).addGap(16)
								.addComponent(moveDown))
						.addComponent(bonesPane))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(removeNewRef)
						.addComponent(useBone)));
		setLayout(layout);
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		refreshLists();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == useBone) {
			for (final Object o : bonesList.getSelectedValuesList()) {
				if (!newRefs.contains(o)) {
					newRefs.addElement((BoneShell) o);
				}
			}
			refreshNewRefsList();
		} else if (e.getSource() == removeNewRef) {
			for (final Object o : newRefsList.getSelectedValuesList()) {
				int i = newRefsList.getSelectedIndex();
				newRefs.removeElement(o);
				if (i > newRefs.size() - 1) {
					i = newRefs.size() - 1;
				}
				newRefsList.setSelectedIndex(i);
			}
			refreshNewRefsList();
		} else if (e.getSource() == moveUp) {
			final int[] indices = newRefsList.getSelectedIndices();
			if (indices != null && indices.length > 0) {
				if (indices[0] > 0) {
					for (int i = 0; i < indices.length; i++) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] - 1, bs);
						indices[i] -= 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		} else if (e.getSource() == moveDown) {
			final int[] indices = newRefsList.getSelectedIndices();
			if (indices != null && indices.length > 0) {
				if (indices[indices.length - 1] < newRefs.size() - 1) {
					for (int i = indices.length - 1; i >= 0; i--) {
						final BoneShell bs = newRefs.get(indices[i]);
						newRefs.removeElement(bs);
						newRefs.add(indices[i] + 1, bs);
						indices[i] += 1;
					}
				}
				newRefsList.setSelectedIndices(indices);
			}
		}
	}

	public void refreshLists() {
		refreshNewRefsList();
	}

	MatrixShell currentMatrix = null;

	public void refreshNewRefsList() {
		// //Does save the currently constructed matrix
		// java.util.List selection = newRefsList.getSelectedValuesList();
		// if( currentMatrix != null )
		// {
		// currentMatrix.newBones.clear();
		// for( Object bs: newRefs.toArray() )
		// {
		// currentMatrix.newBones.add((BoneShell)bs);
		// }
		// }
		// newRefs.clear();
		// if( oldBoneRefsList.getSelectedValue() != null )
		// {
		// for( BoneShell bs:
		// ((MatrixShell)oldBoneRefsList.getSelectedValue()).newBones )
		// {
		// if( bones.contains(bs) )
		// newRefs.addElement(bs);
		// }
		// }
		//
		// int [] indices = new int[selection.size()];
		// for( int i = 0; i < selection.size(); i++ )
		// {
		// indices[i] = newRefs.indexOf(selection.get(i));
		// }
		// newRefsList.setSelectedIndices(indices);
		// currentMatrix = (MatrixShell)oldBoneRefsList.getSelectedValue();
	}

	public void reloadNewRefsList() {
		// //Does not save the currently constructed matrix
		// java.util.List selection = newRefsList.getSelectedValuesList();
		// newRefs.clear();
		// if( oldBoneRefsList.getSelectedValue() != null )
		// {
		// for( BoneShell bs:
		// ((MatrixShell)oldBoneRefsList.getSelectedValue()).newBones )
		// {
		// if( bones.contains(bs) )
		// newRefs.addElement(bs);
		// }
		// }
		//
		// int [] indices = new int[selection.size()];
		// for( int i = 0; i < selection.size(); i++ )
		// {
		// indices[i] = newRefs.indexOf(selection.get(i));
		// }
		// newRefsList.setSelectedIndices(indices);
		// currentMatrix = (MatrixShell)oldBoneRefsList.getSelectedValue();
	}

	public void buildBonesList() {
		bones = new DefaultListModel<>();
		final List<Bone> modelBones = model.getBones();
		// ArrayList<Bone> modelHelpers = model.sortedIdObjects(Bone.class);
		for (final Bone b : modelBones) {
			bones.addElement(new BoneShell(b));
		}
		// for( Bone b: modelHelpers )
		// {
		// bones.addElement(new BoneShell(b));
		// }
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		if (e.getSource() == displayParents) {
			repaint();
		}
	}
}