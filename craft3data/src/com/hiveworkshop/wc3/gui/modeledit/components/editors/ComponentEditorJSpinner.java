package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

public class ComponentEditorJSpinner extends JSpinner {

	public ComponentEditorJSpinner() {
		super();
		init();
	}

	public ComponentEditorJSpinner(final SpinnerModel model) {
		super(model);
		init();
	}

	private void init() {
		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				((JSpinner.DefaultEditor) getEditor()).getTextField()
						.setForeground(UnsavedChangesDocumentListener.unsavedForegroundColor());
				((JSpinner.DefaultEditor) getEditor()).getTextField()
						.setBackground(UnsavedChangesDocumentListener.unsavedBackgroundColor());
			}
		});
		final JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		final DefaultFormatter formatter = (DefaultFormatter) textField.getFormatter();
		formatter.setCommitsOnValidEdit(true);
//		textField.getDocument().addDocumentListener(new UnsavedChangesDocumentListener(textField));
	}

	public void reloadNewValue(final Object value) {
		setValue(value);
		((JSpinner.DefaultEditor) getEditor()).getTextField()
				.setForeground(UnsavedChangesDocumentListener.savedForegroundColor());
		((JSpinner.DefaultEditor) getEditor()).getTextField()
				.setBackground(UnsavedChangesDocumentListener.savedBackgroundColor());
	}

	public void addActionListener(final Runnable runnable) {
		final JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runnable.run();
				}
			}
		});
	}

}
