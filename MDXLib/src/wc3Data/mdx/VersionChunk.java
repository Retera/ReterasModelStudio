package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class VersionChunk {
	public int version;

	public static final String key = "VERS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "VERS");
		int chunkSize = in.readInt();
		version = in.readInt();
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("VERS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		out.writeInt(version);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 4;

		return a;
	}
}
