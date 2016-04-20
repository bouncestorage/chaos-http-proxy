/*
 * Copyright 2015 Bounce Storage, Inc. <info@bouncestorage.com>
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

package com.bouncestorage.chaoshttpproxy;

import java.util.List;
import java.util.Random;

import com.google.common.base.Supplier;

public class RandomFailureSupplier implements Supplier<Failure> {

    private final Random random = new Random();
    private final List<Failure> failures;

    public RandomFailureSupplier(List<Failure> failures) {
        this.failures = failures;
    }

    public final List<Failure> getFailures() {
        return failures;
    }

    @Override
    public final Failure get() {
        return failures.get(random.nextInt(failures.size()));
    }
}
