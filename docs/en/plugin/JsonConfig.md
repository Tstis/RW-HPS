## Composition of Plugin
```
jar
    └───[class]
    plugin.json //Plugin's configuration file          
```

### Plugin.json parsing
```
{
  "name": "Name of the Plugin",
  "author": "The author of the Plugin",
  "main": "The main Main of the Plugin",
  "description": "Introduction of the Plugin",
  "version": "The version of the Plugin", "version": "The version of the Plugin
  "supportedVersions": "Versions that Plugin can support loading" (above 1.3.0-M2+DEV)
  (optional) "import": "who you want to load after"
}
```

#### Normal example
```
{
"name": "NetConnectProtocol",
"author": "dr",
"main": "dr.rwhps.plugin.netconnectprotocol,
"description": "RustedwarfareServer 1.14 NetConnectProtocol",
"version": "1.14 - 1.2.0.1 +",
"supportedVersions": "> 1.3.0-M1"
}
```

#### Example of dependency loading
Please note that recursive dependencies are only recursive once and not multiple times, to prevent high CPU usage.
```
{
"name": "NetConnectProtocol-EX",
"author": "Dr",
"main": "dr.rwhps.plugin.netconnectprotocol,
"description": "RustedwarfareServer 1.14 NetConnectProtocol",
"version": "1.14 - 1.2.0.1 +",
"import": "NetConnectProtocol"
}
```

Use of #### supportedVersions
For a rule, the following options are available
- `1.0.0-M4` requires version 1.0.0-M4, and only version 1.0.0-M4
- `> 1.0.0-RC` requires a version after 1.0.0-RC, not 1.0.0-RC
- `>= 1.0.0-RC` Requires version 1.0.0-RC or later, can be 1.0.0-RC
- `< 1.0.0-RC` Requires a version prior to 1.0.0-RC, cannot be 1.0.0-RC
- `<= 1.0.0-RC` requires version 1.0.0-RC or earlier, can be 1.0.0-RC
- `! = 1.0.0-RC` requires any version except 1.0.0-RC
  - `[1.0.0, 1.2.0]`
  - `(1.0.0, 1.2.0]`
  - `[1.0.0, 1.2.0)`
  - `(1.0.0, 1.2.0)`  
    [Math interval](https://baike.baidu.com/item/%E5%8C%BA%E9%97%B4/1273117)

Special note:
- Dependency rule version numbers do not need to carry version number metadata, which is not involved in the dependency requirement checking
- If the target version number carries a prior version number, please do not forget the prior version number