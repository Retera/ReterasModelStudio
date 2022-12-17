package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Based on the works of Chananya Freiman.
 */
public abstract class MdlxAnimatedObject implements MdlxChunk, MdlxBlock {
	public final List<MdlxTimeline<?>> timelines = new ArrayList<>();

	public void readTimelines(final BinaryReader reader, long size) {
		while (0 < size) {
			final War3ID name = new War3ID(reader.readTag());
			AnimationMap animationMap = AnimationMap.ID_TO_TAG.get(name);
			if(animationMap != null){
				size -=  readTimeline(reader, animationMap);
			} else {
				System.out.println("couldn't find tag for: " + name);
				break;
			}
		}
	}

	public long readTimeline(BinaryReader reader, AnimationMap animationMap) {
		final MdlxTimeline<?> timeline = animationMap.getNewTimeline();
		timeline.readMdx(reader);

		timelines.add(timeline);
		return timeline.getByteLength();
	}

	public void writeTimelines(final BinaryWriter writer) {
		for (final MdlxTimeline<?> timeline : timelines) {
			timeline.writeMdx(writer);
		}
	}

	public Iterator<String> readAnimatedBlock(final MdlTokenInputStream stream) {
		return new TransformedAnimatedBlockIterator(stream.readBlock().iterator());
	}

	public void readTimeline(final MdlTokenInputStream stream, final AnimationMap name) {
		final MdlxTimeline<?> timeline = name.getNewTimeline();

		timeline.readMdl(stream);

		timelines.add(timeline);
	}

	public boolean writeTimeline(final MdlTokenOutputStream stream, final AnimationMap name) {
		for (final MdlxTimeline<?> timeline : timelines) {
			if (timeline.name.equals(name.getWar3id())) {
				timeline.writeMdl(stream);
				return true;
			}
		}
		return false;
	}

	@Override
	public long getByteLength(final int version) {
		long size = 0;
		for (final MdlxTimeline<?> timeline : timelines) {
			size += timeline.getByteLength();
		}
		return size;
	}

	/**
	 * TODO: This code uses StringBuilder implicitly during string concat. This
	 * should be upgraded for performance.
	 *
	 * @author micro
	 */
	private static final class TransformedAnimatedBlockIterator implements Iterator<String> {
		private final Iterator<String> delegate;

		public TransformedAnimatedBlockIterator(final Iterator<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public String next() {
			final String token = delegate.next();
			if (token.equals(MdlUtils.TOKEN_STATIC) && hasNext()) {
				return MdlUtils.TOKEN_STATIC + " " + delegate.next();
			}
			return token;
		}
	}
}
