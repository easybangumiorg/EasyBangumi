## ADDED Requirements

### Requirement: Built-in source assets are packaged from the repository source tree
The Android build SHALL package every file under the repository-level `inner_source` directory under the APK asset path `inner_source/`, preserving each relative path.

#### Scenario: Build packages a source file
- **WHEN** a JS source file exists under repository-level `inner_source`
- **THEN** the produced application assets contain that file at the corresponding `inner_source/` relative path

#### Scenario: Source removal is reflected in packaged assets
- **WHEN** a file is removed from repository-level `inner_source`
- **THEN** a clean application build does not package a stale copy of that file under `inner_source/`

### Requirement: Packaged source manifest is verified
The project SHALL verify that the packaged `inner_source` JS-file manifest matches the repository-level `inner_source` JS-file manifest before release validation succeeds.

#### Scenario: Missing packaged source is detected
- **WHEN** a repository source file is absent from the packaged asset manifest
- **THEN** validation fails and identifies the missing relative path

#### Scenario: Unexpected packaged source is detected
- **WHEN** the packaged asset manifest contains a JS source path not present in repository-level `inner_source`
- **THEN** validation fails and identifies the unexpected relative path

### Requirement: Blocked built-in sources remain non-auto-installed
The runtime SHALL copy packaged built-in source files to its source cache but SHALL exclude files whose filename begins with `block-` from automatic source loading.

#### Scenario: Active source is loaded
- **WHEN** a packaged built-in JS source filename does not begin with `block-`
- **THEN** runtime source loading considers it for normal version selection

#### Scenario: Blocked source is copied but not loaded
- **WHEN** a packaged built-in JS source filename begins with `block-`
- **THEN** it is available in the copied cache but is excluded from automatic source loading
