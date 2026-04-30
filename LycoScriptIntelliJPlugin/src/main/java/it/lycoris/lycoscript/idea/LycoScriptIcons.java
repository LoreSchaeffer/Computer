package it.lycoris.lycoscript.idea;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Manages the visual assets and icons for the LycoScript plugin.
 */
public class LycoScriptIcons {

    /**
     * The default icon for .ls files.
     * Ensure that the lycoscript.svg file exists in the resources/icons directory.
     */
    public static final Icon FILE = IconLoader.getIcon("/icons/lycoscript.svg", LycoScriptIcons.class);

    private LycoScriptIcons() {
        throw new UnsupportedOperationException("Utility class");
    }
}