package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class AttachmentChunk {
	public Attachment[] attachment = new Attachment[0];

	public static final String key = "ATCH";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "ATCH");
		int chunkSize = in.readInt();
		List<Attachment> attachmentList = new ArrayList();
		int attachmentCounter = chunkSize;
		while (attachmentCounter > 0) {
			Attachment tempattachment = new Attachment();
			attachmentList.add(tempattachment);
			tempattachment.load(in);
			attachmentCounter -= tempattachment.getSize();
		}
		attachment = attachmentList.toArray(new Attachment[attachmentList
				.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfAttachments = attachment.length;
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
		public String unknownName = "";
		public int unknownNull;
		public int attachmentId;
		public AttachmentVisibility attachmentVisibility;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			unknownName = in.readCharsAsString(256);
			unknownNull = in.readInt();
			attachmentId = in.readInt();
			if (MdxUtils.checkOptionalId(in, attachmentVisibility.key)) {
				attachmentVisibility = new AttachmentVisibility();
				attachmentVisibility.load(in);
			}

		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeNByteString(unknownName, 256);
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
	}
}
