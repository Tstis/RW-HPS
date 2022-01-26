# RW-HPS - SaveData API


## Plugin to save data
### Design Goals

- Source-level static strong types: avoid `getString()`, `getList()`...
- Fully automatic loading and saving: Plugin is automatically saved by a single line of code link at startup
- Synchronous modification with front-end: can be dynamically synchronized in memory in graphical front-end implementations such as Android
- Storage extensibility: can use a variety of ways to store, whether file or database, the plug-in layer uses the same implementation

In summary, **minimized plug-in authors in the processing of data and configuration do pay **.

* No database saving support for now, but this is on the agenda. *

## [`Value`]
``` java
interface Value<T> {
    private T data;
    
    protected Value(T data) {
        this.data = data;
    }
}
```

Represents a value proxy. In [`PluginData`], the values are wrapped in [`Value`].

## [`PluginData`]

A plugin internal, hidden from the user data object. Similar to a `Map` with property names as keys, corresponding to [`Value`] as values.

The [`PluginData`] interface has a base implementation class, [`AbstractPluginData`], which does not support autosave by default, but only stores key-value relationships and their serializers.

Plugins can inherit from [`AbstractPluginData`], have high freedom of access to implementation details, and extend the data structure.  
But usually, plugins use [`AutoSavePluginData`].

The [`AutoSavePluginData`] listens for modifications to the values saved in it, and at the appropriate time starts a concurrency to save the data under the provided [`AutoSavePluginDataHolder`] concurrency scope.

### Using `PluginData`
example is more efficient than theoretical at this point
1. Use getPluginData() directly from within the Plugin;  