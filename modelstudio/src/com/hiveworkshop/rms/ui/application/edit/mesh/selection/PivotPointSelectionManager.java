package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vertex;

public final class PivotPointSelectionManager extends AbstractSelectionManager<Vertex> {

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	@Override
	public Vertex getCenter() {
		return Vertex.centerOfGroup(selection);
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex sphereCenter) {
		double radius = 0;
		for (final Vertex item : selection) {
			final double distance = sphereCenter.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	private final Bone renderBoneDummy = new Bone();

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView model, final ProgramPreferences programPreferences) {
		final Set<Vertex> drawnSelection = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object.getPivotPoint())) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getSelectColor(),
						programPreferences.getPivotPointsSelectedColor());
				drawnSelection.add(object.getPivotPoint());
			}
		}
		for (final Camera camera : model.getEditableCameras()) {
			renderer.renderCamera(camera,
					selection.contains(camera.getPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(),
					camera.getPosition(),
					selection.contains(camera.getTargetPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(),
					camera.getTargetPosition());
			drawnSelection.add(camera.getPosition());
			drawnSelection.add(camera.getTargetPosition());
		}
		for (final Vertex vertex : selection) {
			if (!drawnSelection.contains(vertex)) {
				renderBoneDummy.setPivotPoint(vertex);
				renderer.renderIdObject(renderBoneDummy, NodeIconPalette.SELECTED, programPreferences.getSelectColor(),
						programPreferences.getPivotPointsSelectedColor());
			}
		}
	}

	@Override
	public Collection<Vertex> getSelectedVertices() {
		return getSelection();
	}

	@Override
	public TVertex getUVCenter(final int tvertexLayerId) {
		return TVertex.ORIGIN;
	}

	@Override
	public Collection<? extends TVertex> getSelectedTVertices(final int tvertexLayerId) {
		return Collections.emptySet();
	}

	@Override
	public double getCircumscribedSphereRadius(final TVertex center, final int tvertexLayerId) {
		return 0;
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {

	}
}
