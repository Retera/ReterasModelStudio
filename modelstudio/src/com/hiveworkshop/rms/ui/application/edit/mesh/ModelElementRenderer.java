package com.hiveworkshop.rms.ui.application.edit.mesh;

import java.awt.Color;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.util.Vector3;

public interface ModelElementRenderer {
	void renderFace(Color borderColor, Color color, GeosetVertex a, GeosetVertex b, GeosetVertex c);

	void renderVertex(Color color, Vector3 vertex);

	void renderIdObject(IdObject object, NodeIconPalette nodeIconPalette, Color lightColor, Color pivotPointColor);

	void renderCamera(Camera camera, Color boxColor, Vector3 position, Color targetColor, Vector3 targetPosition);
}
