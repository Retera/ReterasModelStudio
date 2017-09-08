package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class VertexModelEditor implements ModelEditor {
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private final SelectionManager<Vertex> selectionManager;
	private final UndoActionListener undoActionListener;

	public VertexModelEditor(final UndoActionListener undoActionListener, final ModelView model,
			final ProgramPreferences programPreferences, final SelectionManager<Vertex> selectionManager) {
		this.undoActionListener = undoActionListener;
		this.model = model;
		this.programPreferences = programPreferences;
		this.selectionManager = selectionManager;
	}

	@Override
	public void translate(final double x, final double y, final double z) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.translate(x, y, z);
		}
	}

	@Override
	public void scale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rotate3d(final Vertex center, final Vertex axis, final double radians) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			Vertex.rotateVertex(center, axis, radians, vertex);
		}
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem) {
		// for (final Geoset geo : model.getEditableGeosets()) {
		// final GeosetVisitor geosetRenderer = renderer.beginGeoset(null,
		// null);
		// for (final Triangle triangle : geo.getTriangle()) {
		// final TriangleVisitor triangleRenderer =
		// geosetRenderer.beginTriangle();
		// for (final GeosetVertex geosetVertex : triangle.getVerts()) {
		// if (selection.contains(geosetVertex)) {
		// final VertexVisitor vertexRenderer =
		// triangleRenderer.vertex(geosetVertex.x, geosetVertex.y,
		// geosetVertex.z, geosetVertex.getNormal().x,
		// geosetVertex.getNormal().y,
		// geosetVertex.getNormal().z, geosetVertex.getBoneAttachments());
		// vertexRenderer.vertexFinished();
		// }
		// }
		// triangleRenderer.triangleFinished();
		// }
		// geosetRenderer.geosetFinished();
		// }
		// for (final IdObject object : model.getEditableIdObjects()) {
		// if (selection.contains(object.getPivotPoint())) {
		// object.apply(renderer);
		// }
		// }
		// for (final Camera camera : model.getEditableCameras()) {
		// if (selection.contains(camera.getPosition())) {
		// renderer.camera(camera);
		// }
		// if (selection.contains(camera.getTargetPosition())) {
		// renderer.camera(camera);
		// }
		// }
		selectionManager.renderSelection(renderer, coordinateSystem, model, programPreferences);
	}

	@Override
	public void cloneSelectedComponents() {
		final List<Vertex> source = new ArrayList<>(selectionManager.getSelection());

		final ArrayList<GeosetVertex> vertCopies = new ArrayList<>();
		final ArrayList<Triangle> selTris = new ArrayList<>();
		final ArrayList<IdObject> selBones = new ArrayList<>();
		final ArrayList<IdObject> newBones = new ArrayList<>();
		for (int i = 0; i < source.size(); i++) {
			final Vertex vert = source.get(i);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				vertCopies.add(new GeosetVertex(gv));
			} else {
				vertCopies.add(null);
			}
		}
		for (final IdObject b : model.getEditableIdObjects()) {
			if (source.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		final ArrayList<Triangle> newTriangles = new ArrayList<>();
		for (int k = 0; k < source.size(); k++) {
			final Vertex vert = source.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final ArrayList<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (final Triangle tri : gv.getGeoset().getTriangle()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vertex vTemp : tri.getAll()) {
							if (!source.contains(vTemp)) {
								good = false;
								break;
							}
						}
						if (good) {
							gvTriangles.add(tri);
							if (!selTris.contains(tri)) {
								selTris.add(tri);
							}
						}
					}
				}
			}
		}
		for (final Triangle tri : selTris) {
			final GeosetVertex a = vertCopies.get(source.indexOf(tri.get(0)));
			final GeosetVertex b = vertCopies.get(source.indexOf(tri.get(1)));
			final GeosetVertex c = vertCopies.get(source.indexOf(tri.get(2)));
			newTriangles.add(new Triangle(a, b, c, a.getGeoset()));
		}
		for (final GeosetVertex gv : vertCopies) {
			if (gv != null) {
				model.getModel().add(gv);
			}
		}
		for (final Triangle tri : newTriangles) {
			if (tri != null) {
				model.getModel().add(tri);
			}
			tri.forceVertsUpdate();
		}
		for (final IdObject b : newBones) {
			if (b != null) {
				model.getModel().add(b);
			}
		}
		final Set<Vertex> newSelection = new HashSet<>();
		for (final Vertex ver : vertCopies) {
			if (ver != null) {
				newSelection.add(ver);
				if (ver.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) ver;
					for (int i = 0; i < gv.getBones().size(); i++) {
						final Bone b = gv.getBones().get(i);
						if (selBones.contains(b)) {
							gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
						}
					}
				}
			}
		}
		for (final IdObject b : newBones) {
			newSelection.add(b.getPivotPoint());
			if (selBones.contains(b.getParent())) {
				b.setParent(newBones.get(selBones.indexOf(b.getParent())));
			}
		}
		selectionManager.setSelection(newSelection);
		throw new UnsupportedOperationException("DID NOT RE-CODE GOOD CLONING OF VERTICES YET");
	}

}
