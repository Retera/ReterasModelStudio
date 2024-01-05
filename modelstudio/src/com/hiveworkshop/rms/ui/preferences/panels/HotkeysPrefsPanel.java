package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HotkeysPrefsPanel extends JPanel {

	public HotkeysPrefsPanel(ProgramPreferences pref) {
		super(new MigLayout());

		CollapsablePanel camera_shortcuts = new CollapsablePanel("Camera Shortcuts", new CameraShortcutPrefPanel(pref.getCameraControlPrefs()));
		add(camera_shortcuts, "wrap");

		CollapsablePanel viewActionPanel = new CollapsablePanel("3D Mouse Actions", new Nav3DMousePrefPanel(pref.getNav3DMousePrefs()));
		add(viewActionPanel, "wrap");

		JButton edit_keybindings = new JButton("Edit Keybindings");
		edit_keybindings.addActionListener(e -> viewKBPanel(pref));
		add(edit_keybindings, "wrap");
	}

	private void addSettingRow(String text, Supplier<Integer> buttonExSupplier, Consumer<Integer> integerConsumer) {
		MouseSetting mouseSetting = new MouseSetting(text, buttonExSupplier, integerConsumer, this);
		add(mouseSetting.getLabel());
		add(mouseSetting.getEditButton(), "wrap");
	}

	private void viewKBPanel(ProgramPreferences pref) {
		KeybindingPrefPanel keybindingPrefPanel = new KeybindingPrefPanel(pref.getKeyBindingPrefs());
//		keybindingPrefPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(keybindingPrefPanel, this, "Edit Keybindings");
	}

	private static class MouseSetting {
		String text;
		Supplier<Integer> buttonExSupplier;
		Consumer<Integer> integerConsumer;
		JComponent parent;
		MouseSetting(String text, Supplier<Integer> buttonExSupplier, Consumer<Integer> integerConsumer, JComponent parent){
			this.text = text;
			this.buttonExSupplier = buttonExSupplier;
			this.integerConsumer = integerConsumer;
			this.parent = parent;
		}

		JLabel getLabel(){
			return new JLabel(text);
		}

		JButton getEditButton(){
			JButton editButton = new JButton(MouseEvent.getModifiersExText(buttonExSupplier.get()));
			editButton.addActionListener(e -> integerConsumer.accept(editMouseButtonBinding(text, editButton, buttonExSupplier.get(), false)));
			return editButton;
		}

		private int editMouseButtonBinding(String textKey, JButton button, int mouseModEx, boolean ignoreModifiers) {
			JPanel panel = new JPanel(new MigLayout());
			JLabel bindingLabel = new JLabel(MouseEvent.getModifiersExText(mouseModEx));
			panel.add(bindingLabel);
			final int[] newModEx = {mouseModEx};
			JButton mouseListenButton = new JButton("Click to change binding");
			mouseListenButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (ignoreModifiers) {
						newModEx[0] = MouseEvent.getMaskForButton(e.getButton());
					} else {
						newModEx[0] = e.getModifiersEx();
					}
					bindingLabel.setText(MouseEvent.getModifiersExText(newModEx[0]));
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					super.mouseReleased(e);
				}
			});

			panel.add(mouseListenButton);

			int change = JOptionPane.showConfirmDialog(parent, panel, "Edit mouse-binding for " + textKey, JOptionPane.OK_CANCEL_OPTION);

			if (change == JOptionPane.OK_OPTION) {
				button.setText(MouseEvent.getModifiersExText(newModEx[0]));
				return newModEx[0];
			}
			return mouseModEx;
		}
	}
}
