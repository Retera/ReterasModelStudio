package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.*;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.application.tools.IdObjectTypeChanger;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.TwiTextEditor.FlagPanel;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;


public abstract class ComponentIdObjectPanel<T extends IdObject> extends ComponentPanel<T> {
	protected TwiTextField nameField;
	protected JLabel parentName;
	protected JLabel bindPoseLabel;
	protected IdObjectChooser parentChooser;
	protected T idObject;
	protected JPanel topPanel;
	protected FlagPanel<Vec3> transPanel;
	protected FlagPanel<Quat> rotPanel;
	protected FlagPanel<Vec3> scalePanel;
	protected JLabel pivot;
	protected Vec3SpinnerArray pivotSpinner;

	protected JCheckBox billboardedBox;
	protected JCheckBox billboardLockXBox;
	protected JCheckBox billboardLockYBox;
	protected JCheckBox billboardLockZBox;
	protected JCheckBox dontInheritTranslationBox;
	protected JCheckBox dontInheritRotationBox;
	protected JCheckBox dontInheritScalingBox;

	public ComponentIdObjectPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel, new MigLayout("fillx, gap 0", "[]5[]5[grow]", "[]"));

		parentChooser = new IdObjectChooser(model, true);

		nameField = new TwiTextField(24, null);
		nameField.setFont(new Font("Arial", Font.BOLD, 18));
		add(nameField, "");

		add(getDeleteButton(e -> removeNode()), "skip 1");
		add(getButton("Duplicate", e -> duplicateNode()), "split");
		add(getButton("Change Type", e -> IdObjectTypeChanger.changeNodeType(idObject, modelHandler)), "wrap");

		add(new JLabel("Parent: "), "split, spanx 2");
		parentName = new JLabel("Parent");
		add(parentName);
		add(getButton("change", e -> chooseParent()), "wrap");
		bindPoseLabel = new JLabel("BP:{1111111111111111111111111111111111111111111111111111}");
//		add(bindPoseLabel, "spanx");


		pivotSpinner = new Vec3SpinnerArray().setVec3Consumer(this::setPivot);
		add(new JLabel("pivot: "), "split, spanx 2");
		add(pivotSpinner.spinnerPanel(), "wrap");

		add(getBillboardedPanel(), "spanx, wrap");
		add(getInheritingPanel(), "spanx, wrap");

		topPanel = new JPanel(new MigLayout("fill, ins 0", "[]5[]5[grow]"));
		add(topPanel, "spanx, growx, wrap");

		transPanel = new FlagPanel<>(MdlUtils.TOKEN_TRANSLATION, this::parseVec3, new Vec3(0,0,0), modelHandler);
		scalePanel = new FlagPanel<>(MdlUtils.TOKEN_SCALING, this::parseVec3, new Vec3(1,1,1), modelHandler);
		rotPanel = new FlagPanel<>(MdlUtils.TOKEN_ROTATION, this::parseQuat, new Quat(0,0,0, 1), modelHandler);

		add(transPanel, "spanx, growx, wrap");
		add(scalePanel, "spanx, growx, wrap");
		add(rotPanel, "spanx, growx, wrap");
	}

	@Override
	public ComponentPanel<T> setSelectedItem(T itemToSelect) {
		idObject = itemToSelect;
		nameField.setText(idObject.getName());
		nameField.setTextConsumer(s -> changeName(s, itemToSelect));
		pivotSpinner.setValues(idObject.getPivotPoint());

		IdObject parent = idObject.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}
		if (itemToSelect.getBindPoseM4() != null) {
			bindPoseLabel.setText("BP: " + Arrays.toString(itemToSelect.getBindPoseM4().getBindPose()));
		} else {
			bindPoseLabel.setText("BP: null");
		}

		transPanel.update(idObject, (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_TRANSLATION), new Vec3(0, 0, 0));
		scalePanel.update(idObject, (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_SCALING), new Vec3(1, 1, 1));
		rotPanel.update(idObject, (QuatAnimFlag) idObject.find(MdlUtils.TOKEN_ROTATION), new Quat(0, 0, 0, 1));

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

		return this;
	}

	private Vec3 parseVec3(String s) {
		return Vec3.parseVec3(ValueParserUtil.getString(3,s));
	}
	private Quat parseQuat(String s) {
		return Quat.parseQuat(ValueParserUtil.getString(4,s));
	}

	public void updatePanels() {
	}

	private JPanel getBillboardedPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));

		billboardedBox    = getCheckBox("billboarded",    (b) -> idObject.setBillboarded(b),    () -> idObject.getBillboarded());
		billboardLockXBox = getCheckBox("billboardLockX", (b) -> idObject.setBillboardLockX(b), () -> idObject.getBillboardLockX());
		billboardLockYBox = getCheckBox("billboardLockY", (b) -> idObject.setBillboardLockY(b), () -> idObject.getBillboardLockY());
		billboardLockZBox = getCheckBox("billboardLockZ", (b) -> idObject.setBillboardLockZ(b), () -> idObject.getBillboardLockZ());

		panel.add(billboardedBox);
		panel.add(billboardLockXBox);
		panel.add(billboardLockYBox);
		panel.add(billboardLockZBox);
		return panel;
	}

	private JPanel getInheritingPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));

		dontInheritTranslationBox = getCheckBox("dontInheritTranslation", b -> idObject.setDontInheritTranslation(b), () -> idObject.getDontInheritTranslation());
		dontInheritRotationBox = getCheckBox("dontInheritRotation", b -> idObject.setDontInheritRotation(b), () -> idObject.getDontInheritRotation());
		dontInheritScalingBox = getCheckBox("dontInheritScaling", b -> idObject.setDontInheritScaling(b), () -> idObject.getDontInheritScaling());

		panel.add(dontInheritTranslationBox);
		panel.add(dontInheritRotationBox);
		panel.add(dontInheritScalingBox);
		return panel;
	}

	private JCheckBox getCheckBox(String text, Consumer<Boolean> booleanConsumer, Supplier<Boolean> orgState) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.addActionListener(e -> setThing(booleanConsumer, orgState, checkBox.isSelected(), text));
		return checkBox;
	}

	private void setThing(Consumer<Boolean> consumer, Supplier<Boolean> orgState, boolean b, String name) {
		if (idObject != null && orgState.get() != b) {
			undoManager.pushAction(new BoolAction(consumer, b, "set " + name + " to " + b, changeListener::nodesUpdated).redo());
		}
	}

	private void chooseParent() {
		IdObject newParent = parentChooser.chooseObject(idObject, this.getRootPane());
		if (idObject.getParent() != newParent) {
			undoManager.pushAction(new ParentChangeAction(idObject, newParent, changeListener).redo());
		}
		repaint();
	}

	private void changeName(String newName, IdObject idObject) {
		// When changing which node is being viewed the focus lost event
		// is sent after the new node has been loaded,
		// so to be able to apply an ongoing name change the focus listener needs
		// a reference to the node whose name were being edited
		if (!newName.equals("") && !newName.equals(idObject.getName())) {
			System.out.println("setting new name to: " + newName);
			undoManager.pushAction(new NameChangeAction(idObject, newName, changeListener).redo());
		}
	}

	private void setPivot(Vec3 newPivot) {
		if (!newPivot.equalLocs(idObject.getPivotPoint())) {
			undoManager.pushAction(new SetPivotAction(idObject, newPivot, changeListener).redo());
		}
	}

	private void removeNode() {
		undoManager.pushAction(new DeleteNodesAction(idObject, modelHandler.getModelView(), changeListener).redo());
	}
	private void duplicateNode() {
		undoManager.pushAction(new AddNodeAction(model, idObject.copy(), changeListener).redo());
	}
}
