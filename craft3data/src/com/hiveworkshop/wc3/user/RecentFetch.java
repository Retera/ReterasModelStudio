package com.hiveworkshop.wc3.user;

import java.io.Serializable;
import java.util.Objects;

public class RecentFetch implements Serializable {
	private static final long serialVersionUID = 4242868357053211280L;

	private final RecentFetchType type;
	private final String id;
	private final String name;
	private final String iconPathIfAvailable;

	public RecentFetch(final RecentFetchType type, final String id, final String name,
			final String iconPathIfAvailable) {
		this.type = type;
		this.id = id;
		this.name = name;
		this.iconPathIfAvailable = iconPathIfAvailable;
	}

	public RecentFetchType getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIconPathIfAvailable() {
		return iconPathIfAvailable;
	}

	@Override
	public int hashCode() {
		return Objects.hash(iconPathIfAvailable, id, name, type);
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
		final RecentFetch other = (RecentFetch) obj;
		return Objects.equals(iconPathIfAvailable, other.iconPathIfAvailable) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && type == other.type;
	}

}
