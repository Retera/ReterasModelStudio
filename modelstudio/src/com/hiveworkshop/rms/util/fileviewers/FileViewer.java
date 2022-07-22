package com.hiveworkshop.rms.util.fileviewers;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class FileViewer {

	public void createAndShowHTMLPanel(String filePath, String title) {
		ArrayList<String> htmlTableStrings = getHtmlStrings(filePath);
		createAndShowPanel(title, htmlTableStrings);
	}

	public void createAndShowHTMLPanel(File file, String title) {
		ArrayList<String> htmlTableStrings = getHtmlStrings(file);
		createAndShowPanel(title, htmlTableStrings);
	}

	public void createAndShowHTMLPanel(File file) {
		ArrayList<String> htmlTableStrings = getHtmlStrings(file);
		createAndShowPanel(file.getName(), htmlTableStrings);
	}

	private void createAndShowPanel(String title, ArrayList<String> htmlTableStrings) {
//		HTMLEditorKit editorKit = new HTMLEditorKit();
//		HTMLDocument htmlDocument = new HTMLDocument();
//		System.out.println("filling document with " + title + " (" + htmlTableStrings.size() + " strings)");
//		fillDocument(htmlTableStrings, htmlDocument, editorKit);

		String document = combineStrings(htmlTableStrings);
		System.out.println("should Show \"" + title + "\", strings: " + htmlTableStrings.size());
//		JEditorPane filledEditorPane = getFilledEditorPane(editorKit, document);
		showPanel(title, document);
	}
//	private void createAndShowPanel(String title, ArrayList<String> htmlTableStrings) {
//		HTMLEditorKit editorKit = new HTMLEditorKit();
////		HTMLDocument htmlDocument = new HTMLDocument();
////		System.out.println("filling document with " + title + " (" + htmlTableStrings.size() + " strings)");
////		fillDocument(htmlTableStrings, htmlDocument, editorKit);
//
//		System.out.println("combining " + htmlTableStrings.size() + " strings!");
//		String document = combineStrings(htmlTableStrings);
//		System.out.println("should Show \"" + title + "\", strings: " + htmlTableStrings.size());
//		JEditorPane filledEditorPane = getFilledEditorPane(editorKit, document);
//		showPanel(title, filledEditorPane);
//	}

	protected ArrayList<String> getHtmlStrings(String sklPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		InputStream in = source.getResourceAsStream(sklPath);
		return getStrings(in);
	}

	protected ArrayList<String> getHtmlStrings(File file) {
		try (FileInputStream in = new FileInputStream(file)){
			return getStrings(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getStrings(null);
	}

	protected abstract ArrayList<String> getStrings(InputStream in);

	protected String combineStrings(List<String> strings){
		StringBuilder stringBuilder = new StringBuilder();
		for(String s : strings){
			stringBuilder.append(s);
		}
		return stringBuilder.toString();
	}

	protected Document fillDocument(List<String> strings, HTMLDocument document, HTMLEditorKit htmlEditorKit) {
		Element[] rootElements = document.getRootElements();
		System.out.println("rootElements length: " + rootElements.length);
		for(Element e : rootElements){
			System.out.println(e);
		}
		appendDocString(document, htmlEditorKit, "<html><body>");

//		appendDocString(document, htmlEditorKit, combineStrings(strings));
//		for(String s : strings){
//			appendDocString(document, htmlEditorKit, s);
//		}
		appendDocString(document, htmlEditorKit, "</body></html>");

		return document;
	}

	protected void appendDocString(HTMLDocument document, HTMLEditorKit htmlEditorKit, String s) {
		try {
			htmlEditorKit.insertHTML(document, document.getLength(), s, 0, 0, null);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	protected JEditorPane getFilledEditorPane(EditorKit editorKit, String document) {
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		jEditorPane.setEditorKit(editorKit);
		jEditorPane.setText(document);
		return jEditorPane;
	}

	protected JEditorPane getEditorPane() {
		HTMLEditorKit editorKit = new HTMLEditorKit();
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		jEditorPane.setEditorKit(editorKit);
		return jEditorPane;
	}


	protected void scrollToTopLeft(JScrollPane scrollPane){
		scrollPane.getVerticalScrollBar().setValue(0);
		scrollPane.getHorizontalScrollBar().setValue(0);
		System.out.println("Should be fully loaded");
	}

	protected void showPanel(String title, JEditorPane jEditorPane) {
		JFrame frame = new JFrame(title);
		frame.setSize(650, 500);
		JPanel contentPanel = new JPanel(new MigLayout("fill, ins 0, gap 0"));
		contentPanel.setPreferredSize(new Dimension(650, 500));
		JScrollPane tempScrollPane = new JScrollPane(new JPanel());
		tempScrollPane.setPreferredSize(new Dimension(650, 500));
		contentPanel.add(tempScrollPane, "growx, growy");

		frame.setContentPane(contentPanel);
//		frame.setContentPane(tempScrollPane);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.revalidate();

		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		scrollPane.setPreferredSize(new Dimension(650, 500));
		contentPanel.removeAll();
		contentPanel.add(scrollPane, "growx, growy");
		contentPanel.repaint();
		frame.revalidate();
		frame.repaint();

		SwingUtilities.invokeLater(() -> scrollToTopLeft(scrollPane));
		System.out.println("showing panel \"" + title + "\"?");
	}

	protected void showPanel(String title, String document) {
		JFrame frame = new JFrame(title);
		frame.setSize(650, 500);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		JEditorPane jEditorPane = getEditorPane();
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		jEditorPane.setText("Loading....");


		scrollPane.setPreferredSize(new Dimension(650, 500));
		frame.setContentPane(scrollPane);
		frame.revalidate();
		frame.repaint();

		SwingUtilities.invokeLater(() -> jEditorPane.setText(document));

		SwingUtilities.invokeLater(() -> scrollToTopLeft(scrollPane));
		System.out.println("showing panel \"" + title + "\"?");
	}
}
