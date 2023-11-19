package com.hiveworkshop.rms.util.TwiTextEditor.table;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TwiTableDefaultRenderer extends DefaultTableCellRenderer {
	protected static final Color GREEN_OVERLAY = new Color(0, 255, 0, 128);
	protected static final Color YELLOW_OVERLAY = new Color(255, 255, 0, 128);
	protected static final Color LIGHT_GREEN = new Color(128, 255, 128);
	protected static final Color LIGHT_YELLOW = new Color(255, 255, 128);
	protected Sequence sequence;

	public TwiTableDefaultRenderer(Sequence sequence){
		this.sequence = sequence;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component tableCellRendererComponent =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (column == table.getColumnCount() - 1) {
			deleteButton(tableCellRendererComponent);
		} else if (column == 0 && value!=null) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			timeCell((int) value, isSelected, tableCellRendererComponent);
		} else {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
		}

		return tableCellRendererComponent;
	}

	protected void timeCell(int time, boolean isSelected, Component tableCellRendererComponent) {
		Color bgColor = null;
		setForeground(null);
		if (time == 0 || time == sequence.getLength()) {
			bgColor = GREEN_OVERLAY;
		} else if (0 < time && time < sequence.getLength()) {
			bgColor = YELLOW_OVERLAY;
		}
		this.createToolTip();
		this.setToolTipText(sequence.toString());
		if (isSelected && bgColor != null) {
			tableCellRendererComponent.setBackground(bgColor.darker().darker());
		} else {
			tableCellRendererComponent.setBackground(bgColor);
		}
	}
	protected void deleteButton(Component tableCellRendererComponent) {
		this.setHorizontalAlignment(SwingConstants.CENTER);
		tableCellRendererComponent.setBackground(Color.RED);
		tableCellRendererComponent.setForeground(Color.WHITE);
	}
}
