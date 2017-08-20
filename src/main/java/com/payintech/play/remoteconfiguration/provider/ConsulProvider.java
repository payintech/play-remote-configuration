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
import play.Logger;
import play.Mode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Configuration provider implementation for HashiCorp Consul.
 *
 * @author Thibault Meyer
 * @version 17.08.20
 * @since 17.08.20
 */
public final class ConsulProvider implements RemoteConfigProvider {

    @Override
    public String getShortName() {
        return "CONSUL";
    }

    @Override
    public String getName() {
        return "HashiCorp Consul";
    }

    @Override
    public Config loadConfiguration(final Mode mode, final Config localConfig) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder(1024);
        final String consulAccessToken = localConfig.getString("remote-configuration.consul.authToken");
        String consulEndpoint = localConfig.getString("remote-configuration.consul.endpoint");
        String consulPrefix = localConfig.getString("remote-configuration.consul.prefix").trim();
        if (consulEndpoint != null && consulEndpoint.startsWith("http")) {
            if (!consulEndpoint.endsWith("/")) {
                consulEndpoint += "/";
            }
            if (consulPrefix.endsWith("/")) {
                consulPrefix = consulPrefix.substring(0, consulPrefix.length() - 1);
            }
            if (consulPrefix.startsWith("/")) {
                consulPrefix = consulPrefix.substring(1, consulPrefix.length());
            }
            InputStream is = null;
            try {
                final URL consulUrl = new URL(
                    String.format(
                        "%sv1/kv/%s/?recurse&token=%s",
                        consulEndpoint,
                        consulPrefix,
                        consulAccessToken
                    )
                );
                Logger.debug("Provider {}> {}", this.getName(), consulUrl.toString());
                final HttpURLConnection conn = (HttpURLConnection) consulUrl.openConnection();
                conn.setConnectTimeout(1500);
                if (conn.getResponseCode() / 100 == 2) {
                    is = conn.getInputStream();
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode jsonDocument = mapper.readTree(is);
                    final Base64.Decoder decoder = Base64.getDecoder();
                    for (final JsonNode entry : jsonDocument) {
                        if (entry.hasNonNull("Value")) {
                            stringBuilder.append(
                                entry.get("Key")
                                    .asText()
                                    .replace(consulPrefix.isEmpty() ? "" : consulPrefix + "/", "")
                                    .replace("/", ".")
                            );
                            stringBuilder.append(" = ");
                            stringBuilder.append(
                                new String(
                                    decoder.decode(
                                        entry.get("Value").asText()
                                    )
                                )
                            );
                            stringBuilder.append('\n');
                        }
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
        Logger.debug("Remote Configuration> {}", stringBuilder.toString());
        return ConfigFactory.parseString(stringBuilder.toString());
    }
}
