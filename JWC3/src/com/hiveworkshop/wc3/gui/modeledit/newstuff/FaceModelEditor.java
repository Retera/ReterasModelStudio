package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.TeamColorAddAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class FaceModelEditor extends AbstractModelEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<Triangle> selectionManager) {
		super(selectionManager, model);
		this.programPreferences = programPreferences;
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
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
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
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
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
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
	public void rawRotate3d(final Vertex center, final Vertex axis, final double radians) {
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
		this.selectionManager.renderSelection(renderer, coordinateSystem, this.model, programPreferences);
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new IllegalStateException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new IllegalStateException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction addTeamColor(final ModelStructureChangeListener modelStructureChangeListener) {
		final TeamColorAddAction teamColorAddAction = new TeamColorAddAction(selectionManager.getSelection(),
				model.getModel(), modelStructureChangeListener, selectionManager);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	protected void selectByVertices(final Collection<Vertex> newSelection) {
		final Set<Triangle> newlySelectedFaces = new HashSet<>();
		for (final Geoset geoset : model.getModel().getGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				boolean allInSelection = true;
				for (final GeosetVertex vertex : triangle.getVerts()) {
					if (!newSelection.contains(vertex)) {
						allInSelection = false;
					}
				}
				if (allInSelection) {
					newlySelectedFaces.add(triangle);
				}
			}
		}
		selectionManager.setSelection(newlySelectedFaces);
	}
}
