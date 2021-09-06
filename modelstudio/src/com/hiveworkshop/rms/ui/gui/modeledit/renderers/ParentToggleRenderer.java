package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;

import javax.swing.*;
import java.awt.*;

public class ParentToggleRenderer extends BoneShellListCellRenderer {
	JCheckBox toggleBox;

	boolean showParent;
	boolean showClass = true;

	public ParentToggleRenderer(JCheckBox toggleBox, EditableModel recModel,
	                            EditableModel donModel) {
		super(recModel, donModel);
		this.toggleBox = toggleBox;
	}

	public ParentToggleRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public ParentToggleRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		BoneShellListCellRenderer comp = (BoneShellListCellRenderer) super.getListCellRendererComponent(list, value, index, isSel, hasFoc);
		comp.setText(((BoneShell) value).toString(showClass, toggleBox.isSelected()));
		return this;
	}
}
