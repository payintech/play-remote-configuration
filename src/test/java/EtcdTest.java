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

import com.payintech.play.remoteconfiguration.PlayApplicationLoader;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.Mode;

import java.util.Arrays;
import java.util.HashMap;

/**
 * EtcdTest.
 *
 * @author Thibault Meyer
 * @version 17.08.21
 * @since 17.08.21
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EtcdTest {

    /**
     * Build a preconfigured application.
     *
     * @param prefix The prefix to use with the provider
     * @return The newly created application
     * @since 17.08.20
     */
    private Application buildApplication(final String prefix) {
        return new PlayApplicationLoader().builder(new ApplicationLoader.Context(
            new Environment(Mode.TEST),
            new HashMap<String, Object>() {{
                put("remote-configuration.provider", "ETCD");
                put("remote-configuration.etcd.prefix", prefix);
                put("remote-configuration.etcd.username", "root");
                put("remote-configuration.etcd.password", "123456");
            }}
        )).build();
    }

    /**
     * @since 17.08.20
     */
    @Test
    public void etcdTest_001() {
        final Application application = this.buildApplication("test");

        Assert.assertEquals(
            "org.postgresql.Driver",
            application.config().getString("db.default.driver")
        );

        Assert.assertEquals(
            "org.postgresql.Driver",
            application.config().getString("db.default.driver2")
        );

        Assert.assertEquals(
            5000,
            application.config().getInt("db.default.timeout")
        );

        Assert.assertEquals(
            Arrays.asList(1, 2, 3, 4, 5),
            application.config().getIntList("db.default.excludedIds")
        );

        Assert.assertEquals(
            false,
            application.config().getBoolean("db.default.disabled")
        );

        application.asScala().stop();
    }

    /**
     * @since 17.08.21
     */
    @Test
    public void etcdTest_002() {
        final Application application = this.buildApplication("/");

        Assert.assertEquals(
            "Hello World",
            application.config().getString("my.key")
        );

        Assert.assertEquals(
            Arrays.asList(1, 2, 3, 4, 5),
            application.config().getIntList("test.db.default.excludedIds")
        );

        application.asScala().stop();
    }

    /**
     * @since 17.08.21
     */
    @Test
    public void etcdTest_003() {
        final Application application = this.buildApplication("");

        Assert.assertEquals(
            "Hello World",
            application.config().getString("my.key")
        );

        Assert.assertEquals(
            Arrays.asList(1, 2, 3, 4, 5),
            application.config().getIntList("test.db.default.excludedIds")
        );

        application.asScala().stop();
    }
}
