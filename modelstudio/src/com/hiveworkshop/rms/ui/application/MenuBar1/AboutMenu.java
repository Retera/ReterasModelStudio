package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class AboutMenu extends JMenu {

	public AboutMenu() {
		super("About");
		setMnemonic(KeyEvent.VK_H);

		add(createMenuItem("Changelog", KeyEvent.VK_A, e -> createAndShowRtfPanel("docs/changelist.rtf", "Changelog")));

		add(createMenuItem("About", KeyEvent.VK_A, e -> createAndShowRtfPanel("docs/credits.rtf", "About")));

		add(createMenuItem("Help/Keyboard Shortcuts", KeyEvent.VK_A, e -> createAndShowHTMLPanel("docs/someHelp.html", "Help")));
	}

	void createAndShowRtfPanel(String filePath, String title) {
		DefaultStyledDocument document = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane();
		textPane.setForeground(Color.BLACK);
		textPane.setBackground(Color.WHITE);
		RTFEditorKit rtfk = new RTFEditorKit();
		try {
			rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream(filePath), document, 0);
		} catch (final BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textPane.setDocument(document);
		JFrame frame = new JFrame(title);
		frame.setContentPane(new JScrollPane(textPane));
		frame.setSize(650, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	void createAndShowHTMLPanel(String filePath, String title) {

		JTextPane textPane = new JTextPane();
		textPane.setForeground(Color.BLACK);
		textPane.setBackground(Color.WHITE);

		HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
		Document document = htmlEditorKit.createDefaultDocument();

		try {
			htmlEditorKit.read(GameDataFileSystem.getDefault().getResourceAsStream(filePath), document, 0);
		} catch (final BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JEditorPane jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		jEditorPane.setEditorKit(htmlEditorKit);
		jEditorPane.setDocument(document);

		JFrame frame = new JFrame(title);
		frame.setContentPane(new JScrollPane(jEditorPane));
		frame.setSize(650, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
