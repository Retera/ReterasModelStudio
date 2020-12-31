package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.ChangeFlagEntry;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.material.FloatTrackTableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

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
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	//	private final JPanel keyFrameHeaderHolder;
	boolean doSave;
	String preEditValue;
	private FloatTrackTableModel floatTrackTableModel;
	private AnimFlag animFlag;
	private TimelineContainer timelineContainer;
	private String flagName;

	public FloatValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		this(title, Double.MAX_VALUE, -Double.MAX_VALUE, undoActionListener, modelStructureChangeListener);
//		this(title, 1.0, 0.0);
	}

	public FloatValuePanel(final String title, double maxValue, double minValue, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
//		System.out.println("New FloatValuePanel!");
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
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
		addDeleteListeners();

		add(keyframeTable.getTableHeader(), "span 2, wrap, grow");


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

	private void addDeleteListeners() {
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
	}

	private PropertyChangeListener getPropertyChangeListener() {
		return evt -> {
//			System.out.println("~~~~~~~~~~~~~~\npropChanged:");
//			System.out.println("propName " + evt.getPropertyName());
//			System.out.println("evt.source" + evt.getSource());
//			System.out.println("OLDval " + evt.getOldValue());
//			System.out.println("NEWval " + evt.getNewValue());

//			if(evt.getSource() instanceof JTable){
//				JTable t = (JTable) evt.getSource();
//			}


			Component comp = keyframeTable.getEditorComponent();
			if (comp instanceof JTextField) {
				int row = keyframeTable.getEditingRow();
				int col = keyframeTable.getEditingColumn();
//				System.out.println(keyframeTable.getColumnName(col));
//				System.out.println("compValid: " + comp);
//				System.out.println("comp: " + comp.isValid());
				final JTextField compTextField = (JTextField) comp;
				if (comp.isValid()) {
//					System.out.println("valid");
					doSave = true;
					preEditValue = compTextField.getText();
//					System.out.println("doSave: " + doSave);
				} else if (doSave) {
//					System.out.println("save");
//					System.out.println(keyframeTable.getColumnName(col) + ": " + ((JTextField) comp).getText());
					String newValue = compTextField.getText();
					if (!newValue.equals(preEditValue)) {
//						System.out.println("doSave!");
						changeEntry(row, col, keyframeTable.getColumnName(col), ((JTextField) comp).getText());
					}
//					System.out.println("X: " + keyframeTable.getEditingRow() + " Y: " + keyframeTable.getEditingColumn());
					doSave = false;
//					System.out.println("doSave: " + doSave);
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

//			if (evt.getSource() instanceof JTable) {
//				System.out.println(((JTable) evt.getSource()).isValid());
//			}
		};
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

		if (animFlag == null) {
			staticButton.setSelected(true);
		} else {
			dynamicButton.setSelected(true);
		}
//		System.out.println("colums: " + keyframeTable.getModel().getColumnCount());
		staticSpinner.reloadNewValue(value);
		setTableModel();
	}

	private void setTableModel() {
		if (floatTrackTableModel == null) {
			floatTrackTableModel = new FloatTrackTableModel(animFlag);

			keyframeTable.setModel(floatTrackTableModel);
			keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}

		if (floatTrackTableModel != null) {
			floatTrackTableModel.setTrack(animFlag);
			TableColumn column = keyframeTable.getColumn("");
			if (column != null) {
				int rowHeight = keyframeTable.getRowHeight();
				column.setMaxWidth(rowHeight);
				column.setPreferredWidth(rowHeight);
				column.setMinWidth(5);
			}
		}
	}

	private void addAnim(TimelineContainer timelineContainer, String flagName) {
		animFlag = new AnimFlag(flagName);
		animFlag.addEntry(0, 1);
		timelineContainer.add(animFlag);
	}

	private void addEntry() {
//		System.out.println("addEntry");
//		System.out.println(animFlag + " " + timelineContainer + " " + flagName);
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			UndoAction undoAction = new AddAnimFlagAction(timelineContainer, flagName, modelStructureChangeListener);
			undoActionListener.pushAction(undoAction);
			undoAction.redo();
//			animFlag = new AnimFlag(flagName);
//			animFlag.addEntry(0, 1);
//			timelineContainer.add(animFlag);
		} else if (animFlag != null) {
			AnimFlag.Entry newEntry;
			AnimFlag.Entry lastEntry;
			if (animFlag.getTimes().isEmpty()) {
				if (animFlag.getInterpolationType().tangential()) {
					lastEntry = new AnimFlag.Entry(0, 0, 0, 0);
				} else {
					lastEntry = new AnimFlag.Entry(0, 0);
				}
				newEntry = new AnimFlag.Entry(lastEntry);
			} else {
				lastEntry = animFlag.getEntry(animFlag.getTimes().size() - 1);
				newEntry = new AnimFlag.Entry(lastEntry);
				newEntry.time++;
			}
			UndoAction undoAction = new AddFlagEntryAction(animFlag, newEntry, timelineContainer, modelStructureChangeListener);
			undoActionListener.pushAction(undoAction);
			undoAction.redo();
//			Integer time = lastEntry.time + 1;
//			Object value = AnimFlag.cloneValue(lastEntry.value);
//
//			if (animFlag.tans()) {
//				Object inTan = AnimFlag.cloneValue(lastEntry.inTan);
//				Object outTan = AnimFlag.cloneValue(lastEntry.outTan);
//				animFlag.addEntry(time, value, inTan, outTan);
//			} else {
//				animFlag.addEntry(time, value);
//			}
		}
		setTableModel();

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
		UndoAction undoAction = new RemoveFlagEntryAction(animFlag, row, timelineContainer, modelStructureChangeListener);
		undoActionListener.pushAction(undoAction);
		undoAction.redo();
//		animFlag.getTimes().remove(row);
//		animFlag.getValues().remove(row);
//
//		if (animFlag.tans()) {
//			animFlag.getInTans().remove(row);
//			animFlag.getOutTans().remove(row);
//		}
		revalidate();
		repaint();
	}

	private void changeEntry(int row, int col, String field, String val) {
//		String[] strings = val.split("\\.");
//		if(strings.length>2){
//
//		}
		AnimFlag.Entry entry = animFlag.getEntry(row);
		int orgTime = animFlag.getTimes().get(row);
		float fValue = Float.parseFloat(val);
		int iValue = Integer.parseInt(val.replaceAll("\\D", ""));
//		switch (field) {
//			case "Keyframe" -> {
//				animFlag.getTimes().set(row, iValue);
//				animFlag.sort();
//			}
//			case "Value" -> animFlag.getValues().set(row, fValue);
//			case "InTan" -> animFlag.getInTans().set(row, fValue);
//			case "OutTan" -> animFlag.getOutTans().set(row, fValue);
//		}
		switch (field) {
			case "Keyframe" -> {
				entry.time = iValue;
			}
			case "Value" -> entry.value = fValue;
			case "InTan" -> entry.inTan = fValue;
			case "OutTan" -> entry.outTan = fValue;
		}
		UndoAction undoAction = new ChangeFlagEntry(animFlag, entry, orgTime, timelineContainer, modelStructureChangeListener);
		undoActionListener.pushAction(undoAction);
		undoAction.redo();

//		System.out.println("changed nr " + row + ", " + col + " to: " + val);

		revalidate();
		repaint();
	}

}
