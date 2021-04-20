package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;

public interface SelectionView {
	Vec3 getCenter();

	Collection<Triangle> getSelectedFaces();

	Collection<? extends Vec3> getSelectedVertices();

	// needs to be coord system, not coord axes, so that vertex selection view knows the zoom level,
	// so that the width and height of a vertex in pixels is zoom independent
	// boolean canSelectAt(Point point, CoordinateSystem axes);

	double getCircumscribedSphereRadius(Vec3 center);

	void renderSelection(ModelElementRenderer renderer,
	                     CoordinateSystem coordinateSystem,
	                     ModelView modelView,
	                     ProgramPreferences programPreferences);

	Vec2 getUVCenter(int tvertexLayerId);

	Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId);

	double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId);

	void renderUVSelection(TVertexModelElementRenderer renderer,
	                       ModelView modelView,
	                       ProgramPreferences programPreferences,
	                       int tvertexLayerId);

	boolean isEmpty();
}
