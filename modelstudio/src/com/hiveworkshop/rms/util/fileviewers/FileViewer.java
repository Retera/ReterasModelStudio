package com.hiveworkshop.rms.util.fileviewers;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public abstract class FileViewer {
	protected JEditorPane jEditorPane;
	protected JScrollPane scrollPane;
	protected EditorKit editorKit;

	protected FileViewer(EditorKit editorKit) {
		this.editorKit = editorKit;
		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		jEditorPane.setEditorKit(editorKit);
		jEditorPane.setText("Loading....");
		jEditorPane.setPreferredSize(new Dimension(650, 500));
		scrollPane = new JScrollPane(jEditorPane);
		jEditorPane.setTransferHandler(new TextTransferHandler());
	}

	long timeStart;
	public void loadAndShow(File file, String title) {
		timeStart = System.currentTimeMillis();

		showFrame(title);

		Document document = getFilledDocument(getReadFile(file));
		SwingUtilities.invokeLater(() -> setDocumentAndScroll(document));
	}

	private Document getFilledDocument(String text) {
		Document document = editorKit.createDefaultDocument();

		Reader stringReader = new StringReader(text);
		try {
			editorKit.read(stringReader, document, 0);
		} catch (IOException | BadLocationException e) {
			throw new RuntimeException(e);
		}
		return document;
	}

	protected abstract String getReadFile(File file);

	private void setDocumentAndScroll(Document document) {
		jEditorPane.setDocument(document);
		SwingUtilities.invokeLater(this::scrollToTopLeft);
	}

	protected void scrollToTopLeft() {
		scrollPane.getVerticalScrollBar().setValue(0);
		scrollPane.getHorizontalScrollBar().setValue(0);
		long time = System.currentTimeMillis() - timeStart;
		System.out.println("Took " + time + "ms to load!");
	}

	private void showFrame(String title) {
		scrollPane.setPreferredSize(new Dimension(650, 500));
		JFrame frame = new JFrame(title);
		frame.setSize(650, 500);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setContentPane(scrollPane);
		frame.revalidate();
		frame.repaint();
		System.out.println("showing panel \"" + title + "\"?");
	}
}