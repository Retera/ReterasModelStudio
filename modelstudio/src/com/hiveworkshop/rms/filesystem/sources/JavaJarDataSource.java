package com.hiveworkshop.rms.filesystem.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

public class JavaJarDataSource implements DataSource {
    @Override
    public InputStream getResourceAsStream(final String filepath) throws IOException {
        return JavaJarDataSource.class.getResourceAsStream("/" + filepath.replace('\\', '/'));
    }

    @Override
    public File getFile(final String filepath) throws IOException {
        final InputStream newInputStream = getResourceAsStream(filepath);
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (!tmpdir.endsWith(File.separator)) {
            tmpdir += File.separator;
        }
        final String tempDir = tmpdir + "RMSExtract/";
        final File tempProduct = new File(tempDir + filepath.replace('\\', File.separatorChar));
        tempProduct.delete();
        tempProduct.getParentFile().mkdirs();
        Files.copy(newInputStream, tempProduct.toPath());
        tempProduct.deleteOnExit();
        return tempProduct;
    }

    @Override
    public ByteBuffer read(final String path) throws IOException {
        final InputStream stream = getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return ByteBuffer.wrap(stream.readAllBytes());
    }

    @Override
    public boolean has(final String filepath) {
        return JavaJarDataSource.class.getResource("/" + filepath.replace('\\', '/')) != null;
    }

    @Override
    public boolean allowDownstreamCaching(final String filepath) {
        return true;
    }

    @Override
    public Collection<String> getListfile() {
        return Collections.emptySet(); // breaks API but not easy from Java and not needed
    }

    @Override
    public void close() throws IOException {
        // breaks API but not easy from Java and not needed
    }
}
