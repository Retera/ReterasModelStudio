package com.hiveworkshop.wc3.gui.animedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.AbstractSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelElementRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class NodeAnimationSelectionManager extends AbstractSelectionManager<IdObject> {
	private final RenderModel renderModel;

	public NodeAnimationSelectionManager(final RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	private final Vector4f pivotHeap = new Vector4f();
	private final Vertex centerOfGroupSumHeap = new Vertex(0, 0, 0);

	@Override
	public Vertex getCenter() {
		centerOfGroupSumHeap.x = 0;
		centerOfGroupSumHeap.y = 0;
		centerOfGroupSumHeap.z = 0;
		for (final IdObject object : selection) {
			final Vertex pivot = object.getPivotPoint();
			pivotHeap.x = (float) pivot.x;
			pivotHeap.y = (float) pivot.y;
			pivotHeap.z = (float) pivot.z;
			pivotHeap.w = 1;
			Matrix4f.transform(renderModel.getRenderNode(object).getWorldMatrix(), pivotHeap, pivotHeap);
			centerOfGroupSumHeap.x += pivotHeap.x;
			centerOfGroupSumHeap.y += pivotHeap.y;
			centerOfGroupSumHeap.z += pivotHeap.z;
		}
		if (selection.size() > 0) {
			centerOfGroupSumHeap.x /= selection.size();
			centerOfGroupSumHeap.y /= selection.size();
			centerOfGroupSumHeap.z /= selection.size();
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex sphereCenter) {
		double radius = 0;
		for (final IdObject item : selection) {
			final Vertex pivot = item.getPivotPoint();
			pivotHeap.x = (float) pivot.x;
			pivotHeap.y = (float) pivot.y;
			pivotHeap.z = (float) pivot.z;
			pivotHeap.w = 1;
			Matrix4f.transform(renderModel.getRenderNode(item).getWorldMatrix(), pivotHeap, pivotHeap);
			final double distance = sphereCenter.distance(pivotHeap);
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
		// TODO !!! apply rendering
		final Set<IdObject> drawnSelection = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object)) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getSelectColor(),
						programPreferences.getPivotPointsSelectedColor());
				drawnSelection.add(object);
			}
		}
		for (final IdObject selectedObject : selection) {
			if (!drawnSelection.contains(selectedObject)) {
				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
				renderer.renderIdObject(renderBoneDummy, NodeIconPalette.SELECTED, programPreferences.getSelectColor(),
						programPreferences.getPivotPointsSelectedColor());
			}
		}
	}

	@Override
	public Collection<Vertex> getSelectedVertices() {
		// These reference the MODEL EDITOR pivot points,
		// used only as memory references so that downstream will know
		// to select those pivots, and therefore those IdObject nodes,
		// for static editing (hence we do not apply worldMatrix)
		final List<Vertex> vertices = new ArrayList<>();
		for (final IdObject obj : selection) {
			vertices.add(obj.getPivotPoint());
		}
		return vertices;
	}
}
