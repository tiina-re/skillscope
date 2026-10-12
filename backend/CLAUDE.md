# CLAUDE.md

See fail annab juhiseid Claude Code'ile (claude.ai/code) selles repositooriumis töötamiseks.

**Keel:** Kõik uued kanded sellesse faili kirjutatakse eesti keeles.

## Käsud

```bash
# Ehitamine
./gradlew build

# Käivitamine (nõuab lokaalset PostgreSQL-i)
./gradlew bootRun

# Testide käivitamine
./gradlew test

# Ühe testiklassi käivitamine
./gradlew test --tests "ee.testiplatvorm.service.QuestionServiceTest"

# Ainult kompileerimine (käivitab ka MapStructi annotatsiooni töötluse)
./gradlew compileJava
```

## Andmebaasi seadistamine

PostgreSQL peab töötama `localhost`-is järgmiste seadetega:
- Andmebaas: `vali_it`
- Kasutajanimi: `postgres`
- Parool: `student123`
- Mugav DB url: `jdbc:postgresql://localhost:5432/vali_it`

Käivita skriptid järjekorras kaustast `docs/database`:
1. `1_reset_database.sql` — kustutab ja loob uuesti `testi_platvorm` skeema
2. `2_create.sql` — loob kõik tabelid
3. `3_import.sql` — lisab algandmed

Kõik tabelid asuvad `testi_platvorm` skeemas: `2_create.sql` ja `3_import.sql` seavad `search_path`-i sellele skeemile, entity-d kasutavad `@Table(schema = "testi_platvorm")` ja datasource URL-is on `?currentSchema=testi_platvorm` (vajalik nt `NlToSqlService` toores SQL-i jaoks).

**Dockeriga:** repo juurkaustas on `docker-compose.yml`, mis loob ülaltoodud seadetega PostgreSQL-i ja käivitab `docs/database` skriptid esimesel käivitusel automaatselt. Käivita repo juurest `docker compose up -d`; andmebaasi nullist loomiseks `docker compose down -v && docker compose up -d`.

## Arhitektuur

Tegemist on Spring Boot 4.x / Java 21 REST backendiga. Frontend on eraldi Vue 3 SPA (ei ole selles repos).

### Kihtide struktuur

```
controller/       REST endpointid + päringu/vastuse DTOd
service/          Äriloogika
persistence/      JPA entiteedid, repositooriumid, MapStructi mapperid
infrastructure/   Läbivad komponendid: erindi tüübid, veakoodid, globaalne erindite käsitleja
```

Igal domeenialal on oma alampakk `controller/`-is koos DTOdega, teenusklass ja persistence pakk.

### Olulised konventsioonid

**DTOd vs entiteedid** — Kontrollerid näevad ainult DTOsid. MapStructi mapperid (liidesed annotatsiooniga `@Mapper`) teisendavad DTOd JPA entiteetideks ja vastupidi. Genereeritud mapperi implementatsioonid tekivad kausta `src/main/generated/`.

**Veakäsitlus** — Teenustest visatakse kohandatud erindeid (`DataNotFoundException`, `ForbiddenException`, `PrimaryKeyNotFoundException`), mille püüab kinni `RestExceptionHandler` (`@ControllerAdvice`). Kõik äriveateated ja numbrilised veakoodid on koondatud `ErrorResponse` enumi.

**Muutujate nimetamine** — Muutuja nimi peab peegeldama täistüüpi: `EntityDetailDto entityDetailDto`, mitte `EntityDetailDto dto`.

**Meetodi nimetamine** — `getX()` lubab kindlat tagastust. Kui meetod sisaldab tingimislikku loogikat ja muteerib DTO-d, kasuta `handle`-prefiksit ja anna DTO parameeter sisse: `handleAddImageData(EntityDetailDto entityDetailDto, Integer entityId)`.

**Entiteedi otsing ID järgi** — `repository.findById()` kasutamine `orElseThrow`-ga peab olema `public getValid<Entiteet>By(Integer <entiteet>Id)` meetodis vastava service klassi all (nt `getValidEntityBy(Integer entityId)` `EntityService`-s).

**SQL päringud** — Kohandatud päringud on JPQL, kirjutatud otse Spring Data repositooriumi liidesele `@Query` annotatsiooniga. Vajadusel kasutab repositoorium konstruktori avaldist otse DTOsse projekteerimiseks.

**Repositooriumi meetodi nimetamine** — Meetodi nimi peab mainima, mida ta tagastab (subjekti), mitte jääma geneeriliseks: `findFilteredEntitiesBy(...)` tagastab `List<Entity>`, `findUserBy(...)` tagastab `User`. Väldi kujundeid nagu `findFilteredBy(...)`, kust pole näha, mida meetod tagastab.

**Jagatud DTO-d** — DTO klass, mida kasutab rohkem kui üks ressursipakett (nt mitme kontrolleri/mapperi vahel jagatud), ei kuulu ühegi üksiku ressursi `dto/` paketti, vaid paketti `controller/common/dto/` (nt `SharedEntityDto`).

**SQL logimine** — P6Spy on seadistatud (`spy.properties`), nii et täielik parameetritega SQL kuvatakse konsoolis. Selle saab keelata, lülitades `application.properties`-is tagasi kommenteeritud tavaliste PostgreSQL seadetele.

**Kasutaja tuvastamine** — Sisseloginud kasutaja ID võetakse alati `currentUserService.getUserId()` kaudu, mitte kunagi päringu DTO-st ega URL-ist. Päringust tulevat `userId`-d ei tohi kasutada selle kohta otsuste tegemiseks, kes toimingut teeb.

**Õiguste kontroll** — Iga muutev või tundlik service meetod (loomine, muutmine, kustutamine, teiste kasutajate andmete vaatamine) kontrollib kasutaja rolli, sh `update`- ja `delete`-meetodid, mitte ainult `create`. Spring Securityt projektis ei ole — rolli kontrollitakse service'is ja keelatud juhul visatakse `ForbiddenException`.

**Sisendi valideerimine** — Päringu DTO-de kohustuslikud väljad märgitakse Jakarta Bean Validation annotatsioonidega (`@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size` jne) ja kontrolleri meetodi parameetril on `@Valid`. Service ei tohi eeldada, et väli on olemas, kui see pole valideeritud (nt `Boolean` väli võib olla `null`).

**Transaktsioonid** — Service meetod, mis muudab mitut tabelit või salvestab mitu seotud kirjet (nt küsimus koos vastustega, test koos küsimustega), peab olema `@Transactional`.

### Testid

Testide plaan, seis ja see, mida testid ei kata, on kirjas `docs/testing/testide-kirjutamise-plaan.md`.

- **Tööriistad** — JUnit 5 + Mockito + AssertJ (`spring-boot-starter-test` kaudu, uusi sõltuvusi pole vaja). Service'i unit test kasutab `@ExtendWith(MockitoExtension.class)`, kõik konstruktori sõltuvused on `@Mock` ja testitav klass `@InjectMocks`. Andmebaasi ega Springi unit test ei käivita.
- **Asukoht** — `src/test/java/ee/testiplatvorm/...` samas paketis kui testitav klass, nt `service/QuestionServiceTest.java`.
- **Nimetamine** — testimeetodi nimi on kujul `meetod_olukord_oodatudTulemus`, nt `deleteQuestion_userWithoutPermission_throwsForbiddenException`. Nimest peab olema aru saada, mida test katab, ilma koodi lugemata.
- **Ülesehitus** — Arrange / Act / Assert kommentaaridega. Exception'i kontrollis on Act ja Assert koos: `assertThatThrownBy(() -> ...).isInstanceOf(...)`. Vea korral kontrolli ka, et midagi ei salvestatud: `verify(repository, never()).save(any())`.
- **Oodatud käitumine** — testid kirjutatakse oodatud käitumise (taski fail, äri reegel) põhjal, mitte implementatsiooni lugedes. Kui kood käitub reeglist erinevalt, kirjuta kõigepealt punane test, siis paranda kood.
- **Iga test peab suutma ebaõnnestuda** — pärast testi kirjutamist murra korraks testitavat koodi (nt `== 1` → `>= 1`) ja veendu, et test läheb punaseks.
- **Mockito range režiim** — kasutamata stub annab `UnnecessaryStubbingException`-i. Stub'i ainult seda, milleni kood antud olukorras jõuab (nt õiguste testis ainult kasutaja ja roll).
- **AI (Gemini) testides** — päris AI-d ei kutsuta. `ChatClient` on `@Mock(answer = Answers.RETURNS_DEEP_STUBS)` ja AI vastus stub'itakse terve ahelana (vt `AiQuestionServiceTest`).
- **Nimekonflikt** — entiteet `ee.testiplatvorm.persistence.test.Test` ja JUniti `@Test` on sama nimega: impordi JUniti oma ja kasuta entiteeti täisnimega.
- Muutujate nimetamise reeglid kehtivad ka testides (`QuestionCreateRequestDto questionCreateRequestDto`, mitte `dto`).

### REST API

Baastee: `/api`. Swagger UI on saadaval aadressil `/swagger-ui.html`.

Üksikute teenuste kirjeldused (URL, sisend, väljund, veaolukorrad, vastuvõtu kriteeriumid) ei ole selles failis — need dokumenteeritakse taski failidena kaustas `docs/tasks/backend/`.
