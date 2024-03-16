package com.hiveworkshop.wc3.gui.datachooser;

public class JavaJarDataSourceDescriptor implements DataSourceDescriptor {
	private Class<?> sourceClass;

	public JavaJarDataSourceDescriptor(Class<?> sourceClass) {
		this.sourceClass = sourceClass;
	}

	@Override
	public DataSource createDataSource() {
		return new JavaJarDataSource(sourceClass);
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