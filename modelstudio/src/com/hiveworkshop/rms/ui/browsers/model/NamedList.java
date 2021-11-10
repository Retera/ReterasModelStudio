package com.hiveworkshop.rms.ui.browsers.model;

import java.util.ArrayList;

public class NamedList<E> extends ArrayList<E> {
	private String name;
	private String cachedIconPath = null; // might be present

	public NamedList(String name) {
		this.name = name;
	}

	public NamedList(String name, String cachedIconPath) {
		this.name = name;
		this.cachedIconPath = cachedIconPath;
	}

	public void setCachedIconPath(String cachedIconPath) {
		this.cachedIconPath = cachedIconPath;
	}

	public String getCachedIconPath() {
		return cachedIconPath;
	}

	public String getName() {
		return name;
	}
}
