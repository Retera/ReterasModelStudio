package com.matrixeater.src;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


public class ExceptionPopup {
	public static void display(Exception e) {

		final JTextPane pane = new JTextPane();
		OutputStream stream = new OutputStream() {
			public void updateStreamWith(String s)
			{
				Document doc = pane.getDocument();
				try {
					doc.insertString(doc.getLength(), s, null);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(null,"MDL open error popup failed to create info popup.");
					e.printStackTrace();
				}
			}
			
			@Override
			public void write(final int b) throws IOException
			{
				updateStreamWith(String.valueOf((char)b));
			}

			@Override
			public void write(byte [] b, int off, int len) throws IOException
			{
				updateStreamWith(new String(b, off, len));
			}

			@Override
			public void write(byte [] b) throws IOException
			{
				write(b, 0, b.length);
			}
		};
		PrintStream ps = new PrintStream(stream);
		ps.println("Unknown error occurred:");
		e.printStackTrace(ps);
		JOptionPane.showMessageDialog(null,pane);
	}
	public static void display(String s, Exception e) {

		final JTextPane pane = new JTextPane();
		OutputStream stream = new OutputStream() {
			public void updateStreamWith(String s)
			{
				Document doc = pane.getDocument();
				try {
					doc.insertString(doc.getLength(), s, null);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(null,"MDL open error popup failed to create info popup.");
					e.printStackTrace();
				}
			}
			
			@Override
			public void write(final int b) throws IOException
			{
				updateStreamWith(String.valueOf((char)b));
			}

			@Override
			public void write(byte [] b, int off, int len) throws IOException
			{
				updateStreamWith(new String(b, off, len));
			}

			@Override
			public void write(byte [] b) throws IOException
			{
				write(b, 0, b.length);
			}
		};
		PrintStream ps = new PrintStream(stream);
		ps.println(s+":");
		e.printStackTrace(ps);
		JOptionPane.showMessageDialog(null,pane);
	}
}
