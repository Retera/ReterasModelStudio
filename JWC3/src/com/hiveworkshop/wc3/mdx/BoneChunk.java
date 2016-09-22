package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class BoneChunk {
	public Bone[] bone = new Bone[0];

	public static final String key = "BONE";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "BONE");
		int chunkSize = in.readInt();
		List<Bone> boneList = new ArrayList();
		int boneCounter = chunkSize;
		while (boneCounter > 0) {
			Bone tempbone = new Bone();
			boneList.add(tempbone);
			tempbone.load(in);
			boneCounter -= tempbone.getSize();
		}
		bone = boneList.toArray(new Bone[boneList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfBones = bone.length;
		out.writeNByteString("BONE", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < bone.length; i++) {
			bone[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < bone.length; i++) {
			a += bone[i].getSize();
		}

		return a;
	}

	public class Bone {
		public Node node = new Node();
		public int geosetId;
		public int geosetAnimationId;

		public void load(BlizzardDataInputStream in) throws IOException {
			node = new Node();
			node.load(in);
			geosetId = in.readInt();
			geosetAnimationId = in.readInt();
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			node.save(out);
			out.writeInt(geosetId);
			out.writeInt(geosetAnimationId);

		}

		public int getSize() {
			int a = 0;
			a += node.getSize();
			a += 4;
			a += 4;

			return a;
		}
		
		public Bone() {
			
		}
		public Bone(com.hiveworkshop.wc3.mdl.Bone bone) {
			node = new Node(bone);
			node.flags |= 0x100;
			geosetId = bone.getGeosetId();
			geosetAnimationId = bone.getGeosetAnimId();
		}
	}
}
