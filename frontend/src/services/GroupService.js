import axios from 'axios'

export default {
  getActiveGroups() {
    return axios.get('/api/groups')
  },
  getManagedGroupIds() {
    return axios.get('/api/groups/managed')
  },
}
