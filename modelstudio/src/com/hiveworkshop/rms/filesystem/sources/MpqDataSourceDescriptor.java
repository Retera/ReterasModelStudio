package com.hiveworkshop.rms.filesystem.sources;

import systems.crigges.jmpq3.*;

import java.nio.file.Paths;

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
	public DataSource createDataSource() {
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MpqDataSourceDescriptor other = (MpqDataSourceDescriptor) obj;
		if (mpqFilePath == null) {
			return other.mpqFilePath == null;
		} else {
			return mpqFilePath.equals(other.mpqFilePath);
		}
	}

	public String getMpqFilePath() {
		return mpqFilePath;
	}

	@Override
	public DataSourceDescriptor duplicate() {
		return new MpqDataSourceDescriptor(mpqFilePath);
	}
}
