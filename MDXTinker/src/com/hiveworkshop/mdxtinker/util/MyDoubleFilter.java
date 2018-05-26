package com.hiveworkshop.mdxtinker.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Thanks to: https://stackoverflow.com/questions/11093326/restricting-jtextfield-input-to-integers
 *
 * @author Eric
 *
 */
public final class MyDoubleFilter extends DocumentFilter {
	@Override
	public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr)
			throws BadLocationException {

		final Document doc = fb.getDocument();
		final StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.insert(offset, string);

		if (test(sb.toString())) {
			super.insertString(fb, offset, string, attr);
		} else {
			// warn the user and don't allow the insert
		}
	}

	private boolean test(final String text) {
		if ("".equals(text)) {
			return true;
		}
		try {
			Double.parseDouble(text);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void replace(final FilterBypass fb, final int offset, final int length, final String text,
			final AttributeSet attrs) throws BadLocationException {

		final Document doc = fb.getDocument();
		final StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.replace(offset, offset + length, text);

		if (test(sb.toString())) {
			super.replace(fb, offset, length, text, attrs);
		} else {
			// warn the user and don't allow the insert
		}

	}

	@Override
	public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException {
		final Document doc = fb.getDocument();
		final StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.delete(offset, offset + length);

		if (test(sb.toString())) {
			super.remove(fb, offset, length);
		} else {
			// warn the user and don't allow the insert
		}

	}
}