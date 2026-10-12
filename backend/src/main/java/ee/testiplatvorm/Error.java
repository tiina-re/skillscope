package ee.testiplatvorm;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale emaili aadress või salasõna"),
    NO_PERMISSION("Sul puudub õigus testi luua"),
    NO_PERMISSION_TO_CREATE_QUESTIONS("Sul puudub õigus küsimusi luua"),
    NO_PERMISSION_TO_MANAGE_QUESTIONS("Sul puudub õigus küsimusi vaadata ega muuta"),
    NO_PERMISSION_TO_VIEW_TEST("Sul puudub õigus testi vaadata"),
    NO_PERMISSION_TO_VIEW_DASHBOARD("Sul puudub õigus töölauda vaadata"),
    INVALID_AI_QUESTION("Küsimus ei vasta reeglitele (vastuste arv, õigete vastuste arv, koodinäite pikkus või vastuses on vihje õigele vastusele)"),
    NO_TEST_ASSIGNMENT_FOR_THIS_USER("Kasutajale ei ole vastavat testi määratud"),
    EMAIL_ALREADY_EXISTS("Selle e-posti aadressiga kasutaja on juba olemas"),
    NO_RESULT_FOUND("Tulemust ei leitud"),
    AI_QUESTION_ALREADY_REVIEWED("AI küsimus on juba üle vaadatud"),
    AI_QUESTION_GENERATION_FAILED("AI ei suutnud korrektseid küsimusi luua, proovi uuesti"),
    INVALID_CORRECT_ANSWER_COUNT("Õigete vastuste arv ei vasta küsimuse tüübile");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}
