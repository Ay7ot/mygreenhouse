# My Greenhouse – Design Style Guide

## 1. Color Palette

| Token | Role / Usage | Hex |
|-------|--------------|-----|
| `DarkBackground` | App background (Dark) | `#0D1510` |
| `DarkSurface` | Surfaces / cards (Dark) | `#1A2620` |
| `PrimaryGreen` | Brand primary / highlights | `#4AE54F` |
| `PrimaryGreenLight` | Lighter accent of primary | `#7BC47B` |
| `SecondaryGreen` | Secondary dark tint | `#2A3933` |
| `DarkerGreenButton` | Filled buttons on Dark | `#3A7D44` |
| `TextWhite` | High-contrast text (Dark) | `#FFFFFF` |
| `TextGrey` | Secondary text (Dark) | `#CCCCCC` |
| `TaskAlertGreen` | Success / task done state | `#4AE54F` |
| `LightBackground` | App background (Light) | `#F8FDF8` |
| `LightSurface` | Surfaces / cards (Light) | `#EBF5EB` |
| `PrimaryGreenDark` | Primary on Light | `#2E7D32` |
| `LightPrimary` | M3 `primary` (Light) | `#388E3C` |
| `LightSecondary` | M3 `secondary` (Light) | `#66BB6A` |
| `LightTertiary` | M3 `tertiary` (Light) | `#81C784` |
| `LightOnPrimary` | Content on primary | `#FFFFFF` |
| `LightOnSecondary` | Content on secondary | `#000000` |
| `LightOnTertiary` | Content on tertiary | `#000000` |
| `LightOnBackground` | Primary text (Light) | `#2E7D32` |
| `LightOnSurface` | Surface text (Light) | `#2E7D32` |
| `LightSurfaceVariant` | Subtle surfaces | `#DDE5DA` |
| `LightOnSurfaceVariant` | Text on variant | `#424940` |
| `LightOutline` | Divider / outline | `#727970` |

> The palette follows WCAG-AA contrast where possible and mirrors Material 3 roles for dynamic theming.

---

## 2. Spacing & Layout

| Purpose | Value (px) |
|---------|------------|
| Global screen padding | **16 px** (horizontal), **16-24 px** (vertical) |
| Component internal padding | **12 – 16 px** |
| Dense element gap (icon + label) | **4 – 8 px** |
| List-item vertical spacing | **8 – 12 px** |
| Section spacing / gutters | **24 px** |
| Corner radius | **12 px** (cards), **8 px** (chips / small btns), **16 px** (FAB) |
| Card elevation (shadow) | **0 px** (flat) – emphasis via color, not shadow |
| Button height | **50 – 56 px** |

*All measurements assume a **1× (mdpi)** baseline where **1 dp = 1 px**. Multiply by the device’s scale factor for @2×, @3×, etc.*

---

## 3. Typography

The project uses Material 3 default `Typography` with minimal overrides. Key text styles seen in UI:

| Style | Weight | Size (px) | Typical Usage |
|-------|--------|----------|---------------|
| `headlineMedium` | Bold | **24 px** | Screen titles (empty states, dialogs) |
| `titleMedium` | SemiBold | **16-20 px** | Card / list headers, section titles |
| `bodyLarge` | Regular | **16 px** | Primary body text |
| `bodyMedium` | Regular | **14 px** | Secondary body text |
| `bodySmall` | Medium | **12 px** | Caption, metadata |
| `labelLarge` | Medium | **14 px** | Form labels / chips |

Font family is the system default; weights & letter-spacing follow Material 3 guidance (e.g., 0.5 sp on bodyLarge).

> Type is set in Android **sp** (scale-independent px). Values above equal px at 100 % user font scale.

---

## 4. Iconography

We rely on **Material Symbols** plus a small custom glyph for feeding.

### Task Types

| TaskType | Icon (Material) |
|----------|-----------------|
| Watering | `Opacity` (water-drop) |
| Feeding | Custom `FeedingIcon.WateringCan` |
| Pest Control | `BugReport` |
| Soil Test | `Science` |
| Water Test | `WaterDrop` |
| Light Cycle Change | `LightMode` |
| CO₂ Supplementation | `Co2` |
| Other | `FormatListBulleted` |

### Global / Navigation Icons

- `ArrowBack` / `ArrowBackAndroid` – navigation up    
- `Add` – FABs / create actions    
- `Edit` – inline edit    
- `Delete` – destructive actions    
- `Search`, `FilterList` – search & filter    
- `Star` – rating    
- `Check`, `CheckCircle`, `Done` – completion    
- `Close` – dismiss / clear    
- `PlaylistAdd`, `List`, `Today` – list utilities    

Icons follow Material Design optical sizing with **24 px** bounding boxes (2-px stroke/offsets). Task-status dots are **8 px** circles tinted with semantic colors (success, error, warning).

---

## 5. Theming Strategy

The app ships with *dark* and *light* schemes. Switching is driven by user preference (see `ThemePreference.kt`) and applied through `MyGreenhouseTheme` (Material 3 `dynamicColor = false`).

- Color roles map to the palette above.
- `DarkSurface` & `LightSurfaceVariant` keep UI flat with subtle separators instead of shadows.
- Alpha variations (e.g., `PrimaryGreen.copy(alpha = 0.15f)`) communicate states such as selection, disabled, or background emphasis.

---

### Updating This Guide

Add any new color or component tokens to `Color.kt` and document them here to keep design-dev parity.
