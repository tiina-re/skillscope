<script>
import UserService from '@/services/UserService.js'
import RoleService from '@/services/RoleService.js'
import GroupService from '@/services/GroupService.js'
import AlertDanger from '@/components/AlertDanger.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import { PhCheck, PhEye, PhEyeSlash } from '@phosphor-icons/vue'

export default {
  name: 'AddUserModal',
  components: { AlertDanger, BaseModal, PhCheck, PhEye, PhEyeSlash },

  props: {
    isOpen: Boolean,
  },

  emits: ['event-close', 'event-user-added'],

  data() {
    return {
      isSaving: false,
      isPasswordVisible: false,
      errorMessage: '',
      roles: [],
      groups: [],
      newUser: {
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        roleId: null,
        groupIds: [],
      },
    }
  },

  watch: {
    isOpen(newValue) {
      if (newValue) {
        this.resetForm()
      }
    },
  },

  methods: {
    getAllRoles() {
      RoleService.getAllRoles()
        .then((response) => (this.roles = response.data))
        .catch(() => (this.errorMessage = 'Rollide laadimine ebaõnnestus'))
    },
    getActiveGroups() {
      GroupService.getActiveGroups()
        .then((response) => (this.groups = response.data))
        .catch(() => (this.errorMessage = 'Gruppide laadimine ebaõnnestus'))
    },
    addUser() {
      this.isSaving = true
      this.errorMessage = ''
      UserService.addUser(this.newUser)
        .then(() => this.$emit('event-user-added'))
        .catch((error) => this.handleAddUserError(error))
        .finally(() => (this.isSaving = false))
    },
    handleAddUserError(error) {
      this.errorMessage = error.response?.data?.message ?? 'Kasutaja lisamine ebaõnnestus'
    },
    resetForm() {
      this.errorMessage = ''
      this.isPasswordVisible = false
      this.newUser = {
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        roleId: null,
        groupIds: [],
      }
    },
  },

  beforeMount() {
    this.getAllRoles()
    this.getActiveGroups()
  },
}
</script>

<template>
  <BaseModal :is-open="isOpen" @event-modal-closed="$emit('event-close')">
    <template #title>Lisa kasutaja</template>
    <template #body>
      <form id="add-user-form" @submit.prevent="addUser">
        <AlertDanger :error-message="errorMessage" />
        <div class="row g-3 mb-3">
          <div class="col-sm-6">
            <label for="add-user-first-name" class="form-label fw-semibold">Eesnimi</label>
            <input
              id="add-user-first-name"
              v-model="newUser.firstName"
              type="text"
              class="form-control"
              maxlength="255"
              required
            />
          </div>
          <div class="col-sm-6">
            <label for="add-user-last-name" class="form-label fw-semibold">Perekonnanimi</label>
            <input
              id="add-user-last-name"
              v-model="newUser.lastName"
              type="text"
              class="form-control"
              maxlength="255"
              required
            />
          </div>
        </div>
        <div class="mb-3">
          <label for="add-user-email" class="form-label fw-semibold">E-post</label>
          <input
            id="add-user-email"
            v-model="newUser.email"
            type="email"
            class="form-control"
            placeholder="nimi@example.com"
            required
          />
        </div>
        <div class="mb-3">
          <label for="add-user-password" class="form-label fw-semibold">Parool</label>
          <div class="input-group">
            <input
              id="add-user-password"
              v-model="newUser.password"
              :type="isPasswordVisible ? 'text' : 'password'"
              class="form-control"
              minlength="6"
              maxlength="255"
              autocomplete="new-password"
              required
            />
            <button
              type="button"
              class="btn btn-outline-secondary d-inline-flex align-items-center"
              :title="isPasswordVisible ? 'Peida parool' : 'Näita parooli'"
              @click="isPasswordVisible = !isPasswordVisible"
            >
              <PhEyeSlash v-if="isPasswordVisible" :size="18" />
              <PhEye v-else :size="18" />
            </button>
          </div>
          <div class="form-text">Vähemalt 6 märki.</div>
        </div>
        <div class="mb-3">
          <label for="add-user-role" class="form-label fw-semibold">Roll</label>
          <select id="add-user-role" v-model="newUser.roleId" class="form-select" required>
            <option :value="null" disabled>-- Vali roll --</option>
            <option v-for="role in roles" :key="role.roleId" :value="role.roleId">
              {{ role.roleName }}
            </option>
          </select>
        </div>
        <div class="mt-3">
          <span class="form-label fw-semibold d-block">Grupid</span>
          <div v-if="groups.length > 0" class="d-flex flex-wrap gap-2">
            <label
              v-for="group in groups"
              :key="group.groupId"
              class="group-option rounded-pill d-inline-flex align-items-center gap-1"
              :class="{ selected: newUser.groupIds.includes(group.groupId) }"
            >
              <input
                v-model="newUser.groupIds"
                type="checkbox"
                class="visually-hidden"
                :value="group.groupId"
              />
              <PhCheck v-if="newUser.groupIds.includes(group.groupId)" :size="14" />
              {{ group.groupName }}
            </label>
          </div>
          <div v-else class="text-body-secondary">Aktiivseid gruppe pole</div>
          <div class="form-text">Valikuline. Võid valida mitu gruppi.</div>
        </div>
      </form>
    </template>
    <template #buttons>
      <button type="button" class="btn btn-light fw-bold rounded-2" @click="$emit('event-close')">
        Tühista
      </button>
      <button
        type="submit"
        form="add-user-form"
        class="btn btn-primary fw-bold rounded-2"
        :disabled="isSaving"
      >
        {{ isSaving ? 'Lisan...' : 'Lisa kasutaja' }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.group-option {
  padding: 0.3rem 0.85rem;
  border: 2px solid var(--bs-gray-200);
  background-color: var(--bs-gray-100);
  color: var(--bs-gray-700);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.group-option.selected {
  border-color: var(--bs-blue);
  background-color: var(--bs-gray-200);
  color: var(--bs-blue);
}
</style>
