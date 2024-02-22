/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org)
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

package io.ballerina.lib.redis.config;

import io.ballerina.lib.redis.utils.ConversionUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.lettuce.core.SslVerifyMode;

import static io.ballerina.lib.redis.utils.ConversionUtils.getMapValueOrNull;
import static io.ballerina.lib.redis.utils.ConversionUtils.getStringValueOrNull;

/**
 * This class maps the Ballerina Redis client config to the Java Redis client config.
 *
 * @since 3.0.0
 */
public final class ConfigMapper {

    // Redis client config names as Ballerina string objects
    public static final BString CONFIG_CONNECTION = StringUtils.fromString("connection");
    public static final BString CONFIG_URI = StringUtils.fromString("uri");
    public static final BString CONFIG_HOST = StringUtils.fromString("host");
    public static final BString CONFIG_PORT = StringUtils.fromString("port");
    public static final BString CONFIG_USERNAME = StringUtils.fromString("username");
    public static final BString CONFIG_PASSWORD = StringUtils.fromString("password");
    public static final BString CONFIG_IS_CLUSTER_CONNECTION = StringUtils.fromString("isClusterConnection");
    public static final BString CONFIG_POOLING_ENABLED = StringUtils.fromString("connectionPooling");

    public static final BString CONFIG_OPTIONS = StringUtils.fromString("options");
    public static final BString CONFIG_CLIENT_NAME = StringUtils.fromString("clientName");
    public static final BString CONFIG_DATABASE = StringUtils.fromString("database");
    public static final BString CONFIG_CONNECTION_TIMEOUT = StringUtils.fromString("connectionTimeout");

    public static final BString CONFIG_SECURE_SOCKET = StringUtils.fromString("secureSocket");
    public static final BString CONFIG_CERT = StringUtils.fromString("cert");
    public static final BString CONFIG_KEY = StringUtils.fromString("key");
    public static final BString CONFIG_TRUST_STORE_PATH = StringUtils.fromString("path");
    public static final BString CONFIG_TRUST_STORE_PASSWORD = StringUtils.fromString("password");
    public static final BString CONFIG_KEY_STORE_PATH = StringUtils.fromString("path");
    public static final BString CONFIG_KEY_STORE_PASSWORD = StringUtils.fromString("password");
    public static final BString CONFIG_CERT_FILE = StringUtils.fromString("certFile");
    public static final BString CONFIG_KEY_FILE = StringUtils.fromString("keyFile");
    public static final BString CONFIG_KEY_PASSWORD = StringUtils.fromString("keyPassword");
    private static final BString CONFIG_PROTOCOLS = StringUtils.fromString("protocols");
    private static final BString CONFIG_CIPHERS = StringUtils.fromString("ciphers");
    private static final BString VERIFY_MODE = StringUtils.fromString("verifyMode");
    private static final BString CONFIG_START_TLS_ENABLED = StringUtils.fromString("startTls");

    private ConfigMapper() {
    }

    /**
     * Maps the Ballerina Redis client config to the Java Redis client config object.
     *
     * @param config Ballerina Redis client config
     * @return Java Redis client config
     */
    public static ConnectionConfig from(BMap<BString, Object> config) {
        boolean isClusterConnection = config.getBooleanValue(CONFIG_IS_CLUSTER_CONNECTION);
        boolean poolingEnabled = config.getBooleanValue(CONFIG_POOLING_ENABLED);
        SecureSocket secureSocket = getSecureSocketFromBObject(config);

        Object connection = config.get(CONFIG_CONNECTION);
        if (connection instanceof BString connectionUri) {
            return new ConnectionURI(connectionUri.getValue(), isClusterConnection, poolingEnabled, secureSocket);
        } else {
            BMap<BString, Object> connectionParams = (BMap<BString, Object>) connection;
            String host = connectionParams.getStringValue(CONFIG_HOST).getValue();
            int port = connectionParams.getIntValue(CONFIG_PORT).intValue();
            String username = getStringValueOrNull(connectionParams, CONFIG_USERNAME);
            String password = getStringValueOrNull(connectionParams, CONFIG_PASSWORD);
            BMap<BString, Object> options = getMapValueOrNull(connectionParams, CONFIG_OPTIONS);

            return new ConnectionParams(host, port, username, password, isClusterConnection, poolingEnabled,
                    secureSocket, getConnectionOptionsFromBObject(options));
        }
    }

    private static Options getConnectionOptionsFromBObject(BMap<BString, Object> connection) {
        int database = connection.getIntValue(CONFIG_DATABASE).intValue();
        int connectionTimeout = connection.getIntValue(CONFIG_CONNECTION_TIMEOUT).intValue();
        String clientName = getStringValueOrNull(connection, CONFIG_CLIENT_NAME);

        return new Options(clientName, database, connectionTimeout);
    }

    private static SecureSocket getSecureSocketFromBObject(BMap<BString, Object> connection) {
        BMap<BString, Object> secureSocket = getMapValueOrNull(connection, CONFIG_SECURE_SOCKET);
        if (secureSocket == null) {
            return null;
        }

        String certPath = getStringValueOrNull(secureSocket, CONFIG_CERT);
        BMap<BString, Object> trustStoreMap = getMapValueOrNull(secureSocket, CONFIG_CERT);
        TrustStore trustStore = trustStoreMap != null ? getTrustStoreFromBObject(trustStoreMap) : null;

        KeyStore keyStore = null;
        CertKey certKey = null;
        BMap<BString, Object> keyMap = getMapValueOrNull(secureSocket, CONFIG_KEY);
        if (isKeyStoreConfig(keyMap)) {
            keyStore = getKeyStoreFromBObject(keyMap);
        } else if (isCertKeyConfig(keyMap)) {
            certKey = getCertKeyFromBObject(keyMap);
        }

        BArray protocolsBArr = secureSocket.getArrayValue(CONFIG_PROTOCOLS);
        String[] protocols = protocolsBArr != null ? ConversionUtils.createStringArrayFromBArray(protocolsBArr) : null;

        BArray ciphersBArr = secureSocket.getArrayValue(CONFIG_CIPHERS);
        String[] ciphers = ciphersBArr != null ? ConversionUtils.createStringArrayFromBArray(ciphersBArr) : null;

        String verifyModeStr = getStringValueOrNull(secureSocket, VERIFY_MODE);
        SslVerifyMode verifyMode = verifyModeStr != null ? SslVerifyMode.valueOf(verifyModeStr) : null;
        Boolean startTLS = secureSocket.getBooleanValue(CONFIG_START_TLS_ENABLED);
        startTLS = startTLS != null ? startTLS : false;

        return new SecureSocket(trustStore, certPath, keyStore, certKey, protocols, ciphers, verifyMode, startTLS);
    }

    private static CertKey getCertKeyFromBObject(BMap<BString, Object> keyMap) {
        BString certFile = keyMap.getStringValue(CONFIG_CERT_FILE);
        BString keyFile = keyMap.getStringValue(CONFIG_KEY_FILE);
        BString keyPassword = keyMap.getStringValue(CONFIG_KEY_PASSWORD);

        return new CertKey(certFile.getValue(), keyFile.getValue(), keyPassword.getValue());
    }

    private static boolean isCertKeyConfig(BMap<BString, Object> keyMap) {
        return keyMap != null && keyMap.containsKey(CONFIG_CERT_FILE);
    }

    private static boolean isKeyStoreConfig(BMap<BString, Object> keyMap) {
        return keyMap != null && keyMap.containsKey(CONFIG_KEY_STORE_PATH);
    }

    private static TrustStore getTrustStoreFromBObject(BMap<BString, Object> trustStoreObj) {
        BString path = trustStoreObj.getStringValue(CONFIG_TRUST_STORE_PATH);
        BString password = trustStoreObj.getStringValue(CONFIG_TRUST_STORE_PASSWORD);
        return new TrustStore(path.getValue(), password.getValue());
    }

    private static KeyStore getKeyStoreFromBObject(BMap<BString, Object> keyStoreObj) {
        if (keyStoreObj == null) {
            return null;
        }
        BString path = keyStoreObj.getStringValue(CONFIG_KEY_STORE_PATH);
        BString password = keyStoreObj.getStringValue(CONFIG_KEY_STORE_PASSWORD);

        return new KeyStore(path.getValue(), password.getValue());
    }
}
