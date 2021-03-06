# MsiProxy #

The MSI-Proxy provides a single point of entry for accessing the list of active MSI messages from various data sources in a unified format.

## Providers ##

At the time of writing, the MSI-Proxy defines one MSI provider:

* [msiproxy-provider-dkmsi](msiproxy-provider-dkmsi): provides MSI messages from the Danish legacy MSI-admin production system.

## Interfaces ##

The MSI-Proxy exposes MSI data using several methods.

### Simple HTML Page ###
A simple server-side generated HTML page is available at the URI:

    /details.html?provider=<<PROVIDER>>&lang=<<LANG>>

Where *provider* is one of the available providers, i.e. "dkmsi",
and *lang* is one of the supported languages, i.e. "da" or "en".

The page can e.g. be used in an iframe, and since it is fairly simple and completely generated on the server side, it should be backwards compatible with most browsers.

Example: [http://localhost:8080/details.html?provider=dkmsi&lang=da](http://localhost:8080/details.html?provider=dkmsi&lang=da)

### PDF File ###
A PDF file can be generated by changing ".html" to ".pdf" in the simple HTML page URI format described above.

Example: [http://localhost:8080/details.pdf?provider=dkmsi&lang=da](http://localhost:8080/details.pdf?provider=dkmsi&lang=da)

### Single Page Application ###
A more sophisticated AngularJS based single page application is available at the URI:

    /index.html#/<<PROVIDER>>/<<LANG>>/<<VIEWMODE>>

Where *provider* is one of the available providers, i.e. "dkmsi",
and *lang* is one of the supported languages, i.e. "da" or "en".
*View mode* is either "details" or "map", and defines how the messages are displayed.

The page can e.g. be used in an iframe. It is more sophisticated than the simple HTML page previously described, but may not work properly in old web browsers.

Example: [http://localhost:8080/index.html#/dkmsi/da/details](http://localhost:8080/index.html#/dkmsi/da/details)

### Dispatch Page ###
There is a special dispatching HTML page, that will redirect the browser to one of the pages mentioned above, depending on the browser version. If the browser is
an Internet Exporer version 8 or earlier, the page will redirect to */details.html*. Otherwise, the page will redirect to */index.html*.

The URI scheme is identical to the Single Page Application scheme, i.e.:

    /dispatch.html#/<<PROVIDER>>/<<LANG>>/<<VIEWMODE>>

Example: [http://localhost:8080/dispatch.html#/dkmsi/da/details](http://localhost:8080/dispatch.html#/dkmsi/da/details)

### REST API ###

Each MSI provider exposes a REST API that may be used to fetch JSON or XML data for the list of active MSI messages.
The REST API has the URI format:

    /rest/<<PROVIDER>>/<<VERSION>>/service/messages?<<PARAMETERS>>

Where *provider* is one of the available providers, i.e. "dkmsi",
and *version* is the API version, currently "v1".
The *parameters* are described below:

| Parameter  | Default | Description  |
| ---------- | ------- | ----- |
| refresh    | false   | Can be used to ensure that data is refreshed from the back-end. Normally, cached data is used. |
| format     | json    | Either "json" or "xml". Defines the format of the returned data. |
| lang       | da      | The language. The current providers support "da" and "en" |
| details    | true    | Non-detailed data exclude certain message properties to allow for a more compact format. |
| types      |         | A comma-separated list of message types, e.g. "MSI" or "NM" or any of the sub-types. |
| areaId     |         | Can be used to restrict the messages to a certain area (including all sub-areas). |
| categoryId |         | Can be used to restrict the messages to a certain category (including all sub-categories). |

The format of the returned data can be deduced from the model defined in the [msiproxy-model](msiproxy-model) module.

Example: [http://localhost:8080/rest/dkmsi/v1/service/messages?lang=da](http://localhost:8080/rest/dkmsi/v1/service/messages?lang=da)


## Prerequisites
* Java JDK 1.8
* Maven 3.x
* JBoss Wildfly 8.2.0.Final or later
* MySQL

## Initial setup

### MySQL
On the server running the MSI-Proxy, you need to set up a MySQL database with imported data from the legacy MSI-admin production system.

The procedure is described in [msiproxy-provider-dkmsi/README.md](msiproxy-provider-dkmsi/README.md).

### JBoss Wildfly

#### Configuration
Install and configure the Wildfly application server by running:

    ./install-widlfly.sh
    ./configure-widlfly.sh

#### Local Deployment (Development)

Start Wildfly using the command:

    ./wildfly-8.2.0.Final/bin/standalone.sh

Build and deploy the MSI-Proxy web application using:

    mvn clean install
    cd msiproxy-web
    mvn wildfly:deploy

#### Remote Deployment (Production)

Initially, on the remote Wildfly server, configure a management user:

    ./wildfly-8.2.0.Final/bin/add-user.sh

Start Wildfly using the command:

    ./wildfly-8.2.0.Final/bin/standalone.sh -b 0.0.0.0 -bmanagement=0.0.0.0 \
        -Djboss.socket.binding.port-offset=0 -DbaseUri=<<SERVER URI>>

Build and deploy the MSI-Proxy web application using:

    mvn wildfly:deploy -Dwildfly.hostname=<<REMOTE SERVER>> -Dwildfly.port=9990 \
        -Dwildfly.username=<<USER NAME> -Dwildfly.password=<<PASSWORD>>

### Misc

#### OSM Static Map
The MSI-Proxy use an [OSM static map service](http://sourceforge.net/p/staticmaplite/code/HEAD/tree/staticmap.php) for generating the map grid images.

By default, the service used is [http://osm.e-navigation.net/staticmap.php](http://osm.e-navigation.net/staticmap.php).

You can override the "mapImageServer" system property to designate an alternative static map server,
such as [http://staticmap.openstreetmap.de/staticmap.php](http://staticmap.openstreetmap.de/staticmap.php).

#### Apache Web Server
In order to provide HTTPS access, it is common to run the Apache Web Server in front of Wildfly.
If mod_proxy is used to proxy requests to the Wildfly server, there is a problem in that Wildfly will see the originating request as a HTTP request, and thus, re-directs will fail.
However, this can be fixed by adding an "originalScheme" header with the value "https", i.e.:

    Header add originalScheme "https"
    RequestHeader set originalScheme "https"

Thus, the virtual host may look something along the lines of:

    <VirtualHost *:443>
        ServerName msi-proxy.e-navigation.net
        Include sites-available/msinm-demo-common.conf
        ProxyPreserveHost On
        ProxyPass           /  http://localhost:8080/
        ProxyPassReverse    /  http://localhost:8080/
    </VirtualHost>
