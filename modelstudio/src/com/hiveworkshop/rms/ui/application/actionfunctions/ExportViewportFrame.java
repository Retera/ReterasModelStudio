package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewUgg;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.AnimatedPerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.ViewportRenderExporter;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ExportViewportFrame extends ActionFunction{
	private static FileDialog fileDialog = new FileDialog();

	public ExportViewportFrame(){
		super(TextKey.EXPORT_ANIMATED_PNG, () -> exportAnimatedFramePNG());
	}

	public static void exportAnimatedFramePNG1() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
//			PerspectiveViewport viewport = modelPanel.getPerspArea().getViewport();
			PerspectiveViewUgg modelDependentView = (PerspectiveViewUgg) WindowHandler2.getAllViews().stream().filter(v -> v instanceof PerspectiveViewUgg).findFirst().orElse(null);
			if(modelDependentView != null && modelDependentView.getPerspectiveViewport() != null){
				AnimatedPerspectiveViewport viewport = modelDependentView.getPerspectiveViewport();

				final BufferedImage fBufferedImage = ViewportRenderExporter.getBufferedImage(viewport);
				if (fBufferedImage != null) {
					fileDialog.onClickSaveAs(null, fBufferedImage, FileDialog.SAVE_TEXTURE, false);
				}
			}
		}
	}

	public static void exportAnimatedFramePNG() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DisplayViewUgg modelDependentView = (DisplayViewUgg) WindowHandler2.getAllViews().stream().filter(v -> v instanceof DisplayViewUgg).findFirst().orElse(null);
			if(modelDependentView != null && modelDependentView.getPerspectiveViewport() != null){
				AnimatedPerspectiveViewport viewport = modelDependentView.getPerspectiveViewport();
				viewport.setPixelBufferListener(b -> saveImage(b, viewport.getHeight(), viewport.getWidth()));
			}
		}
	}

	private static void saveImage(ByteBuffer pixels, int height, int width){
		final BufferedImage fBufferedImage = ViewportRenderExporter.getBufferedImage(pixels, height, width);
		if (fBufferedImage != null) {
			fileDialog.onClickSaveAs(null, fBufferedImage, FileDialog.SAVE_TEXTURE, false);
		}
	}
}
