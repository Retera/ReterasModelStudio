package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TxtViewer {



	public void createAndShowHTMLPanel(String filePath, String title) {
		ArrayList<String> htmlTableStrings = getHtmlStringsWithLineBrake(filePath);
		System.out.println("should Show TXT \"" + filePath + "\", lines: " + htmlTableStrings.size());
		HTMLEditorKit editorKit = new HTMLEditorKit();
		HTMLDocument htmlDocument = new HTMLDocument();
		fillDocument(htmlTableStrings, htmlDocument, editorKit);

		showPanel(title, getFilledEditorPane(editorKit, combineStrings(htmlTableStrings), filePath));
	}

	private ArrayList<String> getHtmlStringsWithLineBrake(String sklPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		ArrayList<String> htmlTableStrings = new ArrayList<>();


		try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(sklPath)))) {
			r.lines().forEach(l -> {
				htmlTableStrings.add(l + "<br>");
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		return htmlTableStrings;
	}

	private Document fillDocument(List<String> strings, HTMLDocument document, HTMLEditorKit htmlEditorKit) {
		appendDocString(document, htmlEditorKit, "<html><body>");
		for(String s : strings){
			appendDocString(document, htmlEditorKit, s);
		}
		appendDocString(document, htmlEditorKit, "</body></html>");

		return document;
	}

	private void appendDocString(HTMLDocument document, HTMLEditorKit htmlEditorKit, String s) {
		try {
			htmlEditorKit.insertHTML(document, document.getLength(), s, 0, 0, null);
//			document.insertString(document.getLength(), s, null);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	private String combineStrings(List<String> strings){
		StringBuilder stringBuilder = new StringBuilder();
		for(String s : strings){
			stringBuilder.append(s);
		}
		return stringBuilder.toString();
	}


	private JEditorPane getFilledEditorPane(EditorKit editorKit, String document, String filePath) {
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		jEditorPane.setEditorKit(editorKit);
		jEditorPane.setText(document);
		return jEditorPane;
	}

	private void showPanel(String title, JEditorPane jEditorPane) {
		JFrame frame = new JFrame(title);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
//		scrollPane.scr
		frame.setContentPane(scrollPane);
		frame.setSize(650, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		System.out.println("showing panel \"" + title + "\"?");
	}
}
