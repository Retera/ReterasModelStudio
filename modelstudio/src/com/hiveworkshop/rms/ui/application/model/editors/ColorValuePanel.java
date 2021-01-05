package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.ChangeFlagEntryAction;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.ui.application.actions.model.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

public class ColorValuePanel extends JPanel {
	private static final String NUMBERS = "1234567890";
	private static final String PUNKTATIONS = ".";
	private static final String NEGATIVE = "\\-";
	private static final Color LIGHT_GREEN = new Color(128, 255, 128);
	private static final Color LIGHT_YELLOW = new Color(255, 255, 128);
	//	private static final String DOUBLE_REGEX = "-?(\\d+,\\d+)*\\d+(.+\\d+)?";
	private static final String DOUBLE_REGEX = "[" + NUMBERS + PUNKTATIONS + NEGATIVE + "eE]*";
	private static final String DOUBLE_REPLACE_REGEX = "[^" + NUMBERS + PUNKTATIONS + NEGATIVE + "eE]";

	private static final Vec3 DEFAULT_COLOR = new Vec3(1, 1, 1);
	private final JRadioButton staticButton;
	private final JRadioButton dynamicButton;

	private final JLabel typeLabel;
	private final JButton makeStaticButton;
	private final JButton makeDynamicButton;

	private final JButton addButton;
	private final JTable keyframeTable;
	private final JColorChooser colorChooser;

	private final JComboBox<InterpolationType> interpTypeBox;
	private final JPopupMenu chooseColor;
	private final UndoActionListener undoActionListener;
	private final JButton staticColorButton;
	private final ModelStructureChangeListener modelStructureChangeListener;
	boolean doSave;
	String preEditValue;
	TimelineKeyNamer timelineKeyNamer;
	private FloatVecTrackTableModel floatTrackTableModel;
	private Consumer<Vec3> valueSettingFunction;
	private Vec3 color;
	private AnimFlag animFlag;
	private TimelineContainer timelineContainer;
	private String flagName;

	public ColorValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		timelineKeyNamer = null;

		colorChooser = new JColorChooser();
//		colorChooser.getSelectionModel().addChangeListener(e -> color = new Vec3(colorChooser.getColor().getComponents(null)));
		colorChooser.getSelectionModel().addChangeListener(e -> {
			color = new Vec3(colorChooser.getColor().getComponents(null));
//			System.out.println("color: " + color);
		});
		chooseColor = new JPopupMenu("Choose Color");
		chooseColor.add(colorChooser);
		addPopupListener();
		color = new Vec3(DEFAULT_COLOR);

		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout("hidemode 1"));
		final ButtonGroup staticDynamicGroup = new ButtonGroup();
		staticButton = new JRadioButton("Static");
		dynamicButton = new JRadioButton("Dynamic");
		staticColorButton = new JButton("Choose Color");
		staticColorButton.addActionListener(e -> ugg2());
		staticDynamicGroup.add(staticButton);
		staticDynamicGroup.add(dynamicButton);
//		add(staticButton);
//		add(staticColorButton, "wrap");
//		add(dynamicButton);


		JPanel spinInterpPanel = new JPanel(new MigLayout("wrap, hidemode 1", "", "[shrink 0]"));

		add(spinInterpPanel);

		typeLabel = new JLabel("");
		spinInterpPanel.add(typeLabel);
		spinInterpPanel.add(typeLabel, "wrap");

		interpTypeBox = new JComboBox<>(InterpolationType.values());
		interpTypeBox.addActionListener(e -> setInterpolationType());
		spinInterpPanel.add(interpTypeBox);

		spinInterpPanel.add(staticColorButton);

		JPanel dynStatPanel = new JPanel();
//		dynStatPanel.setOpaque(true);
//		dynStatPanel.setBackground(Color.magenta);
		makeStaticButton = new JButton("Make Static");
		makeStaticButton.addActionListener(e -> makeStatic());
		makeDynamicButton = new JButton("Make Dynamic");
		makeDynamicButton.addActionListener(e -> makeDynamic());
		dynStatPanel.add(makeStaticButton);
		dynStatPanel.add(makeDynamicButton);
		makeStaticButton.setVisible(true);
		makeDynamicButton.setVisible(false);
		add(dynStatPanel, "hidemode 1, wrap");


		floatTrackTableModel = new FloatVecTrackTableModel(null);
		keyframeTable = new JTable(floatTrackTableModel);
		keyframeTable.addPropertyChangeListener(getPropertyChangeListener());
		addDeleteListeners();
		keyframeTable.setDefaultRenderer(String.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(Integer.class, getCellRenderer());
		keyframeTable.setDefaultRenderer(Vec3.class, getCellRenderer());
		add(keyframeTable, "span 2, wrap, grow");


//		TableColumn valueColumn = keyframeTable.getColumn("Value");
//		valueColumn.set


		addButton = new JButton("add");
		addButton.addActionListener(e -> addEntry());
		add(addButton);

//		final ChangeListener l = e -> {
//            staticColorButton.setEnabled(staticButton.isSelected());
//            interpTypeBox.setEnabled(dynamicButton.isSelected());
//            keyframeTable.setVisible(dynamicButton.isSelected());
//        };
//		staticButton.addChangeListener(l);
//		dynamicButton.addChangeListener(l);
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

			if (evt.getSource() instanceof JTable) {
				JTable t = (JTable) evt.getSource();
//				JSpinner.NumberEditor;
			}


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
					if (!newValue.equals(preEditValue)) {
						changeEntry(row, col, keyframeTable.getColumnName(col), ((JTextField) comp).getText());
					}
					doSave = false;
				}

//				if (compTextField.getCaretListeners().length == 0) {
//					compTextField.addCaretListener(e -> {
//						String text = compTextField.getText();
//						if (!text.matches(DOUBLE_REGEX)) {
//							String newText = text.replaceAll(DOUBLE_REPLACE_REGEX, "");
//							SwingUtilities.invokeLater(() -> {
//								CaretListener listener = compTextField.getCaretListeners()[0];
//								compTextField.removeCaretListener(listener);
//								compTextField.setText(newText);
//								compTextField.addCaretListener(listener);
//							});
//						}
//					});
//				}
			}
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
				} else if (column == 1) {
//					System.out.println("rowColor: " + value);
//					String pureString = (value.toString()).replaceAll("[\\[\\](){},. ]", "");
//					Color bgColor = new Color(Integer.parseInt(pureString));
					float[] rowColor = ((Vec3) value).toFloatArray();

					clampColorVector(rowColor);

					Color bgColor = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), rowColor, 1.0f);

					tableCellRendererComponent.setBackground(bgColor);

					double greyValue = 0.2990 * rowColor[0] + 0.5870 * rowColor[1] + 0.1140 * rowColor[2];
//					System.out.println("grey value: " + (0.2990*rowColor[0]+ 0.5870*rowColor[1]+ 0.1140*rowColor[2]));
//					float maxColor = Math.max(rowColor[0], Math.max(rowColor[1], rowColor[2]));
					if (greyValue > .5) {
						tableCellRendererComponent.setForeground(Color.BLACK);
					} else {
						tableCellRendererComponent.setForeground(Color.WHITE);
					}
				} else if (column == 0) {
//					System.out.println("column 0");
					if (timelineKeyNamer != null) {
//						System.out.println("Should get color!");
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
						tableCellRendererComponent.setBackground(bgColor);
					}
				}
				return tableCellRendererComponent;
//				return this;
			}
		};
	}

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < 3; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}

	public void setKeyframeHelper(TimelineKeyNamer timelineKeyNamer) {
		this.timelineKeyNamer = timelineKeyNamer;
	}

	public void reloadNewValue(final Vec3 value, final AnimFlag valueTrack) {
		reloadNewValue(value, valueTrack, null, "");
	}

	public void reloadNewValue(final Vec3 color, final AnimFlag colorTrack, final TimelineContainer timelineContainer, final String flagName, Consumer<Vec3> valueSettingFunction) {
		this.valueSettingFunction = valueSettingFunction;
		reloadNewValue(color, colorTrack, timelineContainer, flagName);
	}

	public void reloadNewValue(final Vec3 color, final AnimFlag colorTrack, final TimelineContainer timelineContainer, final String flagName) {
		this.animFlag = colorTrack;
		this.timelineContainer = timelineContainer;
		this.flagName = flagName;

		if (colorTrack == null) {
			toggleStaticDynamicPanel(true);
			staticButton.setSelected(true);
		} else {
			toggleStaticDynamicPanel(false);
			dynamicButton.setSelected(true);
		}
		if (color != null) {
			this.color = color;
		}
		staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
		floatTrackTableModel.setTrack(colorTrack);
	}

	private void setTableModel() {
		if (floatTrackTableModel == null) {
			floatTrackTableModel = new FloatVecTrackTableModel(animFlag);

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

	private void setInterpolationType() {
		if (interpTypeBox.getSelectedItem() != null) {
			animFlag.setInterpType(InterpolationType.getType(interpTypeBox.getSelectedItem().toString()));
		}
	}

	private void makeStatic() {
		toggleStaticDynamicPanel(true);
		RemoveAnimFlagAction removeAnimFlagAction = new RemoveAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
		undoActionListener.pushAction(removeAnimFlagAction);
		removeAnimFlagAction.redo();
//		System.out.println("made static!");
	}

	private void makeDynamic() {
//		if (oldAnimFlag == null && timelineContainer != null && !flagName.equals("")) {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			toggleStaticDynamicPanel(false);
			AnimFlag newAnimFlag = new AnimFlag(flagName);
			newAnimFlag.addEntry(0, new Vec3(1, 1, 1));
			AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, newAnimFlag, modelStructureChangeListener);
			undoActionListener.pushAction(addAnimFlagAction);
			addAnimFlagAction.redo();
//		} else if (oldAnimFlag != null){
		} else if (animFlag != null) {
			toggleStaticDynamicPanel(false);
			AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
			undoActionListener.pushAction(addAnimFlagAction);
			addAnimFlagAction.redo();
		}
//		System.out.println("made dynamic!");
	}

	private void toggleStaticDynamicPanel(boolean isStatic) {
		boolean isDynamic = !isStatic;
		keyframeTable.setVisible(isDynamic);
		interpTypeBox.setVisible(isDynamic);
		makeStaticButton.setVisible(isDynamic);
		keyframeTable.getTableHeader().setVisible(isDynamic);
		addButton.setVisible(isDynamic);
		makeDynamicButton.setVisible(isStatic);
		staticButton.setVisible(isStatic);
		staticColorButton.setVisible(isStatic);
		if (isDynamic) {
			typeLabel.setText("Dynamic");
		} else {
			typeLabel.setText("Static");
		}
	}

	private void addEntry() {
//		System.out.println("addEntry");
//		System.out.println(animFlag + " " + timelineContainer + " " + flagName);
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
//			UndoAction undoAction = new AddAnimFlagAction(timelineContainer, flagName, modelStructureChangeListener);
//			undoActionListener.pushAction(undoAction);
//			undoAction.redo();

			animFlag = new AnimFlag(flagName);
			animFlag.addEntry(0, new Vec3(1, 1, 1));
			timelineContainer.add(animFlag);
		} else if (animFlag != null) {
			AnimFlag.Entry newEntry;
			AnimFlag.Entry lastEntry;
			if (animFlag.getTimes().isEmpty()) {
				if (animFlag.getInterpolationType().tangential()) {
					lastEntry = new AnimFlag.Entry(0, new Vec3(0, 0, 0), new Vec3(0, 0, 0), new Vec3(0, 0, 0));
				} else {
					lastEntry = new AnimFlag.Entry(0, new Vec3(0, 0, 0));
				}
				newEntry = new AnimFlag.Entry(lastEntry);
			} else {
				lastEntry = animFlag.getEntry(animFlag.getTimes().size() - 1);
				newEntry = new AnimFlag.Entry(lastEntry);
				newEntry.time++;
			}


			animFlag.addEntry(newEntry);

//			UndoAction undoAction = new AddFlagEntryAction(animFlag, newEntry, timelineContainer, modelStructureChangeListener);
//			undoActionListener.pushAction(undoAction);
//			undoAction.redo();

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

	private void changeEntry(int row, int col, String field, String val) {
//		String[] strings = val.split("\\.");
//		if(strings.length>2){
//
//		}
		AnimFlag.Entry entry = animFlag.getEntry(row);
		int orgTime = animFlag.getTimes().get(row);
//		float fValue = Float.parseFloat(val);
		int iValue = 0;
		Vec3 vecValue = (Vec3) entry.value;
		if (field.equals("Keyframe")) {
			iValue = Integer.parseInt(val.replaceAll("\\D", ""));

		} else {
			vecValue = Vec3.parseVec3(val);

		}

		switch (field) {
			case "Keyframe" -> {
				entry.time = iValue;
			}
			case "Value" -> entry.value = vecValue;
			case "InTan" -> entry.inTan = vecValue;
			case "OutTan" -> entry.outTan = vecValue;
		}

//		animFlag.setEntry(orgTime, entry);

		UndoAction undoAction = new ChangeFlagEntryAction(animFlag, entry, orgTime, timelineContainer, modelStructureChangeListener);
		undoActionListener.pushAction(undoAction);
		undoAction.redo();

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
//		AnimFlag.Entry entry = animFlag.getEntry(row);
//		animFlag.removeKeyframe(entry.time);

		revalidate();
		repaint();
	}

	private void addPopupListener() {
		chooseColor.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//				System.out.println("new color: " + color);
				if (valueSettingFunction != null) {
					valueSettingFunction.accept(color);
				}
				staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

	}

	private void ugg2() {

		colorChooser.setColor(new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(color.toFloatArray()), 1.0f));
		chooseColor.show(staticColorButton, 0, 0);
	}

}
