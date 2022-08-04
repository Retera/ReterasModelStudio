package com.hiveworkshop.rms.ui.util.colorchooser;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class ColorChooserPopup extends JColorChooser {
	private final ColorTrackerPanel colorTrackerPanel;


	private boolean mouseDown = false;
	private Color selectedColor;
	private Consumer<Color> updateConsumer;

	public ColorChooserPopup(){
		colorTrackerPanel = new ColorTrackerPanel(this::setColor);
		setPreviewPanel(colorTrackerPanel);

		getSelectionModel().addChangeListener(getChangeListener());
		MouseAdapter l = getMouseAdapter();

		addMouseListener(l);

		for (AbstractColorChooserPanel ugg : getChooserPanels()) {
			ugg.addMouseListener(l);
			for (int i = 0; i < ugg.getComponentCount(); i++) {
				Component component = ugg.getComponent(i);
				component.addMouseListener(l);
			}
		}
	}
	public Color getNewColor(Color oldColor, Component parent) {
		setColor(oldColor);
		colorTrackerPanel.setOldColor(oldColor);
		int option = JOptionPane.showConfirmDialog(parent, this, "Choose Background Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION && getColor() != null) {
			return getColor();
		}
		return oldColor;
	}
	public void getNewColor1(Color oldColor, Component parent, Consumer<Color> colorConsumer, Consumer<Color> updateConsumer) {
		colorTrackerPanel.setOldColor(oldColor);
		setColor(oldColor);
		this.updateConsumer = updateConsumer;
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(this);
		System.out.println("setting popup things");
		popupMenu.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
					setColor(oldColor);
				}
				super.keyPressed(e);
			}
		});
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				System.out.println("PopupMenuEvent: " + e);
				colorConsumer.accept(getColor());
				ColorChooserPopup.this.updateConsumer = null;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				System.out.println("canceled");
//				colorConsumer.accept(oldColor);
			}
		});
		popupMenu.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				super.componentHidden(e);
			}
		});
		if(parent.isShowing()){
			popupMenu.show(parent, 0, parent.getHeight());
		}
		System.out.println("showing popup!");
//		int option = JOptionPane.showConfirmDialog(parent, this, "Choose Background Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
//		if (option == JOptionPane.OK_OPTION && getColor() != null) {
//			return getColor();
//		}
//		return oldColor;
	}

	private ChangeListener getChangeListener() {
		return e -> {
			selectedColor = getColor();
			getSelectionModel().setSelectedColor(selectedColor);
			if(updateConsumer != null){
				updateConsumer.accept(getColor());
			}
			if (!mouseDown) {
				colorTrackerPanel.addAndUpdateSwatchesPanel(selectedColor);
			}
		};
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseDown = true;
				if(updateConsumer != null){
					updateConsumer.accept(getColor());
				}
				System.out.println("mouse pressed");
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
				colorTrackerPanel.addColorToLatest(selectedColor);
				if(updateConsumer != null){
					updateConsumer.accept(getColor());
				}
				System.out.println("mouse released");
			}
		};
	}

}
//package com.hiveworkshop.rms.ui.util;
//
//import javax.swing.*;
//import javax.swing.colorchooser.AbstractColorChooserPanel;
//import javax.swing.event.ChangeListener;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//
//public class ColorChooserPopupThing2 {
//	private static final JColorChooser colorChooser = new JColorChooser();
//	private final ColorTrackerPanel colorTrackerPanel;
//
//
//	private boolean mouseDown = false;
//	private Color selectedColor;
//
//	public ColorChooserPopupThing2(){
//		colorTrackerPanel = new ColorTrackerPanel(colorChooser::setColor);
//		colorChooser.setPreviewPanel(colorTrackerPanel);
//
//		colorChooser.getSelectionModel().addChangeListener(getChangeListener());
//		MouseAdapter l = getMouseAdapter();
//
//		colorChooser.addMouseListener(l);
//
//		for (AbstractColorChooserPanel ugg : colorChooser.getChooserPanels()) {
//			ugg.addMouseListener(l);
//			for (int i = 0; i < ugg.getComponentCount(); i++) {
//				Component component = ugg.getComponent(i);
//				component.addMouseListener(l);
//			}
//		}
//	}
//	public Color getNewColor(Color oldColor, Component parent) {
//		colorChooser.setColor(oldColor);
//		colorTrackerPanel.setOldColor(oldColor);
//		int option = JOptionPane.showConfirmDialog(parent, colorChooser, "Choose Background Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
//		if (option == JOptionPane.OK_OPTION && colorChooser.getColor() != null) {
//			return colorChooser.getColor();
//		}
//		return oldColor;
//	}
//
//
//	private ChangeListener getChangeListener() {
//		return e -> {
//			selectedColor = colorChooser.getColor();
//			colorChooser.getSelectionModel().setSelectedColor(selectedColor);
//			if (!mouseDown) {
//				colorTrackerPanel.addAndUpdateSwatchesPanel(selectedColor);
//			}
//		};
//	}
//
//	private MouseAdapter getMouseAdapter() {
//		return new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				mouseDown = true;
//				System.out.println("mouse pressed");
//			}
//
//			@Override
//			public void mouseReleased(MouseEvent e) {
//				mouseDown = false;
//				colorTrackerPanel.addColorToLatest(selectedColor);
//				System.out.println("mouse released");
//			}
//		};
//	}
//
//}
