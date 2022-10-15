
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetCamerasEdibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetCamerasVisibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowHideMultipleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeCamerasTopNode extends NodeThing<String> {

	public ComponentTreeCamerasTopNode(ModelHandler modelHandler) {
		super(modelHandler, "Cameras");
		updateState();
	}

	protected JLabel getItemLabel(String title) {
		return new JLabel(title);
	}
	public String updateState() {
		this.visible = modelView.isCamerasVisible();
		this.editable = modelView.isCamerasEditable();
		updateButtons();
		return item;
	}

	protected UndoAction getShowHideSingleAction(boolean visible) {
		return new SetCamerasVisibilityAction(visible, modelView, changeListener);
	}

	protected UndoAction getShowHideMultipleAction(boolean visible) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set cameras visibility", changeListener::nodesUpdated,
				new SetCamerasVisibilityAction(visible, modelView, null),
				new ShowHideMultipleAction(itemSet, visible, modelView, null));
	}

	protected UndoAction getSetEditableSingleAction(boolean editable) {
		return new SetCamerasEdibilityAction(editable, modelView, changeListener);
	}

	protected UndoAction getSetEditableMultipleAction(boolean editable) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set cameras editability", changeListener::nodesUpdated,
				new SetCamerasEdibilityAction(editable, modelView, null),
				new SetEditableMultipleAction(itemSet, editable, modelView, null));
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);

		itemLabel.setText("Cameras");
		treeRenderComponent.setBackground(color1);
		return treeRenderComponent;
	}

}
