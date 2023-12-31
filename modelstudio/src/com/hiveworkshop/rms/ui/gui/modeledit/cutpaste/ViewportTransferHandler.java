package com.hiveworkshop.rms.ui.gui.modeledit.cutpaste;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddCameraAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddTextureAnimAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.model.AddFaceEffectAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlLoadSave;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPanel;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

public class ViewportTransferHandler extends TransferHandler {

	private static final String PLACEHOLDER_TAG = "_COPYPLACEHOLDER";

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (canImport(info) && getViewPort(info) != null) {
			EditableModel pastedModel = getPastedModel(info);
			if (pastedModel != null) {
				Point point = new Point(0, 0);
				if (info.isDrop()) { // This is a drop
					DropLocation dl = info.getDropLocation();
					Point dropPoint = dl.getDropPoint();
					pasteModelIntoViewport(pastedModel, dropPoint);
				} else { // This is a paste
					pasteModelIntoViewport(pastedModel, point);
				}
				return true;
			}
		}

		return false;
	}

	private Component getViewPort(TransferSupport info) {
		Component viewPort = null;
		if (info.getComponent() instanceof PerspectiveViewport
				|| info.getComponent() instanceof DisplayPanel
				|| info.getComponent() instanceof ViewportPanel) {
			viewPort = info.getComponent();
		}
		return viewPort;
	}

	private EditableModel getPastedModel(TransferHandler.TransferSupport info) {
		try {
			String data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
			MdlxModel mdlxModel = MdxUtils.modelFrom(ByteBuffer.wrap(data.getBytes()));
			return TempOpenModelStuff.createEditableModel(mdlxModel);
		} catch (final UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
		} catch (final IOException ioe) {
			System.out.println("importData: I/O exception");
		}
		return null;
	}

	private void pasteModelIntoViewport(EditableModel pastedModel, Point dropPoint) {
		ModelHandler currModelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
		EditableModel currentModel = currModelHandler.getModel();
		List<UndoAction> undoActions = new ArrayList<>();
		Map<String, IdObject> placeHolderBones = new HashMap<>();
		Map<IdObject, IdObject> placeHolderBonesToModelBones = new HashMap<>();

		ArrayList<Sequence> allSequences = currentModel.getAllSequences();
		for (GlobalSeq globalSeq : pastedModel.getGlobalSeqs()) {
			if (!currentModel.contains(globalSeq)) {
				undoActions.add(new AddSequenceAction(currentModel, globalSeq.deepCopy(), null));
				allSequences.add(globalSeq);
			}
		}
		Map<Sequence, Sequence> sequenceMap = getSequenceMap(pastedModel.getAllSequences(), allSequences);
		ModelUtils.doForAnimFlags(pastedModel, a -> replaceAnimations(a, sequenceMap));

		Set<IdObject> validIdObjects = new HashSet<>();
		for (IdObject idObject : pastedModel.getIdObjects()) {
			if (!idObject.getName().endsWith(PLACEHOLDER_TAG)) {
				undoActions.add(new AddNodeAction(currentModel, idObject, null));
				placeHolderBonesToModelBones.put(idObject, idObject);
				validIdObjects.add(idObject);
			} else {
				placeHolderBones.put(idObject.getName().replaceAll(PLACEHOLDER_TAG, ""), idObject);
			}
		}
		for (Bone bone : currentModel.getBones()) {
			if (placeHolderBones.containsKey(bone.getName())) {
				placeHolderBonesToModelBones.put(placeHolderBones.get(bone.getName()), bone);
			}
		}

		List<GeosetVertex> pastedVerts = new ArrayList<>();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.setParentModel(currentModel);
			for (GeosetVertex vertex : pastedGeoset.getVertices()) {
				if (vertex.getSkinBones() != null) {
					for (SkinBone skinBone : vertex.getSkinBones()) {
						Bone bone = skinBone.getBone();
						if (bone != null && bone.getName().endsWith(PLACEHOLDER_TAG)) {
							skinBone.setBone((Bone) placeHolderBonesToModelBones.get(bone));
						}
					}
				} else if (!vertex.getBones().isEmpty()) {
					vertex.replaceBones(placeHolderBonesToModelBones);
				}
			}
			pastedVerts.addAll(pastedGeoset.getVertices());
			undoActions.add(new AddGeosetAction(pastedGeoset, currentModel, null));
		}

		for (Material material : pastedModel.getMaterials()) {
			if (!currentModel.contains(material)) {
				undoActions.add(new AddMaterialAction(material, currentModel, null));
			}
		}

		for (Bitmap bitmap : pastedModel.getTextures()) {
			if (!currentModel.contains(bitmap)) {
				undoActions.add(new AddBitmapAction(bitmap, currentModel, null));
			}
		}

		for (TextureAnim textureAnim : pastedModel.getTexAnims()) {
			if (!currentModel.contains(textureAnim)) {
				undoActions.add(new AddTextureAnimAction(textureAnim, currentModel, null));
			}
		}

		Set<CameraNode> cameraNodes = new HashSet<>();
		for (Camera idObject : pastedModel.getCameras()) {
			undoActions.add(new AddCameraAction(currentModel, idObject, null));
			cameraNodes.add(idObject.getSourceNode());
			cameraNodes.add(idObject.getTargetNode());
		}
		undoActions.add(new AddFaceEffectAction(pastedModel.getFaceEffects(), currentModel, null));


		UndoAction pasteAction = new CompoundAction("Paste", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);

		SelectionBundle pastedSelection = new SelectionBundle(pastedVerts, validIdObjects, cameraNodes);

		ModelView currentModelView = currModelHandler.getModelView();
		UndoAction selectPasted = new SetSelectionUggAction(pastedSelection, currentModelView, "select pasted", null);

		UndoManager undoManager = currModelHandler.getUndoManager();
		UndoAction pasteAndSelectAction = new CompoundAction("Paste", ModelStructureChangeListener.changeListener::geosetsUpdated, pasteAction, selectPasted);
		undoManager.pushAction(pasteAndSelectAction.redo());
	}

	private <Q> void replaceAnimations(AnimFlag<Q> animFlag, Map<Sequence, Sequence> sequenceMap) {
		for (Sequence sequence : sequenceMap.keySet()) {
			if (animFlag.getEntryMap(sequence) != null && sequenceMap.get(sequence) != null) {
//				System.out.println("replacing " + sequence + " with " + sequenceMap.get(sequence));
				TreeMap<Integer, Entry<Q>> entryMap = animFlag.getEntryMap(sequence);
				animFlag.deleteAnim(sequence);
				animFlag.setEntryMap(sequenceMap.get(sequence), entryMap);
			}
		}
	}

	private Map<Sequence, Sequence> getSequenceMap(List<Sequence> pastedModelSequences, List<Sequence> sequences) {
		Map<Sequence, Sequence> sequenceMap = new HashMap<>();
		for (Sequence sequence : sequences) {
			for (Sequence pSequence : pastedModelSequences) {
				if (pSequence instanceof Animation && sequence instanceof Animation) {
					if (getSeqCompName(pSequence).equals(getSeqCompName(sequence))) {
//						System.out.println(pSequence + " == " + sequence);
						sequenceMap.put(pSequence, sequence);
						break;
					}
				} else if (pSequence instanceof GlobalSeq
						&& sequence instanceof GlobalSeq
						&& pSequence.getLength() == sequence.getLength()) {
//					System.out.println(pSequence + " == " + sequence);
					sequenceMap.put(pSequence, sequence);
					break;
				}
			}
		}

		return sequenceMap;
	}

	private String getSeqCompName(Sequence sequence){
		return sequence.getName()
				.toUpperCase(Locale.US)
				.replaceAll("[^\\w\\d ]", "")
				.replaceAll(" +", " ");
	}

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		System.out.println("createTransf_______________________________________________________");

		ModelView currentModelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		EditableModel currentModel = currentModelView.getModel();

		EditableModel stringableModel = new EditableModel("CopyPastedModelData");
		stringableModel.setFormatVersion(currentModel.getFormatVersion());
		stringableModel.setExtents(currentModel.getExtents());


		for (Sequence sequence : currentModel.getAllSequences()) {
			// Need to be original instances to not mess timeline saving
			if (sequence instanceof Animation) {
				stringableModel.add((Animation) sequence);
			} else if (sequence instanceof GlobalSeq) {
				stringableModel.add((GlobalSeq) sequence);
			}
		}

		CopiedModelData copySelection = copySelection(currentModelView);

		Bone dummyBone = new Bone("CopiedModelDummy");
		int count = 0;

		count += copySelection.getIdObjects().size();
		for (IdObject object : copySelection.getIdObjects()) {
			stringableModel.add(object);
			dummyBone.getPivotPoint().add(object.getPivotPoint());
		}
		count += copySelection.getCameras().size();
		for (Camera camera : copySelection.getCameras()) {
			stringableModel.add(camera);
			dummyBone.getPivotPoint().add(camera.getPosition());
		}
		for (Geoset geoset : copySelection.getGeosets()) {
			stringableModel.add(geoset);
			stringableModel.add(geoset.getMaterial());
			count += geoset.getVertices().size();

			dummyBone.getPivotPoint().add(fixVertBones(stringableModel, dummyBone, geoset));
			applyVerticesToMatrices(geoset, stringableModel);
		}

		for (Material material : stringableModel.getMaterials()) {
			for (Layer layer : material.getLayers()) {
				if (layer.getTextureAnim() != null) {
					stringableModel.add(layer.getTextureAnim());
				}
			}
		}

		dummyBone.getPivotPoint().scale(1f / count);
		if (800 < currentModel.getFormatVersion()) {
			dummyBone.getBindPoseM4().translate(dummyBone.getPivotPoint());
		}

		final MdlxModel mdlx = TempSaveModelStuff.toMdlx(stringableModel, true);
		byte[] array = MdlLoadSave.saveMdl(mdlx).array();
		String value = new String(array);

		return new StringSelection(value);
	}



	public void applyVerticesToMatrices(Geoset geoset, EditableModel model) {
		geoset.clearMatrices();
		for (GeosetVertex vertex : geoset.getVertices()) {
			vertex.getMatrix().cureBones(model.getBones());
		}
		geoset.reMakeMatrixList();
	}

	private Vec3 fixVertBones(EditableModel stringableModel, Bone dummyBone, Geoset geoset) {
		Vec3 vertPosSum = new Vec3(0,0,0);
		for (GeosetVertex geosetVertex : geoset.getVertices()) {
			vertPosSum.add(geosetVertex);
			if (geosetVertex.getSkinBones() != null) {
				for (SkinBone skinBone : geosetVertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && !stringableModel.contains(skinBone.getBone())) {
						skinBone.setBone(dummyBone);
					}
				}
			} else {
				List<Bone> bones = geosetVertex.getBones();
				for (int i = bones.size() - 1; i >= 0; i--) {
					Bone bone = bones.get(i);
					if (!stringableModel.contains(bone)) {
						geosetVertex.removeBone(bone);
					}
				}
				if (geosetVertex.getMatrix().isEmpty()) {
					if (!stringableModel.contains(dummyBone)) {
						stringableModel.add(dummyBone);
					}
					geosetVertex.addBoneAttachment(dummyBone);
				}
			}
		}
		return vertPosSum;
	}

	/**
	 * The list handles both copy and move actions.
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	 * When the export is complete, remove the old list entry if the action was a move.
	 */
	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
//		System.out.println("expDone_______________________________________________________");
//		if (action != MOVE) {
//			return;
//		}
		// JList list = (JList) c;
		// DefaultListModel model = (DefaultListModel) list.getModel();
		// int index = list.getSelectedIndex();
		// model.remove(index);
	}

	/**
	 * We only support importing strings.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		// we only import Strings
//		System.out.println("canImp_______________________________________________________");
		return support.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	public CopiedModelData copySelection(ModelView modelView) {
		Map<IdObject, IdObject> nodesToClonedNodes = new HashMap<>();
		for (IdObject b : modelView.getSelectedIdObjects()) {
			nodesToClonedNodes.put(b, b.copy());
		}
		for (IdObject obj : nodesToClonedNodes.values()) {
			obj.setParent(nodesToClonedNodes.getOrDefault(obj.getParent(), null));
		}
		Set<Camera> clonedCameras = new HashSet<>();
		for (Camera camera : modelView.getSelectedCameras()) {
			clonedCameras.add(camera.deepCopy());
		}

		List<Geoset> copiedGeosets = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			if (geoset.getVertices().stream().anyMatch(modelView::isSelected)) {
				Geoset newGeoset = copySelectedFromGeoset(modelView, geoset);
				copiedGeosets.add(newGeoset);
				replaceBonesWithNewBones(nodesToClonedNodes, newGeoset.getVertices());
			}
		}

		return new CopiedModelData(copiedGeosets, nodesToClonedNodes.values(), clonedCameras);
	}

	private Geoset copySelectedFromGeoset(ModelView modelView, Geoset geoset) {
		Geoset newGeoset = new Geoset();
		newGeoset.setSelectionGroup(geoset.getSelectionGroup());
		for (Animation anim : geoset.getAnimExts().keySet()) {
			newGeoset.add(anim, geoset.getAnimExtent(anim).deepCopy());
		}
		newGeoset.setMaterial(geoset.getMaterial());

		Map<GeosetVertex, GeosetVertex> vertToCopiedVert = new HashMap<>();
		for (GeosetVertex vertex : geoset.getVertices()) {
			if (modelView.isSelected(vertex)) {
				GeosetVertex newVertex = vertex.deepCopy();
				newVertex.clearTriangles();
				newVertex.setGeoset(newGeoset);
				vertToCopiedVert.put(vertex, newVertex);
				newGeoset.add(newVertex);
			}
		}
		for (Triangle triangle : geoset.getTriangles()) {
			if (triangleFullySelected(triangle, modelView)) {
				Triangle newTriangle = new Triangle(
						vertToCopiedVert.get(triangle.get(0)),
						vertToCopiedVert.get(triangle.get(1)),
						vertToCopiedVert.get(triangle.get(2)),
						newGeoset).addToVerts();
				newGeoset.add(newTriangle);
			}
		}
		return newGeoset;
	}

	private void replaceBonesWithNewBones(Map<IdObject, IdObject> nodesToClonedNodes, List<GeosetVertex> vertices) {
		for (GeosetVertex vertex : vertices) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					Bone bone = skinBone.getBone();
					if (bone != null) {
						if (nodesToClonedNodes.get(bone) != null) {
							skinBone.setBone((Bone) nodesToClonedNodes.get(bone));
						} else {
							Bone copy = getPlaceholderBone(bone);
							nodesToClonedNodes.put(bone, copy);
						}
					}
				}
			} else if (!vertex.getBones().isEmpty()) {
				for (Bone bone : vertex.getBones()) {
					nodesToClonedNodes.computeIfAbsent(bone, k -> getPlaceholderBone(bone));
				}
				vertex.replaceBones(nodesToClonedNodes);
			}
		}
	}

	private Bone getPlaceholderBone(Bone bone) {
		Bone copy = bone.copy();
		copy.setName(copy.getName() + PLACEHOLDER_TAG);
		return copy;
	}

	private boolean triangleFullySelected(Triangle triangle, ModelView modelView) {
		return modelView.isSelected(triangle.get(0))
				&& modelView.isSelected(triangle.get(1))
				&& modelView.isSelected(triangle.get(2));
	}
}