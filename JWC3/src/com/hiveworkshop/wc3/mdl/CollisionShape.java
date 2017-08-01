package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;
import com.hiveworkshop.wc3.mdx.CollisionShapeChunk;
import com.hiveworkshop.wc3.mdx.Node;

/**
 * A class for CollisionShapes, which handle unit selection and related matters
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class CollisionShape extends IdObject {
	ArrayList<String> flags = new ArrayList<String>();
	ExtLog extents;
	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	ArrayList<AnimFlag> animFlags = new ArrayList<AnimFlag>();

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
		} else if (mdxSource.type == 2) {
			add("Sphere");
			extents = new ExtLog(mdxSource.boundsRadius);
			vertices.add(new Vertex(mdxSource.vertexs));
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

		x.name = name + " copy";
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.parent = parent;

		x.flags = new ArrayList<String>(flags);
		x.vertices = new ArrayList<Vertex>(vertices);
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
				} else if (line.contains("Extent") || (line).contains("BoundsRadius")) {
					MDLReader.reset(mdl);
					e.extents = ExtLog.read(mdl);
				} else if (line.contains("Vertices")) {
					while (!((line = MDLReader.nextLine(mdl)).contains("\t}"))) {
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
	public void printTo(final PrintWriter writer) {
		// Remember to update the ids of things before using this
		// -- uses objectId value of idObject superclass
		// -- uses parentId value of idObject superclass
		// -- uses the parent (java Object reference) of idObject superclass
		writer.println(MDLReader.getClassName(this.getClass()) + " \"" + getName() + "\" {");
		if (objectId != -1) {
			writer.println("\tObjectId " + objectId + ",");
		}
		if (parentId != -1) {
			writer.println("\tParent " + parentId + ",\t// \"" + parent.getName() + "\"");
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
}
