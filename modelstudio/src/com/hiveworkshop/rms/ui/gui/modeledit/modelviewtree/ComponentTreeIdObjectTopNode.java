
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
import com.hiveworkshop.rms.editor.actions.selection.SetIdObjectsEdibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetIdObjectsVisibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowHideMultipleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeIdObjectTopNode extends NodeThing<String> {
	public ComponentTreeIdObjectTopNode(ModelHandler modelHandler) {
		super(modelHandler, "Nodes");
		updateState();
	}

	protected JLabel getItemLabel(String title) {
		return new JLabel(title);
	}
	public String updateState() {
		this.visible = modelView.isIdObjectsVisible();
		this.editable = modelView.isIdObjectsEditable();
		updateButtons();
		return item;
	}

	protected UndoAction getShowHideSingleAction(boolean visible) {
		return new SetIdObjectsVisibilityAction(visible, modelView, changeListener);
	}

	protected UndoAction getShowHideMultipleAction(boolean visible) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set nodes visibility", changeListener::nodesUpdated,
				new SetIdObjectsVisibilityAction(visible, modelView, null),
				new ShowHideMultipleAction(itemSet, visible, modelView, null));
	}

	protected UndoAction getSetEditableSingleAction(boolean editable) {
		return new SetIdObjectsEdibilityAction(editable, modelView, changeListener);
	}

	protected UndoAction getSetEditableMultipleAction(boolean editable) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set nodes editability", changeListener::nodesUpdated,
				new SetIdObjectsEdibilityAction(editable, modelView, null),
				new SetEditableMultipleAction(itemSet, editable, modelView, null));
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);

		itemLabel.setText("Nodes");
		treeRenderComponent.setBackground(color1);

		return treeRenderComponent;
	}
}
