import axios from 'axios'

export default {
  sendGenerateQuestionRequest(instructions, previousMessages) {
    return axios.post('/api/ai-questions/generate', { instructions, previousMessages })
  },

  // Küsimuste genereerimine eelvalitud kompetentsi taseme jaoks (QuestionBankAiGenerator)
  sendGenerateLevelQuestionsRequest(aiQuestionGenerateRequestDto) {
    return axios.post('/api/ai-questions/generate-by-level', aiQuestionGenerateRequestDto)
  },

  sendSaveQuestionRequest(question) {
    console.log("question", question);
    return axios.post('/api/ai-questions', question)
  },

  getAiQuestionsRequest(status, competenceId) {
    return axios.get('/api/ai-questions', { params: { status, competenceId } })
  },

  reviewAiQuestionRequest(aiQuestionId, aiQuestionReviewRequestDto) {
    return axios.post(`/api/ai-questions/${aiQuestionId}/review`, aiQuestionReviewRequestDto)
  },
}
