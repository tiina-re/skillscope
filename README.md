Testide loomise ja läbimise platvorm: Spring Boot backend + Vue 3 frontend.

## Projekti taust

Projekt sai alguse Vali-IT kursuse ühisprojektina, mille tegime tiimiga koos
([magicflute21/testi-platvorm](https://github.com/magicflute21/testi-platvorm)).
See repo on minu isiklik jätk, kus arendan projekti edasi.

### Minu panus tiimiprojektis
- Testi loomise vaade ja teenus (`TestService.createTest`)
- Töölaud (`Dashboard`)
- Küsimuste pank (`QuestionService`)
- Küsimuste genereerimise AI chat.

### Pärast tiimiprojekti lisatud

**Testid** – 80 unit testi (JUnit 5 + Mockito + AssertJ), kirjutatud ärireeglite põhjal, osa TDD-ga:
- teenused: `QuestionService`, `TestService`, `TestAttemptService`, `ResultService`, `AiQuestionService`, `AiQuestionBankService`
- kontroller: `TestController` (`@WebMvcTest` + `MockMvc`)
- plaan ja seis: [`docs/testing/testide-kirjutamise-plaan.md`](docs/testing/testide-kirjutamise-plaan.md)

**Turvaparandused** (testide kirjutamisel leitud vead):
- küsimusi ja teste saavad vaadata, muuta ja kustutada ainult ADMIN ja HALDUR
- AI küsimuste chat on ainult ADMIN-ile ja HALDUR-ile (nii backendis kui frontendi router'is)
- testi looja ID võetakse sessioonist, mitte päringust
- tavakasutaja näeb ainult enda testi tulemust

**Valideerimine:**
- tõene/väär küsimusel peab olema täpselt 2 vastusevarianti

## Struktuur

```
backend/    Spring Boot 4.x / Java 21 REST API
frontend/   Vue 3 + Vite SPA
docs/       Dokumentatsioon ja andmebaasiskriptid
```

Backend ja frontend on eraldi arendatavad ja käivitatavad rakendused. Ehitus-, käivitus- ja testikäskude ning arhitektuuri kohta vaata `backend/CLAUDE.md` ja `frontend/CLAUDE.md`.
