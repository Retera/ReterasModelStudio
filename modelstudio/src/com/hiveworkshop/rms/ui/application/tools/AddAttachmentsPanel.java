package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetParentAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddAttachmentsPanel extends JPanel {
	private final BoneChooser boneChooser;
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private final Set<FutureAttachment> attachments = new HashSet<>();
	private final JPanel attachmentListPanel;
	private final ModelHandler modelHandler;

	private Runnable onFinished;

	public AddAttachmentsPanel(ModelHandler modelHandler){
		super(new MigLayout("","",""));
		this.modelHandler = modelHandler;
		boneChooser = new BoneChooser(modelHandler.getModel());
		attachmentListPanel = new JPanel(new MigLayout("fillx, ins 0, wrap 1, top"));
		addRow();
		JPanel innerPanel = new JPanel(new MigLayout("fill, ins 0, wrap 1, top", "[]", "[][][grow]"));
		innerPanel.add(attachmentListPanel, "top, growx");
		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> addRow());
		innerPanel.add(addButton, "top");

		JScrollPane comp = new JScrollPane(innerPanel);
		comp.setPreferredSize(new Dimension(400, 400));
		comp.getVerticalScrollBar().setUnitIncrement(16);
		add(comp, "span 2, growx, growy, wrap");

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(e -> onDone());
		add(doneButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> finnish());
		add(cancelButton);
	}

	public AddAttachmentsPanel setOnFinished(Runnable onFinished) {
		this.onFinished = onFinished;
		return this;
	}

	private void onDone(){
		List<UndoAction> undoActions = new ArrayList<>();
		for (FutureAttachment fAttachment : attachments){
			Attachment attachment = new Attachment(fAttachment.getName());
			undoActions.add(new AddNodeAction(modelHandler.getModel(), attachment, null));
			if(fAttachment.getParentToBe() != null){
				attachment.setPivotPoint(fAttachment.getParentToBe().getPivotPoint());
				undoActions.add(new SetParentAction(attachment, fAttachment.getParentToBe(), null));
			}
		}
		CompoundAction action = new CompoundAction("Add Attachments", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);
		modelHandler.getUndoManager().pushAction(action.redo());
		finnish();
	}

	private void finnish(){
		attachmentListPanel.removeAll();
		attachments.clear();
		if(onFinished != null){
			onFinished.run();
		}
	}

	private void addRow(){
		FutureAttachment newAttachment = getNewAttatchment();
		attachments.add(newAttachment);
		attachmentListPanel.add(newAttachment.getPanel(), "growx, wrap");
		System.out.println("Add! attatchments: " + attachments.size());
		revalidate();
		repaint();
	}

	private FutureAttachment getNewAttatchment(){
		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		FutureAttachment attachment = new FutureAttachment(panel);
		TwiComboBox<String> stringTwiComboBox = new TwiComboBox<>(standardAttachments, "Chest Mount Right Alternate Ref");
		stringTwiComboBox.addOnSelectItemListener(attachment::setName);
		stringTwiComboBox.setSelectedItem(null);
		stringTwiComboBox.setSelectedIndex(0);
		panel.add(stringTwiComboBox, "growx, pushx");
		JButton chooseBone = getButton("None", attachment);
		panel.add(chooseBone);

		JButton removeButton = new JButton("X");
		removeButton.addActionListener(e -> removeRow(attachment));
		panel.add(removeButton, "");

		return attachment;
	}

	private void removeRow(FutureAttachment attachment){
		attachmentListPanel.remove(attachment.getPanel());
		attachments.remove(attachment);
		revalidate();
		repaint();
	}


	protected JButton getButton(String text, FutureAttachment attachment) {
		JButton button = new JButton(text);
		button.setIcon(iconHandler.getImageIcon(modelHandler.getModel()));
		button.addActionListener(e -> chooseRecBone(button, attachment));
		return button;
	}

	protected void chooseRecBone(JButton chooseBone, FutureAttachment attachment) {
		Bone chosenRecBone = boneChooser.chooseBone(attachment.getParentToBe(), this);
		if (chosenRecBone != null) {
			chooseBone.setText(chosenRecBone.getName());
		} else {
			chooseBone.setText("None");
		}
		attachment.setParentToBe(chosenRecBone);
		chooseBone.setIcon(iconHandler.getImageIcon(chosenRecBone, modelHandler.getModel()));
		repaint();
	}
	String[] standardAttachments = {
			"Chest Ref",
			"Chest Alternate Ref", //Medivh
			"Chest Mount Ref", //Gyro
//			"Chest Mount Left Ref", //Gyro
			"Chest Mount Left Ref", //Griffin
			"Chest Mount Rear Ref", //Griffin
			"Chest Mount Right Ref", //Griffin
			"Chest Mount Alternate Ref", //Destroyer
			"Foot Left Ref ",
			"Foot Left Rear Ref", //Griffin
			"Foot Right Ref ",
			"Foot Right Rear Ref", //Griffin
			"Foot Right Alternate Ref", // Deamon Hunter
			"Foot Left Alternate Ref", // Deamon Hunter
			"Hand Left Ref", //Fenix
//			"Hand Left Ref ",
//			"Hand Right Ref ",
			"Hand Right Ref", //Griffin
			"Hand Left Alternate Ref", //Medivh
			"Hand Right Alternate Ref", //Medivh
			"Head - Ref",
			"Head Ref", //Paladin
			"Head Alternate Ref", //Medivh
			"Head Alternate Ref ", // Deamon Hunter
			"Head Mount - Ref", //Gyro
			"Head Mount Ref", //Griffin
			"OverHead Alternate Ref", //Medivh
			"Origin Alternate Ref", //Medivh
			"Origin Ref ",
			"OverHead Ref ",
			"Sprite First Ref", //Gyro
			"Sprite Second Ref", //Gyro
			"Sprite Third Ref", //Gyro
			"Sprite Fourth Ref", //GrainWarehouse
			"Sprite Fifth Ref", //TownHall
//			"Weapon Ref",
			"Weapon Ref ", //Griffin
//			"Weapon Ref ",
			"Weapon Alternate Ref", //Medivh
			"BirthLink", //SacricicialAltar
			"Sprite RallyPoint Ref", //SacricicialAltar
			"Foot Left Rear Alternate Ref", //Destroyer
			"Foot Right Rear Alternate Ref", //Destroyer
			"Chest Mount Rear Alternate Ref", //Destroyer
			"Chest Mount Left Alternate Ref", //Destroyer
			"Chest Mount Right Alternate Ref", //Destroyer
//			"",
//			"",
			"OverHead Ref 01", //HumanTransportShip
	};

	String[] standardAttachments1 = {
			"BirthLink",
			"Chest",
			"Chest Alternate",
			"Chest Mount",
			"Chest Mount Alternate",
			"Chest Mount Left",
			"Chest Mount Left Alternate",
			"Chest Mount Rear",
			"Chest Mount Rear Alternate",
			"Chest Mount Right",
			"Chest Mount Right Alternate",
			"Foot Left",
			"Foot Left Alternate",
			"Foot Left Rear",
			"Foot Left Rear Alternate",
			"Foot Right",
			"Foot Right Alternate",
			"Foot Right Rear",
			"Foot Right Rear Alternate",
			"Hand Left",
			"Hand Left Alternate",
			"Hand Right",
			"Hand Right Alternate",
			"Head",
			"Head Alternate",
			"Head Mount",
			"Origin",
			"Origin Alternate",
			"OverHead",
			"OverHead Alternate",
			"Sprite First",
			"Sprite Second",
			"Sprite Third",
			"Sprite Fourth",
			"Sprite Fifth",
			"Sprite RallyPoint",
			"Weapon",
			"Weapon Alternate",
			"",
			"",
			"",
	};

	String[] standardAttachments2 = {
			"Chest",
			"Chest Mount",
			"Chest Mount Left",
			"Chest Mount Rear",
			"Chest Mount Right",
			"Foot Left",
			"Foot Left Rear",
			"Foot Right",
			"Foot Right Rear",
			"Hand Left",
			"Hand Right",
			"Head",
			"Head Mount",
			"Origin",
			"OverHead",
			"Weapon",
	};

	String[] standardAttachments3 = {
			"Chest",
			"Chest Mount",
			"Chest Mount Left",
			"Chest Mount Rear",
			"Chest Mount Right",
			"Foot Left",
			"Foot Left Rear",
			"Foot Right",
			"Foot Right Rear",
			"Hand Left",
			"Hand Right",
			"Head",
			"Head Mount",
			"Origin",
			"OverHead",
			"Weapon",

			"BirthLink",
			"Sprite First",
			"Sprite Second",
			"Sprite Third",
			"Sprite Fourth",
			"Sprite Fifth",
			"Sprite RallyPoint",
			"",
			"",
			"",
	};

	static class FutureAttachment{
		JPanel panel;
		String name;
		Bone parentToBe;
		FutureAttachment(JPanel panel){
			this.panel = panel;
		}

		public JPanel getPanel() {
			return panel;
		}

		public FutureAttachment setName(String name) {
			this.name = name;
			return this;
		}

		public String getName() {
			return name;
		}

		public FutureAttachment setParentToBe(Bone parentToBe) {
			this.parentToBe = parentToBe;
			return this;
		}

		public Bone getParentToBe() {
			return parentToBe;
		}
	}
}
