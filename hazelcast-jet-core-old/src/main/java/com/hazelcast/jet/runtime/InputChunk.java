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

package com.hazelcast.jet.runtime;

import java.util.Iterator;

/**
 * Represents a finite part of an input for a processor
 *
 * @param <T> type of the input objects
 */
public interface InputChunk<T> extends Iterable<T> {
    /**
     * Returns the object at the specified index
     */
    T get(int idx);

    /**
     * Returns the size of the given chunk
     */
    int size();

    /**
     * Returns a flyweight iterator over the input chunk
     */
    Iterator<T> iterator();
}
