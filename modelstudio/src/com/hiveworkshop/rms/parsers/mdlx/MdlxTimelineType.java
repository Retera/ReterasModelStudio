package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;

import java.util.function.Supplier;

public enum MdlxTimelineType {
	UINT32_TIMELINE(MdlxUInt32Timeline::new),
	FLOAT_TIMELINE(MdlxFloatTimeline::new),
	VECTOR3_TIMELINE(() -> new MdlxFloatArrayTimeline(3)),
	VECTOR4_TIMELINE(() -> new MdlxFloatArrayTimeline(4));

	Supplier<MdlxTimeline<?>> timelineCreator;

	MdlxTimelineType(Supplier<MdlxTimeline<?>> timelineCreator) {
		this.timelineCreator = timelineCreator;
	}

	public MdlxTimeline<?> createTimeline() {
		return timelineCreator.get();
	}
}
