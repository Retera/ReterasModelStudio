package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class AttachmentChunk {
	public Attachment[] attachment = new Attachment[0];

	public static final String key = "ATCH";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "ATCH");
		final int chunkSize = in.readInt();
		final List<Attachment> attachmentList = new ArrayList();
		int attachmentCounter = chunkSize;
		while (attachmentCounter > 0) {
			final Attachment tempattachment = new Attachment();
			attachmentList.add(tempattachment);
			tempattachment.load(in);
			attachmentCounter -= tempattachment.getSize();
		}
		attachment = attachmentList.toArray(new Attachment[attachmentList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfAttachments = attachment.length;
		out.writeNByteString("ATCH", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < attachment.length; i++) {
			attachment[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < attachment.length; i++) {
			a += attachment[i].getSize();
		}

		return a;
	}

	public class Attachment {
		public Node node = new Node();
		public String unknownName_modelPath = "";
		public int unknownNull;
		public int attachmentId;
		public AttachmentVisibility attachmentVisibility;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			unknownName_modelPath = in.readCharsAsString(256);
			unknownNull = in.readInt();
			attachmentId = in.readInt();
			if (MdxUtils.checkOptionalId(in, AttachmentVisibility.key)) {
				attachmentVisibility = new AttachmentVisibility();
				attachmentVisibility.load(in);
			}

		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeNByteString(unknownName_modelPath, 256);
			out.writeInt(unknownNull);
			out.writeInt(attachmentId);
			if (attachmentVisibility != null) {
				attachmentVisibility.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += node.getSize();
			a += 256;
			a += 4;
			a += 4;
			if (attachmentVisibility != null) {
				a += attachmentVisibility.getSize();
			}

			return a;
		}

		public Attachment() {

		}

		public Attachment(final com.hiveworkshop.wc3.mdl.Attachment atc) {
			node = new Node(atc);
			node.flags |= 0x800;
			unknownName_modelPath = atc.getPath();
			attachmentId = atc.getAttachmentID();
			for (final AnimFlag af : atc.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					attachmentVisibility = new AttachmentVisibility();
					attachmentVisibility.globalSequenceId = af.getGlobalSeqId();
					attachmentVisibility.interpolationType = af.getInterpType();
					attachmentVisibility.scalingTrack = new AttachmentVisibility.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final AttachmentVisibility.ScalingTrack mdxEntry = attachmentVisibility.new ScalingTrack();
						attachmentVisibility.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
		}
	}
}
