package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TriCheckBox extends JCheckBox {
	boolean isIndeterminate = false;
	ImageIcon indetIcon;
	ImageIcon indetIconRoll;
	ImageIcon indetIconPress;

	public TriCheckBox(String text){
		super(text);
		setupIcons();


		addItemListener(e -> {
//			System.out.println("stateChanged: " + e);
			if(isIndeterminate){
				setIcon(null);
				setPressedIcon(null);
				setRolloverIcon(null);
				isIndeterminate = false;
			}
		});
	}

	private void setupIcons(){
		JCheckBox tempBox = new JCheckBox(getText());
		Icon tempIcon = UIManager.getIcon("CheckBox.icon");

		Color foreground = tempBox.getForeground();
		Color color = new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 180);

		indetIcon = getIndetIcon(tempIcon, color, tempBox);
		tempBox.getModel().setRollover(true);
		indetIconRoll = getIndetIcon(tempIcon, color, tempBox);
		tempBox.getModel().setPressed(true);
		indetIconPress = getIndetIcon(tempIcon, color, tempBox);

	}

	public ImageIcon getIndetIcon(Icon tempIcon, Color color, JCheckBox tempBox) {
		BufferedImage image = new BufferedImage(tempIcon.getIconWidth(), tempIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		tempIcon.paintIcon(tempBox, image.getGraphics(), 0,0);
		Graphics graphics = image.getGraphics();

		graphics.setColor(color);
		int edgeOffset = 4;
		graphics.fillRect(edgeOffset, edgeOffset, tempIcon.getIconWidth()-edgeOffset*2, tempIcon.getIconHeight()-edgeOffset*2);

		return new ImageIcon(image);
	}

	public TriCheckBox setIndeterminate(boolean indeterminate) {
		setSelected(false);
		isIndeterminate = indeterminate;
		setIcon(indetIcon);
		setPressedIcon(indetIconPress);
		setRolloverIcon(indetIconRoll);
		return this;
	}



	@Override
	public boolean isSelected() {
		return super.isSelected();
	}

}
