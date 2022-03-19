package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TextureTableCellRenderer extends DefaultTableCellRenderer {
	EditableModel model;
	HashMap<Bitmap, ImageIcon> map = new HashMap<>();
	HashMap<Bitmap, Boolean> validImageMap = new HashMap<>();
	Font theFont = new Font("Arial", Font.PLAIN, 18);
	Color orgFgColor = getForeground();
	BufferedImage noImage;
	int imageSize = 20;

	public TextureTableCellRenderer(final EditableModel model) {
		this.model = model;
		noImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = noImage.getGraphics();
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, imageSize, imageSize);
		graphics.dispose();
	}

	@Override
//	public Component getTableCellRendererComponent(final JList list, final Object value, final int index, final boolean iss, final boolean chf) {
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean iss, final boolean chf, final int row, final int column) {
		System.out.println("painting bitmap: " + value);
		Bitmap bitmap = null;
		if(value instanceof Bitmap){
			bitmap = (Bitmap) value;
		}
		if(value instanceof Integer){
			bitmap = model.getTexture((Integer) value);
		}
		if(bitmap != null){
			final String name = ((Bitmap) bitmap).getName();
			Color fgColor = orgFgColor;
			ImageIcon myIcon = getImageIcon(bitmap);
			if (!validImageMap.get(bitmap)) {
				fgColor = Color.gray;
			}
			Component tableCellRendererComponent = super.getTableCellRendererComponent(table, name, iss, chf, row, column);
			setIcon(myIcon);
			setFont(theFont);
			setForeground(fgColor);
		}

		return this;
	}

	public Component getPaintedRect(Component component, Rectangle rectangle, Object value) {
		System.out.println("painting bitmap: " + value);
		Bitmap bitmap = null;
		if(value instanceof Bitmap){
			bitmap = (Bitmap) value;
		}
		if(value instanceof Integer){
			bitmap = model.getTexture((Integer) value);
		}
		if(bitmap != null){
			final String name = ((Bitmap) bitmap).getName();
			Color fgColor = orgFgColor;
			ImageIcon myIcon = getImageIcon(bitmap);
			if (!validImageMap.get(bitmap)) {
				fgColor = Color.gray;
			}
			setIcon(myIcon);
			setFont(theFont);
			setForeground(fgColor);

//			component.getGraphics().drawImage(myIcon.getImage(), rectangle.x, rectangle.y, rectangle.width, rectangle.height, 1,1, 20, 20, null);
			component.getGraphics().drawImage(myIcon.getImage(), rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
		}

		return component;
	}

	public ImageIcon getImageIcon(Bitmap bitmap) {
		return map.computeIfAbsent(bitmap, k -> getNewImageIcon(bitmap));
	}

	private ImageIcon getNewImageIcon(Bitmap bitmap) {
		BufferedImage bufferedImage = BLPHandler.getImage(bitmap, model.getWrappedDataSource());
		if (bufferedImage == null) {
//					System.out.println("could not load icon for \"" + name + "\"");
			bufferedImage = noImage;
			validImageMap.put(bitmap, false);
		} else {
			validImageMap.put(bitmap, true);
		}
		return new ImageIcon(bufferedImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
	}

//	public Component getTableCellRendererComponent1(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
////				System.out.println(row + ", " + column + " , value:" + value + " editor:");
////				System.out.println("correct? " + table.getSelectedColumn() + ", " + table.getSelectedRow());
//		setBackground(null);
//		setForeground(null);
////		final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
////		this.setHorizontalAlignment(SwingConstants.RIGHT);
////		if (column == table.getColumnCount() - 1) {
////			this.setHorizontalAlignment(SwingConstants.CENTER);
////			tableCellRendererComponent.setBackground(Color.RED);
////			tableCellRendererComponent.setForeground(Color.WHITE);
////		} else if (column == 0) {
////			if (value!=null) {
////				Color bgColor = Color.WHITE;
////				int time = (int) value;
////				if (time == 0 || time == sequence.getLength()) {
////					bgColor = LIGHT_GREEN;
////				} else if (0 < time && time < sequence.getLength()) {
////					bgColor = LIGHT_YELLOW;
////				}
////				this.createToolTip();
////				this.setToolTipText(sequence.toString());
////				if (isSelected) {
////					tableCellRendererComponent.setBackground(bgColor.darker().darker());
////				} else {
////					tableCellRendererComponent.setBackground(bgColor);
////				}
////			}
////		} else {
////			if (valueRenderingConsumer != null && column == 1) {
////				valueRenderingConsumer.accept(tableCellRendererComponent, value);
////			}
//////					valueCellRendering(tableCellRendererComponent, table, value, isSelected, hasFocus, row, column);
////		}
////		return tableCellRendererComponent;
//////				return this;
//	}
}
