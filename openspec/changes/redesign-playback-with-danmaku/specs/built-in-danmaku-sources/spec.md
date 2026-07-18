## ADDED Requirements

### Requirement: Danmaku sources use a built-in source contract
The system SHALL model danmaku providers through a source contract that includes stable metadata, anime search, remote episode listing, comment retrieval, and automatic resolution capability. The registry SHALL only accept sources packaged within the application.

#### Scenario: Resolve available sources
- **WHEN** the application initializes danmaku sources
- **THEN** the registry SHALL expose only built-in sources registered by the application

#### Scenario: External source installation attempt
- **WHEN** a user attempts to add an externally supplied danmaku source definition
- **THEN** the system SHALL reject the operation and SHALL NOT execute or register the definition

### Requirement: DanDanPlay is the initial built-in source
The system SHALL register DanDanPlay as the initial built-in danmaku source and SHALL identify third-party provenance returned inside DanDanPlay comments separately from the source itself.

#### Scenario: Load comments with provenance
- **WHEN** DanDanPlay returns comments with upstream provenance metadata
- **THEN** the system SHALL retain that metadata for comment filtering without presenting the upstream as an independently installable source

### Requirement: Users can manage built-in source state
The system SHALL provide a danmaku-source management surface that lists built-in sources and lets users enable or disable an available source and select a default source. Built-in sources SHALL be marked as non-removable.

#### Scenario: Disable the default source
- **WHEN** the user disables the current default danmaku source
- **THEN** the system SHALL require selection of another enabled source or show no default source when none remain enabled

#### Scenario: View built-in source metadata
- **WHEN** the user opens danmaku-source management
- **THEN** the system SHALL show DanDanPlay as built-in and non-removable

### Requirement: DanDanPlay integration protects credentials and request usage
The system SHALL obtain DanDanPlay credentials from application-specific secure build or runtime configuration, SHALL NOT commit secrets in source code, and SHALL make bounded user-initiated requests with caching and required attribution.

#### Scenario: Release configuration lacks credentials
- **WHEN** a release build lacks usable DanDanPlay credentials
- **THEN** the system SHALL present DanDanPlay as unavailable rather than issuing unauthenticated requests with embedded fallback secrets
