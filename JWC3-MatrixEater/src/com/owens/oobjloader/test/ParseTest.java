package com.owens.oobjloader.test;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import com.owens.oobjloader.parser.Parse;
import com.owens.oobjloader.builder.Build;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;

public class ParseTest {

    private static Logger log = Logger.getLogger(com.owens.oobjloader.lwjgl.DisplayTest.class.getName());

    public static void main(String[] argv) {

        log.log(INFO, "STARTING PARSING TEST - NOTHING WILL BE DISPLAYED - SEE com.owens.oobjloader.lwjgl.Test if you want to see things displayed.");
        for (String filename : argv) {

            log.log(INFO, "LOADING FILE " + filename);
            try {
                Build builder = new Build();
                Parse obj = new Parse(builder, filename);
            } catch (java.io.FileNotFoundException e) {
                log.log(INFO, "FileNotFoundException loading file "+filename+", e=" + e);
                e.printStackTrace();
            } catch (java.io.IOException e) {
                log.log(INFO, "IOException loading file "+filename+", e=" + e);
                e.printStackTrace();
            }
            log.log(INFO, "DONE LOADING FILE " + filename);
        }
    }
}