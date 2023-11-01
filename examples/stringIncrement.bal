import ballerinax/redis;
import ballerina/io;

redis:ConnectionConfig redisConfig = {
    host: "localhost:6379",
    password: "",
    options: { connectionPooling: true, isClusterConnection: false, ssl: false,
        startTls: false, verifyPeer: false }
};

public function main() returns error? {

    redis:Client conn = check new (redisConfig);

    var result = conn->incr("incrKey");
    if (result is int) {
        io:println(result);
    } else {
        io:println("Error in incrementing");
    }

    result = conn->incrBy("incrByKey", 3);
    if (result is int) {
        io:println(result);
    } else {
        io:println("Error in incrementing");
    }

    var floatResult = conn->incrByFloat("testIncrByKey", 0.2);
    if (floatResult is float) {
        io:println(floatResult);
    } else {
        io:println("Error in incrementing in float");
    }

    conn.close();
}
