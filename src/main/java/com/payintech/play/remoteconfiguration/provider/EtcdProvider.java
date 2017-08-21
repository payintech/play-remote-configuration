/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 PayinTech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.payintech.play.remoteconfiguration.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Mode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Configuration provider implementation for CoreOS etcd.
 *
 * @author Thibault Meyer
 * @version 17.08.21
 * @since 17.08.21
 */
public class EtcdProvider implements RemoteConfigProvider {

    @Override
    public String getShortName() {
        return "ETCD";
    }

    @Override
    public String getName() {
        return "CoreOS etcd";
    }

    /**
     * Explore the Json document to retrieve valid Key/Value couples.
     *
     * @param prefix        The key prefix to remove
     * @param stringBuilder The buffer to put configuration
     * @param jsonNode      The Json node to explore
     * @since 17.08.21
     */
    private void exploreJsonNode(final String prefix, final StringBuilder stringBuilder, final JsonNode jsonNode) {
        for (final JsonNode entry : jsonNode) {
            if (entry.hasNonNull("dir") && entry.get("dir").asBoolean()) {
                this.exploreJsonNode(prefix, stringBuilder, entry.get("nodes"));
            } else if (entry.hasNonNull("value")) {
                if (prefix.isEmpty()) {
                    stringBuilder.append(
                        StringUtils.removeFirst(
                            entry.get("key").asText(),
                            "/"
                        ).replace("/", ".")
                    );
                } else {
                    stringBuilder.append(
                        entry.get("key")
                            .asText()
                            .replace("/" + prefix + "/", "")
                            .replace("/", ".")
                    );
                }
                stringBuilder.append(" = ");
                stringBuilder.append(
                    entry.get("value").asText()
                );
                stringBuilder.append('\n');
            }
        }
    }

    @Override
    public Config loadConfiguration(final Mode mode, final Config localConfig) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder(1024);
        String etcdEndpoint = localConfig.getString("remote-configuration.etcd.endpoint");
        String etcdPrefix = localConfig.getString("remote-configuration.etcd.prefix").trim();
        if (etcdEndpoint != null && etcdEndpoint.startsWith("http")) {
            if (!etcdEndpoint.endsWith("/")) {
                etcdEndpoint += "/";
            }
            if (etcdPrefix.endsWith("/")) {
                etcdPrefix = etcdPrefix.substring(0, etcdPrefix.length() - 1);
            }
            if (etcdPrefix.startsWith("/")) {
                etcdPrefix = etcdPrefix.substring(1, etcdPrefix.length());
            }
            InputStream is = null;
            try {
                final URL consulUrl = new URL(
                    String.format(
                        "%sv2/keys/%s?recursive=true",
                        etcdEndpoint,
                        etcdPrefix
                    )
                );
                Logger.debug("Provider {}> {}", this.getName(), consulUrl.toString());
                final HttpURLConnection conn = (HttpURLConnection) consulUrl.openConnection();
                conn.setConnectTimeout(1500);
                if (conn.getResponseCode() / 100 == 2) {
                    is = conn.getInputStream();
                    final ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(is).get("node");
                    if (jsonNode.get("dir").asBoolean()) {
                        jsonNode = jsonNode.get("nodes");
                        this.exploreJsonNode(etcdPrefix, stringBuilder, jsonNode);
                    } else {
                        Logger.warn("Provider {} prefix must reference a directory", this.getName());
                    }
                } else {
                    Logger.warn("Provider {} return non 200 status: {}", this.getName(), conn.getResponseCode());
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (final IOException ignore) {
                    }
                }
            }
        } else {
            throw new RuntimeException("Bad configuration");
        }
        return ConfigFactory.parseString(stringBuilder.toString());
    }
}
