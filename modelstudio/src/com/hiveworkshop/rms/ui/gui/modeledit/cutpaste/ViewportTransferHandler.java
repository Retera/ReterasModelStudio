package com.hiveworkshop.rms.ui.gui.modeledit.cutpaste;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddCameraAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.GeometryModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class ViewportTransferHandler extends TransferHandler {

	private static final String PLACEHOLDER_TAG = "_COPYPLACEHOLDER";
	/**
	 * Perform the actual data import.
	 */
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		String data;
		EditableModel pastedModel;
//		System.out.println("imorpt daga_______________________________________________________");

		// If we can't handle the import, bail now.
		if (!canImport(info)) {

//			System.out.println("cant Imp");
			return false;
		}
		Viewport viewport = null;
		PerspectiveViewport pv = null;
		DisplayPanel dp = null;
//		System.out.println("info.getComponent(): " + info.getComponent());
		if(info.getComponent() instanceof Viewport){
			viewport = (Viewport) info.getComponent();
//			System.out.println("found Viewport");
		}
		if(info.getComponent() instanceof PerspectiveViewport){
//			System.out.println("found PerspectiveViewport");
			pv = (PerspectiveViewport) info.getComponent();
		}
		if(info.getComponent() instanceof DisplayPanel){
//			System.out.println("found DisplayPanel");
			dp = (DisplayPanel) info.getComponent();
		}

		// Fetch the data -- bail if this fails
		try {
			data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
			pastedModel = MdxUtils.loadEditable(inputStream);
			inputStream.close();
//			System.out.println("done reading model_______________________________________________________");
		} catch (final UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
			return false;
		} catch (final IOException ioe) {
			System.out.println("importData: I/O exception");
			return false;
		}

		if(viewport != null){
			if (info.isDrop()) { // This is a drop
//				System.out.println("drop Viewport_______________________________________________________");
				Viewport.DropLocation dl = (Viewport.DropLocation) info.getDropLocation();
				Point dropPoint = dl.getDropPoint();
				pasteModelIntoViewport(pastedModel, viewport, dropPoint);
			} else { // This is a paste
//				System.out.println("paste Viewport_______________________________________________________");
				pasteModelIntoViewport(pastedModel, viewport, viewport.getLastMouseMotion());
			}
		}
		if(pv != null || dp != null){
			Point point = new Point(0,0);
			if (info.isDrop()) { // This is a drop
//				System.out.println("drop PerspectiveViewport_______________________________________________________");
				Viewport.DropLocation dl = (Viewport.DropLocation) info.getDropLocation();
				Point dropPoint = dl.getDropPoint();
				pasteModelIntoViewport(pastedModel, dropPoint);
			} else { // This is a paste

//				System.out.println("paste PerspectiveViewport_______________________________________________________");
				pasteModelIntoViewport(pastedModel, point);
			}
		}

//		System.out.println("done?_______________________________________________________");
		return true;
	}

	private void pasteModelIntoViewport(EditableModel pastedModel, Viewport viewport, Point dropPoint) {
		ModelHandler modelHandler = new ModelHandler(pastedModel);
		ModelView pastedModelView = modelHandler.getModelView();
		pastedModelView.setIdObjectsVisible(true);
		pastedModelView.setCamerasVisible(true);
		List<IdObject> idObjects = pastedModel.getIdObjects();
		for (IdObject object : idObjects) {
			pastedModelView.makeIdObjectEditable(object);
		}
		for (Camera object : pastedModel.getCameras()) {
			pastedModelView.makeCameraEditable(object);
		}
		GeometryModelEditor modelEditor = new GeometryModelEditor(new SelectionManager(modelHandler.getRenderModel(), pastedModelView, SelectionItemTypes.VERTEX), modelHandler, SelectionItemTypes.VERTEX);
		pastedModelView.selectAll();
		Double geomPoint = new Point2D.Double(viewport.getCoordinateSystem().geomX(dropPoint.x), viewport.getCoordinateSystem().geomY(dropPoint.y));
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(viewport.getCoordinateSystem().getPortFirstXYZ(), geomPoint.x);
		vertex.setCoord(viewport.getCoordinateSystem().getPortSecondXYZ(), geomPoint.y);
		modelEditor.setPosition(pastedModelView.getSelectionCenter(), vertex).redo();

		// this is the model they're actually working on
		ModelView currentModelView = viewport.getModelView();
		List<UndoAction> undoActions = new ArrayList<>();
		List<GeosetVertex> pastedVerts = new ArrayList<>();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.setParentModel(currentModelView.getModel());
			pastedVerts.addAll(pastedGeoset.getVertices());
			undoActions.add(new AddGeosetAction(pastedGeoset, currentModelView, null));
		}
		for (IdObject idObject : idObjects) {
			undoActions.add(new AddNodeAction(currentModelView.getModel(), idObject, null));
		}
		currentModelView.setSelectedIdObjects(idObjects);
		for (Camera idObject : pastedModel.getCameras()) {
			undoActions.add(new AddCameraAction(currentModelView.getModel(), idObject, null));
		}

		UndoAction pasteAction = new CompoundAction("Paste", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);

		SelectionBundle pastedSelection = new SelectionBundle(pastedVerts, pastedModel.getIdObjects(), pastedModel.getCameras());
		UndoAction selectPasted = new SetSelectionUggAction(pastedSelection, currentModelView, "select pasted");

		UndoManager undoManager = ProgramGlobals.getCurrentModelPanel().getModelHandler().getUndoManager();
		UndoAction pasteAndSelectAction = new CompoundAction("Paste", ModelStructureChangeListener.changeListener::geosetsUpdated, pasteAction, selectPasted);
		undoManager.pushAction(pasteAndSelectAction.redo());
	}
	private void pasteModelIntoViewport(EditableModel pastedModel, Point dropPoint) {
//		System.out.println("pasting model!");
		ModelHandler modelHandler = new ModelHandler(pastedModel);
		ModelView pastedModelView = modelHandler.getModelView();
		pastedModelView.setIdObjectsVisible(true);
		pastedModelView.setCamerasVisible(true);
		List<IdObject> idObjects = pastedModel.getIdObjects();
		for (IdObject object : idObjects) {
			pastedModelView.makeIdObjectEditable(object);
		}
		for (Camera object : pastedModel.getCameras()) {
			pastedModelView.makeCameraEditable(object);
		}
		GeometryModelEditor listener = new GeometryModelEditor(new SelectionManager(modelHandler.getRenderModel(), pastedModelView, SelectionItemTypes.VERTEX), modelHandler, SelectionItemTypes.VERTEX);
		pastedModelView.selectAll();
//		Double geomPoint = CoordSysUtils.geom(viewport.getCoordinateSystem(), dropPoint);
		Vec3 pasteCenter = new Vec3(0, 0, 0);
//		pasteCenter.setCoord(viewport.getCoordinateSystem().getPortFirstXYZ(), geomPoint.x);
//		pasteCenter.setCoord(viewport.getCoordinateSystem().getPortSecondXYZ(), geomPoint.y);
		listener.setPosition(pastedModelView.getSelectionCenter(), pasteCenter).redo();

		// this is the model they're actually working on
		ModelView currentModelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		List<UndoAction> undoActions = new ArrayList<>();
		Map<String, IdObject> placeHolderBones = new HashMap<>();
		Map<IdObject, IdObject> placeHolderBonesToModelBones = new HashMap<>();

		Set<IdObject> validIdObjects = new HashSet<>();
		for (IdObject idObject : idObjects) {
			if(!idObject.getName().endsWith(PLACEHOLDER_TAG)){
				undoActions.add(new AddNodeAction(currentModelView.getModel(), idObject, null));
				placeHolderBonesToModelBones.put(idObject, idObject);
				validIdObjects.add(idObject);
			} else {
				placeHolderBones.put(idObject.getName().replaceAll(PLACEHOLDER_TAG, ""), idObject);
			}
		}
		for(Bone bone : currentModelView.getModel().getBones()){
			if(placeHolderBones.containsKey(bone.getName())){
				placeHolderBonesToModelBones.put(placeHolderBones.get(bone.getName()), bone);
			}
		}

		List<GeosetVertex> pastedVerts = new ArrayList<>();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.setParentModel(currentModelView.getModel());
			for (GeosetVertex vertex : pastedGeoset.getVertices()){
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
			undoActions.add(new AddGeosetAction(pastedGeoset, currentModelView, null));
		}
//		currentModelView.setSelectedIdObjects(idObjects);
		for (Camera idObject : pastedModel.getCameras()) {
			undoActions.add(new AddCameraAction(currentModelView.getModel(), idObject, null));
		}

		UndoAction pasteAction = new CompoundAction("Paste", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated);

//		SelectionBundle pastedSelection = new SelectionBundle(pastedVerts, pastedModel.getIdObjects(), pastedModel.getCameras());
		SelectionBundle pastedSelection = new SelectionBundle(pastedVerts, validIdObjects, pastedModel.getCameras());
		UndoAction selectPasted = new SetSelectionUggAction(pastedSelection, currentModelView, "select pasted");

		UndoManager undoManager = ProgramGlobals.getCurrentModelPanel().getModelHandler().getUndoManager();
		UndoAction pasteAndSelectAction = new CompoundAction("Paste", ModelStructureChangeListener.changeListener::geosetsUpdated, pasteAction, selectPasted);
		undoManager.pushAction(pasteAndSelectAction.redo());
	}

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
//		System.out.println("createTransf_______________________________________________________");
//		Viewport viewport = (Viewport) c;
//		ModelView currentModelView = viewport.getModelView();
		ModelView currentModelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		EditableModel currentModel = currentModelView.getModel();

		EditableModel stringableModel = new EditableModel("CopyPastedModelData");
		stringableModel.setFormatVersion(currentModel.getFormatVersion());
		stringableModel.setExtents(currentModel.getExtents());

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
		dummyBone.getPivotPoint().scale(1f / count);

		String value = "";
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			final MdlxModel mdlx = TempSaveModelStuff.toMdlx(stringableModel);
			MdxUtils.saveMdl(mdlx, outputStream);
			value = outputStream.toString();
		} catch (final IOException e) {
			System.out.println("failed to create output stream from copied model");
			e.printStackTrace();
		}
		return new StringSelection(value);
	}



	public void applyVerticesToMatrices(Geoset geoset, EditableModel model) {
		geoset.getMatrices().clear();
		for (GeosetVertex vertex : geoset.getVertices()) {
			Matrix matrix = vertex.getMatrix();


			matrix.cureBones(model);
//			matrix.updateIds(model);
			if (!geoset.getMatrices().contains(matrix)) {
				geoset.getMatrices().add(matrix);
//				matrix.updateIds(model);
			}
//			vertex.setVertexGroup(geoset.getMatrices().indexOf(matrix));
//			vertex.setMatrix(matrix);
		}
	}

	private Vec3 fixVertBones(EditableModel stringableModel, Bone dummyBone, Geoset geoset) {
		Vec3 vertPosSum = new Vec3(0,0,0);
		for (GeosetVertex geosetVertex : geoset.getVertices()) {
			vertPosSum.add(geosetVertex);
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
	 * When the export is complete, remove the old list entry if the action was a
	 * move.
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
		Set<Camera> clonedCameras = new HashSet<>(modelView.getSelectedCameras());

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
		newGeoset.setAnims(geoset.getAnims());
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
						vertToCopiedVert.get(triangle.get(2)));
				newTriangle.setGeoset(newGeoset);
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
						if(nodesToClonedNodes.get(bone) != null){
							skinBone.setBone((Bone) nodesToClonedNodes.get(bone));
						}else {
							Bone copy = getPlaceholderBone(bone);
							nodesToClonedNodes.put(bone, copy);
						}
					}
				}
			} else if (!vertex.getBones().isEmpty()) {
				for (Bone bone : vertex.getBones()){
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