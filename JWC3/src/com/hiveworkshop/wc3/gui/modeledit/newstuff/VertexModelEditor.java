package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.etheller.collections.HashMap;
import com.etheller.collections.Map;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.TeamColorAddAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.AutoCenterBonesAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.RenameBoneAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class VertexModelEditor extends AbstractModelEditor<Vertex> {
	private final ProgramPreferences programPreferences;

	public VertexModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<Vertex> selectionManager) {
		super(selectionManager, model);
		this.programPreferences = programPreferences;
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.translate(x, y, z);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		for (final Vertex vertex : selectionManager.getSelection()) {
			vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rawRotate3d(final Vertex center, final Vertex axis, final double radians) {
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
	public UndoAction autoCenterSelectedBones() {
		final Set<IdObject> selBones = new HashSet<>();
		for (final IdObject b : model.getEditableIdObjects()) {
			selBones.add(b);
		}

		final Map<Bone, Vertex> boneToOldPosition = new HashMap<>();
		for (final IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				final Bone bone = (Bone) obj;
				final ArrayList<GeosetVertex> childVerts = new ArrayList<>();
				for (final Geoset geo : model.getModel().getGeosets()) {
					childVerts.addAll(geo.getChildrenOf(bone));
				}
				if (childVerts.size() > 0) {
					final Vertex pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vertex(pivotPoint));
					pivotPoint.setTo(Vertex.centerOfGroup(childVerts));
				}
			}
		}
		return new AutoCenterBonesAction(boneToOldPosition);
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		final Vertex selectedVertex = selectionManager.getSelection().iterator().next();
		IdObject node = null;
		for (final IdObject bone : this.model.getEditableIdObjects()) {
			if (bone.getPivotPoint() == selectedVertex) {
				if (node != null) {
					throw new IllegalStateException(
							"Flagrant error. Multiple bones are bound to the same memory addresses. Save your work and restart the application.");
				}
				node = bone;
			}
		}
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		final RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addTeamColor(final ModelStructureChangeListener modelStructureChangeListener) {
		final TeamColorAddAction teamColorAddAction = new TeamColorAddAction(selectionManager.getSelectedFaces(),
				model.getModel(), modelStructureChangeListener, selectionManager);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	protected void selectByVertices(final Collection<Vertex> newSelection) {
		selectionManager.setSelection(newSelection);
	}
}
