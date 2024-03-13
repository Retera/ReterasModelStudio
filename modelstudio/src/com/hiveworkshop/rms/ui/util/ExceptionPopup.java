package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.CloseModel;
import com.hiveworkshop.rms.util.ProgramVersion;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
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
		pane.setBackground(new Color(30,30,30));
		pane.setForeground(new Color(240, 80, 60));

		final OutputStream stream = getOutputStream(pane);
		final PrintStream ps = new PrintStream(stream);
		ps.println(s);
		e.printStackTrace(ps);

		JScrollPane jScrollPane = new JScrollPane(pane);
		// Make the exception popup not huge and scrollable
		jScrollPane.setPreferredSize(ScreenInfo.getSmallWindow());
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel panel = new JPanel(new MigLayout("fill, ins 0,"));
		panel.add(jScrollPane, "spanx, growx, growy, wrap");
		SwingUtilities.invokeLater(() -> jScrollPane.getVerticalScrollBar().setValue(0));
//		jScrollPane.getVerticalScrollBar().setValue(0);


		// Some QoL buttons:
		// Undo so that users that get stuck in a error-loop has a
		// chance to get out of it without loosing their progress
		panel.add(Button.create("try to undo", ProgramGlobals.getUndoHandler().getUndoAction()));
		// And a way close the current model
		// This will also trigger the save-prompt
		panel.add(Button.create("close model", a -> closeCurrentModel()));
		// And a way to exit RMS without going through the Task Manager
		// This will also trigger the save-prompt
		panel.add(Button.create("exit RMS", a -> exitRMS()));
		panel.add(Button.create("force exit RMS", a -> forceExit()));


		JOptionPane.showMessageDialog(null, panel, "Warning (" + ProgramVersion.get() + ")", JOptionPane.WARNING_MESSAGE, null);
	}

	private static void closeCurrentModel() {
		ProgramGlobals.getMainPanel().setVisible(false);
		CloseModel.closeModelPanel();
		ProgramGlobals.getMainPanel().setVisible(true);
	}

	private static void exitRMS() {
		ProgramGlobals.getMainPanel().setVisible(false);
		if (CloseModel.closeAll()) {
			System.exit(-1);
		}
		ProgramGlobals.getMainPanel().setVisible(true);
	}

	private static void forceExit() {
		ProgramGlobals.getMainPanel().setVisible(false);
		System.exit(-1);
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
				JButton exceptionButton = Button.create("Show first Exception", e -> display(firstException, "First exception to occur:"));
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
			SwingUtilities.invokeLater(() -> jScrollPane.getVerticalScrollBar().setValue(0));

			clearStringsToShow();
			JOptionPane.showMessageDialog(null, infoPanel, "Warning (" + ProgramVersion.get() + ")", JOptionPane.WARNING_MESSAGE, null);

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
