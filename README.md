# SkillScope

Testide loomise ja läbimise platvorm kompetentside hindamiseks: Spring Boot backend + Vue 3 frontend + PostgreSQL andmebaas.

## Projekti taust

Projekt sai alguse Vali-IT kursuse ühisprojektina, mille tegime tiimiga koos ([magicflute21/testi-platvorm](https://github.com/magicflute21/testi-platvorm)).
See repo on minu isiklik jätk, kus arendan projekti edasi. Tiimi hilisemad muudatused on siia samuti üle toodud.

### Minu panus tiimiprojektis
- Testi loomise vaade ja teenus (`TestService.createTest`)
- Testi eelvaade (`/tests/:testId`)
- Töölaud (`Dashboard`)
- Küsimuste pank (`QuestionService`, `GET /api/question-bank`) – küsimuste muutmine ja kustutamine
- AI chat testiküsimuste genereerimiseks ning AI küsimuste ülevaatus hinnanguga

### Pärast tiimiprojekti lisatud

**Funktsionaalsus:**
- Kasutajate haldus – muutmine, kustutamine ja taasaktiveerimine + otsing
- Kompetentside vaate sisu (`GET /api/competences/{id}`)
- Ühtne filter kõigis vaadetes
- Andmebaasi skeem töölaual (admin vaates)
- Külgmenüü avaneb mobiilivaates menüünupust
- Vaadete stiil ühtlustatud heledas ja tumedas teemas

**Testid** (JUnit 5 + Mockito + AssertJ), kirjutatud ärireeglite põhjal, osa TDD-ga:
- teenused: `QuestionService`, `TestService`, `TestAttemptService`, `ResultService`, `AiQuestionService`, `AiQuestionBankService`, `UserService`
- kontroller: `TestController` (`@WebMvcTest` + `MockMvc`)
- plaan ja seis: [`docs/testing/testide-kirjutamise-plaan.md`](docs/testing/testide-kirjutamise-plaan.md)

**Turvaparandused**:
- küsimusi ja teste saavad vaadata, muuta ja kustutada ainult ADMIN ja HALDUR
- AI küsimuste chat on ainult ADMIN-ile ja HALDUR-ile (nii backendis kui frontendi router'is)
- testi looja ID võetakse sessioonist, mitte päringust
- kasutajate haldus: rolli muudab ja kustutab ainult ADMIN, ADMIN ei saa iseennast kustutada ega enda rolli muuta

## Tehnoloogiad
| Backend | Java 21, Spring Boot 4, Spring Data JPA, MapStruct, Spring AI (Gemini) |
| Frontend | Vue 3, Vite, Vue Router, Bootstrap 5 |
| Andmebaas | PostgreSQL (Docker), skeem `testi_platvorm` |
| Testid | JUnit 5, Mockito, AssertJ |

## Arendusprotsess
Esialgse Vali-IT õppeperioodi ajal arendasin projekti pigem ise koodi kirjutades, AI küsimuste genereerimise võimekuse lisamisel ja edaspidi kasutasin juba rohkem AI-assistenti [Claude Code](https://claude.com/claude-code).
Minu roll oli nõuete ja ärireeglite paikapanek, lahenduste valik ning iga muudatuse ülevaatus ja testimine.

- **Plaan enne koodi** – suuremate muudatuste puhul analüüsisime enne läbi variandid ja valisin sobiva
- **Testid ärireeglite põhjal** – testjuhtumid tulevad reeglitest, mitte koodist; esimesed testid kirjutasin
  käsitsi, hiljem panin kirja juhtumid ja vaatasin AI kirjutatud testid üle. Iga test on kontrollitud
  koodi murdmisega (test peab minema punaseks)
- **Projekti reeglid AI-le** – koodikonventsioonid ja töökäsud on kirjas failides:
 - [`CLAUDE.md`](CLAUDE.md) – üldine ülevaade, keel ja keskkond
 - [`backend/CLAUDE.md`](backend/CLAUDE.md) – arhitektuur, nimetamise reeglid, õiguste kontroll ja testide kirjutamise reeglid
 - [`frontend/CLAUDE.md`](frontend/CLAUDE.md) – käsud, arhitektuur ja koodistiil
 - [`docs/testing/testide-kirjutamise-plaan.md`](docs/testing/testide-kirjutamise-plaan.md)
 - [`.claude/skills/`](.claude/skills) – korduvate tööde juhised (skillid), nt taski ja implementatsiooniplaani loomine; osa tuli kursuse toorikuga

## Struktuur
- backend/              Spring Boot REST API
- frontend/             Vue 3 + Vite SPA
- docs/database/        PostgreSQL skeem ja algandmed (1_reset, 2_create, 3_import)
- docs/                 Muu dokumentatsioon (testide plaan, taskid, mockupid)
- docker-compose.yml    PostgreSQL koos skeemi ja algandmetega

## Käivitamine
```bash
docker compose up -d                 # andmebaas (esimesel korral käivitab docs/database skriptid)
cd backend && ./gradlew bootRun      # backend: http://localhost:8080, Swagger: /swagger-ui.html
cd frontend && npm install && npm run dev   # frontend: http://localhost:8081
cd backend && ./gradlew test         # testid
