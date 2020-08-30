package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;

public interface MdlxTimelineDescriptor {
	MdlxTimeline<?> createTimeline();

	MdlxTimelineDescriptor UINT32_TIMELINE = () -> new MdlxUInt32Timeline();

	MdlxTimelineDescriptor FLOAT_TIMELINE = () -> new MdlxFloatTimeline();

	MdlxTimelineDescriptor VECTOR3_TIMELINE = () -> new MdlxFloatArrayTimeline(3);

	MdlxTimelineDescriptor VECTOR4_TIMELINE = () -> new MdlxFloatArrayTimeline(4);
}
