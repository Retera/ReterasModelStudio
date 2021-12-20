package com.hiveworkshop.rms.filesystem.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class CompoundDataSource implements DataSource {
	private final List<DataSource> mpqList = new ArrayList<>();
	Map<String, File> cache = new HashMap<>();

	public CompoundDataSource(final List<DataSource> dataSources) {
		if (dataSources != null) {
			mpqList.addAll(dataSources);
		}
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
			if (mpq.has(filepath)) {
				return mpq.allowDownstreamCaching(filepath);
			}
		}
		return false;
	}

	@Override
	public File getFile(final String filepath) {
		if (cache.containsKey(filepath)) {
			return cache.get(filepath);
		}
		try {
//			for (int i = mpqList.size() - 1; i >= 0; i--) {
//				final DataSource mpq = mpqList.get(i);
//				final File tempProduct = mpq.getFile(filepath);
//				if (tempProduct != null) {
//					cache.put(filepath, tempProduct);
//					return tempProduct;
//				}
////				else {
////					JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(), "Could not find \"" + filepath + "\"", "File not found", JOptionPane.ERROR_MESSAGE);
////				}
//			}
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final DataSource mpq = mpqList.get(i);
				final File tempProduct = mpq.getFile(filepath);
				if (tempProduct != null) {
					cache.put(filepath, tempProduct);
					return tempProduct;
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) {
		try {
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final DataSource mpq = mpqList.get(i);
				final InputStream resourceAsStream = mpq.getResourceAsStream(filepath);
//				if (filepath.endsWith(".html")){
//					System.out.println("checking for file: \"" + filepath
//							+ "\", in: \"" + mpq.getClass().getSimpleName()
//							+ "\", has: " + mpq.has(filepath) + ", stream: " + resourceAsStream);
//				}
				if (resourceAsStream != null) {
					return resourceAsStream;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ByteBuffer read(final String filePath) throws IOException {
		try {
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final DataSource mpq = mpqList.get(i);
				final ByteBuffer buffer = mpq.read(filePath);
				if (buffer != null) {
					return buffer;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean has(final String filepath) {
//		String fp = "/" + filepath.replace('\\', '/');
//		if (filepath.endsWith(".html")){
//			System.out.println("checking for file: \"" + filepath + "\", cashed: \"" + cache.containsKey(filepath) + "\"");
//		}
		if (cache.containsKey(filepath)) {
			return true;
		}
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
//			if (filepath.endsWith(".html")){
//				System.out.println("checking for file: \"" + filepath + "\", in: \"" + mpq.getClass().getSimpleName() + "\", has: " + mpq.has(filepath));
//			}
			if (mpq.has(filepath)) {
				return true;
			}
		}
		return false;
	}

	public void refresh(final List<DataSourceDescriptor> dataSourceDescriptors) {
		for (final DataSource dataSource : mpqList) {
			try {
				dataSource.close();
			} catch (final NullPointerException | IOException e) {
				e.printStackTrace();
			}
        }
		cache.clear();
		mpqList.clear();
		if (dataSourceDescriptors != null) {
			for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
				mpqList.add(descriptor.createDataSource());
			}
		}
	}

	public Set<String> getMergedListfile() {
		final Set<String> listfile = new HashSet<>();
		for (DataSource mpqGuy : mpqList) {
			Collection<String> dataSourceListfile = mpqGuy.getListfile();
			if (dataSourceListfile != null) {
                listfile.addAll(dataSourceListfile);
			}
		}
		return listfile;
	}

	@Override
	public Collection<String> getListfile() {
		return getMergedListfile();
	}

	@Override
	public void close() throws IOException {
		for (final DataSource mpqGuy : mpqList) {
			mpqGuy.close();
		}
	}
}
