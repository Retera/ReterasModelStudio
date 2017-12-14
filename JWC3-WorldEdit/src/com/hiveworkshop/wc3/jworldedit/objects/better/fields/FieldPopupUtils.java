package com.hiveworkshop.wc3.jworldedit.objects.better.fields;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;

public enum FieldPopupUtils {
	;

	public static int showPopup(final Component parentComponent, final JPanel message, final String title,
			final int optionType, final int messageType, final JComponent componentToFocus) {
		componentToFocus.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(final AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(final AncestorEvent event) {
			}

			@Override
			public void ancestorAdded(final AncestorEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if (componentToFocus instanceof JSpinner) {
									final JFormattedTextField textField = ((JSpinner.DefaultEditor) ((JSpinner) componentToFocus)
											.getEditor()).getTextField();
									// textField.requestFocus();
									// textField.setText(textField.getText());
									textField.selectAll();
								} else {
									componentToFocus.requestFocus();
									if (componentToFocus instanceof JTextComponent) {
										((JTextComponent) componentToFocus).selectAll();
									}
								}
							}
						});
					}
				});
			}
		});
		return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
	}
}
