## Why

Some anime sources and metadata endpoints may require users to route requests through a local or system proxy. EasyBangumi already centralizes most network access through Ktor clients, but there is no product-level network settings page and no user-configurable proxy model.

The current Ktor clients are long-lived in several places: the global Koin `HttpClient` is a singleton, several built-in source modules bind their own singleton `HttpClient`, and a few helper/business classes lazily create clients. Because of this lifecycle, the first version should keep proxy behavior simple and predictable: proxy changes are persisted immediately but take effect only after application restart.

## What Changes

- Add a network settings subpage to the settings screen.
- Add proxy configuration preferences:
  - proxy mode: disabled, manual, and desktop-only system mode
  - proxy protocol for manual mode: HTTP or SOCKS5
  - proxy URL or host/port value for manual mode
- Apply proxy preferences when Ktor clients are created.
- Keep existing Ktor client lifecycle unchanged.
- Make proxy changes restart-effective rather than hot-reloading existing clients.
- On desktop, support following the system proxy mode by using the platform/Ktor default proxy behavior.
- On Android, expose disabled and manual proxy modes only.

## Capabilities

### New Capabilities

- `network-proxy-settings`: Allows users to configure application network proxy behavior for Ktor-backed requests.

### Modified Capabilities

- Settings gains a network settings subpage on Android and Desktop.
- Ktor client creation reads persisted network proxy preferences.

## Impact

- Adds new persisted preferences under the shared preference layer.
- Updates Ktor factory configuration in Android and Desktop source sets.
- Updates settings UI for Android and Desktop.
- Desktop settings currently has a placeholder page; this change will need to give Desktop a real settings structure for at least the network subpage.
- Existing `HttpClient` singleton/lazy lifecycle remains unchanged, so users must restart the app after changing proxy settings.
- WebView/JCEF/WebKit proxy behavior is not in scope unless it already follows platform defaults independently.
