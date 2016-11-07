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

package com.hazelcast.jet.impl.job;

import com.hazelcast.jet.impl.data.io.JetPacket;
import com.hazelcast.nio.Address;

/**
 * Exception thrown when a job has been failed to complete
 */
public class JobException extends RuntimeException {
    private final String initiatorAddress;
    private final JetPacket wrongPacket;
    private final Object reason;

    public JobException(Object reason, Address initiator) {
        this.reason = reason;
        this.initiatorAddress = initiator != null ? initiator.toString() : "";
        this.wrongPacket = null;
    }

    public JobException(Address initiator) {
        this.initiatorAddress = initiator != null ? initiator.toString() : "";
        this.wrongPacket = null;
        this.reason = null;
    }

    public JobException(Address initiator, JetPacket wrongPacket) {
        this.initiatorAddress = initiator != null ? initiator.toString() : "";
        this.wrongPacket = wrongPacket;
        this.reason = null;
    }

    @Override
    public Throwable getCause() {
        if ((this.reason != null) && (this.reason instanceof Throwable)) {
            return (Throwable) this.reason;
        }

        return null;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    public String toString() {
        if (initiatorAddress != null) {
            String error = "Job was invalidated by member with address: " + initiatorAddress;

            if (wrongPacket == null) {
                return error;
            } else {
                return error + " wrongPacket=" + wrongPacket.toString();
            }
        } else if (reason != null) {
            return "Job was invalidated because of : " + reason.toString();
        } else {
            return "Job was invalidated for unknown reason";
        }
    }
}
