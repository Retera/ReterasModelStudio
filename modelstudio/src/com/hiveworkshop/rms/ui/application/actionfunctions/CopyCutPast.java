package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.animation.KeyframeHandler;
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
//			super(TextKey.COPY, getAsAction(TextKey.COPY, (e) -> copyCutPast(e)));
			super(TextKey.COPY, getAsAction(TextKey.COPY, (e) -> copyCutPast(TextKey.COPY)));
//			super(TextKey.COPY, () -> TransferHandler.getCutAction());
			setKeyStroke("control C");
		}
	}
	private static class Cut extends ActionFunction {
		Cut(){
//			super(TextKey.CUT, getAsAction(TextKey.CUT, (e) -> copyCutPast(e)));
			super(TextKey.CUT, getAsAction(TextKey.CUT, (e) -> copyCutPast(TextKey.CUT)));
//			super(TextKey.CUT, () -> TransferHandler.getCopyAction());
			setKeyStroke("control X");
		}
	}
	private static class Paste extends ActionFunction {
		Paste(){
//			super(TextKey.PASTE, getAsAction(TextKey.PASTE, (e) -> copyCutPast(e)));
			super(TextKey.PASTE, getAsAction(TextKey.PASTE, (e) -> copyCutPast(TextKey.PASTE)));
//			super(TextKey.PASTE, () -> TransferHandler.getPasteAction());
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
			System.out.println("copyCutPaste!");
			transferActionListener.actionPerformed(e);
		} else {
//			MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//			KeyframeHandler keyframeHandler = mainLayoutCreator.getTimeSliderView().getTimeSliderPanel().getKeyframeHandler();
			WindowHandler2 windowHandler2 = ProgramGlobals.getRootWindowUgg().getWindowHandler2();
			KeyframeHandler keyframeHandler = windowHandler2.getTimeSliderView().getTimeSliderPanel().getKeyframeHandler();
			if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
				keyframeHandler.cut();
			} else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
				keyframeHandler.copy();
			} else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
				keyframeHandler.paste();
			}
		}
	}
	public static void copyCutPast(TextKey textKey) {
		if (!(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			System.out.println("copyCutPaste!");
			transferActionListener.doActionPerformed(textKey);
		} else {
//			MainLayoutCreator mainLayoutCreator = ProgramGlobals.getMainPanel().getMainLayoutCreator();
//			KeyframeHandler keyframeHandler = mainLayoutCreator.getTimeSliderView().getTimeSliderPanel().getKeyframeHandler();
			WindowHandler2 windowHandler2 = ProgramGlobals.getRootWindowUgg().getWindowHandler2();
			KeyframeHandler keyframeHandler = windowHandler2.getTimeSliderView().getTimeSliderPanel().getKeyframeHandler();
//			if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
//				keyframeHandler.cut();
//			} else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
//				keyframeHandler.copy();
//			} else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
//				keyframeHandler.paste();
//			}
		}
	}

//	private void ugg(){
//
//		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
//		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
//		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
//	}

	private static AbstractAction getAsAction(TextKey name, Consumer<ActionEvent> actionEventConsumer) {
		return new AbstractAction(name.toString()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionEventConsumer.accept(e);
			}
		};
	}
}
