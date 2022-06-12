package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

import net.miginfocom.swing.MigLayout;

public class SmartMappingChooserPanel extends JPanel {
	private final DefaultListModel<Pairing> pairingListModel;
	private final BoneJList importList;
	private final BoneJList currentList;
	private final JList<Pairing> pairingList;
	private final Set<BoneShell> hasPairingSet = new HashSet<>();

	public SmartMappingChooserPanel(final ModelView importModelView, final ModelView currentModelView,
			final DefaultListModel<BoneShell> newBonesInResultModel) {
		final EditableModel importModel = importModelView.getModel();
		final EditableModel currentModel = currentModelView.getModel();
		pairingListModel = new DefaultListModel<Pairing>();
		pairingList = new JList<Pairing>(pairingListModel);
		final Map<String, LinkedList<BoneShell>> nameToBones = new HashMap<>();
		for (int i = 0; i < newBonesInResultModel.size(); i++) {
			final BoneShell futureBoneShell = newBonesInResultModel.get(i);
			final String futureBoneName = futureBoneShell.bone.getName();
			LinkedList<BoneShell> bones = nameToBones.get(futureBoneName);
			if (bones == null) {
				bones = new LinkedList<>();
				nameToBones.put(futureBoneName, bones);
			}
			bones.add(futureBoneShell);
		}
		final DefaultListModel<BoneShell> leftListModel = new DefaultListModel<BoneShell>();
		for (final Bone bone : currentModel.sortedIdObjects(Bone.class)) {
			final BoneShell bs = new BoneShell(bone);
			bs.showClass = true;
			bs.modelName = currentModel.getName();
			leftListModel.addElement(bs);
			LinkedList<BoneShell> defaultPairFriend = nameToBones.get(bone.getName());
			IdObject currentParent = bone;
			while (currentParent != null && defaultPairFriend == null || defaultPairFriend.isEmpty()) {
				defaultPairFriend = nameToBones.get(currentParent.getName());
				currentParent = currentParent.getParent();
			}
			if (defaultPairFriend != null && !defaultPairFriend.isEmpty()) {
				pairingListModel.addElement(new Pairing(bs, defaultPairFriend.getFirst()));
				hasPairingSet.add(bs);
				if (defaultPairFriend.size() > 1) {
					defaultPairFriend.removeFirst();
				}
			}
		}
		for (final Bone bone : importModel.sortedIdObjects(Bone.class)) {
			final BoneShell bs = new BoneShell(bone);
			bs.showClass = true;
			bs.modelName = importModel.getName();
			leftListModel.addElement(bs);
			LinkedList<BoneShell> defaultPairFriend = nameToBones.get(bone.getName());
			IdObject currentParent = bone;
			while (currentParent != null && (defaultPairFriend == null || defaultPairFriend.isEmpty())) {
				defaultPairFriend = nameToBones.get(currentParent.getName());
				currentParent = currentParent.getParent();
			}
			if (defaultPairFriend != null && !defaultPairFriend.isEmpty()) {
				pairingListModel.addElement(new Pairing(bs, defaultPairFriend.getFirst()));
				hasPairingSet.add(bs);
				if (defaultPairFriend.size() > 1) {
					defaultPairFriend.removeFirst();
				}
			}
		}
		importList = new BoneJList(leftListModel);
		currentList = new BoneJList(newBonesInResultModel);

		setLayout(new MigLayout("fill", "[fill, grow][fill, grow][fill, grow]", "[][fill, grow]"));

		final JCheckBox showParentsBox = new JCheckBox("Show parents");
		final ParentToggleRenderer parentToggleRenderer = new ParentToggleRenderer(showParentsBox, currentModelView,
				importModelView);
		showParentsBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				repaint();
			}
		});
		add(showParentsBox, "wrap");
		add(new JScrollPane(importList.list), "grow");
		add(new JScrollPane(pairingList), "grow");
		add(new JScrollPane(currentList.list), "grow");
		importList.list.setCellRenderer(parentToggleRenderer);
		currentList.list.setCellRenderer(parentToggleRenderer);
		importList.list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				final Component listCellRendererComponent = parentToggleRenderer.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				if (!hasPairingSet.contains(value)) {
					if (isSelected) {
						listCellRendererComponent.setBackground(Color.MAGENTA);
					} else {
						listCellRendererComponent.setBackground(Color.PINK);
					}
				} else {
					listCellRendererComponent.setBackground(null);
				}
				return listCellRendererComponent;
			}
		});

		setPreferredSize(new Dimension(1280, 720));

		currentList.list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				System.out.println("select");
				if (!e.getValueIsAdjusting()) {
					System.out.println("select good");
					final BoneShell left = importList.list.getSelectedValue();
					final BoneShell right = currentList.list.getSelectedValue();
					if (left != null && right != null) {
						System.out.println("select real stuff");
						int indexToRemove = -1;
						for (int i = 0; i < pairingListModel.size(); i++) {
							final Pairing existingPairing = pairingListModel.get(i);
							if (existingPairing.importBone.bone == left.bone) {
								indexToRemove = i;
							}
						}
						if (indexToRemove != -1) {
							pairingListModel.remove(indexToRemove);
						}

						System.out.println("add");
						pairingListModel.addElement(new Pairing(left, right));
						hasPairingSet.add(left);
						currentList.list.setSelectedValue(null, false);
						repaint();
					}
				}
			}
		});
		pairingList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					final Pairing selectedValue = pairingList.getSelectedValue();
					if (selectedValue != null) {
						hasPairingSet.remove(selectedValue.importBone);
						pairingListModel.remove(pairingList.getSelectedIndex());
						repaint();
					}
				}
			}
		});

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(final ComponentEvent e) {
				Component p = SmartMappingChooserPanel.this;
				while (p != null) {
					if (p instanceof JDialog) {
						((JDialog) p).setResizable(true);
					}
					p = p.getParent();
				}
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				Component p = SmartMappingChooserPanel.this;
				while (p != null) {
					if (p instanceof JDialog) {
						((JDialog) p).setResizable(true);
					}
					p = p.getParent();
				}
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
				Component p = SmartMappingChooserPanel.this;
				while (p != null) {
					if (p instanceof JDialog) {
						((JDialog) p).setResizable(true);
					}
					p = p.getParent();
				}
			}

			@Override
			public void componentHidden(final ComponentEvent e) {

			}
		});
	}

	public DefaultListModel<Pairing> getPairingListModel() {
		return pairingListModel;
	}

	static class BoneJList {
		private final DefaultListModel<BoneShell> listModel;
		private final JList<BoneShell> list;

		public BoneJList(final DefaultListModel<BoneShell> listModel) {
			this.listModel = listModel;
			list = new JList<BoneShell>(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		}
	}

	public static class Pairing {
		BoneShell importBone;
		BoneShell currentBone;

		public Pairing(final BoneShell importBone, final BoneShell currentBone) {
			this.importBone = importBone;
			this.currentBone = currentBone;
		}

		@Override
		public String toString() {
			return importBone.toString() + " ---> " + currentBone.toString();
		}

		public BoneShell getImportBone() {
			return importBone;
		}

		public BoneShell getCurrentBone() {
			return currentBone;
		}
	}
}
