# Dependency Explorer

![Build](https://github.com/Gyeom/dependency-explorer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
Dependency Explorer is an IntelliJ IDEA plugin that simplifies the exploration of Gradle dependencies and allows you to quickly navigate to their detailed pages in Maven Repository.

### Key Features:
- Supports multiple dependency formats:
  - Gradle Standard: `implementation("group:artifact:version")`
  - Gradle DSL Short: `runtimeOnly 'group:artifact:version'`
  - Gradle DSL Named: `runtimeOnly group: 'group', name: 'artifact', version: 'version'`
- Automatically generates [Maven Repository](https://mvnrepository.com/) links for dependencies.
- Resolves variables in dependency declarations.

This plugin is designed to make dependency management easier for developers using IntelliJ IDEA.
<!-- Plugin description end -->

## Installation

### Using the IDE Built-in Plugin System:
1. Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd>.
2. Search for "Dependency Explorer".
3. Click <kbd>Install</kbd>.

### Using JetBrains Marketplace:
1. Visit [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID).
2. Click the <kbd>Install to ...</kbd> button while your IDE is running.

### Manual Installation:
1. Download the [latest release](https://github.com/Gyeom/dependency-explorer/releases/latest).
2. In your IDE:
  - Navigate to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>.
  - Select the downloaded file.

## Usage

1. Open a Gradle project file in IntelliJ IDEA.
2. Place your cursor on a dependency line.
3. Right-click and select `Open in Maven Repository`.
4. The corresponding Maven Repository page will open in your browser.

## Examples

### Gradle
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
runtimeOnly 'io.github.oshai:kotlin-logging-jvm:7.0.0'
runtimeOnly group: 'io.github.oshai', name: 'kotlin-logging-jvm', version: '7.0.0'