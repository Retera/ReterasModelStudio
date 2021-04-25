package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ExceptionPopup {
	private static final List<String> stringsToShow = new ArrayList<>();
	private static Exception firstException;

	public static void display(final Throwable e) {

		display(e, "Unknown error occurred:");
	}

	public static void display(final String s, final Exception e) {

		display(e, s + ":");
	}

	public static void display(Throwable e, String s) {
		final JTextPane pane = new JTextPane();

		final OutputStream stream = getOutputStream(pane);
		final PrintStream ps = new PrintStream(stream);
		ps.println(s);
		e.printStackTrace(ps);

		JScrollPane jScrollPane = new JScrollPane(pane);
		// Make the exception popup not huge and scrollable
		jScrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JOptionPane.showMessageDialog(null, jScrollPane, "Warning (" + MainFrame.getVersion() + ")", JOptionPane.WARNING_MESSAGE, null);
	}


	public static void addStringToShow(String s) {
		stringsToShow.add(s);
	}

	public static void clearStringsToShow() {
		stringsToShow.clear();
	}

	public static void setFirstException(Exception e) {
		if (firstException == null) {
			firstException = e;
		}
	}

	public static void clearFirstException() {
		firstException = null;
	}

	public static void displayIfNotEmpty() {
		if (!stringsToShow.isEmpty()) {
			JPanel infoPanel = new JPanel(new MigLayout("fill, ins 0", "[grow][]", "[][][grow]"));
			infoPanel.add(new JLabel("Errors occurred while loading model."), "cell 0 0");
			infoPanel.add(new JLabel("To get more information run RMS from a terminal."), "cell 0 1");

			if (firstException != null) {
				JButton exceptionButton = new JButton("Show first Exeption");
				exceptionButton.addActionListener(e -> display(firstException, "First exception to occur:"));
				infoPanel.add(exceptionButton, "cell 1 0, spany 2, al 100% 50%, wrap");
			}

			JTextArea textArea = new JTextArea();
			for (String s : stringsToShow) {
				textArea.append(s);
				textArea.append("\n");
			}
			textArea.setEditable(false);
			JScrollPane jScrollPane = new JScrollPane(textArea);

			infoPanel.add(jScrollPane, "cell 0 2, spanx, growx, growy, wrap");

			// Make the exception popup not huge and scrollable
			infoPanel.setPreferredSize(ScreenInfo.getSmallWindow());
			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			clearStringsToShow();
			JOptionPane.showMessageDialog(null, infoPanel, "Warning (" + MainFrame.getVersion() + ")", JOptionPane.WARNING_MESSAGE, null);

			clearFirstException();
		}
	}

	public static OutputStream getOutputStream(JTextPane pane) {
		return new OutputStream() {
			public void updateStreamWith(final String s) {
				final Document doc = pane.getDocument();
				try {
					doc.insertString(doc.getLength(), s, null);
				} catch (final BadLocationException e) {
					JOptionPane.showMessageDialog(null,
							"MDL open error popup failed to create info popup.");
					e.printStackTrace();
				}
			}

			@Override
			public void write(final int b) {
				updateStreamWith(String.valueOf((char) b));
			}

			@Override
			public void write(final byte[] b, final int off, final int len) {
				updateStreamWith(new String(b, off, len));
			}

			@Override
			public void write(final byte[] b) {
				write(b, 0, b.length);
			}
		};
	}
}
