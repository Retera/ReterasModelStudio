package com.hiveworkshop.rms.filesystem.sources;

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
