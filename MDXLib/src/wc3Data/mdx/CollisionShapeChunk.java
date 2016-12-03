package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class CollisionShapeChunk {
	public CollisionShape[] collisionShape = new CollisionShape[0];

	public static final String key = "CLID";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "CLID");
		int chunkSize = in.readInt();
		List<CollisionShape> collisionShapeList = new ArrayList();
		int collisionShapeCounter = chunkSize;
		while (collisionShapeCounter > 0) {
			CollisionShape tempcollisionShape = new CollisionShape();
			collisionShapeList.add(tempcollisionShape);
			tempcollisionShape.load(in);
			collisionShapeCounter -= tempcollisionShape.getSize();
		}
		collisionShape = collisionShapeList
				.toArray(new CollisionShape[collisionShapeList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfCollisionShapes = collisionShape.length;
		out.writeNByteString("CLID", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < collisionShape.length; i++) {
			collisionShape[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < collisionShape.length; i++) {
			a += collisionShape[i].getSize();
		}

		return a;
	}

	public class CollisionShape {
		public Node node = new Node();
		public int type;
		public float[] vertexs = new float[0];
		public float boundsRadius;

		public void load(BlizzardDataInputStream in) throws IOException {
			node = new Node();
			node.load(in);
			type = in.readInt();
			vertexs = MdxUtils.loadFloatArray(in, (2 - type / 2) * 3);
			if (type == 2) {
				boundsRadius = in.readFloat();
			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			int nrOfVertices = vertexs.length / 3;
			node.save(out);
			out.writeInt(type);
			if (vertexs.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array vertexs needs either the length 3 or a multiple of this number. (got "
								+ vertexs.length + ")");
			}
			MdxUtils.saveFloatArray(out, vertexs);
			if (type == 2) {
				out.writeFloat(boundsRadius);
			}

		}

		public int getSize() {
			int a = 0;
			a += node.getSize();
			a += 4;
			a += (2 - type / 2) * 12;
			if (type == 2) {
				a += 4;
			}

			return a;
		}
	}
}
