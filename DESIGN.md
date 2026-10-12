---
name: SkillScope
description: Kompetentsitestide platvorm — täpne, mõõdetud ja sõbralik.
colors:
  slate-primary: "#37474f"
  slate-hover: "#263238"
  slate-ink: "#1c262b"
  slate-mist: "#eceff1"
  slate-haze: "#b0bec5"
  slate-soft-fill: "#e3e9ec"
  aloe-green: "#1f8a4c"
  aloe-mint: "#92e3a9"
  pass-teal: "#00b894"
  pass-teal-ink: "#00604d"
  pass-teal-mist: "#d9f7f0"
  warning-amber: "#ffc107"
  danger-red: "#dc3545"
  field-bg: "#f5f8f6"
  surface: "#ffffff"
  ink: "#1a1a2e"
  nav-arc: "#e4ede7"
  dark-field-bg: "#101d1d"
  dark-surface: "#1b2a29"
  dark-surface-raised: "#233434"
  dark-border: "#314444"
  dark-text: "#dce7e7"
  dark-slate-primary: "#186e86"
  dark-slate-accent: "#84cbdc"
  dark-aloe-green: "#4cc47f"
  dark-pass-teal: "#34d1a8"
  dark-warning-amber: "#f0c454"
typography:
  wordmark:
    fontFamily: "Sora, system-ui, sans-serif"
    fontSize: "1.6rem"
    fontWeight: 800
    letterSpacing: "-0.06em"
  headline:
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1.2
  title:
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 500
    lineHeight: 1.2
  body:
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  small:
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 700
    letterSpacing: "0.1em"
rounded:
  sm: "0.375rem"
  nav: "0.6rem"
  md: "0.75rem"
  lg: "1rem"
  xl: "1.5rem"
  pill: "50rem"
spacing:
  xs: "0.25rem"
  sm: "0.5rem"
  md: "1rem"
  lg: "1.5rem"
  xl: "3rem"
  dot-grid: "22px"
components:
  button-primary:
    backgroundColor: "{colors.slate-primary}"
    textColor: "{colors.surface}"
    rounded: "{rounded.sm}"
    padding: "0.375rem 0.75rem"
  button-primary-hover:
    backgroundColor: "{colors.slate-hover}"
    textColor: "{colors.surface}"
  button-soft:
    backgroundColor: "{colors.slate-soft-fill}"
    textColor: "{colors.slate-primary}"
    rounded: "{rounded.sm}"
    padding: "0.375rem 0.75rem"
  button-soft-hover:
    backgroundColor: "#cfd8dc"
    textColor: "{colors.slate-hover}"
  button-success:
    backgroundColor: "{colors.pass-teal}"
    textColor: "{colors.surface}"
    rounded: "{rounded.sm}"
    padding: "0.375rem 0.75rem"
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "1rem"
  sidebar:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.lg}"
    padding: "1.25rem 0.75rem"
    width: "220px"
  nav-link:
    textColor: "#6c757d"
    rounded: "{rounded.nav}"
    padding: "0.6rem 0.75rem"
  nav-link-active:
    backgroundColor: "rgba(55, 71, 79, 0.1)"
    textColor: "{colors.slate-primary}"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "0.375rem 0.75rem"
  badge-active:
    backgroundColor: "{colors.pass-teal-mist}"
    textColor: "{colors.pass-teal-ink}"
    rounded: "{rounded.pill}"
  tooltip:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "0.6rem 0.85rem"
---

# Design System: SkillScope

## Overview

**Creative North Star: "Täpne mõõtekoda" (The Measured Field)**

SkillScope on koht, kus oskust mõõdetakse, mitte ei reklaamita. Kogu maailm toetub mõõteruudustikule: lehe taustal on 22px sammuga täpivõrk, mis jookseb läbi navbari kaare ja lehe sisu ning ühtlustab kõik pinnad. Logo on sihtmärk ("scope") ja tulemuseks on protsendiring. Visuaal kinnitab, et hindamine on selge, korratav ja usaldusväärne.

Täpsus ei tähenda külmust. Komponendid on **sõbralikud ja kindlad**: ümarad nurgad, paks nupukiri ja selged olekud. Pinnad hõljuvad heledal rohekal väljal pehmelt tõstetuna. Põhitoon on tumehall kiltkivi (`slate-primary`), mis hoiab struktuuri ja tegevusi. Aaloe-roheline on harv brändiaktsent ning sinakasroheline `pass-teal` tähistab ainult edukat tulemust. Liikumine on peaaegu vaikne: iga 15 sekundi järel libiseb üle täpivõrgu (ka navbari kaare) suuremate täppidega laine, mis meenutab mõõteseadme skaneerimist.

Tihedus on töörakenduse oma (Operate): desktop on esmane, sisu on kaartides ja külgmenüü on alati käepärast. Hele ja tume teema on võrdväärsed. Tume teema on "mõõtekoda öösel": pinnad on süvaroheka-sinakashalliks toonitud, mitte neutraalselt hallid. Nende kohal helendavad teraskiltkivi, aaloe-roheline ja mündi täpivõrk.

**Key Characteristics:**
- 22px täpivõrk kui ühine mõõteruudustik (taust, navbari kaar, täpilaine)
- Kiltkivihall struktuur, aaloe-roheline ainult brändiks, türkiis ainult edukaks tulemuseks
- Pehmelt tõstetud valged pinnad, mille eeskuju on külgmenüü kahekihiline vari
- Ümarad (1rem) konteinerid, kompaktsed (0.375rem) nupud
- Süsteemifont sisuks, Sora ainult sõnamärgiks
- Täielik hele/tume paarsus; tume teema on toonitud ja helendav "öine mõõtekoda", mitte kustutatud tuled

## Colors

Palett on võetud aaloe illustratsioonist: üks kiltkivihall põhitoon, kaks rohelist brändiaktsenti, eraldi türkiis edu jaoks ja rohekas neutraalne väli.

### Primary
- **Kiltkivihall** (`slate-primary`): nupud, lingid, valitud olek, aktiivne menüülink, märgitud linnuke ja külgmenüü päiseriba. See on süsteemi tegevusvärv. Hoveril muutub see tumedamaks (`slate-hover`) ja vajutusel veel tumedamaks (`slate-ink`).
- **Kiltkivi udu ja vine** (`slate-mist`, `slate-haze`): primary õrnad taustad ja piirjooned, fookusringi piirjoon ning dropdowni hover.
- **Pehme täide** (`slate-soft-fill`): sekundaarse nupu taust. Bootstrapi vaikimisi `btn-light` oleks valgel kaardil nähtamatu.

### Secondary
- **Aaloe-roheline** (`aloe-green`): logo, `.text-brand` pealkirjad ja "sooritatud" tulemustes. See on ainus brändiaktsent.
- **Aaloe münt** (`aloe-mint`): progressiriba, logo sisemine ring ja eduka tulemuse gradiendi lõpp.

### Tertiary
- **Edu-türkiis** (`pass-teal`, `pass-teal-ink`, `pass-teal-mist`): Bootstrapi success-värv. See on külmem kui brändirohelised, et "edukas" ei läheks brändiga segi. Seda kasutatakse aktiivse staatuse märgil, success-nupul ja eduka tulemuse päises.
- **Merevaigukollane** (`warning-amber`, tumedas `dark-warning-amber`): AI märgid, staatus "pooleli" ja tärnhinnang.
- **Ohu-punane** (`danger-red`): mitteaktiivne staatus, kustutamine ja tagasilükkamine.

### Neutral
- **Rohekas väli** (`field-bg`): lehe taust täpivõrgu all. Paremas ülanurgas on kerge mündikuma.
- **Pind** (`surface`): kaardid, külgmenüü, navbar ja tooltip.
- **Tint** (`ink`): esiletõstetud tekst (menüülingi hover, sõnamärgi "Skill").
- **Navbari kaar** (`nav-arc`): navbari vasaku kaare täide, millel on oma täpivõrk.
- **Tume teema** (`dark-*`): neutraalid on toonitud (OKLCH toon ~195, väike küllastus). Pinnad heledamad astmeti: väli → kaart → tõstetud pind. Primary-nupp on `dark-slate-primary` (teraskiltkivi, valge tekst 5.8:1), primary-tekst, lingid ja aktiivne olek `dark-slate-accent`. Lehel on kaks kuma: aaloe-roheline paremal üleval ja teraskiltkivi vasakul all. Täpivõrk ja täpilaine on mündi tooniga. Külgmenüü päiseriba on gradient: aaloe-roheline → türkiis → teraskiltkivi.

### Named Rules
**Roheline-ei-ole-õige reegel.** Rohelist ega türkiisi ei kasutata testi lahendamise ajal valitud vastuse märkimiseks, sest see mõjuks nagu "õige vastus". Valitud olek on alati kiltkivihall.

**Üks aktsent reegel.** Aaloe-roheline on brändi hääl ja seda kasutatakse säästlikult: logos, üksikutes pealkirjades ja tulemuses. Tegevused ja olekud on kiltkivihallid.

**Öise mõõtekoja reegel.** Tume teema ei ole heleda teema tuledeta versioon. Neutraalid on brändi tooniga toonitud, aktsendid on pimedas küllastunumad ja helendavad, ning pinnad eristuvad heleduse astmetega, mitte ainult varjuga. Puhast neutraalset halli (`#2b3036` tüüpi) ei kasutata.

**Kaksikmuutuja reegel.** Iga Bootstrapi muutuja või komponendivärv, mis `:root`-is üle kirjutatakse, tuleb uuesti määrata ka `[data-bs-theme='dark']` all. Muidu jääb see tumedas teemas heledaks.

## Typography

**Display Font:** Sora 800 (Google Fonts), ainult sõnamärgiks "SkillScope"
**Body Font:** Bootstrapi süsteemifondi pinu (system-ui, -apple-system, Segoe UI, Roboto…)

**Character:** Neutraalne ja kiirelt loetav süsteemifont teeb sisu tööriista sarnaseks. Sora tihe ja negatiivse vahega sõnamärk annab brändile isiksuse ainult ühes kohas.

### Hierarchy
- **Wordmark** (800, 1.6rem, -0.06em): ainult navbari logos. "Skill" on tindivärvi, "Scope" aaloe-rohelise gradiendiga.
- **Headline** (700, 1.75rem, SUURTÄHTEDES): lehe pealkiri, nt "KÜSIMUSTE PANK". Kasutatakse `h1` elementi `h3` suurusega.
- **Title** (500, 1.5rem): kaardi pealkiri (`h4`). Pikk pealkiri kärbitakse kahe rea järel.
- **Body** (400, 1rem, 1.5): kirjeldused, vormid ja küsimuste tekst.
- **Small** (400, 0.875rem): tooltip, abitekst ja sekundaarne info.
- **Label** (700, 0.75rem, 0.1em, SUURTÄHTEDES): külgmenüü ja sektsioonide sildid. Eyebrow-variant on 600 kaaluga, vahega 0.08em ja 80% läbipaistvusega.

### Named Rules
**Sora on allkiri reegel.** Sora kirjatüüpi kasutatakse ainult sõnamärgis. Pealkirjad ja sisu jäävad süsteemifondile.

**Paks nupp reegel.** Tegevusnuppudel on `fw-bold`. Nupu tekst on kindel, mitte õhuke.

## Layout

Rakenduse karkass: navbar (57px kõrge) ja selle all kaherealine paigutus. Vasakul on kleepuv külgmenüü (220px laiune, 1.5rem kaugusel ülaservast) ja paremal sisuala (`container-fluid py-4`). Lehe taust (`.layout`) täidab ülejäänud kõrguse ja on `background-attachment: fixed`, nii et täpivõrk ei keri kaasa.

Vahed järgivad Bootstrapi skaalat (0.25 / 0.5 / 1 / 1.5 / 3rem). Kaardiruudustikes on vahe 1.5rem (`gap-4`). Väga laiadel ekraanidel (1800px, 2400px, 3000px) lisatakse ruudustikule veerge, et kaardid ei veniks. Mobiil on teisejärguline: modaalid lähevad alla 576px täislaiusesse.

### Named Rules
**Ruudustiku reegel.** Kõik täpivõrgud (lehe taust, navbari kaar, täpilaine) kasutavad 22px sammu ja fikseeritud kinnitust, et täpid joonduksid üle kõigi kihtide. Uus täpiline pind peab järgima sama sammu.

## Elevation & Depth

Süsteem on **pehmelt tõstetud**: valged pinnad hõljuvad rohekal täpilisel väljal. Normatiivne eeskuju on **külgmenüü vari**. See on kahekihiline ja väga hajus ning sellega käib kaasas peaaegu nähtamatu 1px piirjoon. Vari on märkidena `theme.css`-is (`--app-surface-shadow`, `--app-surface-shadow-hover`, `--app-surface-border`, mõlemas teemas) ja kaardid saavad selle klassiga `.app-surface`.

### Shadow Vocabulary
- **Pinna vari** (`--app-surface-shadow`: `0 1px 2px rgba(26,26,46,0.04), 0 8px 24px rgba(26,26,46,0.08)`, koos `--app-surface-border` 1px piiriga; klass `.app-surface`): külgmenüü ja kõik puhkeolekus kaardid ning paneelid.
- **Pinna hover-vari** (`--app-surface-shadow-hover`: `0 2px 4px rgba(26,26,46,0.05), 0 12px 32px rgba(26,26,46,0.12)`): klikitava kaardi hover koos 4px tõusuga.
- **Hõljuv vari** (`0 0.5rem 1.5rem rgba(26,26,46,0.12)`; tumedas `rgba(0,0,0,0.4)`): tooltip ja muud ajutiselt pinna kohal olevad elemendid.
- **Tulemusringi kuma** (`0 0.5rem 1.5rem rgba(var(--bs-primary-rgb),0.12)`): tulemusring, mis kattub osaliselt päisega.
- **Modaali vari** (`0 1.5rem 3.5rem rgba(28,38,43,0.35)`): dialoogid.

### Named Rules
**Külgmenüü-vari reegel.** Uue pinna vari tuletatakse külgmenüü kahekihilisest tindivärvi toonitud varjust, mitte Bootstrapi `.shadow` klassist. Varjud on toonitud tindiga (`26,26,46`), mitte puhta mustaga.

**Hover tõstab reegel.** Klikitav kaart tõuseb hoveril 4px üles (`translateY(-4px)`, 0.15s ease). Staatiline pind ei liigu.

## Shapes

Kujukeel on ühtlaselt ümar ja selgelt astmeline. Konteinerid (kaardid, külgmenüü, tulemuskaart, modaalid) on 1rem nurgaga (`rounded-4`). Väiksemad paneelid, dropdownid, tooltipid ja ülevaatuse kokkuvõtted on 0.75rem nurgaga. Menüülingid on 0.6rem nurgaga. Nupud ja väljad on kompaktsed 0.375rem nurgaga (`rounded-2`). Staatuse märgid on pillikujulised. Ikoonimärgid, logo sihtmärk ja tulemusring on täisringid.

Ainus mitteortogonaalne vorm on navbari vasak kaar: maskiga lõigatud kõver, mis tuleb lehe täpivõrgust navbari sisse.

### Named Rules
**Kaks astet allapoole reegel.** Sisemine element on alati väiksema raadiusega kui tema konteiner (1rem kaart → 0.75rem paneel → 0.375rem nupp). Sama raadiusega pesastatud kaste ei kasutata.

## Components

### Buttons
Sõbralikud ja kindlad: kompaktne kuju ja paks kiri.
- **Shape:** kergelt ümardatud (0.375rem). Tekst on `fw-bold` ja ikoon asub teksti kõrval (`d-flex gap-2`).
- **Primary:** kiltkivihall taust ja valge tekst. Hoveril tumedam (`slate-hover`), vajutusel `slate-ink`. Tumedas teemas on taust teraskiltkivi `dark-slate-primary`, hoveril veidi heledam.
- **Soft (btn-light):** sinakashall täide ja kiltkivihall tekst. Kasutatakse sekundaarsete tegevuste jaoks (Tühista, filtrid). Tumedas teemas `#363d43`.
- **Outline primary:** kiltkivihall piir ja tekst, hoveril täidetud.
- **Success:** türkiis. Kasutatakse ainult kinnitavate või positiivsete lõpptegevuste jaoks.
- **Outline danger:** kustutamine ja tagasilükkamine.
- **Focus:** Bootstrapi fookusring kiltkivihalli tooniga (25%).

### Cards / Containers
- **Corner Style:** 1rem (`rounded-4`)
- **Background:** pind (`surface`), tumedas `dark-surface`
- **Shadow Strategy:** klass `.app-surface` (külgmenüü pinna vari + 1px piir, vt Elevation). Hoveril `--app-surface-shadow-hover`.
- **Internal Padding:** 1rem (`card-body`). Tühjad olekud on 3rem (`p-5`), keskel ikoon (40px), pealkiri ja sekundaarne tekst.
- **PreviewCard:** pealkiri on kahe rea järel kärbitud. Paremal ülal on staatuse märk või kolme täpi menüü, all tegevusnupud. Klikitaval kaardil on 4px hover-tõus.

### Inputs / Fields
- **Style:** Bootstrapi vaikimisi väli (1px piir, 0.375rem).
- **Focus:** piirjoon muutub `slate-haze` tooniks, ümber tekib 0.25rem kiltkivihall halo (25%). Tumedas teemas on piir `#5e9daf` ja halo teraskiltkivi 30%.
- **Checked:** kiltkivihall linnuke või raadionupp, mitte kunagi roheline (vt Roheline-ei-ole-õige reegel).

### Chips / Status badges
- **Style:** pillikujuline märk, mille taust, tekst ja piir tulevad õrnadest semantilistest toonidest. Aktiivne on türkiisi-udu, pooleli merevaigukollane ja mitteaktiivne punane (`badge-active`, `badge-in-progress`, `badge-inactive`).

### Navigation
- **Navbar:** valge riba, mille all on 1px piir. Vasakul on täpivõrguga roheka kaare mask ja sõnamärk koos sihtmärgi ikooniga (hoveril pöörab 90° ja suureneb 1.1 korda). Paremal on teemalüliti ja kontoikoon.
- **Sidebar:** kleepuv valge kaart (220px, 1rem raadius, pinna vari). Selle ülaservas on 3px kiltkivihall riba.
- **Nav link:** hall tekst (500), 0.6rem raadius. Hoveril 6% primary-täide ja tindivärvi tekst. Aktiivse lingi täide on 10% ja tekst primary-värvi. Alamleht (nt `/questions/ai`) hoiab vanemlingi aktiivsena.

### Tooltip
- Hele ja ümar (0.75rem) mull: pinna taust, poolläbipaistev piir, hõljuv vari ja 0.875rem tekst. Asukoha määrab komponent ise.

### Score Ring (signature)
Tulemusvaate keskne element: 10rem läbimõõduga ring, mis kattub 3rem ulatuses tulemuse päisega. Protsent animeeritakse 0-st tulemuseni (`@property --ring-percentage`). Edukal sooritusel on päises türkiisist mündini kulgev gradient ja ikoon "hüppab" sisse (`cubic-bezier(0.34, 1.56, 0.64, 1)`). Ebaõnnestumisel on päis rahulik hall gradient ja hüpet ei ole.

### Dot Wave (signature)
Ühine kiht (`.app-dot-wave-layer`, `theme.css`) lehe täpivõrgu kohal. Selles on samas 22px rütmis suuremad (2px raadius, tavalistel täppidel 1px) ja tumedamad täpid, mis on nähtavad ainult läbi kitsa diagonaalse maski. Laine läbib ekraani umbes 6 sekundiga, esimene kord 8 sekundit pärast lehe avamist ja edaspidi iga 15 sekundi järel. Sama laine jookseb ka navbari kaarel logo taga: mask on vaateakna mõõdus (300vw × 100vh), nii et mõlemad kihid joonistavad laine samas kohas ja alustavad samal hetkel. Testi alustamise ja lahendamise ajal lainet ei näidata. Kui kasutaja on vähendatud liikumise sisse lülitanud, laine välja lülitatakse.

## Do's and Don'ts

### Do:
- **Do** kasuta tegevuste ja valitud oleku jaoks kiltkivihalli (`slate-primary`) ning aaloe-rohelist ainult brändiaktsendiks.
- **Do** anna uutele kaartidele ja paneelidele klass `.app-surface` (külgmenüü vari koos 1px tindivärvi piiriga).
- **Do** määra iga `:root` ülekirjutus uuesti ka `[data-bs-theme='dark']` all ja kontrolli mõlemas teemas WCAG AA kontrasti.
- **Do** hoia täpivõrgud 22px sammus ja fikseeritud kinnitusega.
- **Do** kasuta lehe pealkirjaks suurtähtedes paksu `h1` elementi `h3` suurusega ja nuppudel `fw-bold`.
- **Do** lülita animatsioonid välja, kui kasutaja on vähendatud liikumise sisse lülitanud.

### Don't:
- **Don't** märgi testi lahendamisel valitud vastust rohelise ega türkiisiga.
- **Don't** kasuta uutel pindadel Bootstrapi `.shadow` klassi ega puhta musta varju.
- **Don't** kasuta Sora kirjatüüpi väljaspool sõnamärki.
- **Don't** toetu `main.css` failis olevale Vue toorikust päritud globaalsele `a` stiilile (roheline hsla, padding 3px ja hover-taust). Navigatsioonilinkidel tuleb see üle kirjutada.
- **Don't** kasuta Bootstrapi vaikimisi sinist. Dropdowni, nav-pills'i ja progressiriba värvid on `theme.css`-is eraldi üle kirjutatud ja uued Bootstrapi komponendid vajavad sama kohtlemist.
