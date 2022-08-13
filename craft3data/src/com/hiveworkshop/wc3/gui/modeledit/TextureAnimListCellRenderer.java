package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.TextureAnim;

public class TextureAnimListCellRenderer extends DefaultListCellRenderer {
	Font theFont = new Font("Arial", Font.BOLD, 32);
	private final EditableModel model;

	public TextureAnimListCellRenderer(final EditableModel model) {
		this.model = model;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		String name;
		if (value == null) {
			name = "(No value)";
		}
		else {
			name = "TextureAnim " + model.getTextureAnimId((TextureAnim) value);
		}
		super.getListCellRendererComponent(list, name, index, iss, chf);
		setFont(theFont);
		return this;
	}
}
