package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxTimeline;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxUInt32Timeline;

public interface MdlxTimelineDescriptor {
	MdlxTimeline<?> createTimeline();

	public static final MdlxTimelineDescriptor UINT32_TIMELINE = new MdlxTimelineDescriptor() {
		@Override
		public MdlxUInt32Timeline createTimeline() {
			return new MdlxUInt32Timeline();
		}
	};

	public static final MdlxTimelineDescriptor FLOAT_TIMELINE = new MdlxTimelineDescriptor() {
		@Override
		public MdlxFloatTimeline createTimeline() {
			return new MdlxFloatTimeline();
		}
	};

	public static final MdlxTimelineDescriptor VECTOR3_TIMELINE = new MdlxTimelineDescriptor() {
		@Override
		public MdlxFloatArrayTimeline createTimeline() {
			return new MdlxFloatArrayTimeline(3);
		}
	};

	public static final MdlxTimelineDescriptor VECTOR4_TIMELINE = new MdlxTimelineDescriptor() {
		@Override
		public MdlxFloatArrayTimeline createTimeline() {
			return new MdlxFloatArrayTimeline(4);
		}
	};
}
