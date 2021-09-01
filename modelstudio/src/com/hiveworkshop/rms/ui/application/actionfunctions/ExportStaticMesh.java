package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportStaticMesh extends ActionFunction {
	public ExportStaticMesh(){
		super(TextKey.EXPORT_STATIC_MESH, () -> exportAnimatedToStaticMesh());
		setMenuItemMnemonic(KeyEvent.VK_X);
	}

	public static void exportAnimatedToStaticMesh() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (!(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			JOptionPane.showMessageDialog(mainPanel, "You must be in the Animation Editor to use that!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ModelPanel modelContext = ProgramGlobals.getCurrentModelPanel();
		RenderModel editorRenderModel = modelContext.getEditorRenderModel();
		EditableModel model = modelContext.getModel();

		TimeEnvironmentImpl renderEnv = editorRenderModel.getTimeEnvironment();
		Sequence currentAnimation = renderEnv.getCurrentSequence();

		String s = "At" + renderEnv.getAnimationTime();
		System.out.println(currentAnimation);
		if (currentAnimation != null) {
			if(currentAnimation instanceof Animation){
				s = ((Animation) currentAnimation).getName() + s;
			} else {
				s = "GlobalSeq" + model.getGlobalSeqId((GlobalSeq) currentAnimation) + s;
			}
		}
		EditableModel frozenModel = TempStuffFromEditableModel.deepClone(model, model.getHeaderName() + s);
		if (frozenModel.getFileRef() != null) {
			frozenModel.setFileRef(new File(frozenModel.getFileRef().getPath().replaceFirst("(?<=\\w)\\.(?=md[lx])", s + ".")));
		}

		for (int geosetIndex = 0; geosetIndex < frozenModel.getGeosets().size(); geosetIndex++) {
			Geoset geoset = model.getGeoset(geosetIndex);
			Geoset frozenGeoset = frozenModel.getGeoset(geosetIndex);

			for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
				GeosetVertex vertex = geoset.getVertex(vertexIndex);
				GeosetVertex frozenVertex = frozenGeoset.getVertex(vertexIndex);
				Mat4 skinBonesMatrixSumHeap = ModelUtils.processBones(editorRenderModel, vertex, geoset);
				Vec4 vertexSumHeap = Vec4.getTransformed(new Vec4(vertex, 1), skinBonesMatrixSumHeap);
				frozenVertex.set(vertexSumHeap);
				if (vertex.getNormal() != null) {
					Vec4 normalSumHeap = Vec4.getTransformed(new Vec4(vertex.getNormal(), 0), skinBonesMatrixSumHeap);
					normalSumHeap.normalize();
					frozenVertex.getNormal().set(normalSumHeap);
				}
			}
		}
		frozenModel.clearAllIdObjects();
		Bone boneRoot = new Bone("Bone_Root");
		boneRoot.setPivotPoint(new Vec3(0, 0, 0));
		frozenModel.add(boneRoot);

		for (Geoset geoset : frozenModel.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (vertex.getSkinBones() != null) {
					vertex.setSkinBones(new Bone[] {boneRoot, null, null, null}, new short[] {255, 0, 0, 0});
				} else {
					vertex.clearBoneAttachments();
					vertex.addBoneAttachment(boneRoot);
				}
			}
		}

		List<Geoset> geosetsToRemove = new ArrayList<>();
		for (Geoset geoset : frozenModel.getGeosets()) {
			GeosetAnim geosetAnim = geoset.getGeosetAnim();

			if (geosetAnim != null && geosetAnim.getVisibilityFlag() != null) {
				Float visibilityValue = geosetAnim.getVisibilityFlag().interpolateAt(renderEnv);

				if (visibilityValue != null) {
					double visValue = visibilityValue;

					if (visValue < 0.01) {
						geosetsToRemove.add(geoset);
						frozenModel.remove(geosetAnim);
					}
				}
			}
		}

		for (Geoset geoset : geosetsToRemove) {
			frozenModel.remove(geoset);
		}

		frozenModel.getAnims().clear();
		Animation stand = new Animation("Stand", 333, 1333);
		frozenModel.add(stand);
		List<AnimFlag<?>> allAnimFlags = frozenModel.getAllAnimFlags();
		for (AnimFlag<?> flag : allAnimFlags) {
			if (!flag.hasGlobalSeq() && flag.size() > 0) {
				addFlagEntry(renderEnv, stand, flag);
			}
		}

		FileDialog fileDialog = new FileDialog();
		fileDialog.onClickSaveAs(frozenModel, FileDialog.SAVE_MODEL, false);
	}

	private static <T> void addFlagEntry(TimeEnvironmentImpl renderEnv, Animation stand, AnimFlag<T> flag) {
		T value = flag.interpolateAt(renderEnv);
		flag.setInterpType(InterpolationType.DONT_INTERP);
		flag.clear();
		flag.addEntry(0, value, stand);
	}
}
