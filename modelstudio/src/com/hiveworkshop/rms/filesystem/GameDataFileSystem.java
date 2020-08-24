package com.hiveworkshop.rms.filesystem;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;

import java.util.ArrayList;
import java.util.List;

public final class GameDataFileSystem {
	private static CompoundDataSource current;

	public static CompoundDataSource getDefault() {
		if (current == null) {
			final List<DataSourceDescriptor> dataSourceDescriptors = SaveProfile.get().getDataSources();
			final List<DataSource> dataSources = new ArrayList<>();
			dataSources.add(new JavaJarDataSource());
			if (dataSourceDescriptors != null) {
				for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
					dataSources.add(descriptor.createDataSource());
				}
			}
			current = new CompoundDataSource(dataSources);
		}
		return current;
	}

	public static void refresh(final List<DataSourceDescriptor> dataSources) {
		final List<DataSourceDescriptor> dataSourcesFixed = new ArrayList<>();
		dataSourcesFixed.add(new JavaJarDataSourceDescriptor());
		dataSourcesFixed.addAll(dataSources);
		getDefault().refresh(dataSourcesFixed);
	}
}
