package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.util.function.BiConsumer;

public class TableTextEditor extends DefaultCellEditor {
	String allowedCharacters = "-1234567890.eE, ";
	protected String preEditValue;
	JTextField textField;
	private BiConsumer<JTextField, Integer> editRenderingConsumer;
	public TableTextEditor(){
		super(new JTextField());
		textField = (JTextField) getComponent();
		this.editorComponent = textField;
		textField.addCaretListener(e -> onCaretUpdate(textField));

	}

	int col = 0;
	public void setCol(int col){
		this.col = col;
	}

	private void onCaretUpdate(JTextField compTextField) {
		if (editRenderingConsumer != null) {
			editRenderingConsumer.accept(compTextField, col);
		}
		String text = compTextField.getText();
		if (!text.matches("[" + allowedCharacters + "]*")) {
			String newText = text.replaceAll("[^" + allowedCharacters + "]*", "");
			SwingUtilities.invokeLater(() -> {
				applyFilteredText(compTextField, newText);
			});
		}
		if (text.matches("(.*\\.\\.+.*)")) {
			String newText = text.replaceAll("(\\.+)", ".");
			SwingUtilities.invokeLater(() -> {
				applyFilteredText(compTextField, newText);
			});
		}
	}

	public Object getCellEditorValue() {
		System.out.println("getCellEditorValue: " + textField.getText() + ", parsing text!");
		return textField.getText();
	}

	public String getEditorValue(){
		System.out.println("getEditorValue: " + textField.getText() + ", parsing text!");
		return textField.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
	                                             boolean isSelected,
	                                             int row, int column) {
		setCol(column);
		preEditValue = value.toString().replaceAll("[^" + allowedCharacters + "]*", "");
		textField.setText(value.toString().replaceAll("[^" + allowedCharacters + "]*", ""));

//		System.out.println("returning textfield! value: " + value + ", "+ value.getClass().getSimpleName());
		return textField;
	}

	private void applyFilteredText(JTextField compTextField, String newText) {
//		System.out.println("filtering text!");
		CaretListener listener = compTextField.getCaretListeners()[0];
		compTextField.removeCaretListener(listener);

		int carPos = compTextField.getCaretPosition();
		compTextField.setText(newText);

		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
		compTextField.setCaretPosition(newCarPos);
		compTextField.addCaretListener(listener);
	}
}
