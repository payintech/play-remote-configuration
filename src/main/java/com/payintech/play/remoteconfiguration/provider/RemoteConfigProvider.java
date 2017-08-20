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
import play.Mode;

import java.io.IOException;

/**
 * Remote configuration provider.
 *
 * @author Thibault Meyer
 * @version 17.08.20
 * @since 17.08.20
 */
public interface RemoteConfigProvider {

    /**
     * Retrieve the provider short name. This name
     * must be unique along all available providers.
     *
     * @return The provider short name
     * @since 17.08.20
     */
    String getShortName();

    /**
     * Retrieve the provider name.
     *
     * @return The provider name
     * @since 17.08.20
     */
    String getName();

    /**
     * Retrieve configuration from the provider.
     *
     * @param mode        The current running mode
     * @param localConfig The local configuration
     * @return The retrieved configuration
     * @throws IOException When I/O related errors occur
     * @since 17.08.20
     */
    Config loadConfiguration(final Mode mode, final Config localConfig) throws IOException;
}
