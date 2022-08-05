package com.hiveworkshop.rms.util.TwiTextEditor.table;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;

public class TwiTableColorRenderer extends TwiTableDefaultRenderer {

	public TwiTableColorRenderer(Sequence sequence){
		super(sequence);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component tableCellRendererComponent =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBackground(null);
		setForeground(null);
		if (column == table.getColumnCount() - 1) {
			deleteButton(tableCellRendererComponent);
		} else if (column == 0 && value!=null) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			timeCell((int) value, isSelected, tableCellRendererComponent);
		} else {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			if (value instanceof String) {
				Color bgColor = getClampedColor((String) value);
				Color fgColor = getTextColor(bgColor);
				setBackground(bgColor);
				setForeground(fgColor);
			} else if (value instanceof Vec3){
//				Color bgColor = getClampedColor((String) value);
				Color bgColor = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(((Vec3)value).toFloatArray()), 1.0f);
				Color fgColor = getTextColor(bgColor);
				setBackground(bgColor);
				setForeground(fgColor);
			}
		}

		return tableCellRendererComponent;
	}


	private Color getClampedColor(String string) {
		Vec3 tempColor = Vec3.parseVec3(ValueParserUtil.getString(3, string).replaceAll("-", ""));
		return new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(tempColor.toFloatArray()), 1.0f);
	}

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < rowColor.length; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
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
}
