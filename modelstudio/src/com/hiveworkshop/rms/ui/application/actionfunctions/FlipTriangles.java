package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.FlipTrianglesAction;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.Set;

public class FlipTriangles extends ActionFunction {
	public FlipTriangles(){
//		super(TextKey.FLIP_TRIANGLES, FlipTriangles::doFlipp, "control F");
		super(TextKey.FLIP_TRIANGLES, FlipTriangles::doFlip);
//		setMenuItemMnemonic(KeyEvent.VK_F);
	}

	public static void doFlip(ModelHandler modelHandler) {
		Set<Triangle> selectedTriangles = modelHandler.getModelView().getSelectedTriangles();
		if(2 <= selectedTriangles.size()){
			FlipTrianglesAction flipTrianglesAction = new FlipTrianglesAction(modelHandler.getModelView().getSelectedTriangles());
			if(0 < flipTrianglesAction.getNewTriCount()){
				modelHandler.getUndoManager().pushAction(flipTrianglesAction.redo());
			}
		}
	}
}
