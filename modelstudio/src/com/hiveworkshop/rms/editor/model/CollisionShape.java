package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape.Type;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for CollisionShapes, which handle unit selection and related matters
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject {
	ExtLog extents;
	Type type = Type.BOX;
	List<Vec3> vertices = new ArrayList<>();

	public CollisionShape(CollisionShape shape) {
		copyObject(shape);

		type = shape.type;

		vertices = new ArrayList<>(shape.vertices);

		if (shape.extents != null) {
			extents = new ExtLog(shape.extents);
		}
	}

	public CollisionShape(MdlxCollisionShape shape) {
		if ((shape.flags & 8192) != 8192) {
			System.err.println("MDX -> MDL error: A collisionshape '" + shape.name
					+ "' not flagged as collisionshape in MDX!");
		}

		loadObject(shape);

		type = shape.type;

		float[][] vertices = shape.vertices;

		this.vertices.add(new Vec3(vertices[0]));

		if (type != Type.SPHERE) {
			this.vertices.add(new Vec3(vertices[1]));
		}

		if (type == Type.SPHERE || type == Type.CYLINDER) {
			extents = new ExtLog(shape.boundsRadius);
		}
	}

	public MdlxCollisionShape toMdlx(EditableModel model) {
		MdlxCollisionShape shape = new MdlxCollisionShape();

		objectToMdlx(shape, model);

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

	public void addVertex(Vec3 v) {
		vertices.add(v);
	}

	public Vec3 getVertex(int vertId) {
		return vertices.get(vertId);
	}

	public int numVerteces() {
		return vertices.size();
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(ExtLog extents) {
		this.extents = extents;
	}

	public List<Vec3> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vec3> vertices) {
		this.vertices = vertices;
	}

//	@Override
//	public void apply(IdObjectVisitor visitor) {
//		visitor.collisionShape(this);
//	}

	@Override
	public double getClickRadius(CoordinateSystem coordinateSystem) {
		byte xDimension = coordinateSystem.getPortFirstXYZ();
		byte yDimension = coordinateSystem.getPortSecondXYZ();
		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(xDimension));
		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(yDimension));
		if (type == Type.BOX) {
			if (vertices.size() > 0) {
				// Vertex vertex = vertices.get(0);
				// int secondXCoord = (int)
				// coordinateSystem.convertX(vertex.getCoord(xDimension));
				// int secondYCoord = (int)
				// coordinateSystem.convertY(vertex.getCoord(yDimension));
				// int minXCoord = Math.min(xCoord, secondXCoord);
				// int minYCoord = Math.min(yCoord, secondYCoord);
				// int maxXCoord = Math.max(xCoord, secondXCoord);
				// int maxYCoord = Math.max(yCoord, secondYCoord);
				// int generalRadius = Math.max(maxXCoord - minXCoord, maxYCoord -
				// minYCoord) / 2;
				return DEFAULT_CLICK_RADIUS / CoordSysUtils.getZoom(coordinateSystem);
			} else {
				return DEFAULT_CLICK_RADIUS / CoordSysUtils.getZoom(coordinateSystem);
			}
		}
		if (extents == null) {
			return DEFAULT_CLICK_RADIUS / CoordSysUtils.getZoom(coordinateSystem);
		}
		return extents.getBoundsRadius();
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return 1;
	}
}
