package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.selection.HideVerticesAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowVerticesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class ShowHideStuff {
	private static class HideVerts extends ActionFunction{
		public HideVerts() {
			super(TextKey.HIDE_VERTICES, HideVerts::doHide);

		}
		private static void doHide(ModelHandler modelHandler){
			modelHandler.getUndoManager().pushAction(new HideVerticesAction(modelHandler.getModelView(), null).redo());
		}
	}

	private static class ShowVerts extends ActionFunction{
		public ShowVerts() {
			super(TextKey.UNHIDE_ALL_VERTICES, ShowVerts::doShow);

		}
		private static void doShow(ModelHandler modelHandler){
			modelHandler.getUndoManager().pushAction(new ShowVerticesAction(modelHandler.getModelView(), null).redo());

		}
	}

	public static JMenuItem getHideVertsMenuItem(){
		return new HideVerts().getMenuItem();
	}
	public static JMenuItem getShowVertsMenuItem(){
		return new ShowVerts().getMenuItem();
	}

}
