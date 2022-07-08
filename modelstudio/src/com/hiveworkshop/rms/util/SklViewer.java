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

public class SklViewer {



	public void createAndShowHTMLPanel(String filePath, String title) {
		ArrayList<String> htmlTableStrings = getHtmlTableStrings(filePath);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		HTMLDocument htmlDocument = new HTMLDocument();
//		Document document = editorKit.createDefaultDocument();
//		fillDocument(htmlTableStrings, document);
		fillDocument(htmlTableStrings, htmlDocument, editorKit);

//		showPanel(title, getFilledEditorPane(editorKit, document, filePath));
		showPanel(title, getFilledEditorPane(editorKit, combineStrings(htmlTableStrings), filePath));
	}

	private ArrayList<String> getHtmlTableStrings(String sklPath) {
		CompoundDataSource source = GameDataFileSystem.getDefault();
		ArrayList<String> htmlTableStrings = new ArrayList<>();
//		appendDocString(document);

		htmlTableStrings.add("\n\t<table>");


		try (BufferedReader r = new BufferedReader(new InputStreamReader(source.getResourceAsStream(sklPath)))) {
			ArrayList<String> htmlTableCells = new ArrayList<>();
			r.lines().forEach(l -> {
				if (l.startsWith("C;X1;Y")) {
					if (0 < htmlTableCells.size()){
						htmlTableStrings.add(getHtmlTableRow(combineStrings(htmlTableCells)));
						htmlTableCells.clear();
					}
					String[] split = l.split(";K");
					htmlTableCells.add(getHtmlTableCell(split[0].split("Y")[1]));
					htmlTableCells.add(getHtmlTableCell(split[1]));
				} else if (l.matches("C;X\\d+;K.+")){
					htmlTableCells.add(getHtmlTableCell(l.split(";K")[1]));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		htmlTableStrings.add("\n\t</table>");

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

	private void appendDocString(Document document, String s) {
		try {
			document.insertString(document.getLength(), s, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
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

	private String getHtmlTableCell(String string){
		return "\n\t\t\t<td>" + string + "</td>";
	}

	private String getHtmlTableRow(String string){
		return "\n\t\t<tr>" + string + "\n\t\t</tr>";
	}


//	String[] tag_name_inBeta = new String[3];
//
//	private void processMappingLine(String s) {
//		if (s.startsWith("C;X1;Y")) {
//			String[] strings = s.split("\"");
//			if (strings.length > 1) {
//				tag_name_inBeta[0] = strings[1];
//			}
//
//		} else if (s.startsWith("C;X2;K")) {
//			String[] strings = s.split("\"");
//			if (strings.length > 1) {
//				tag_name_inBeta[1] = strings[1];
//			}
//		} else if (s.startsWith("C;X3;K0") || s.startsWith("C;X3;K1")) {
//			String[] strings = s.split(";K");
//			if (!strings[1].contains("\"")) {
//				tag_name_inBeta[2] = strings[1];
////				System.out.println(Arrays.toString(tag_name_inBeta));
//				Sound sound = tagToSoundMap.computeIfAbsent(tag_name_inBeta[0], k -> new Sound(tag_name_inBeta));
//				nameToTags.computeIfAbsent(sound.getSoundName(), k -> new ArrayList<>()).add(sound.getTag());
//			}
//		}
//	}

	private JEditorPane getFilledEditorPane(EditorKit editorKit, Document document, String filePath) {
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
//		jEditorPane.setContentType("text/html");
		jEditorPane.setEditorKit(editorKit);
		jEditorPane.setDocument(document);

//		if(editorKit instanceof  HTMLEditorKit){
//
//			try {
//				((HTMLEditorKit)editorKit).insertHTML(((HTMLDocument)document), document.getLength(), "<b>hello", 0, 0, HTML.Tag.B);
//				((HTMLEditorKit)editorKit).insertHTML(((HTMLDocument)document), document.getLength(), "<font color='red'><u>world</u></font>", 0, 0, null);
//			} catch (BadLocationException | IOException e) {
//				e.printStackTrace();
//			}
//		}
		return jEditorPane;
	}
	private JEditorPane getFilledEditorPane(EditorKit editorKit, String document, String filePath) {
		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
//		jEditorPane.setContentType("text/html");
		jEditorPane.setEditorKit(editorKit);
		jEditorPane.setText(document);

//		if(editorKit instanceof  HTMLEditorKit){
//
//			try {
//				((HTMLEditorKit)editorKit).insertHTML(((HTMLDocument)document), document.getLength(), "<b>hello", 0, 0, HTML.Tag.B);
//				((HTMLEditorKit)editorKit).insertHTML(((HTMLDocument)document), document.getLength(), "<font color='red'><u>world</u></font>", 0, 0, null);
//			} catch (BadLocationException | IOException e) {
//				e.printStackTrace();
//			}
//		}
		return jEditorPane;
	}

	private Document getDocument(String filePath, EditorKit editorKit) {
		Document document = editorKit.createDefaultDocument();

		try {
			editorKit.read(GameDataFileSystem.getDefault().getResourceAsStream(filePath), document, 0);
		} catch (final BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}
	private void scrollToTopLeft(JScrollPane scrollPane){
		scrollPane.getVerticalScrollBar().setValue(0);
		scrollPane.getHorizontalScrollBar().setValue(0);
	}

	private void showPanel(String title, JEditorPane jEditorPane) {
		JFrame frame = new JFrame(title);
		JScrollPane scrollPane = new JScrollPane(jEditorPane);
		frame.setContentPane(scrollPane);
		frame.setSize(650, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		SwingUtilities.invokeLater(() -> scrollToTopLeft(scrollPane));
		System.out.println("showing panel \"" + title + "\"?");
	}
}
