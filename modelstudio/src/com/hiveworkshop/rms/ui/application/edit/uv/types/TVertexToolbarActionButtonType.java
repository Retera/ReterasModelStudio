package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorActivityDescriptor;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonType;

import javax.swing.*;

public abstract class TVertexToolbarActionButtonType implements ToolbarButtonType, TVertexEditorActivityDescriptor {
	private final ImageIcon imageIcon;
	private final String name;

	public TVertexToolbarActionButtonType(final ImageIcon imageIcon, final String name) {
		this.imageIcon = imageIcon;
		this.name = name;
	}

	@Override
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	@Override
	public String getName() {
		return name;
	}
}
