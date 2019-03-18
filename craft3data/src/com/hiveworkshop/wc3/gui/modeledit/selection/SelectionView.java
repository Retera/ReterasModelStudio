package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.Collection;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelElementRenderer;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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
