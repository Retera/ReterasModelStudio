package com.hiveworkshop.rms.util.TwiTextEditor.table;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.function.Function;

public class TwiTableCellEditor<T> implements TableCellEditor {
	private final Function<String, T> parseFunction;
	private final T defalutValue;
	private final JTextField textField;
	private final String allowedCharacters;

	private EventListenerList listenerList = new EventListenerList();
	private transient ChangeEvent changeEvent = null;

	public TwiTableCellEditor(Function<String, T> parseFunction, T defalutValue){
		this.parseFunction = parseFunction;
		this.defalutValue = defalutValue;
		textField = new JTextField();
		textField.addCaretListener(e -> onCaretUpdate(textField));
		if(defalutValue instanceof Integer || defalutValue instanceof Long){
			allowedCharacters = "-1234567890 ";
		} else {
			allowedCharacters = "-1234567890.eE, ";
		}
	}



	private void onCaretUpdate(JTextField compTextField) {
//		if (editRenderingConsumer != null) {
//			editRenderingConsumer.accept(compTextField, col);
//		}
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

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

//		preEditValue = value.toString().replaceAll("[^" + allowedCharacters + "]*", "");
		textField.setText(value.toString().replaceAll("[^" + allowedCharacters + "]*", ""));

//		System.out.println("returning textfield! value: " + value + ", "+ value.getClass().getSimpleName());
		return textField;
	}

	@Override
	public Object getCellEditorValue() {
		return parseFunction.apply(textField.getText());
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		int clickCountToStart = 2;
		if (anEvent instanceof MouseEvent) {
			return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
		}
		return true;
//		return false;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}

	protected void fireEditingCanceled() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
			}
		}
	}
	protected void fireEditingStopped() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
			}
		}
	}
}
