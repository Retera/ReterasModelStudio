package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.util.TwiComboBox;

import javax.swing.*;
import java.awt.*;

public class TableComboBoxEditor<T> extends DefaultCellEditor {
//	String allowedCharacters = "-1234567890.eE, ";
	protected String preEditValue;
	TwiComboBox<T> comboBox;
//	private BiConsumer<JTextField, Integer> editRenderingConsumer;
	public TableComboBoxEditor(TwiComboBox<T> comboBox){
		super(comboBox);
		this.comboBox = (TwiComboBox<T>) getComponent();
		this.editorComponent = this.comboBox;
//		textField.addCaretListener(e -> onCaretUpdate(textField));

	}

	int col = 0;
	public void setCol(int col){
		this.col = col;
	}

	int row = 0;
	public void setRow(int row){
		this.row = row;
	}


	public Object getCellEditorValue() {
//		System.out.println("getCellEditorValue: " + comboBox.getSelected() + ", parsing text!");
		return comboBox.getSelected();
	}

	public T getEditorValue(){
//		System.out.println("getEditorValue: " + comboBox.getSelected() + ", parsing text!");
		return comboBox.getSelected();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
	                                             boolean isSelected,
	                                             int row, int column) {
		setCol(column);
		setRow(row);
		preEditValue = value.toString();
		comboBox.setSelectedItem(value);

//		System.out.println("returning textfield! value: " + value + ", "+ value.getClass().getSimpleName());
		return comboBox;
	}
}
