package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class TempActionsForActionMap {

	public static void maximizeFocusedWindow() {
		if (isTextField()) return;
		View focusedView = ProgramGlobals.getMainPanel().getRootWindow().getFocusedView();
		if (focusedView != null) {
			if (focusedView.isMaximized()) {
				ProgramGlobals.getMainPanel().getRootWindow().setMaximizedWindow(null);
			} else {
				focusedView.maximize();
			}
		}
	}

	private static boolean isTextField() {
		return focusedComponentNeedsTyping(getFocusedComponent());
	}


	private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
	}

	private static Component getFocusedComponent() {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		return kfm.getFocusOwner();
	}

}
