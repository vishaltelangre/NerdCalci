# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.4.5] - 2026-03-12
### Added
- Introduced new keywords (`last`, `prev`, `previous`, `above`, and `_`) that reference the result of the preceding non-empty line.

## [2.4.4] - 2026-03-11
### Added
- Added support to swipe left on any file to quickly delete it, with an undo option.

## [2.4.3] - 2026-03-10
### Fixed
- Added a fix for an issue where `sum`/`total` values would not update automatically after inserting or deleting lines.

## [2.4.2] - 2026-03-09
### Added
- Added support to copy calculation results on tap.

## [2.4.1] - 2026-03-09
### Added
- Added a new in-app Changelog viewer to easily see recent updates and improvements.

## [2.4.0] - 2026-03-08
### Added
- Added a "File info" dialog to view detailed file metadata and statistics.

### Changed
- New files are now created instantly with a default name for a faster workflow.
- Improved file import process to preserve original creation and modification dates.

### Fixed
- Enhanced editor stability and performance.

## [2.3.0] - 2026-03-08
### Fixed
- Fixed an issue where HeliBoard keyboard layout was automatically switching back to the default layout while editing.
- Improved editor stability when moving between lines.

## [2.2.1] - 2026-03-07
### Changed
- Now tapping on "Err" shows a clear explanation of what went wrong!

## [2.1.0] - 2026-03-07
### Added
- Added a global setting to toggle line numbers in the editor.
- Added a temporary toggle for line numbers in the calculator menu.

## [2.0.1] - 2026-03-07
### Added
- Added a new "Result precision" setting to configure displayed decimal places (0-10)

### Changed
- Improved internal result storage to preserve unrounded precision and prevent data loss

### Fixed
- Ensured full backward compatibility for older, statically formatted backups and data

## [2.0.0] - 2026-03-06
### Added
- Added support for user-defined functions with multi-statement bodies (using `;`)
- Functions have scope isolation and recursion protection for safer evaluation
- Refined autocomplete suggestions with better syntax highlighting and typography

## [1.9.1] - 2026-03-06
### Added
- Added `avg` and `average` aggregate keywords to calculate the block average of all line results (up to the nearest blank/comment/error line)
- `avg` and `average` are aliases; both can be used inline in expressions (e.g. `half_avg = avg / 2`)

### Changed
- Explicit reassignment (e.g. `avg = 10`) overrides the aggregate meaning

## [1.9.0] - 2026-03-06
### Added
- Added `sum` and `total` aggregate keywords to calculate the sum of all line results in the current block (up to the nearest blank line)
- `sum` and `total` are aliases; both can be used inline in expressions (e.g. total * 0.05)

### Changed
- Explicit reassignment (e.g. total = 4) overrides the aggregate meaning

## [1.8.0] - 2026-03-05
### Added
- Introduced a custom expression engine replacing the external unmaintained `exp4j` library.
- Added a comprehensive in-app Language Reference accessible from the Help screen.

## [1.7.1] - 2026-03-04
### Changed
- Improved responsiveness when editing large files: recalculation now only processes lines affected by the edit, skipping unchanged preceding lines.
- All database result writes are now batched into a single transaction per edit, reducing per-keystroke overhead.

## [1.6.0] - 2026-03-03
### Added
- Added ability to export a calculation file as a PDF or an image file.

### Fixed
- Improved logging for backup and restore operations to help with debugging.

## [1.5.1] - 2026-03-02
### Added
- Added a new Legal section in Settings with quick links to Privacy Policy and Terms of Service.
- Added a "Report an Issue" action in Settings that opens GitHub issue creation for support.

### Changed
- Updated and clarified privacy policy details around backups (automatic/manual behavior and retention).

## [1.5.0] - 2026-03-02
### Added
- Added support for composite operations (+=, -=, *=, /=, %=) and increment/decrement (++, --) operators!

## [1.4.0] - 2026-03-02
### Added
- Added adaptive monochrome app icon!

## [1.3.0] - 2026-03-02
### Fixed
- Fix: The backspace key will now properly clear out a line's text when deleting the last character instead of deleting the entire line along with it. This restores the natural and expected editing behavior in the calculator!

## [1.2.0] - 2026-03-01
### Added
- Added file search functionality

### Changed
- Search highlights matching text and shows number of matches.
- Returning from a file preserves search query and search result position.

## [1.1.1] - 2026-02-28
### Fixed
- Removed the unnecessary `ACCESS_NETWORK_STATE` permission that was pulled in by WorkManager.

## [1.1.0] - 2026-02-27
### Added
- Added automatic backups with configurable frequency (daily or weekly).
- Default backup location now uses app storage, with optional custom folder support.
- Added "Back up now" and "Restore from backup" flows with clearer confirmation dialogs.

### Changed
- Unified restore experience across Settings and Home screens.
- Backup retention now keeps the latest 30 backups.

## [1.0.1] - 2026-02-27
### Changed
- Improved the file list empty state with a Material 3 hero, polished copy, and explicit CTAs for creating, importing, or opening help so beginners feel guided right away.

## [1.0.0] - 2026-02-12
### Added
- Initial release of NerdCalci!
- Variable support for reusable calculations
- Percentage calculations with natural syntax
- Syntax highlighting and auto-completion
- Multiple files with auto-save
- Pin important files
- Duplicate a file
- Undo/Redo support
- Import/Export files
- Dark/Light theme
