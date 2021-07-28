package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.MainLayoutCreator;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class CopyCutPast {
	static TransferActionListener transferActionListener = new TransferActionListener();

	private static class Copy extends ActionFunction {
		Copy(){
			super(TextKey.COPY, getAsAction(TextKey.COPY, (e) -> copyCutPast(e)));
			setKeyStroke("control C");
		}
	}
	private static class Cut extends ActionFunction {
		Cut(){
			super(TextKey.CUT, getAsAction(TextKey.CUT, (e) -> copyCutPast(e)));
			setKeyStroke("control X");
		}
	}
	private static class Paste extends ActionFunction {
		Paste(){
			super(TextKey.PASTE, getAsAction(TextKey.PASTE, (e) -> copyCutPast(e)));
			setKeyStroke("control V");

		}
	}

	public static JMenuItem getCopyItem(){
		JMenuItem menuItem = new Copy().getMenuItem();
		menuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
		return menuItem;
	}
	public static JMenuItem getCutItem(){
		JMenuItem menuItem = new Cut().getMenuItem();
		menuItem.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
		return menuItem;
	}
	public static JMenuItem getPasteItem(){
		JMenuItem menuItem = new Paste().getMenuItem();
		menuItem.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
		return menuItem;
	}

	public static void copyCutPast(ActionEvent e) {
		if (!(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			transferActionListener.actionPerformed(e);
		} else {
			MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
			if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
				mainLayoutCreator.getTimeSliderPanel().cut();
			} else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
				mainLayoutCreator.getTimeSliderPanel().copy();
			} else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
				mainLayoutCreator.getTimeSliderPanel().paste();
			}
		}
	}

	private static AbstractAction getAsAction(TextKey name, Consumer<ActionEvent> actionEventConsumer) {
		return new AbstractAction(name.toString()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionEventConsumer.accept(e);
			}
		};
	}
}
