package com.hiveworkshop.wc3.gui.modeledit.cutpaste;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D.Double;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.Viewport;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorNotifier;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.PivotPointModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.PivotPointSelectionManager;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class ViewportTransferHandler extends TransferHandler {

	/**
	 * Perform the actual data import.
	 */
	@Override
	public boolean importData(final TransferHandler.TransferSupport info) {
		String data = null;
		MDL pastedModel = null;

		// If we can't handle the import, bail now.
		if (!canImport(info)) {
			return false;
		}

		final Viewport list = (Viewport) info.getComponent();
		final ModelView modelView = list.getModelView();
		// Fetch the data -- bail if this fails
		try {
			data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
			pastedModel = MDL.read(new ByteArrayInputStream(data.getBytes()));
		} catch (final UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
			return false;
		} catch (final IOException ioe) {
			System.out.println("importData: I/O exception");
			return false;
		}

		if (info.isDrop()) { // This is a drop
			final Viewport.DropLocation dl = (Viewport.DropLocation) info.getDropLocation();
			final Point dropPoint = dl.getDropPoint();
			pasteModelIntoViewport(pastedModel, list, dropPoint, list.getModelStructureChangeListener());
			return true;
		} else { // This is a paste
			pasteModelIntoViewport(pastedModel, list, list.getLastMouseMotion(),
					list.getModelStructureChangeListener());
			return true;
		}
	}

	private void pasteModelIntoViewport(final MDL pastedModel, final Viewport viewport, final Point dropPoint,
			final ModelStructureChangeListener modelStructureChangeListener) {
		final ModelViewManager pastedModelView = new ModelViewManager(pastedModel);
		for (final IdObject object : pastedModel.getIdObjects()) {
			pastedModelView.makeIdObjectVisible(object);
		}
		for (final Camera object : pastedModel.getCameras()) {
			pastedModelView.makeCameraVisible(object);
		}
		final ModelEditorNotifier modelEditorNotifier = new ModelEditorNotifier();
		modelEditorNotifier.subscribe(new GeosetVertexModelEditor(pastedModelView, null,
				new GeosetVertexSelectionManager(), viewport.getModelStructureChangeListener()));
		modelEditorNotifier.subscribe(new PivotPointModelEditor(pastedModelView, null, new PivotPointSelectionManager(),
				viewport.getModelStructureChangeListener()));
		modelEditorNotifier.selectAll();
		final Double geomPoint = CoordinateSystem.Util.geom(viewport, dropPoint);
		final Vertex vertex = new Vertex(0, 0, 0);
		vertex.setCoord(viewport.getPortFirstXYZ(), geomPoint.x);
		vertex.setCoord(viewport.getPortSecondXYZ(), geomPoint.y);
		modelEditorNotifier.setPosition(modelEditorNotifier.getSelectionCenter(), vertex.x, vertex.y, vertex.z);

		// this is the model they're actually working on
		final ModelView currentModelView = viewport.getModelView();
		final List<Geoset> geosetsAdded = new ArrayList<>();
		for (final Geoset pastedGeoset : pastedModel.getGeosets()) {
			boolean foundMatch = false;
			for (final Geoset currentModelGeoset : currentModelView.getModel().getGeosets()) {
				if (pastedGeoset.getMaterial().equals(currentModelGeoset.getMaterial())) {
					// matching materials
					for (final Triangle triangle : pastedGeoset.getTriangles()) {
						currentModelGeoset.add(triangle);
						for (final GeosetVertex geosetVertex : triangle.getAll()) {
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
		modelStructureChangeListener.geosetsAdded(geosetsAdded);
		for (final IdObject idObject : pastedModel.getIdObjects()) {
			currentModelView.getModel().add(idObject);
		}
		modelStructureChangeListener.nodesAdded(pastedModel.getIdObjects());
		for (final Camera idObject : pastedModel.getCameras()) {
			currentModelView.getModel().add(idObject);
		}
		modelStructureChangeListener.camerasAdded(pastedModel.getCameras());
		for (final Geoset pastedGeoset : pastedModel.getGeosets()) {
			pastedGeoset.applyVerticesToMatrices(currentModelView.getModel());
		}
	}

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(final JComponent c) {
		final Viewport viewport = (Viewport) c;
		final MDL stringableModel = new MDL("CopyPastedModelData");
		final CopiedModelData copySelection = viewport.getModelEditor().copySelection();
		final Bone dummyBone = new Bone("CopiedModelDummy");
		final List<Vertex> verticesInNewMesh = new ArrayList<>();
		for (final Geoset geoset : copySelection.getGeosets()) {
			stringableModel.add(geoset);
			verticesInNewMesh.addAll(geoset.getVertices());
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				final List<Bone> bones = geosetVertex.getBones();
				for (int i = bones.size() - 1; i >= 0; i--) {
					final Bone bone = bones.get(i);
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
		for (final IdObject object : copySelection.getIdObjects()) {
			stringableModel.add(object);
			verticesInNewMesh.add(object.getPivotPoint());
		}
		for (final Camera camera : copySelection.getCameras()) {
			stringableModel.add(camera);
			verticesInNewMesh.add(camera.getPosition());
		}
		dummyBone.setPivotPoint(Vertex.centerOfGroup(verticesInNewMesh));
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		stringableModel.printTo(outputStream);
		final byte[] byteArray = outputStream.toByteArray();
		final String value = new String(byteArray);
		return new StringSelection(value);
	}

	/**
	 * The list handles both copy and move actions.
	 */
	@Override
	public int getSourceActions(final JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	 * When the export is complete, remove the old list entry if the action was a move.
	 */
	@Override
	protected void exportDone(final JComponent c, final Transferable data, final int action) {
		if (action != MOVE) {
			return;
		}
		// final JList list = (JList) c;
		// final DefaultListModel model = (DefaultListModel) list.getModel();
		// final int index = list.getSelectedIndex();
		// model.remove(index);
	}

	/**
	 * We only support importing strings.
	 */
	@Override
	public boolean canImport(final TransferHandler.TransferSupport support) {
		// we only import Strings
		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		return true;
	}
}