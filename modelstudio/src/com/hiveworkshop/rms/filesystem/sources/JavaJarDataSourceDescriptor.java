package com.hiveworkshop.rms.filesystem.sources;

public class JavaJarDataSourceDescriptor implements DataSourceDescriptor {
    @Override
    public JavaJarDataSource createDataSource() {
        return new JavaJarDataSource();
    }

    @Override
    public String getDisplayName() {
        return "JAR";
    }

    @Override
    public JavaJarDataSourceDescriptor duplicate() {
        return this;
    }


    public String getPath() {
        return null;
    }
}
