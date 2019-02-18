package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import java.util.Collection;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelElementRenderer;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public interface TVertexSelectionView {
	TVertex getCenter();

	Collection<Triangle> getSelectedFaces();

	Collection<? extends TVertex> getSelectedVertices();

	// needs to be coord system, not coord axes, so that
	// vertex selection view knows the zoom level,
	// so that the width and height of a vertex in pixels
	// is zoom independent
	// boolean canSelectAt(Point point, CoordinateSystem axes);

	double getCircumscribedSphereRadius(TVertex center);

	void renderSelection(ModelElementRenderer renderer, ModelView modelView, ProgramPreferences programPreferences);

	boolean isEmpty();
}