package ee.testiplatvorm.controller.test;

import ee.testiplatvorm.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest käivitab ainult veebikihi: TestController + RestExceptionHandler + JSON-i teisendus.
// Andmebaasi, teenuseid ega repositooriume ei käivitata.
@WebMvcTest(TestController.class)
class TestControllerTest {

    // MockMvc saadab kontrollerile võlts-HTTP päringuid ilma päris serverita
    @Autowired
    private MockMvc mockMvc;

    // Kontroller vajab TestService-it – Springi kontekstis asendame selle mock'iga
    @MockitoBean
    private TestService testService;

    // Korrektne päring testi loomiseks. Igas testis muudame sellest ühte välja.
    private static final String VALID_TEST_CREATE_REQUEST = """
            {
              "competenceId": 1,
              "competenceLevelId": 2,
              "testName": "Java põhitõed",
              "testShortDescription": "Lühike kirjeldus",
              "testDescription": "Pikk kirjeldus",
              "isTimed": false,
              "timerMin": null,
              "passPercent": 70,
              "roundScoreUp": true,
              "questions": [ { "questionId": 10 } ]
            }
            """;

    @Test
    void createTest_validRequest_returns200AndCallsService() throws Exception {
        // Act + Assert
        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEST_CREATE_REQUEST))
                .andExpect(status().isOk());

        verify(testService).createTest(any());
    }

    @Test
    void createTest_emptyQuestionList_returns400() throws Exception {
        // Arrange – kõik väljad korrektsed, ainult küsimuste nimekiri on tühi
        String testCreateRequest = """
                {
                  "competenceId": 1,
                  "competenceLevelId": 2,
                  "testName": "Java põhitõed",
                  "testShortDescription": "Lühike kirjeldus",
                  "testDescription": "Pikk kirjeldus",
                  "isTimed": false,
                  "timerMin": null,
                  "passPercent": 70,
                  "roundScoreUp": true,
                  "questions": []
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testCreateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value(startsWith("questions:")));

        verify(testService, never()).createTest(any());
    }

    @Test
    void createTest_missingRoundScoreUp_returns400() throws Exception {
        // Arrange – väli roundScoreUp puudub üldse
        String testCreateRequest = """
                {
                  "competenceId": 1,
                  "competenceLevelId": 2,
                  "testName": "Java põhitõed",
                  "testShortDescription": "Lühike kirjeldus",
                  "testDescription": "Pikk kirjeldus",
                  "isTimed": false,
                  "timerMin": null,
                  "passPercent": 70,
                  "questions": [ { "questionId": 10 } ]
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testCreateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INCORRECT_INPUT"))
                .andExpect(jsonPath("$.message").value(startsWith("roundScoreUp:")));

        verify(testService, never()).createTest(any());
    }

}
