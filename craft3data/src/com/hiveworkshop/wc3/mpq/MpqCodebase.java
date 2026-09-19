package com.hiveworkshop.wc3.mpq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.etheller.collections.HashSet;
import com.etheller.collections.Set;
import com.etheller.collections.SetView;
import com.etheller.util.CollectionUtils;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.gui.datachooser.JavaJarDataSource;
import com.hiveworkshop.wc3.gui.datachooser.MpqDataSourceDescriptor;
import com.hiveworkshop.wc3.user.SaveProfile;

import mpq.MPQException;

public class MpqCodebase implements Codebase, DataSource {
	private final List<DataSource> mpqList = new ArrayList<>();

	public MpqCodebase(final List<DataSourceDescriptor> dataSourceDescriptors) {
		mpqList.add(new JavaJarDataSource());
		if (dataSourceDescriptors != null) {
			for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
				mpqList.add(descriptor.createDataSource());
			}
		}
	}

	Map<String, File> cache = new HashMap<>();

	@Override
	public File getFile(final String filepath) {
		if (cache.containsKey(filepath)) {
			return cache.get(filepath);
		}
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
			try {
				final File tempProduct = mpq.getFile(filepath);
				if (tempProduct != null) {
					cache.put(filepath, tempProduct);
					return tempProduct;
				}
			} catch (final IOException e) {
				reportSourceFailure(mpq, filepath, e);
			}
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) {
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
			try {
				final InputStream resourceAsStream = mpq.getResourceAsStream(filepath);
				if (resourceAsStream != null) {
					return resourceAsStream;
				}
			} catch (final IOException e) {
				reportSourceFailure(mpq, filepath, e);
			}
		}
		return null;
	}

	@Override
	public ByteBuffer read(final String path) throws IOException {
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
			try {
				final ByteBuffer resourceAsStream = mpq.read(path);
				if (resourceAsStream != null) {
					return resourceAsStream;
				}
			} catch (final IOException e) {
				reportSourceFailure(mpq, path, e);
			}
		}
		return null;
	}

	/**
	 * One source failing to read a file should neither abort the lookup in the
	 * remaining sources nor be silent. Failures show up later as missing textures
	 * or data, so say which source and which path so they can be traced.
	 */
	private static void reportSourceFailure(final DataSource source, final String filepath, final IOException e) {
		System.err.println("MpqCodebase: " + source.getClass().getSimpleName() + " failed to read \"" + filepath
				+ "\": " + e);
		e.printStackTrace();
	}

	@Override
	public boolean has(final String filepath) {
		if (cache.containsKey(filepath)) {
			return true;
		}
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = mpqList.get(i);
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
			} catch (final NullPointerException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		cache.clear();
		mpqList.clear();
		mpqList.add(new JavaJarDataSource());
		if (dataSourceDescriptors != null) {
			for (final DataSourceDescriptor descriptor : dataSourceDescriptors) {
				mpqList.add(descriptor.createDataSource());
			}
		}
	}

	public LoadedMPQ loadMPQ(final Path path) throws IOException, MPQException {
		final DataSource mpqDataSource = new MpqDataSourceDescriptor(path.toString()).createDataSource();
		mpqList.add(mpqDataSource);
		return new LoadedMPQ() {
			@Override
			public void unload() {
				mpqList.remove(mpqDataSource);
				try {
					mpqDataSource.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean hasListfile() {
				return mpqDataSource.has("(listfile)");
			}

			@Override
			public boolean has(final String path) {
				return mpqDataSource.has(path);
			}
		};
	}

	public interface LoadedMPQ {
		void unload();

		boolean hasListfile();

		boolean has(String path);
	}

	public SetView<String> getMergedListfile() {
		final Set<String> listfile = new HashSet<>();
		for (final DataSource mpqGuy : mpqList) {
			final Collection<String> dataSourceListfile = mpqGuy.getListfile();
			if (dataSourceListfile != null) {
				for (final String element : dataSourceListfile) {
					listfile.add(element);
				}
			}
		}
		return listfile;
	}

	private static MpqCodebase current;

	public static MpqCodebase get() {
		if (current == null) {
			current = new MpqCodebase(SaveProfile.get().getDataSources());
		}
		return current;
	}

	@Override
	public boolean allowDownstreamCaching(final String filepath) {
		for (int i = this.mpqList.size() - 1; i >= 0; i--) {
			final DataSource mpq = this.mpqList.get(i);
			if (mpq.has(filepath)) {
				return mpq.allowDownstreamCaching(filepath);
			}
		}
		return false;
	}

	@Override
	public Collection<String> getListfile() {
		return CollectionUtils.toJava(getMergedListfile());
	}

	@Override
	public void close() throws IOException {
		for (final DataSource mpqGuy : this.mpqList) {
			mpqGuy.close();
		}
	}
}
