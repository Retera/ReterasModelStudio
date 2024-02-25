package com.hiveworkshop.rms.filesystem.sources;

import java.nio.file.Paths;
import java.util.Objects;

public class FolderDataSourceDescriptor implements DataSourceDescriptor {
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = -476724730967709309L;
	private final String folderPath;

	public FolderDataSourceDescriptor(final String folderPath) {
		this.folderPath = folderPath;
	}

	@Override
	public FolderDataSource createDataSource() {
		return new FolderDataSource(Paths.get(folderPath));
	}

	@Override
	public String getDisplayName() {
		return "Folder: " + folderPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((folderPath == null) ? 0 : folderPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof final FolderDataSourceDescriptor other) {
			return Objects.equals(folderPath, other.folderPath);
		}
		return false;
	}

	public String getFolderPath() {
		return folderPath;
	}
	public String getPath() {
		return folderPath;
	}

	@Override
	public FolderDataSourceDescriptor duplicate() {
		return new FolderDataSourceDescriptor(folderPath);
	}
}
