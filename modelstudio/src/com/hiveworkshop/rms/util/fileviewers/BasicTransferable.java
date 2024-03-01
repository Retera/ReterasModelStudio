package com.hiveworkshop.rms.util.fileviewers;


import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashMap;
import java.util.Map;

public class BasicTransferable implements Transferable, UIResource {
	/**
	 * This is a striped down copy of BasicTransferable merged with BasicTextUI#TextTransferable
	 * Used to provide tab-separated cell values when copying selections from a html-table.
	 * (It might be possible to strip it even more...)
	 */

	private static Map<String, Boolean> mimeSubtypoeSuport = new HashMap<>(17);
	static {
		mimeSubtypoeSuport.put("sgml", Boolean.TRUE);
		mimeSubtypoeSuport.put("xml", Boolean.TRUE);
		mimeSubtypoeSuport.put("html", Boolean.TRUE);
		mimeSubtypoeSuport.put("enriched", Boolean.TRUE);
		mimeSubtypoeSuport.put("richtext", Boolean.TRUE);
		mimeSubtypoeSuport.put("uri-list", Boolean.TRUE);
		mimeSubtypoeSuport.put("directory", Boolean.TRUE);
		mimeSubtypoeSuport.put("css", Boolean.TRUE);
		mimeSubtypoeSuport.put("calendar", Boolean.TRUE);
		mimeSubtypoeSuport.put("plain", Boolean.TRUE);
		mimeSubtypoeSuport.put("rtf", Boolean.FALSE);
		mimeSubtypoeSuport.put("tab-separated-values", Boolean.FALSE);
		mimeSubtypoeSuport.put("t140", Boolean.FALSE);
		mimeSubtypoeSuport.put("rfc822-headers", Boolean.FALSE);
		mimeSubtypoeSuport.put("parityfec", Boolean.FALSE);
	}

	private String richText;
	protected String plainData;
	protected String htmlData;

	private static DataFlavor[] htmlFlavors;
	private static DataFlavor[] stringFlavors;
	private static DataFlavor[] plainFlavors;
	private DataFlavor[] richerFlavors;

	static {
		try {
			htmlFlavors = new DataFlavor[3];
			htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
			htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
			htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");

			plainFlavors = new DataFlavor[3];
			plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String");
			plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
			plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");

			stringFlavors = new DataFlavor[2];
			stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=java.lang.String");
			stringFlavors[1] = DataFlavor.stringFlavor;


		} catch (ClassNotFoundException cle) {
			System.err.println("error initializing javax.swing.plaf.basic.BasicTranserable");
		}
	}


	public BasicTransferable(JTextComponent c, int start, int end) {
		this.plainData = null;
		this.htmlData = null;
		this.richText = null;
		richerFlavors = new DataFlavor[0];
		System.out.println("\nBasicTransferable");

		Document doc = c.getDocument();
		try {
			Position p0 = doc.createPosition(start);
			Position p1 = doc.createPosition(end);
			plainData = c.getSelectedText();

			if (c instanceof JEditorPane ep) {

				String mimeType = ep.getContentType();
				if (mimeType.startsWith("text/plain")) {
					return;
				}

				StringWriter sw = new StringWriter(p1.getOffset() - p0.getOffset());
				ep.getEditorKit().write(sw, doc, p0.getOffset(), p1.getOffset() - p0.getOffset());

				if (mimeType.startsWith("text/html")) {
					htmlData = sw.toString();
				} else {
					richText = sw.toString();
					richerFlavors = new DataFlavor[3];
					try {
						richerFlavors[0] = new DataFlavor(mimeType + ";class=java.lang.String");
						richerFlavors[1] = new DataFlavor(mimeType + ";class=java.io.Reader");
						richerFlavors[2] = new DataFlavor(mimeType + ";class=java.io.InputStream;charset=unicode");
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} catch (BadLocationException | IOException ignored) {}
	}

	public DataFlavor[] getTransferDataFlavors() {
		System.out.println("getTransferDataFlavors");
		int nRicher = (richText != null) ? richerFlavors.length : 0;
		int nHTML = (htmlData != null) ? htmlFlavors.length : 0;
		int nPlain = (plainData != null) ? plainFlavors.length: 0;
		int nString = (plainData != null) ? stringFlavors.length : 0;
		int nFlavors = nRicher + nHTML + nPlain + nString;
		DataFlavor[] flavors = new DataFlavor[nFlavors];

		// fill in the array
		int nDone = 0;
		if (nRicher > 0) {
			System.arraycopy(richerFlavors, 0, flavors, nDone, nRicher);
			nDone += nRicher;
		}
		if (nHTML > 0) {
			System.arraycopy(htmlFlavors, 0, flavors, nDone, nHTML);
			nDone += nHTML;
		}
		if (nPlain > 0) {
			System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
			nDone += nPlain;
		}
		if (nString > 0) {
			System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
			nDone += nString;
		}
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		System.out.println("isDataFlavorSupported: " + flavor);
		DataFlavor[] flavors = getTransferDataFlavors();
		for (DataFlavor dataFlavor : flavors) {
			if (dataFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		System.out.println("getTransferData");
		if (isRicherFlavor(flavor)) {
			System.out.println("rich flavor");
			if (String.class.equals(flavor.getRepresentationClass())) {
				return (richText == null) ? "" : richText;
			} else if (Reader.class.equals(flavor.getRepresentationClass())) {
				return new StringReader((richText == null) ? "" : richText);
			} else if (InputStream.class.equals(flavor.getRepresentationClass())) {
				return new StringBufferInputStream((richText == null) ? "" : richText);
			}
		} else if (isHTMLFlavor(flavor)) {
			System.out.println("html flavor");
			if (String.class.equals(flavor.getRepresentationClass())) {
				return (htmlData == null) ? "" : htmlData;
			} else if (Reader.class.equals(flavor.getRepresentationClass())) {
				return new StringReader((htmlData == null) ? "" : htmlData);
			} else if (InputStream.class.equals(flavor.getRepresentationClass())) {
				return createInputStream(flavor, (htmlData == null) ? "" : htmlData);
			}
			// fall through to unsupported
		} else if (isPlainFlavor(flavor)) {
			System.out.println("plain flavor");
			if (String.class.equals(flavor.getRepresentationClass())) {
//				System.out.println(plainData.replaceAll("\\n(?=\\S)", "\t"));
				return (plainData == null) ? "" : plainData.replaceAll("(?<=\\S)\\n(?=\\S)", "\t");
			} else if (Reader.class.equals(flavor.getRepresentationClass())) {
				return new StringReader((plainData == null) ? "" : plainData);
			} else if (InputStream.class.equals(flavor.getRepresentationClass())) {
				return createInputStream(flavor, (plainData == null) ? "" : plainData);
			}
			// fall through to unsupported

		} else if (isStringFlavor(flavor)) {
			System.out.println("string flavor");
			return (plainData == null) ? "" : plainData;
		}
		throw new UnsupportedFlavorException(flavor);
	}


	private InputStream createInputStream(DataFlavor flavor, String data) throws IOException, UnsupportedFlavorException {
		System.out.println("createInputStream");
		if (isFlavorCharsetTextType(flavor)) {
			String encoding = flavor.getParameter("charset");
			String cs = (encoding != null) ? encoding : Charset.defaultCharset().name();
			return new ByteArrayInputStream(data.getBytes(cs));
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public static boolean isFlavorCharsetTextType(DataFlavor flavor) {
		System.out.println("isFlavorCharsetTextType");
		if (DataFlavor.stringFlavor.equals(flavor)) {
			return true;
		}

		if ("text".equals(flavor.getPrimaryType())
				&& flavor.getSubType() != null
				&& mimeSubtypoeSuport.computeIfAbsent(flavor.getSubType(), k -> (flavor.getParameter("charset") != null))) {
			Class<?> rep_class = flavor.getRepresentationClass();

			if (flavor.isRepresentationClassReader() || String.class.equals(rep_class) || flavor.isRepresentationClassCharBuffer() || char[].class.equals(rep_class)) {
				return true;
			}

			if (flavor.isRepresentationClassInputStream() || flavor.isRepresentationClassByteBuffer() || byte[].class.equals(rep_class)) {
				String charset = flavor.getParameter("charset");
				if (charset != null) {
					try {
						return Charset.isSupported(charset);
					} catch (IllegalCharsetNameException icne) {
						return false;
					}
				}
				return true;
			}
		}

		return false;

	}

	protected boolean isRicherFlavor(DataFlavor flavor) {
		for (DataFlavor richerFlavor : richerFlavors) {
			if (richerFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isHTMLFlavor(DataFlavor flavor) {
		for (DataFlavor dataFlavor : htmlFlavors) {
			if (dataFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isPlainFlavor(DataFlavor flavor) {
		for (DataFlavor dataFlavor : plainFlavors) {
			if (dataFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isStringFlavor(DataFlavor flavor) {
		for (DataFlavor dataFlavor : stringFlavors) {
			if (dataFlavor.equals(flavor)) {
				return true;
			}
		}
		return false;
	}


}
