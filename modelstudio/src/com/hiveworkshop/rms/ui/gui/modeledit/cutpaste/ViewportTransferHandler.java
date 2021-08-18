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
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
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
import java.awt.geom.Point2D.Double;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class ViewportTransferHandler extends TransferHandler {

	/**
	 * Perform the actual data import.
	 */
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		String data;
		EditableModel pastedModel;

		// If we can't handle the import, bail now.
		if (!canImport(info)) {
			return false;
		}

		Viewport viewport = (Viewport) info.getComponent();
		// Fetch the data -- bail if this fails
		try {
			data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
			pastedModel = MdxUtils.loadEditable(inputStream);
			inputStream.close();
		} catch (final UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
			return false;
		} catch (final IOException ioe) {
			System.out.println("importData: I/O exception");
			return false;
		}

		if (info.isDrop()) { // This is a drop
			Viewport.DropLocation dl = (Viewport.DropLocation) info.getDropLocation();
			Point dropPoint = dl.getDropPoint();
			pasteModelIntoViewport(pastedModel, viewport, dropPoint);
		} else { // This is a paste
			pasteModelIntoViewport(pastedModel, viewport, viewport.getLastMouseMotion());
		}
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
		GeometryModelEditor listener = new GeometryModelEditor(new SelectionManager(modelHandler.getRenderModel(), pastedModelView, SelectionItemTypes.VERTEX), modelHandler, SelectionItemTypes.VERTEX);
		pastedModelView.selectAll();
		Double geomPoint = CoordSysUtils.geom(viewport.getCoordinateSystem(), dropPoint);
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(viewport.getCoordinateSystem().getPortFirstXYZ(), geomPoint.x);
		vertex.setCoord(viewport.getCoordinateSystem().getPortSecondXYZ(), geomPoint.y);
		listener.setPosition(pastedModelView.getSelectionCenter(), vertex);

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

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		Viewport viewport = (Viewport) c;
		ModelView currentModelView = viewport.getModelView();
		EditableModel currentModel = currentModelView.getModel();

		EditableModel stringableModel = new EditableModel("CopyPastedModelData");
		stringableModel.setFormatVersion(currentModel.getFormatVersion());
		stringableModel.setExtents(currentModel.getExtents());

		CopiedModelData copySelection = copySelection(currentModelView);

		Bone dummyBone = null;
		Vec3 dummyPivot = new Vec3(0, 0, 0);
		int count = 0;

		for (IdObject object : copySelection.getIdObjects()) {
			stringableModel.add(object);
			dummyPivot.add(object.getPivotPoint());
			count++;
		}
		for (Camera camera : copySelection.getCameras()) {
			stringableModel.add(camera);
			dummyPivot.add(camera.getPosition());
			count++;
		}
		for (Geoset geoset : copySelection.getGeosets()) {
			stringableModel.add(geoset);
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				dummyPivot.add(geosetVertex);
				count++;
				List<Bone> bones = geosetVertex.getBones();
				for (int i = bones.size() - 1; i >= 0; i--) {
					Bone bone = bones.get(i);
					if (!copySelection.getIdObjects().contains(bone)) {
						geosetVertex.removeBone(bone);
					}
				}
				if (geosetVertex.getMatrix().isEmpty()) {
					if (dummyBone == null) {
						dummyBone = new Bone("CopiedModelDummy");
						stringableModel.add(dummyBone);
					}
					geosetVertex.addBoneAttachment(dummyBone);
				}
			}
			geoset.applyVerticesToMatrices(stringableModel);
		}
		if (dummyBone != null) {
			dummyPivot.scale(1f / count);
			dummyBone.setPivotPoint(dummyPivot);
		}

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
				GeosetVertex newVertex = new GeosetVertex(vertex);
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
				for (GeosetVertex.SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone.getBone() != null) {
						skinBone.setBone((Bone) nodesToClonedNodes.get(skinBone.getBone()));
					}
				}
			} else if (!vertex.getBones().isEmpty()) {
				vertex.replaceBones(nodesToClonedNodes);
			}
		}
	}

	private boolean triangleFullySelected(Triangle triangle, ModelView modelView) {
		return modelView.isSelected(triangle.get(0))
				&& modelView.isSelected(triangle.get(1))
				&& modelView.isSelected(triangle.get(2));
	}
}