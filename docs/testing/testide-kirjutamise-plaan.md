# Testide kirjutamise plaan (backend)

See fail on jätkamiseks Claude Code'i sessioonis (nt IntelliJ-s). Loe see läbi enne testidega töö jätkamist.

## Eesmärk

1. Õppida backendi teste kirjutama (JUnit 5 + Mockito + AssertJ). Kõik vajalik on juba olemas `spring-boot-starter-test` kaudu, uusi sõltuvusi ei ole vaja.
2. Katta testidega minu (Tiina) enda kirjutatud kood: testi loomine (`TestService`) ja küsimuste pank (`QuestionService`, hiljem AI küsimuste pank).

**Seis:** sammud 1–7 on tehtud, kokku 92 testi — `QuestionServiceTest` (18), `TestServiceTest` (8), `ResultServiceTest` (12), `TestAttemptServiceTest` (16), `TestControllerTest` (3), `AiQuestionServiceTest` (15), `AiQuestionBankServiceTest` (8) ja `UserServiceTest` (12). Täpne nimekiri, mida testid katavad, on testide nimedes (`meetod_olukord_tulemus`); siin on kirjas ainult see, mida nimedest välja ei loe.

## Kuidas Claude peab aitama (tähtis!)

See on õppimisprojekt. Eesmärk ei ole, et AI kirjutaks kõik testid ise.

- **Sammud 1 ja 2 kirjutan mina käsitsi.** Claude võib luua tühja testiklassi koos seadistusega (mockid, importid) ja selgitada iga osa, aga testjuhtumite koodi ja assertid kirjutan ise. Kui jään kinni, selgita viga, ära paranda seda minu eest.
- **Alates sammust 3:** mina panen kirja testjuhtumid, Claude kirjutab koodi, mina vaatan iga rea üle.
- **Testid kirjutatakse oodatud käitumise põhjal (taski fail / see plaan), mitte implementatsiooni lugedes.** Kui juhtum on ebaselge, küsi, ära arva.
- **Iga test peab suutma ebaõnnestuda.** Pärast testi kirjutamist murra korraks koodi (nt `>=` → `>`) ja veendu, et test läheb punaseks.
- Järgi `backend/CLAUDE.md` nimetamise konventsioone ka testides (nt `QuestionCreateRequestDto questionCreateRequestDto`, mitte `dto`).

## Testide tüübid

| Tüüp | Mida kontrollib | Kui palju |
|---|---|---|
| Unit test (JUnit + Mockito, `@ExtendWith(MockitoExtension.class)`) | Ühe service'i loogika, repositooriumid on mockitud | Enamik teste |
| Kontrolleri test (`@WebMvcTest`) | URL, JSON, HTTP staatused, `RestExceptionHandler` | Mõni kontrolleri kohta |
| Integratsioonitest (`@SpringBootTest` + päris andmebaas) | Kogu voog koos JPQL päringutega | Ainult mõni põhivoo jaoks |

Testid asuvad kaustas `backend/src/test/java/ee/testiplatvorm/...` samas paketis kui testitav klass, nt `service/QuestionServiceTest.java`.

**Nimekonflikt:** entiteet `ee.testiplatvorm.persistence.test.Test` ja JUniti annotatsioon `@Test` on sama nimega. Testifailis tuleb ühte neist kasutada täisnimega.

Käivitamine: IntelliJ-s roheline ▶ testi kõrval või `./gradlew test --tests "ee.testiplatvorm.service.QuestionServiceTest"`.

## Järjekord

### ✅ Samm 1: `QuestionService.createQuestion` — õigete vastuste reeglid (käsitsi)

- `SINGLE_CHOICE`: täpselt 1 õige → OK; 0 või 2 õiget → `BadRequestException` (`INVALID_CORRECT_ANSWER_COUNT`)
- `MULTIPLE_CHOICE`: 1+ õiget → OK; 0 õiget → `BadRequestException`
- `TRUE_FALSE`: 2 vastust ja 1 õige → OK; 3 vastust → `BadRequestException`
- Kasutaja, kelle roll ei ole `ADMIN` ega `HALDUR` → `ForbiddenException`
- Edukal juhul: küsimus salvestatakse staatusega aktiivne, vastused salvestatakse (`verify(...)`)

Märkused:
- Kaks esimest testi kirjutasin ise, ülejäänud 5 kirjutas Claude sama mustri järgi.
- Ei ole testitud: `TRUE_FALSE` 2 vastuse ja 1 õigega → OK (õnnelik juhtum); küsimuse tüübi või kompetentsitaseme puudumine (`PrimaryKeyNotFoundException`).
- Vigaseid juhtumeid ei testita päris vastuste salvestamise sisu järgi — `questionAnswerMapper` on mock, mis tagastab vaikimisi tühja nimekirja.

### ✅ Samm 2: `TestService.createTest` (käsitsi, rohkem mockimist)

- Admin loob testi → test salvestatakse aktiivsena, küsimused positsioonidega 1, 2, 3…
- Mitte-admin → `ForbiddenException`
- Küsimuse ID-d ei eksisteeri → `PrimaryKeyNotFoundException`

Märkused:
- Kirjutas Claude, mina vaatasin üle. Positsioonide kontrolliks kasutatakse `ArgumentCaptor`-it.
- `createTest` salvestab testi **enne** küsimuste kontrolli. Kui küsimust ei leita, tühistab päris rakenduses `@Transactional` testi salvestuse, aga unit testis seda näha ei ole — seepärast kontrollib test ainult, et `TestQuestion`-it ei salvestatud. Selle kontrollimiseks oleks vaja integratsioonitesti.
- Kasutaja ID tuleb nüüd sessioonist (`currentUserService.getUserId()`), mitte päringust (vt leitud viga nr 1).

### ✅ Samm 3: `TestService.findTestDetail` ja ülejäänud `QuestionService` (Claude pakkus juhtumid, mina vaatasin üle)

- `findTestDetail`: küsimuste arv ja maksimumpunktid; looja nimi profiilist või profiili puudumisel e-post; ADMIN ja HALDUR näevad, tavakasutaja → `ForbiddenException`; testi pole → `PrimaryKeyNotFoundException`
- `updateQuestion` / `deleteQuestion`: õnnelik juhtum, küsimust pole, tavakasutaja → `ForbiddenException`. Kustutamine ei kustuta päriselt, vaid muudab staatuse `"I"`-ks.
- `findAllQuestionsBy`: iga küsimus saab oma aktiivsed vastused; küsimusi pole → tühi nimekiri; tavakasutaja → `ForbiddenException`
- `findQuestionsBy`: ainult aktiivsed küsimused; tavakasutaja → `ForbiddenException`

Märkused:
- Tehtud TDD-ga: õiguste testid kirjutati enne ja olid punased, siis lisati `QuestionService`-i `validateUserCanManageQuestions()` (vt leitud viga nr 2).
- Reegel: küsimusi ja teste näevad, muudavad ja kustutavad ainult `ADMIN` ja `HALDUR`. Tavakasutaja näeb ainult oma gruppi ja talle määratud testide infot.
- Ei ole testitud: `TestService.findAllTests` ja `findTestStartInfo`.
- `QuestionController`-i Swaggeri kirjeldustes puudub uute kontrollide `403` vastus.

### ✅ Samm 4: `ResultService.calculateUserResultPercentage` ja `TestAttemptService.submitTest` (Claude pakkus juhtumid, mina vaatasin üle)

- 7/10 → `70.00`; 2/3 ilma ümardamiseta → `66.67`; 2/3 ümardamisega → `67`; 1/8 ümardamisega → `13` (12.5 ümardatakse üles); `maxScore = 0` → `100`
- Kõik õiged → `STATUS_PASSED`; alla läbimisprotsendi → `STATUS_FAILED`; täpselt piiril → `STATUS_PASSED`; 66.67% ja piir 67% → ümardamisega läbib, ilma ei läbi
- Mitme õige vastusega küsimusel ainult osa õigeid või õiged + üks vale → 0 punkti; õiged teises järjekorras → täispunktid
- Vastamata küsimus (tühi valik või päringust puudu) ei loe `questionsAnswered` hulka; vastus küsimusele, mida testis pole, jäetakse arvestamata
- Pärast esitamist on kasutaja testimäärang suletud (`"C"`); avatud määramiseta → `ForbiddenException`

Märkused:
- `ResultServiceTest` ei kasuta mock'e üldse — protsendi arvutus on puhas funktsioon.
- `TestAttemptServiceTest` kasutab `ResultService`-i jaoks `@Spy`-d (päris objekt, mitte mock), et läbimise kontroll kasutaks päris ümardamise loogikat.
- `maxScore = 0` olukorda päriselt tekkida ei saa (vt leitud viga nr 3); test ainult dokumenteerib, et nulliga ei jagata.
- Ei ole testitud: `getTestAttempt` ja `ResultService.findTestResult`.

### ✅ Samm 5: kontrolleri test (`@WebMvcTest`) — `TestControllerTest` (Claude kirjutas, mina vaatasin üle)

- `POST /api/tests` korrektse päringuga → `200`, teenus kutsutakse välja
- `POST /api/tests` tühja `questions` nimekirjaga → `400` (`@NotEmpty`), `errorCode: INCORRECT_INPUT`, teenust ei kutsuta
- `POST /api/tests` ilma `roundScoreUp` väljata → `400` (`@NotNull`), teenust ei kutsuta
- Need reeglid kehtivad ainult kontrolleri tasemel (`@Valid`), teenuse unit test neid ei näe.

Märkused:
- Spring Boot 4-s: `@WebMvcTest` on paketis `org.springframework.boot.webmvc.test.autoconfigure`, teenuse mock'imiseks `@MockitoBean` (vana `@MockBean` asemel).
- Ei ole testitud: teiste kontrollerite valideerimine ja `403` vastused (nt `QuestionController`).

### ✅ Samm 6: AI küsimuste pank (`AiQuestionService`, `AiQuestionBankService`) — Gemini klient on mockitud (Claude kirjutas, mina vaatasin üle)

- `AiQuestionService`: tavakasutaja ei saa genereerida ega salvestada (ja AI-d ei kutsuta); AI täpsustav küsimus; olematu tase → vaikimisi täpsustus; kokku max 5 küsimust; AI liigsed küsimused lõigatakse ära; vigased küsimused (vihje vastuses, vale õigete arv, üle 5 vastuse, üle 12 rea kirjeldust, `TRUE_FALSE` mitte 2 vastusega) jäetakse välja; kõik vigased → viga; salvestamine staatusega `"P"`
- `AiQuestionBankService`: kinnitamisel kopeeritakse küsimus koos vastustega küsimuste panka (punktid `TRUE_FALSE` 5, muud 10); tagasilükkamisel staatus `"R"` ja ei kopeerita; juba üle vaadatud → `BadRequestException`; tavakasutaja → `ForbiddenException`

Märkused:
- Tehtud TDD-ga: õiguste ja `TRUE_FALSE` testid olid enne parandust punased.
- Reegel: AI chatti, AI küsimuste panka ja küsimuste loomist kasutavad ainult `ADMIN` ja `HALDUR`. `AiQuestionBankService`-is oli rolli kontroll puudu ja frontendi routeris puudus `questions/ai` lehel `meta: STAFF` — mõlemad parandatud.
- `TRUE_FALSE` peab olema täpselt 2 vastusega. Vastuste tekste ("Tõene" / "Väär") ei kontrollita, ainult arvu.
- Gemini `ChatClient` on mock `RETURNS_DEEP_STUBS`-iga, et saaks stub'ida terve ahela `prompt().system().user().call().responseEntity(...).getEntity()`. Teenus luuakse `@BeforeEach`-is käsitsi, sest konstruktor kutsub `builder.build()`.
- Ei ole testitud: eelnevate sõnumite (`previousMessages`) liitmine AI päringusse, vastuste segamine (`Collections.shuffle`), pikkade tekstide lühendamine.

### ✅ Samm 7: kasutajate haldus (`UserService`) — õiguste reeglid (Claude kirjutas, mina vaatan üle)

- `deleteUser`: admin ei saa iseennast kustutada; HALDUR ei saa kustutada; kustutamine muudab staatuse `"I"`-ks
- `updateUser`: admin ei saa muuta enda rolli; HALDUR ei saa muuta adminit; HALDUR-i puhul jäetakse roll ja võõra kasutaja nimi vahele, aga oma hallatava grupi saab lisada; HALDUR ei saa eemaldada gruppi, mida ta ei halda
- `addUser`: aktiivse kasutaja e-post → `ForbiddenException`; HALDUR ei saa lisada ADMIN rolliga kasutajat ega lisada kasutajat gruppi, mida ta ei halda; kustutatud kasutaja e-postiga lisamine taastab kasutaja (`restored: true`, vanad grupid asendatakse uutega)
- `activateUser`: profiilita (registreerimata) kasutaja läheb ootele (`"P"`), mitte aktiivseks

Märkused:
- Iga test on kontrollitud koodi murdmisega (12 mutatsiooni, kõik läksid punaseks).
- Reegel „iga kasutaja peab kuuluma vähemalt ühte gruppi“ kehtib ainult DTO tasemel (`@NotEmpty groupIds`), teenuse unit test seda ei näe. Selleks oleks vaja kontrolleri testi nagu sammus 5.
- Ei ole testitud: `findAllUsers`, HALDUR-i enda nime muutmine, profiiliga kasutaja aktiveerimine (`"A"`), `GroupService.findManagedGroups`.

Frontendi testid (Vitest) on eraldi teema, nendega hiljem.

## Koodi lugedes leitud võimalikud vead (meeskonnaga üle arutada)

Need sobivad hästi testjuhtumiteks: kirjuta test oodatud käitumise jaoks ja vaata, kas see läbib.

1. ✅ **Parandatud.** **`TestService.createTest` võtab kasutaja ID päringust** (`testCreateRequestDto.getUserId()`), mitte `currentUserService.getUserId()` kaudu nagu `createQuestion`. Igaüks saaks saata admini ID ja luua testi tema nimel — tõenäoliselt turvaviga.
2. ✅ **Parandatud** (ka `findQuestionsBy` ja `findAllQuestionsBy`). **`QuestionService.updateQuestion` ja `deleteQuestion` ei kontrolli rolli.** Küsimuse loomiseks on vaja `ADMIN`/`HALDUR` rolli, muutmiseks ja kustutamiseks mitte.
3. ✅ **Pole viga.** ~~`createTest` lubab tühja küsimuste nimekirja.~~ `TestCreateRequestDto.questions` on `@NotEmpty` ja kontrolleril on `@Valid` → tühi nimekiri annab `400`. Teste luuakse ainult `createTest` kaudu ja `TestQuestion`-it ei kustutata kuskil, seega igal testil on alati vähemalt 1 küsimus. Kontrolleri test sammus 5.
4. **Kui vastusel puudub `isCorrect`**, tagastab `getIsCorrect()` `null` ja `createQuestion` kukub `NullPointerException`-iga selge veateate asemel.
5. ✅ **Pole viga.** ~~`ResultService`: kui `test.getRoundScoreUp()` on `null`, tekib `NullPointerException`.~~ `roundScoreUp` ei saa olla `null`: frontend vaikimisi `true`, DTO `@NotNull` + `@Valid`, entiteet `@NotNull`, andmebaas `NOT NULL`. `false` (ümardamiseta) on lubatud.
6. **`TestAttemptService.submitTest` paneb `startedAt` võrdseks esitamise hetkega** (`startedAt == completedAt`), seega iga testi täitmise aeg on 0 sekundit. Ajaga testide (`isTimed`) puhul ei saa kontrollida, kas kasutaja jäi aja sisse. Õige algusaeg tuleks salvestada siis, kui kasutaja testi avab (`getTestAttempt`).
7. **`Status` enum'is on kahel staatusel sama kood:** `STATUS_PASSED("P")` ja `STATUS_PENDING("P")`. Praegu viga ei teki, sest neid kasutatakse eri tabelites: `STATUS_PASSED` ainult `result` tabelis, `STATUS_PENDING` ainult `ai_question` / `ai_question_answer` tabelites. Risk on tulevikus: kui ühes tabelis on vaja mõlemat, ei saa neid eristada. Arutada tiimiga (nt `STATUS_PENDING("W")`).
8. ✅ **Parandatud.** **`AiQuestionBankService` ei kontrollinud rolli** — iga tavakasutaja sai AI küsimusi vaadata ja kinnitada (kinnitatud küsimus läheb päris küsimuste panka). Ka frontendis puudus `questions/ai` lehel `meta: STAFF`.
9. ✅ **Parandatud.** **AI `TRUE_FALSE` küsimus võis olla 3+ vastusega** — `AiQuestionService.isValidQuestion` kontrollis ainult õigete vastuste arvu, mitte vastuste koguarvu.
10. ✅ **Parandatud (TDD).** **`ResultService.findTestResult` näitas igale kasutajale iga tulemust** — URL-is `userTestId` muutes sai vaadata võõraid tulemusi. Nüüd näeb tavakasutaja ainult enda tulemust, `ADMIN` ja `HALDUR` kõiki. Võõra tulemuse korral tuleb sama viga mis puuduva korral (`NO_RESULT_FOUND`), et ei saaks ära arvata, kas see on olemas.

## Edasi

- Iga uus feature või muudatus tuleb koos testidega samas branchis/PR-is.
- Vea leidmisel: kõigepealt test, mis vea reprodutseerib (punane), siis parandus (roheline).
- Hiljem: GitHub Actions workflow, mis käivitab testid igal PR-il.
