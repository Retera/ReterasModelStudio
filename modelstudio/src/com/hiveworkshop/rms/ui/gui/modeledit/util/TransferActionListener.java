package com.hiveworkshop.rms.ui.gui.modeledit.util;

import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.language.TextKey;
import org.lwjgl.opengl.AWTGLCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class that tracks the focused component. This is necessary to delegate the
 * menu cut/copy/paste commands to the right component. An instance of this
 * class is listening and when the user fires one of these commands, it calls
 * the appropriate action on the currently focused component.
 *
 * Copied from
 * https://docs.oracle.com/javase/tutorial/uiswing/dnd/listpaste.html
 */
public class TransferActionListener implements ActionListener, PropertyChangeListener {
	private JComponent focusOwner = null;
	private PerspectiveViewport perspectiveViewport = null;
	private AWTGLCanvas canvas = null;

	public TransferActionListener() {
		final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addPropertyChangeListener("permanentFocusOwner", this);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e) {
//		System.out.println("property changed");
		final Object o = e.getNewValue();
//		System.out.println("e: " + e.getNewValue());
		if (o instanceof JComponent) {
			focusOwner = (JComponent) o;
		} else {
			focusOwner = null;
		}
		if (o instanceof PerspectiveViewport) {
			perspectiveViewport = (PerspectiveViewport) o;
		} else {
			perspectiveViewport = null;
		}
		if (o instanceof AWTGLCanvas) {
			canvas = (AWTGLCanvas) o;
		} else {
			canvas = null;
		}
//		System.out.println("focusOwner: " + focusOwner);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
//		System.out.println("actionPerformed");
		if (focusOwner == null && perspectiveViewport == null && canvas == null) {
//			System.out.println("focusOwner == null");
			return;
		}
		if(focusOwner != null){
			final String action = e.getActionCommand();
			System.out.println("action: " + action);
			final Action a = focusOwner.getActionMap().get(action);
			if (a != null) {
				System.out.println("will complete action!");
				a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
			}
		} else if(perspectiveViewport != null){
			final String action = e.getActionCommand();
			System.out.println("action: " + action + " (e: " + e + ")");
			System.out.println("action: " + action.length() + " (e: " + e + ")");
			JPanel parent = (JPanel) perspectiveViewport.getParent().getParent();
			final Action a = parent.getActionMap().get(action);
			if (a != null) {
				System.out.println("will complete action!");
				a.actionPerformed(new ActionEvent(parent, ActionEvent.ACTION_PERFORMED, null));
			}
		} else if(canvas != null){
			final String action = e.getActionCommand();
			System.out.println("action: " + action + " (e: " + e + ")");
			System.out.println("action: " + action.length() + " (e: " + e + ")");
			JPanel parent = (JPanel) canvas.getParent().getParent();
			final Action a = parent.getActionMap().get(action);
			if (a != null) {
				System.out.println("will complete action!");
				a.actionPerformed(new ActionEvent(parent, ActionEvent.ACTION_PERFORMED, null));
			}
		}
	}

	public void doActionPerformed(TextKey textKey) {
		System.out.println("actionPerformed");
		if (focusOwner == null && perspectiveViewport == null && canvas == null) {
			System.out.println("focusOwner == null");
			return;
		}
		if(focusOwner != null){
			System.out.println("action: " + textKey);
			final Action a = focusOwner.getActionMap().get(textKey);
			if (a != null) {
				System.out.println("will complete action!");
				a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
			}
		} else if(perspectiveViewport != null){
			JPanel parent = (JPanel) perspectiveViewport.getParent().getParent();
			final Action a = parent.getActionMap().get(textKey);
			if (a != null) {
				System.out.println("perspectiveViewport - " + "will complete action!");
				a.actionPerformed(new ActionEvent(parent, ActionEvent.ACTION_PERFORMED, null));
			}
		} else if(canvas != null){
			JPanel parent = (JPanel) canvas.getParent().getParent();
			final Action a = parent.getActionMap().get(textKey);
			if (a != null) {
				System.out.println("canvas - " + "will complete action!");
				a.actionPerformed(new ActionEvent(parent, ActionEvent.ACTION_PERFORMED, null));
			}
		}
	}
}