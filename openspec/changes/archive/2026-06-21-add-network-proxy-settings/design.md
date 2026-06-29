## Context

Ktor clients are created through `KtorFactory` in the shared Ktor module, with platform-specific factories:

- Android uses the Ktor Android engine.
- Desktop uses the Ktor Java engine.

The codebase does not create every `HttpClient` per request. There are multiple long-lived client creation points:

- platform `ktorModule` registers a global singleton `HttpClient`
- built-in source modules such as Age, GGL, Xifan, AniCh, and Debug register source-scoped singleton `HttpClient` instances
- `HttpHelperImpl` lazily creates an `HttpClient`
- `BangumiBusiness` lazily creates an `HttpClient`

This means proxy preference changes cannot reliably affect all existing clients without introducing a client holder/rebuild mechanism. That is intentionally deferred.

## Goals / Non-Goals

**Goals:**

- Provide user-configurable network proxy preferences.
- Support disabled proxy mode.
- Support manual HTTP proxy mode.
- Support manual SOCKS5 proxy mode.
- Support desktop-only system proxy mode.
- Apply proxy configuration whenever new Ktor clients are created.
- Make settings restart-effective and communicate that in the UI.
- Keep Ktor client lifetime and Koin bindings stable for the first version.

**Non-Goals:**

- Do not hot-reload or rebuild existing `HttpClient` instances when settings change.
- Do not introduce a `HttpClientHolder` or dynamic Koin module reload.
- Do not configure WebView/JCEF/WebKit proxy behavior in this change.
- Do not add proxy authentication unless a later change explicitly scopes it.
- Do not add per-source proxy overrides.

## Preference Model

Add a shared network preference holder, likely near `MainPreference` or as a separate Koin-provided `NetworkPreference`.

```text
NetworkPreference
  proxyMode: ProxyMode
  proxyProtocol: ProxyProtocol
  proxyUrl: String

ProxyMode
  DISABLED
  MANUAL
  SYSTEM   // desktop UI only

ProxyProtocol
  HTTP
  SOCKS5
```

Suggested default:

```text
proxyMode = DISABLED
proxyProtocol = HTTP
proxyUrl = ""
```

Desktop can expose `SYSTEM` as a selectable mode. Android should not expose `SYSTEM`; if it ever reads `SYSTEM` from stale/cross-platform preferences, it should treat it as `DISABLED` or normalize it back to a supported value.

## Ktor Proxy Mapping

Ktor 3 provides `HttpClientEngineConfig.proxy` and `ProxyBuilder`.

```text
DISABLED
  -> proxy = Proxy.NO_PROXY

MANUAL + HTTP
  -> proxy = ProxyBuilder.http(normalizedHttpProxyUrl)

MANUAL + SOCKS5
  -> proxy = ProxyBuilder.socks(host, port)

SYSTEM desktop
  -> proxy = null
```

The distinction between disabled and system matters because Ktor's engine config defaults to system proxy behavior when proxy remains `null`. Therefore disabled mode should explicitly set `Proxy.NO_PROXY` on JVM-backed engines.

## Manual Proxy URL Parsing

The UI can store a single `proxyUrl` string, but the Ktor mapping needs host and port.

Supported first-version inputs:

- `127.0.0.1:7890`
- `localhost:7890`
- `http://127.0.0.1:7890`
- `socks5://127.0.0.1:7890`

Parsing rules:

- If the user selects HTTP and omits a scheme, treat the value as `http://host:port`.
- If the user selects SOCKS5 and omits a scheme, parse as `host:port`.
- If the scheme conflicts with the selected protocol, prefer the selected protocol and use the URL only for host/port.
- Invalid or missing host/port should fail closed: do not apply a manual proxy, and ideally show validation feedback in the settings UI.

## Settings UI

Android already has a settings home page with appearance and player subpages. Add a network item:

```text
Settings
  Appearance
  Player
  Network
```

Desktop currently shows a placeholder settings page. For this change, Desktop should gain a minimal real settings page that includes the network subpage. It can mirror Android's structure without needing all Android-only player settings.

Network settings content:

```text
Proxy mode
  Disabled
  Manual
  Follow system    // desktop only

Manual protocol    // visible/enabled only in Manual mode
  HTTP
  SOCKS5

Proxy URL          // visible/enabled only in Manual mode
  host:port or URL

Restart note
  Proxy settings take effect after restarting the app.
```

## Client Lifecycle Decision

Proxy settings are restart-effective.

Reasoning:

- Existing clients are held as Koin singletons or lazy properties.
- Rebuilding clients safely would need ownership, close semantics, and coordination with in-flight requests.
- A restart-effective model is simpler, less risky, and matches the current architecture.

Future hot-reload support could be introduced later by replacing direct `HttpClient` injection with a holder/provider and explicitly closing old clients on network setting changes.

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Users expect proxy changes to apply immediately | Confusing behavior | Show restart-effective copy in the network settings page |
| `proxy = null` accidentally follows system proxy when user selected disabled | Requests may unexpectedly use system proxy | Explicitly set `Proxy.NO_PROXY` for disabled mode on JVM-backed engines |
| Android engine behavior differs from JVM Java engine | Manual proxy behavior may need platform-specific code | Keep mapping in platform source sets and verify Android/Desktop compilation |
| Invalid manual proxy input | Network requests fail or use no proxy unexpectedly | Validate host/port before applying and keep disabled/no-proxy fallback |
| WebView traffic differs from Ktor traffic | Login/browser pages may not use the same proxy | Document WebView proxy as out of scope for this change |

## Open Questions

- Should invalid manual proxy input block saving, or allow saving but not apply during Ktor client creation?
- Should the network settings page include a lightweight "test connection" action in a later change?
- Should Desktop system proxy mode also include JCEF/WebView behavior later, or remain Ktor-only?
