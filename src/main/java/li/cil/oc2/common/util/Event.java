/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import java.util.HashSet;

public final class Event extends HashSet<Runnable> implements Runnable {
    private static final long serialVersionUID = 1L;

    @Override
    public void run() {
        for (final Runnable runnable : this) {
            runnable.run();
        }
    }
}
