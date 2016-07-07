/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.memory.memoryblock;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Bits;
import com.hazelcast.util.ExceptionUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static com.hazelcast.util.QuickMath.normalize;

@SuppressWarnings("checkstyle:magicnumber")
final class UnsafeUtil {
    /**
     * If this constant is {@code true}, then {@link Unsafe} refers to a usable {@code Unsafe}
     * instance.
     */
    static final boolean UNSAFE_AVAILABLE;

    /**
     * The {@link sun.misc.Unsafe} instance which is available and ready to use.
     */
    static final Unsafe UNSAFE;

    private static final ILogger LOGGER = Logger.getLogger(UnsafeUtil.class);

    static {
        Unsafe unsafe;
        try {
            unsafe = findUnsafe();
            // Test if unsafe has required methods...
            if (unsafe != null) {
                long arrayBaseOffset = unsafe.arrayBaseOffset(byte[].class);
                byte[] buffer = new byte[(int) arrayBaseOffset + (2 * Bits.LONG_SIZE_IN_BYTES)];
                unsafe.putByte(buffer, arrayBaseOffset, (byte) 0x00);
                unsafe.putBoolean(buffer, arrayBaseOffset, false);
                unsafe.putChar(buffer, normalize(arrayBaseOffset, Bits.CHAR_SIZE_IN_BYTES), '0');
                unsafe.putShort(buffer, normalize(arrayBaseOffset, Bits.SHORT_SIZE_IN_BYTES), (short) 1);
                unsafe.putInt(buffer, normalize(arrayBaseOffset, Bits.INT_SIZE_IN_BYTES), 2);
                unsafe.putFloat(buffer, normalize(arrayBaseOffset, Bits.FLOAT_SIZE_IN_BYTES), 3f);
                unsafe.putLong(buffer, normalize(arrayBaseOffset, Bits.LONG_SIZE_IN_BYTES), 4L);
                unsafe.putDouble(buffer, normalize(arrayBaseOffset, Bits.DOUBLE_SIZE_IN_BYTES), 5d);
                unsafe.copyMemory(new byte[buffer.length], arrayBaseOffset,
                        buffer, arrayBaseOffset,
                        buffer.length);
            }
        } catch (Throwable t) {
            unsafe = null;
            LOGGER.warning("Unable to get an instance of Unsafe. Unsafe-based operations will be unavailable", t);
        }
        UNSAFE = unsafe;
        UNSAFE_AVAILABLE = UNSAFE != null;
    }

    private UnsafeUtil() {
    }

    private static Unsafe findUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException se) {
            return AccessController.doPrivileged(new PrivilegedAction<Unsafe>() {
                @Override
                public Unsafe run() {
                    try {
                        Class<Unsafe> type = Unsafe.class;
                        try {
                            Field field = type.getDeclaredField("theUnsafe");
                            field.setAccessible(true);
                            return type.cast(field.get(type));
                        } catch (Exception e) {
                            for (Field field : type.getDeclaredFields()) {
                                if (type.isAssignableFrom(field.getType())) {
                                    field.setAccessible(true);
                                    return type.cast(field.get(type));
                                }
                            }
                        }
                    } catch (Throwable t) {
                        throw ExceptionUtil.rethrow(t);
                    }
                    throw new RuntimeException("Unsafe unavailable");
                }
            });
        }
    }
}

