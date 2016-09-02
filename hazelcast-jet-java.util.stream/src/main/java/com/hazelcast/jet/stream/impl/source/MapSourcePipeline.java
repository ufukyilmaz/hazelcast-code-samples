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

package com.hazelcast.jet.stream.impl.source;

import com.hazelcast.core.IMap;
import com.hazelcast.jet.source.MapSource;
import com.hazelcast.jet.Source;
import com.hazelcast.jet.io.Pair;
import com.hazelcast.jet.stream.Distributed;
import com.hazelcast.jet.stream.impl.AbstractSourcePipeline;
import com.hazelcast.jet.stream.impl.pipeline.StreamContext;

import java.util.AbstractMap;
import java.util.Map;


public class MapSourcePipeline<K, V> extends AbstractSourcePipeline<Map.Entry<K, V>> {

    private final IMap<K, V> map;

    public MapSourcePipeline(StreamContext context, IMap<K, V> map) {
        super(context);
        this.map = map;
    }

    @Override
    public Source getSourceTap() {
        return new MapSource(map.getName());
    }

    @Override
    public Distributed.Function<Pair, Map.Entry<K, V>> fromPairMapper() {
        return t -> new AbstractMap.SimpleEntry<>(((Pair<K, V>) t).getKey(), ((Pair<K, V>) t).getValue());
    }
}

