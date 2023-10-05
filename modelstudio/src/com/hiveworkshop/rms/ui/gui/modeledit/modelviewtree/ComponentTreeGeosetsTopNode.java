
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
import com.hiveworkshop.rms.editor.actions.selection.SetGeosetsEdibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetGeosetsVisibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowHideMultipleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeGeosetsTopNode extends NodeThing<String> {

	public ComponentTreeGeosetsTopNode(ModelHandler modelHandler) {
		super(modelHandler, "Geosets");
		updateState();
	}

	protected JLabel getItemLabel(String title) {
		return new JLabel(title);
	}
	public String updateState() {
		this.visible = modelView.isGeosetsVisible();
		this.editable = modelView.isGeosetsEditable();
//		System.out.println("[GeosetTopNode] updateState, vis: " + visible + ", ed: " + editable);
		updateButtons();
		return item;
	}

	protected UndoAction getShowHideSingleAction(boolean visible) {
		return new SetGeosetsVisibilityAction(visible, modelView, changeListener);
	}

	protected UndoAction getShowHideMultipleAction(boolean visible) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set geosets visibility", changeListener::nodesUpdated,
				new SetGeosetsVisibilityAction(visible, modelView, null),
				new ShowHideMultipleAction(itemSet, visible, modelView, null));
	}

	protected UndoAction getSetEditableSingleAction(boolean editable) {
		return new SetGeosetsEdibilityAction(editable, modelView, changeListener);
	}

	protected UndoAction getSetEditableMultipleAction(boolean editable) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new CompoundAction("Set geosets editability", changeListener::nodesUpdated,
				new SetGeosetsEdibilityAction(editable, modelView, null),
				new SetEditableMultipleAction(itemSet, editable, modelView, null));
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);
		treeRenderComponent.revalidate();

		itemLabel.setText("Geosets");
		treeRenderComponent.setBackground(color1);

		return treeRenderComponent;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComponentTreeGeosetsTopNode) {
			if (this == obj) {
				return true;
			}
//			System.out.println("ComponentTreeGeosetsTopNode equals " + this.getClass().getSimpleName() + " " + this.getClass().isInstance(obj));
			return this.getClass().isInstance(obj)
					&& this.modelHandler == ((ComponentTreeGeosetsTopNode) obj).modelHandler
					&& this.item.equals(((ComponentTreeGeosetsTopNode) obj).item);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + this.getClass().hashCode();
		result = (prime * result) + modelHandler.hashCode();
		result = (prime * result) + item.hashCode();
		return result;
	}
}
