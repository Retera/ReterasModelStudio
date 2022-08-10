package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewCanvas;
import com.hiveworkshop.rms.ui.application.viewer.ViewportRenderExporter;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ExportViewportFrame extends ActionFunction{
	private static FileDialog fileDialog = new FileDialog();

	public ExportViewportFrame(){
		super(TextKey.EXPORT_ANIMATED_PNG, ExportViewportFrame::exportAnimatedFramePNG);
	}

	public static void exportAnimatedFramePNG() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DisplayViewCanvas modelDependentView = (DisplayViewCanvas) WindowHandler2.getAllViews().stream().filter(v -> v instanceof DisplayViewCanvas).findFirst().orElse(null);
			if(modelDependentView != null && modelDependentView.getPerspectiveViewport() != null){
				String modelName = modelPanel.getModel().getName();
				Sequence sequence = modelPanel.getModelHandler().getRenderModel().getTimeEnvironment().getCurrentSequence();
				String seqName = sequence == null ? "" : "_" + sequence;
				String suggestedName = modelName + seqName;

				ViewportCanvas viewport = modelDependentView.getPerspectiveViewport();
				viewport.setPixelBufferListener(b -> saveImage(b, suggestedName, viewport.getHeight(), viewport.getWidth()));
			}
		}
	}

	private static void saveImage(ByteBuffer pixels, String name, int height, int width){
		final BufferedImage fBufferedImage = ViewportRenderExporter.getBufferedImage(pixels, height, width);
		ExportTexture.onClickSaveAs(fBufferedImage, name, FileDialog.SAVE_TEXTURE, fileDialog, ProgramGlobals.getMainPanel());
	}
}
