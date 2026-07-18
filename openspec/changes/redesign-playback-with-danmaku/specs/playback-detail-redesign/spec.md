## ADDED Requirements

### Requirement: Isolated playback-detail page preserves the legacy page
The system SHALL provide a new playback-detail page for the redesigned experience without modifying the existing playback-detail page's composition or behavior. The system SHALL provide a scoped mechanism to select the legacy page during the migration.

#### Scenario: Default V2 playback entry
- **WHEN** a user opens a supported cartoon playback detail through the V2 route
- **THEN** the system SHALL show the redesigned playback-detail page using the existing playback and cartoon state

#### Scenario: Legacy rollback entry
- **WHEN** the legacy playback-detail selection mechanism is active
- **THEN** the system SHALL open the unchanged legacy playback-detail page for the same cartoon and selected episode

### Requirement: V2 page preserves core media information and actions
The V2 page SHALL display the video player, cartoon identity, a synopsis clamped to two lines, an expand affordance, and the existing actions for follow, search, website, download, and external playback. The action row SHALL have a visible divider below the entire group and SHALL NOT place dividers between individual actions.

#### Scenario: Collapsed synopsis
- **WHEN** the V2 page first displays a cartoon with a synopsis
- **THEN** the synopsis SHALL be limited to two lines and an expand affordance SHALL be visible

#### Scenario: Expanded synopsis
- **WHEN** the user activates the synopsis expand affordance
- **THEN** the page SHALL display the full synopsis without removing the core action row

### Requirement: Playback source and quick episode navigation are grouped
The V2 page SHALL present playback source selection as a horizontally scrollable source row and SHALL present the selected source's episodes as a horizontally scrollable quick episode rail. The quick episode buttons SHALL use a wider short rounded shape and SHALL indicate the currently playing episode.

#### Scenario: Switch playback source
- **WHEN** the user selects another playback source
- **THEN** the quick episode rail SHALL update to that source's sorted episodes without changing the current playing episode until a new episode is selected

#### Scenario: Select an episode from the quick rail
- **WHEN** the user selects an episode in the quick episode rail
- **THEN** the system SHALL change playback using the selected source and episode

### Requirement: Episode controls and picker support complete selection
The episode heading SHALL expose sorting and an all-episodes action. The all-episodes action SHALL open a Material bottom sheet with source selection, episode search, current sorting, a grid of episodes, and current-episode focus.

#### Scenario: Open all episodes
- **WHEN** the user activates all episodes from the episode heading
- **THEN** the system SHALL open the episode picker bottom sheet without inserting an all-episodes item into the quick episode rail

#### Scenario: Select an episode in the picker
- **WHEN** the user selects an episode in the episode picker
- **THEN** the system SHALL update playback and dismiss the picker

#### Scenario: Apply episode sorting
- **WHEN** the user changes the episode sort or display configuration
- **THEN** the quick rail and episode picker SHALL use the same updated ordering configuration
