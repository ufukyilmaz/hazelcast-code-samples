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


import com.hazelcast.jet.Processor;
import com.hazelcast.jet.impl.ringbuffer.IOBuffer;
import com.hazelcast.jet.impl.runtime.task.TaskProcessor;
import com.hazelcast.jet.runtime.Consumer;
import com.hazelcast.jet.runtime.InputChunk;
import com.hazelcast.jet.runtime.TaskContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.hazelcast.util.Preconditions.checkNotNull;

public class ConsumerTaskProcessor implements TaskProcessor {
    protected static final Object[] EMPTY_CHUNK = new Object[0];

    protected final Consumer[] consumers;
    protected final Processor processor;
    protected final IOBuffer inputBuffer;
    protected final IOBuffer outputBuffer;
    protected boolean producersWriteFinished;
    protected final TaskContext taskContext;
    protected boolean consumedSome;
    protected boolean finalized;
    protected boolean finalizationFinished;
    protected boolean finalizationStarted;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ConsumerTaskProcessor(
            Consumer[] consumers, Processor processor, TaskContext taskContext) {
        checkNotNull(consumers);
        checkNotNull(processor);
        checkNotNull(taskContext);

        this.consumers = consumers;
        this.processor = processor;
        this.taskContext = taskContext;
        this.inputBuffer = new IOBuffer<>(EMPTY_CHUNK);
        int chunkSize = taskContext.getJobContext().getJobConfig().getChunkSize();
        this.outputBuffer = new IOBuffer<>(new Object[chunkSize]);
        reset();
    }

    private void checkFinalization() {
        if (finalizationStarted && finalizationFinished) {
            finalized = true;
            finalizationStarted = false;
            finalizationFinished = false;
            resetConsumers();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process() throws Exception {
        if (outputBuffer.size() > 0) {
            return consumeChunkAndResetOutputIfSuccess();
        }
        if (finalizationStarted) {
            finalizationFinished = processor.complete(outputBuffer);
        } else {
            if (producersWriteFinished) {
                return true;
            }
            processor.process(inputBuffer, outputBuffer, null);
        }
        if (outputBuffer.size() > 0) {
            return consumeChunkAndResetOutputIfSuccess();
        }
        checkFinalization();
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean consumeChunkAndResetOutputIfSuccess() throws Exception {
        if (onChunk(outputBuffer)) {
            outputBuffer.reset();
            checkFinalization();
            return true;
        }
        return false;
    }

    @Override
    public boolean onChunk(InputChunk<Object> inputChunk) {
        if (inputChunk.size() == 0) {
            return true;
        }
        this.consumedSome = false;
        boolean success = true;
        for (Consumer consumer : consumers) {
            int consumedCount = consumer.consume(inputChunk);
            success &= consumer.isFlushed();
            consumedSome |= consumedCount > 0;
        }
        if (success) {
            resetConsumers();
        }
        return success;
    }

    @Override
    public boolean didWork() {
        return consumedSome;
    }

    @Override
    public boolean isFinalized() {
        return finalized;
    }

    @Override
    public void reset() {
        resetConsumers();
        finalizationStarted = false;
        finalizationFinished = false;
        producersWriteFinished = false;
        finalized = false;
    }

    private void resetConsumers() {
        consumedSome = false;
        outputBuffer.reset();
    }

    @Override
    public void onOpen() {
        for (Consumer consumer : consumers) {
            consumer.open();
        }
        reset();
    }

    @Override
    public void onClose() {
        reset();
        for (Consumer consumer : consumers) {
            if (!consumer.isShuffled()) {
                consumer.close();
            }
        }
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
}
