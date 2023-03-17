package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

public class TwiPopup {

	/**
	 * Creates and displays a non-modal dialog that is dismissed as soon as it looses focus.
	 */
	public static void quickDismissPopup(Component parent, Object message, String title){
//		JOptionPane.showMessageDialog(null, "message", "title", JOptionPane.PLAIN_MESSAGE);
		JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);

		optionPane.setComponentOrientation(((parent == null) ?
				JOptionPane.getRootFrame() : parent).getComponentOrientation());


		Window window = getWindowForComponent(parent);
		JDialog dialog;
		if (window instanceof Frame) {
			dialog = new JDialog((Frame)window, title, false);
		} else {
			dialog = new JDialog((Dialog)window, title, false);
		}
		dialog.setComponentOrientation(optionPane.getComponentOrientation());
		dialog.addWindowListener(getDisposeOnDeactivate(dialog));

		Container contentPane = dialog.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(optionPane, BorderLayout.CENTER);

		final PropertyChangeListener listener = getPropertyChangeListener(optionPane, dialog);
		optionPane.addPropertyChangeListener(listener);

		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

	private static PropertyChangeListener getPropertyChangeListener(JOptionPane optionPane, JDialog dialog) {
		return event -> {
			// Let the defaultCloseOperation handle the closing
			// if the user closed the window without selecting a button
			// (newValue = null in that case).  Otherwise, close the dialog.
			if (dialog.isVisible() && event.getSource() == optionPane &&
					(event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) &&
					event.getNewValue() != null &&
					event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
//				System.out.println("Pane property changed: " + event);
				dialog.setVisible(false);
			}
		};
	}

	private static WindowAdapter getDisposeOnDeactivate(JDialog dialog) {
		return new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				super.windowDeactivated(e);
				dialog.dispose();
			}
		};
	}

	static Window getWindowForComponent(Component parent)
			throws HeadlessException {
		if (parent == null)
			return JOptionPane.getRootFrame();
		if (parent instanceof Frame || parent instanceof Dialog)
			return (Window)parent;
		return getWindowForComponent(parent.getParent());
	}

}
