package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.util.TwiComboBoxModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class ShaderBox<T> extends JComboBox<T> {
	private ComponentEditorTextField comboBoxEditor;
	private final Consumer<T> consumer;
	private final TwiComboBoxModel<T> comboBoxModel;

	public ShaderBox(T[] options, Consumer<T> consumer) {
		comboBoxModel = new TwiComboBoxModel<>(options);
		setModel(comboBoxModel);
		this.consumer = consumer;
		setRenderer(getShaderBoxRenderer());
		setEditor(getShaderBoxEditor());
		setEditable(true);
		if (options.length > 0) {
			setSelectedItem(options[0]);
		}
		addItemListener(this::optionChanged);
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

//			consumer.accept((T) getSelectedItem());
			consumer.accept(comboBoxModel.getSelectedTyped());
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
