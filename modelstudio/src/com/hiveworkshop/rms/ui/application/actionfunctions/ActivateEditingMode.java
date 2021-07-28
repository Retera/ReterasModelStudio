package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class ActivateEditingMode {

	private static class Translation extends ActionFunction {
		Translation(){
			super(TextKey.SET_TRANSL_MODE_ANIM, () -> setMode(ModelEditorActionType3.TRANSLATION), "W");
		}
	}

	private static class Scaling extends ActionFunction {
		Scaling(){
			super(TextKey.SET_SCALING_MODE_ANIM, () -> setMode(ModelEditorActionType3.SCALING), "E");
		}
	}

	private static class Rotation extends ActionFunction {
		Rotation(){
			super(TextKey.SET_ROTATE_MODE_ANIM, () -> setMode(ModelEditorActionType3.ROTATION), "R");
		}
	}

	private static class Extrude extends ActionFunction {
		Extrude(){
			super(TextKey.SET_EXTRUDE_MODE_ANIM, () -> setMode(ModelEditorActionType3.EXTRUDE), "T");
		}
	}

	private static class Extend extends ActionFunction {
		Extend(){
			super(TextKey.SET_EXTEND_MODE_ANIM, () -> setMode(ModelEditorActionType3.EXTEND), "Y");
		}
	}

	private static void setMode(ModelEditorActionType3 mode){
		if (!isTextField()) ProgramGlobals.setEditorActionTypeButton(mode);
	}

	public static JMenuItem getTranslationItem(){
		return new Translation().getMenuItem();
	}
	public static JMenuItem getScalingItem(){
		return new Scaling().getMenuItem();
	}
	public static JMenuItem getRotationItem(){
		return new Rotation().getMenuItem();
	}
	public static JMenuItem getExtrudeItem(){
		return new Extrude().getMenuItem();
	}
	public static JMenuItem getExtendItem(){
		return new Extend().getMenuItem();
	}

	private static boolean isTextField() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextComponent);
	}
}
