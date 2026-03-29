/* SPDX-License-Identifier: MIT */

package li.cil.oc2.api.bus.device;

/**
 * Lists built-in device types for convenience.
 */
public final class DeviceTypes {
    public static final DeviceType MEMORY = li.cil.oc2.common.bus.device.DeviceTypes.MEMORY.get();
    public static final DeviceType HARD_DRIVE = li.cil.oc2.common.bus.device.DeviceTypes.HARD_DRIVE.get();
    public static final DeviceType FLASH_MEMORY = li.cil.oc2.common.bus.device.DeviceTypes.FLASH_MEMORY.get();
    public static final DeviceType CARD = li.cil.oc2.common.bus.device.DeviceTypes.CARD.get();
    public static final DeviceType ROBOT_MODULE = li.cil.oc2.common.bus.device.DeviceTypes.ROBOT_MODULE.get();
    public static final DeviceType FLOPPY = li.cil.oc2.common.bus.device.DeviceTypes.FLOPPY.get();
    public static final DeviceType NETWORK_TUNNEL = li.cil.oc2.common.bus.device.DeviceTypes.NETWORK_TUNNEL.get();

    private DeviceTypes() {
    }
}
