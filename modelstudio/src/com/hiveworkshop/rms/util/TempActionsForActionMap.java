package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class TempActionsForActionMap {

	public static void maximizeFocusedWindow() {
		if (isTextField()) return;
//		RootWindow rootWindow = ProgramGlobals.getMainPanel().getRootWindow();
		RootWindow rootWindow = ProgramGlobals.getRootWindowUgg();
		View focusedView = rootWindow.getFocusedView();
		if (focusedView != null) {
			if (focusedView.isMaximized()) {
				rootWindow.setMaximizedWindow(null);
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
