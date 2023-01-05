package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape.Type;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for CollisionShapes, which handle unit selection and related matters
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject {
	private Type type;
	private final List<Vec3> vertices = new ArrayList<>();
	private double boundsRadius = 0;

	public CollisionShape(CollisionShape shape) {
		super(shape);

		type = shape.type;

		for(Vec3 vert : shape.vertices){
			vertices.add(new Vec3(vert));
		}
		boundsRadius = shape.boundsRadius;
	}

	public CollisionShape(String name) {
		this(name, Type.SPHERE);
	}

	public CollisionShape() {
		this(Type.SPHERE);
	}
	public CollisionShape(Type type) {
		this("", type);
	}
	public CollisionShape(String name, Type type) {
		this.name = name;
		this.type = type;
		vertices.add(new Vec3());
		if(type != Type.SPHERE){
			vertices.add(new Vec3());
		}
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

	public void setVertex(int vertId, Vec3 v){
		if(vertId < vertices.size()) {
			if(v == null){
				vertices.remove(vertId);
			} else {
				vertices.get(vertId).set(v);
			}
		} else if (v != null) {
			vertices.add(v);
		}
	}

	public Vec3 getVertex(int vertId) {
		if(vertId<vertices.size()){
			return vertices.get(vertId);
		}
		return null;
	}

	public int numVerteces() {
		return vertices.size();
	}

	public List<Vec3> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vec3> vertices) {
		this.vertices.clear();
		this.vertices.addAll(vertices);
	}

	public CollisionShape setBoundsRadius(double boundsRadius) {
		this.boundsRadius = boundsRadius;
		return this;
	}
	public double getBoundsRadius() {
		return boundsRadius;
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
////		byte xDimension = coordinateSystem.getPortFirstXYZ();
////		byte yDimension = coordinateSystem.getPortSecondXYZ();
////		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(xDimension));
////		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(yDimension));
//		if (type == Type.BOX) {
//			if (vertices.size() > 0) {
//				// Vertex vertex = vertices.get(0);
//				// int secondXCoord = (int)
//				// coordinateSystem.convertX(vertex.getCoord(xDimension));
//				// int secondYCoord = (int)
//				// coordinateSystem.convertY(vertex.getCoord(yDimension));
//				// int minXCoord = Math.min(xCoord, secondXCoord);
//				// int minYCoord = Math.min(yCoord, secondYCoord);
//				// int maxXCoord = Math.max(xCoord, secondXCoord);
//				// int maxYCoord = Math.max(yCoord, secondYCoord);
//				// int generalRadius = Math.max(maxXCoord - minXCoord, maxYCoord -
//				// minYCoord) / 2;
//				return DEFAULT_CLICK_RADIUS;
//			} else {
//				return DEFAULT_CLICK_RADIUS;
//			}
//		}
//		if (extents == null) {
//			return DEFAULT_CLICK_RADIUS;
//		}
//		return extents.getBoundsRadius();
////		byte xDimension = coordinateSystem.getPortFirstXYZ();
////		byte yDimension = coordinateSystem.getPortSecondXYZ();
////		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(xDimension));
////		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(yDimension));
////		if (type == Type.BOX) {
////			if (vertices.size() > 0) {
////				// Vertex vertex = vertices.get(0);
////				// int secondXCoord = (int)
////				// coordinateSystem.convertX(vertex.getCoord(xDimension));
////				// int secondYCoord = (int)
////				// coordinateSystem.convertY(vertex.getCoord(yDimension));
////				// int minXCoord = Math.min(xCoord, secondXCoord);
////				// int minYCoord = Math.min(yCoord, secondYCoord);
////				// int maxXCoord = Math.max(xCoord, secondXCoord);
////				// int maxYCoord = Math.max(yCoord, secondYCoord);
////				// int generalRadius = Math.max(maxXCoord - minXCoord, maxYCoord -
////				// minYCoord) / 2;
////				return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
////			} else {
////				return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
////			}
////		}
////		if (extents == null) {
////			return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
////		}
////		return extents.getBoundsRadius();
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return 1;
	}
}
