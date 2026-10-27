import axios from 'axios'

export default {

  getAllTests(competenceId) {
    return axios.get('/api/tests', { params: { competenceId } })
  },
  getTestDetail(testId) {
    return axios.get(`/api/tests/${testId}`)
  },
  getTestResult(userTestId) {
    return axios.get('/api/result', { params : { userTestId }})
  }
}
