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

GWT clients configure browser persistence through the existing `IFileAccess`
abstraction. Once a GWT-compatible message runtime is configured, the platform
setup is:

```java
MpnllClient client = new MpnllClient(
    new MpnllWebsocketClient(),
    new LocalStorageFileAccess("mpnll-auth"),
    new LocalStorageFileAccess("mpnll-last-connection")
);
```

The core client uses in-memory storage until platform-specific file access is
configured. `TempFileAccess` is JVM-only and therefore lives in `client-java`.

The transport, asynchronous result, timer, and persistence layers pass strict
GWT compilation. The complete `MpnllClient` is not yet part of that GWT module
because the shared messages currently use the JVM `protobuf-java` runtime; that
message-runtime migration is a separate remaining step.
