<script>
import UserService from '@/services/UserService.js'
import Status from '@/Status.js'
import LoadingText from '@/components/LoadingText.vue'
import AlertDanger from '@/components/AlertDanger.vue'
import AddUserModal from '@/components/AddUserModal.vue'
import { PhCaretDown, PhCheck, PhTrash, PhUserPlus } from '@phosphor-icons/vue'
import MainTitle from '@/components/MainTitle.vue'

export default {
  name: 'UsersView',
  components: {
    MainTitle,
    LoadingText,
    AlertDanger,
    AddUserModal,
    PhCaretDown,
    PhCheck,
    PhTrash,
    PhUserPlus,
  },

  data() {
    return {
      isLoading: true,
      isAddUserModalOpen: false,
      errorMessage: '',
      userStatus: Status,
      statusCodes: ['A', 'P', 'I'],
      users: [
        {
          userId: 0,
          firstName: '',
          lastName: '',
          email: '',
          groupNames: [],
          roleName: '',
          status: '',
        },
      ],
    }
  },

  methods: {
    getAllUsers() {
      UserService.getAllUsers()
        .then((response) => this.handleGetAllUsers(response))
        .catch(() => (this.errorMessage = 'Kasutajate laadimine ebaõnnestus'))
        .finally(() => (this.isLoading = false))
    },
    handleGetAllUsers(response) {
      this.errorMessage = ''
      this.users = response.data
    },
    handleError(error, defaultMessage) {
      this.errorMessage = error.response?.data?.message ?? defaultMessage
    },
    handleUserAdded() {
      this.isAddUserModalOpen = false
      this.getAllUsers()
    },
    updateUserStatus(user, status) {
      UserService.updateUserStatus(user.userId, status)
        .then(() => this.getAllUsers())
        .catch((error) => this.handleError(error, 'Staatuse muutmine ebaõnnestus'))
    },
    deleteUser(user) {
      if (!confirm(`Kas soovid kasutaja ${user.email} kustutada?`)) {
        return
      }
      UserService.deleteUser(user.userId)
        .then(() => this.getAllUsers())
        .catch((error) => this.handleError(error, 'Kasutaja kustutamine ebaõnnestus'))
    },
    hasName(user) {
      return user.firstName !== null || user.lastName !== null
    },
    getFullName(user) {
      return `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim()
    },
    getInitials(user) {
      if (!this.hasName(user)) {
        return user.email.charAt(0).toUpperCase()
      }
      return `${user.firstName?.charAt(0) ?? ''}${user.lastName?.charAt(0) ?? ''}`.toUpperCase()
    },
  },

  beforeMount() {
    this.getAllUsers()
  },
}
</script>

<template>
  <div class="container py-4">
    <LoadingText v-if="isLoading" />
    <div v-else>
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-5">
        <MainTitle title="Kasutajad" />
        <button
          class="btn btn-color rounded-2 d-inline-flex align-items-center gap-2"
          @click="isAddUserModalOpen = true"
        >
          <PhUserPlus :size="20" />
          Lisa kasutaja
        </button>
      </div>
      <AlertDanger :error-message="errorMessage" />
      <div class="card app-surface rounded-4">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th class="ps-4">Nimi</th>
                <th>E-post</th>
                <th>Roll</th>
                <th>Grupid</th>
                <th>Staatus</th>
                <th class="pe-4"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.userId">
                <td class="ps-4">
                  <div class="d-flex align-items-center gap-3">
                    <span class="user-avatar rounded-circle flex-shrink-0">
                      {{ getInitials(user) }}
                    </span>
                    <span v-if="hasName(user)" class="fw-semibold">{{ getFullName(user) }}</span>
                    <span v-else class="text-body-secondary fst-italic">Registreerimata</span>
                  </div>
                </td>
                <td>{{ user.email }}</td>
                <td>{{ user.roleName }}</td>
                <td>
                  <div class="d-flex flex-wrap gap-1">
                    <span
                      v-for="groupName in user.groupNames"
                      :key="groupName"
                      class="group-pill rounded-pill"
                    >
                      {{ groupName }}
                    </span>
                    <span v-if="user.groupNames.length === 0" class="text-body-secondary">—</span>
                  </div>
                </td>
                <td>
                  <div class="dropdown">
                    <button
                      class="btn btn-sm p-0 border-0 d-inline-flex align-items-center gap-1"
                      data-bs-toggle="dropdown"
                      data-bs-popper-config='{"strategy":"fixed"}'
                      title="Muuda staatust"
                    >
                      <span
                        class="badge rounded-pill"
                        :class="userStatus[user.status]?.badgeClass ?? 'text-bg-secondary'"
                      >
                        {{ userStatus[user.status]?.name ?? user.status }}
                      </span>
                      <PhCaretDown :size="14" class="text-body-secondary" />
                    </button>
                    <ul class="dropdown-menu dropdown-menu-custom">
                      <li v-for="statusCode in statusCodes" :key="statusCode">
                        <button
                          class="dropdown-item d-flex align-items-center justify-content-between gap-2"
                          :disabled="statusCode === user.status"
                          @click="updateUserStatus(user, statusCode)"
                        >
                          <span
                            class="badge rounded-pill"
                            :class="userStatus[statusCode].badgeClass"
                          >
                            {{ userStatus[statusCode].name }}
                          </span>
                          <PhCheck v-if="statusCode === user.status" :size="16" />
                        </button>
                      </li>
                    </ul>
                  </div>
                </td>
                <td class="pe-4 text-end">
                  <button
                    v-if="user.status !== 'I'"
                    class="btn btn-sm btn-delete rounded-2 d-inline-flex align-items-center gap-1"
                    @click="deleteUser(user)"
                  >
                    <PhTrash :size="16" />
                    Kustuta
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <AddUserModal
      :is-open="isAddUserModalOpen"
      @event-close="isAddUserModalOpen = false"
      @event-user-added="handleUserAdded"
    />
  </div>
</template>

<style scoped>
.card {
  overflow: hidden;
}

.table {
  --bs-table-hover-bg: var(--bs-gray-100);
}

.table th {
  padding-top: 1rem;
  padding-bottom: 1rem;
  background-color: var(--bs-gray-100);
  color: var(--bs-gray-600);
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.table td {
  padding-top: 0.9rem;
  padding-bottom: 0.9rem;
}

.table tbody tr:last-child td {
  border-bottom: 0;
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  background-color: var(--bs-gray-200);
  color: var(--bs-blue);
  font-size: 0.95rem;
  font-weight: bold;
}

.group-pill {
  padding: 0.15rem 0.65rem;
  background-color: var(--bs-gray-200);
  color: var(--bs-blue);
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

.btn-color {
  --bs-btn-bg: var(--bs-gray-200);
  --bs-btn-color: var(--bs-blue);
  font-weight: bold;
  --bs-btn-hover-bg: var(--bs-blue);
  --bs-btn-hover-color: var(--bs-light);
  --bs-btn-active-bg: var(--bs-gray-400);
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 11rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-hover-bg: var(--bs-gray-200);
  --bs-dropdown-link-disabled-color: var(--bs-body-color);
}

.btn-delete {
  --bs-btn-bg: var(--bs-gray-200);
  --bs-btn-color: var(--bs-red);
  --bs-btn-hover-bg: var(--bs-red);
  --bs-btn-hover-color: var(--bs-light);
  --bs-btn-active-bg: var(--bs-gray-400);
  font-weight: 600;
}

@media (min-width: 1800px) {
  .container {
    max-width: 1760px;
  }
}
</style>
