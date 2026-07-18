## ADDED Requirements

### Requirement: Danmaku renders as a playback overlay
The system SHALL render loaded comments through DanmakuFlameMaster in a transparent overlay above the video texture and below interactive player controls. The overlay SHALL not intercept player-control interactions.

#### Scenario: Render overlay with controls visible
- **WHEN** danmaku is enabled during in-app playback
- **THEN** comments SHALL render above video while the existing player controls remain usable

### Requirement: Renderer follows playback timing and lifecycle
The renderer SHALL synchronize start, pause, seek, hide/show, and release with the active ExoPlayer session. It SHALL clear or reposition comments appropriately when the user seeks or switches episodes.

#### Scenario: Pause and resume playback
- **WHEN** ExoPlayer pauses and later resumes
- **THEN** the danmaku renderer SHALL pause and resume on the corresponding playback timeline

#### Scenario: Seek playback
- **WHEN** the user seeks to a new playback position
- **THEN** the renderer SHALL discard obsolete visible comments and synchronize rendering to the new position

#### Scenario: Leave playback page
- **WHEN** the V2 playback page is disposed or its player session is released
- **THEN** the renderer SHALL release its view and resources and SHALL not continue emitting comments

### Requirement: User display preferences are respected
The system SHALL provide persisted controls for enabling danmaku and supported visibility preferences, including comment category/source filtering and time offset. Preference changes SHALL apply without requiring the user to reopen the playback page.

#### Scenario: Disable danmaku
- **WHEN** the user disables danmaku
- **THEN** the overlay SHALL stop displaying comments while preserving the resolved binding for later reuse

#### Scenario: Adjust time offset
- **WHEN** the user changes the danmaku time offset
- **THEN** subsequent comment scheduling SHALL use the updated offset
