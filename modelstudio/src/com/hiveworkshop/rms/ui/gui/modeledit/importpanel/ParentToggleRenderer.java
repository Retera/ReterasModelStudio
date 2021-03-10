package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

import javax.swing.*;
import java.awt.*;

public class ParentToggleRenderer extends BoneShellListCellRenderer {
	JCheckBox toggleBox;

	public ParentToggleRenderer(final JCheckBox toggleBox, final ModelView currentModelDisp,
	                            final ModelView importedModelDisp) {
		super(currentModelDisp, importedModelDisp);
		this.toggleBox = toggleBox;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		final BoneShellListCellRenderer comp = (BoneShellListCellRenderer) super.getListCellRendererComponent(list, value, index, iss, chf);
		comp.setText(((BoneShell) value).toString(true, toggleBox.isSelected()));
		return this;
	}
}
