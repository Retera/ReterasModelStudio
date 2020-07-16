package com.hiveworkshop.wc3.gui.datachooser;

import java.io.Serializable;

public interface DataSourceDescriptor extends Serializable {
	DataSource createDataSource();

	String getDisplayName();

	DataSourceDescriptor duplicate();
}
