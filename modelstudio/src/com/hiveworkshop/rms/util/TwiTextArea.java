package com.hiveworkshop.rms.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

//public class TwiTextArea extends JTextArea {
public class TwiTextArea extends JTextPane {
	public TwiTextArea(String text) {
//		super(text);
		setEditable(false);
		setOpaque(false);
//		setLineWrap(true);
//		setWrapStyleWord(true);

		setSize(400, 200);

//		setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);
//		this.setPreferredSize(new Dimension(400, 50));
		DefaultStyledDocument doc = new DefaultStyledDocument();
//		setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		Style def = StyleContext.getDefaultStyleContext().
				getStyle(StyleContext.DEFAULT_STYLE);


		Style regular = doc.addStyle("regular", def);
		Style mono = doc.addStyle("mono", def);
//		StyleConstants.setFontFamily(def, "SansSerif");
		StyleConstants.setFontFamily(mono, Font.MONOSPACED);

		Style italic = doc.addStyle("italic", mono);
		StyleConstants.setItalic(italic, true);

		Style hl1 = doc.addStyle("hl1", mono);
		StyleConstants.setBackground(hl1, new Color(255, 255, 255, 10));
		StyleConstants.setRightIndent(hl1, 0);
		StyleConstants.setAlignment(hl1, StyleConstants.ALIGN_JUSTIFIED);
		Style hl2 = doc.addStyle("hl2", mono);
		StyleConstants.setBackground(hl2, new Color(0, 0, 0, 10));
		StyleConstants.setRightIndent(hl2, 0);


		Style hl1Just = doc.addStyle("hl1Just", regular);
		StyleConstants.setBackground(hl1Just, new Color(255, 255, 255, 10));
		StyleConstants.setRightIndent(hl1Just, 0);
		StyleConstants.setAlignment(hl1Just, StyleConstants.ALIGN_JUSTIFIED);

		Style hl2Just = doc.addStyle("hl2Just", regular);
		StyleConstants.setBackground(hl2Just, new Color(0, 0, 0, 10));
		StyleConstants.setRightIndent(hl2Just, 0);
//		StyleConstants.setTabSet(hl2Just, new TabSet());
//		StyleConstants.setAlignment(hl2Just, StyleConstants.ALIGN_JUSTIFIED);
		StyleConstants.setAlignment(hl2Just, StyleConstants.ALIGN_RIGHT);

//		StyleConstants.ParagraphConstants.getComponent(hl2Just).setPreferredSize(new Dimension(12, 200));


		Style bold = doc.addStyle("bold", mono);
		StyleConstants.setBold(bold, true);
		String[] split = text.split("\n");
		try {
			int offs = 0;
			boolean hl = false;
//			String endStr = "  \n";
//			String endStr = "\n";
//			String endStr = "\u2029";
			String endStr = "\n\r";
//			String endStr = "\t";
//			String endStr2 = "\n";
			for (String s : split) {
//				doc.insertString(offs, s, hl2Just);
//				offs+=s.length();
////				doc.insertString(offs, endStr, hl1Just);
//				doc.insertString(offs, endStr, hl2Just);
//				offs+=endStr.length();

				if (hl) {
//					doc.insertString(offs, s + "\n", hl1);
					doc.insertString(offs, s, hl1);
					offs += s.length();
					doc.insertString(offs, endStr, hl1);
					offs += endStr.length();
//					doc.insertString(offs, endStr2, hl1);
//					offs+=endStr2.length();
				} else {
//					doc.insertString(offs, s + "\n", hl2);
					doc.insertString(offs, s, hl2);
					offs += s.length();
//					doc.insertString(offs, endStr, hl2Just);
					doc.insertString(offs, endStr, hl2);
					offs += endStr.length();
//					doc.insertString(offs, endStr2, hl2);
//					offs+=endStr2.length();
				}
				hl = !hl;
				System.out.println(s);
//				offs+=s.length();
//				doc.insertString(offs++, "\n", hl2);
			}
//			doc.insertString(0, text, mono);
//			doc.insertString(0, text, bold);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		setDocument(doc);
		revalidate();
		repaint();
	}
//	public TwiTextArea(String text, boolean editable){
//		super(text);
//		setEditable(editable);
//		setOpaque(false);
//		setLineWrap(true);
//		setWrapStyleWord(true);
//	}


}
