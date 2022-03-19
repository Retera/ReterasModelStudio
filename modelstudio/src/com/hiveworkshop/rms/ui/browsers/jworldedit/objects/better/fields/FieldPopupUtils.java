package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.text.ParseException;

public enum FieldPopupUtils {
	;

	public static int showPopup(Component parentComponent, JPanel message, String title, int optionType,
	                            int messageType, JComponent componentToFocus) {
		return showConfirmDialog(parentComponent, message, title, optionType, messageType, componentToFocus);
	}

	public static int showPopup(Component parentComponent, JPanel message, String title, JComponent componentToFocus) {
		return showConfirmDialog(parentComponent, message, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, componentToFocus);
	}

	/**
	 * This method shows a confirmation dialog with the given message, title,
	 * messageType and optionType. The frame owner will be the same frame as the one
	 * that holds the given parentComponent. This method returns the option selected
	 * by the user.
	 *
	 * @param parentComponent The component to find a frame in.
	 * @param message         The message displayed.
	 * @param title           The title of the dialog.
	 * @param optionType      The optionType.
	 * @param messageType     The messageType.
	 * @return The selected option.
	 */
	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType,
	                                    int messageType, JComponent componentWantingFocus) {
		PopupContext popupContext = new PopupContext();
		if (componentWantingFocus instanceof JSpinner) {
			JSpinner spinner = (JSpinner) componentWantingFocus;
			JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
			JFormattedTextField textField = editor.getTextField();
			spinner.addChangeListener(e -> {
				commitEdit(parentComponent, spinner);
			});
			textField.addActionListener(e -> {
				disposeExisting(popupContext);
			});
		}
		JOptionPane pane = new JOptionPane(message, messageType, optionType) {
			@Override
			public void selectInitialValue() {
				SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> selectAll(componentWantingFocus)));
			}
		};
		JDialog dialog = pane.createDialog(parentComponent, title);
		popupContext.dialog = dialog;
		popupContext.optionPane = pane;
		dialog.setVisible(true);

		if (pane.getValue() instanceof Integer) {
			return (Integer) pane.getValue();
		}
		return -1;
	}

	private static void selectAll(JComponent componentWantingFocus) {
		if (componentWantingFocus instanceof JSpinner) {
			JSpinner spinner = (JSpinner) componentWantingFocus;
			((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().selectAll();
		} else {
			if (componentWantingFocus instanceof JTextComponent) {
				((JTextComponent) componentWantingFocus).selectAll();
			}
		}
	}

	private static void disposeExisting(PopupContext popupContext) {
		if (popupContext.dialog != null) {
			popupContext.optionPane.setValue(JOptionPane.OK_OPTION);
			popupContext.dialog.dispose();
		}
	}

	private static void commitEdit(Component parentComponent, JSpinner spinner) {
		try {
			spinner.commitEdit();
		} catch (ParseException e1) {
			JOptionPane.showMessageDialog(parentComponent,
					"Unable to commit edit because: " + e1.getClass() + ": " + e1.getMessage());
			e1.printStackTrace();
		}
	}

	private static final class PopupContext {
		private JOptionPane optionPane;
		private JDialog dialog;
	}
}
