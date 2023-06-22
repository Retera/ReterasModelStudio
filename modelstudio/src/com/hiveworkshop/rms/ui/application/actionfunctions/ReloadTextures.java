package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ReloadTextures extends ActionFunction {

	public ReloadTextures() {
		super(TextKey.RELOAD_TEXTURES, ReloadTextures::doStuff);
	}

	public static void doStuff(ModelHandler modelHandler){
		BLPHandler.get().dropCache();
		modelHandler.getRenderModel().getBufferFiller().clearTextureMap();
		modelHandler.getPreviewRenderModel().getBufferFiller().clearTextureMap();
	}
}
