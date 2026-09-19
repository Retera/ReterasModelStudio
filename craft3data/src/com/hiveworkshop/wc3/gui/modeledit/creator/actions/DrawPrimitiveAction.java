package com.hiveworkshop.wc3.gui.modeledit.creator.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.util.PrimitiveMeshes;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveMesh;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveOptions;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveShape;

/**
 * Creates one primitive inside a geoset. While the user drags, the base
 * rectangle and height change through updateTranslation and the geometry is
 * regenerated, so spinner changes made during the drag show up at once.
 */
public final class DrawPrimitiveAction implements GenericMoveAction {
	private final PrimitiveShape shape;
	private final PrimitiveOptions options;
	private final Geoset geoset;
	private final byte dim1, dim2;
	private double x1, y1, x2, y2;
	private double height;
	private final List<GeosetVertex> vertices = new ArrayList<>();
	private final List<Triangle> triangles = new ArrayList<>();

	public DrawPrimitiveAction(final PrimitiveShape shape, final PrimitiveOptions options, final double x1,
			final double y1, final double x2, final double y2, final byte dim1, final byte dim2,
			final Geoset geoset) {
		this.shape = shape;
		this.options = options;
		this.geoset = geoset;
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.height = 1;
	}

	private int uvLayersOfGeoset() {
		int layers = 1;
		for (final GeosetVertex vertex : geoset.getVertices()) {
			layers = Math.max(layers, vertex.getTverts().size());
		}
		return layers;
	}

	private void detach() {
		for (final Triangle triangle : triangles) {
			geoset.remove(triangle);
		}
		for (final GeosetVertex vertex : vertices) {
			geoset.remove(vertex);
		}
	}

	private void attach() {
		for (final GeosetVertex vertex : vertices) {
			vertex.setGeoset(geoset);
			geoset.add(vertex);
		}
		for (final Triangle triangle : triangles) {
			triangle.setGeoset(geoset);
			for (final GeosetVertex vertex : triangle.getAll()) {
				if (!vertex.getTriangles().contains(triangle)) {
					vertex.getTriangles().add(triangle);
				}
			}
			geoset.addTriangle(triangle);
		}
	}

	private void rebuild() {
		detach();
		vertices.clear();
		triangles.clear();
		final PrimitiveMesh mesh = PrimitiveMeshes.build(shape, options, dim1, dim2, x1, y1, x2, y2, height,
				uvLayersOfGeoset());
		vertices.addAll(mesh.vertices);
		triangles.addAll(mesh.triangles);
		attach();
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		x2 += deltaX;
		y2 += deltaY;
		height += deltaZ;
		rebuild();
	}

	@Override
	public void redo() {
		if (vertices.isEmpty()) {
			rebuild();
		} else {
			attach();
		}
	}

	@Override
	public void undo() {
		detach();
	}

	@Override
	public String actionName() {
		return "create " + shape.getDisplayName().toLowerCase();
	}
}
