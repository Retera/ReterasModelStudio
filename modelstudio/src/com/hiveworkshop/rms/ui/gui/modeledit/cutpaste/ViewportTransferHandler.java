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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		ModelHandler modelHandler = new ModelHandler(pastedModel, null);
		ModelView pastedModelView = modelHandler.getModelView();
		pastedModelView.setIdObjectsVisible(true);
		pastedModelView.setCamerasVisible(true);
		List<IdObject> idObjects = pastedModel.getIdObjects();
		for (IdObject object : idObjects) {
			pastedModelView.makeIdObjectVisible(object);
		}
		for (Camera object : pastedModel.getCameras()) {
			pastedModelView.makeCameraVisible(object);
		}
		// ToDo needs access to modelView...
		final AbstractModelEditor listener = new AbstractModelEditor(new SelectionManager(pastedModelView, SelectionItemTypes.VERTEX), viewport.getModelStructureChangeListener(), modelHandler, SelectionItemTypes.VERTEX);
		pastedModelView.selectAll();
		Double geomPoint = CoordSysUtils.geom(viewport.getCoordinateSystem(), dropPoint);
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(viewport.getCoordinateSystem().getPortFirstXYZ(), geomPoint.x);
		vertex.setCoord(viewport.getCoordinateSystem().getPortSecondXYZ(), geomPoint.y);
		listener.setPosition(pastedModelView.getSelectionCenter(), vertex);

		// this is the model they're actually working on
		ModelView currentModelView = viewport.getModelView();
		List<Geoset> geosetsAdded = new ArrayList<>();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			boolean foundMatch = false;
			for (Geoset currentModelGeoset : currentModelView.getModel().getGeosets()) {
				if (pastedGeoset.getMaterial().equals(currentModelGeoset.getMaterial())) {
					// matching materials
					for (Triangle triangle : pastedGeoset.getTriangles()) {
						currentModelGeoset.add(triangle);
						for (GeosetVertex geosetVertex : triangle.getAll()) {
							currentModelGeoset.add(geosetVertex);
						}
					}
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				currentModelView.getModel().getGeosets().add(pastedGeoset);
				geosetsAdded.add(pastedGeoset);
			}
		}
		for (IdObject idObject : idObjects) {
			currentModelView.getModel().add(idObject);
		}
		modelStructureChangeListener.nodesUpdated();
		for (Camera idObject : pastedModel.getCameras()) {
			currentModelView.getModel().add(idObject);
		}
		modelStructureChangeListener.camerasUpdated();
		for (Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.applyVerticesToMatrices(currentModelView.getModel());
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

//		CopiedModelData copySelection = viewport.getModelEditorManager().getModelEditor().copySelection();
		CopiedModelData copySelection = copySelection(viewport.getModelView());
		Bone dummyBone = new Bone("CopiedModelDummy");
		List<Vec3> verticesInNewMesh = new ArrayList<>();
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
		}
		for (IdObject object : copySelection.getIdObjects()) {
			stringableModel.add(object);
			verticesInNewMesh.add(object.getPivotPoint());
		}
		for (Camera camera : copySelection.getCameras()) {
			stringableModel.add(camera);
			verticesInNewMesh.add(camera.getPosition());
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
		List<Geoset> copiedGeosets = new ArrayList<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			Geoset copy = new Geoset();
			copy.setSelectionGroup(geoset.getSelectionGroup());
			copy.setAnims(geoset.getAnims());
			copy.setMaterial(geoset.getMaterial());
			Set<Triangle> copiedTriangles = new HashSet<>();
			Set<GeosetVertex> copiedVertices = new HashSet<>();
			for (Triangle triangle : geoset.getTriangles()) {
				boolean triangleIsFullySelected = true;
				List<GeosetVertex> triangleVertices = new ArrayList<>(3);
				for (GeosetVertex geosetVertex : triangle.getAll()) {
					if (modelView.isSelected(geosetVertex)) {
						GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.clearTriangles();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					} else {
						triangleIsFullySelected = false;
					}
				}
				if (triangleIsFullySelected) {
					Triangle newTriangle = new Triangle(triangleVertices.get(0), triangleVertices.get(1), triangleVertices.get(2), copy);
					copiedTriangles.add(newTriangle);
				}
			}
			for (Triangle triangle : copiedTriangles) {
				copy.add(triangle);
			}
			for (GeosetVertex geosetVertex : copiedVertices) {
				copy.add(geosetVertex);
			}
			if ((copiedTriangles.size() > 0) || (copiedVertices.size() > 0)) {
				copiedGeosets.add(copy);
			}
		}

		Set<IdObject> clonedNodes = new HashSet<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(b)) {
				clonedNodes.add(b.copy());
			}
		}
		for (IdObject obj : clonedNodes) {
			if (!clonedNodes.contains(obj.getParent())) {
				obj.setParent(null);
			}
		}
		Set<Camera> clonedCameras = new HashSet<>(modelView.getEditableCameras());

		return new CopiedModelData(copiedGeosets, clonedNodes, clonedCameras);
	}
}