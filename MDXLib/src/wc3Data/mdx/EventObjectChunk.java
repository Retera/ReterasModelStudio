package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class EventObjectChunk {
	public EventObject[] eventObject = new EventObject[0];

	public static final String key = "EVTS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "EVTS");
		int chunkSize = in.readInt();
		List<EventObject> eventObjectList = new ArrayList();
		int eventObjectCounter = chunkSize;
		while (eventObjectCounter > 0) {
			EventObject tempeventObject = new EventObject();
			eventObjectList.add(tempeventObject);
			tempeventObject.load(in);
			eventObjectCounter -= tempeventObject.getSize();
		}
		eventObject = eventObjectList.toArray(new EventObject[eventObjectList
				.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfEventObjects = eventObject.length;
		out.writeNByteString("EVTS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < eventObject.length; i++) {
			eventObject[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < eventObject.length; i++) {
			a += eventObject[i].getSize();
		}

		return a;
	}

	public class EventObject {
		public Node node = new Node();
		public Tracks tracks;

		public void load(BlizzardDataInputStream in) throws IOException {
			node = new Node();
			node.load(in);
			if (MdxUtils.checkOptionalId(in, tracks.key)) {
				tracks = new Tracks();
				tracks.load(in);
			}

		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			node.save(out);
			if (tracks != null) {
				tracks.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += node.getSize();
			if (tracks != null) {
				a += tracks.getSize();
			}

			return a;
		}
	}
}
