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

package com.hazelcast.jet.stream.impl.processor;

import com.hazelcast.jet.container.ProcessorContext;
import com.hazelcast.jet.data.io.ConsumerOutputStream;
import com.hazelcast.jet.data.io.ProducerInputStream;
import com.hazelcast.jet.data.JetPair;
import com.hazelcast.jet.io.Pair;
import com.hazelcast.jet.processor.Processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collector;

public class GroupingAccumulatorProcessor<K, V, A, R> implements Processor<Pair<K, V>, Pair<K, A>> {

    private final Map<K, A> cache = new HashMap<>();
    private final Collector<V, A, R> collector;
    private Iterator<Map.Entry<K, A>> finalizationIterator;

    public GroupingAccumulatorProcessor(Collector<V, A, R> collector) {
        this.collector = collector;
    }

    @Override
    public boolean process(ProducerInputStream<Pair<K, V>> inputStream,
                           ConsumerOutputStream<Pair<K, A>> outputStream,
                           String sourceName,
                           ProcessorContext processorContext) throws Exception {
        for (Pair<K, V> input : inputStream) {
            A value = this.cache.get(input.getKey());
            if (value == null) {
                value = collector.supplier().get();
                this.cache.put(input.getKey(), value);
            }
            collector.accumulator().accept(value, input.getValue());
        }
        return true;
    }

    @Override
    public boolean complete(ConsumerOutputStream<Pair<K, A>> outputStream,
                            ProcessorContext processorContext) throws Exception {
        boolean finalized = false;
        try {
            if (finalizationIterator == null) {
                this.finalizationIterator = this.cache.entrySet().iterator();
            }
            int idx = 0;
            while (this.finalizationIterator.hasNext()) {
                Map.Entry<K, A> next = this.finalizationIterator.next();
                outputStream.consume(new JetPair<>(next.getKey(), next.getValue()));
                if (idx == processorContext.getConfig().getChunkSize() - 1) {
                    break;
                }
                idx++;
            }
            finalized = !this.finalizationIterator.hasNext();
        } finally {
            if (finalized) {
                this.finalizationIterator = null;
                this.cache.clear();
            }
        }
        return finalized;
    }
}
