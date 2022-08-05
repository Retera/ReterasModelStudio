package com.hiveworkshop.rms.util.TwiTextEditor.table;

import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.function.Function;

public class TwiTableColorEditor<T> implements TableCellEditor {
	ColorChooserPopup popup;
	Vec3 colorV = new Vec3();
	Color color;
	private final Function<String, T> parseFunction;
	private final T defalutValue;
	private final JTextField textField;
	private final String allowedCharacters;

	private EventListenerList listenerList = new EventListenerList();
	private transient ChangeEvent changeEvent = null;

	public TwiTableColorEditor(Function<String, T> parseFunction, T defalutValue){
		popup = new ColorChooserPopup();
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
				updateColorByText();
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
		textField.setText(getFixedString(value));
		updateColorByText();
//		textField.setBackground(Color.red);
		textField.setBackground(color);
		textField.setForeground(getTextColor(color));
		Rectangle cellRect = table.getCellRect(row, column, true);

		SwingUtilities.invokeLater(() -> popup.getNewColor1(color, textField, c -> updateColorByColor(c), c -> updateColorByColor(c)));

//		System.out.println("returning textfield! value: " + value + ", "+ value.getClass().getSimpleName());
		return textField;
	}


	private Color getTextColor(Color bgColor) {
		float[] rgb = bgColor.getRGBColorComponents(null);
		double greyValue = 0.2990 * rgb[0] + 0.5870 * rgb[1] + 0.1140 * rgb[2];
		if (greyValue > .5) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}

	private String getFixedString(Object value) {
		return value.toString().replaceAll("[^" + allowedCharacters + "]*", "");
	}

	private static Vec3 parseVec3(String s){
		return Vec3.parseVec3(ValueParserUtil.getString(3,s));
	}

	private void updateColorByText(){
		colorV.set(parseVec3(textField.getText()));
		color = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), parseVec3(textField.getText()).toFloatArray(), 1.0f);
		textField.setBackground(color);
		textField.setForeground(getTextColor(color));
	}
	private void updateColorByColor(Color newColor){
		color = newColor;
		colorV.set(newColor.getComponents(null));
		textField.setText(getFixedString(colorV));
		textField.setBackground(color);
		textField.setForeground(getTextColor(color));
	}

	@Override
	public Object getCellEditorValue() {
		return parseFunction.apply(textField.getText());
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		int clickCountToStart = 1;
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
