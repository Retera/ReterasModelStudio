package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.TimeSliderView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class TimeSkip {
	private static class Play extends ActionFunction {
		Play(){
			super(TextKey.PLAY_ACTION, () -> playAnimation(), "control SPACE");
		}
	}
	private static class Ffw1 extends ActionFunction {
		Ffw1(){
			super(TextKey.JUMP_ONE_FRAME_FW, () -> jumpFrames(1), "UP");
		}
	}
	private static class Ffw10 extends ActionFunction {
		Ffw10(){
			super(TextKey.JUMP_TEN_FRAMES_FW, () -> jumpFrames(10), "shift UP");
		}
	}
	private static class Bbw1 extends ActionFunction {
		Bbw1(){
			super(TextKey.JUMP_ONE_FRAME_BW, () -> jumpFrames(-1), "DOWN");
		}
	}
	private static class Bbw10 extends ActionFunction {
		Bbw10(){
			super(TextKey.JUMP_TEN_FRAMES_BW, () -> jumpFrames(-10), "shift DOWN");
		}
	}
	private static class NextKF extends ActionFunction {
		NextKF(){
			super(TextKey.NEXT_KEYFRAME, () -> nextKeyframe(), "RIGHT");
		}
	}

	private static class PrevKF extends ActionFunction {
		PrevKF() {
			super(TextKey.PREV_KEYFRAME, () -> previousKeyframe(), "LEFT");
		}
	}


	public static JMenuItem getPlayItem() {
		return new Play().getMenuItem();
	}

	public static JMenuItem getFfw1Item() {
		return new Ffw1().getMenuItem();
	}

	public static JMenuItem getFfw10Item() {
		return new Ffw10().getMenuItem();
	}

	public static JMenuItem getBbw1Item() {
		return new Bbw1().getMenuItem();
	}

	public static JMenuItem getBbw10Item() {
		return new Bbw10().getMenuItem();
	}

	public static JMenuItem getNextKFItem() {
		return new NextKF().getMenuItem();
	}

	public static JMenuItem getPrevKFItem() {
		return new PrevKF().getMenuItem();
	}


	public static void playAnimation() {
		if (!isTextField()) getTimeSliderView().getTimeSliderPanel().play();
	}

	private static TimeSliderView getTimeSliderView() {
//		return ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderView();
		return ProgramGlobals.getRootWindowUgg().getWindowHandler2().getTimeSliderView();
	}

	public static void previousKeyframe() {
		if (!isTextField() && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			getTimeSliderView().getTimeSliderPanel().jumpToPreviousTime();
		}
	}

	public static void nextKeyframe() {
		if (!isTextField() && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)
			getTimeSliderView().getTimeSliderPanel().jumpToNextTime();
	}



	public static void jumpFrames(int i) {
		if (!isTextField() && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
//			getTimeSliderView().getTimeSliderPanel().jumpFrames(i);
			getTimeSliderView().getTimeSliderPanel().timeStep(i);
		}
	}



	private static boolean isTextField() {
		return focusedComponentNeedsTyping(getFocusedComponent());
	}

	private boolean isTextField2() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextArea)
				|| (focusedComponent instanceof JTextField)
				|| (focusedComponent instanceof JTextPane);
	}

	private static boolean focusedComponentNeedsTyping(final Component focusedComponent) {
//		return (focusedComponent instanceof JTextArea) || (focusedComponent instanceof JTextField) || (focusedComponent instanceof JTextPane);
		return (focusedComponent instanceof JTextComponent);
	}

	private static Component getFocusedComponent() {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		return kfm.getFocusOwner();
	}
}
