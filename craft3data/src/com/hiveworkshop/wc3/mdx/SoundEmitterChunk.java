package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class SoundEmitterChunk {
	public SoundEmitter[] soundEmitter = new SoundEmitter[0];

	public static final String key = "SNEM";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, key);
		int chunkSize = in.readInt();
		List<SoundEmitter> soundEmitterList = new ArrayList();
		int soundEmitterCounter = chunkSize;
		while (soundEmitterCounter > 0) {
			SoundEmitter tempSoundEmitter = new SoundEmitter();
			soundEmitterList.add(tempSoundEmitter);
			tempSoundEmitter.load(in);
			soundEmitterCounter -= tempSoundEmitter.getSize();
		}
		soundEmitter = soundEmitterList.toArray(new SoundEmitter[soundEmitterList
				.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfSoundEmitters = soundEmitter.length;
		out.writeNByteString(key, 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < soundEmitter.length; i++) {
			soundEmitter[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < soundEmitter.length; i++) {
			a += soundEmitter[i].getSize();
		}

		return a;
	}

	public class SoundEmitter {
		public Node node = new Node();
		public Tracks tracks;

		public void load(BlizzardDataInputStream in) throws IOException {
			node = new Node();
			node.load(in);
			if (MdxUtils.checkOptionalId(in, Tracks.key)) {
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
		public SoundEmitter() {
			
		}
		public SoundEmitter(com.hiveworkshop.wc3.mdl.EventObject other) {
			node = new Node(other);
			node.flags |= 0x400;
			tracks = new Tracks();
			tracks.globalSequenceId = other.getGlobalSeqId();
			tracks.tracks = new int[other.getEventTrack().size()];
			for( int i = 0; i < tracks.tracks.length; i++ ) {
				tracks.tracks[i] = other.getEventTrack().get(i);
			}
		}
	}
}
