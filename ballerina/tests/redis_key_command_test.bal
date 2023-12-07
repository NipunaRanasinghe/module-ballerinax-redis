// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations

import ballerina/jballerina.java;
import ballerina/lang.runtime;
import ballerina/test;

@test:Config {
}
function testDel() {
    var result = redis->del(["testDelKey1", "testDelKey2", "testDelKey3"]);
    if (result is int) {
        test:assertEquals(result, 3);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testExists() {
    var result1 = redis->exists(["testExistsKey"]);
    var result2 = redis->exists(["nonExistentKey"]);
    if (result1 is int) {
        test:assertEquals(result1, 1);
    } else {
        test:assertFail("error from Connector: " + result1.message());
    }
    if (result2 is int) {
        test:assertEquals(result2, 0);
    } else {
        test:assertFail("error from Connector: " + result2.message());
    }
}

@test:Config {
}
function testExpire() {
    var result = redis->expire("testExpireKey", 3);
    if (result is boolean) {
        test:assertTrue(result);
        runtime:sleep(3);
        test:assertEquals(exist(java:fromString("testExpireKey")), 0);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testKeys() {
    var result = redis->keys("testKeysKey*");
    if (result is string[]) {
        test:assertEquals(result.length(), 3);
        boolean allMatchingKeysRetrieved = true;
        string[] keys = ["testKeysKey1", "testKeysKey2", "testKeysKey3"];
        foreach var k in keys {
            boolean keyExists = false;
            foreach var r in result {
                if (k == r) {
                    keyExists = true;
                    break;
                }
            }
            if (!keyExists) {
                allMatchingKeysRetrieved = false;
                break;
            }
        }
        test:assertTrue(allMatchingKeysRetrieved);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testMove() {
    var result = redis->move("testMoveKey", 1);
    if (result is boolean) {
        test:assertTrue(result);
        test:assertEquals(exist(java:fromString("testMoveKey")), 0);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testPersist() {
    var result = redis->persist("testPersistKey");
    runtime:sleep(3);
    if (result is boolean) {
        test:assertFalse(result);
        test:assertEquals(exist(java:fromString("testPersistKey")), 1);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testPExpire() {
    var result = redis->pExpire("testPExpireKey", 3000);
    if (result is boolean) {
        test:assertTrue(result);
        runtime:sleep(3.5);
        test:assertEquals(exist(java:fromString("testPExpireKey")), 0);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testPTtl() {
    _ = pexpire(java:fromString("testPTtlKey"), 10000);
    var result = redis->pTtl("testPTtlKey");
    runtime:sleep(5);
    int ttl = pttl(java:fromString("testPTtlKey"));
    if (result is int) {
        test:assertTrue(result >= ttl && result <= 10000);
    }
}

@test:Config {
}
function testRandomKey() {
    var result = redis->randomKey();
    if (result is string) {
        test:assertNotEquals(result, "");
    } else if (result is ()) {
        test:assertFail("Key not found");
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testRename() {
    var result = redis->rename("testRenameKey", "testRenameKey1");
    if (result is string) {
        test:assertEquals(result, "OK");
        test:assertEquals(exist(java:fromString("testRenameKey")), 0);
        test:assertEquals(exist(java:fromString("testRenameKey1")), 1);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testRenameNx() {
    var result1 = redis->renameNx("testRenameNxKey", "testRenameNxKeyRenamed");
    var result2 = redis->renameNx("testRenameNxKey1", "testRenameNxKeyExisting");
    if (result1 is boolean) {
        test:assertTrue(result1);
    } else {
        test:assertFail("error from Connector: " + result1.message());
    }
    if (result2 is boolean) {
        test:assertFalse(result2);
    } else {
        test:assertFail("error from Connector: " + result2.message());
    }
    test:assertEquals(exist(java:fromString("testRenameNxKey")), 0);
    test:assertEquals(exist(java:fromString("testRenameNxKeyRenamed")), 1);
    test:assertEquals(exist(java:fromString("testRenameNxKey1")), 1);
}

@test:Config {
}
function testSort() {
    var result = redis->sort("testSortKey");
    if (result is string[]) {
        test:assertEquals(result.length(), 6);
        boolean elementsInOrder = true;
        string[] values = ["0", "1", "2", "3", "4", "8"];
        int count = 0;
        foreach var v in values {
            if (v != values[count]) {
                elementsInOrder = false;
                break;
            }
            count += 1;
        }
        test:assertTrue(elementsInOrder);
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}

@test:Config {
}
function testTtl() {
    _ = pexpire(java:fromString("testTtlKey"), 10);
    var result = redis->pTtl("testTtlKey");
    int ttl = pttl(java:fromString("testTtlKey"));
    if (result is int) {
        test:assertTrue(result >= ttl && result <= 10000);
    }
}

@test:Config {
}
function testType() {
    var result = redis->redisType("testTypeKey");
    if (result is string) {
        test:assertEquals(result, "string");
    } else {
        test:assertFail("error from Connector: " + result.message());
    }
}
