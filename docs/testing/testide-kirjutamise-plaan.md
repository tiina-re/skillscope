# Testide kirjutamise plaan (backend)

See fail on jätkamiseks Claude Code'i sessioonis (nt IntelliJ-s). Loe see läbi enne testidega töö jätkamist.

## Eesmärk

1. Õppida backendi teste kirjutama (JUnit 5 + Mockito + AssertJ). Kõik vajalik on juba olemas `spring-boot-starter-test` kaudu, uusi sõltuvusi ei ole vaja.
2. Katta testidega minu (Tiina) enda kirjutatud kood: testi loomine (`TestService`) ja küsimuste pank (`QuestionService`, hiljem AI küsimuste pank).

**Seis:** sammud 1–3 on tehtud — `QuestionServiceTest` (18 testi) ja `TestServiceTest` (8 testi). Täpne nimekiri, mida testid katavad, on testide nimedes (`meetod_olukord_tulemus`); siin on kirjas ainult see, mida nimedest välja ei loe.

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

### Samm 4: `ResultService.calculateUserResultPercentage` ja `TestAttemptService.submitTest`

- 7/10 → `70.00`; 2/3 ilma ümardamiseta → `66.67`; 2/3 ümardamisega → `67`; `maxScore = 0` → `100`
- Kõik õiged → `STATUS_PASSED`; alla läbimisprotsendi → `STATUS_FAILED`
- Mitme õige vastusega küsimusel ainult osa õigeid valitud → 0 punkti
- Vastamata küsimus ei loe `questionsAnswered` hulka

### Samm 5: üks kontrolleri test käsitsi (`@WebMvcTest`), ülejäänud AI abiga

### Samm 6: AI küsimuste pank (`AiQuestionService`, `AiQuestionBankService`) — Gemini klient tuleb mockida

Frontendi testid (Vitest) on eraldi teema, nendega hiljem.

## Koodi lugedes leitud võimalikud vead (meeskonnaga üle arutada)

Need sobivad hästi testjuhtumiteks: kirjuta test oodatud käitumise jaoks ja vaata, kas see läbib.

1. ✅ **Parandatud.** **`TestService.createTest` võtab kasutaja ID päringust** (`testCreateRequestDto.getUserId()`), mitte `currentUserService.getUserId()` kaudu nagu `createQuestion`. Igaüks saaks saata admini ID ja luua testi tema nimel — tõenäoliselt turvaviga.
2. ✅ **Parandatud** (ka `findQuestionsBy` ja `findAllQuestionsBy`). **`QuestionService.updateQuestion` ja `deleteQuestion` ei kontrolli rolli.** Küsimuse loomiseks on vaja `ADMIN`/`HALDUR` rolli, muutmiseks ja kustutamiseks mitte.
3. **`createTest` lubab tühja küsimuste nimekirja.** Koos `ResultService` reegliga `maxScore = 0 → 100%` läbiks tühja testi igaüks automaatselt.
4. **Kui vastusel puudub `isCorrect`**, tagastab `getIsCorrect()` `null` ja `createQuestion` kukub `NullPointerException`-iga selge veateate asemel.
5. **`ResultService`: kui `test.getRoundScoreUp()` on `null`**, tekib `NullPointerException`.

## Edasi

- Iga uus feature või muudatus tuleb koos testidega samas branchis/PR-is.
- Vea leidmisel: kõigepealt test, mis vea reprodutseerib (punane), siis parandus (roheline).
- Hiljem: GitHub Actions workflow, mis käivitab testid igal PR-il.
