package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.MergeGeosetsAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinimizeGeosets extends ActionFunction {
	public MinimizeGeosets(){
		super(TextKey.MINIMIZE_GEOSETS, () -> minimizeGeoset());

	}

	public static void minimizeGeoset() {
//		final int confirm = JOptionPane.showConfirmDialog(mainPanel,
//				"This is experimental and I did not code the Undo option for it yet. Continue?" +
//						"\nMy advice is to click cancel and save once first.",
//				"Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
//		if (confirm != JOptionPane.OK_OPTION) {
//			return;
//		}

		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		EditableModel model = modelPanel.getModel();
//		TempSaveModelStuff.doSavePreps(model);

		List<UndoAction> mergeActions = new ArrayList<>();
		Set<Geoset> geosetsToMerge = new HashSet<>();
		Set<Geoset> geosetsToKeep = new HashSet<>();

		for (Geoset geoset : model.getGeosets()) {
			for (Geoset retainedGeoset : geosetsToKeep) {
				if (retainedGeoset.getMaterial().equals(geoset.getMaterial())
						&& (retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup())
						&& (retainedGeoset.getUnselectable() == geoset.getUnselectable())
						&& isGeosetAnimationsMergable(retainedGeoset.getGeosetAnim(), geoset.getGeosetAnim())) {

					geosetsToMerge.add(geoset);
					mergeActions.add(new MergeGeosetsAction(retainedGeoset, geoset, modelPanel.getModelView(), null));
					break;
				}
			}
			if (!geosetsToMerge.contains(geoset)) {
				geosetsToKeep.add(geoset);
			}
		}

		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		UndoAction undoAction = new CompoundAction("Minimize Geosets", mergeActions, changeListener::geosetsUpdated);
		modelPanel.getUndoManager().pushAction(undoAction.redo());
	}

	private static boolean isGeosetAnimationsMergable(final GeosetAnim first, final GeosetAnim second) {
		if ((first == null) && (second == null)) {
			return true;
		}
		if ((first == null) || (second == null)) {
			return false;
		}
		final AnimFlag<?> firstVisibilityFlag = first.getVisibilityFlag();
		final AnimFlag<?> secondVisibilityFlag = second.getVisibilityFlag();
		if ((firstVisibilityFlag == null) != (secondVisibilityFlag == null)) {
			return false;
		}
		if ((firstVisibilityFlag != null) && !firstVisibilityFlag.equals(secondVisibilityFlag)) {
			return false;
		}
		if (first.isDropShadow() != second.isDropShadow()) {
			return false;
		}
		if (Math.abs(first.getStaticAlpha() - second.getStaticAlpha()) > 0.001) {
			return false;
		}
		if ((first.getStaticColor() == null) != (second.getStaticColor() == null)) {
			return false;
		}
		if ((first.getStaticColor() != null) && !first.getStaticColor().equalLocs(second.getStaticColor())) {
			return false;
		}
		final AnimFlag<?> firstAnimatedColor = first.find("Color");
		final AnimFlag<?> secondAnimatedColor = second.find("Color");
		if ((firstAnimatedColor == null) != (secondAnimatedColor == null)) {
			return false;
		}
		return (firstAnimatedColor == null) || firstAnimatedColor.equals(secondAnimatedColor);
	}
}
