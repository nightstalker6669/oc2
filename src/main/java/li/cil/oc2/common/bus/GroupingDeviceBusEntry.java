/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus;

import li.cil.oc2.api.bus.device.Device;

import java.util.Optional;
import java.util.OptionalInt;

interface GroupingDeviceBusEntry {
    Optional<String> getDeviceDataKey();

    OptionalInt getDeviceEnergyConsumption();

    Device getDevice();
}
