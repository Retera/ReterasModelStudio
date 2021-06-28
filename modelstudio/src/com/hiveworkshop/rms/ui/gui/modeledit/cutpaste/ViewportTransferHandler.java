package com.hiveworkshop.rms.ui.gui.modeledit.cutpaste;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
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
		String data = null;
		EditableModel pastedModel = null;

		// If we can't handle the import, bail now.
		if (!canImport(info)) {
			return false;
		}

		Viewport list = (Viewport) info.getComponent();
//		ModelView modelView = list.getModelView();
		// Fetch the data -- bail if this fails
		try {
			data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
			pastedModel = MdxUtils.loadEditable(new ByteArrayInputStream(data.getBytes()));
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
			pasteModelIntoViewport(pastedModel, list, dropPoint, list.getModelStructureChangeListener());
		} else { // This is a paste
			pasteModelIntoViewport(pastedModel, list, list.getLastMouseMotion(), list.getModelStructureChangeListener());
		}
		return true;
	}

	private void pasteModelIntoViewport(EditableModel pastedModel, Viewport viewport, Point dropPoint, ModelStructureChangeListener modelStructureChangeListener) {
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
		// ToDo needs access to modelView...
		final AbstractModelEditor listener = new AbstractModelEditor(new SelectionManager(pastedModelView, SelectionItemTypes.VERTEX), modelHandler, SelectionItemTypes.VERTEX);
		pastedModelView.selectAll();
		Double geomPoint = CoordSysUtils.geom(viewport.getCoordinateSystem(), dropPoint);
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(viewport.getCoordinateSystem().getPortFirstXYZ(), geomPoint.x);
		vertex.setCoord(viewport.getCoordinateSystem().getPortSecondXYZ(), geomPoint.y);
		listener.setPosition(pastedModelView.getSelectionCenter(), vertex);

		// this is the model they're actually working on
		ModelView currentModelView = viewport.getModelView();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			currentModelView.getModel().getGeosets().add(pastedGeoset);
		}
		for (IdObject idObject : idObjects) {
			currentModelView.getModel().add(idObject);
		}
		currentModelView.setSelectedIdObjects(idObjects);
		for (Camera idObject : pastedModel.getCameras()) {
			currentModelView.getModel().add(idObject);
		}
		currentModelView.setSelectedCameras(pastedModel.getCameras());
		currentModelView.setSelectedVertices(new HashSet<>());
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.applyVerticesToMatrices(currentModelView.getModel());
			currentModelView.addSelectedVertices(pastedGeoset.getVertices());
		}
		modelStructureChangeListener.geosetsUpdated();
	}

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		Viewport viewport = (Viewport) c;
		EditableModel stringableModel = new EditableModel("CopyPastedModelData");
		stringableModel.setFormatVersion(viewport.getModelView().getModel().getFormatVersion());
		stringableModel.setExtents(viewport.getModelView().getModel().getExtents());

		CopiedModelData copySelection = copySelection(viewport.getModelView());
		Bone dummyBone = new Bone("CopiedModelDummy");
		List<Vec3> verticesInNewMesh = new ArrayList<>();
		for (IdObject object : copySelection.getIdObjects()) {
			stringableModel.add(object);
			verticesInNewMesh.add(object.getPivotPoint());
		}
		for (Camera camera : copySelection.getCameras()) {
			stringableModel.add(camera);
			verticesInNewMesh.add(camera.getPosition());
		}
		for (Geoset geoset : copySelection.getGeosets()) {
			stringableModel.add(geoset);
			verticesInNewMesh.addAll(geoset.getVertices());
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				List<Bone> bones = geosetVertex.getBones();
				for (int i = bones.size() - 1; i >= 0; i--) {
					Bone bone = bones.get(i);
					if (!copySelection.getIdObjects().contains(bone)) {
						bones.remove(i);
					}
				}
				if (bones.isEmpty()) {
					bones.add(dummyBone);
					if (!stringableModel.contains(dummyBone)) {
						stringableModel.add(dummyBone);
					}
				}
			}
			geoset.applyVerticesToMatrices(stringableModel);
		}
		dummyBone.setPivotPoint(Vec3.centerOfGroup(verticesInNewMesh));
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			MdxUtils.saveMdx(stringableModel, outputStream);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] byteArray = outputStream.toByteArray();
		String value = new String(byteArray);
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
		if (action != MOVE) {
			return;
		}
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
		for (IdObject obj : nodesToClonedNodes.keySet()) {
			if (!nodesToClonedNodes.containsKey(obj.getParent())) {
				obj.setParent(null);
			} else {
				nodesToClonedNodes.get(obj).setParent(obj.getParent());
			}
		}
		Set<Camera> clonedCameras = new HashSet<>(modelView.getSelectedCameras());

		List<Geoset> copiedGeosets = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			if (geoset.getVertices().stream().anyMatch(modelView::isSelected)) {
				Geoset newGeoset = new Geoset();
				newGeoset.setSelectionGroup(geoset.getSelectionGroup());
				newGeoset.setAnims(geoset.getAnims());
				newGeoset.setMaterial(geoset.getMaterial());
				copiedGeosets.add(newGeoset);

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

				for (GeosetVertex vertex : newGeoset.getVertices()) {
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
		}

		return new CopiedModelData(copiedGeosets, nodesToClonedNodes.values(), clonedCameras);
	}

	private boolean triangleFullySelected(Triangle triangle, ModelView modelView) {
		return modelView.isSelected(triangle.get(0))
				&& modelView.isSelected(triangle.get(1))
				&& modelView.isSelected(triangle.get(2));
	}
}