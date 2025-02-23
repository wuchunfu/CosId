/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.benchmark;


import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.segment.SegmentIDGenImpl;
import com.sankuai.inf.leaf.segment.dao.IDAllocDao;
import com.sankuai.inf.leaf.segment.dao.impl.IDAllocDaoImpl;
import me.ahoo.cosid.util.MockIdGenerator;
import org.openjdk.jmh.annotations.*;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author ahoo wang
 */
@State(Scope.Benchmark)
public class LeafBenchmark extends AbstractBenchmark {
    DataSource dataSource;
    IDGen idGen;
    String bizTag;

    @Setup
    public void setup() {
        bizTag = MockIdGenerator.INSTANCE.generateAsString();
        dataSource = DataSourceFactory.createDataSource();
        LeafInitializer.initSegment(dataSource, bizTag, step);

        IDAllocDao dao = new IDAllocDaoImpl(dataSource);
        idGen = new SegmentIDGenImpl();
        ((SegmentIDGenImpl) idGen).setDao(dao);
        idGen.init();
    }


    @Benchmark
    public long generate() {
        Result result = idGen.get(bizTag);
        if (Status.SUCCESS.equals(result.getStatus())){
            return result.getId();
        }
        return generate();
    }

    @TearDown
    public void tearDown() {
        if (dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
