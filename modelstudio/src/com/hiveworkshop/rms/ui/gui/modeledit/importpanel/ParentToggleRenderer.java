package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

import javax.swing.*;
import java.awt.*;

public class ParentToggleRenderer extends BoneShellListCellRenderer {
	JCheckBox toggleBox;

	boolean showParent;
	boolean showClass = true;

	public ParentToggleRenderer(final JCheckBox toggleBox, final ModelView recModelDisp,
	                            final ModelView donModelDisp) {
		super(recModelDisp, donModelDisp);
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
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean isSelected, final boolean chf) {
		final BoneShellListCellRenderer comp = (BoneShellListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, chf);
		comp.setText(((BoneShell) value).toString(showClass, toggleBox.isSelected()));
		return this;
	}
}
