package com.hiveworkshop.rms.util.TwiTextEditor.table;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TwiTableDefaultRenderer extends DefaultTableCellRenderer {
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


//
//	private DefaultTableCellRenderer getCellRenderer() {
//		return new DefaultTableCellRenderer() {
//			@Override
//			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
////				System.out.println(row + ", " + column + " , value:" + value + " editor:");
////				System.out.println("correct? " + table.getSelectedColumn() + ", " + table.getSelectedRow());
//				setBackground(null);
//				setForeground(null);
//				final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//				this.setHorizontalAlignment(SwingConstants.RIGHT);
//				if (column == table.getColumnCount() - 1) {
//					deleteButton(tableCellRendererComponent);
//				} else if (column == 0) {
//					if (value!=null) {
//						timeCell((int) value, isSelected, tableCellRendererComponent);
//					}
//				} else {
//					if (valueRenderingConsumer != null && column == 1) {
//						valueRenderingConsumer.accept(tableCellRendererComponent, value);
//					}
////					valueCellRendering(tableCellRendererComponent, table, value, isSelected, hasFocus, row, column);
//				}
//				return tableCellRendererComponent;
////				return this;
//			}
//
//
//		};
//	}
	private void timeCell(int value, boolean isSelected, Component tableCellRendererComponent) {
		Color bgColor = Color.WHITE;
		setForeground(null);
		int time = value;
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
	private void deleteButton(Component tableCellRendererComponent) {
		this.setHorizontalAlignment(SwingConstants.CENTER);
		tableCellRendererComponent.setBackground(Color.RED);
		tableCellRendererComponent.setForeground(Color.WHITE);
	}
}
