# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Platvormil on kolm rolli:

- **ADMIN** ja **HALDUR** — koostavad ja haldavad kompetentse, küsimustepanka ja teste, määravad teste kasutajatele, vaatavad töölauda ja kasutajaid. Nemad kasutavad ka AI küsimuste genereerijat.
- **KASUTAJA** — näeb ainult talle määratud teste ("Minu testid"), lahendab neid ja vaatab oma tulemusi.

Tegemist on Vali-IT grupiprojektiga (õppeprojekt/demo), millel pole kindlat päris klienti. Eesmärk on töötav ja veenev demo ning portfooliotöö; tegelikud kasutajad on demo ajal projekti meeskond, juhendajad ja hindajad.

## Product Purpose

Kompetentsitestide platvorm: haldur kirjeldab kompetentsi ja selle tasemed, koostab küsimustepangast testi, määrab selle kasutajatele ning kasutaja lahendab testi ja saab tulemuse. Edu tähendab, et kogu ahel — kompetentsist ja küsimusest kuni lahendatud testi tulemuseni — töötab demos sujuvalt ja arusaadavalt.

## Positioning

Kaks asja, mida tulevane töö peab hoidma ja esile tooma:

1. **Kompetents + tase on kõige alus.** Küsimused ja testid on seotud konkreetse kompetentsi ja tasemega (nt JavaScript / Juunior, Vue.js / Seenior); kompetentsil on kirjeldus ja kompetentsikirjelduse PDF (nt `docs/competences/java-competence.pdf`).
2. **AI küsimuste genereerimine inimese kontrolli all.** AI pakub kompetentsi ja taseme põhjal küsimusi; haldur vaatab need üle, hindab ja kinnitab enne, kui need küsimustepanka jõuavad. AI on abiline, mitte otsustaja.

## Operating Context

- Testide koostamine, haldamine ja lahendamine toimub peamiselt arvutis (desktop). Mobiil on teisejärguline.
- Peamised vaated: Töölaud, Testid, Loo uus test, Minu testid, testi alustamine/lahendamine/tulemus, Kompetentsid, Küsimuste pank (sh AI küsimuste alamvaade), Kasutajad.
- Küsimuste tüübid: üks õige vastus (`SINGLE_CHOICE`), mitu õiget vastust (`MULTIPLE_CHOICE`), õige/vale (`TRUE_FALSE`).
- Vaadete kavandid on Balsamiqi mockupides; nende märkmed asuvad `docs/balsamic/notes/`.

## Capabilities and Constraints

- Stack: Vue 3 + Vite SPA (`frontend/`), Bootstrap 5, Phosphor ikoonid, Pinia; Spring Boot 4 / Java 21 REST API (`backend/`); PostgreSQL.
- Rollipõhine ligipääs: STAFF-vaated (ADMIN, HALDUR) vs. kõigile sisselogitutele avatud vaated (`frontend/src/router`).
- AI küsimuste genereerimine (`POST /api/ai-questions/generate`, kuni 5 küsimust päringu kohta) kasutab Google'i mudelit; saadetakse kompetentside nimed/kirjeldused ja küsimuste pealkirjad, isikuandmeid mitte.
- Kasutajaliides on **ainult eestikeelne**; mitmekeelsust pole plaanis. Terminid: Töölaud, Kompetents, Tase (Algaja, Juunior, Mid, Seenior), Küsimuste pank, Minu testid.
- Toetatud peavad olema nii **hele kui tume teema** (teemavahetus: `ThemeToggle.vue`, `ThemeService.js`).
- Teadaolevad avatud turvaküsimused on kirjas `TODO.md`-s; need on backendi teema, mitte disainiotsused.

## Brand Commitments

- Toote nimi koodibaasis: "testi-platvorm". Olemasolevad logofailid: `frontend/src/assets/logo.svg`, `frontend/src/assets/logo.png`.
- Muid siduvaid brändikohustusi pole kinnitatud.

## Evidence on Hand

- Näidisandmed (kompetentsid, tasemed, küsimused, testid, kasutajad): `docs/database/3_import.sql`.
- Kompetentsikirjelduste PDF-id: `docs/competences/`.
- Kliente, kasutajate tagasisidet, statistikat ega arvustusi pole — neid ei tohi välja mõelda.

## Product Principles

1. **Kompetents ja tase on alati nähtaval.** Iga küsimus, test ja tulemus peab olema loetav oma kompetentsi ja taseme kontekstis.
2. **AI ettepanek, inimene otsustab.** AI genereeritud sisu on selgelt eristatav ja läbib halduri ülevaatuse enne kasutusse jõudmist.
3. **Kaks selget maailma.** Halduri tööriistad (koostamine, haldamine) ja kasutaja testilahendus on eraldi kogemused; kasutaja näeb ainult seda, mis talle on määratud.
4. **Demo peab veenma.** Iga põhivoog peab töötama otsast lõpuni ja olema esitlusel arusaadav ka esmakordsele vaatajale.

## Accessibility & Inclusion

- Nõutav tase: **WCAG 2.1 AA** (kontrastsus, klaviatuuriga kasutatavus, fookuse nähtavus, semantika) nii heledas kui tumedas teemas.
