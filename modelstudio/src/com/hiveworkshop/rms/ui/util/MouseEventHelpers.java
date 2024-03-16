package com.hiveworkshop.rms.ui.util;

import java.awt.event.MouseEvent;

public class MouseEventHelpers {
	public static final int mouseButtonMask = ~0b1111111111;

	/**
	 * Strips the event's modifierEx of all keyboard modifiers
	 * @return modifierEx consisting of only mouse buttons
	 */
	public static int getMouseButtonMasks(MouseEvent e) {
		return getMouseButtonMasks(e.getModifiersEx());
	}
	/**
	 * Strips modifierEx of all keyboard modifiers
	 * @return modifierEx consisting of only mouse buttons
	 */
	public static int getMouseButtonMasks(int modifiersEx) {
		return modifiersEx & mouseButtonMask;
	}

	/**
	 * Strips the event's modifierEx of all mouse buttons
	 * @return modifierEx consisting of only keyboard modifiers
	 */
	public static int getModifierMasks(MouseEvent e) {
		return getMouseButtonMasks(e.getModifiersEx());
	}
	/**
	 * Strips modifierEx of all mouse buttons
	 * @return modifierEx consisting of only keyboard modifiers
	 */
	public static int getModifierMasks(int modifiersEx) {
		return (modifiersEx & ~mouseButtonMask);
	}

	/**
	 * Checks is a MouseEvent uses the same mouse buttons as modifiersEx, ignoring keyboard modifiers
	 * @param e MouseEvent to check
	 * @param modifiersEx modifiers to compare with
	 * @return {@code true} if the event contains exactly the same mouse buttons as {@code modifiersEx}
	 * otherwise false
	 */
	public static boolean isSameMouseButton(MouseEvent e, int modifiersEx) {
		return isSameMouseButton(e.getModifiersEx(), modifiersEx);
	}
	/**
	 * Checks is eModifiersEx uses the same mouse buttons as modifiersEx, ignoring keyboard modifiers
	 * @param eModifiersEx modifiers to check
	 * @param modifiersEx modifiers to compare with
	 * @return true if eModifiersEx contains exactly the same mouse buttons as modifiersEx
	 * otherwise false
	 */
	public static boolean isSameMouseButton(int eModifiersEx, int modifiersEx) {
		return (modifiersEx & mouseButtonMask) == (eModifiersEx & mouseButtonMask);
	}

	/**
	 *
	 * @param e MouseEvent to check
	 * @param mainMods modifiers that must be present
	 * @param posMods zero or more optional modifier
	 * @return true if e.getModifiersEx == mainMods or
	 * e.getModifiersEx == (mainMods | posMods[i])
	 * otherwise false
	 */
	public static boolean matches(MouseEvent e, int mainMods, int... posMods) {
		return matches(e.getModifiersEx(), mainMods, posMods);
	}
	/**
	 *
	 * @param modifiersEx modifiers to check
	 * @param mainMods modifiers that must be present
	 * @param posMods zero or more optional modifier
	 * @return true if modifiersEx == mainMods or modifiersEx == (mainMods | posMods[i])
	 * otherwise false
	 */
	public static boolean matches(int modifiersEx, int mainMods, int... posMods) {
		int xor_MB = modifiersEx ^ mainMods;
		if (xor_MB != 0) {
			for (int mod : posMods) {
				if ((xor_MB ^ mod) == 0) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 *
	 * @param e MouseEvent to check
	 * @param mainMods modifiers that must be present
	 * @param posMods zero or more optional modifier
	 * @return mainMods if e.getModifiersEx == mainMods,
	 * posMods[i] if e.getModifiersEx == (mainMods | posMods[i])
	 * otherwise MouseEvent.NOBUTTON
	 */
	public static int getMatch(MouseEvent e, int mainMods, int... posMods) {
		return getMatch(e.getModifiersEx(), mainMods, posMods);
	}
	/**
	 *
	 * @param modifiersEx modifiers to check
	 * @param mainMods modifiers that must be present
	 * @param posMods zero or more optional modifier
	 * @return mainMods if modifiersEx == mainMods,
	 * posMods[i] if modifiersEx == (mainMods | posMods[i])
	 * otherwise MouseEvent.NOBUTTON
	 */
	public static int getMatch(int modifiersEx, int mainMods, int... posMods) {
		int xor_MB = modifiersEx ^ mainMods;
		if (xor_MB != 0) {
			for (int mod : posMods) {
				if ((xor_MB ^ mod) == 0) {
					return mod;
				}
			}
			return MouseEvent.NOBUTTON;
		}
		return mainMods;
	}

	/**
	 * Checks if the modifier is present in the MouseEvent
	 * @param e MouseEvent to check
	 * @param modifier modifier to check
	 * @return modifier != MouseEvent.NOBUTTON && ((e.getModifiersEx() & modifier) == modifier)
	 */
	public static boolean hasModifier(MouseEvent e, int modifier) {
		return hasModifier(e.getModifiersEx(), modifier);
	}
	/**
	 * Checks if the modifier is present in modifiersEx
	 * @param modifiersEx modifiers to check
	 * @param modifier modifier to check
	 * @return modifier != MouseEvent.NOBUTTON && ((modifiersEx & modifier) == modifier)
	 */
	public static boolean hasModifier(int modifiersEx, int modifier) {
		return modifier != MouseEvent.NOBUTTON && ((modifiersEx & modifier) == modifier);
	}
}
