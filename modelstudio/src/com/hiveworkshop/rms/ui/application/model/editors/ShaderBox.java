package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class ShaderBox<T> extends JComboBox<T> {
	private ComponentEditorTextField comboBoxEditor;
	private Consumer<T> consumer;

	public ShaderBox(T[] options, Consumer<T> consumer) {
		super(options);
		this.consumer = consumer;
		setRenderer(ShaderBoxRenderer());
		setEditor(ShaderBoxEditor());
		setEditable(true);
		if (options != null && options.length > 0) {
			setSelectedItem(options[0]);
		}
		addItemListener(e -> optionChanged(e));
	}

	@Override
	public void setSelectedItem(Object anObject) {
		super.setSelectedItem(anObject);
		if (comboBoxEditor != null) {
			comboBoxEditor.setColorToSaved();
		}
	}

	private void optionChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && consumer != null) {
			consumer.accept((T) getSelectedItem());
		}
	}

	private BasicComboBoxRenderer ShaderBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if ((getText() == null) || getText().isEmpty()) {
					g.setColor(Color.LIGHT_GRAY);
					g.drawString("<empty>", 0, (getHeight() + g.getFontMetrics().getMaxAscent()) / 2);
				}
			}
		};
	}

	private BasicComboBoxEditor ShaderBoxEditor() {
		return new BasicComboBoxEditor() {
			@Override
			protected JTextField createEditorComponent() {
				final ComponentEditorTextField editor = getEditor1();
				comboBoxEditor = editor;
				editor.setBorder(null);
				return editor;
			}


		};
	}

	private ComponentEditorTextField getEditor1() {
		return new ComponentEditorTextField("", 9) {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if ((getText() == null) || getText().isEmpty()) {
					g.setColor(Color.LIGHT_GRAY);
					g.drawString("<empty>", 0, (getHeight() + g.getFontMetrics().getMaxAscent()) / 2);
				}
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
