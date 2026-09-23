/**
 * The Warsmash-derived MDX/MDL parser: a plain data model of a Warcraft III model ({@link
 * com.hiveworkshop.rms.parsers.mdlx.MdlxModel}) with binary and text load/save, copied from tw1lac's fork of
 * Retera Model Studio (https://github.com/tw1lac/ReterasModelStudio, MIT) where it is itself a Java port of
 * Chananya Freiman's mdx-m3-viewer handlers (https://github.com/flowtsohg/mdx-m3-viewer, MIT), first ported by
 * Retera for Warsmash. It is used as an optional alternative to this program's own {@code wc3.mdl} text parser
 * (see {@code ModelParserPreference} and {@code WarsmashParserBridge}). Differences from the fork: no Swing
 * dialogs ({@link com.hiveworkshop.rms.parsers.mdlx.MdlxParseLog} collects the messages instead), and support
 * for the Warcraft III 3.0 ("Forsaken Kingdom") format versions 1300-1800 ({@link
 * com.hiveworkshop.rms.parsers.mdlx.MdlxVersion}).
 */
package com.hiveworkshop.rms.parsers.mdlx;
