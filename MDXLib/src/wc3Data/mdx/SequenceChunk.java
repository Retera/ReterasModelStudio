package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class SequenceChunk {
	public Sequence[] sequence = new Sequence[0];

	public static final String key = "SEQS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "SEQS");
		int chunkSize = in.readInt();
		List<Sequence> sequenceList = new ArrayList();
		int sequenceCounter = chunkSize;
		while (sequenceCounter > 0) {
			Sequence tempsequence = new Sequence();
			sequenceList.add(tempsequence);
			tempsequence.load(in);
			sequenceCounter -= tempsequence.getSize();
		}
		sequence = sequenceList.toArray(new Sequence[sequenceList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfSequences = sequence.length;
		out.writeNByteString("SEQS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < sequence.length; i++) {
			sequence[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < sequence.length; i++) {
			a += sequence[i].getSize();
		}

		return a;
	}

	public class Sequence {
		public String name = "";
		public int intervalStart;
		public int intervalEnd;
		public float moveSpeed;
		public int nonLooping;
		public float rarity;
		public int unknownNull;
		public float boundsRadius;
		public float[] minimumExtent = new float[3];
		public float[] maximumExtent = new float[3];

		public void load(BlizzardDataInputStream in) throws IOException {
			name = in.readCharsAsString(80);
			intervalStart = in.readInt();
			intervalEnd = in.readInt();
			moveSpeed = in.readFloat();
			nonLooping = in.readInt();
			rarity = in.readFloat();
			unknownNull = in.readInt();
			boundsRadius = in.readFloat();
			minimumExtent = MdxUtils.loadFloatArray(in, 3);
			maximumExtent = MdxUtils.loadFloatArray(in, 3);
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeNByteString(name, 80);
			out.writeInt(intervalStart);
			out.writeInt(intervalEnd);
			out.writeFloat(moveSpeed);
			out.writeInt(nonLooping);
			out.writeFloat(rarity);
			out.writeInt(unknownNull);
			out.writeFloat(boundsRadius);
			if (minimumExtent.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array minimumExtent needs either the length 3 or a multiple of this number. (got "
								+ minimumExtent.length + ")");
			}
			MdxUtils.saveFloatArray(out, minimumExtent);
			if (maximumExtent.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array maximumExtent needs either the length 3 or a multiple of this number. (got "
								+ maximumExtent.length + ")");
			}
			MdxUtils.saveFloatArray(out, maximumExtent);

		}

		public int getSize() {
			int a = 0;
			a += 80;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			a += 12;

			return a;
		}
	}
}
