package com.hiveworkshop.rms.ui.preferences.panels;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseSettingPanel extends JPanel {
	private final JLabel newModExLabel;
	private int newModEx;
	private final int oldModEx;

	public MouseSettingPanel(Integer oldModEx) {
		super(new MigLayout("fill, ins 0", "", ""));
		this.oldModEx = oldModEx;
		newModEx = oldModEx;
		JLabel oldModExLabel = new JLabel(MouseEvent.getModifiersExText(oldModEx));
		newModExLabel = new JLabel(MouseEvent.getModifiersExText(oldModEx));


		JButton mouseListenButton = new JButton("Click to change binding");
		mouseListenButton.addMouseListener(getMouseAdapter());

		add(new JLabel("Old:"), "");
		add(oldModExLabel, "wrap");
		add(new JLabel("New:"), "");
		add(newModExLabel, "wrap");
		add(mouseListenButton, "spanx");
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			Integer lastPressed;
			@Override
			public void mousePressed(MouseEvent e) {

				lastPressed = e.getModifiersEx();

				newModExLabel.setText(MouseEvent.getModifiersExText(lastPressed));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				newModEx = lastPressed;
				super.mouseReleased(e);
			}
		};
	}


	public void onEdit() {
		newModExLabel.setText("");
		requestFocus();
	}
	public void onRemove() {
		newModExLabel.setText("");
	}
	public void onReset() {
		newModExLabel.setText(MouseEvent.getModifiersExText(oldModEx));
	}

	public Integer getNewKeyStroke() {
//		return newModEx;
		return newModEx != -1 ? newModEx : oldModEx;
	}
}