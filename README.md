# Play Remote Configuration


[![Latest release](https://img.shields.io/badge/latest_release-17.06-orange.svg)](https://github.com/payintech/play-remote-configuration/releases)
[![JitPack](https://jitpack.io/v/payintech/play-remote-configuration.svg)](https://jitpack.io/#payintech/play-remote-configuration)
[![Build](https://img.shields.io/travis-ci/payintech/play-remote-configuration.svg?branch=master&style=flat)](https://travis-ci.org/payintech/play-remote-configuration)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/payintech/play-remote-configuration/master/LICENSE)

Remote configuration for Play Framework 2
*****

## About this project
In production, it is not always easy to manage the configuration files of a
Play Framework application, especially when it running on multiple servers.
The purpose of this project is to provide a simple way to use a remote
configuration with a Play Framework application.


By default, the following providers are provided:

| Short name | Name             |  Authentication  |
|------------|------------------|:----------------:|
| CONSUL     | HashiCorp Consul |        ✓         |
| ETCD       | CoreOS etcd      |        x         |


## Add play-remote-configuration to your project

#### build.sbt

```sbtshell
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.payintech" % "play-remote-configuration" % "release~YY.MM"
```


## How to use

You can use this project in two ways. The first is to simply set the right
application loader in your configuration file. The second, if you have an
existing application loader, is to extend it with the class provided in this
project : _com.payintech.play.remoteconfiguration.PlayApplicationLoader_.

#### application.conf (first way)

```hocon
play {
  application {

    ## Application Loader
    # https://www.playframework.com/documentation/latest/JavaDependencyInjection
    # ~~~~~
    loader = "com.payintech.play.remoteconfiguration.PlayApplicationLoader"
  }
}
```

#### ApplicationLoader.java (second way)

```java
import com.payintech.play.remoteconfiguration.PlayApplicationLoader;

public class ApplicationLoader extends PlayApplicationLoader {

    @Override
    public GuiceApplicationBuilder builder(final Context context) {
        final GuiceApplicationBuilder newInitialBuilder = super.builder();
        // Your custom code
        return newInitialBuilder;
    }
}
```


## Configuration

```hocon
remote-configuration {

  ## Provider to use
  # Short name of the provider to use to retrieve remote
  # configuration. Built-in available providers are:
  #  - CONSUL    (HashiCorp Consul)
  # ~~~~~
  provider = ""
  provider = ${?RCONF_PROVIDER}

  ## HashiCorp Consul
  # ~~~~~
  consul {
  
    # API endpoint. HTTPS endpoint could be used,
    # but the SSL certificate must be valid
    endpoint = "http://127.0.0.1:8500/"
    endpoint = ${?RCONF_CONSUL_ENDPOINT}
    
    # Authentication token. If ACL are anabled on
    # your Consul cluster, this variable allow you
    # to set the token to use with each API calls
    authToken = ""
    authToken = ${?RCONF_CONSUL_AUTHTOKEN}
    
    # Prefix. Get only values with key beginning
    # with the configured prefix
    prefix = "/"
    prefix = ${?RCONF_CONSUL_PREFIX}
  }
  
  ## CoreOS etcd
  # ~~~~~
  etcd {
    # API endpoint. HTTPS endpoint could be used,
    # but the SSL certificate must be valid
    endpoint = "http://127.0.0.1:2379/"
    endpoint = ${?RCONF_ETCD_ENDPOINT}

    # Prefix. Get only values with key beginning
    # with the configured prefix. With etcd, it
    # must be a directory.
    prefix = "/"
    prefix = ${?RCONF_ETCD_PREFIX}
  }  
}
```
