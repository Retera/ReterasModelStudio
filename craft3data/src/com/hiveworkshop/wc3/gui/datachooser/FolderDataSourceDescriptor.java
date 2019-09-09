package com.hiveworkshop.wc3.gui.datachooser;

import java.nio.file.Paths;

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
	public DataSource createDataSource() {
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FolderDataSourceDescriptor other = (FolderDataSourceDescriptor) obj;
		if (folderPath == null) {
			if (other.folderPath != null) {
				return false;
			}
		} else if (!folderPath.equals(other.folderPath)) {
			return false;
		}
		return true;
	}

}
