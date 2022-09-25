# HaHelper

Need your data highly available but don't want the overheard of deploying and managing a third party database? Attach your data directly to your app!
HaHelper is a client library that turns your server into a highly available data store for itself!

HaHelper stores your data and provides many configuration options to help facilitate how your data is made available.

- Easily replicate your data to tolerate node failures
- Cross Language support! Link two servers in any language!
- Listen for data changes and act on updates!
- Choose between disk-based or memory-based storage
- Configurable service discovery methods

# Getting Started

To being using HaHelper, simply download the HaHelper client library for the language of your choice.
Read the documentation for your language for more details about implementation.

## Starting a Server

Making your server HA is as simple as starting the HAHelper server/client library.

```
HaHelper(HaHelperServerConfig.load()).start()
```

## Configuration

Configuration can be provided to HaHelper in a number of ways.
Using `HaHelperServerConfig.load()` will search the following default locations in order of priority:

- File defined at by the environment variable: `HA_HELPER_CONFIG_PATH`
- ./ha_helper_config.yaml
- ./config/ha_helper_config.yaml

If multiple configuration files exist, the first file found will be used.
You must provide a configuration, no defaults exist.

The following configuration options are available:

| Config Value                              | Description                                                                                                                                                                                            | DataType     |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| `local.serverPort`                        | The port to start the server on                                                                                                                                                                        | Int          |
| `cluster.clusterSize`                     | The number of services your cluster contains. Used to determine quorum.                                                                                                                                | Int          |
| `cluster.multiMaster`                     | If the cluster should have evnetualConsistency instead of using leaderElection.                                                                                                                        | Boolean      |
| `cluster.discoveryMethod`                 | How the server should detect other servers in the cluster. Either one of `knownHosts` or `domainDiscoverey` should be used.                                                                            | Object       |
| `cluster.discoveryMethod.knownHosts`      | A list of known IP addresses and ports that are in the cluster. Example: <pre>cluster.discoveryMethod.knownHosts:<br>  - hostname: localhost<br>  port: 9998</pre> List may or may not include itself. | List<Host>   |
| `cluster.discoveryMethod.domainDiscovery` | A list of domain names to search                                                                                                                                                                       | List<String> |
| `persistence.enabled`     | If data should be written to disk                                                                                                                                                                      | Boolean      |
| `persistence.replicas`     | The number of locations where the data should be written.                                                                                                                                              | Int          |
| `persistence.storageLocation`     | The file path of where files should be written                                                                                                                                                         | String       |
| `persistence.compactionIntervalSeconds`     | If compaction should occur. Compaction will minimize stored files to only include the most recently committed key-value pairs. Otherwise, all deltas are kept. Set to zero to disable compaction.      | Long         |


## Storing Data

TODO

## Fetching Data

TODO

## Listening for Changes

TODO