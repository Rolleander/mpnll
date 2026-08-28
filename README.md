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

## Publishing

All artifacts use `projectVersion` from the root `gradle.properties` file. Publish
the complete set to the local Maven repository with:

```shell
./gradlew publishAll
```

The public coordinates are:

- `com.broll.mpnll:server:<version>` for a server
- `com.broll.mpnll:client:<version>` for the platform-independent client
- `com.broll.mpnll:client-java:<version>` for the Java TCP transport
- `com.broll.mpnll:client-gwt:<version>` for the GWT WebSocket transport

The native client artifacts pull in `client` transitively, so an application can
depend on `client-java` or `client-gwt` directly. The `shared` artifact is also
published as an internal transitive dependency of the server and core client.

## Custom protocol messages

Put application-specific messages in a Java library shared by the server and
client. The MPNLL protocol plugin applies the protobuf compiler, reads files from
`src/main/proto`, and wires generated Java into compilation:

When testing a locally published plugin, add Maven Local to plugin resolution in
the consuming project's `settings.gradle`:

```groovy
pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
  }
}
```

Then apply it in `build.gradle`:

```groovy
plugins {
  id 'com.broll.mpnll.protocol' version '0.1.0'
}

repositories {
  mavenCentral()
  mavenLocal()
}
```

The protocol JAR includes compiled classes, generated Java sources, original
`.proto` files, and any `.gwt.xml` files under `src/main/java`. The sources are
included so the same protocol artifact can be translated by GWT.

Create one registrar in the protocol project and use it on both ends:

```java
public final class GameMessages {
    public static void register(MessageRegistrySetup registry) {
        registry.register(MoveRequest.newBuilder());
        registry.register(MoveResult.newBuilder());
    }
}
```

```java
server.registerMessages(GameMessages::register);
client.registerMessages(GameMessages::register);
```

Both sides must register messages in the same order. The server and client
projects should each depend on this shared protocol project or artifact.
