package com.hiveworkshop.rms.parsers.mdlx;

import java.util.Iterator;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

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

		for (final MdlxTimeline<?> timeline : timelines) {
			if (isGeneric(timeline)) {
				timeline.writeMdx(writer);
			}
		}
	}

	public void writeNonGenericAnimationChunks(final BinaryWriter writer) {
		for (final MdlxTimeline<?> timeline : timelines) {
			if (!isGeneric(timeline)) {
				timeline.writeMdx(writer);
			}
		}
	}

	protected final Iterable<String> readMdlGeneric(final MdlTokenInputStream stream) {
		name = stream.read();
		return () -> new WrappedMdlTokenIterator(readAnimatedBlock(stream), MdlxGenericObject.this,
                stream);
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

	public long getGenericByteLength(final int version) {
		long size = 96;

		for (final MdlxTimeline<?> timeline : timelines) {
			if (isGeneric(timeline)) {
				size += timeline.getByteLength();
			}
		}

		return size;
	}

	public boolean isGeneric(final MdlxTimeline<?> timeline) {
		AnimationMap type = AnimationMap.ID_TO_TAG.get(timeline.name);

		return (type == AnimationMap.KGTR) || (type == AnimationMap.KGRT) || (type == AnimationMap.KGSC);
	}

	@Override
	public long getByteLength(final int version) {
		return 96 + super.getByteLength(version);
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
								case MdlUtils.TOKEN_ROTATION -> updatingObject.flags |= 0x2;
								case MdlUtils.TOKEN_TRANSLATION -> updatingObject.flags |= 0x1;
								case MdlUtils.TOKEN_SCALING -> updatingObject.flags |= 0x0;
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
