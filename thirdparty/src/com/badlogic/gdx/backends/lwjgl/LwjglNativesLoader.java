/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/* Included in MatrixEater by Retera for deployability */

package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.io.File;
import java.lang.reflect.Method;

import static com.badlogic.gdx.utils.SharedLibraryLoader.*;

public final class LwjglNativesLoader {
	static public boolean load = true;

	static {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");

		// Don't extract natives if using JWS.
		try {
			final Method method = Class.forName("javax.jnlp.ServiceManager").getDeclaredMethod("lookup", String.class);
			method.invoke(null, "javax.jnlp.PersistenceService");
			load = false;
		} catch (final Throwable ex) {
			load = true;
		}
	}

	/**
	 * Extracts the LWJGL native libraries from the classpath and sets the "org.lwjgl.librarypath" system property.
	 */
	static public void load() {
		if (!load) {
			return;
		}

		final SharedLibraryLoader loader = new SharedLibraryLoader();
		File nativesDir = null;
		try {
			if (isWindows) {
				nativesDir = loader.extractFile(is64Bit ? "lwjgl64.dll" : "lwjgl.dll", null).getParentFile();
				if (!false) {
					loader.extractFileTo(is64Bit ? "OpenAL64.dll" : "OpenAL32.dll", nativesDir);
				}
			} else if (isMac) {
				nativesDir = loader.extractFile("liblwjgl.dylib", null).getParentFile();
				if (!false) {
					loader.extractFileTo("openal.dylib", nativesDir);
				}
			} else if (isLinux) {
				nativesDir = loader.extractFile(is64Bit ? "liblwjgl64.so" : "liblwjgl.so", null).getParentFile();
				if (!false) {
					loader.extractFileTo(is64Bit ? "libopenal64.so" : "libopenal.so", nativesDir);
				}
			}
		} catch (final Throwable ex) {
			throw new RuntimeException("Unable to extract LWJGL natives.", ex);
		}
		System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
		load = false;
	}
}
