## ADDED Requirements

### Requirement: Enabled Kazumi playlists preserve source-page grouping and order
Each enabled Kazumi source SHALL extract play lines from the verified playlist containers on its detail page, preserving the source-page line boundaries and episode order.

#### Scenario: Multi-line playlist is returned
- **WHEN** an enabled source detail page exposes multiple verified playlist containers
- **THEN** the returned play lines contain the same container count and each line contains only that container's episode links in page order

#### Scenario: Selector no longer matches a stable playlist container
- **WHEN** live validation finds that an enabled source selector yields duplicate, unrelated, or reordered episode links
- **THEN** the rule is corrected or marked `block-` before it is automatically enabled

### Requirement: Enabled Kazumi episode URLs resolve to verified media
Each enabled Kazumi source SHALL return a final playable media URL or HLS manifest for its validated episode using the headers required by that source.

#### Scenario: Plain direct media URL is present
- **WHEN** an episode page exposes an unencrypted direct MP4 or HLS URL
- **THEN** the source returns that direct URL only after validating it as media with the source request headers

#### Scenario: Encoded player data or iframe is present
- **WHEN** an episode page represents playback through encoded player data or a player iframe
- **THEN** the source resolves playback through the renderer or a verified decoder and returns the final media URL rather than the intermediate page or encoded value

#### Scenario: Media verification fails
- **WHEN** an episode's resolved URL cannot be verified as playable media or HLS
- **THEN** validation reports the source and failing URL and the source is not promoted as automatically enabled

### Requirement: Playback validation produces actionable diagnostics
Kazumi live-rule validation SHALL report the source key, detail URL, selected play line, selected episode URL, final resolved URL, media classification, and failure reason for every tested enabled source.

#### Scenario: Rule validation completes
- **WHEN** the validation suite runs against enabled Kazumi sources
- **THEN** it writes a per-source result that can distinguish selector failures, renderer failures, and unverified media responses
