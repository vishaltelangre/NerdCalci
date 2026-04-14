# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.7.2] - 2026-04-15
### Added
- Added multi-word energy units (gigajoules, TNT-equivalent variants), "dozen" multiplier, and physical derivation rules for speed, time, and distance (PR #110).

### Fixed
- Fixed an editor synchronization issue that caused cursor jumping or text overwriting during rapid typing (PR #121).

## [3.7.1] - 2026-04-13
### Added
- Added support for plural numeral multipliers (hundreds, thousands, etc.) and quadrillion/quintillion scales (PR #122).

### Fixed
- Fixed Enter key behavior in the calculator to correctly split lines at the cursor position instead of appending a new line (PR #123).

## [3.7.0] - 2026-04-10
### Added
- Added file sorting functionality (Issue #79).
- Added ability to lock files to protect them from accidental edits and deletions (Issue #57).

### Changed
- Removed duplicate create and scratchpad shortcuts on home when there are no files to show (Issue #116).
- File rows now show both created and modified timestamps.

## [3.6.0] - 2026-04-09
### Added
- Added a global setting to show an ellipsis for truncated numbers that exceed the configured result precision (Issue #106).
- Added a configurable setting to show or hide the temporary scratchpad shortcut on the home screen via a new bolt icon (Issue #109).

### Fixed
- Fixed an issue where changing "Auto-open a file on launch" settings caused unintended navigation.
- Fixed an issue where the calculator's last sync time was not persisted across application restarts.

## [3.5.0] - 2026-04-09
### Added
- Added flexible auto-open on launch modes: Choose between Home, Scratchpad, Daily Journal, or a Specific file (Issue #108).

### Fixed
- Fixed Rational mode resetting when splitting, merging, or deleting lines, and when using undo/redo (Issue #107).

## [3.4.2] - 2026-04-04
### Fixed
- Made built-in math functions handle unit values more safely, with clearer errors when a function cannot use a given unit.
- Fixed modulo behavior so values with physical units now fail clearly instead of producing incorrect results.

## [3.4.1] - 2026-04-03
### Fixed
- Fixed unit exponentiation so powered quantities like `ft^2` and `ft^3` preserve their units correctly.
- Fixed a crash when opening Settings on Android 8.1 and earlier (Issue #94).

## [3.4.0] - 2026-04-03
### Added
- Added a temporary "Scratchpad" for quick calculations, with an option to auto-open it on launch (Issue #66).

### Fixed
- Disallowed additive operations on units of different categories and unitless values for predictable results.
- Fixed temperature unit arithmetic handling in multiplication and division operations.
- Made unit math more user-friendly: `ft * ft` stays in square feet, mixed units like `ft * m` now fail clearly.

## [3.3.0] - 2026-04-01
### Added
- Added "Rational mode" for exact arithmetic using fractions (Issue #87).
- Added `rational(x)`/`fraction(x)` functions for rational display override and `float(x)` for float display override.
- Support units on parenthesized expressions (e.g., `(1/2) kg`).
- Unified number formatting under a single "Region" selector, replacing manual decimal/separator settings.

### Changed
- Improved the `.nerdcalci` file format to use a structured JSON-based result serialization. This ensures that calculated results can be unambiguously identified and stripped during import while perfectly preserving user comments.
- Increased the threshold for scientific notation so large numbers and small numbers are displayed in standard decimal format by default.

## [3.2.0] - 2026-03-31
### Added
- Added support for merging lines (Backspace) and splitting lines (Enter) for a more convenient editing experience.
- Added automatic focus and keyboard activation for newly created or empty files (Issue #92).
- Added locale-aware number formatting setting for separator and decimal styles (Issue #81).
- Added support for variables as quantity anchors in unit conversions (e.g., `a = 15; a km to m`).

### Fixed
- Fixed mixed unit multiplication and division so derived dimensions now preserve area and volume correctly (Issue #88).
- Fixed a cursor synchronization issue in the calculator editor where the cursor would jump during rapid typing or backspacing (Issue #84).

## [3.1.2] - 2026-03-30
### Added
- Added `factorial()` and `fact()` built-in functions (Issue #69).

### Fixed
- Fixed an issue where the user-defined result precision was ignored during PDF and image exports (Issue #73).
- Improved `total` and `average` variables to preserve units, enforce dimensional safety, and handle unitless quantities as scalars (Issue #74).
- Fixed precision loss bugs for very large numbers (Issue #80).
- Fixed temperature addition and subtraction so mixed-unit expressions normalize consistently before arithmetic (Issue #83).

## [3.1.1] - 2026-03-29
### Added
- Added `value()`, `dropUnit()`, and `raw()` functions to extract the numeric value from quantities (e.g. `value(10km)` returns `10`).
- Improved unit conversion autocomplete with support for multi-word units (e.g., "kilometers per hour").

### Changed
- Refined syntax highlighting for conversion keywords (`to`, `in`, `as`).

### Fixed
- Fixed a bug during unit autocomplete which inserted the unit name right after conversion keyword without inserting space in between.

## [3.1.0] - 2026-03-27
### Added
- Sync your calculations files across devices using your preferred sync method (Syncthing, WebDAV, etc.).
- Synchronization of file pinning status across devices.

### Changed
- Exported files now embed `syncId`, `isPinned`, `lastModified`, and `createdAt` timestamps for perfect restoration.
- Backup and sync locations now show human-readable absolute paths.

### Fixed
- Calculator display results now respect the device's locale (e.g., correct decimal separators) while exports remain locale-independent for stability.

## [3.0.1] - 2026-03-23
### Added
-   Added `pi` and `π` as aliases for the `PI` constant.
-   Added `e` as an alias for `E`.
-   Added `π` symbol to the shortcuts bar for quick access.

## [3.0.0] - 2026-03-22
### Added
- Added support for converting between 100+ units across Time, Length, Weight, Temperature, Data, and more!
- Type conversions naturally: e.g., `10 km in m` or `2 hours to minutes`.
- Mix units in math safely: e.g., `10 km + 500m` automatically scales the result.
- Type high numbers quickly with words like `million`, `crore`, `thousand`, etc.
- Smarter autocomplete suggestions for units and keywords right when you type numbers.
- Added the degree symbol (`°`) to the shortcut bar for easy entry.

### Changed
- The built-in trigonometric functions now accept arguments like `45°` or `45 deg`; by default, the arguments are treated as radians.

## [2.8.0] - 2026-03-20
### Added
- Added a conflict resolution screen when restoring or importing files with duplicate names.
- Shows options to keep local file, replace with file from ZIP, or keep both files with safe auto-renaming.
- Shows restore/import progress and a completion summary (counts for added, replaced, and skipped files).

### Changed
- Opening a file no longer updates its last-modified time.

## [2.7.0] - 2026-03-19
### Added
- Added support for referencing variables and functions from other files (e.g., `file("Summary").someVariable` or `otherFile.someFunction(2, localVar)`).

## [2.6.1] - 2026-03-17
### Fixed
- Fixed an issue where the modulo operator (e.g. `10%3`) was parsed as a percentage literal, causing calculation errors.

## [2.6.0] - 2026-03-17
### Added
- Added a numbers shortcuts bar below the special symbols shortcuts bar for quick digit input.
- Added global and local settings to toggle the visibility of the symbols and numbers shortcuts bars independently.

## [2.5.2] - 2026-03-17
### Fixed
- Fixed an issue where empty lines were lost during file export/import cycles.
- Improved inline comment restoration on calculations and simple assignments to prevent them from being discarded on import.

## [2.4.9] - 2026-03-16
### Fixed
- Fixed the cursor visual offset bug on empty lines while maintaining correct backspace line deletion behavior.

## [2.4.8] - 2026-03-15
### Added
- Added support for smart fuzzy matching highlights in file searches.
- Autofocus search field and retain the search term on navigating back to search screen from file screen.

### Changed
- Updated UI of the search screen to match with the overall app theme.

## [2.4.7] - 2026-03-14
### Added
- Improved autocomplete suggestions with advanced fuzzy matching for more relevant suggestions.
- Function suggestions now include a `()` suffix for clearer visual distinction from variables.
- Added a global setting to toggle autocomplete suggestions in Settings.
- Added a per-file toggle for autocomplete suggestions in the calculator menu for quick overrides.

### Changed
- Refined autocomplete popup UI with a better badge design and consistent layouts.
- Enhanced identifier detection and parenthesis matching logic for more reliable editing.

### Fixed
- Fixed an issue where autocomplete suggestion selection would not replace the keyword under the cursor correctly.

## [2.4.6] - 2026-03-13
### Added
- Introduced new keywords (`lineno`, `linenumber`, and `currentLineNumber`) to reference the current line number in calculations.
- Refined the shortcut bar to include the `^` (power) symbol and reordered symbols for a better editing experience.

### Fixed
- Fixed syntax highlighting for all reserved keywords including `lineno`, `last`, and `_`.

## [2.4.5] - 2026-03-12
### Added
- Introduced new keywords (`last`, `prev`, `previous`, `above`, and `_`) to reference the result of the line directly above, or `0` if that line is blank, a comment, or an error.

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
