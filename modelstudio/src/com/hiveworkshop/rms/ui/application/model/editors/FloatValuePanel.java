package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.model.material.FloatTrackTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.EventObject;

public class FloatValuePanel extends JPanel {
	private static final String NUMBERS = "1234567890";
	private static final String PUNKTATIONS = ".";
	private static final String NEGATIVE = "\\-";
	//	private static final String DOUBLE_REGEX = "-?(\\d+,\\d+)*\\d+(.+\\d+)?";
	private static final String DOUBLE_REGEX = "[" + NUMBERS + PUNKTATIONS + NEGATIVE + "eE]*";
	private static final String DOUBLE_REPLACE_REGEX = "[^" + NUMBERS + PUNKTATIONS + NEGATIVE + "eE]";
	private final JRadioButton staticButton;
	private final JRadioButton dynamicButton;
	private final JButton addButton;
	private final ComponentEditorJSpinner staticSpinner;
	private final JComboBox<InterpolationType> interpTypeBox;
	private final JTable keyframeTable;
	//	private final JPanel keyFrameHeaderHolder;
	boolean doSave;
	private FloatTrackTableModel floatTrackTableModel;
	private AnimFlag animFlag;
	private TimelineContainer timelineContainer;
	private String flagName;

	public FloatValuePanel(final String title) {
		this(title, Double.MAX_VALUE, -Double.MAX_VALUE);
//		this(title, 1.0, 0.0);
	}

	public FloatValuePanel(final String title, double maxValue, double minValue) {
		System.out.println("New FloatValuePanel!");
		animFlag = null;
		timelineContainer = null;
		flagName = "";

		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout());
		final ButtonGroup staticDynamicGroup = new ButtonGroup();
		staticButton = new JRadioButton("Static");
		dynamicButton = new JRadioButton("Dynamic");
		staticDynamicGroup.add(staticButton);
		staticDynamicGroup.add(dynamicButton);
		add(staticButton);
		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, minValue, maxValue, 0.01));
		// below: fix this stupid nonsense java bug that a spinner with infinite value
		// range takes infinite GUI space on the screen
		final JSpinner standinGuiSpinner = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());
		add(staticSpinner, "wrap");

		add(dynamicButton);

		interpTypeBox = new JComboBox<>(InterpolationType.values());
		add(interpTypeBox, "wrap");

		keyframeTable = new JTable();
		keyframeTable.addPropertyChangeListener(getPropertyChangeListener());
		keyframeTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				checkDeletePressed();
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});
		keyframeTable.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					checkDeletePressed();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
//		keyFrameHeaderHolder = new JPanel();
//		add(keyFrameHeaderHolder, "span 2, wrap, grow");
		add(keyframeTable.getTableHeader(), "span 2, wrap, grow");


//		System.out.println(keyframeTable.getTableHeader().getColumnModel().getColumn(2).getHeaderValue());

		keyframeTable.setDefaultRenderer(String.class, getCellRenderer());

		add(keyframeTable, "span 2, wrap, grow");

		addButton = new JButton("add");
		addButton.addActionListener(e -> addEntry());
		add(addButton);

		final ChangeListener l = e -> {
			staticSpinner.setEnabled(staticButton.isSelected());
			interpTypeBox.setEnabled(dynamicButton.isSelected());
			keyframeTable.setVisible(dynamicButton.isSelected());
			keyframeTable.getTableHeader().setVisible(dynamicButton.isSelected());
			addButton.setVisible(dynamicButton.isSelected());
		};
		staticButton.addChangeListener(l);
		dynamicButton.addChangeListener(l);
	}

	private PropertyChangeListener getPropertyChangeListener() {
		return evt -> {
			System.out.println("~~~~~~~~~~~~~~\npropChanged:");
			System.out.println("propName " + evt.getPropertyName());
			System.out.println("evt.source" + evt.getSource());
			System.out.println("OLDval " + evt.getOldValue());
			System.out.println("NEWval " + evt.getNewValue());

//			if(evt.getSource() instanceof JTable){
//				JTable t = (JTable) evt.getSource();
//			}


			Component comp = keyframeTable.getEditorComponent();
			if (comp instanceof JTextField) {
				int row = keyframeTable.getEditingRow();
				int col = keyframeTable.getEditingColumn();
				System.out.println(keyframeTable.getColumnName(col));
				System.out.println("compValid: " + comp);
				System.out.println("comp: " + comp.isValid());
				final JTextField compTextField = (JTextField) comp;
				if (comp.isValid()) {
					doSave = true;
					System.out.println("doSave: " + doSave);
				} else if (doSave) {
					System.out.println(keyframeTable.getColumnName(col) + ": " + ((JTextField) comp).getText());
					changeEntry(row, col, keyframeTable.getColumnName(col), ((JTextField) comp).getText());
					System.out.println("X: " + keyframeTable.getEditingRow() + " Y: " + keyframeTable.getEditingColumn());
					doSave = false;
					System.out.println("doSave: " + doSave);
				}

				if (compTextField.getCaretListeners().length == 0) {
					compTextField.addCaretListener(e -> {
						String text = compTextField.getText();
						if (!text.matches(DOUBLE_REGEX)) {
							String newText = text.replaceAll(DOUBLE_REPLACE_REGEX, "");
							SwingUtilities.invokeLater(() -> {
								CaretListener listener = compTextField.getCaretListeners()[0];
								compTextField.removeCaretListener(listener);
								compTextField.setText(newText);
								compTextField.addCaretListener(listener);
							});
						}
					});
				}
			}

			if (evt.getSource() instanceof JTable) {
				System.out.println(((JTable) evt.getSource()).isValid());
			}
		};
	}

	private DefaultTableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				System.out.println(row + ", " + column + " , value:" + value + " editor:");
				System.out.println("correct? " + table.getSelectedColumn() + ", " + table.getSelectedRow());
				setBackground(null);
				setForeground(null);
				final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if ("Delete".equals(value)) {
//					System.out.println("Delete?");
//					tableCellRendererComponent.setSize(10,10);
					tableCellRendererComponent.setBackground(Color.RED);
					tableCellRendererComponent.setForeground(Color.WHITE);
				} else if (column == table.getColumnCount() - 1) {
//					System.out.println("Delete?");
//					tableCellRendererComponent.setSize(1,1);
					this.setHorizontalAlignment(SwingConstants.CENTER);
					tableCellRendererComponent.setBackground(Color.RED);
					tableCellRendererComponent.setForeground(Color.WHITE);
				}
				return tableCellRendererComponent;
//				return this;
			}
		};
	}

	public void reloadNewValue(final float value, final AnimFlag valueTrack) {
		reloadNewValue(value, valueTrack, null, "");
	}

	public void reloadNewValue(final float value, final AnimFlag animFlag, final TimelineContainer timelineContainer, final String flagName) {
		this.animFlag = animFlag;
		this.timelineContainer = timelineContainer;
		this.flagName = flagName;

		TableColumn column = null;
		if (animFlag == null) {
			staticButton.setSelected(true);
//			if(timelineContainer != null && !flagName.equals("")){
//				ActionListener[] actionListeners = addButton.getActionListeners();
//				if(actionListeners != null){
//					for (ActionListener actionListener : actionListeners){
//						addButton.removeActionListener(actionListener);
//					}
//				}
//				addButton.addActionListener(e -> addAnim(timelineContainer, flagName));
//			}
		} else {
			if (floatTrackTableModel == null) {
				floatTrackTableModel = new FloatTrackTableModel(animFlag);
//				floatTrackTableModel.addTableModelListener(e -> ugg());
				keyframeTable.setModel(floatTrackTableModel);
				keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//				keyframeTable.sizeColumnsToFit(2);
//				keyframeTable.getTableHeader();
				column = keyframeTable.getColumn("");
//				column.setHeaderValue("");
				int rowHeight = keyframeTable.getRowHeight();
				column.setMaxWidth(rowHeight);
				System.out.println("rh: " + rowHeight);
				column.setPreferredWidth(rowHeight);
//				column.setWidth(1);
				column.setMinWidth(1);
//				keyframeTable.getColumn("Delete").setHeaderValue(" ");
//				System.out.println("column: " + keyframeTable.getColumn("Delete").getHeaderValue());
			}
			dynamicButton.setSelected(true);
//			ActionListener[] actionListeners = addButton.getActionListeners();
//			if(actionListeners != null){
//				for (ActionListener actionListener : actionListeners){
//					addButton.removeActionListener(actionListener);
//				}
//			}
//			addButton.addActionListener(e -> addEntry());

//			PropertyChangeListener[] propertyChangeListeners = keyframeTable.getPropertyChangeListeners();
//			if(actionListeners != null){
//				for (PropertyChangeListener propertyChangeListener : propertyChangeListeners){
//					keyframeTable.removePropertyChangeListener(propertyChangeListener);
//				}
//			}

		}
		System.out.println("colums: " + keyframeTable.getModel().getColumnCount());
		staticSpinner.reloadNewValue(value);
		if (floatTrackTableModel != null) {
			floatTrackTableModel.setTrack(animFlag);
			column = keyframeTable.getColumn("");
			if (column != null) {
				int rowHeight = keyframeTable.getRowHeight();
				column.setMaxWidth(rowHeight);
				column.setPreferredWidth(rowHeight);
//				column.setWidth(1);
				column.setMinWidth(1);
				System.out.println("rh1: " + rowHeight);
			}
//			keyframeTable.getColumn("Delete").setPreferredWidth(10);
		}
	}

	private void addAnim(TimelineContainer timelineContainer, String flagName) {
		animFlag = new AnimFlag(flagName);
		animFlag.addEntry(0, 1);
		timelineContainer.add(animFlag);
	}

	private void addEntry() {
		System.out.println("addEntry");
		System.out.println(animFlag + " " + timelineContainer + " " + flagName);
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			animFlag = new AnimFlag(flagName);
			animFlag.addEntry(0, 1);
			timelineContainer.add(animFlag);
		} else if (animFlag != null) {
			AnimFlag.Entry lastEntry = animFlag.getEntry(animFlag.getTimes().size() - 1);

			Integer time = lastEntry.time + 1;
			Object value = AnimFlag.cloneValue(lastEntry.value);

			if (animFlag.tans()) {
				Object inTan = AnimFlag.cloneValue(lastEntry.inTan);
				Object outTan = AnimFlag.cloneValue(lastEntry.outTan);
				animFlag.addEntry(time, value, inTan, outTan);
			} else {
				animFlag.addEntry(time, value);
			}
		}
//		floatTrackTableModel.setTrack(animFlag);

		revalidate();
		repaint();

	}

	private void checkDeletePressed() {
		int maxCol = keyframeTable.getColumnCount() - 1;
		if (keyframeTable.getSelectedColumn() == maxCol) {
			removeEntry(keyframeTable.getSelectedRow());
		}
	}

	private void removeEntry(int row) {
		animFlag.getTimes().remove(row);
		animFlag.getValues().remove(row);

		if (animFlag.tans()) {
			animFlag.getInTans().remove(row);
			animFlag.getOutTans().remove(row);
		}
		revalidate();
		repaint();
	}

	private void changeEntry(int row, int col, String field, String val) {
//		String[] strings = val.split("\\.");
//		if(strings.length>2){
//
//		}
		float fValue = Float.parseFloat(val);
		int iValue = Integer.parseInt(val.replaceAll("\\D", ""));
		switch (field) {
			case "Keyframe" -> {
				animFlag.getTimes().set(row, iValue);
				animFlag.sort();
			}
			case "Value" -> animFlag.getValues().set(row, fValue);
			case "InTan" -> animFlag.getInTans().set(row, fValue);
			case "OutTan" -> animFlag.getOutTans().set(row, fValue);
		}

		System.out.println("changed nr " + row + ", " + col + " to: " + val);

		revalidate();
		repaint();
	}

	private void ugg(ListSelectionEvent event) {
		System.out.println("ugg " + event);
	}

	private void ugg() {
		System.out.println("ugg ");
	}

	private void setCellEditor(JTable table) {
		table.setCellEditor(new TableCellEditor() {
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
				System.out.println("value" + value);
				return null;
			}

			@Override
			public Object getCellEditorValue() {

				System.out.println("value");
				return null;
			}

			@Override
			public boolean isCellEditable(EventObject anEvent) {

				System.out.println("editable, " + anEvent.getSource());
				return true;
			}

			@Override
			public boolean shouldSelectCell(EventObject anEvent) {

				return true;
			}

			@Override
			public boolean stopCellEditing() {

				System.out.println("stop");
				return false;
			}

			@Override
			public void cancelCellEditing() {

				System.out.println("cancel");

			}

			@Override
			public void addCellEditorListener(CellEditorListener l) {

			}

			@Override
			public void removeCellEditorListener(CellEditorListener l) {

			}
		});
	}

	private void newFilter(TableRowSorter<FloatTrackTableModel> sorter) {
		RowFilter<FloatTrackTableModel, Object> rf = null;
		//If current expression doesn't parse, don't update.
		try {
			System.out.println("filter!");
			rf = RowFilter.regexFilter("\\d+", 0);
		} catch (java.util.regex.PatternSyntaxException e) {
			System.out.println("no filter");
			return;
		}
		sorter.setRowFilter(rf);
	}
}
