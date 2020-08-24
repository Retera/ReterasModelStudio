package com.hiveworkshop.rms.filesystem.sources;

import java.io.Serializable;

public interface DataSourceDescriptor extends Serializable {
	DataSource createDataSource();

	String getDisplayName();

	DataSourceDescriptor duplicate();
}
