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
package com.payintech.play.remoteconfiguration;

import com.payintech.play.remoteconfiguration.provider.RemoteConfigProvider;
import com.typesafe.config.Config;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * Extends the default application loader to inject the
 * retrieved configuration into the local configuration.
 *
 * @author Thibault Meyer
 * @version 17.08.20
 * @since 17.08.20
 */
public class PlayApplicationLoader extends GuiceApplicationLoader {

    /**
     * Look for the configuration key "remote-configuration.provider" and
     * try to instantiate class.
     *
     * @param localConfiguration The local configuration
     * @return An optional {@code RemoteConfigProvider}
     * @since 17.08.20
     */
    private RemoteConfigProvider loadProvider(final Config localConfiguration) {
        final String provider = localConfiguration.getString("remote-configuration.provider");
        if (!provider.isEmpty()) {
            final ServiceLoader<RemoteConfigProvider> serviceLoaderRCP = ServiceLoader.load(RemoteConfigProvider.class);
            for (final RemoteConfigProvider rcp : serviceLoaderRCP) {
                if (rcp.getShortName().compareToIgnoreCase(provider) == 0) {
                    return rcp;
                }
            }
            throw new RuntimeException("Can't resolve the remote configuration provider '" + provider + "'");
        }
        return null;
    }

    @Override
    public GuiceApplicationBuilder builder(final Context context) {
        final Config localConfiguration = context.initialConfig();
        final RemoteConfigProvider provider = this.loadProvider(localConfiguration);

        if (provider != null) {
            Logger.info("Retrieving configuration from {}", provider.getName());
            try {
                final Config remoteConfiguration = provider.loadConfiguration(
                    context.environment().mode(),
                    localConfiguration
                );
                return this.initialBuilder
                    .in(context.environment())
                    .loadConfig(remoteConfiguration.withFallback(localConfiguration))
                    .overrides(overrides(context));
            } catch (final IOException ex) {
                Logger.error("Can't retrieve remote configuration from {}", provider.getName());
                throw new RuntimeException(ex);
            } catch (final RuntimeException ex) {
                Logger.error("Can't retrieve remote configuration from {}", provider.getName());
                throw ex;
            }
        }

        return this.initialBuilder
            .in(context.environment())
            .loadConfig(localConfiguration)
            .overrides(overrides(context));
    }
}
