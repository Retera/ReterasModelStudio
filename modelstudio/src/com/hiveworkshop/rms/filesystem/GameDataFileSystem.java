package com.hiveworkshop.rms.filesystem;

import com.hiveworkshop.rms.filesystem.sources.*;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class GameDataFileSystem {
	private static CompoundDataSource current;

	public static CompoundDataSource getDefault() {
		if (current == null) {
			final List<DataSource> dataSources = new ArrayList<>();
			dataSources.add(new JavaJarDataSource());
			if (SaveProfileNew.get().hasDataSources()) {
				final List<DataSourceDescriptor> dataSourceDescriptors = SaveProfileNew.get().getDataSources();
				for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
					try {
						dataSources.add(descriptor.createDataSource());
					} catch (Exception e){
						System.err.println("failed to load data source " + descriptor.getClass().getSimpleName());
						e.printStackTrace();
					}
				}
				System.out.println(dataSources.size()-1 + " out of " + dataSourceDescriptors.size() + " data sources loaded");
				if(dataSources.size()-1 != dataSourceDescriptors.size()){
					int tot = dataSourceDescriptors.size();
					int loaded = dataSources.size()-1;
					int failed = tot - loaded;
					System.err.println("Failed to load " + failed + " / " + tot + " data sources");
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Failed to load " + failed + " / " + tot + " data sources"));
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
