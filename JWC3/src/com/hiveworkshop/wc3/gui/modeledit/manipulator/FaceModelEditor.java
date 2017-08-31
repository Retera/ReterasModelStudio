package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class FaceModelEditor implements ModelEditor {
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private final SelectionManager<Triangle> selectionManager;
	private final UndoActionListener undoActionListener;

	public FaceModelEditor(final UndoActionListener undoActionListener, final ModelView model,
			final ProgramPreferences programPreferences, final SelectionManager<Triangle> selectionManager) {
		this.undoActionListener = undoActionListener;
		this.model = model;
		this.programPreferences = programPreferences;
		this.selectionManager = selectionManager;
	}

	@Override
	public void translate(final double x, final double y, final double z) {
		final Set<Vertex> translatedVertices = new HashSet<>();
		for (final Triangle triangle : selectionManager.getSelection()) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!translatedVertices.contains(vertex)) {
					vertex.translate(x, y, z);
					translatedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void scale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selectionManager.getSelection()) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void rotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selectionManager.getSelection()) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void rotate3d(final Vertex center, final Vertex axis, final double radians) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selectionManager.getSelection()) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					Vertex.rotateVertex(center, axis, radians, vertex);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem) {
		// for (final Geoset geo : model.getEditableGeosets()) {
		// final GeosetVisitor geosetRenderer = renderer.beginGeoset(null,
		// null);
		// for (final Triangle triangle : geo.getTriangle()) {
		// if (selection.contains(triangle)) {
		// final TriangleVisitor triangleRenderer =
		// geosetRenderer.beginTriangle();
		// for (final GeosetVertex geosetVertex : triangle.getVerts()) {
		// final VertexVisitor vertexRenderer =
		// triangleRenderer.vertex(geosetVertex.x, geosetVertex.y,
		// geosetVertex.z, geosetVertex.getNormal().x,
		// geosetVertex.getNormal().y,
		// geosetVertex.getNormal().z, geosetVertex.getBoneAttachments());
		// vertexRenderer.vertexFinished();
		// }
		// triangleRenderer.triangleFinished();
		// }
		// }
		// geosetRenderer.geosetFinished();
		// }
		for (final Triangle triangle : selectionManager.getSelection()) {
			renderer.renderFace(Color.RED, FACE_HIGHLIGHT_COLOR, triangle.get(0), triangle.get(1), triangle.get(2));
		}
	}

	@Override
	public void cloneSelectedComponents() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("DID NOT CODE CLONING SELECTED FACES YET");
	}

}
