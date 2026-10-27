<script>
import UserService from '@/services/UserService.js'
import Status from '@/Status.js'
import LoadingText from '@/components/LoadingText.vue'
import AlertDanger from '@/components/AlertDanger.vue'
import AddUserModal from '@/components/AddUserModal.vue'
import EditUserModal from '@/components/EditUserModal.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import SuccessToast from '@/components/SuccessToast.vue'
import FilterDropdown from '@/components/FilterDropdown.vue'
import {
  PhArrowCounterClockwise,
  PhDotsThree,
  PhMagnifyingGlass,
  PhPencilSimple,
  PhTrash,
  PhUserPlus,
} from '@phosphor-icons/vue'
import MainTitle from '@/components/MainTitle.vue'

export default {
  name: 'UsersView',
  components: {
    MainTitle,
    LoadingText,
    AlertDanger,
    AddUserModal,
    EditUserModal,
    BaseModal,
    SuccessToast,
    FilterDropdown,
    PhArrowCounterClockwise,
    PhDotsThree,
    PhMagnifyingGlass,
    PhPencilSimple,
    PhTrash,
    PhUserPlus,
  },

  data() {
    return {
      isLoading: true,
      isAddUserModalOpen: false,
      isDeleting: false,
      isActivating: false,
      errorMessage: '',
      deleteErrorMessage: '',
      activateErrorMessage: '',
      successMessage: '',
      isAdmin: sessionStorage.getItem('roleName') === 'ADMIN',
      currentUserId: Number(sessionStorage.getItem('userId')),
      userToEdit: null,
      userToDelete: null,
      userToActivate: null,
      searchText: '',
      // null - kõik, muidu staatuse kood (A, P, I)
      statusFilter: null,
      statusOptions: ['A', 'P', 'I'].map((statusCode) => ({
        value: statusCode,
        label: Status[statusCode].name,
      })),
      userStatus: Status,
      users: [
        {
          userId: 0,
          firstName: '',
          lastName: '',
          email: '',
          groupIds: [],
          groupNames: [],
          roleId: 0,
          roleName: '',
          status: '',
        },
      ],
    }
  },

  computed: {
    // Otsing vaatab nime, e-posti, rolli ja gruppe. Staatust ei vaata, sest "aktiivne" leiaks ka "Mitteaktiivne".
    filteredUsers() {
      const searchText = this.searchText.trim().toLowerCase()
      return this.users.filter((user) => {
        if (!this.isStatusMatching(user)) {
          return false
        }
        const searchableText = [
          this.getFullName(user),
          user.email,
          user.roleName,
          ...user.groupNames,
        ]
          .join(' ')
          .toLowerCase()
        return searchableText.includes(searchText)
      })
    },
  },

  methods: {
    isStatusMatching(user) {
      return this.statusFilter === null || user.status === this.statusFilter
    },
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
    // restored = true, kui sama e-postiga kustutatud kasutaja taastati
    handleUserAdded(email, restored) {
      this.successMessage = restored
        ? `Kustutatud kasutaja ${email} taastati`
        : `Kasutaja ${email} on lisatud`
      this.isAddUserModalOpen = false
      this.getAllUsers()
    },
    handleUserUpdated() {
      this.successMessage = `Kasutaja ${this.userToEdit.email} on muudetud`
      this.userToEdit = null
      this.getAllUsers()
    },
    // Muuta saab ainult aktiivset kasutajat. HALDUR ei saa muuta ADMIN-it.
    canEdit(user) {
      return user.status === 'A' && (this.isAdmin || user.roleName !== 'ADMIN')
    },
    // Kustutada saab ainult ADMIN ja mitte iseennast
    canDelete(user) {
      return this.isAdmin && user.status !== 'I' && user.userId !== this.currentUserId
    },
    // Aktiveerida saab ainult ADMIN ja ainult kustutatud kasutajat
    canActivate(user) {
      return this.isAdmin && user.status === 'I'
    },
    hasActions(user) {
      return this.canEdit(user) || this.canDelete(user) || this.canActivate(user)
    },
    openActivateConfirmation(user) {
      this.activateErrorMessage = ''
      this.userToActivate = user
    },
    closeActivateConfirmation() {
      this.userToActivate = null
    },
    activateUser() {
      this.isActivating = true
      UserService.activateUser(this.userToActivate.userId)
        .then(() => this.handleActivateUser())
        .catch((error) => this.handleActivateUserError(error))
        .finally(() => (this.isActivating = false))
    },
    handleActivateUser() {
      this.successMessage = `Kasutaja ${this.userToActivate.email} on aktiveeritud`
      this.closeActivateConfirmation()
      this.getAllUsers()
    },
    handleActivateUserError(error) {
      this.activateErrorMessage =
        error.response?.data?.message ?? 'Kasutaja aktiveerimine ebaõnnestus. Proovi uuesti.'
    },
    openDeleteConfirmation(user) {
      this.deleteErrorMessage = ''
      this.userToDelete = user
    },
    closeDeleteConfirmation() {
      this.userToDelete = null
    },
    deleteUser() {
      this.isDeleting = true
      UserService.deleteUser(this.userToDelete.userId)
        .then(() => this.handleDeleteUser())
        .catch((error) => this.handleDeleteUserError(error))
        .finally(() => (this.isDeleting = false))
    },
    handleDeleteUser() {
      this.successMessage = `Kasutaja ${this.userToDelete.email} on kustutatud`
      this.closeDeleteConfirmation()
      this.getAllUsers()
    },
    handleDeleteUserError(error) {
      this.deleteErrorMessage =
        error.response?.data?.message ?? 'Kasutaja kustutamine ebaõnnestus. Proovi uuesti.'
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
      <MainTitle title="Kasutajad">
        <button
          class="btn btn-primary fw-bold rounded-2 d-inline-flex align-items-center gap-2"
          @click="isAddUserModalOpen = true"
        >
          <PhUserPlus :size="20" />
          Lisa kasutaja
        </button>
      </MainTitle>
      <AlertDanger :error-message="errorMessage" />
      <!-- Laadimise vea korral ei näidata tabelit, muidu jääks sinna data() näidiskasutaja -->
      <template v-if="errorMessage === ''">
        <div class="d-flex flex-wrap align-items-center gap-3 mb-3">
          <div class="input-group search-input">
            <span class="input-group-text"><PhMagnifyingGlass :size="18" /></span>
            <input
              v-model="searchText"
              type="search"
              class="form-control"
              placeholder="Otsi nime, e-posti, rolli või grupi järgi"
              aria-label="Otsi kasutajaid"
            />
          </div>
          <FilterDropdown
            :options="statusOptions"
            :selected-value="statusFilter"
            :all-option="{ value: null, label: 'Kõik staatused' }"
            @event-option-selected="statusFilter = $event"
          />
        </div>
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
                <tr v-if="filteredUsers.length === 0">
                  <td colspan="6" class="text-center text-body-secondary py-5">
                    Kasutajaid ei leitud
                  </td>
                </tr>
                <tr v-for="user in filteredUsers" :key="user.userId">
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
                        class="badge rounded-pill badge-group"
                      >
                        {{ groupName }}
                      </span>
                      <span v-if="user.groupNames.length === 0" class="text-body-secondary">—</span>
                    </div>
                  </td>
                  <td>
                    <span
                      class="badge rounded-pill"
                      :class="userStatus[user.status]?.badgeClass ?? 'badge-neutral'"
                    >
                      {{ userStatus[user.status]?.name ?? user.status }}
                    </span>
                  </td>
                  <td class="pe-4 text-end">
                    <div v-if="hasActions(user)" class="dropdown">
                      <button
                        class="btn btn-sm menu-button"
                        data-bs-toggle="dropdown"
                        data-bs-popper-config='{"strategy":"fixed"}'
                        aria-expanded="false"
                        aria-label="Kasutaja tegevused"
                      >
                        <PhDotsThree :size="18" />
                      </button>
                      <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
                        <li v-if="canEdit(user)">
                          <button
                            class="dropdown-item d-flex align-items-center gap-2"
                            @click="userToEdit = user"
                          >
                            <PhPencilSimple :size="16" />
                            Muuda
                          </button>
                        </li>
                        <li v-if="canActivate(user)">
                          <button
                            class="dropdown-item d-flex align-items-center gap-2"
                            @click="openActivateConfirmation(user)"
                          >
                            <PhArrowCounterClockwise :size="16" />
                            Aktiveeri
                          </button>
                        </li>
                        <li v-if="canEdit(user) && canDelete(user)">
                          <hr class="dropdown-divider" />
                        </li>
                        <li v-if="canDelete(user)">
                          <button
                            class="dropdown-item d-flex align-items-center gap-2 text-danger"
                            @click="openDeleteConfirmation(user)"
                          >
                            <PhTrash :size="16" />
                            Kustuta
                          </button>
                        </li>
                      </ul>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </div>
    <AddUserModal
      :is-open="isAddUserModalOpen"
      @event-close="isAddUserModalOpen = false"
      @event-user-added="handleUserAdded"
    />
    <EditUserModal
      :user="userToEdit"
      @event-close="userToEdit = null"
      @event-user-updated="handleUserUpdated"
    />

    <BaseModal :is-open="userToDelete !== null" @event-modal-closed="closeDeleteConfirmation">
      <template #title>Kustuta kasutaja?</template>
      <template #body>
        <p class="mb-2">
          <strong>{{ userToDelete.email }}</strong>
        </p>
        <AlertDanger :error-message="deleteErrorMessage" class="mb-3" />
        <p class="text-secondary mb-0">
          Kasutaja muutub mitteaktiivseks ega saa enam sisse logida. Tema testitulemused jäävad
          alles.
        </p>
      </template>
      <template #buttons>
        <button
          class="btn btn-light fw-bold rounded-2"
          :disabled="isDeleting"
          @click="closeDeleteConfirmation"
        >
          Tühista
        </button>
        <button
          class="btn btn-danger fw-bold rounded-2 d-flex align-items-center gap-2"
          :disabled="isDeleting"
          @click="deleteUser"
        >
          <PhTrash :size="16" />
          Kustuta
        </button>
      </template>
    </BaseModal>

    <BaseModal :is-open="userToActivate !== null" @event-modal-closed="closeActivateConfirmation">
      <template #title>Aktiveeri kasutaja?</template>
      <template #body>
        <p class="mb-2">
          <strong>{{ userToActivate.email }}</strong>
        </p>
        <AlertDanger :error-message="activateErrorMessage" class="mb-3" />
        <p class="text-secondary mb-0">
          Kasutaja saab uuesti sisse logida oma vana parooliga. Roll ja grupid jäävad samaks, mis
          enne kustutamist.
        </p>
      </template>
      <template #buttons>
        <button
          class="btn btn-light fw-bold rounded-2"
          :disabled="isActivating"
          @click="closeActivateConfirmation"
        >
          Tühista
        </button>
        <button
          class="btn btn-primary fw-bold rounded-2 d-flex align-items-center gap-2"
          :disabled="isActivating"
          @click="activateUser"
        >
          <PhArrowCounterClockwise :size="16" />
          Aktiveeri
        </button>
      </template>
    </BaseModal>

    <SuccessToast :success-message="successMessage" @event-close="successMessage = ''" />
  </div>
</template>

<style scoped>
.card {
  overflow: hidden;
}

.search-input {
  max-width: 28rem;
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
  background-color: var(--bs-primary-bg-subtle);
  color: var(--bs-primary-text-emphasis);
  font-size: 0.95rem;
  font-weight: bold;
}

/* Ääristeta kolme täpiga nupp nagu küsimuste kaardil (QuestionBankCard.vue) */
.menu-button {
  padding: 0.125rem 0.25rem;
  border: 0;
  border-radius: 0.5rem;
  color: var(--bs-gray-600);
}

.menu-button:hover,
.menu-button.show {
  background-color: var(--bs-gray-200);
  color: var(--bs-body-color);
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 10rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
