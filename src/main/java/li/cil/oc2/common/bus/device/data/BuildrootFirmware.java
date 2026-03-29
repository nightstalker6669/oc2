/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.data;

import li.cil.oc2.api.bus.device.data.Firmware;
import li.cil.sedna.api.memory.MemoryMap;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.memory.MemoryMaps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public final class BuildrootFirmware extends ForgeRegistryEntry<Firmware> implements Firmware {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public boolean run(final MemoryMap memory, final long startAddress) {
        try (final InputStream firmware = Buildroot.getFirmware();
             final InputStream linuxImage = Buildroot.getLinuxImage()) {
            if (firmware == null || linuxImage == null) {
                LOGGER.warn("Missing buildroot firmware payload; skipping buildroot firmware install.");
                return false;
            }

            MemoryMaps.store(memory, startAddress, firmware);
            MemoryMaps.store(memory, startAddress + 0x200000, linuxImage);
            return true;
        } catch (final IOException e) {
            LOGGER.error(e);
            return false;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Sedna Linux");
    }
}
