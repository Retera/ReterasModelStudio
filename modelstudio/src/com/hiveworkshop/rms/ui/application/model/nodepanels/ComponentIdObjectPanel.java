package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.actions.nodes.ParentChangeAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.QuatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.model.editors.Vec3ValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;
import java.util.function.Consumer;


public abstract class ComponentIdObjectPanel<T extends IdObject> extends ComponentPanel<T> {
	JLabel title;
	TwiTextField nameField;
	JLabel parentName;
	ParentChooser parentChooser;
	protected T idObject;
	protected JPanel topPanel;
	protected Vec3ValuePanel transPanel;
	protected Vec3ValuePanel scalePanel;
	protected QuatValuePanel rotPanel;
	protected JLabel pivot;

	JCheckBox billboardedBox;
	JCheckBox billboardLockXBox;
	JCheckBox billboardLockYBox;
	JCheckBox billboardLockZBox;
	JCheckBox dontInheritTranslationBox;
	JCheckBox dontInheritRotationBox;
	JCheckBox dontInheritScalingBox;


	public ComponentIdObjectPanel(ModelHandler modelHandler) {
		super(modelHandler);

		parentChooser = new ParentChooser(model);

//		setLayout(new MigLayout("fill, gap 0", "[]5[]5[grow]", "[][][][][][grow]"));
		setLayout(new MigLayout("fillx, gap 0", "[]5[]5[grow]", "[]"));
		title = new JLabel("Select a IdObject");
		add(title, "wrap");
		nameField = new TwiTextField(24, this::changeName);
		add(nameField, "");

		add(getDeleteButton(e -> removeNode()), "skip 1, wrap");

		add(new JLabel("Parent: "), "split, spanx 2");
		parentName = new JLabel("Parent");
		add(parentName);
		add(getButton("change", e -> chooseParent()), "wrap");

		pivot = new JLabel("(0.0,0.0,0.0)");
		add(new JLabel("pivot: "));
		add(pivot, "wrap");

		add(getBillboardedPanel(), "spanx, wrap");
		add(getInheritingPanel(), "spanx, wrap");

		topPanel = new JPanel(new MigLayout("fill, ins 0", "[]5[]5[grow]"));
		add(topPanel, "spanx, wrap");

		transPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_TRANSLATION);
		scalePanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_SCALING);
		rotPanel = new QuatValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION);
		add(transPanel, "spanx, growx, wrap");
		add(scalePanel, "spanx, growx, wrap");
		add(rotPanel, "spanx, growx, wrap");
	}

	@Override
	public void setSelectedItem(T itemToSelect) {
		idObject = itemToSelect;
		title.setText(idObject.getName());
		nameField.setText(idObject.getName());
		pivot.setText(idObject.getPivotPoint().toString());
		IdObject parent = idObject.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}

		transPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_TRANSLATION), idObject, MdlUtils.TOKEN_TRANSLATION, null);
		scalePanel.reloadNewValue(new Vec3(1, 1, 1), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_SCALING), idObject, MdlUtils.TOKEN_SCALING, null);
		rotPanel.reloadNewValue(new Quat(0, 0, 0, 1), (QuatAnimFlag) idObject.find(MdlUtils.TOKEN_ROTATION), idObject, MdlUtils.TOKEN_ROTATION, null);

		billboardedBox.setSelected(idObject.getBillboarded());
		billboardLockXBox.setSelected(idObject.getBillboardLockX());
		billboardLockYBox.setSelected(idObject.getBillboardLockY());
		billboardLockZBox.setSelected(idObject.getBillboardLockZ());
		dontInheritTranslationBox.setSelected(idObject.getDontInheritTranslation());
		dontInheritRotationBox.setSelected(idObject.getDontInheritRotation());
		dontInheritScalingBox.setSelected(idObject.getDontInheritScaling());

		updatePanels();
		revalidate();
		repaint();

	}

	public void updatePanels() {
	}

	private JPanel getBillboardedPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));

		billboardedBox = new JCheckBox("billboarded");
		billboardedBox.addActionListener(e -> setThing((b) -> idObject.setBillboarded(b), billboardedBox.isSelected(), ""));

		billboardLockXBox = new JCheckBox("billboardLockX");
		billboardLockXBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockX(b), billboardLockXBox.isSelected(), ""));

		billboardLockYBox = new JCheckBox("billboardLockY");
		billboardLockYBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockY(b), billboardLockYBox.isSelected(), ""));

		billboardLockZBox = new JCheckBox("billboardLockZ");
		billboardLockZBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockZ(b), billboardLockZBox.isSelected(), ""));

//		billboardedBox = getCheckbox("billboarded", (b) -> idObject.setBillboarded(b));
////		billboardedBox.addActionListener(e -> setThing((b) -> idObject.setBillboarded(b), billboardedBox.isSelected(), ""));
//
//		billboardLockXBox = getCheckbox("billboardLockX", (b) -> idObject.setBillboardLockX(b));
////		billboardLockXBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockX(b), billboardLockXBox.isSelected(), ""));
//
//		billboardLockYBox = getCheckbox("billboardLockY", (b) -> idObject.setBillboardLockY(b));
////		billboardLockYBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockY(b), billboardLockYBox.isSelected(), ""));
//
//		billboardLockZBox = getCheckbox("billboardLockZ", (b) -> idObject.setBillboardLockZ(b));
////		billboardLockZBox.addActionListener(e -> setThing((b) -> idObject.setBillboardLockZ(b), billboardLockZBox.isSelected(), ""));

		panel.add(billboardedBox);
		panel.add(billboardLockXBox);
		panel.add(billboardLockYBox);
		panel.add(billboardLockZBox);
		return panel;
	}

	private JPanel getInheritingPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));

//		Consumer<Boolean> booleanConsumer1 = (b) -> idObject.setDontInheritTranslation(b);
//		dontInheritTranslationBox = new JCheckBox("dontInheritTranslation");
//		dontInheritTranslationBox.addActionListener(e -> setThing((b) -> idObject.setDontInheritTranslation(b), dontInheritTranslationBox.isSelected()));
//
//
//		dontInheritRotationBox = new JCheckBox("dontInheritRotation");
//		dontInheritRotationBox.addActionListener(e -> setThing((b) -> idObject.setDontInheritRotation(b), dontInheritRotationBox.isSelected()));
//
//		dontInheritScalingBox = new JCheckBox("dontInheritScaling");
//		dontInheritScalingBox.addActionListener(e -> setThing((b) -> idObject.setDontInheritScaling(b), dontInheritScalingBox.isSelected()));


		dontInheritTranslationBox = getCheckbox("dontInheritTranslation", (b) -> idObject.setDontInheritTranslation(b));
//		dontInheritTranslationBox.addActionListener(e -> setThing((b) -> idObject.setDontInheritTranslation(b), dontInheritTranslationBox.isSelected()));


		dontInheritRotationBox = getCheckbox("dontInheritRotation", (b) -> idObject.setDontInheritRotation(b));

		dontInheritScalingBox = getCheckbox("dontInheritScaling", (b) -> idObject.setDontInheritScaling(b));
//		dontInheritScalingBox.addActionListener(e -> setThing((b) -> idObject.setDontInheritScaling(b), dontInheritScalingBox.isSelected()));

		panel.add(dontInheritTranslationBox);
		panel.add(dontInheritRotationBox);
		panel.add(dontInheritScalingBox);
		return panel;
	}

	private JCheckBox getCheckbox(String text, Consumer<Boolean> booleanConsumer) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.addActionListener(e -> setThing(booleanConsumer, checkBox.isSelected(), text));
		return checkBox;
	}

	private void setThing(Consumer<Boolean> consumer, boolean b, String name) {
		if (idObject != null) {
//			undoManager.pushAction(new ConsumerAction<>(consumer, b, !b, "set " + name + " to " + b).redo());
			consumer.accept(b);
			ModelStructureChangeListener.changeListener.nodesUpdated();
		}
	}

	private void chooseParent() {
		IdObject newParent = parentChooser.chooseParent(idObject, this.getRootPane());
		undoManager.pushAction(new ParentChangeAction(idObject, newParent, changeListener).redo());
		repaint();
	}

	private void changeName(String newName) {
		if (!newName.equals("")) {
			System.out.println("setting new name to: " + newName);
			undoManager.pushAction(new NameChangeAction(idObject, newName, changeListener).redo());
		}
	}

	private void removeNode() {
		undoManager.pushAction(new DeleteNodesAction(Collections.singleton(idObject), Collections.emptySet(), changeListener, model).redo());
	}
}
