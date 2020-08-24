package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Iterator;
import java.util.List;

/**
 * A generic object.
 * <p>
 * The parent class for all objects that exist in the world, and may contain
 * spatial animations. This includes bones, particle emitters, and many other
 * things.
 * <p>
 * Based on the works of Chananya Freiman.
 */
public abstract class MdlxGenericObject extends MdlxAnimatedObject {
	public String name = "";
	public int objectId = -1;
	public int parentId = -1;
	public int flags = 0;

	public MdlxGenericObject(final int flags) {
		this.flags = flags;
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final long size = reader.readUInt32();

		name = reader.read(80);
		objectId = reader.readInt32();
		parentId = reader.readInt32();
		flags = reader.readInt32();

		readTimelines(reader, size - 96);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getGenericByteLength(version));
		writer.writeWithNulls(name, 80);
		writer.writeInt32(objectId);
		writer.writeInt32(parentId);
		writer.writeInt32(flags);

		for (final MdlxTimeline<?> timeline : eachTimeline(true)) {
			timeline.writeMdx(writer);
		}
	}

	public void writeNonGenericAnimationChunks(final BinaryWriter writer) {
		for (final MdlxTimeline<?> timeline : eachTimeline(false)) {
			timeline.writeMdx(writer);
		}
	}

	protected final Iterable<String> readMdlGeneric(final MdlTokenInputStream stream) {
		name = stream.read();
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new WrappedMdlTokenIterator(readAnimatedBlock(stream), MdlxGenericObject.this,
						stream);
			}
		};
	}

	public void writeGenericHeader(final MdlTokenOutputStream stream) {
		stream.writeAttrib(MdlUtils.TOKEN_OBJECTID, objectId);

		if (parentId != -1) {
			stream.writeAttrib("Parent", parentId);
		}

		if ((flags & 0x40) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_BILLBOARDED_LOCK_Z);
		}

		if ((flags & 0x20) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_BILLBOARDED_LOCK_Y);
		}

		if ((flags & 0x10) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_BILLBOARDED_LOCK_X);
		}

		if ((flags & 0x8) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_BILLBOARDED);
		}

		if ((flags & 0x80) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_CAMERA_ANCHORED);
		}

		if ((flags & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_DONT_INHERIT + " { " + MdlUtils.TOKEN_ROTATION + " }");
		}

		if ((flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_DONT_INHERIT + " { " + MdlUtils.TOKEN_TRANSLATION + " }");
		}

		if ((flags & 0x4) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_DONT_INHERIT + " { " + MdlUtils.TOKEN_SCALING + " }");
		}
	}

	public void writeGenericTimelines(final MdlTokenOutputStream stream) {
		writeTimeline(stream, AnimationMap.KGTR);
		writeTimeline(stream, AnimationMap.KGRT);
		writeTimeline(stream, AnimationMap.KGSC);
	}

	public Iterable<MdlxTimeline<?>> eachTimeline(final boolean generic) {
		return new TimelineMaskingIterable(generic);
	}

	public long getGenericByteLength(final int version) {
		long size = 96;
		for (final MdlxTimeline<?> animation : eachTimeline(true)) {
			size += animation.getByteLength();
		}
		return size;
	}

	@Override
	public long getByteLength(final int version) {
		return 96 + super.getByteLength(version);
	}

	private final class TimelineMaskingIterable implements Iterable<MdlxTimeline<?>> {
		private final boolean generic;

		private TimelineMaskingIterable(final boolean generic) {
			this.generic = generic;
		}

		@Override
		public Iterator<MdlxTimeline<?>> iterator() {
			return new TimelineMaskingIterator(generic, timelines);
		}
	}

	private static final class TimelineMaskingIterator implements Iterator<MdlxTimeline<?>> {
		private final boolean wantGeneric;
		private final Iterator<MdlxTimeline<?>> delegate;
		private boolean hasNext;
		private MdlxTimeline<?> next;

		public TimelineMaskingIterator(final boolean wantGeneric, final List<MdlxTimeline<?>> timelines) {
			this.wantGeneric = wantGeneric;
			delegate = timelines.iterator();
			scanUntilNext();
		}

		private boolean isGeneric(final MdlxTimeline<?> timeline) {
			final War3ID name = timeline.name;
			final boolean generic = AnimationMap.KGTR.getWar3id().equals(name)
					|| AnimationMap.KGRT.getWar3id().equals(name) || AnimationMap.KGSC.getWar3id().equals(name);
			return generic;
		}

		private void scanUntilNext() {
			boolean hasNext = false;
			if (hasNext = delegate.hasNext()) {
				do {
					next = delegate.next();
				}
				while ((isGeneric(next) != wantGeneric) && (hasNext = delegate.hasNext()));
			}
			if (!hasNext) {
				next = null;
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public MdlxTimeline<?> next() {
			final MdlxTimeline<?> last = next;
			scanUntilNext();
			return last;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported");
		}

	}

	private static final class WrappedMdlTokenIterator implements Iterator<String> {
		private final Iterator<String> delegate;
		private final MdlxGenericObject updatingObject;
		private final MdlTokenInputStream stream;
		private String next;
		private boolean hasLoaded = false;

		public WrappedMdlTokenIterator(final Iterator<String> delegate, final MdlxGenericObject updatingObject,
									   final MdlTokenInputStream stream) {
			this.delegate = delegate;
			this.updatingObject = updatingObject;
			this.stream = stream;
		}

		@Override
		public boolean hasNext() {
			if (delegate.hasNext()) {
				next = read();
				hasLoaded = true;
				return next != null;
			}
			return false;
		}

		@Override
		public String next() {
			if (!hasLoaded) {
				next = read();
			}
			hasLoaded = false;
			return next;
		}

		private String read() {
			String token;
			InteriorParsing:
			do {
				token = delegate.next();
				if (token == null) {
					break;
				}
				switch (token) {
					case MdlUtils.TOKEN_OBJECTID:
						updatingObject.objectId = Integer.parseInt(delegate.next());
						token = null;
						break;
					case MdlUtils.TOKEN_PARENT:
						updatingObject.parentId = Integer.parseInt(delegate.next());
						token = null;
						break;
					case MdlUtils.TOKEN_BILLBOARDED_LOCK_Z:
						updatingObject.flags |= 0x40;
						token = null;
						break;
					case MdlUtils.TOKEN_BILLBOARDED_LOCK_Y:
						updatingObject.flags |= 0x20;
						token = null;
						break;
					case MdlUtils.TOKEN_BILLBOARDED_LOCK_X:
						updatingObject.flags |= 0x10;
						token = null;
						break;
					case MdlUtils.TOKEN_BILLBOARDED:
						updatingObject.flags |= 0x8;
						token = null;
						break;
					case MdlUtils.TOKEN_CAMERA_ANCHORED:
						updatingObject.flags |= 0x80;
						token = null;
						break;
					case MdlUtils.TOKEN_DONT_INHERIT:
						for (final String subToken : stream.readBlock()) {
							switch (subToken) {
								case MdlUtils.TOKEN_ROTATION:
									updatingObject.flags |= 0x2;
									break;
								case MdlUtils.TOKEN_TRANSLATION:
									updatingObject.flags |= 0x1;
									break;
								case MdlUtils.TOKEN_SCALING:
									updatingObject.flags |= 0x0;
									break;
							}
						}
						token = null;
						break;
					case MdlUtils.TOKEN_TRANSLATION:
						updatingObject.readTimeline(stream, AnimationMap.KGTR);
						token = null;
						break;
					case MdlUtils.TOKEN_ROTATION:
						updatingObject.readTimeline(stream, AnimationMap.KGRT);
						token = null;
						break;
					case MdlUtils.TOKEN_SCALING:
						updatingObject.readTimeline(stream, AnimationMap.KGSC);
						token = null;
						break;
					default:
						break InteriorParsing;
				}
			}
			while (delegate.hasNext());
			return token;
		}

	}
}
