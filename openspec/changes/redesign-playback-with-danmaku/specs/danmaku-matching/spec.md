## ADDED Requirements

### Requirement: Danmaku section communicates current state
The V2 playback-detail page SHALL show a peer-level `弹幕` section that communicates disabled, matching, matched, empty, unavailable, or failed state for the current playback episode and exposes an appropriate next action.

#### Scenario: No existing binding
- **WHEN** the current episode has no resolved danmaku binding
- **THEN** the danmaku section SHALL identify the source as DanDanPlay and expose automatic matching and manual search actions

#### Scenario: Matched episode
- **WHEN** danmaku comments have been resolved for the current playback episode
- **THEN** the section SHALL show the matched DanDanPlay anime and episode identity, comment availability, and a replacement action

### Requirement: Automatic matching is safe and reusable
The system SHALL attempt automatic matching only when automatic matching is enabled and no usable binding exists. It SHALL reuse a saved binding before performing a new network search and SHALL only save an automatic binding for a unique or high-confidence result.

#### Scenario: Reuse a prior binding
- **WHEN** the user returns to a playback episode with a valid saved DanDanPlay binding
- **THEN** the system SHALL load comments using that binding without repeating anime search

#### Scenario: Ambiguous automatic result
- **WHEN** automatic matching returns multiple plausible remote episodes without a high-confidence winner
- **THEN** the system SHALL not create a binding and SHALL offer manual matching

### Requirement: Manual matching follows an editable three-stage flow
The system SHALL provide one manual-matching flow that starts with an editable title query, displays DanDanPlay anime candidates, displays remote episode candidates for the selected anime, and saves the user-selected episode binding before loading comments.

#### Scenario: User selects a remote episode
- **WHEN** the user selects a remote episode in the manual matching flow
- **THEN** the system SHALL persist the binding for the current playback identity and load its comments

#### Scenario: Search returns no candidates
- **WHEN** a manual title search produces no anime candidates
- **THEN** the system SHALL retain the user's editable query and present an empty-result state without changing the current binding

### Requirement: Matching and comment retrieval are bounded and cache-aware
The system SHALL cache search, remote episode, and comment results with expiry and SHALL cancel or ignore stale matching/load work when the playback episode changes. Failures SHALL not disable video playback or replace an existing valid binding.

#### Scenario: Episode changes during loading
- **WHEN** the user changes playback episode before a matching or comment request finishes
- **THEN** the stale result SHALL NOT update the newly selected episode's danmaku state

#### Scenario: DanDanPlay is unavailable
- **WHEN** a DanDanPlay request fails because of network, authentication, quota, or service error
- **THEN** the system SHALL retain video playback, surface a retryable unavailable state, and preserve any existing valid binding
