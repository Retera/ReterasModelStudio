package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ColorValuePanel extends ValuePanel<Vec3> {

	private static final Vec3 DEFAULT_COLOR = new Vec3(1, 1, 1);


	private final JColorChooser colorChooser;

	private final JPopupMenu chooseColor;

	private JButton staticColorButton;

	//	private Consumer<Vec3> valueSettingFunction;
	private Vec3 color;
	private Vec3 selectedColor;


	public ColorValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		super(title, undoActionListener, modelStructureChangeListener);

		colorChooser = new JColorChooser();
//		colorChooser.getSelectionModel().addChangeListener(e -> color = new Vec3(colorChooser.getColor().getComponents(null)));
		colorChooser.getSelectionModel().addChangeListener(e -> {
			selectedColor = new Vec3(colorChooser.getColor().getComponents(null));
//			System.out.println("color: " + color);
		});
		chooseColor = new JPopupMenu("Choose Color");
		chooseColor.add(colorChooser);
		addPopupListener();
		color = new Vec3(DEFAULT_COLOR);
		selectedColor = new Vec3(DEFAULT_COLOR);
		allowedCharacters += "\\{}, ";
		floatTrackTableModel.addExtraColumn("", "\uD83C\uDFA8", Integer.class);  // 🎨 \uD83C\uDFA8
		floatTrackTableModel.setValueClass(String.class);
//		keyframeTable.setModel(floatTrackTableModel);
//		keyframeTable.getTableHeader().
		keyframeTable.setDefaultRenderer(String.class, getCellRenderer());
		addColorChangeListeners();

		TableColumn column = keyframeTable.getColumnModel().getColumn(keyframeTable.getColumnCount() - 2);
		if (column != null) {
			int rowHeight = keyframeTable.getRowHeight();
			column.setMaxWidth(rowHeight);
			column.setPreferredWidth(rowHeight);
			column.setMinWidth(5);
		}
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

				if (column == 1) {
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
				}
				return tableCellRendererComponent;
			}
		};
	}

	private Color getClampedColor(String string) {
		Vec3 tempColor = parseValue(string);
		return new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(tempColor.toFloatArray()), 1.0f);
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

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < 3; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}

//	public void setKeyframeHelper(TimelineKeyNamer timelineKeyNamer) {
//		this.timelineKeyNamer = timelineKeyNamer;
//	}

	protected void editFieldSpecial(JTextField textField, int column) {
		// sets the background color of the editor field to the color currently entered
		Color bgColor = getClampedColor(textField.getText());
		textField.setBackground(bgColor);
		textField.setForeground(getTextColor(bgColor));
	}

	@Override
	JComponent getStaticComponent() {
		staticColorButton = new JButton("Choose Color");
		staticColorButton.addActionListener(e -> ugg2());
		return staticColorButton;
	}

	@Override
	void reloadStaticValue(Vec3 color) {
		if (color != null) {
			this.color = color;
		}
		staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(this.color, 48, 48)));
	}


	@Override
	Vec3 getZeroValue() {
		return new Vec3(0, 0, 0);
	}

	@Override
	Vec3 parseValue(String valueString) {
		Vec3 vecValue = staticValue;
		String polishedString = valueString;
		if (valueString != null && valueString.matches("\\{? ?((\\d+(\\.\\d*)?)|(\\d*\\.\\d+))( ?, ?((\\d+(\\.\\d*)?)|(\\d*\\.\\d+)))* ?}")) {
			System.out.println("match");
			polishedString = valueString.replaceAll("[\\{} ]", "");
			System.out.println("polishedString pre: " + polishedString);
			String[] split = polishedString.split(",");
			int vecSize = split.length;
			if (vecSize < 3) {
				String addString = ",0";
				if (!split[vecSize - 1].equals("")) {
					addString = "," + split[vecSize - 1];
				}
				polishedString += addString;
				if (vecSize < 2) {
					polishedString += addString;
				}
			} else {
				if (split[2].equals("")) {
					polishedString += split[1];
				}
			}
		} else {
			System.out.println("nja");
			polishedString = valueString.replaceAll("[\\{} ]", "");
			System.out.println("polishedString pre: " + polishedString);
			String[] split = polishedString.split(",");
			if (split.length < 3) {
				polishedString = polishedString.replaceAll("\\.\\.", ".0,.");
				split = polishedString.split(",");
			}
			StringBuilder newS = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				if (i < split.length) {
					String s = split[i];
					if (s.equals("") || s.equals(".")) {
						split[i] = "0";
					} else if (s.matches("\\d+\\.")) {
						split[i] += "0";
					} else if (s.matches("\\d*\\.\\d+\\..*")) {
						split[i] = s.substring(0, s.indexOf(".", s.indexOf(".") + 1));
					}
					newS.append(split[i]);
				} else if (split.length != 0) {
					newS.append(split[split.length - 1]);
				} else {
					newS.append("0");
				}
				if (i < 2) {
					newS.append(",");
				}
			}
			polishedString = newS.toString();

		}
		System.out.println("polishedString: " + polishedString);
		vecValue = Vec3.parseVec3(polishedString);

		clamp(vecValue, 0.0f, 1.0f);
		return vecValue;
	}

	private void clamp(Vec3 vec3, float min, float max) {
		float[] vecFloats = vec3.toFloatArray();
		for (int i = 0; i < 3; i++) {
			vecFloats[i] = Math.max(min, Math.min(max, vecFloats[i]));
		}
		vec3.x = vecFloats[0];
		vec3.y = vecFloats[1];
		vec3.z = vecFloats[2];
	}

	private void addColorChangeListeners() {
		keyframeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("clickPoint: " + e.getPoint() + ", comp: " + e.getComponent());
				checkChangeColorPressed(e.getPoint(), KeyEvent.VK_ENTER);
			}
		});

		keyframeTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("CVP keyReleased! " + e.getKeyCode());
				System.out.println("rect: " + keyframeTable.getCellRect(keyframeTable.getSelectedRow(), keyframeTable.getSelectedColumn(), false));
				System.out.println("loc: " + keyframeTable.getCellRect(keyframeTable.getSelectedRow(), keyframeTable.getSelectedColumn(), false).getLocation());
//				System.out.println("loc: " + keyframeTable.getCellRect(keyframeTable.getSelectedRow(), keyframeTable.getSelectedColumn(), false));
				if (e.getKeyCode() == KeyEvent.VK_C) {
					System.out.println("C-Point: " + e.getComponent().getLocation() + ", comp: " + e.getComponent());
					Point compPoint = e.getComponent().getLocation();
					Point point = new Point(compPoint.y, compPoint.x);

					checkChangeColorPressed(point, e.getKeyCode());
				}
			}
		});
	}

	private void checkChangeColorPressed(Point point, int keyCode) {
		int colorChangeColumnIndex = keyframeTable.getColumnCount() - 2;
		if (keyCode == KeyEvent.VK_C || keyCode == KeyEvent.VK_ENTER && keyframeTable.getSelectedColumn() == colorChangeColumnIndex) {
			colorChooser.setColor(new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(((Vec3) animFlag.getValues().get(keyframeTable.getSelectedRow())).toFloatArray()), 1.0f));

//			keyframeTable.getCellRenderer(keyframeTable.getSelectedRow(),keyframeTable.getSelectedColumn());
//			keyframeTable.
			chooseColor.show(keyframeTable, point.x, point.y);
//			chooseColor.show(keyframeTable.getCellRenderer(keyframeTable.getSelectedRow(),keyframeTable.getSelectedColumn()), 0, 0);
//			changeRowColor(keyframeTable.getSelectedRow());

		}
	}

	private void changeRowColor(int row) {

	}

	private void changeColor() {
		if (animFlag == null) {
			color = new Vec3(selectedColor);
			if (valueSettingFunction != null) {
				valueSettingFunction.accept(color);
			}
			staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
		} else {
			System.out.println("selected row: " + keyframeTable.getSelectedRow());
			System.out.println("selected col: " + keyframeTable.getSelectedColumn());
			changeEntry(keyframeTable.getSelectedRow(), 1, "Value", selectedColor.toString());
		}

	}


	private void addPopupListener() {
		chooseColor.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//				System.out.println("new color: " + color);
				changeColor();
//				if (valueSettingFunction != null) {
//					valueSettingFunction.accept(color);
//				}
//				staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(color, 48, 48)));
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
