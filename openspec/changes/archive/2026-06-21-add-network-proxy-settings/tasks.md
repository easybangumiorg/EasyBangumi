## 1. Preparation

- [x] 1.1 Confirm current Ktor client creation points and long-lived client lifetimes.
- [x] 1.2 Confirm settings UI structure on Android and Desktop.
- [x] 1.3 Confirm Ktor proxy APIs available to the Android and Java engines.

## 2. Preferences

- [x] 2.1 Add a shared network preference holder.
- [x] 2.2 Add `ProxyMode` with `DISABLED`, `MANUAL`, and `SYSTEM`.
- [x] 2.3 Add `ProxyProtocol` with `HTTP` and `SOCKS5`.
- [x] 2.4 Add persisted preferences for mode, protocol, and proxy URL.
- [x] 2.5 Ensure Android handles desktop-only `SYSTEM` mode safely.

## 3. Ktor Proxy Configuration

- [x] 3.1 Add a common proxy configuration helper or model that can be used by platform Ktor factories.
- [x] 3.2 Parse manual proxy host and port from supported URL formats.
- [x] 3.3 Apply disabled mode as explicit no-proxy behavior.
- [x] 3.4 Apply manual HTTP proxy mode.
- [x] 3.5 Apply manual SOCKS5 proxy mode.
- [x] 3.6 Apply desktop system proxy mode by preserving platform/Ktor default proxy behavior.
- [x] 3.7 Keep existing Ktor client singleton and lazy lifecycles unchanged.

## 4. Settings UI

- [x] 4.1 Add a network settings item to the Android settings home page.
- [x] 4.2 Add Android network settings content for proxy mode, protocol, and URL.
- [x] 4.3 Replace the Desktop settings placeholder with a minimal settings structure.
- [x] 4.4 Add Desktop network settings content including follow-system mode.
- [x] 4.5 Show restart-effective copy on the network settings page.
- [x] 4.6 Validate or guard manual proxy input before it can produce a broken Ktor proxy config.

## 5. Verification

- [ ] 5.1 Run Android compilation or build verification.
- [ ] 5.2 Run Desktop compilation or build verification.
- [x] 5.3 Verify disabled mode does not follow system proxy.
- [x] 5.4 Verify manual HTTP proxy mapping.
- [x] 5.5 Verify manual SOCKS5 proxy mapping.
- [x] 5.6 Verify Desktop system mode preserves system proxy behavior.
- [x] 5.7 Verify settings copy makes restart-effective behavior clear.
