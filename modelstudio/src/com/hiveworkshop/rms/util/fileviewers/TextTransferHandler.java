package com.hiveworkshop.rms.util.fileviewers;


import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import java.awt.datatransfer.Transferable;

public class TextTransferHandler extends TransferHandler implements UIResource {

	private JTextComponent exportComp;

	public int getSourceActions(JComponent c) {
		System.out.println("\ngetSourceActions");
		if (c instanceof JPasswordField) return NONE;
		return ((JTextComponent)c).isEditable() ? COPY_OR_MOVE : COPY;
	}

	protected Transferable createTransferable(JComponent comp) {
		System.out.println("createTransferable");
		exportComp = (JTextComponent)comp;
		int p0 = exportComp.getSelectionStart();
		int p1 = exportComp.getSelectionEnd();
		return (p0 != p1) ? (new BasicTransferable(exportComp, p0, p1)) : null;
	}

	protected void exportDone(JComponent source, Transferable data, int action) {
		exportComp = null;
	}

}