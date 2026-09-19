package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ComponentCollisionShapeCard extends ComponentNodeCard<CollisionShape> {
	private static final List<String> TYPES = Arrays.asList("Box", "Plane", "Sphere", "Cylinder");
	private JPanel secondVertexRow;

	@Override
	protected void addTypeRows() {
		beginSection("Collision Shape");
		addComboBox("Type", () -> TYPES, type -> type, this::typeOf, (shape, type) -> {
			shape.getFlags().removeAll(TYPES);
			if (type != null) {
				shape.getFlags().add(type);
			}
			while (shape.getVertices().size() < ("Sphere".equals(type) ? 1 : 2)) {
				shape.getVertices().add(new Vertex(0, 0, 0));
			}
		});
		addVertexRow("Vertex 1", 1.0, shape -> vertexAt(shape, 0), (shape, vertex) -> setVertexAt(shape, 0, vertex));
		secondVertexRow = addVertexRow("Vertex 2", 1.0, shape -> vertexAt(shape, 1),
				(shape, vertex) -> setVertexAt(shape, 1, vertex));
		addDoubleSpinner("Bounds Radius", 1.0,
				shape -> (shape.getExtents() == null) || !shape.getExtents().hasBoundsRadius() ? 0.0
						: shape.getExtents().getBoundsRadius(),
				(shape, radius) -> {
					if (shape.getExtents() == null) {
						shape.setExtents(new ExtLog(radius));
					} else {
						shape.getExtents().setBounds(radius);
					}
				});
		endSection();
	}

	@Override
	protected void onReload() {
		super.onReload();
		final boolean sphere = "Sphere".equals(typeOf(item));
		secondVertexRow.setEnabled(!sphere);
		for (final java.awt.Component child : secondVertexRow.getComponents()) {
			child.setEnabled(!sphere);
		}
	}

	private String typeOf(final CollisionShape shape) {
		for (final String type : TYPES) {
			if (shape.getFlags().contains(type)) {
				return type;
			}
		}
		return "Box";
	}

	private static Vertex vertexAt(final CollisionShape shape, final int index) {
		return index < shape.getVertices().size() ? shape.getVertices().get(index) : null;
	}

	private static void setVertexAt(final CollisionShape shape, final int index, final Vertex vertex) {
		final ArrayList<Vertex> vertices = shape.getVertices();
		while (vertices.size() <= index) {
			vertices.add(new Vertex(0, 0, 0));
		}
		if (vertex == null) {
			vertices.set(index, new Vertex(0, 0, 0));
		} else {
			vertices.get(index).setTo(vertex);
		}
	}
}
