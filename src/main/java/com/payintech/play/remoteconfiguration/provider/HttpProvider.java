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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.Mode;

import java.io.IOException;
import java.net.URL;

/**
 * Configuration provider implementation for simple HTTP.
 *
 * @author Thibault Meyer
 * @version 17.11.28
 * @since 17.11.28
 */
public class HttpProvider implements RemoteConfigProvider {

    @Override
    public String getShortName() {
        return "HTTP";
    }

    @Override
    public String getName() {
        return "HTTP";
    }

    @Override
    public Config loadConfiguration(final Mode mode, final Config localConfig) throws IOException {
        final String configurationUrl = localConfig.getString("remote-configuration.http.url");
        if (configurationUrl != null && configurationUrl.startsWith("http")) {
            final URL httpUrl = new URL(configurationUrl);
            Logger.debug("Provider {}> {}", this.getName(), httpUrl.toString());
            return ConfigFactory.parseURL(httpUrl);
        } else {
            throw new RuntimeException("Bad configuration");
        }
    }
}
