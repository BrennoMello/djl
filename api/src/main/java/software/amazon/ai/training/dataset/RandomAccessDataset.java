/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package software.amazon.ai.training.dataset;

import java.util.RandomAccess;
import software.amazon.ai.training.Trainer;
import software.amazon.ai.util.Pair;

/**
 * RandomAccessDataset represent the dataset that support random access reads. i.e. it could access
 * certain data item given the index
 */
public abstract class RandomAccessDataset<I, L> implements Dataset<I, L>, RandomAccess {

    protected long size;
    protected Sampler sampler;
    protected DataLoadingConfiguration config;

    public RandomAccessDataset(BaseBuilder<?> builder) {
        this.sampler = builder.getSampler();
        this.config = builder.getConfig();
    }

    public abstract Pair<I, L> get(long index);

    @Override
    public Iterable<Record> getRecords(Trainer<I, L, ?> trainer) {
        return new DataIterable<>(this, trainer, sampler, config);
    }

    public long size() {
        return size;
    }

    @SuppressWarnings("rawtypes")
    public abstract static class BaseBuilder<B extends BaseBuilder> {

        private Sampler sampler;
        private DataLoadingConfiguration config;

        public Sampler getSampler() {
            if (sampler == null) {
                throw new IllegalArgumentException("The sampler must be set");
            }
            return sampler;
        }

        public B setSampling(long batchSize) {
            return setSampling(batchSize, true, false);
        }

        public B setSampling(long batchSize, boolean shuffle) {
            return setSampling(batchSize, shuffle, false);
        }

        public B setSampling(long batchSize, boolean shuffle, boolean dropLast) {
            Sampler.SubSampler subSampler = shuffle ? new RandomSampler() : new SequenceSampler();
            sampler = new BatchSampler(subSampler, batchSize, dropLast);
            return self();
        }

        public B setSampler(Sampler sampler) {
            this.sampler = sampler;
            return self();
        }

        public DataLoadingConfiguration getConfig() {
            if (config != null) {
                return config;
            } else {
                return new DataLoadingConfiguration.Builder().build();
            }
        }

        public B optConfig(DataLoadingConfiguration config) {
            this.config = config;
            return self();
        }

        public abstract B self();
    }
}