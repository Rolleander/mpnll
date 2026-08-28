# Multi Platform Network Lobby Library

## Asynchronous client API

Client operations are non-blocking and return `ClientFuture<T>`. This works on
the JVM and in GWT without blocking the browser event loop:

```java
client.listLobbies("ws://localhost:8081/")
    .onSuccess(lobbies -> showLobbies(lobbies))
    .onFailure(error -> showError(error.getMessage()));
```

For JVM callers that want `CompletableFuture`, use
`CompletableFutureAdapter.from(client.listLobbies(...))` from `client-java`.

GWT clients use `LocalStorageFileAccess` through the existing `IFileAccess`
abstraction. The GWT transport supplies it automatically:

```java
MpnllClient client = new MpnllClient(new MpnllWebsocketClient());
client.registerMessages(NtLobbyMessagesRegistry::register);
```

The core client uses in-memory storage until platform-specific file access is
configured. `TempFileAccess` is JVM-only and therefore lives in `client-java`.

The same generated messages from `shared` are used on both targets. JVM modules
use `protobuf-java`; `client-gwt` excludes that JVM runtime and substitutes the
GWT-compatible `protobuf-gwt` implementation of the same API.

Object mappings include their protobuf schema type name explicitly, so they do
not depend on descriptor reflection and work on both targets:

```java
registry.registerMapping(
    Settings.class,
    "example.SettingsMessage",
    SettingsMessage.getDefaultInstance(),
    SettingsCodec::encode,
    SettingsCodec::decode
);
```

Run the end-to-end lobby-list sanity page with `:server:start` and
`:client-gwt:gwtSmoke`, then open `http://localhost:8888/smoke/`.
