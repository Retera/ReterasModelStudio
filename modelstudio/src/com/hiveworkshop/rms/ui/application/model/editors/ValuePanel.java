package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ValuePanel<T> extends JPanel {
	protected static final Color LIGHT_GREEN = new Color(128, 255, 128);
	protected static final Color LIGHT_YELLOW = new Color(255, 255, 128);
	protected final JLabel typeLabel;
	protected final JButton makeStaticButton;
	protected final JButton makeDynamicButton;
	protected final JButton addButton;
	//	protected final ComponentEditorJSpinner staticSpinner;
	protected final JComponent staticComponent;
	protected final JComboBox<InterpolationType> interpTypeBox;
	protected final UndoActionListener undoActionListener;
	protected final ModelStructureChangeListener modelStructureChangeListener;
	protected String allowedCharacters = "-1234567890.eE";
	protected JTable keyframeTable;
	protected FloatTrackTableModel floatTrackTableModel;
	protected T staticValue;
	protected TimelineContainer timelineContainer;
	protected String flagName;
	protected AnimFlag<T> animFlag;
	protected AnimFlag<T> oldAnimFlag;
	protected TimelineKeyNamer timelineKeyNamer;
	protected boolean doSave;
	protected String preEditValue;

	protected Map<Integer, Integer> columnSizes = new HashMap<>();

	protected int selectNewIndex = -1;

	protected Consumer<T> valueSettingFunction;

	JScrollPane tableScrollPane = new JScrollPane();
	JPanel tablePanel;

	public ValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		this(title, Double.MAX_VALUE, -Double.MAX_VALUE, undoActionListener, modelStructureChangeListener);
	}

	public ValuePanel(final String title, double maxValue, double minValue, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		//		System.out.println("New FloatValuePanel!");
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		setFocusable(true);
		timelineKeyNamer = null;
		animFlag = null;
		timelineContainer = null;
		flagName = "";

		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout("fill, hidemode 1", "[]", "[][][]0[][]"));


		JPanel dynStatPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[]"));

		typeLabel = new JLabel("");
		dynStatPanel.add(typeLabel, "al 0% 0%");

		makeStaticButton = new JButton("Make Static");
		makeStaticButton.addActionListener(e -> makeStatic());
		makeStaticButton.setVisible(true);
		dynStatPanel.add(makeStaticButton, "al 100% 0%");

		makeDynamicButton = new JButton("Make Dynamic");
		makeDynamicButton.addActionListener(e -> makeDynamic());
		makeDynamicButton.setVisible(false);
		dynStatPanel.add(makeDynamicButton, "al 100% 0%");

		add(dynStatPanel, "align center, growx, wrap");


		JPanel spinInterpPanel = new JPanel(new MigLayout("ins 0, gap 0, hidemode 3", "[]", "[]"));
		add(spinInterpPanel, "wrap");

		staticComponent = this.getStaticComponent();
		spinInterpPanel.add(staticComponent);

		interpTypeBox = new JComboBox<>(InterpolationType.values());
		interpTypeBox.addActionListener(e -> setInterpolationType());
		spinInterpPanel.add(interpTypeBox);

//		add(getTablePanel(), "growx, wrap");

		add(getTablePanel(), "spanx, growx, wrap");

		addButton = new JButton("add");
		addButton.addActionListener(e -> addEntry(keyframeTable.getRowCount() - 1));
		add(addButton, "wrap");
	}

	private JPanel getTablePanel() {
		tablePanel = new JPanel(new MigLayout("gap 0, ins 0, fill, hidemode 2", "[grow][][grow, 1%:50%:80%]", "[][]"));
		JPanel tablePanel2 = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[]"));

		keyframeTable = new JTable();
		keyframeTable.addPropertyChangeListener(getPropertyChangeListener());
		addDeleteListeners();

		String tableConstraints = "growx, wrap";
		tablePanel.add(keyframeTable.getTableHeader(), tableConstraints);
		setTableModel();
		columnSizes.put(-1, keyframeTable.getRowHeight());


		keyframeTable.setDefaultRenderer(Integer.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(String.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(Float.class, getCellRenderer());

		tablePanel2.add(keyframeTable, "growx");

		tableScrollPane = new JScrollPane(tablePanel2);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		tableScrollPane.setBorder(null);
		tableScrollPane.setMaximumSize(ScreenInfo.getSuitableSize(700, 300, 0.6));
		JScrollBar verticalScrollBar = tableScrollPane.getVerticalScrollBar();

		verticalScrollBar.setUnitIncrement(16);


		tablePanel.add(tableScrollPane, "growx");
		tablePanel.add(verticalScrollBar, "spany, growy, wrap");
		return tablePanel;
	}

	private void makeStatic() {
//		oldAnimFlag = animFlag;
		toggleStaticDynamicPanel(true);
		RemoveAnimFlagAction removeAnimFlagAction = new RemoveAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
		undoActionListener.pushAction(removeAnimFlagAction);
		removeAnimFlagAction.redo();
	}

	private void makeDynamic() {
//		if (oldAnimFlag == null && timelineContainer != null && !flagName.equals("")) {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			toggleStaticDynamicPanel(false);
			if (staticValue == null) {
				staticValue = getZeroValue();
			}
			AnimFlag<T> newAnimFlag = getNewAnimFlag();

			if (newAnimFlag != null) {
				newAnimFlag.addEntry(0, staticValue);
				AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, newAnimFlag, modelStructureChangeListener);
				undoActionListener.pushAction(addAnimFlagAction);
				addAnimFlagAction.redo();
			}
//		} else if (oldAnimFlag != null){
		} else if (animFlag != null) {
			toggleStaticDynamicPanel(false);
			AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
			undoActionListener.pushAction(addAnimFlagAction);
			addAnimFlagAction.redo();
		}
//		System.out.println("ftt: "+ floatTrackTableModel.getColumnCount());
//		keyframeTable.setModel(floatTrackTableModel);
//		System.out.println("kft_b: " + keyframeTable.getColumnCount());
//		System.out.println("kftM_b: " + keyframeTable.getModel().getColumnCount());
//		keyframeTable.revalidate();
//		System.out.println("kft_a: " + keyframeTable.getColumnCount());
//		System.out.println("kftM_a: " + keyframeTable.getModel().getColumnCount());
	}

	private AnimFlag<T> getNewAnimFlag() {
		if (staticValue instanceof Integer) {
			return (AnimFlag<T>) new IntAnimFlag(flagName);
		} else if (staticValue instanceof Float) {
			return (AnimFlag<T>) new FloatAnimFlag(flagName);
		} else if (staticValue instanceof Vec3) {
			return (AnimFlag<T>) new Vec3AnimFlag(flagName);
		} else if (staticValue instanceof Quat) {
			return (AnimFlag<T>) new QuatAnimFlag(flagName);
		} else {
			return null;
		}
	}

	private void toggleStaticDynamicPanel(boolean isStatic) {
		boolean isDynamic = !isStatic;

		tablePanel.setVisible(isDynamic);
		keyframeTable.setVisible(isDynamic);
		interpTypeBox.setVisible(isDynamic);
		makeStaticButton.setVisible(isDynamic && this.valueSettingFunction != null);
		keyframeTable.getTableHeader().setVisible(isDynamic);
		addButton.setVisible(isDynamic);

		makeDynamicButton.setVisible(isStatic);
		staticComponent.setVisible(isStatic);
		if (isDynamic) {
			typeLabel.setText("Dynamic");
		} else {
			typeLabel.setText("Static");
		}
	}

	private void addDeleteListeners() {
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

	private PropertyChangeListener getPropertyChangeListener() {
		return evt -> {
			Component comp = keyframeTable.getEditorComponent();
			if (comp instanceof JTextField) {
				int row = keyframeTable.getEditingRow();
				int col = keyframeTable.getEditingColumn();

				final JTextField compTextField = (JTextField) comp;
				if (comp.isValid()) {
					doSave = true;
					preEditValue = compTextField.getText();
				} else if (doSave) {
					String newValue = compTextField.getText();
					if (!newValue.equals(preEditValue) && !newValue.equals("")) {
						changeEntry(row, col, keyframeTable.getColumnName(col), ((JTextField) comp).getText());
					}
					doSave = false;
				}

				if (compTextField.getCaretListeners().length == 0) {
					compTextField.addCaretListener(getCaretListener(col, compTextField));
				}
			}
		};
	}

	private CaretListener getCaretListener(int col, JTextField compTextField) {
		return e -> {
			editFieldRendering(compTextField, col);
			String text = compTextField.getText();
			if (!text.matches("[" + allowedCharacters + "eE]*")) {
				String newText = text.replaceAll("[^" + allowedCharacters + "eE]*", "");
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
					if (timelineKeyNamer != null) {
						Color bgColor = Color.WHITE;
						int time = (int) value;
						TimelineKeyNamer.AnimationMarker animationMarker = timelineKeyNamer.getAnimationMarker(time);
						if (animationMarker != null && animationMarker.contains(time)) {
							if (animationMarker.isEndPoint(time)) {
								bgColor = LIGHT_GREEN;
							} else {
								bgColor = LIGHT_YELLOW;
							}
							this.createToolTip();
							this.setToolTipText(animationMarker.name);
						}
						if (isSelected) {
							tableCellRendererComponent.setBackground(bgColor.darker().darker());
						} else {
							tableCellRendererComponent.setBackground(bgColor);
						}
					}
				} else {
					valueCellRendering(tableCellRendererComponent, table, value, isSelected, hasFocus, row, column);
				}
				return tableCellRendererComponent;
//				return this;
			}
		};
	}

	public void setKeyframeHelper(TimelineKeyNamer timelineKeyNamer) {
		this.timelineKeyNamer = timelineKeyNamer;
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag) {
		reloadNewValue(value, animFlag, null, "");
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag, final TimelineContainer timelineContainer, final String flagName, Consumer<T> valueSettingFunction) {
		this.valueSettingFunction = valueSettingFunction;
		reloadNewValue(value, animFlag, timelineContainer, flagName);
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag, final TimelineContainer timelineContainer, final String flagName) {
		this.animFlag = animFlag;
		this.timelineContainer = timelineContainer;
		this.flagName = flagName;
		staticValue = value;

		if (animFlag == null) {
			toggleStaticDynamicPanel(true);
		} else {
			toggleStaticDynamicPanel(false);
//			List<InterpolationType> types =  animFlag.getForbiddenInterpolationTypes();
//			types.forEach(t -> interpTypeBox.remove());
			ActionListener actionListener = interpTypeBox.getActionListeners()[0];
			interpTypeBox.removeActionListener(actionListener);
			interpTypeBox.setSelectedItem(animFlag.getInterpolationType());
			interpTypeBox.addActionListener(actionListener);
		}

		reloadStaticValue(value);
		setTableModel();
		if (selectNewIndex != -1 && selectNewIndex < keyframeTable.getRowCount()) {
			keyframeTable.setRowSelectionInterval(selectNewIndex, selectNewIndex);
			selectNewIndex = -1;
		}
	}

	abstract JComponent getStaticComponent();

	abstract void reloadStaticValue(T value);

	private void setInterpolationType() {
		if (interpTypeBox.getSelectedItem() != null) {
			UndoAction undoAction = new ChangeInterpTypeAction(animFlag, InterpolationType.getType(interpTypeBox.getSelectedItem().toString()), modelStructureChangeListener);
			undoActionListener.pushAction(undoAction);
			undoAction.redo();

			setTableModel();
		}
	}

	private void setTableModel() {
		if (floatTrackTableModel == null) {
			floatTrackTableModel = new FloatTrackTableModel(animFlag);

			keyframeTable.setModel(floatTrackTableModel);
			keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}

		if (floatTrackTableModel != null) {
			floatTrackTableModel.setTrack(animFlag);

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

			tableScrollPane.getVerticalScrollBar().setVisible(keyframeTable.getRowCount() > 5);
		}
	}

	private void addEntry(int row) {
//		System.out.println("addEntry");
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			AnimFlag<T> flag = getNewAnimFlag();
			if (flag != null) {
				AnimFlag.Entry<T> entry = new AnimFlag.Entry<>(0, staticValue);
				flag.addEntry(entry);
//			UndoAction undoAction = new AddAnimFlagAction(timelineContainer, flagName, modelStructureChangeListener);
				UndoAction undoAction = new AddAnimFlagAction(timelineContainer, flag, modelStructureChangeListener);
				undoActionListener.pushAction(undoAction);
				undoAction.redo();
			}
		} else if (animFlag != null) {
			AnimFlag.Entry<T> newEntry;
			AnimFlag.Entry<T> lastEntry;
			T zeroValue = getZeroValue();
			if (staticValue == null) {
				staticValue = zeroValue;
			}
			if (animFlag.getTimes().isEmpty()) {
				if (animFlag.getInterpolationType().tangential()) {
					lastEntry = new AnimFlag.Entry<>(0, staticValue, zeroValue, zeroValue);
				} else {
					lastEntry = new AnimFlag.Entry<>(0, staticValue);
				}
				newEntry = new AnimFlag.Entry<>(lastEntry);
			} else {
//				lastEntry = animFlag.getEntry(animFlag.getTimes().size() - 1);
				lastEntry = animFlag.getEntry(row);
				newEntry = new AnimFlag.Entry<>(lastEntry);
				newEntry.time++;
			}

			UndoAction undoAction = new AddFlagEntryAction(animFlag, newEntry, timelineContainer, modelStructureChangeListener);
			undoActionListener.pushAction(undoAction);
			undoAction.redo();

		}
		setTableModel();

		revalidate();
		repaint();

	}

	// To let implementations change how the editfield is displayed
	protected void editFieldRendering(JTextField textField, int column) {
	}

	/**
	 * Lets implementations change how the fields of {@link #keyframeTable}, except delete and time, is displayed
	 *
	 * @param tableCellRendererComponent the render component for the current cell
	 * @param value                      the value of the cell
	 */
	protected void valueCellRendering(Component tableCellRendererComponent, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	}

	abstract T getZeroValue();

	abstract T parseValue(String valueString);

	private void checkDeletePressed() {
		int maxColumnIndex = keyframeTable.getColumnCount() - 1;
		if (keyframeTable.getSelectedColumn() == maxColumnIndex) {
			removeEntry(keyframeTable.getSelectedRow());
		}
	}

	private void removeEntry(int row) {
//		System.out.println("removeEntry");
		oldAnimFlag = animFlag;

		UndoAction undoAction = new RemoveFlagEntryAction(animFlag, row, timelineContainer, modelStructureChangeListener);
		undoActionListener.pushAction(undoAction);
		undoAction.redo();

		revalidate();
		repaint();
	}

	protected void changeEntry(int row, int col, String field, String val) {
//		System.out.println("changeEntry");
		AnimFlag.Entry<T> entry = animFlag.getEntry(row);
		int orgTime = animFlag.getTimes().get(row);
		T tValue = parseValue(val);
		System.out.println(val);
		System.out.println(tValue);
//		String intString = val.replaceAll("[^\\d.]", "");//.split("\\.")[0];
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));
		int iValue = Integer.parseInt(intString);

		switch (field) {
			case "Keyframe" -> entry.time = iValue;
			case "Value" -> entry.value = tValue;
			case "InTan" -> entry.inTan = tValue;
			case "OutTan" -> entry.outTan = tValue;
		}

		UndoAction undoAction = new ChangeFlagEntryAction(animFlag, entry, orgTime, timelineContainer, modelStructureChangeListener);
		undoActionListener.pushAction(undoAction);
		undoAction.redo();

//		revalidate();
//		repaint();

		selectNewIndex = animFlag.ceilIndex(entry.time);
	}

}
