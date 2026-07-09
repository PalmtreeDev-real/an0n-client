<p align="center">
<img src="https://raw.githubusercontent.com/Palmtreedev-real/AN0N/main/src/main/resources/assets/anon-client/icon.png" alt="anon-client-logo" width="15%"/>
</p>

<h1 align="center">AN0N Client</h1>
<p align="center">A Minecraft Fabric Utility Mod for anarchy servers — completely free and open source.</p>

<div align="center">
    <a href="https://discord.gg/fsyFenWkm"><img src="https://img.shields.io/discord/000000000000000000?logo=discord" alt="Discord"/></a>
    <br>
    <img src="https://img.shields.io/github/last-commit/Palmtreedev-real/AN0N" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/commit-activity/w/Palmtreedev-real/AN0N" alt="GitHub commit activity"/>
    <img src="https://img.shields.io/github/contributors/Palmtreedev-real/AN0N" alt="GitHub contributors"/>
    <br>
    <img src="https://img.shields.io/github/languages/code-size/Palmtreedev-real/AN0N" alt="GitHub code size in bytes"/>
    <img src="https://img.shields.io/github/license/Palmtreedev-real/AN0N" alt="GitHub license"/>
</div>

> **⚠️ AN0N Client is in active development.** You may encounter bugs and breaking changes. Please report any issues on [Discord](https://discord.gg/fsyFenWkm) or [GitHub Issues](https://github.com/PalmtreeDev-real/an0n-client/issues).

## Why AN0N?

### vs Meteor Client
AN0N is built on Meteor but adds features Meteor doesn't have — custom rendering pipeline with shaders, an advanced HUD system, Swarm multi-client control, Notebot, ClientChat for cross-server communication, and unique modules like AnonPvp, AnonStealth, and AnonT1me version spoofing. We also maintain our own mixins and optimizations that aren't in upstream Meteor.

### vs LiquidBounce
AN0N is fully open source (Apache 2.0), lightweight, and focused on modern Minecraft versions. No bloat, no proprietary code — everything is transparent and auditable.

### vs Doomsday Client
AN0N is actively maintained, built on a modern codebase (Meteor), and doesn't lock features behind paywalls. We have a larger module selection and proper integration with Baritone, Sodium, Lithium, and other performance mods.

### vs Paid Clients (Prestige, etc.)
**AN0N is 100% free.** No payments, no subscriptions, no Patreon-exclusive features. Everything we build is available to everyone. We believe utility mods should be accessible, not monetized. You get the same features as paid clients — often more — without spending a dime.

## Flagship Features

- **AnonPvp** — Combat automation bot with skillset presets, more human-like than KillAura.
- **AnonStealth** — Hidden HUD and client files, activated only by pressing Ctrl five times.
- **AnonT1me** — Version spoofing to connect across different Minecraft versions.

---

## Error Codes

AN0N SocialChat and An0nAI display error codes to quickly identify issues.

### 🚫 Ban & Punishment
| Code | Name | Meaning |
|------|------|---------|
| 624 | Device Ban | 🛡️ Your hardware is permanently exiled from An0n SocialChat. |
| 977 | Permanent Ban | ⚰️ You are persona non grata. No appeal. No mercy. |
| 992 | Suspended from Chat | ⏳ You're on timeout. The AI is watching you. |

### ⚙️ Client & System Errors
| Code | Name | Meaning |
|------|------|---------|
| 726 | Outdated Client | 🔄 Update your client or get left behind. |
| 872 | Client Issue | 🔧 Something's broken on your end. Reinstall. |
| 192 | AI Not Found | 🧠 The brain is missing. Did Ollama fail? |

### 🌐 Network & Server Errors
| Code | Name | Meaning |
|------|------|---------|
| 762 | Server Overloaded | 🌍 Too many An0n users. The network is popping off. |

### ✨ Future / Meme Codes
| Code | Name | Meaning |
|------|------|---------|
| 420 | Roasted by AI | 💥 You got called out publicly in SocialChat. |
| 666 | AI Corruption | 👿 The AI has gone rogue. Good luck. |
| 001 | Admin Override | 👀 You did something. Or the AI did. Who knows. |
| 404 | Feature Not Found | 🔍 You tried to enable something that doesn't exist yet. |

---

## ⚠️ Disclaimer: Read the Code

AN0N Client is **fully open source**. Every line of code is visible in this repository. Before making accusations about malware, RATs, or malicious code:

- **Read the source.** The entire codebase is here for you to inspect, audit, and build yourself.
- **Build from source.** If you don't trust pre-built binaries, clone the repo and run `./gradlew build` — you'll get a clean JAR compiled from the exact code you see.
- **No obfuscation.** Unlike many clients (including some paid ones), our code is not obfuscated. What you see is what you get.

We take security and transparency seriously. If you find a genuine security issue, please report it responsibly.

---

## Usage

### Building
- Clone this repository
- Run `./gradlew build`
- The built JAR will be in `build/libs/`

### Installation
1. Install [Fabric Loader](https://fabricmc.net/) for **Minecraft 26.1.2**.
2. Download the [latest AN0N release JAR](https://github.com/PalmtreeDev-real/an0n-client/releases).
3. Download the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) for **Minecraft 26.1.2**.
4. Place both the AN0N JAR and Fabric API JAR in your `.minecraft/mods` folder.
5. Launch Minecraft with the **Fabric** profile.

> **Note:** You **must** have Minecraft 26.1.2 and Fabric API installed. AN0N will not work on other versions or without Fabric API.

## Contributions
We will review and help with all reasonable pull requests as long as the guidelines below are met.

- The license header must be applied to all java source code files.
- IDE or system-related files should be added to the `.gitignore`, never committed in pull requests.
- In general, check existing code to make sure your code matches relatively close to the code already in the project.
- Favour readability over compactness.

## Bugs and Suggestions
Bug reports and suggestions should be made in this repo's issue tracker using the templates provided.
Please provide as much information as you can to best help us understand your issue and give a better chance of it being resolved.

## Credits
- AtlasDevMC - AN0N Client development
- Meteor Development - Original Meteor Client this is based on
- Mojang AB - Minecraft
- Fabric Team - Fabric mod loader and Yarn mappings
- Apache Software Foundation - Maven build system
- Cabaletta and WagYourTail - Baritone
- All other open source contributors (see NOTICE file)

## Licensing
This project is licensed under the Apache License, Version 2.0.
Contains code derived from Meteor Client (GPL-3.0) and other open source projects.
See the NOTICE file for detailed attributions.
