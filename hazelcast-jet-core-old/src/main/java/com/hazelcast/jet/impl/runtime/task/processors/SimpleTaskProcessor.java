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

package com.hazelcast.jet.impl.runtime.task.processors;

import com.hazelcast.jet.runtime.InputChunk;
import com.hazelcast.jet.impl.runtime.task.TaskProcessor;
import com.hazelcast.jet.impl.ringbuffer.IOBuffer;
import com.hazelcast.jet.Processor;

import static com.hazelcast.util.Preconditions.checkNotNull;

public class SimpleTaskProcessor implements TaskProcessor {
    private static final Object[] EMPTY_CHUNK = new Object[0];

    protected boolean finalizationStarted;
    protected boolean producersWriteFinished;
    private final Processor processor;
    private final IOBuffer inputBuffer;
    private final IOBuffer outputBuffer;
    private boolean finalized;

    public SimpleTaskProcessor(Processor processor) {
        checkNotNull(processor);
        this.processor = processor;
        this.inputBuffer = new IOBuffer<>(EMPTY_CHUNK);
        this.outputBuffer = new IOBuffer<>(EMPTY_CHUNK);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process() throws Exception {
        if (!finalizationStarted) {
            if (producersWriteFinished) {
                return true;
            }
            processor.process(inputBuffer, outputBuffer, null);
            return true;
        } else {
            finalized = processor.complete(outputBuffer);
            return true;
        }
    }

    @Override
    public boolean didWork() {
        return false;
    }

    @Override
    public boolean isFinalized() {
        return finalized;
    }

    @Override
    public void reset() {
        finalized = false;
        inputBuffer.reset();
        outputBuffer.reset();
        finalizationStarted = false;
        producersWriteFinished = false;
    }

    @Override
    public void startFinalization() {
        finalizationStarted = true;
    }

    @Override
    public void onProducersWriteFinished() {
        producersWriteFinished = true;
    }

    @Override
    public void onReceiversClosed() {

    }

    @Override
    public boolean producersReadFinished() {
        return true;
    }

    @Override
    public boolean onChunk(InputChunk inputChunk) throws Exception {
        return true;
    }

    @Override
    public void onOpen() {
        reset();
    }

    @Override
    public void onClose() {

    }
}
