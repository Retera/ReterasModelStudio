package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.language.TextKey;

public class File {
	private static final FileDialog fileDialog = new FileDialog();

	public static class Save extends ActionFunction{
		public Save(){
			super(TextKey.SAVE, () -> fileDialog.onClickSave(), "control S");
		}
	}

	public static class Open extends ActionFunction{
		public Open(){
			super(TextKey.OPEN, () -> fileDialog.onClickOpen(), "control O");
		}
	}


	public static class SaveAs extends ActionFunction{
		public SaveAs(){
			super(TextKey.SAVE_AS, () -> fileDialog.onClickSaveAs(), "control Q");
		}
	}
}
