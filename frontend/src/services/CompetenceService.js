import axios from 'axios'

export default {
  getCompetencesRequest() {
    return axios.get('/api/competences')
  },
  getCompetenceDetailRequest(competenceId) {
    return axios.get(`/api/competences/${competenceId}`)
  },
}
