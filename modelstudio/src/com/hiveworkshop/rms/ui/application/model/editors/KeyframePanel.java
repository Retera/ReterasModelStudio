package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelTextureThings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class KeyframePanel<T> extends JPanel {
	protected static final Color LIGHT_GREEN = new Color(128, 255, 128);
	protected static final Color LIGHT_YELLOW = new Color(255, 255, 128);
	protected String allowedCharacters = "-1234567890.eE";
	protected AnimFlag<T> animFlag;
	protected Sequence sequence;
//	protected AnimFlag<T> oldAnimFlag;
	protected boolean doSave;
	protected String preEditValue;

	BiConsumer<Sequence, Integer> addAction;
	BiConsumer<Sequence, Collection<Integer>> removeAction;
	BiConsumer<Integer, Entry<T>> changeAction;
	Function<String, T> parseFunction;

	BiConsumer<Component, Object> valueRenderingConsumer;
	BiConsumer<JTextField, Integer> editRenderingConsumer;

	TwiTablePane<T> tablePanel;

	protected Map<Integer, Integer> columnSizes = new HashMap<>();

	public KeyframePanel(EditableModel model,
	                     BiConsumer<Sequence, Integer> addAction,
	                     BiConsumer<Sequence, Collection<Integer>> removeAction,
	                     BiConsumer<Integer, Entry<T>> changeAction,
	                     Function<String, T> parseFunction) {
		super(new MigLayout("gap 0, ins 0, fill, hidemode 3", "[grow]", "[][]"));

		this.addAction = addAction;
		this.removeAction = removeAction;
		this.changeAction = changeAction;
		this.parseFunction = parseFunction;

		tablePanel = new TwiTablePane<>(parseFunction, changeAction);

//		tablePanel.getKeyframeTable().addPropertyChangeListener(getPropertyChangeListener2());

		tablePanel.setRowsDeleteListener(this::removeEntry);
		tablePanel.setAddListener(this::addEntry);

		tablePanel.setRenderer(Integer.class, getCellRenderer());
		tablePanel.setRenderer(Bitmap.class, ModelTextureThings.getTextureTableCellRenderer());
		tablePanel.setRenderer(String.class, getCellRenderer());
		tablePanel.setRenderer(Float.class, getCellRenderer());

		add(tablePanel, "growx, wrap");

		JButton addButton = new JButton("add");
		addButton.addActionListener(e -> addEntry(tablePanel.getRowCount() - 1));
		add(addButton, "wrap, hidemode 3");
	}

	public void updateFlag(AnimFlag<T> animFlag, Sequence sequence) {
		this.animFlag = animFlag;
		this.sequence = sequence;
		tablePanel.setSequence(animFlag, sequence);
		revalidate();
		repaint();
	}

	private void addEntry(int row) {
		if (addAction != null) {
			addAction.accept(sequence, row);
		}
	}

	public FloatTrackTableModel<T> getFloatTrackTableModel() {
		return tablePanel.getFloatTrackTableModel();
	}

//	private CaretListener getCaretListener(int col, JTextField compTextField) {
//		return e -> {
//			if (editRenderingConsumer != null) {
//				editRenderingConsumer.accept(compTextField, col);
//			}
//			String text = compTextField.getText();
//			if (!text.matches("[" + allowedCharacters + "]*")) {
//				String newText = text.replaceAll("[^" + allowedCharacters + "]*", "");
//				SwingUtilities.invokeLater(() -> {
//					applyFilteredText(compTextField, newText);
//				});
//			}
//			if (text.matches("(.*\\.\\.+.*)")) {
//				String newText = text.replaceAll("(\\.+)", ".");
//				SwingUtilities.invokeLater(() -> {
//					applyFilteredText(compTextField, newText);
//				});
//			}
//		};
//	}
//
//	private void applyFilteredText(JTextField compTextField, String newText) {
//		CaretListener listener = compTextField.getCaretListeners()[0];
//		compTextField.removeCaretListener(listener);
//
//		int carPos = compTextField.getCaretPosition();
//		compTextField.setText(newText);
//
//		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
//		compTextField.setCaretPosition(newCarPos);
//		compTextField.addCaretListener(listener);
//	}


	private DefaultTableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
//				System.out.println(row + ", " + column + " , value:" + value + " editor:");
//				System.out.println("correct? " + table.getSelectedColumn() + ", " + table.getSelectedRow());
				setBackground(null);
				setForeground(null);
				final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				this.setHorizontalAlignment(SwingConstants.RIGHT);
				if (column == table.getColumnCount() - 1) {
					this.setHorizontalAlignment(SwingConstants.CENTER);
					tableCellRendererComponent.setBackground(Color.RED);
					tableCellRendererComponent.setForeground(Color.WHITE);
				} else if (column == 0) {
					if (value!=null) {
						Color bgColor = Color.WHITE;
						int time = (int) value;
						if (time == 0 || time == sequence.getLength()) {
							bgColor = LIGHT_GREEN;
						} else if (0 < time && time < sequence.getLength()) {
							bgColor = LIGHT_YELLOW;
						}
						this.createToolTip();
						this.setToolTipText(sequence.toString());
						if (isSelected) {
							tableCellRendererComponent.setBackground(bgColor.darker().darker());
						} else {
							tableCellRendererComponent.setBackground(bgColor);
						}
					}
				} else {
					if (valueRenderingConsumer != null && column == 1) {
						valueRenderingConsumer.accept(tableCellRendererComponent, value);
					}
//					valueCellRendering(tableCellRendererComponent, table, value, isSelected, hasFocus, row, column);
				}
				return tableCellRendererComponent;
//				return this;
			}
		};
	}

	private void removeEntry(int... rows) {
		if (removeAction != null) {
			ArrayList<Integer> orgTimes = new ArrayList<>();
			for (int row : rows){
				orgTimes.add((int) tablePanel.getFloatTrackTableModel().getValueAt(row, 0));
			}
			removeAction.accept(sequence, orgTimes);
		}
	}

	private Integer parseTime(String val) {
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));

		return Integer.parseInt(intString);
	}

	protected void changeEntry(int row, String field, String val) {
		if (parseFunction != null && changeAction != null && animFlag != null && sequence != null) {
			int orgTime = (int) tablePanel.getFloatTrackTableModel().getValueAt(row, 0);
			Entry<T> newEntry = animFlag.getEntryAt(sequence, orgTime).deepCopy();


			switch (field) {
				case "Keyframe" -> newEntry.setTime(parseTime(val));
				case "Value" -> newEntry.setValue(parseFunction.apply(val));
				case "InTan" -> newEntry.setInTan(parseFunction.apply(val));
				case "OutTan" -> newEntry.setOutTan(parseFunction.apply(val));
			}

			changeAction.accept(orgTime, newEntry);
			tablePanel.setSelectNew(animFlag.getIndexOfTime(sequence, newEntry.time));
//			selectNewIndex = animFlag.getIndexOfTime(sequence, newEntry.time);
		}
	}

//	protected void changeEntry(int row, int col, T val) {
//		if (parseFunction != null && changeAction != null && animFlag != null && sequence != null) {
//			int orgTime = (int) tablePanel.getFloatTrackTableModel().getValueAt(row, 0);
//			Entry<T> newEntry = animFlag.getEntryAt(sequence, orgTime).deepCopy();
//
//
//			switch (tablePanel.getFloatTrackTableModel().getColumnName(col)) {
//				case "Keyframe" -> newEntry.setTime(parseTime(val));
//				case "Value" -> newEntry.setValue(val);
//				case "InTan" -> newEntry.setInTan(val);
//				case "OutTan" -> newEntry.setOutTan(val);
//			}
//
//			changeAction.accept(orgTime, newEntry);
//			tablePanel.setSelectNew(animFlag.getIndexOfTime(sequence, newEntry.time));
////			selectNewIndex = animFlag.getIndexOfTime(sequence, newEntry.time);
//		}
//	}

	public KeyframePanel<T> setColumnSize(int column, int size) {
		columnSizes.put(column, size);
		return this;
	}

	public KeyframePanel<T> addAllowedCharatcters(String chars) {
		allowedCharacters += chars;
		return this;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public int getTableRowHeight() {
		return tablePanel.getRowHeight();
	}

	public JTable getTable() {
		return tablePanel.getKeyframeTable();
	}

	public KeyframePanel<T> setValueRenderingConsumer(BiConsumer<Component, Object> valueRenderingConsumer) {
		this.valueRenderingConsumer = valueRenderingConsumer;
		return this;
	}

	public KeyframePanel<T> setEditRenderingConsumer(BiConsumer<JTextField, Integer> editRenderingConsumer) {
		this.editRenderingConsumer = editRenderingConsumer;
		return this;
	}
}
