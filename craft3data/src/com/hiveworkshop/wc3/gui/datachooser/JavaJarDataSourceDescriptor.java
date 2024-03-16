package com.hiveworkshop.wc3.gui.datachooser;

public class JavaJarDataSourceDescriptor implements DataSourceDescriptor {
	@Override
	public DataSource createDataSource() {
		return new JavaJarDataSource();
	}

	@Override
	public String getDisplayName() {
		return "JAR";
	}

	@Override
	public DataSourceDescriptor duplicate() {
		return this;
	}
}