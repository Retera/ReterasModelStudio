package com.hiveworkshop.rms.filesystem.sources;

import systems.crigges.jmpq3.JMpqEditor;
import systems.crigges.jmpq3.MPQOpenOption;

import java.nio.file.Paths;
import java.util.Objects;

public class MpqDataSourceDescriptor implements DataSourceDescriptor {
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 8424254987711783598L;
	private final String mpqFilePath;

	public MpqDataSourceDescriptor(final String mpqFilePath) {
		this.mpqFilePath = mpqFilePath;
	}

	@Override
	public MpqDataSource createDataSource() {
		try {
			return new MpqDataSource(new JMpqEditor(Paths.get(mpqFilePath), MPQOpenOption.READ_ONLY));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDisplayName() {
		return "MPQ Archive: " + mpqFilePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((mpqFilePath == null) ? 0 : mpqFilePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof final MpqDataSourceDescriptor other) {
			return Objects.equals(mpqFilePath, other.mpqFilePath);
		}
		return false;
	}

	public String getMpqFilePath() {
		return mpqFilePath;
	}
	public String getPath() {
		return mpqFilePath;
	}

	@Override
	public MpqDataSourceDescriptor duplicate() {
		return new MpqDataSourceDescriptor(mpqFilePath);
	}
}
