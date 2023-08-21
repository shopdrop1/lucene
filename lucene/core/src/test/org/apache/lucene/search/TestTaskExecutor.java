/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.search;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.NamedThreadFactory;

public class TestTaskExecutor extends LuceneTestCase {

  public void testUnwrapExceptions() {
    ExecutorService executorService =
        Executors.newFixedThreadPool(
            1, new NamedThreadFactory(TestTaskExecutor.class.getSimpleName()));
    try {
      TaskExecutor taskExecutor = new TaskExecutor(executorService);
      {
        FutureTask<?> task =
            new FutureTask<>(
                () -> {
                  throw new IOException("io exception");
                });
        IOException ioException =
            expectThrows(
                IOException.class, () -> taskExecutor.invokeAll(Collections.singletonList(task)));
        assertEquals("io exception", ioException.getMessage());
      }
      {
        FutureTask<?> task =
            new FutureTask<>(
                () -> {
                  throw new RuntimeException("runtime");
                });
        RuntimeException runtimeException =
            expectThrows(
                RuntimeException.class,
                () -> taskExecutor.invokeAll(Collections.singletonList(task)));
        assertEquals("runtime", runtimeException.getMessage());
        assertNull(runtimeException.getCause());
      }
      {
        FutureTask<?> task =
            new FutureTask<>(
                () -> {
                  throw new Exception("exc");
                });
        RuntimeException runtimeException =
            expectThrows(
                RuntimeException.class,
                () -> taskExecutor.invokeAll(Collections.singletonList(task)));
        assertEquals("exc", runtimeException.getCause().getMessage());
      }
    } finally {
      executorService.shutdown();
    }
  }
}
