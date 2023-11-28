package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;

public class DrawBoneActivity extends DrawActivity {

	public DrawBoneActivity(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}

	public DrawBoneActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager,
	                        ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (MouseEventHelpers.matches(e, getModify(), getSnap())) {
			Vec2 point = getPoint(e);
			mouseStartPoint.set(point);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
			this.viewProjectionMatrix.set(viewProjectionMatrix);

			Bone bone = new Bone(getNumberName("Bone", modelView.getModel().getIdObjects()));
			Vec3 startPoint3d = get3DPoint(mouseStartPoint);
			bone.setPivotPoint(startPoint3d);

			List<UndoAction> undoActions = new ArrayList<>();
			AddNodeAction addNodeAction = new AddNodeAction(modelHandler.getModel(), bone, null);
			undoActions.add(addNodeAction);
			undoActions.add(new SetSelectionUggAction(new SelectionBundle(Collections.singleton(bone)), modelView, "Select Bone", null));
			UndoAction setupAction = new CompoundAction("Draw Bone", undoActions,  changeListener::nodesUpdated);
			transformAction = new DrawNodeAction("Draw Bone", startPoint3d, rotMat, bone, setupAction, null).doSetup();
		}
	}

	private static String getNumberName(String name, Collection<IdObject> idObjects) {
		Set<String> allBoneNames = idObjects.stream()
				.map(IdObject::getName)
				.filter(oName -> oName.toLowerCase().startsWith(name.toLowerCase()))
				.collect(Collectors.toSet());
		for (int i = 1; i <= allBoneNames.size()+1; i++) {
			String posName = name + String.format("%3s", i).replace(' ', '0');
			if (allBoneNames.contains(posName)) {
				return posName;
			}
		}

		return name;
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (transformAction != null) {
			transformAction.setTranslation(get3DPoint(getPoint(e)));
		}
	}

}
