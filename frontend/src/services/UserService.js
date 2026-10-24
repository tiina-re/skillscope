import axios from 'axios'

export default {
  getAllUsers() {
    return axios.get('/api/users')
  },
  addUser(newUser) {
    return axios.post('/api/users', newUser)
  },
  updateUser(userId, userUpdate) {
    return axios.put(`/api/users/${userId}`, userUpdate)
  },
  activateUser(userId) {
    return axios.patch(`/api/users/${userId}/activate`)
  },
  deleteUser(userId) {
    return axios.delete(`/api/users/${userId}`)
  },
}
