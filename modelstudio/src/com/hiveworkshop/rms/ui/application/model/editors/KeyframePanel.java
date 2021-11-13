package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class KeyframePanel<T> extends JPanel {
	protected static final Color LIGHT_GREEN = new Color(128, 255, 128);
	protected static final Color LIGHT_YELLOW = new Color(255, 255, 128);
	protected JTable keyframeTable;
	protected String allowedCharacters = "-1234567890.eE";
	protected FloatTrackTableModel<T> floatTrackTableModel;
	protected AnimFlag<T> animFlag;
	protected Sequence sequence;
//	protected AnimFlag<T> oldAnimFlag;
	protected boolean doSave;
	protected String preEditValue;
	protected TimelineKeyNamer timelineKeyNamer;

	BiConsumer<Sequence, Integer> addAction;
	BiConsumer<Sequence, Integer> removeAction;
	BiConsumer<Integer, Entry<T>> changeAction;
	Function<String, T> parseFunction;

	BiConsumer<Component, Object> valueRenderingConsumer;
	BiConsumer<JTextField, Integer> editRenderingConsumer;

	JScrollBar tableScrollBar;

	protected int selectNewIndex = -1;

	protected Map<Integer, Integer> columnSizes = new HashMap<>();

	public KeyframePanel(EditableModel model,
	                     BiConsumer<Sequence, Integer> addAction,
	                     BiConsumer<Sequence, Integer> removeAction,
	                     BiConsumer<Integer, Entry<T>> changeAction,
	                     Function<String, T> parseFunction) {
		super(new MigLayout("gap 0, ins 0, fill, hidemode 3", "[grow]", "[][]"));
		timelineKeyNamer = new TimelineKeyNamer(model);

		this.addAction = addAction;
		this.removeAction = removeAction;
		this.changeAction = changeAction;
		this.parseFunction = parseFunction;

		JPanel tablePanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[]"));
//		JPanel tableAndScrollPanel = new JPanel(new MigLayout("gap 0, ins 0, fill, hidemode 3", "[grow][][grow, 1%:50%:80%]", "[][]"));
//		JPanel tableAndScrollPanel = new JPanel(new MigLayout("gap 0, ins 0, fill, hidemode 3", "[grow 150][grow 70]", "[][]"));
		JPanel tableAndScrollPanel = new JPanel(new MigLayout("gap 0, ins 0, fill, hidemode 3", "[][]", "[][]"));

		keyframeTable = new JTable();
		keyframeTable.addPropertyChangeListener(getPropertyChangeListener());
		addInteractionListeners();

		JScrollPane tableScrollPane = new JScrollPane(tablePanel);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		tableScrollPane.setBorder(null);
		tableScrollPane.setMaximumSize(ScreenInfo.getSuitableSize(700, 300, 0.6));
		tableScrollBar = tableScrollPane.getVerticalScrollBar();
		tableScrollBar.setUnitIncrement(16);

		tableAndScrollPanel.add(keyframeTable.getTableHeader(), "growx, wrap");
		setTableModel();
		columnSizes.put(-1, keyframeTable.getRowHeight());


		keyframeTable.setDefaultRenderer(Integer.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(String.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(Float.class, getCellRenderer());

		tablePanel.add(keyframeTable, "growx");


		tableAndScrollPanel.add(tableScrollPane, "growx");
		tableAndScrollPanel.add(tableScrollBar, "left, spany, growy");
		add(tableAndScrollPanel, "growx, wrap");

		JButton addButton = new JButton("add");
		addButton.addActionListener(e -> addEntry(keyframeTable.getRowCount() - 1));
		add(addButton, "wrap, hidemode 3");
	}

	public void updateFlag(AnimFlag<T> animFlag, Sequence sequence) {
		this.animFlag = animFlag;
		this.sequence = sequence;
		timelineKeyNamer.update();
		setTableModel();
		if (selectNewIndex != -1 && selectNewIndex < keyframeTable.getRowCount()) {
			keyframeTable.setRowSelectionInterval(selectNewIndex, selectNewIndex);
			selectNewIndex = -1;
		}
		revalidate();
		repaint();
	}

	private void setTableModel() {
		if (floatTrackTableModel == null) {

			floatTrackTableModel = new FloatTrackTableModel<>(animFlag, sequence);

			keyframeTable.setModel(floatTrackTableModel);
			keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			revalidate();
		}

		if (floatTrackTableModel != null) {
			floatTrackTableModel.setTrack(animFlag, sequence);

			keyframeTable.setModel(floatTrackTableModel);
			keyframeTable.createDefaultColumnsFromModel();
			keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


			for (int i : columnSizes.keySet()) {
				int count = keyframeTable.getColumnCount();
				int columnIndex = (count + i) % count;
				TableColumn column = keyframeTable.getColumnModel().getColumn(columnIndex);
				if (column != null) {
					column.setMaxWidth(columnSizes.get(i));
					column.setPreferredWidth(columnSizes.get(i));
					column.setMinWidth(5);
				}
			}
			revalidate();

			tableScrollBar.setVisible(keyframeTable.getRowCount() > 5);
			revalidate();
		}
	}

	private PropertyChangeListener getPropertyChangeListener() {
		return evt -> {
			Component comp = keyframeTable.getEditorComponent();
			if (comp instanceof JTextField) {
				int row = keyframeTable.getEditingRow();
				int col = keyframeTable.getEditingColumn();

				final JTextField compTextField = (JTextField) comp;
				if (comp.isValid()) {
					doSave = true;
					preEditValue = compTextField.getText().replaceAll("[^" + allowedCharacters + "]*", "");
				} else if (doSave) {
					String newValue = compTextField.getText();
					if (!newValue.equals(preEditValue) && !newValue.equals("")) {
						changeEntry(row, keyframeTable.getColumnName(col), ((JTextField) comp).getText());
					}
					doSave = false;
				}

				if (compTextField.getCaretListeners().length == 0) {
					compTextField.addCaretListener(getCaretListener(col, compTextField));
				}
			}
		};
	}

	private void addInteractionListeners() {
		keyframeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				checkDeletePressed();
			}
		});
		keyframeTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("VP keyReleased! " + e.getKeyCode() + ", char: " + e.getKeyChar());
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					System.out.println("was enter");
					checkDeletePressed();
				}
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeEntry(keyframeTable.getSelectedRow());
				}
				if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
					addEntry(keyframeTable.getSelectedRow());
				}
			}
		});
	}

	private void addEntry(int row) {
//		System.out.println("addEntry");
		if (addAction != null) {
			addAction.accept(sequence, row);
//			setTableModel();
		}
	}

	public FloatTrackTableModel<T> getFloatTrackTableModel() {
		return floatTrackTableModel;
	}

	private CaretListener getCaretListener(int col, JTextField compTextField) {
		return e -> {
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
		};
	}

	private void applyFilteredText(JTextField compTextField, String newText) {
		CaretListener listener = compTextField.getCaretListeners()[0];
		compTextField.removeCaretListener(listener);
		int carPos = compTextField.getCaretPosition();
		compTextField.setText(newText);
		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
		compTextField.setCaretPosition(newCarPos);
		compTextField.addCaretListener(listener);
	}


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

	private void checkDeletePressed() {
		int maxColumnIndex = keyframeTable.getColumnCount() - 1;
		if (keyframeTable.getSelectedColumn() == maxColumnIndex) {
			removeEntry(keyframeTable.getSelectedRow());
		}
	}

	private void removeEntry(int row) {
//		System.out.println("removeEntry");
		if (removeAction != null) {
//			oldAnimFlag = animFlag;
			int orgTime = (int) floatTrackTableModel.getValueAt(row, 0);
			removeAction.accept(sequence, orgTime);
//			setTableModel();
		}
//
//		revalidate();
//		repaint();
	}

	private Integer parseTime(String val) {
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));

		return Integer.parseInt(intString);
	}

	protected void changeEntry(int row, String field, String val) {
		if (parseFunction != null && changeAction != null && animFlag != null && sequence != null) {
			int orgTime = (int) floatTrackTableModel.getValueAt(row, 0);
			Entry<T> newEntry = animFlag.getEntryAt(sequence, orgTime).deepCopy();


			switch (field) {
				case "Keyframe" -> newEntry.setTime(parseTime(val));
				case "Value" -> newEntry.setValue(parseFunction.apply(val));
				case "InTan" -> newEntry.setInTan(parseFunction.apply(val));
				case "OutTan" -> newEntry.setOutTan(parseFunction.apply(val));
			}

			changeAction.accept(orgTime, newEntry);
			selectNewIndex = animFlag.getIndexOfTime(sequence, newEntry.time);
			setTableModel();
		}
	}

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
		return keyframeTable.getRowHeight();
	}

	public JTable getTable() {
		return keyframeTable;
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
