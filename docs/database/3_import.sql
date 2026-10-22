-- ============================================================
-- Dummy seed data for the full schema (22 tables)
-- Order respects FK dependencies. Wrapped in a transaction.
--
-- ⚠️ NOTE: user.password_hash is varchar(30) in this script — too
-- short for a real bcrypt hash (60 chars). Using a short placeholder
-- below. Widen the column (varchar(60) or varchar(255)) before
-- wiring up real authentication.
--
-- Status code legend used below (table-specific, not universal):
--   user.status / competence.status / test.status / group.status /
--   competence_level.status / question.status / question_answer.status
--     'A' = Active, 'P' = Pending
--   invitation.status: 'P' = Pending, 'C' = Completed
--   user_test.status:  'O' = Open/in-progress, 'C' = Completed
--   result.status:     'P' = Pass, 'F' = Fail
--   ai_question.status: 'P' = Pending review, 'A' = Approved, 'R' = Rejected
--   test_question_result.status: 'OK' (generic marker, varchar(2))
--
-- paired_answer_id / ai_question_answer_id self-references are left
-- NULL throughout — they're only used for "matching" type questions,
-- which this minimal dataset doesn't include.
-- ============================================================

-- Kõik andmed lisatakse testi_platvorm skeemi
SET search_path TO testi_platvorm;

BEGIN;

-- ------------------------------------------------------------
-- role
-- ------------------------------------------------------------
INSERT INTO role (id, name) VALUES
    (1, 'ADMIN'),
    (2, 'HALDUR'),
    (3, 'KASUTAJA');

-- ------------------------------------------------------------
-- "user"  (4th row = pending invite, not yet completed registration)
-- ------------------------------------------------------------
INSERT INTO "user" (id, email, password_hash, role_id, status, created_at, updated_at)
VALUES (1, 'admin', '123', 1, 'A', now(), now()),
       (2, 'manager@example.com', 'manager123', 2, 'A', now() - interval '60 days', now()),
       (3, 'user@example.com', 'user123', 3, 'A', now() - interval '30 days', now()),
       (4, 'newhire@example.com', NULL, 3, 'P', now() - interval '2 days', now() - interval '2 days');

-- ------------------------------------------------------------
-- level
-- ------------------------------------------------------------
INSERT INTO "level" (id, "level", name, description)
VALUES (1, 1, 'Juunior', 'Teema põhiteadmised, töötab juhendamisel'),
       (2, 2, 'Mid', 'Praktilised, töös kasutatavad teadmised, töötab iseseisvalt'),
       (3, 3, 'Seenior', 'Eksperditasemel oskused, juhendab teisi ja teeb arhitektuurseid otsuseid'),
       -- Pehmete oskuste (nt Kommunikatsioon) tasemed
       (4, 1, 'Algaja', 'Tunneb põhimõtteid ja rakendab neid lihtsamates olukordades'),
       (5, 2, 'Edasijõudnu', 'Rakendab oskusi iseseisvalt ka keerulisemates olukordades'),
       (6, 3, 'Spetsialist', 'Valdab oskusi eeskujulikult ja oskab neid teistele õpetada');

-- ------------------------------------------------------------
-- competence
-- ------------------------------------------------------------
INSERT INTO competence (id, name, short_description, description, updated_at, doc_url, doc_filename, status, created_by,
                        created_at)
VALUES (1, 'JavaScript', 'JavaScripti keele alused', 'JavaScripti põhisüntaks, sulundid, asünkroonsed mustrid ja DOM.',
        now(), 'https://docs.example.com/js', 'javascript-competence.pdf', 'A', 1, now() - interval '90 days'),
       (2, 'SQL', 'Relatsiooniliste andmebaaside päringud',
        'SQL-päringute kirjutamine ja optimeerimine: liitmised, agregeerimine ja indeksid.', now(),
        'https://docs.example.com/sql', 'sql_juhend.pdf', 'A', 1, now() - interval '85 days'),
       (3, 'Kommunikatsioon', 'Professionaalsed kommunikatsioonioskused',
        'Kirjalik ja suuline suhtlus meeskonna- ja ärikontekstis.', now(), 'https://docs.example.com/comms',
        'kommunikatsiooni_juhend.pdf', 'A', 1, now() - interval '80 days'),
       (4, 'Vue.js', 'Vue 3 frontendi arendus',
        'Vue 3 komponendid, reaktiivsus, Options ja Composition API, Vue Router ning komponentidevaheline suhtlus.',
        now(), 'https://docs.example.com/vue', 'vue_juhend.pdf', 'A', 1, now() - interval '40 days'),
       (5, 'Spring Boot', 'Javas REST API-de arendus Spring Bootiga',
        'Spring Booti rakenduse kihid, REST kontrollerid, Spring Data JPA, valideerimine ja veakäsitlus.',
        now(), 'https://docs.example.com/spring-boot', 'spring_boot_juhend.pdf', 'A', 1, now() - interval '40 days'),
       (6, 'Git', 'Versioonihaldus Gitiga',
        'Commitid, harud, ühendamine (merge), rebase, konfliktide lahendamine ja pull requestid meeskonnatöös.',
        now(), 'https://docs.example.com/git', 'git_juhend.pdf', 'A', 1, now() - interval '40 days'),
       (7, 'Vali-IT', 'Backendi head tavad, mida koos live-coding sessioonides harjutame',
        'Siin on koos need asjad, mida ma sulle iga backend taski juures ikka ja jälle meelde tuletan: kihtide järjekord, DTO-d ja mapperid, nimetamistavad, Optional-i teadlik käsitlemine ja korralik töövoog. Kui need on käpas, läheb iga uus task palju libedamalt!',
        now(), 'https://docs.example.com/vali-it', 'vali_it_juhend.pdf', 'A', 1, now() - interval '1 days'),
       (8, 'Java', 'Java keele alused ja objektorienteeritud programmeerimine',
        'Java süntaks, klassid ja objektid, kollektsioonid, erindite käsitlus, streamid ning ühiktestimine JUnitiga.',
        now(), 'https://docs.example.com/java', 'java-competence.pdf', 'A', 1, now() - interval '1 days');

-- ------------------------------------------------------------
-- competence_level  (competence x level junction)
-- ------------------------------------------------------------
INSERT INTO competence_level (id, competence_id, level_id, status)
VALUES (1, 1, 1, 'A'),  -- JavaScript / Juunior
       (2, 1, 2, 'A'),  -- JavaScript / Mid
       (3, 2, 1, 'A'),  -- SQL / Juunior
       (4, 3, 4, 'A'),  -- Kommunikatsioon / Algaja
       (5, 4, 1, 'A'),  -- Vue.js / Juunior
       (6, 4, 2, 'A'),  -- Vue.js / Mid
       (7, 4, 3, 'A'),  -- Vue.js / Seenior
       (8, 5, 1, 'A'),  -- Spring Boot / Juunior
       (9, 5, 2, 'A'),  -- Spring Boot / Mid
       (10, 5, 3, 'A'), -- Spring Boot / Seenior
       (11, 6, 1, 'A'), -- Git / Juunior
       (12, 6, 2, 'A'), -- Git / Mid
       (13, 6, 3, 'A'), -- Git / Seenior
       (14, 3, 5, 'A'), -- Kommunikatsioon / Edasijõudnu
       (15, 3, 6, 'A'), -- Kommunikatsioon / Spetsialist
       (16, 7, 6, 'A'), -- Vali-IT / Spetsialist
       (17, 8, 1, 'A'), -- Java / Juunior
       (18, 8, 2, 'A'); -- Java / Mid

-- ------------------------------------------------------------
-- competence_file
-- ------------------------------------------------------------
-- Päris PDF-id kaustast docs/competences/ (base64-kodeeritud). Ilma failita kompetentsidele (nt SQL)
-- faili ei lisata - vigane PDF paneks AI päringu ebaõnnestuma.
INSERT INTO competence_file (id, competence_id, file_name, file_type, file_data)
VALUES (1, 8, 'java-competence.pdf', 'application/pdf', decode('JVBERi0xLjQKJZOMi54gUmVwb3J0TGFiIEdlbmVyYXRlZCBQREYgZG9jdW1lbnQgKG9wZW5zb3VyY2UpCjEgMCBvYmoKPDwKL0YxIDIgMCBSIC9GMiAzIDAgUiAvRjMgNCAwIFIKPj4KZW5kb2JqCjIgMCBvYmoKPDwKL0Jhc2VGb250IC9IZWx2ZXRpY2EgL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcgL05hbWUgL0YxIC9TdWJ0eXBlIC9UeXBlMSAvVHlwZSAvRm9udAo+PgplbmRvYmoKMyAwIG9iago8PAovQmFzZUZvbnQgL0hlbHZldGljYS1Cb2xkIC9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIC9OYW1lIC9GMiAvU3VidHlwZSAvVHlwZTEgL1R5cGUgL0ZvbnQKPj4KZW5kb2JqCjQgMCBvYmoKPDwKL0Jhc2VGb250IC9Db3VyaWVyIC9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIC9OYW1lIC9GMyAvU3VidHlwZSAvVHlwZTEgL1R5cGUgL0ZvbnQKPj4KZW5kb2JqCjUgMCBvYmoKPDwKL0NvbnRlbnRzIDEwIDAgUiAvTWVkaWFCb3ggWyAwIDAgNTk1LjI3NTYgODQxLjg4OTggXSAvUGFyZW50IDkgMCBSIC9SZXNvdXJjZXMgPDwKL0ZvbnQgMSAwIFIgL1Byb2NTZXQgWyAvUERGIC9UZXh0IC9JbWFnZUIgL0ltYWdlQyAvSW1hZ2VJIF0KPj4gL1JvdGF0ZSAwIC9UcmFucyA8PAoKPj4gCiAgL1R5cGUgL1BhZ2UKPj4KZW5kb2JqCjYgMCBvYmoKPDwKL0NvbnRlbnRzIDExIDAgUiAvTWVkaWFCb3ggWyAwIDAgNTk1LjI3NTYgODQxLjg4OTggXSAvUGFyZW50IDkgMCBSIC9SZXNvdXJjZXMgPDwKL0ZvbnQgMSAwIFIgL1Byb2NTZXQgWyAvUERGIC9UZXh0IC9JbWFnZUIgL0ltYWdlQyAvSW1hZ2VJIF0KPj4gL1JvdGF0ZSAwIC9UcmFucyA8PAoKPj4gCiAgL1R5cGUgL1BhZ2UKPj4KZW5kb2JqCjcgMCBvYmoKPDwKL1BhZ2VNb2RlIC9Vc2VOb25lIC9QYWdlcyA5IDAgUiAvVHlwZSAvQ2F0YWxvZwo+PgplbmRvYmoKOCAwIG9iago8PAovQXV0aG9yICgpIC9DcmVhdGlvbkRhdGUgKEQ6MjAyNjEwMTExMjQ4MjYrMDAnMDAnKSAvQ3JlYXRvciAoXCh1bnNwZWNpZmllZFwpKSAvS2V5d29yZHMgKCkgL01vZERhdGUgKEQ6MjAyNjEwMTExMjQ4MjYrMDAnMDAnKSAvUHJvZHVjZXIgKFJlcG9ydExhYiBQREYgTGlicmFyeSAtIFwob3BlbnNvdXJjZVwpKSAKICAvU3ViamVjdCAoXCh1bnNwZWNpZmllZFwpKSAvVGl0bGUgKEphdmEgQ29tcGV0ZW5jZTogR2VuZXJhbCBEZXNjcmlwdGlvbikgL1RyYXBwZWQgL0ZhbHNlCj4+CmVuZG9iago5IDAgb2JqCjw8Ci9Db3VudCAyIC9LaWRzIFsgNSAwIFIgNiAwIFIgXSAvVHlwZSAvUGFnZXMKPj4KZW5kb2JqCjEwIDAgb2JqCjw8Ci9GaWx0ZXIgWyAvQVNDSUk4NURlY29kZSAvRmxhdGVEZWNvZGUgXSAvTGVuZ3RoIDMwNjUKPj4Kc3RyZWFtCkdhdG08YkJEVnUmRGQ0NllaLG9fS3IhU2kuWil0PDNHSW0xYjM/bmJMa0kkISxmJ0pSJ2BFSUshJGsiVCRFYSpRNSplWHMyMzBoQyJyIjlCXlMxT2AxV0o5KGZXLjUnKC5OLEZAYzp1U0BaKyVLQW9bTUIuXWQ2QlZoNioxOipNVGU5RXNQOUdfQzJWJWMiOXA/WFxfb2ZiRFVjQ2AnRSpURkUtVD8xMCNxRXFXNz5LczJiXmBHPjBSaj9pSSY+bWg8XT1daFpvKE1yVywvVWpKJDVORExucTg2QSRyNSRiWVlnYFdNJnF1VEFkKltcPFJSM2gpPmQzOi4zZkROaEJVUSVBOksqUjpYW3BdZUk8LD8qZjdMdFE8aTRacC9ZcVFHLE0qbS5yQilGdTMlX0JPX2lyczMlSUxJTC5WIVdIPUs2TzJmPUxYVE5BJlg5UiglMklVTXI+YFpVIzoiOClrL2w6OEA+bXJeYloiP3NNR05GUGllW2U+WEFzZk5rSGFBSF8lPV9HJSQ8blsnIW9kSlJePjpKVTlCPywicl5fXCotWk07Mi10P0ddMUQlQ10jN1Ezai0lQWVlT2VbTTpQT08+RFpXNkVATlY9VSVlN2ZFXSZKWD5ELDBnYjo8XDY1LDRwVUxyO3VGWzRESkJia0liMDlJP01WQTxBYkc0XT0hWjBaZ286SDcrPTRWSiE9RWIxWGY7QCNcOU1BcUA8JyQrWlFfKTZDUydKXTlTMDd1UC1jQFhgMi9VIjNzZTYnTitcWnBgNkkzczNPSlVXPDNUZCE9SyUsXVwlIiJITUImUjNdKUhCYzczLF1QdW1QR0M+WSVHOWlJcGZgP2QqTSZIbGJaTVNsNCVbZT9IRlJJRWdGczRuaCkraCQoZ25qSF51JENFcmBCImBKVV42S2tNO0dsKj1TTVRCMDZLbCRjRnRVR2UpOTpaRFx0NmBCN2NwL1puND0uSEU6PychYl1caUlDZFU1VnJwKisqbjM1Nl07JmpKQEFjK14pcFxSJChoWU5hVHE8LFNvQzcpPCVeJltIYWJYcmFmczhFUGxkNSFwTU8tVidHMmUjK3IrcTlqKTR1UEdsXmFdZ2M9LzhRSkVAO1QmKHNZN0hEXi0+LDYkWytzPmoxMD1fW0koPkJJbGJYVXU/W2JPNE1YVD8oVm1uVk5hTmo2SHNfUmEtbjREZ1dIO0AwX0xiXTw9Im9OZGlfKjM1RCZmIS1zPlAnMEM3OSc0UGxlY2ljLUxBcmQyTC5CQzxYJElCRmVBXklvViRTWWRfSTIjXkFmY1dzJClFcU0rVTdRLkEyXEVmLHEiP2hTVzJjIUxsL11sTSUjMFBZa21fbU5pLjBiMzBVWEYwXkY+W1laPmMjYiNFO1BkODBHJiNENkREKyY4SDlGJSYzcDYoQjcqZzZtWWlma2BAMitOQyImOyJVMmlYV3VRUUUnITc7M3VqbUtTRUs0JFk3SyRpLCVaUzRIMkAtTSlUKiQpT1smRz5OQiYkQDpFWjpAIilGbDxtblYmIjtERCJFUjQ5TDtdLlg8KEFdcDNzUTc5Tys2NHIwcVgjKjlYRS9GMmZNbDBPUiFyayNhMl8mZztoNW09MktEMHM1KSwmcUwwM2k5PFAhNThNKz9qQENQa0pkUS5QNSEiUj0iSmhPLmRCJzd1VUteV2pNJGg0QjxENjVLYyJvKGtJcFFNOil0WDJxQl8/TS0uIjNNLFs6ZEhsJF0+S29kZGwwWFIjZUxBa1c3MTAkJzZkYEdoaWM5cWI2IU42OSFHJjxgNFllajEpKUxgcTZhQSU1dGotMUtNKmk4ZCI8OUZPbSZaczhZNEhsYyphPGA+K0AiPko8TipRYDY2KXJjXipPTm5COz1ObkVeS2w5bE5QQUZyIXA/OV1SNyNzKjIiW1I9ak9fKkJjQztjOldnamJXY1VzJ09uXiVKOmJMY2pvaTUlUDxLZGhHOTYzXWsqZ0pWJE9ZP0Q8LCs/dElfbmFAbTRhckVqLjZvbWJlXFEhaDVHPGMzWWRsZDNbYVlac2VWZlUuWV9OXWIhTihQMWJOMzNJai0/WSM2Ty9bMFBcMC48OWckIkMyT25IOW9yVypNWy5pcWJNNUxJVz0lPldCPS9NVVZMKEhmLDhNQFYrNGtyciVXSjQ2Y1hYLGlLYk41UixWOUBHZnBAMjE3MS5MRE9tYGsqSzBnIVdgXmJkJWxDI05FU2ZxaUF1T142Xl0jKyspcEZWJSNBUUc2ayo6JEgzcWYzUV5qOVBgUUBgZ0JgUUE9MD9VZyJpK2VAW2xEOy1BbjUyUChZL0hXLyRcaERWKCNnQDkhXkVkZjYhdVxoMGhUSU5JaXJHTG8wIVpmNFlcWVExIUNgaUJFckk0MTRNWmwzQVJDKVBVPXBWK2JtN15hXGokcVwoVHRmVTNKKG5gJydOMWBaWi8wN0YvaDs0ZEJ1RCpoWk5ebEs4IWc9P1RSbTx0YnVBXGdiPGkoRiwiNE5DUSIiT09QXTxWW01kVHMtVSVFbjVuPDxGUzJiNWtoRF5FMCoiLUkxOHNMJW46TlshaDU5S0MwKVdfK1dsZGU2cWtpdE1DO2YvcWlrYTpLX3NdMDg+SVtPPXRyVG0jJVRGKDFcVHUhIXNEZkwnWGUvN246Ry0hQElCKEFEQDlmRFNGSWhwXEE8cU5kUEdTV0Ihc2hAOXNROHRfS2ZjL0cuLDkiZDJKLUsrODBkWktUal1Xa0FzSXA5bSk5KDYmSGIrPk9mUC1FSEFJNHJzRE1ZMm03MCw2LmtaJ2VMZGglNVZzdTRIQE04cUhEYCQubDpxNlZpLk1XRm5zWklwb1wqVUZSaSxjMlxjZ3I1Wz5WZD8qIV9Na1w1Rzk8JUIjYiVqLmxXUmpgX1lwJFhscXNET0pQcU1oPXBTW2txLHNCQ3A+dEd0SEExTSVXRkdSTG1NXylhUlAlQWlFb1xSVDdsRWNAbXImaF5ZOFtFWiEicC9fQC5kITEqIVQ6WGpZUTM3Ik4ySWlnamBlIitxRTxwO29jJVAzPSlHV2ZoRWxKRGBuPVYvLmtiKlJ1ZWZuXGw0VCs+TWVvVDczWFZzZWFVVEUwdT5eXEJRSCZeLDQuRy0oRkU+JG1hNGc+WiluXz8zXWk8ck9ccSMrUGI8VTVtaEpRLUk0KTkrVmVaMkYsT21jIS9lK1RUMCpcb0c/RSVxJD5PRlJgIUdeMEJza3IuT1lqa0NibF08O0w+KlA2P2xxPjhxYzdxPDRLKTdDRGg3bzo1cj87ZTcjK3RJXDJbLm8hVV0jQDA/QSlARkZmNUpDY1JEYXBEUy8qRGJqXHA5LlJaXz5fIVU/U29mVURfRlo4aDJNIl9QUWZJPTdbXiRUNThUNT4tSmFadSltNmI8UktFIk9kSjw+M3A/clpSOy8yMkxpKFU4ZzRzVzFjTHFbWnRkQ21oQkg+Jig3MnAkajNxalUkSGg7LXI6azgvTU5oZ0UiPGdmc3VpQi4yTkhHMyVTIThcVVZTLFIoQ1ppYGdWXmpWQU90PSFjPEokXy9AZzBoTDhVSj5BKi07ZmEiKShrP0EpVzRUPDdjci5JSS9bP0MoWUQiPEpoWkQvTGhaXVdMRzZVKmBTbFFCaUpnMFktLCE3YmxoNG9iIz5BKlZjSS1ASEhyOGpzLlhmQiNEMGM2WTgyIWlnWF5sP1FRLk5cNC5wIWtrbmpnc1Q3NGcib3BJOWw0OlJhZydZPSswYUZMdDZFMGtjS2cwIyleJEMuKHItWVprbGArQTBvaig1M2JiZm9iTmdbNlVPaVQ9KF8idDxrUD9LN1hbaXRbbUZCUDdCSFpPWC9IQS9lM2s8L0VvI1I6NkA3IVUrN10qdUMrdWREUS4kJmZhK3QxOkA1c2c6Zmcici8ic15RbGc0XC5ROytRYHNdNj9VQyMoa0VxQWxAIWBVLEIhIzRVUjRxI1MmQT5VS01yIipwXU01Ry1pOFEyRkRGckhPLF0qJi85IW1kOjRzSCosUzYtcCFFNidCJEctc0thMDJHKlknKXVaQTBXYDJcNVRWQkQ5VFByVz5wI21jWH4+ZW5kc3RyZWFtCmVuZG9iagoxMSAwIG9iago8PAovRmlsdGVyIFsgL0FTQ0lJODVEZWNvZGUgL0ZsYXRlRGVjb2RlIF0gL0xlbmd0aCAyMjk2Cj4+CnN0cmVhbQpHYXVITD8kRyRdJnEwTVhSKCQoQkE8aXJZSUpEJis0KU1YdTJNbV0yKzhjMiZPRyJNdD1GZGNZSik8PTE3N2pySy4qXWNQLV1uTjVvNiJeNiRsRGxtXlgzIlFBNDZIaGRTRmRLJDZ1ImIuMy5zYTFxZmFPbC5NXEFAQyVhIkhVNi4nN1BsKGddc28tcEUiN3FVUDk/QSg2Z0BDP0dWSHMoNDwrbi9BL0FsPSssXjE9OV91Rk4pWWBKOTVITFtWTyhYJDFNKjJmY0pRRWRSPSxLTS1VPjdWNkNPTWhQKSs2a0xmaDFJNT10K29ua29WYlxPaTthYWhjQWVga1FVb1ZKK1NkSWhvXEhnLkQyPWFXVWEjUWIyYyYtN0g4QiswSDpNOTV1RF9pLCMtbGonZXRdQlFVSFchK2tdcWsnSm5eRCxFXGxDZmM+O144MGolNmE3WmpGKVs3c3BOQ0w+MEUpNWprV0k7UjhWUjNVWSZzRl1LLiw+Jjg1dGZmVUNLQFVtKUhCbikkK2plOiZUVERQLCxZW1ZgXWJBRCQvTCQ+Vj5KUW9wJE1GQWwxV1c9czUlJTJTOUYvR3FsQHApRmRLOSNTM0s3PEVyR1ApcmVLP3MrV0hBSTZ1Nm5iXkRpcWcrJlhBMG1BWDxpIWlGaGVeRWY1NldgMmJcMChSZV5wISwyR186TycjJGs8Q1NgaTdEPWpva1djOFw4bGRBWzVSMCdQRmVxKztMZiFPIyxAOlwnZTlmYDcqLmxna2l0bWhzKWFhMXI3WTJBJmozNiw5MSddW3I0cWlAQkluVj5iPWlFX2MqamI1S3BsN09pLEZoVighNEs6RGtUWmM1dDhtOVUkTk0wIkZZaHJCK1BHYVlJOF9hUS5ZQVgvLHFCPERHbTcuOVozNjhVZ3RUP2w6WElKOmE0PSVTVzUtVUpXSW1QdWBUPk05YzRUWEtrMTlOPy4xSjArPms6ISokVipOZzRaTkJybE49NGwzdGxqZUl0aHJvSGcjOWVLIlAtaV9eQCpyK2IlRTpOK11hbzA+TFdDaT88IV1BJXVFNi5ucXI7NU5VMmk+SXMjXEcuZktAK3AvXWw9bUxMLCIsU3EkXlA8ZV5BaVItJG9RaWwpb0lMbj1zQjBYXVlyZy8pLF1tJk5UNyxycHNOKWswQyxYb2BuNk5qMGZFZE5wWS5uLWhfS1JScy9bdW1XXGVQRyw0ME0nQi9oX0s0Z3FRIlpPYWw0PiJNIVZYazs3aDlIcnReZFFSYCY+S0AlOCVvLWtYNChEMCJbYDUwYVo4cTYzVSJqREFqdGZaZW0uLF08VipjLmEha0csOE5faVIvRDNuUFZnYyJQS0FFXUVXPlkhVkxTS3U1ITlsUU1fYS8+VkJjaSQsVEtoU08lSy46KzoldU41QFteJ1ktI0pvaS1zPV5SXzguSzEqQlI6bU1qMSxwKi4+ZFplRCohYFBrSENbZHRbVTwscGFhSjAmVSJRV0xWYFZONjUvQi0lK1JWV3I5NFFLbVJjJVJdWyM0WiJCR0pTby0yRDJnS2dQL1hAXEFYZC0vKlFXMiwwQCpbUWZXRTpaTVhJMF88LiZEW28lJ2dyUGREVC07UEhtKldqWUJvay5PY2t1P19IZyZWYDckNkkyQUVmIkFOdGYxYFVcY25yKlwwRTc/KG5TJTUyaDNjXltBI2Y+Lj4/ViFnSVJqQ147ITdzbzEuazA0NVhDRFNKSlZSNSg8RzJDXT0zTDgmUFI4cD5RLjRzR0k/VW9JMiZeTmQoPkxIRTlnM3VFcCxOZ3QvPl5kJzRALVtxYHFTLCNfNWkxVC5xLGI0JD5XJjg0OWAsQDNgXVBGcGlQIXFvZFFSLTlaM1NGUmJQP2ZAWkU3KzE3WSFZYy5VZnRUKXQ2J0xNNUY2PChYV2JnZmxNV0QiYSt1VGRCZHAmb2tzRG9CIWNnRD1BQmQiQFdbOSJwWVM/Ml87PTZwZy1sMl9JImZWK2AsQj9zJWRzIkhoOlAoJz5jJm1PPShsIWcxQTdAQSZALzNuMldtWFVVQldPPlRmXyJSIl9iTklDVz07OTtJWkc2c1dXU0BvUHBMIXNPYVI6Tk1kTFhxNmpHXGpAbG0oOnVRQkFwY1wnQ0pSXCdxR11RQ20qNSI2X0cmR15Yb2BhNlFCYjdCcXAlT1xSR2FKOVomaCo5Si5fXUVgdFNLYVBZTCtQIVZhImw2ZFNLdHI3OSctOycsWEE/PSdMaSFhcWpyYjo0J0tTdWdtWlxiKGhdKG8pQSxrTjplciVWLD5bLisrSzsnajVeYz9kXEFOZENZUzAhSlRsW2I3IlNZOFk9aW89KFcnYyVmQ2U1XElqbl5tSmguJT1udWUpMS9CTEFoOiE+Tl9xLCtYOGYkLV5SSFY9Wy5ZZlpdQF9fQTohQCk8am9VZmxRUFhQbFtZVykkIkg/NGQwPnRGREBJJzpubzZEJ0c3OztoXy1QLG5RQTFYdS8+b3JVKk00MitAK2lGOEZlYTs1VFYlYkRvT0w/MDJBJztLZjApcm9rYSZcRG8oLG4oMSxwXEw8PVtmYkp0WV1uXFw8UmByUFJTMUlmKmteSnBvZC9KPl4yTkhfX11KYl1XS19bWTA7bDFfb2xbcz01WWEjNGhnYiVfPz1RVVwlS2pkcklwRTctQ1tUXmlAImpbMTtVaWwpN2JzSjk8Mk9eIjVGXCdAakRIOjIuNTs/P11gRVB1L0VjTCVJTlVwKDs2QyUhaiVmTi5tYT1lLG1BTnQwTC0/WzJvVkltJFhdcldCR0ZcRC5AVWNAQC9iSzVzcnQ+NiFASW1NbFliUlQvUzE6VDlbMWdBZDkqR3U3LmQmOV9JZTxqPFVARzlDVCRKT183P15mWyk9bGEmVixQbC5ARjIzSmluJEheL0FHSWFhblY+b0w1dWI3TVsucC9XTFZQKkRjcUg4NlVAPSduRUFDQWRsWTojWnV0U2ZDayVNcnJqQyMrUmBzcmxXUUs5PDVQSHQ2OlpYNFRqQHMlWzlKMSQmRjUzZ1heSkAiaFpiSFAhR3M8OH4+ZW5kc3RyZWFtCmVuZG9iagp4cmVmCjAgMTIKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDYxIDAwMDAwIG4gCjAwMDAwMDAxMTIgMDAwMDAgbiAKMDAwMDAwMDIxOSAwMDAwMCBuIAowMDAwMDAwMzMxIDAwMDAwIG4gCjAwMDAwMDA0MzYgMDAwMDAgbiAKMDAwMDAwMDY0MCAwMDAwMCBuIAowMDAwMDAwODQ0IDAwMDAwIG4gCjAwMDAwMDA5MTIgMDAwMDAgbiAKMDAwMDAwMTIwMiAwMDAwMCBuIAowMDAwMDAxMjY3IDAwMDAwIG4gCjAwMDAwMDQ0MjQgMDAwMDAgbiAKdHJhaWxlcgo8PAovSUQgCls8NmE4YTMwMDg2NThjODFlYjViMzQzNGMzODM5ZmU5NmM+PDZhOGEzMDA4NjU4YzgxZWI1YjM0MzRjMzgzOWZlOTZjPl0KJSBSZXBvcnRMYWIgZ2VuZXJhdGVkIFBERiBkb2N1bWVudCAtLSBkaWdlc3QgKG9wZW5zb3VyY2UpCgovSW5mbyA4IDAgUgovUm9vdCA3IDAgUgovU2l6ZSAxMgo+PgpzdGFydHhyZWYKNjgxMgolJUVPRgo=', 'base64')),
       (2, 1, 'javascript-competence.pdf', 'application/pdf', decode('JVBERi0xLjQKJZOMi54gUmVwb3J0TGFiIEdlbmVyYXRlZCBQREYgZG9jdW1lbnQgKG9wZW5zb3VyY2UpCjEgMCBvYmoKPDwKL0YxIDIgMCBSIC9GMiAzIDAgUiAvRjMgNCAwIFIKPj4KZW5kb2JqCjIgMCBvYmoKPDwKL0Jhc2VGb250IC9IZWx2ZXRpY2EgL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcgL05hbWUgL0YxIC9TdWJ0eXBlIC9UeXBlMSAvVHlwZSAvRm9udAo+PgplbmRvYmoKMyAwIG9iago8PAovQmFzZUZvbnQgL0hlbHZldGljYS1Cb2xkIC9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIC9OYW1lIC9GMiAvU3VidHlwZSAvVHlwZTEgL1R5cGUgL0ZvbnQKPj4KZW5kb2JqCjQgMCBvYmoKPDwKL0Jhc2VGb250IC9Db3VyaWVyIC9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIC9OYW1lIC9GMyAvU3VidHlwZSAvVHlwZTEgL1R5cGUgL0ZvbnQKPj4KZW5kb2JqCjUgMCBvYmoKPDwKL0NvbnRlbnRzIDEwIDAgUiAvTWVkaWFCb3ggWyAwIDAgNTk1LjI3NTYgODQxLjg4OTggXSAvUGFyZW50IDkgMCBSIC9SZXNvdXJjZXMgPDwKL0ZvbnQgMSAwIFIgL1Byb2NTZXQgWyAvUERGIC9UZXh0IC9JbWFnZUIgL0ltYWdlQyAvSW1hZ2VJIF0KPj4gL1JvdGF0ZSAwIC9UcmFucyA8PAoKPj4gCiAgL1R5cGUgL1BhZ2UKPj4KZW5kb2JqCjYgMCBvYmoKPDwKL0NvbnRlbnRzIDExIDAgUiAvTWVkaWFCb3ggWyAwIDAgNTk1LjI3NTYgODQxLjg4OTggXSAvUGFyZW50IDkgMCBSIC9SZXNvdXJjZXMgPDwKL0ZvbnQgMSAwIFIgL1Byb2NTZXQgWyAvUERGIC9UZXh0IC9JbWFnZUIgL0ltYWdlQyAvSW1hZ2VJIF0KPj4gL1JvdGF0ZSAwIC9UcmFucyA8PAoKPj4gCiAgL1R5cGUgL1BhZ2UKPj4KZW5kb2JqCjcgMCBvYmoKPDwKL1BhZ2VNb2RlIC9Vc2VOb25lIC9QYWdlcyA5IDAgUiAvVHlwZSAvQ2F0YWxvZwo+PgplbmRvYmoKOCAwIG9iago8PAovQXV0aG9yICgpIC9DcmVhdGlvbkRhdGUgKEQ6MjAyNjEwMTIwODQxMTMrMDAnMDAnKSAvQ3JlYXRvciAoXCh1bnNwZWNpZmllZFwpKSAvS2V5d29yZHMgKCkgL01vZERhdGUgKEQ6MjAyNjEwMTIwODQxMTMrMDAnMDAnKSAvUHJvZHVjZXIgKFJlcG9ydExhYiBQREYgTGlicmFyeSAtIFwob3BlbnNvdXJjZVwpKSAKICAvU3ViamVjdCAoXCh1bnNwZWNpZmllZFwpKSAvVGl0bGUgKEphdmFTY3JpcHQgQ29tcGV0ZW5jZTogR2VuZXJhbCBEZXNjcmlwdGlvbikgL1RyYXBwZWQgL0ZhbHNlCj4+CmVuZG9iago5IDAgb2JqCjw8Ci9Db3VudCAyIC9LaWRzIFsgNSAwIFIgNiAwIFIgXSAvVHlwZSAvUGFnZXMKPj4KZW5kb2JqCjEwIDAgb2JqCjw8Ci9GaWx0ZXIgWyAvQVNDSUk4NURlY29kZSAvRmxhdGVEZWNvZGUgXSAvTGVuZ3RoIDMxMTIKPj4Kc3RyZWFtCkdhdG09PkVkTXEnN0hLW1R1WEFQOV5MUjFNVjtPTWxwQU11MFdFYkI/NS1WI2MuSj5lajlhTEhzLixJJ01mIlYyWzdpOzJOb1g1SjpTNjRKQ2s9PWxJLm47b3JuK0dmRGRETE9DcFJYPmJhR2BcI1BNYk5vRFFaXEdLdVEjYDEmQUFbVGtBUnEvZWc8SFdabCxqYTtPWyQjVj0vWTlzbkYwbEtBWzRYNGs6U2oyXis1RUBTRT9IRkgrPiw4bFdjTWVOV2FFYyE5JFk6KHVSNkcmS0RmIyJhQyUqIUk2WCEvYi9NM0tscTheJzJdNl8jYUBeKz8xN2VUMFJnSmtETTNNJCE6NjAoblNCMDsyVy8wNzNcKjs3NUhBMzpbUDA5Uk9BM2RRPk1TOyM/UydZMyY/VT0+NTNhQzlEOk8jZU84XTNIW1ZMUCFNOStTKltgOyg7OEk+OjhhRlhmZ2o0KE1pL1hoSnRNdUBJKUAyY2RrNmBWcDtyNEpub2dqXFAkRzhsYGEvUlNrMTRGOkJSSkFSPksqai84IjRrRUYzaGNDQk08WCxLMF1qW3UiKE5UJi49U0xRSDllQnBLJHIuXStOSF9SRilnP20wKVVgXTM8KjUjSkNYLCUrWlw+ak9qUnE4M2lKJk0zImg6LF1pWkYrTypfIkNZW2oqWzVUcy1TOlFwaFs9ZyVSJEVkTzgxOVduYnU+SEcnYWcuMy1SWCJkPF8/bWJhIW5EJk1JZmYnbGdUbmpWK00rOk4xJlVRI1wxMF1MSFBvJzt1QmJOTVMvX0gkIiZsLmc4NVEoN25RaEo0OidaIVQjM2BucVFjbm5caXJvKElVbjRpO1RyZms6L2pcYCs+T3RnSUxbNEM7ZD5HdCRaR1UuanImOkBMVEc3QElFVCNGa2EuUidjUyEsW0BQWDxvMDNjUUpkTkthUjhyQjpmO2ctNSwvXVBqRCQ9K2RfTCE/JD0sUDpjV1o8Wmc9U2dbNE82aj1pUkI0VypCWUc0JSNoU2J0U0c+aiZKczgkOU0lUGtkWCtZailQazQ1MGFXZWlXXnBjJCxPaHBlJFZnP18ncWR0JFReTWtiLjdvOnN0RylcUVBBUTgxbzpPNlwka1szQ1RYOGtwVyUrTF5YdVZmdF0hPTAzYEs5U04mIVpMJjZCaj5sY2FMP3BJTE8mUWgtYDI2NHQhbFZmWClmTlksUDBVNyxkcDFdcjtecktMaWJhTCcqOGkmNSNmSj5mVVUkYWxNcXBXSWw2WVNgRWRlQ28kP0Q1JmchKTViKk0zdG1hb15ibi80P2JILDoyN25XKSJPIXM4KGY2Uz1bQDlzInBYTy1fPyRMXmByUWpAKTowc0VAVzxEYiJQSEZlZTw4QEovMnBxS1g3Nyc8WFtCYDtTcGxISyREa0ZXNEpHJ1ZlRGxGYTZwRiMkS3NDUmR0WitaaFcucWw5SWdlLlhdRzMwKEIpYUNvdSo3bGZAL2lWLGFWcDUqJnE0SGJLbVZCRG1hVXQkWnFUUSs2NVI0LXA3WWRDcTxmQFBPNlgjTCVCQ25uOXMrRzdsckpcO090aW01SEslYEZSSWc8OlFAYWdpW0ItVGBSR2wwQnVOUTZuZ0s/TkQocHIhWjFUPE1oYlhRVjg5PHEiW288Lzo4Oy9aXC5FLjdpcDxHOlRdXiJpNkBFYzBTLEBMViZPQVlKayNHLS8jJ0A3PltFXDJAX15fO182JG1gVkFAa10hbWlKLzBUTzhyW0I8X0p0T0E6SzFgOTUlZGJYRTNgZChHQGhpNjxJUlhjc2wmR1U1blNrYShVYUdhUUA+W1MocixociNAaWFtJGZWPiI0PFI/Y1VOaFMlJ1YrIlQyOkVQMVhIV0tQdCkyLyIuXSVDbGUwa0JPVlZxISYyJlJjNj9QNDMrVU8ydWQkI3E4KVxELmVKN0owQ2YyOl9taGpuKHVhYkRpTVVlLkBLKHMkNFZNZFtMPCYjOUtgI1FqL1NKbXBoSGdTMF5ULCEqYykxJkEhWCwyWz51bmYmSzQ8cVpOLjlmUCdrN3FMP0Y0Q0FSNnAiTFxgL10yPmhGLk8xLCxzPTZyLE1vYGMtTERJUiRVOylRXjhvUDZsKCEnb1ZMRTVuLyJCVUxqSl46P29HbUBWcDFvSUIwNE5hYF1yQSNLL0gsSU9JP3JQYV1rS15FVSdlUitpbig2aDxAajonVWkuWjw4WCozMCY2NDcyVWsoMWwlLzxvJFU9JVQ8O0dVajZVPUluMEduQSc6UU1eREdqSXIoOFRZWUI5NkZKKiEmJk9GOytQTjZHNW9XRFI+L3RtJ1NXMTdgW3JTYW9XN1RKWj5rMy91WlNaZWFxLy5kSVQ7M05tX1Rac1pwVW8yPyZZWjE5LE1BVTJtInEjNVFiZEYwM0NNPmsmJkVyI29LIVUyVSlQNmQ2dHUmQ0lIOFdbJ2pec045UDBYIlcmWEBlVitvTShoOy1dN2xNXVEraFtcUU1xRkEwWCgmY1JuSkdOL0wtKkFcaGEySTBmMz48XiQlVjx1aHBrMyFyZklpJmhrXVo3Si9qTzhuZnJzVGNdaU5TVVc8LCwpRzVWcWJTWEMoYi9nYFplZ0M9ZGgkck09b1hcUTtpTHMzTmBlKU9yYW0lSTgzb2B1LSUnV0daXVNILT5xTTw3P2ZXMTdfOF9TQyY2cjBUbmlYIy9eJ1RJRUFIaVlSJFdtYSZAbWlPWE48Tmk9LF8xNmpIV1dQKVJpSFpwJGBENVsyRDhzclBIW2dvZVpHZV9nX2hGKjJjajZcWSpZaTM4LWQkcmBFWUNVdChxTi5SVy9BVUxBZTk1bm8tVEkzUz80Zyc/bzwkbyhPYFN1WT8kbCMpMTRicDwqRydWP0tqK0RpVVY7ZlssIyhfMzIhLmEmVSE9WHVjJSVibFU6P2leJFApVyc0bVJAKDpkamFyZldANjdCcDI3WjtEQGpkJWgyI1E0ZkQ6Z1d1OTpnZzBBTlsxJDRsZVhoK18xcVc5RSdOJFYsTkMiaGFVaFE6Q1g/Y2kuL1JTL2I9b1tnUG9ZSzIhLXAkNDtYLVhwTXAsXyEhV0c9L1A1P0FTTW1wVz1PanA9bDVVNUk+OXIpRWhWOEJRPCRHTUdvYDNXXiZzcyYmV2c7Wy81PUNQaGdPMm8pdTwibzswWChPcGE0RVk0TmwzRUFHN19fUl0oRz9zRk8xP18tJkktTSFrdGFLW1YwTCJpbGJTI1A5MmhcT3RLXFpiOS1XIk0rNiM8dTlXRlBIZF4iVUxFRGRyZ2JIOUhMTiQ2VyUxLChKPCpKKF9WVD09aHFGWTtkS1BtOSkrYzFDQGwrPCkhRCNPOGVuVXVGQzJkXVVtT2pqaDkrSSguZV9LVmoraF10Xk5wJTNxYVlKXTdzIlhhQy1IU1BoOlEjOUpFPi8uSkhjIk0nPXFSVDliPltoS2IhVihCKExqSkw/Vmk4YjQpRD5mZ0xaaj5jNztYS0VRbUdnbV5GKWhBSWw9U2BrXUpQTDhkcVhVX3JlRT5PXG86MSRVPi8rdUMpS2khQyssSiVAN0JSTCM1bUU6OF1wcjo1bjVxNGFXckU8MkYyM3NnLkNTV1k5QD0zcSoiVCZnI0REKkRFaDB1bkU0ZUMmWzRWYDRbWkRRTHBaQWxgXC9Ha2g1cUElWyYsSiNyL01ZaytwTytPMklCa14jY2s4Pz5MMUBKal1OI1pPLm0wVWJTWSs6cGsuZFk/bkJrXzxrM3A1WEQzOT1MaSRzSzcuUXRlJWZbXVJtXStiM2wtJTh1ZD5WPC8hRlMoJVU+Si9tN01DT2wtazZ1PCRURyVjM3FSLyk3WlI/W1E0PFhvUkMrUjNYdD8qQ1w/O19HZjteSEdUWGAqPUE5NERrLjVjTjArVVQxR0JxSy80I1xOO05CYFhyZGBNXV1ETVheVzZAbTJwU19BQFVDYDc5OyYpZUVvI2hkY0cnNCVqMzYvTF49bD5OYkFEUWFmcHU5Nz90JUxbVXUhcGdwSThxInAkcDZoNGk7LW5SKkpiIi0kbFlZXVg/P3RVO3FURTwsJlA0SyJGJ0BKZmE1KHBJJCxlPF9OI3RIMU9ZYEMwaFlyYWZIUiZNbTpzdCg5UTEhZWJXcU1gR1lZYywoSFVRfj5lbmRzdHJlYW0KZW5kb2JqCjExIDAgb2JqCjw8Ci9GaWx0ZXIgWyAvQVNDSUk4NURlY29kZSAvRmxhdGVEZWNvZGUgXSAvTGVuZ3RoIDI1MjIKPj4Kc3RyZWFtCkdhdUhMOTZdLiEnJk5MZWJqSHJEWkhCJ2RmcWxBV2c3NHRMV1JaMVAlN2wnYEBoPCNKZCMuaSRTSDEvKEBFVyo1N1J0TGIjUWIrUGxeTWYoNkBiXVRyUWY4MFQ5ZiEsbE9ebz1RNmg0KyxfPiY9XDYzLWReJ19LP0JmSEVuR1QrQ1RTUFJabjlsTSxlU2gsSjQwYiVRc0xzZmdiP2cyWXJOQnNiTixWPXAhOC9DX0BNV1FXdTdmZDk1alVZRkRQRE9AKGdCTVFNa2piTV1ML1lrN2lMNnU4STE0Ojs3Nz1CNDksUEBqU0BzSTctV3BANWldY0A3MDYuWHVKcDdtUnJnKVZoci4sNF0/VUNfcT1ZTyYvSCQ1cnI1K2ErTEFHUWZwTUkkPzckP0xCSydeIU1ZVTtRVVBnJF9xZyQ+dEM5QE9IRzNQbks/ZUlHSllFYURaPTE6R2VocF1RaiJGZ01NLVspI1tsPjFjWC9RXjVpMEhPX2o8I1VtalJNWC5ZV3ExKk48IXEpcXIlMUJ1Wi04XXIhPT5aWm4qcExlcVI9aWN1MEVXXk5cPlVbXzdPSzVzcWxrbDJCWFFkSjJBJnBLanE+ME51SVYkQCVHLXJOblMnJW1SSyVfJ0VUY2csMiMoTWFxYmlKKW1TSF9rbCxAM0tKTkxPLk4zdEwsKzo1aSokTlJJT00wJ0s2NGxnWlcoOUtaLWs1STddV2pISmg7b2dFYzgpX01MTm84OVhKUmpyKWREXmZWXjBqNVs6X2tMXlU4Wj9nVi8zXSYlVUZZWkNLPlg3WHFSJE84JktGdTJJXjdhMidMT00tLiIsRzBbKCYlWXEkOkVmRiVcPTdSbGxCTXB1WC4oKTc+JUEvSkZFKklRYic1cTJlaGdPKXAuNmReYS90ODYzP0lqZkRpLWxKNVNMaGk+UCYodD82LWIjW1gtKlFILE1RZSRcTUBMSllfN3Q+Q2I3c1AzMm4oSExqOzBbRUQ8dEQ7XGFDLk5sTGFtVFM3WDc+MV1LO0MiVlBzcForLi9CQGNjTFstOS9TQSpETyNDVk9odU1Qbjp1bT5OZTBPSComQTg8MGA0LT5vZSFbWEskaWN0QVpFL1JNcSo5X1MvTTw6K00+XC46S05gXydXK2cxMXBebU02aG0ucF9mLk0uaVYsOTQ9TEMmOGZyPV8iTEU2V1NxSCJpUiRCRSstNTgqQlJwJ1xib0VyKUtma15AXCxvM2cvPnAucWNXVmdWXkIpPmhJJzpSbzIjLVQjPzVHYkBWaDRkOnQmLWMxS1xkR1o9dF8zKltEclEhQWRTRilcL1pvRFJ0QEhYIitSTydDPEpwP0UrXzUiZjlvTW0+Xl5HLl1cL2ldJSRyMVopV2YsT29aWiU4WGRhT0I8PEA8W2tLY2IpS2FfMC5RK0ZoMFtwQS9UX1JeJ2VSMFtmSyljPTtXQ0BiJWxaYzBGbSkmLzJvRU41WCJDY21EWDFrVExXLGFJS11KOGtQIzEnQGIpdV9lSEo7Xm1OaGxWJTYwI3BYS0AwIVUuY3UoOldcaFc4UVw2YUMrUDxPZWQyKTlLNXI6NUZmbzkjKW5XRk41ZlhnSEIoIW5IUUoyVWI+Q1pmVTknTVpQU19TXCo+LTBDM28uQXNAIUQ1YChtOTE2N15rQSZfLkxDZ3VYOkZfW1c3JUxuJ1QwKyhWO0BEdElCRyclJksqVlVON0RjXyZQSl5Ibl9SYV5fPHVPO2ZbbFctJTxXOkQhISwsKjZQcyguZEEzPmQ0LmxnJ2FJRU9sODNeLVcqXi43IUNTZkJ0KTdMUjFIXFAoUlBVLGlDRy00YGYqYy8qTSRcZl9aWExDdGRZTDEiXUVkcCFsOWUuYlRMM19DOFJdV05DTiI8blM5OylkUV0kZVZSX1RaJ0MzMHVXUmNoTnFsJi9kPFRbRnIxRjFsOz43Mz4wOjksPF07ckRdNWBbXilsOmkwSz5EW2lJJi5aVDtsX2toKyZZJT9lXW1qaUpHWXVMKTs2Lj05QDEsUjFKTmxPcStrVV4nOjpmUDRKX2lRYCwqZWNSU1BnME9nPTImZmBTWEpTPVxLO0ZyVk5CNHI1aFcpLkdnPkU3Ql1uM1lJcjlhQCU7Uj46ZWxUVmBDdGQoMz02M1YobCkjRkhSbGtnUkhvSC4pSXVVL0Q8WUZmcWZVZ2xYPDFWVlVyaD51QyFtKWZDZFAmZ3AqRE1hPWZzSVAsYENdVjgqIkExbFNmNCo3UTIlUUpObVNyOiMqVU9JUWdTUGFSYm42PU0oXEZgNTFAc1NELT0iWChyKl8vY1U4bDNVRT5QPmwxb0g+cSNUKjw3J1Y5V2tZT1A6R1JmLDNTZWNUTmg5JDxubGBcT1BcKU5mN2xiSiRvVyRZPkEqNE9UIidHWVppK0JvV2RNKEhoViEqSD06aS5EciNHbmZSOWMpTDQuSCxHN0JZNTxZQUVGcTE6U0EmLWZBREIsUnNObSFEcS4vWEFpT1BRdG4+MCxCbXRpJU46Sm4hbTk/aHFmUk5EJiolbDw/Lz8qVFQnYSVMODtGTy0zQ15XSVgiKEZSSWVBXkhSWCJcPSZhLUVdLicwO2hTUDA6cUMpSVZnSCshJCltLExRMF09XzlLXSdDWkFgIUV0YC9fVHRLbEdSRVFJVyJjaHIpMDNyNmlsO19eLiZHbSYjU0hDJV1JPGk+XEBLSXJRRCpNZUI1UGlIOW49XEFvT2g0SE1RV0NIM1dmb2olK0RYLVo7QkczOnMkQGVnY2khSEFEKzQvR3Q4YFtILnNfcCokNEteJS05PUs9bi9vIWE3dD5tOHBQX15eN0xmXSRHPU1dcDU0blw4NyVWXD09KDxlWkd0NEZWZVNkMWBuZzdnXmtUWCk9JChtPjZ0O0xCMi8yRiRbR0I4TyU6WT9TYXElQFxdI25jRF08JVstZFdPUEBoYCohMVFLOk1jWylIT0QyPl0nMV49VjxcN2Y5LnAvWWlZVl5LXV44SENONjlCUFAsWS1JQFcmOzIzZWJZcCNhPCNjWWtEPVhYZmhIXD5fRlJQaFxnbEdpaGIsQXRGNyhmXWkrNDJiMmBhQD9vXHExXzBCPStSMCE4VTYkX2FsLl5zXC9eUXNkJXFrU2hedEM0amYwWHJGRzNYJShAZTRtczRMS3NkZidQV0R1QWE8Om08KEdCUmxrJiQubypwKj1dLnF0WTNQRVkjaltoVVdeL2xIbmBQNTZsJmAzOyxaR3VgOkg2JEh0JCpdR0tjTHNGRGU5QT9aIlYiV0kkUk9pW1MrbT9laDxzJGZKW0ohZGhmPC5WYGtyWWM2WDojNmFQQXNfIy5gaWI1NUAiLi8jVUJJNH4+ZW5kc3RyZWFtCmVuZG9iagp4cmVmCjAgMTIKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDYxIDAwMDAwIG4gCjAwMDAwMDAxMTIgMDAwMDAgbiAKMDAwMDAwMDIxOSAwMDAwMCBuIAowMDAwMDAwMzMxIDAwMDAwIG4gCjAwMDAwMDA0MzYgMDAwMDAgbiAKMDAwMDAwMDY0MCAwMDAwMCBuIAowMDAwMDAwODQ0IDAwMDAwIG4gCjAwMDAwMDA5MTIgMDAwMDAgbiAKMDAwMDAwMTIwOCAwMDAwMCBuIAowMDAwMDAxMjczIDAwMDAwIG4gCjAwMDAwMDQ0NzcgMDAwMDAgbiAKdHJhaWxlcgo8PAovSUQgCls8NTJjOTU1ZTg4MmJlZmJkNTAwYmFhNDllNWQzYzg3Yjc+PDUyYzk1NWU4ODJiZWZiZDUwMGJhYTQ5ZTVkM2M4N2I3Pl0KJSBSZXBvcnRMYWIgZ2VuZXJhdGVkIFBERiBkb2N1bWVudCAtLSBkaWdlc3QgKG9wZW5zb3VyY2UpCgovSW5mbyA4IDAgUgovUm9vdCA3IDAgUgovU2l6ZSAxMgo+PgpzdGFydHhyZWYKNzA5MQolJUVPRgo=', 'base64'));

-- ------------------------------------------------------------
-- "group"
-- ------------------------------------------------------------
INSERT INTO "group" (id, name, status, created_at, updated_at)
VALUES (1, 'Frontendi meeskond', 'A', now() - interval '90 days', now()),
       (2, 'Backendi meeskond', 'A', now() - interval '90 days', now()),
       (3, 'Testijate meeskond', 'A', now() - interval '90 days', now());

-- ------------------------------------------------------------
-- group_manager
-- ------------------------------------------------------------
INSERT INTO group_manager (id, group_id, user_id, status)
VALUES (1, 1, 2, 'A'), -- manager manages Frontend
       (2, 2, 2, 'A'), -- manager manages Backend
       (3, 3, 1, 'A');
-- admin manages QA

-- ------------------------------------------------------------
-- group_member
-- ------------------------------------------------------------
INSERT INTO group_member (id, group_id, user_id, added_by, created_at, updated_at)
VALUES (1, 1, 3, 2, now() - interval '29 days', now() - interval '29 days'),
       (2, 2, 3, 2, now() - interval '20 days', now() - interval '20 days'),
       (3, 1, 1, 2, now() - interval '89 days', now() - interval '89 days');

-- ------------------------------------------------------------
-- invitation
-- ------------------------------------------------------------
INSERT INTO invitation (id, invited_by, user_id, token, created_at, expires_at, status)
VALUES (1, 1, 4, 'inv_tok_abc123pending', now() - interval '2 days', now() + interval '5 days', 'P'),
       (2, 1, 2, 'inv_tok_xyz987completed', now() - interval '61 days', now() - interval '54 days', 'C');

-- ------------------------------------------------------------
-- profile  (only for users who completed registration: 1, 2, 3)
-- ------------------------------------------------------------
INSERT INTO profile (id, user_id, first_name, last_name, phone_number, created_at, updated_at)
VALUES (1, 1, 'Anna', 'Admin', '+37255512345', now() - interval '90 days', now()),
       (2, 2, 'Marko', 'Haldur', '+37255598765', now() - interval '60 days', now()),
       (3, 3, 'Kati', 'Kasutaja', NULL, now() - interval '30 days', now());

-- ------------------------------------------------------------
-- question_type
-- ------------------------------------------------------------
INSERT INTO question_type (id, name)
VALUES (1, 'SINGLE_CHOICE'),
       (2, 'MULTIPLE_CHOICE'),
       (3, 'TRUE_FALSE');

-- ------------------------------------------------------------
-- question
-- ------------------------------------------------------------
INSERT INTO question (id, competence_id, competence_level_id, title, description, question_type_id, score, status,
                      created_at, created_by, updated_at)
VALUES (1, 1, 1, 'Mis on sulund (closure)?', 'Vali JavaScripti sulundi kõige täpsem definitsioon.', 1, 10, 'A',
        now() - interval '80 days', 1, now()),
       (2, 1, 2, 'Millised järgnevatest on JS primitiivtüübid?', 'Vali kõik JavaScripti primitiivtüübid.', 2, 10, 'A',
        now() - interval '75 days', 1, now()),
       (3, 2, 3, 'Kas SQL-i võtmesõnad on tõstutundlikud?', 'Tõene või väär.', 3, 5, 'A', now() - interval '70 days', 1,
        now()),
       (4, 1, 1, 'Mis vahe on let ja const vahel?', 'Vali õige väide.', 1, 10, 'A', now() - interval '60 days', 1,
        now()),
       (5, 1, 1, 'Mida tagastab typeof null?', 'Vali õige vastus.', 1, 10, 'A', now() - interval '60 days', 1, now()),
       (6, 2, 3, 'Milline käsk tagastab tabelist andmeid?', 'Vali õige SQL-i käsk.', 1, 10, 'A',
        now() - interval '55 days', 1, now()),
       (7, 2, 3, 'Kas WHERE-tingimus filtreerib ridu enne GROUP BY-d?', 'Tõene või väär.', 3, 5, 'A',
        now() - interval '55 days', 1, now()),
       (8, 3, 4, 'Mis on aktiivne kuulamine?', 'Vali kõige täpsem kirjeldus.', 1, 10, 'A', now() - interval '50 days', 1,
        now()),
       (9, 3, 4, 'Milline e-kirja pealkiri on kõige selgem?', 'Vali parim näide.', 1, 10, 'A',
        now() - interval '50 days', 1, now()),
       -- Vue.js
       (10, 4, 5, 'Milline direktiiv seob sisendvälja väärtuse kahesuunaliselt andmetega?',
        'Vali Vue direktiiv, mis hoiab sisendvälja ja komponendi andmed omavahel sünkroonis.', 1, 10, 'A',
        now() - interval '30 days', 1, now()),
       (11, 4, 6, 'Millised väited computed omaduste kohta on õiged?',
        'Vali kõik õiged väited Vue computed omaduste kohta.', 2, 10, 'A', now() - interval '30 days', 1, now()),
       (12, 4, 7, 'Kas alamkomponent tohib props väärtust otse muuta?',
        'Tõene või väär: alamkomponent võib vanemalt saadud props väärtust otse üle kirjutada.', 3, 5, 'A',
        now() - interval '30 days', 1, now()),
       -- Spring Boot
       (13, 5, 8, 'Milline annotatsioon märgib klassi REST kontrolleriks?',
        'Vali annotatsioon, mis teeb klassist REST endpointe pakkuva kontrolleri.', 1, 10, 'A',
        now() - interval '30 days', 1, now()),
       (14, 5, 9, 'Millised annotatsioonid seovad HTTP päringu andmed meetodi parameetriga?',
        'Vali kõik annotatsioonid, millega saab päringust andmeid kontrolleri meetodisse.', 2, 10, 'A',
        now() - interval '30 days', 1, now()),
       (15, 5, 10, 'Kas @Transactional meetodis visatud RuntimeException tühistab vaikimisi tehingu?',
        'Tõene või väär: Springi vaikekäitumisel tehakse kontrollimata erindi korral rollback.', 3, 5, 'A',
        now() - interval '30 days', 1, now()),
       -- Git
       (16, 6, 11, 'Milline käsk loob uue haru ja lülitub kohe sellele?',
        'Vali käsk, mis teeb mõlemat korraga.', 1, 10, 'A', now() - interval '30 days', 1, now()),
       (17, 6, 12, 'Millised käsud muudavad kohalikku tööpuud või haru ajalugu?',
        'Vali kõik käsud, mis muudavad kohalikke faile või harusid (mitte ainult ei loe infot).', 2, 10, 'A',
        now() - interval '30 days', 1, now()),
       (18, 6, 13, 'Kas jagatud haru rebase ja force push võib teiste arendajate töö segamini ajada?',
        'Tõene või väär: juba pushitud ja teistega jagatud haru ajaloo ümberkirjutamine võib teistele probleeme tekitada.',
        3, 5, 'A', now() - interval '30 days', 1, now()),
       -- Kommunikatsioon
       (19, 3, 14, 'Millised võtted aitavad konfliktset arutelu rahulikult lahendada?',
        'Vali kõik võtted, mis aitavad meeskonnas tekkinud pingelises arutelus jõuda lahenduseni.', 2, 10, 'A',
        now() - interval '30 days', 1, now()),
       (20, 3, 15, 'Kas tagasiside on kõige tõhusam, kui see keskendub isiku omadustele, mitte käitumisele?',
        'Tõene või väär: hea tagasiside hindab inimest ennast, mitte tema konkreetset tegevust või tulemust.', 3, 5, 'A',
        now() - interval '30 days', 1, now()),
       -- Vali-IT: backendi koodi head tavad (rAIn-i juhendamise põhitõed)
       (21, 7, 16, 'Millises järjekorras ehitame GET teenuse kihtide kaupa üles?',
        'Mõtle, kuidas me live-coding sessioonis alustame — kust tuleb esimene samm ja kuhu me lõpuks tagasi jõuame?', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (22, 7, 16, 'Kas RestController võib vastusena tagastada otse entiteedi?',
        'Tõene või väär? Mõtle hetkeks, mida kontroller meie projektis üldse näha tohib — entiteete või DTO-sid?', 3, 5, 'A', now() - interval '1 days', 1, now()),
       (23, 7, 16, 'Mapperi signatuur on olemas ja Ctrl+Space näitas tühja malli. Mis saab nüüd iga target-väljaga?',
        'See on koht, kus näen õpilasi tihti mõne välja unustamas. Vali õige tava.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (24, 7, 16, 'Kutsusid välja meetodi, mis midagi tagastab, ja tahad selle infoga edasi töötada. Mida teed?',
        'Meenuta „meetodi palvet“.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (25, 7, 16, 'Repository tagastas Optional<MingiEntiteet>. Kuidas seda käsitled?',
        'Ära lase Optional-il lihtsalt seista — otsusta teadlikult, mis saab siis, kui väärtust pole. Vali kõik õiged valikud.', 2, 10, 'A', now() - interval '1 days', 1, now()),
       (26, 7, 16, 'Kus elab projektis rida repositoorium.findById(mingiId).orElseThrow(...)?',
        'Vihje: mõtle, kust saaks iga teine service sama kontrolli taaskasutada, ilma et peaks koodi kopeerima.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (27, 7, 16, 'Milline muutuja nimi on projekti tava järgi õige?',
        'Pisike asi, aga ma vaatan seda alati! Hea nimi ütleb kohe, mis tüüpi asjaga tegu on.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (28, 7, 16, 'Meetod lisab DTO-le andmed ainult siis, kui need on olemas. Kuidas selle nimetad?',
        'Meetodis on tingimus ja see muudab DTO-d. Nimi peaks seda kohe reetma — milline sobib?', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (29, 7, 16, 'Kood töötab — nüüd make it beautiful. Millised väited meetodite järjekorra kohta on õiged?',
        'Nüüd vaatame koodi ilu poole pealt. Vali kõik õiged väited.', 2, 10, 'A', now() - interval '1 days', 1, now()),
       (30, 7, 16, 'Kas enne iga taski alustamist võtad masterist uue branchi?',
        'Tõene või väär? See on esimene asi, mida ma enne iga taski alustamist sinult küsin 😉', 3, 5, 'A', now() - interval '1 days', 1, now()),
       (31, 7, 16, 'Kas @Mapping annotatsioonid käivad ka list-meetodile?',
        'Tõene või väär? Näen seda tihti: @Mapping-read kirjutatakse meetodile List<MingiDto> toMingiDtos(List<MingiEntiteet> mingiEntiteedid).', 3, 5, 'A', now() - interval '1 days', 1, now()),
       (32, 7, 16, 'Kuidas nimetad mapperi meetodi, mis teisendab terve listi?',
        'Vaata nime lõppu — see on üks levinumaid pisivigu, mida koodi üle vaadates leian.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       -- Java / Juunior (competence_level 17) — test 9 "Java algtaseme test"
       (33, 8, 17, 'Mis vahe on klassil ja objektil?', 'Vali kõige täpsem vastus.', 1, 10, 'A', now() - interval '1 days', 1, now()),
       (34, 8, 17, 'Millised järgnevatest on Java primitiivtüübid?', 'Vali kõik Java primitiivtüübid.', 2, 10, 'A',
        now() - interval '1 days', 1, now()),
       (35, 8, 17, 'Kas sõnede (String) sisu võrdlemiseks sobib operaator ==?',
        'Tõene või väär? Mõtle, mida == objektide puhul tegelikult võrdleb.', 3, 5, 'A', now() - interval '1 days', 1, now());

-- ------------------------------------------------------------
-- question_answer
-- ------------------------------------------------------------
INSERT INTO question_answer (id, question_id, answer_text, correct_choice, correct_position, paired_answer_id, status)
VALUES (1, 1, 'Funktsioon, mis mäletab oma leksikaalset skoopi', true, NULL, NULL, 'A'),
       (2, 1, 'Tsükkel, mis ei lõppe kunagi', false, NULL, NULL, 'A'),
       (3, 1, 'CSS-i omadus', false, NULL, NULL, 'A'),
       (4, 2, 'string', true, NULL, NULL, 'A'),
       (5, 2, 'number', true, NULL, NULL, 'A'),
       (6, 2, 'massiiv', false, NULL, NULL, 'A'),
       (7, 2, 'objekt', false, NULL, NULL, 'A'),
       (8, 3, 'Tõene', false, NULL, NULL, 'A'),
       (9, 3, 'Väär', true, NULL, NULL, 'A'),
       (10, 4, 'const muutujale ei saa uut väärtust omistada', true, NULL, NULL, 'A'),
       (11, 4, 'let muutuja on alati globaalne', false, NULL, NULL, 'A'),
       (12, 4, 'Nende vahel ei ole vahet', false, NULL, NULL, 'A'),
       (13, 5, '"object"', true, NULL, NULL, 'A'),
       (14, 5, '"null"', false, NULL, NULL, 'A'),
       (15, 5, '"undefined"', false, NULL, NULL, 'A'),
       (16, 6, 'SELECT', true, NULL, NULL, 'A'),
       (17, 6, 'INSERT', false, NULL, NULL, 'A'),
       (18, 6, 'UPDATE', false, NULL, NULL, 'A'),
       (19, 7, 'Tõene', true, NULL, NULL, 'A'),
       (20, 7, 'Väär', false, NULL, NULL, 'A'),
       (21, 8, 'Kõnelejale täielik keskendumine ja tagasiside andmine', true, NULL, NULL, 'A'),
       (22, 8, 'Vestluskaaslase katkestamine oma mõtte jagamiseks', false, NULL, NULL, 'A'),
       (23, 8, 'Samal ajal e-kirjadele vastamine', false, NULL, NULL, 'A'),
       (24, 9, 'Kohtumise aja muutus: neljapäev kell 14', true, NULL, NULL, 'A'),
       (25, 9, 'Küsimus', false, NULL, NULL, 'A'),
       (26, 9, 'Tere!', false, NULL, NULL, 'A'),
       -- Vue.js
       (27, 10, 'v-model', true, NULL, NULL, 'A'),
       (28, 10, 'v-bind', false, NULL, NULL, 'A'),
       (29, 10, 'v-if', false, NULL, NULL, 'A'),
       (30, 10, 'v-for', false, NULL, NULL, 'A'),
       (31, 11, 'Computed omaduse väärtus puhverdatakse ja arvutatakse uuesti alles siis, kui sõltuvused muutuvad', true, NULL, NULL, 'A'),
       (32, 11, 'Computed omadust kasutatakse mallis nagu tavalist andmevälja, ilma sulgudeta', true, NULL, NULL, 'A'),
       (33, 11, 'Computed omadus käivitub igal renderdamisel uuesti nagu meetod', false, NULL, NULL, 'A'),
       (34, 11, 'Computed omaduses on hea teha API päringuid', false, NULL, NULL, 'A'),
       (35, 12, 'Tõene', false, NULL, NULL, 'A'),
       (36, 12, 'Väär', true, NULL, NULL, 'A'),
       -- Spring Boot
       (37, 13, '@RestController', true, NULL, NULL, 'A'),
       (38, 13, '@Service', false, NULL, NULL, 'A'),
       (39, 13, '@Repository', false, NULL, NULL, 'A'),
       (40, 13, '@Entity', false, NULL, NULL, 'A'),
       (41, 14, '@PathVariable', true, NULL, NULL, 'A'),
       (42, 14, '@RequestParam', true, NULL, NULL, 'A'),
       (43, 14, '@RequestBody', true, NULL, NULL, 'A'),
       (44, 14, '@Autowired', false, NULL, NULL, 'A'),
       (45, 15, 'Tõene', true, NULL, NULL, 'A'),
       (46, 15, 'Väär', false, NULL, NULL, 'A'),
       -- Git
       (47, 16, 'git checkout -b uus-haru', true, NULL, NULL, 'A'),
       (48, 16, 'git branch uus-haru', false, NULL, NULL, 'A'),
       (49, 16, 'git commit -b uus-haru', false, NULL, NULL, 'A'),
       (50, 16, 'git push uus-haru', false, NULL, NULL, 'A'),
       (51, 17, 'git merge', true, NULL, NULL, 'A'),
       (52, 17, 'git rebase', true, NULL, NULL, 'A'),
       (53, 17, 'git status', false, NULL, NULL, 'A'),
       (54, 17, 'git log', false, NULL, NULL, 'A'),
       (55, 18, 'Tõene', true, NULL, NULL, 'A'),
       (56, 18, 'Väär', false, NULL, NULL, 'A'),
       -- Kommunikatsioon
       (57, 19, 'Kuulata teise poole seisukoht lõpuni ära ja võtta see oma sõnadega kokku', true, NULL, NULL, 'A'),
       (58, 19, 'Rääkida oma vajadustest mina-sõnumitega, näiteks „Mul on raske, kui…"', true, NULL, NULL, 'A'),
       (59, 19, 'Otsida mõlemale poolele sobivaid lahendusi, mitte süüdlast', true, NULL, NULL, 'A'),
       (60, 19, 'Tõsta häält, et oma seisukoht selgelt kõlama jääks', false, NULL, NULL, 'A'),
       (61, 20, 'Tõene', false, NULL, NULL, 'A'),
       (62, 20, 'Väär', true, NULL, NULL, 'A'),
       -- Vali-IT
       (63, 21, 'RestController → Service → Repository → Service → Mapper → RestController', true, NULL, NULL, 'A'),
       (64, 21, 'Repository → Mapper → Service → RestController', false, NULL, NULL, 'A'),
       (65, 21, 'Mapper → RestController → Service → Repository', false, NULL, NULL, 'A'),
       (66, 21, 'RestController → Mapper → Repository → Service', false, NULL, NULL, 'A'),
       (67, 22, 'Tõene', false, NULL, NULL, 'A'),
       (68, 22, 'Väär', true, NULL, NULL, 'A'),
       (69, 23, 'Igal target-väljal on kas source = "..." või ignore = true — ka siis, kui nimi kattub', true, NULL, NULL, 'A'),
       (70, 23, 'Kirjutan @Mapping-u ainult väljadele, mille nimi erineb', false, NULL, NULL, 'A'),
       (71, 23, 'Jätan read tühjaks, MapStruct saab ise hakkama', false, NULL, NULL, 'A'),
       (72, 23, 'Puuduvad väljad täidan hiljem RestControlleris', false, NULL, NULL, 'A'),
       (73, 24, 'TagastatavTüüp muutujaNimi = teenuseMuutuja.meetodiNimi(parameeter);', true, NULL, NULL, 'A'),
       (74, 24, 'teenuseMuutuja.meetodiNimi(parameeter); — tulemus jääb ise meelde', false, NULL, NULL, 'A'),
       (75, 24, 'Kutsun sama meetodi uuesti välja iga kord, kui väärtust vajan', false, NULL, NULL, 'A'),
       (76, 24, 'Panen tulemuse klassi staatilisse välja', false, NULL, NULL, 'A'),
       (77, 25, 'Kui väärtus on kohustuslik: .orElseThrow(() -> new MingiException(...))', true, NULL, NULL, 'A'),
       (78, 25, 'Kui puudumine on lubatud: .orElse(...) või isPresent() kontroll', true, NULL, NULL, 'A'),
       (79, 25, 'Jätan Optional-i nii, Spring teeb ülejäänu ise', false, NULL, NULL, 'A'),
       (80, 25, 'Kutsun alati .get(), kontrolli pole vaja', false, NULL, NULL, 'A'),
       (81, 26, 'MingiService klassi public getValidMingiBy(Integer mingiId) meetodis', true, NULL, NULL, 'A'),
       (82, 26, 'Otse RestControlleri meetodis', false, NULL, NULL, 'A'),
       (83, 26, 'Mapperi liideses', false, NULL, NULL, 'A'),
       (84, 26, 'Kopeerin selle iga service meetodi sisse eraldi', false, NULL, NULL, 'A'),
       (85, 27, 'MingiDetailDto mingiDetailDto', true, NULL, NULL, 'A'),
       (86, 27, 'MingiDetailDto dto', false, NULL, NULL, 'A'),
       (87, 27, 'MingiDetailDto d', false, NULL, NULL, 'A'),
       (88, 27, 'MingiDetailDto tulemus', false, NULL, NULL, 'A'),
       (89, 28, 'handleLisaMidagi(MingiDto mingiDto, Integer mingiId)', true, NULL, NULL, 'A'),
       (90, 28, 'getLisaMidagi(MingiDto mingiDto, Integer mingiId)', false, NULL, NULL, 'A'),
       (91, 28, 'findLisaMidagi(MingiDto mingiDto, Integer mingiId)', false, NULL, NULL, 'A'),
       (92, 28, 'isLisaMidagi(MingiDto mingiDto, Integer mingiId)', false, NULL, NULL, 'A'),
       (93, 29, 'public meetodid tulevad enne private meetodeid', true, NULL, NULL, 'A'),
       (94, 29, 'Peameetod on üleval, tema helper-meetodid allpool', true, NULL, NULL, 'A'),
       (95, 29, 'Meetodid järjestatakse tähestiku järgi', false, NULL, NULL, 'A'),
       (96, 29, 'private meetodid käivad klassi algusesse', false, NULL, NULL, 'A'),
       (97, 30, 'Tõene', true, NULL, NULL, 'A'),
       (98, 30, 'Väär', false, NULL, NULL, 'A'),
       (99, 31, 'Tõene', false, NULL, NULL, 'A'),
       (100, 31, 'Väär', true, NULL, NULL, 'A'),
       (101, 32, 'List<MingiDto> toMingiDtos(List<MingiEntiteet> mingiEntiteedid)', true, NULL, NULL, 'A'),
       (102, 32, 'List<MingiDto> toMingiDto(List<MingiEntiteet> mingiEntiteedid)', false, NULL, NULL, 'A'),
       (103, 32, 'List<MingiDto> mingiList(List<MingiEntiteet> mingiEntiteedid)', false, NULL, NULL, 'A'),
       (104, 32, 'List<MingiDto> mapAll(List<MingiEntiteet> mingiEntiteedid)', false, NULL, NULL, 'A'),
       -- Java / Juunior küsimused 33-35
       (105, 33, 'Klass on mall, objekt on selle põhjal loodud konkreetne eksemplar', true, NULL, NULL, 'A'),
       (106, 33, 'Objekt on mall, klass on selle põhjal loodud eksemplar', false, NULL, NULL, 'A'),
       (107, 33, 'Klass ja objekt tähendavad Javas sama asja', false, NULL, NULL, 'A'),
       (108, 33, 'Klassis on ainult meetodid, objektis ainult väljad', false, NULL, NULL, 'A'),
       (109, 34, 'int', true, NULL, NULL, 'A'),
       (110, 34, 'boolean', true, NULL, NULL, 'A'),
       (111, 34, 'double', true, NULL, NULL, 'A'),
       (112, 34, 'String', false, NULL, NULL, 'A'),
       (113, 35, 'Tõene', false, NULL, NULL, 'A'),
       (114, 35, 'Väär', true, NULL, NULL, 'A');

-- ------------------------------------------------------------
-- test
-- ------------------------------------------------------------
INSERT INTO test (id, competence_id, competence_level_id, name, short_description, description, is_timed, timer_min, pass_percent,
                  round_score_up, status, created_by, created_at, updated_at)
VALUES (1, 1, 1, 'JavaScripti algtaseme test', 'JavaScripti algteadmiste kontroll.', 'Test algajatele JS-i baasteadmiste kontrollimiseks.', true, 30, 60.00, true, 'A', 1,
        now() - interval '75 days', now()),
       (2, 1, 2, 'JavaScripti kesktaseme test', 'JavaScripti süvitsi minevad teemad.', 'Kesktaseme test JS-i süvitsi minevate teemade kohta.', true, 45, 70.00, false, 'A', 1,
        now() - interval '70 days', now()),
       (3, 2, 3, 'SQL-i aluste test', 'SQL-i põhiteadmiste kontroll.', 'Lühike test SQL-i põhiteadmiste hindamiseks.', false, NULL, 65.00, true, 'A', 1,
        now() - interval '65 days', now()),
       (4, 3, 4, 'Suhtlemise algtaseme test', 'Professionaalse suhtluse põhitõed.', 'Test aktiivse kuulamise ja selge kirjaliku suhtluse kohta.', false, NULL, 50.00, true, 'A', 1,
        now() - interval '10 days', now()),
       (5, 2, 3, 'SQL-päringute test', 'SQL-päringute praktilised teadmised.', 'Test SELECT-päringute, filtreerimise ja SQL-i süntaksi kohta.', true, 15, 60.00, false, 'A', 1,
        now() - interval '10 days', now()),
       -- Maksimaalse pikkusega pealkiri (255) ja lühikirjeldus (150) — kaartide ja testi vaate paigutuse kontrolliks
       (6, 4, 6, 'Vue.js-i kesktaseme põhjalik test: komponentide ülesehitus, reaktiivsus, computed omadused ja jälgijad, elutsükli konksud, props ja emit sündmused, slotid, Vue Router, olekuhaldus Pinia abil ning komponentide jõudluse optimeerimine suuremates veebiäppides',
        'Põhjalik kontroll Vue 3 komponentide, reaktiivsuse, marsruutimise ja olekuhalduse kohta kesktaseme arendajale, kes ehitab iseseisvalt päris rakendusi.',
        E'Test on mõeldud arendajale, kes on Vue 3-ga juba mõne päris projekti teinud ja tahab oma teadmisi süvendada. Küsimused katavad komponentide ülesehitust, reaktiivsuse põhimõtteid (ref, reactive, computed ja watch), elutsükli konkse ning komponentidevahelist suhtlust props-ide, emit sündmuste ja slotide abil.\n\nLisaks kontrollitakse Vue Routeri kasutamist (dünaamilised marsruudid, navigeerimise kaitsed, props-ide edastamine marsruudilt) ning olekuhaldust Pinia store''ide abil. Mitmes küsimuses tuleb hinnata, kas antud koodinäide töötab ootuspäraselt või tekitab märkamatu vea, näiteks props-i otsese muutmise või reaktiivsuse kadumise tõttu.\n\nTesti läbimiseks on aega 40 minutit ja taimer käivitub kohe, kui test avatakse. Vastuseid saab enne esitamist muuta, kuid pärast esitamist enam mitte. Test loetakse sooritatuks, kui kogud vähemalt 75% maksimaalsest punktisummast.',
        true, 40, 75.00, false, 'A', 1, now() - interval '5 days', now()),
       (7, 6, 11, 'Giti algtaseme test', 'Giti igapäevased põhikäsud.', 'Lühike test harude loomise, commitimise ja põhiliste Giti käskude kohta.', false, NULL, 50.00, true, 'A', 2,
        now() - interval '2 days', now()),
       (8, 7, 16, 'Backendi koodi head tavad',
        'Vaatame koos, kas rAIn-i põhitõed on käpas: kihid, DTO-d, mapperid, nimetamine ja Optional. Sa saad sellega hakkama! 💪',
        E'Tere! Mina olen rAIn 👋 Selles testis vaatame üle need asjad, mida ma sulle iga backend taski juures ikka ja jälle meelde tuletan — täpselt nagu siis, kui istume koos ja kirjutame koodi.\n\nKüsin sinult kihtide järjekorra, DTO-de ja mapperite, nimetamistavade (getValid…By, handle…), Optional-i ja meetodite järjekorra kohta. Need on kohad, kus näen õpilasi kõige sagedamini komistamas — ja ausalt öeldes, kunagi komistasin seal ka ise.\n\nAega on sul 20 minutit ja läbimiseks piisab 50%-st. Ära muretse, kui mõni vastus kohe meelde ei tule — see test on harjutamiseks. Edu! 🚀',
        true, 20, 50.00, true, 'A', 1, now() - interval '1 days', now()),
       (9, 8, 17, 'Java algtaseme test', 'Java põhiteadmiste kontroll.',
        'Test algajatele Java klasside ja objektide, primitiivtüüpide ning sõnede võrdlemise kohta.', true, 20, 60.00, true, 'A', 1,
        now() - interval '1 days', now());
-- ------------------------------------------------------------
-- test_question
-- ------------------------------------------------------------
INSERT INTO test_question (id, test_id, question_id, position, added_by, created_at, updated_at)
VALUES (1, 1, 1, 1, 1, now() - interval '75 days', now()),
       (2, 1, 2, 2, 1, now() - interval '75 days', now()),
       (3, 2, 2, 1, 1, now() - interval '70 days', now()),
       (4, 3, 3, 1, 1, now() - interval '65 days', now()),
       (5, 4, 8, 1, 1, now() - interval '10 days', now()),
       (6, 4, 9, 2, 1, now() - interval '10 days', now()),
       (7, 5, 6, 1, 1, now() - interval '10 days', now()),
       (8, 5, 7, 2, 1, now() - interval '10 days', now()),
       (9, 5, 3, 3, 1, now() - interval '10 days', now()),
       (10, 6, 11, 1, 1, now() - interval '5 days', now()),
       (11, 7, 16, 1, 2, now() - interval '2 days', now()),
       (12, 8, 21, 1, 1, now() - interval '1 days', now()),
       (13, 8, 22, 2, 1, now() - interval '1 days', now()),
       (14, 8, 23, 3, 1, now() - interval '1 days', now()),
       (15, 8, 24, 4, 1, now() - interval '1 days', now()),
       (16, 8, 25, 5, 1, now() - interval '1 days', now()),
       (17, 8, 26, 6, 1, now() - interval '1 days', now()),
       (18, 8, 27, 7, 1, now() - interval '1 days', now()),
       (19, 8, 28, 8, 1, now() - interval '1 days', now()),
       (20, 8, 29, 9, 1, now() - interval '1 days', now()),
       (21, 8, 30, 10, 1, now() - interval '1 days', now()),
       (22, 8, 31, 11, 1, now() - interval '1 days', now()),
       (23, 8, 32, 12, 1, now() - interval '1 days', now()),
       (24, 9, 33, 1, 1, now() - interval '1 days', now()),
       (25, 9, 34, 2, 1, now() - interval '1 days', now()),
       (26, 9, 35, 3, 1, now() - interval '1 days', now());

-- ------------------------------------------------------------
-- user_test (assignments) — 10 currently open, 3 completed
-- NB! Kasutajal võib sama testi kohta olla ainult üks avatud ('O') määramine,
-- muidu tagastab UserTestRepository.getValidUserTestBy mitu rida ja backend vastab 500.
-- ------------------------------------------------------------
INSERT INTO user_test (id, test_id, user_id, opens_at, closes_at, status, group_id, assigned_by, created_at)
VALUES (1, 1, 3, now(), now() + interval '7 days', 'O', 1, 2, now()),
       (2, 3, 3, now() - interval '3 days', now() + interval '4 days', 'O', 1, 2, now() - interval '3 days'),
       (3, 1, 1, now() - interval '10 days', now() - interval '3 days', 'C', 2, 2, now() - interval '10 days'),
       (4, 2, 2, now() - interval '15 days', now() - interval '8 days', 'C', 2, 1, now() - interval '15 days'),
       (5, 3, 1, now() - interval '20 days', now() - interval '13 days', 'C', 1, 2, now() - interval '20 days'),
       -- Puhtad avatud testid frontendi testimiseks (ilma result kirjeta)
       (6, 1, 1, now(), now() + interval '7 days', 'O', 1, 2, now()),
       (7, 4, 1, now(), now() + interval '7 days', 'O', 1, 2, now()),
       (8, 5, 1, now(), now() + interval '7 days', 'O', 1, 2, now()),
       (9, 2, 3, now(), now() + interval '7 days', 'O', 2, 2, now()),
       (10, 4, 3, now(), now() + interval '7 days', 'O', 1, 2, now()),
       (11, 5, 3, now(), now() + interval '7 days', 'O', 2, 2, now()),
       -- Vali-IT "Backendi koodi head tavad" — määratud kõigile adminitele
       (12, 8, 1, now(), now() + interval '30 days', 'O', 1, 2, now()),
       -- "Java algtaseme test" (Java / Juunior) — määratud adminile
       (13, 9, 1, now(), now() + interval '7 days', 'O', 2, 2, now());

-- ------------------------------------------------------------
-- result  (only for the 3 completed user_test rows: 3, 4, 5)
-- ------------------------------------------------------------
INSERT INTO result (id, user_test_id, status, max_score, score_total, completed_at, started_at, total_questions,
                    questions_answered)
VALUES (1, 3, 'P', 20, 20, now() - interval '3 days', now() - interval '3 days' - interval '25 min', 2, 2),
       (2, 4, 'P', 10, 10, now() - interval '8 days', now() - interval '8 days' - interval '15 min', 1, 1),
       (3, 5, 'F', 5, 0, now() - interval '13 days', now() - interval '13 days' - interval '5 min', 1, 1);

-- ------------------------------------------------------------
-- test_question_result  (per-question breakdown of each result)
-- ------------------------------------------------------------
INSERT INTO test_question_result (id, result_id, question_id, correct_answer, status, sequence_number)
VALUES (1, 1, 1, true, 'OK', 1),
       (2, 1, 2, true, 'OK', 2),
       (3, 2, 2, true, 'OK', 1),
       (4, 3, 3, false, 'OK', 1);

-- ------------------------------------------------------------
-- test_question_answer  (which specific answer(s) the user picked)
-- ------------------------------------------------------------
INSERT INTO test_question_answer (id, test_question_result_id, question_answer_id, correct_choice_user_answer,
                                  correct_position_user_answer, paired_answer_id_user_answer)
VALUES (1, 1, 1, true, NULL, NULL), -- correctly picked closure definition
       (2, 2, 4, true, NULL, NULL), -- picked "string" for primitive types
       (3, 2, 5, true, NULL, NULL), -- picked "number" for primitive types
       (4, 3, 4, true, NULL, NULL), -- manager's run of test 2, same correct picks
       (5, 3, 5, true, NULL, NULL),
       (6, 4, 8, false, NULL, NULL);
-- picked "Tõene" — wrong, SQL keywords aren't case-sensitive

-- ------------------------------------------------------------
-- ai_question  (AI-suggested draft questions awaiting review)
-- ------------------------------------------------------------
INSERT INTO ai_question (id, title, description, competence_id, competence_level_id, question_type_id, status,
                         created_at, created_by, updated_at, score, feedback, is_good)
VALUES (1, 'Millele viitab "this" noolefunktsioonis?', 'Vali parim vastus.', 1, 2, 1, 'P', now() - interval '1 day', 1,
        now() - interval '1 day', NULL, NULL, NULL),
       (2, 'Selgita SQL-i JOIN-tüüpe', 'Lühivastus / mõiste kontroll.', 2, 3, 1, 'A', now() - interval '5 days', 1,
        now() - interval '4 days', 4, 'Selge küsimus, sõnastust tuleb veidi täpsustada', true),
       (3, 'JavaScript on dünaamiliselt tüübitud – tõene või väär?', 'Tõene/väär kontroll.', 1, 1, 3, 'R',
        now() - interval '6 days', 1, now() - interval '5 days', 2, 'Liiga lihtne, kattub olemasoleva küsimusega',
        false);

-- ------------------------------------------------------------
-- ai_question_answer
-- ------------------------------------------------------------
INSERT INTO ai_question_answer (id, ai_question_id, is_correct, correct_position, status, answer_text,
                                ai_question_answer_id, score, feedback)
VALUES (1, 1, true, NULL, 'P', 'Ümbritseva leksikaalse skoobi "this" väärtus', NULL, 5, 4),
       (2, 1, false, NULL, 'P', 'Alati globaalne window-objekt', NULL, 2, 2),
       (3, 2, true, NULL, 'A', 'INNER, LEFT, RIGHT ja FULL OUTER liitmised', NULL, 5, 5),
       (4, 3, true, NULL, 'R', 'Tõene', NULL, 3, 3),
       (5, 3, false, NULL, 'R', 'Väär', NULL, 1, 1);

-- ------------------------------------------------------------
-- Lisaandmed töölaua graafikute jaoks (keskmine tulemus kompetentsi järgi, läbimise määr):
-- 6 kasutajat ja 24 sooritatud testi koos tulemustega viimase 2 nädala jooksul.
-- Punktid vastavad testide maksimumpunktidele, P/F vastab testi läbimise protsendile.
-- test_question_result ridu neil pole, seega nende tulemuste detailvaade on küsimusteta.
-- ------------------------------------------------------------
INSERT INTO "user" (id, email, password_hash, role_id, status, created_at, updated_at)
VALUES (5, 'mari.tamm@example.com', 'user123', 3, 'A', now() - interval '28 days', now()),
       (6, 'jaan.kask@example.com', 'user123', 3, 'A', now() - interval '27 days', now()),
       (7, 'liis.saar@example.com', 'user123', 3, 'A', now() - interval '25 days', now()),
       (8, 'rasmus.mets@example.com', 'user123', 3, 'A', now() - interval '24 days', now()),
       (9, 'kertu.lepp@example.com', 'user123', 3, 'A', now() - interval '22 days', now()),
       (10, 'oliver.rebane@example.com', 'user123', 3, 'A', now() - interval '20 days', now());

INSERT INTO profile (id, user_id, first_name, last_name, phone_number, created_at, updated_at)
VALUES (4, 5, 'Mari', 'Tamm', NULL, now() - interval '28 days', now()),
       (5, 6, 'Jaan', 'Kask', NULL, now() - interval '27 days', now()),
       (6, 7, 'Liis', 'Saar', NULL, now() - interval '25 days', now()),
       (7, 8, 'Rasmus', 'Mets', NULL, now() - interval '24 days', now()),
       (8, 9, 'Kertu', 'Lepp', NULL, now() - interval '22 days', now()),
       (9, 10, 'Oliver', 'Rebane', NULL, now() - interval '20 days', now());

INSERT INTO group_member (id, group_id, user_id, added_by, created_at, updated_at)
VALUES (4, 1, 5, 2, now() - interval '27 days', now() - interval '27 days'),
       (5, 1, 6, 2, now() - interval '26 days', now() - interval '26 days'),
       (6, 2, 7, 2, now() - interval '24 days', now() - interval '24 days'),
       (7, 2, 8, 2, now() - interval '23 days', now() - interval '23 days'),
       (8, 3, 9, 1, now() - interval '21 days', now() - interval '21 days'),
       (9, 3, 10, 1, now() - interval '19 days', now() - interval '19 days');

INSERT INTO user_test (id, test_id, user_id, opens_at, closes_at, status, group_id, assigned_by, created_at)
VALUES (14, 1, 5, now() - interval '5 days', now() + interval '2 days', 'C', 1, 2, now() - interval '5 days'),
       (15, 1, 6, now() - interval '7 days', now(), 'C', 1, 2, now() - interval '7 days'),
       (16, 1, 7, now() - interval '9 days', now() - interval '2 days', 'C', 2, 2, now() - interval '9 days'),
       (17, 2, 5, now() - interval '6 days', now() + interval '1 days', 'C', 1, 2, now() - interval '6 days'),
       (18, 2, 8, now() - interval '12 days', now() - interval '5 days', 'C', 2, 2, now() - interval '12 days'),
       (19, 3, 6, now() - interval '8 days', now() - interval '1 days', 'C', 1, 2, now() - interval '8 days'),
       (20, 3, 9, now() - interval '14 days', now() - interval '7 days', 'C', 3, 1, now() - interval '14 days'),
       (21, 4, 7, now() - interval '4 days', now() + interval '3 days', 'C', 2, 2, now() - interval '4 days'),
       (22, 4, 10, now() - interval '10 days', now() - interval '3 days', 'C', 3, 1, now() - interval '10 days'),
       (23, 4, 8, now() - interval '15 days', now() - interval '8 days', 'C', 2, 2, now() - interval '15 days'),
       (24, 5, 5, now() - interval '5 days', now() + interval '2 days', 'C', 1, 2, now() - interval '5 days'),
       (25, 5, 9, now() - interval '11 days', now() - interval '4 days', 'C', 3, 1, now() - interval '11 days'),
       (26, 5, 10, now() - interval '17 days', now() - interval '10 days', 'C', 3, 1, now() - interval '17 days'),
       (27, 6, 6, now() - interval '6 days', now() + interval '1 days', 'C', 1, 2, now() - interval '6 days'),
       (28, 6, 7, now() - interval '13 days', now() - interval '6 days', 'C', 2, 2, now() - interval '13 days'),
       (29, 7, 8, now() - interval '2 days', now() + interval '5 days', 'C', 2, 2, now() - interval '2 days'),
       (30, 7, 9, now() - interval '9 days', now() - interval '2 days', 'C', 3, 1, now() - interval '9 days'),
       (31, 7, 10, now() - interval '16 days', now() - interval '9 days', 'C', 3, 1, now() - interval '16 days'),
       (32, 8, 5, now() - interval '3 days', now() + interval '27 days', 'C', 1, 2, now() - interval '3 days'),
       (33, 8, 6, now() - interval '6 days', now() + interval '24 days', 'C', 1, 2, now() - interval '6 days'),
       (34, 8, 7, now() - interval '10 days', now() + interval '20 days', 'C', 2, 2, now() - interval '10 days'),
       (35, 8, 3, now() - interval '5 days', now() + interval '25 days', 'C', 1, 2, now() - interval '5 days'),
       (36, 9, 8, now() - interval '4 days', now() + interval '3 days', 'C', 2, 2, now() - interval '4 days'),
       (37, 9, 10, now() - interval '8 days', now() - interval '1 days', 'C', 3, 1, now() - interval '8 days');

INSERT INTO result (id, user_test_id, status, max_score, score_total, completed_at, started_at, total_questions,
                    questions_answered)
VALUES (4, 14, 'P', 20, 20, now() - interval '2 days', now() - interval '2 days' - interval '18 min', 2, 2),
       (5, 15, 'F', 20, 10, now() - interval '4 days', now() - interval '4 days' - interval '22 min', 2, 2),
       (6, 16, 'P', 20, 20, now() - interval '6 days', now() - interval '6 days' - interval '12 min', 2, 2),
       (7, 17, 'P', 10, 10, now() - interval '3 days', now() - interval '3 days' - interval '9 min', 1, 1),
       (8, 18, 'F', 10, 0, now() - interval '9 days', now() - interval '9 days' - interval '14 min', 1, 1),
       (9, 19, 'P', 5, 5, now() - interval '5 days', now() - interval '5 days' - interval '4 min', 1, 1),
       (10, 20, 'F', 5, 0, now() - interval '11 days', now() - interval '11 days' - interval '6 min', 1, 1),
       (11, 21, 'P', 20, 20, now() - interval '1 days', now() - interval '1 days' - interval '11 min', 2, 2),
       (12, 22, 'P', 20, 10, now() - interval '7 days', now() - interval '7 days' - interval '15 min', 2, 2),
       (13, 23, 'P', 20, 20, now() - interval '12 days', now() - interval '12 days' - interval '10 min', 2, 2),
       (14, 24, 'P', 20, 15, now() - interval '2 days', now() - interval '2 days' - interval '13 min', 3, 3),
       (15, 25, 'F', 20, 5, now() - interval '8 days', now() - interval '8 days' - interval '15 min', 3, 3),
       (16, 26, 'P', 20, 20, now() - interval '14 days', now() - interval '14 days' - interval '9 min', 3, 3),
       (17, 27, 'P', 10, 10, now() - interval '3 days', now() - interval '3 days' - interval '20 min', 1, 1),
       (18, 28, 'F', 10, 0, now() - interval '10 days', now() - interval '10 days' - interval '25 min', 1, 1),
       (19, 29, 'P', 10, 10, now() - interval '1 days', now() - interval '1 days' - interval '5 min', 1, 1),
       (20, 30, 'P', 10, 10, now() - interval '6 days', now() - interval '6 days' - interval '7 min', 1, 1),
       (21, 31, 'F', 10, 0, now() - interval '13 days', now() - interval '13 days' - interval '6 min', 1, 1),
       (22, 32, 'P', 105, 85, now() - interval '2 days', now() - interval '2 days' - interval '19 min', 12, 12),
       (23, 33, 'F', 105, 45, now() - interval '5 days', now() - interval '5 days' - interval '20 min', 12, 12),
       (24, 34, 'P', 105, 95, now() - interval '9 days', now() - interval '9 days' - interval '17 min', 12, 12),
       (25, 35, 'P', 105, 70, now() - interval '4 days', now() - interval '4 days' - interval '18 min', 12, 12),
       (26, 36, 'P', 15, 15, now() - interval '3 days', now() - interval '3 days' - interval '16 min', 2, 2),
       (27, 37, 'P', 15, 10, now() - interval '7 days', now() - interval '7 days' - interval '19 min', 2, 2);

-- ------------------------------------------------------------
-- Keep sequences in sync after manual id inserts
-- ------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('role', 'id'), (SELECT MAX(id) FROM role));
SELECT setval(pg_get_serial_sequence('"user"', 'id'), (SELECT MAX(id) FROM "user"));
SELECT setval(pg_get_serial_sequence('"level"', 'id'), (SELECT MAX(id) FROM "level"));
SELECT setval(pg_get_serial_sequence('competence', 'id'), (SELECT MAX(id) FROM competence));
SELECT setval(pg_get_serial_sequence('competence_level', 'id'), (SELECT MAX(id) FROM competence_level));
SELECT setval(pg_get_serial_sequence('competence_file', 'id'), (SELECT MAX(id) FROM competence_file));
SELECT setval(pg_get_serial_sequence('"group"', 'id'), (SELECT MAX(id) FROM "group"));
SELECT setval(pg_get_serial_sequence('group_manager', 'id'), (SELECT MAX(id) FROM group_manager));
SELECT setval(pg_get_serial_sequence('group_member', 'id'), (SELECT MAX(id) FROM group_member));
SELECT setval(pg_get_serial_sequence('invitation', 'id'), (SELECT MAX(id) FROM invitation));
SELECT setval(pg_get_serial_sequence('profile', 'id'), (SELECT MAX(id) FROM profile));
SELECT setval(pg_get_serial_sequence('question_type', 'id'), (SELECT MAX(id) FROM question_type));
SELECT setval(pg_get_serial_sequence('question', 'id'), (SELECT MAX(id) FROM question));
SELECT setval(pg_get_serial_sequence('question_answer', 'id'), (SELECT MAX(id) FROM question_answer));
SELECT setval(pg_get_serial_sequence('test', 'id'), (SELECT MAX(id) FROM test));
SELECT setval(pg_get_serial_sequence('test_question', 'id'), (SELECT MAX(id) FROM test_question));
SELECT setval(pg_get_serial_sequence('user_test', 'id'), (SELECT MAX(id) FROM user_test));
SELECT setval(pg_get_serial_sequence('result', 'id'), (SELECT MAX(id) FROM result));
SELECT setval(pg_get_serial_sequence('test_question_result', 'id'), (SELECT MAX(id) FROM test_question_result));
SELECT setval(pg_get_serial_sequence('test_question_answer', 'id'), (SELECT MAX(id) FROM test_question_answer));
SELECT setval(pg_get_serial_sequence('ai_question', 'id'), (SELECT MAX(id) FROM ai_question));
SELECT setval(pg_get_serial_sequence('ai_question_answer', 'id'), (SELECT MAX(id) FROM ai_question_answer));

COMMIT;
