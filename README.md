Testide loomise ja läbimise platvorm: Spring Boot backend + Vue 3 frontend.

## Projekti taust

Projekt sai alguse Vali-IT kursuse ühisprojektina, mille tegime tiimiga koos
([magicflute21/testi-platvorm](https://github.com/magicflute21/testi-platvorm)).
See repo on minu isiklik jätk, kus arendan projekti edasi.

### Minu panus tiimiprojektis
- Testi loomise vaade ja teenus (`TestService.createTest`)
- Töölaud
- Küsimuste pank (`QuestionService`)
- Küsimuste genereerimise AI chat.

### Pärast tiimiprojekti lisatud
- Unit testid (JUnit 5 + Mockito + AssertJ) teenustele `QuestionService` ja `TestService`

## Struktuur

```
backend/    Spring Boot 4.x / Java 21 REST API
frontend/   Vue 3 + Vite SPA
docs/       Dokumentatsioon ja andmebaasiskriptid
```

Backend ja frontend on eraldi arendatavad ja käivitatavad rakendused. Ehitus-, käivitus- ja testikäskude ning arhitektuuri kohta vaata `backend/CLAUDE.md` ja `frontend/CLAUDE.md`.
