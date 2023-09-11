package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.SetCollisionExtents;
import com.hiveworkshop.rms.editor.actions.nodes.SetCollisionShapeShapeAction;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;

public class ComponentCollisionPanel extends ComponentIdObjectPanel<CollisionShape> {
	TwiComboBox<MdlxCollisionShape.Type> typeBox;
	Vec3SpinnerArray v1SpinnerArray;
	Vec3SpinnerArray v2SpinnerArray;
	FloatEditorJSpinner boundsSpinner;

	public ComponentCollisionPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);

		typeBox = new TwiComboBox<>(MdlxCollisionShape.Type.values(), MdlxCollisionShape.Type.CYLINDER);
		typeBox.addOnSelectItemListener(this::setType);
		v1SpinnerArray = new Vec3SpinnerArray().setVec3Consumer(this::setV1);
		v2SpinnerArray = new Vec3SpinnerArray().setVec3Consumer(this::setV2);
		boundsSpinner = new FloatEditorJSpinner(0, 0, 1);
		boundsSpinner.setFloatEditingStoppedListener(this::setBoundRad);

		topPanel.add(typeBox, "wrap");
		topPanel.add(v1SpinnerArray.spinnerPanel(), "wrap");
		topPanel.add(v2SpinnerArray.spinnerPanel(), "wrap");
		topPanel.add(boundsSpinner, "wrap");
	}

	@Override
	public void updatePanels() {
		typeBox.selectOrFirst(idObject.getType());
		v1SpinnerArray.setValues(idObject.getVertex(0));
		Vec3 v2 = idObject.getVertex(1);
		if (v2 != null) {
			v2SpinnerArray.setValues(v2).setEnabled(true);
		} else {
			v2SpinnerArray.setValues(Vec3.ZERO).setEnabled(false);
		}
		boundsSpinner.reloadNewValue(idObject.getBoundsRadius())
				.setEnabled(idObject.getType() == MdlxCollisionShape.Type.CYLINDER
						|| idObject.getType() == MdlxCollisionShape.Type.SPHERE);
	}

	private void setType(MdlxCollisionShape.Type type) {
		if (type != idObject.getType()) {
			undoManager.pushAction(new SetCollisionShapeShapeAction(type, idObject, changeListener).redo());
		}
	}

	private void setV1(Vec3 v1) {
		if (!v1.equalLocs(idObject.getVertex(0))) {
			Vec3 v2 = idObject.getVertex(1);
			undoManager.pushAction(new SetCollisionExtents(idObject, idObject.getBoundsRadius(), v1, v2, changeListener).redo());
		}
	}

	private void setV2(Vec3 v2) {
		if (!v2.equalLocs(idObject.getVertex(1))) {
			Vec3 v1 = idObject.getVertex(0);
			undoManager.pushAction(new SetCollisionExtents(idObject, idObject.getBoundsRadius(), v1, v2, changeListener).redo());
		}
	}
	private void setBoundRad(float boundsRadius) {
		if (boundsRadius != idObject.getBoundsRadius() && boundsRadius>=0 && idObject.getBoundsRadius()>=0) {
			Vec3 v1 = idObject.getVertex(0);
			Vec3 v2 = idObject.getVertex(1);
			undoManager.pushAction(new SetCollisionExtents(idObject, boundsRadius, v1, v2, changeListener).redo());
		}
	}
}
