/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.ceres;

import li.cil.ceres.api.DeserializationVisitor;
import li.cil.ceres.api.SerializationException;
import li.cil.ceres.api.SerializationVisitor;
import li.cil.ceres.api.Serializer;
import li.cil.sedna.api.memory.MemoryRange;

import javax.annotation.Nullable;
import java.util.Objects;

public final class MemoryRangeSerializer implements Serializer<MemoryRange> {
    @Override
    public void serialize(@Nullable final SerializationVisitor visitor, @Nullable final Class<MemoryRange> type, @Nullable final Object value) throws SerializationException {
        final MemoryRange range = (MemoryRange) Objects.requireNonNull(value);
        final SerializationVisitor actualVisitor = Objects.requireNonNull(visitor);
        actualVisitor.putLong("start", range.start);
        actualVisitor.putLong("end", range.end);
    }

    @Nullable
    @Override
    public MemoryRange deserialize(@Nullable final DeserializationVisitor visitor, @Nullable final Class<MemoryRange> type, @Nullable final Object value) throws SerializationException {
        final DeserializationVisitor actualVisitor = Objects.requireNonNull(visitor);
        if (!actualVisitor.exists("start") || !actualVisitor.exists("end")) {
            return (MemoryRange) value;
        }

        return MemoryRange.of(actualVisitor.getLong("start"), actualVisitor.getLong("end"));
    }
}
