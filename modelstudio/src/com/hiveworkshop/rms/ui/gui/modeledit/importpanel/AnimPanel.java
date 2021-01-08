package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class AnimPanel extends JPanel implements ChangeListener, ItemListener, ListSelectionListener {
	// Animation panel for controlling which are imported

	static final String IMPORTBASIC = "Import as-is";
	static final String CHANGENAME = "Change name to:";
	static final String TIMESCALE = "Time-scale into pre-existing:";
	static final String GLOBALSEQ = "Rebuild as global sequence";
	// title
	JLabel title;
	// Import option
	JCheckBox doImport;
	// Import option
	JCheckBox inReverse;
	// The animation for this panel
	Animation anim;
	String[] animOptions = {IMPORTBASIC, CHANGENAME, TIMESCALE, GLOBALSEQ};

	JComboBox<String> importTypeBox = new JComboBox<>(animOptions);

	JPanel cardPane = new JPanel();

	JPanel blankCardImp = new JPanel();
	JPanel blankCardGS = new JPanel();

	JPanel nameCard = new JPanel();
	JTextField newNameEntry = new JTextField("", 40);

	JPanel animListCard = new JPanel();
	DefaultListModel<AnimShell> existingAnims;
	DefaultListModel<AnimShell> listModel;
	JList<AnimShell> animList;
	JScrollPane animListPane;
	Object[] oldSelection = new Object[0];
	boolean listenSelection = true;

	public AnimPanel(final Animation anim, final DefaultListModel<AnimShell> existingAnims, final AnimListCellRenderer renderer) {
		this.existingAnims = existingAnims;
		listModel = new DefaultListModel<>();
		for (int i = 0; i < existingAnims.size(); i++) {
			listModel.addElement(existingAnims.get(i));
		}
		this.anim = anim;

		title = new JLabel(anim.getName());
		title.setFont(new Font("Arial", Font.BOLD, 26));

		doImport = new JCheckBox("Import this Sequence");
		doImport.setSelected(true);
		doImport.addChangeListener(this);

		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addChangeListener(this);

		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		// Restricts users to pre-existing choices,
		// they cannot enter text in the box
		// (I think? that's an untested guess)

		// Combo box items:
		newNameEntry.setText(anim.getName());
		nameCard.add(newNameEntry);

		animList = new JList<>(listModel);
		animList.setCellRenderer(renderer);
		animList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Use getSelectedValuesList().toArray() to request an array of selected
		// animations

		// Select any animation found that has the same name automatically
		// -- This iterates through the list of old animations and picks out
		// and like-named ones, so that the default selection is any animation
		// with the same name
		// (although this should stop after the first one is picked)
		animList.addListSelectionListener(this);
		for (int i = 0; (i < existingAnims.size()) && (animList.getSelectedIndex() == -1); i++) {
			final Animation iAnim = ((AnimShell) listModel.get(i)).anim;
			if (iAnim.getName().toLowerCase().equals(anim.getName().toLowerCase())) {
				animList.setSelectedValue(listModel.get(i), true);
			}
		}

		animListPane = new JScrollPane(animList);
		animListCard.add(animListPane);

		final CardLayout cardLayout = new CardLayout();
		cardPane.setLayout(cardLayout);
		cardPane.add(blankCardImp, IMPORTBASIC);
		cardPane.add(nameCard, CHANGENAME);
		cardPane.add(animListPane, TIMESCALE);
		cardPane.add(blankCardGS, GLOBALSEQ);
		// cardLayout.show(cardPane,IMPORTBASIC);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(8)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(title)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(doImport)
								.addComponent(inReverse)
								.addGroup(layout.createSequentialGroup()
										.addComponent(importTypeBox)
										.addComponent(cardPane)))).addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title).addGap(16)
				.addComponent(doImport)
				.addComponent(inReverse)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(importTypeBox)
						.addComponent(cardPane)));
		setLayout(layout);
	}

	public void setSelected(final boolean flag) {
		doImport.setSelected(flag);
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		importTypeBox.setEnabled(doImport.isSelected());
		cardPane.setEnabled(doImport.isSelected());
		animList.setEnabled(doImport.isSelected());
		newNameEntry.setEnabled(doImport.isSelected());
		updateSelectionPicks();

	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
		// --
		// http://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CardLayoutDemoProject/src/layout/CardLayoutDemo.java
		// -- http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
		// Thanks to the CardLayoutDemo.java at the above urls
		// in the JavaDocs for the example use of a CardLayout
		final CardLayout myLayout = (CardLayout) cardPane.getLayout();
		myLayout.show(cardPane, (String) e.getItem());
		updateSelectionPicks();
	}

	public void updateSelectionPicks() {
		listenSelection = false;
		// DefaultListModel newModel = new DefaultListModel();
		final Object[] selection = animList.getSelectedValuesList().toArray();
		listModel.clear();
		for (int i = 0; i < existingAnims.size(); i++) {
			final Animation temp = ((AnimShell) existingAnims.get(i)).importAnim;
			if ((temp == null) || (temp == anim)) {
				listModel.addElement(existingAnims.get(i));
			}
		}
		// for( int i = 0; i < existingAnims.size(); i++ )
		// {
		// newModel.addElement(existingAnims.get(i));
		// }
		// existingAnims.clear();
		// for( int i = 0; i < order.size(); i++ )
		// {
		// Object o = order.get(i);
		// if( newModel.contains(o) )
		// {
		// existingAnims.addElement(o);
		// }
		// }
		final int[] indices = new int[selection.length];
		for (int i = 0; i < selection.length; i++) {
			indices[i] = listModel.indexOf(selection[i]);
		}
		animList.setSelectedIndices(indices);
		listenSelection = true;

		final Object[] newSelection;
		if (doImport.isSelected() && (importTypeBox.getSelectedIndex() == 2)) {
			newSelection = animList.getSelectedValuesList().toArray();
		} else {
			newSelection = new Object[0];
		}
		// ImportPanel panel = getImportPanel();
		for (final Object a : oldSelection) {
			((AnimShell) a).setImportAnim(null);
		}
		for (final Object a : newSelection) {
			((AnimShell) a).setImportAnim(anim);
		}
		// panel.addAnimPicks(oldSelection,this);
		// panel.removeAnimPicks(newSelection,this);
		oldSelection = newSelection;
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (listenSelection && e.getValueIsAdjusting()) {
			updateSelectionPicks();
		}
	}

	public ImportPanel getImportPanel() {
		Container temp = getParent();
		while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
			temp = temp.getParent();
		}
		return (ImportPanel) temp;
	}

//	public void reorderToModel(final DefaultListModel order) {
//		// listenSelection = false;
//		// DefaultListModel newModel = new DefaultListModel();
//		// for( int i = 0; i < order.size(); i++ )
//		// {
//		// Object o = order.get(i);
//		// if( this.existingAnims.contains(o) )
//		// {
//		// newModel.addElement(o);
//		// }
//		// }
//		// this.existingAnims = newModel;
//		// animList.setModel(existingAnims);
//		// int [] indices = new int[oldSelection.length];
//		// for( int i = 0; i < oldSelection.length; i++ )
//		// {
//		// indices[i] = existingAnims.indexOf(oldSelection[i]);
//		// }
//		// animList.setSelectedIndices(indices);
//		// listenSelection = true;
//
//		// listenSelection = false;
//		// DefaultListModel newModel = new DefaultListModel();
//		// Object [] selection = animList.getSelectedValuesList().toArray();
//		// for( int i = 0; i < existingAnims.size(); i++ )
//		// {
//		// newModel.addElement(existingAnims.get(i));
//		// }
//		// existingAnims.clear();
//		// for( int i = 0; i < order.size(); i++ )
//		// {
//		// Object o = order.get(i);
//		// if( newModel.contains(o) )
//		// {
//		// existingAnims.addElement(o);
//		// }
//		// }
//		// int [] indices = new int[selection.length];
//		// for( int i = 0; i < selection.length; i++ )
//		// {
//		// indices[i] = existingAnims.indexOf(selection[i]);
//		// }
//		// animList.setSelectedIndices(indices);
//		// listenSelection = true;
//	}
}
