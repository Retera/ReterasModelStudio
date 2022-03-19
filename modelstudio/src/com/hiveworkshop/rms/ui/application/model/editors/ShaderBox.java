package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.util.TwiComboBox;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.function.Consumer;

public class ShaderBox<T> extends TwiComboBox<T> {
	private ComponentEditorTextField comboBoxEditor;

	public ShaderBox(T[] options, Consumer<T> consumer) {
		super(options);
		addOnSelectItemListener(consumer);
		setRenderer(getShaderBoxRenderer());
		setEditor(getShaderBoxEditor());
		setEditable(true);
		if (options.length > 0) {
			setSelectedItem(options[0]);
		}
	}

	@Override
	public void setSelectedItem(Object anObject) {
		super.setSelectedItem(anObject);
		if (comboBoxEditor != null) {
			comboBoxEditor.setColorToSaved();
		}
	}

	private void paintEmpty(String text, Graphics g){
		if (text == null || text.isEmpty()) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("<empty>", 0, (getHeight() + g.getFontMetrics().getMaxAscent()) / 2);
		}
	}

	private BasicComboBoxRenderer getShaderBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				paintEmpty(getText(), g);
			}
		};
	}

	private BasicComboBoxEditor getShaderBoxEditor() {
		return new BasicComboBoxEditor() {
			@Override
			protected JTextField createEditorComponent() {
				final ComponentEditorTextField editor = getComponentEditor();
				comboBoxEditor = editor;
				editor.setBorder(null);
				return editor;
			}


		};
	}

	private ComponentEditorTextField getComponentEditor() {
		return new ComponentEditorTextField("", 9) {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				paintEmpty(getText(), g);
			}

			@Override
			public void setText(final String s) {
				if (getText().equals(s)) {
					return;
				}
				super.setText(s);
			}

			@Override
			public void setBorder(final Border b) {
				if (!(b instanceof BasicComboBoxEditor.UIResource)) {
					super.setBorder(b);
				}
			}
		};
	}
}
