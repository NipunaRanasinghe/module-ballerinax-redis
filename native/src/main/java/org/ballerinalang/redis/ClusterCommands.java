/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.redis;

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.redis.connection.RedisConnectionCommandExecutor;

import static org.ballerinalang.redis.utils.ConversionUtils.createBError;
import static org.ballerinalang.redis.utils.RedisUtils.getConnection;

/**
 * Ballerina native util implementation for redis cluster commands.
 *
 * @since 3.0.0
 */
@SuppressWarnings("unused")
public class ClusterCommands {

    /**
     * Retrieve information and statistics about the cluster observed by the current node. This command is
     * exclusively available in cluster mode.
     * If the connection is in a non-clustered mode, the API will return a `redis:Error`. Other errors will also be
     * appropriately handled.
     *
     * @param redisClient redis client BObject
     * @return a bulk-string-reply as a string array or, a `redis:Error` if the connection is non-clustered or
     * encounters any other exceptions.
     */
    public static Object clusterInfo(BObject redisClient) {
        try {
            RedisConnectionCommandExecutor executor = getConnection(redisClient).getConnectionCommandExecutor();
            return executor.clusterInfo();
        } catch (Throwable e) {
            return createBError(e);
        }
    }
}
