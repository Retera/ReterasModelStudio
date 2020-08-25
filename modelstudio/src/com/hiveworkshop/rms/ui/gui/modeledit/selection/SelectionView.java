package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.util.Collection;

public interface SelectionView {
	Vertex getCenter();

	Collection<Triangle> getSelectedFaces();

	Collection<? extends Vertex> getSelectedVertices();

	// needs to be coord system, not coord axes, so that
	// vertex selection view knows the zoom level,
	// so that the width and height of a vertex in pixels
	// is zoom independent
	// boolean canSelectAt(Point point, CoordinateSystem axes);

	double getCircumscribedSphereRadius(Vertex center);

	void renderSelection(ModelElementRenderer renderer, final CoordinateSystem coordinateSystem, ModelView modelView,
                         ProgramPreferences programPreferences);

	TVertex getUVCenter(int tvertexLayerId);

	Collection<? extends TVertex> getSelectedTVertices(int tvertexLayerId);

	double getCircumscribedSphereRadius(TVertex center, int tvertexLayerId);

	void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView,
                           ProgramPreferences programPreferences, int tvertexLayerId);

	boolean isEmpty();
}
