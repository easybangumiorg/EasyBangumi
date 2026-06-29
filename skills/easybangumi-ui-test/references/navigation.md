# Navigation

## Common Packages

- Debug package: `com.heyanle.easybangumi4.debug`
- Release package: `com.heyanle.easybangumi4`

## Main Bottom Tabs

- `主页`
- `追番`
- `历史`
- `更多`

## Common Entry Paths

### Source Management

1. Open app.
2. Enter `更多`.
3. Tap `番源管理`.

### Repository Page

1. Enter `番源管理`.
2. Switch to `番源仓库`.
3. Tap top-right `添加仓库` to open the repository manager dialog.

### Repository Manager Dialog

- Text field label: `Add Repository`
- Add button: `确定`
- List section title: `JS 仓库`
- Dialog close button: `关闭`

## Route Notes

- In code, source management route is `source_manage`.
- Repository page is the second tab inside source management.
- When multiple app variants share the same launcher label, do not trust launcher icon taps; launch by package name instead.
