package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.CollisionShapeChunk;
import com.hiveworkshop.wc3.mdx.Node;

/**
 * A class for CollisionShapes, which handle unit selection and related matters
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject {
	ArrayList<String> flags = new ArrayList<>();
	ExtLog extents;
	ArrayList<Vertex> vertices = new ArrayList<>();
	ArrayList<AnimFlag> animFlags = new ArrayList<>();

	public CollisionShape(final CollisionShapeChunk.CollisionShape mdxSource) {
		this.name = mdxSource.node.name;
		if ((mdxSource.node.flags & 8192) != 8192) {
			System.err.println("MDX -> MDL error: A collisionshape '" + mdxSource.node.name
					+ "' not flagged as collisionshape in MDX!");
		}
		if (mdxSource.type == 0) {
			add("Box");
			for (int i = 0; i < mdxSource.vertexs.length; i += 3) {
				vertices.add(new Vertex(mdxSource.vertexs[i + 0], mdxSource.vertexs[i + 1], mdxSource.vertexs[i + 2]));
			}
		} else if (mdxSource.type == 1) {
			add("Plane");
			for (int i = 0; i < mdxSource.vertexs.length; i += 3) {
				vertices.add(new Vertex(mdxSource.vertexs[i + 0], mdxSource.vertexs[i + 1], mdxSource.vertexs[i + 2]));
			}
		} else if (mdxSource.type == 2) {
			add("Sphere");
			extents = new ExtLog(mdxSource.boundsRadius);
			vertices.add(new Vertex(mdxSource.vertexs));
		} else if (mdxSource.type == 3) {
			add("Cylinder");
			for (int i = 0; i < mdxSource.vertexs.length; i += 3) {
				vertices.add(new Vertex(mdxSource.vertexs[i + 0], mdxSource.vertexs[i + 1], mdxSource.vertexs[i + 2]));
			}
		}
		// ----- Convert Base NODE to "IDOBJECT" -----
		setParentId(mdxSource.node.parentId);
		setObjectId(mdxSource.node.objectId);
		final Node node = mdxSource.node;
		if (node.geosetTranslation != null) {
			add(new AnimFlag(node.geosetTranslation));
		}
		if (node.geosetScaling != null) {
			add(new AnimFlag(node.geosetScaling));
		}
		if (node.geosetRotation != null) {
			add(new AnimFlag(node.geosetRotation));
		}
		// ----- End Base NODE to "IDOBJECT" -----
	}

	@Override
	public IdObject copy() {
		final CollisionShape x = new CollisionShape();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.flags = new ArrayList<>(flags);
		x.vertices = new ArrayList<>(vertices);
		if (extents != null) {
			x.extents = new ExtLog(extents);
		}
		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
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

	private CollisionShape() {

	}

	public static CollisionShape read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("CollisionShape")) {
			final CollisionShape e = new CollisionShape();
			e.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			line = MDLReader.nextLine(mdl);
			while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
					&& !line.equals("COMPLETED PARSING")) {
				if (line.contains("ObjectId")) {
					e.objectId = MDLReader.readInt(line);
				} else if (line.contains("Parent")) {
					e.parentId = MDLReader.splitToInts(line)[0];
					// e.parent = mdlr.getIdObject(e.parentId);
				} else if (line.contains("Extent") || line.contains("BoundsRadius")) {
					MDLReader.reset(mdl);
					e.extents = ExtLog.read(mdl);
				} else if (line.contains("Vertices")) {
					while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
						e.addVertex(Vertex.parseText(line));
					}
				} else if ((line.contains("Scaling") || line.contains("Rotation") || line.contains("Translation"))
						&& !line.contains("DontInherit")) {
					MDLReader.reset(mdl);
					e.animFlags.add(AnimFlag.read(mdl));
				} else {
					e.flags.add(MDLReader.readFlag(line));
				}
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			return e;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse CollisionShape: Missing or unrecognized open statement.");
		}
		return null;
	}

	@Override
	public void printTo(final PrintWriter writer, final int version) {
		// Remember to update the ids of things before using this
		// -- uses objectId value of idObject superclass
		// -- uses parentId value of idObject superclass
		// -- uses the parent (java Object reference) of idObject superclass
		writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
		if (objectId != -1) {
			writer.println("\tObjectId " + objectId + ",");
		}
		if (parentId != -1) {
			writer.println("\tParent " + parentId + ",\t// \"" + getParent().getName() + "\"");
		}
		for (final String s : flags) {
			writer.println("\t" + s + ",");
		}
		writer.println("\tVertices " + vertices.size() + " {");
		for (final Vertex v : vertices) {
			writer.println("\t\t" + v.toString() + ",");
		}
		writer.println("\t}");
		if (extents != null) {
			extents.printTo(writer, 1);
		}
		for (int i = 0; i < animFlags.size(); i++) {
			animFlags.get(i).printTo(writer, 1);
		}
		writer.println("}");
	}

	@Override
	public void flipOver(final byte axis) {
		final String currentFlag = "Rotation";
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag flag = animFlags.get(i);
			flag.flipOver(axis);
		}
	}

	@Override
	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	@Override
	public void add(final String flag) {
		flags.add(flag);
	}

	@Override
	public List<String> getFlags() {
		return flags;
	}

	@Override
	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(final ArrayList<Vertex> vertices) {
		this.vertices = vertices;
	}

	public void setFlags(final ArrayList<String> flags) {
		this.flags = flags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> animFlags) {
		this.animFlags = animFlags;
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
		if (flags.contains("Box")) {
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
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	public double getSphereClickRadius(final CoordinateSystem coordinateSystem) {
		if (extents != null) {
			if (extents.hasBoundsRadius()) {
				return extents.getBoundsRadius();
			}
		}
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return 1;
	}

	@Override
	public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Translation");
		if (translationFlag != null) {
			return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}

	@Override
	public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Rotation");
		if (translationFlag != null) {
			return (QuaternionRotation) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}

	@Override
	public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag translationFlag = AnimFlag.find(animFlags, "Scaling");
		if (translationFlag != null) {
			return (Vertex) translationFlag.interpolateAt(animatedRenderEnvironment);
		}
		return null;
	}
}
