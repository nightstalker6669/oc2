/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import li.cil.oc2.common.blockentity.ProjectorBlockEntity;

/**
 * Temporary 1.21.1 compatibility placeholder.
 * <p>
 * The old depth-composited projector pass depends on several rendering and event APIs that changed
 * substantially between 1.18 Forge and 1.21 NeoForge. The visible projector renderer stays intact;
 * this class only suppresses the legacy depth pass until that dedicated port is completed.
 */
public final class ProjectorDepthRenderer {
    private ProjectorDepthRenderer() {
    }

    public static void addProjector(final ProjectorBlockEntity projector) {
    }

    public static boolean willRenderProjectorDepth() {
        return false;
    }

    public static boolean isIsRenderingProjectorDepth() {
        return false;
    }

    public static void captureMainCameraDepth() {
    }
}
