package com.hiveworkshop.rms.filesystem.sources;

import java.util.ArrayList;
import java.util.List;

public class CompoundDataSourceDescriptor implements DataSourceDescriptor {
	private final List<DataSourceDescriptor> dataSourceDescriptors;

	public CompoundDataSourceDescriptor(final List<DataSourceDescriptor> dataSourceDescriptors) {
		this.dataSourceDescriptors = dataSourceDescriptors;
	}

	@Override
	public DataSource createDataSource() {
		final List<DataSource> dataSources = new ArrayList<>();
		for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
			dataSources.add(descriptor.createDataSource());
		}
		return new CompoundDataSource(dataSources);
	}

	@Override
	public String getDisplayName() {
		return "CompoundDataSourceDescriptor";
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceDescriptors;
	}

	@Override
	public DataSourceDescriptor duplicate() {
		return new CompoundDataSourceDescriptor(new ArrayList<>(dataSourceDescriptors));
	}
}
