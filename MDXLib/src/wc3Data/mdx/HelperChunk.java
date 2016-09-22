package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class HelperChunk {
	public Helper[] helper = new Helper[0];

	public static final String key = "HELP";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "HELP");
		int chunkSize = in.readInt();
		List<Helper> helperList = new ArrayList();
		int helperCounter = chunkSize;
		while (helperCounter > 0) {
			Helper temphelper = new Helper();
			helperList.add(temphelper);
			temphelper.load(in);
			helperCounter -= temphelper.getSize();
		}
		helper = helperList.toArray(new Helper[helperList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfHelpers = helper.length;
		out.writeNByteString("HELP", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < helper.length; i++) {
			helper[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < helper.length; i++) {
			a += helper[i].getSize();
		}

		return a;
	}

	public class Helper {
		public Node node = new Node();

		public void load(BlizzardDataInputStream in) throws IOException {
			node = new Node();
			node.load(in);
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			node.save(out);

		}

		public int getSize() {
			int a = 0;
			a += node.getSize();

			return a;
		}
	}
}
