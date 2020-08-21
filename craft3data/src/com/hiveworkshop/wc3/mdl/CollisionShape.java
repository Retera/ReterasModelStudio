package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxCollisionShape;
import com.etheller.warsmash.parsers.mdlx.MdlxCollisionShape.Type;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * A class for CollisionShapes, which handle unit selection and related matters
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject {
	ExtLog extents;
	Type type = Type.BOX;
	List<Vertex> vertices = new ArrayList<>();

	public CollisionShape(final CollisionShape shape) {
		copyObject(shape);
	
		vertices = new ArrayList<>(shape.vertices);

		if (shape.extents != null) {
			extents = new ExtLog(shape.extents);
		}
	}

	public CollisionShape(final MdlxCollisionShape shape) {
		if ((shape.flags & 8192) != 8192) {
			System.err.println("MDX -> MDL error: A collisionshape '" + shape.name
					+ "' not flagged as collisionshape in MDX!");
		}

		loadObject(shape);

		type = shape.type;

		float[][] vertices = shape.vertices;

		this.vertices.add(new Vertex(vertices[0]));

		if (type != Type.SPHERE) {
			this.vertices.add(new Vertex(vertices[1]));
		}

		if (type == Type.SPHERE || type == Type.CYLINDER) {
			extents = new ExtLog(shape.boundsRadius);
		}
	}

	public MdlxCollisionShape toMdlx() {
		MdlxCollisionShape shape = new MdlxCollisionShape();
		
		objectToMdlx(shape);

		shape.type = type;

		shape.vertices[0] = getVertex(0).toFloatArray();

		if (shape.type != MdlxCollisionShape.Type.SPHERE) {
			shape.vertices[1] = getVertex(1).toFloatArray();
		}

		if (shape.type == MdlxCollisionShape.Type.SPHERE || shape.type == MdlxCollisionShape.Type.CYLINDER) {
			shape.boundsRadius = (float)getExtents().getBoundsRadius();
		}

		return shape;
	}

	@Override
	public CollisionShape copy() {
		return new CollisionShape(this);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void addVertex(final Vertex v) {
		vertices.add(v);
	}

	public Vertex getVertex(final int vertId) {
		return vertices.get(vertId);
	}

	public int numVerteces() {
		return vertices.size();
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(final List<Vertex> vertices) {
		this.vertices = vertices;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.collisionShape(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		final byte xDimension = coordinateSystem.getPortFirstXYZ();
		final byte yDimension = coordinateSystem.getPortSecondXYZ();
		final int xCoord = (int) coordinateSystem.convertX(pivotPoint.getCoord(xDimension));
		final int yCoord = (int) coordinateSystem.convertY(pivotPoint.getCoord(yDimension));
		if (type == Type.BOX) {
			if (vertices.size() > 0) {
				// final Vertex vertex = vertices.get(0);
				// final int secondXCoord = (int)
				// coordinateSystem.convertX(vertex.getCoord(xDimension));
				// final int secondYCoord = (int)
				// coordinateSystem.convertY(vertex.getCoord(yDimension));
				// final int minXCoord = Math.min(xCoord, secondXCoord);
				// final int minYCoord = Math.min(yCoord, secondYCoord);
				// final int maxXCoord = Math.max(xCoord, secondXCoord);
				// final int maxYCoord = Math.max(yCoord, secondYCoord);
				// final int generalRadius = Math.max(maxXCoord - minXCoord, maxYCoord -
				// minYCoord) / 2;
				return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
			} else {
				return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
			}
		}
		if (extents == null) {
			return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
		}
		return extents.getBoundsRadius();
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return 1;
	}
}
