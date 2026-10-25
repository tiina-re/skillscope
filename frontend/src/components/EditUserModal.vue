<script>
import UserService from '@/services/UserService.js'
import RoleService from '@/services/RoleService.js'
import GroupService from '@/services/GroupService.js'
import AlertDanger from '@/components/AlertDanger.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import { PhCheck } from '@phosphor-icons/vue'

export default {
  name: 'EditUserModal',
  components: { AlertDanger, BaseModal, PhCheck },

  props: {
    // Muudetav kasutaja tabelist; null tähendab, et modal on suletud
    user: { type: Object, default: null },
  },

  emits: ['event-close', 'event-user-updated'],

  data() {
    return {
      isSaving: false,
      errorMessage: '',
      isAdmin: sessionStorage.getItem('roleName') === 'ADMIN',
      currentUserId: Number(sessionStorage.getItem('userId')),
      roles: [],
      groups: [],
      managedGroupIds: [],
      userUpdate: {
        firstName: '',
        lastName: '',
        roleId: null,
        groupIds: [],
      },
    }
  },

  computed: {
    isSelf() {
      return this.user.userId === this.currentUserId
    },
    // Rolli muudab ainult ADMIN ja mitte iseenda oma
    isRoleLocked() {
      return !this.isAdmin || this.isSelf
    },
    // HALDUR muudab enda ja oma gruppide liikmete nime
    isNameLocked() {
      const isInManagedGroup = this.user.groupIds.some((groupId) =>
        this.managedGroupIds.includes(groupId),
      )
      return !this.isAdmin && !this.isSelf && !isInManagedGroup
    },
  },

  watch: {
    user(newUser) {
      if (newUser) {
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
    getManagedGroupIds() {
      GroupService.getManagedGroupIds()
        .then((response) => (this.managedGroupIds = response.data))
        .catch(() => (this.errorMessage = 'Hallatavate gruppide laadimine ebaõnnestus'))
    },
    // HALDUR saab märkida ja eemaldada ainult oma hallatavaid gruppe
    isGroupLocked(groupId) {
      return !this.isAdmin && !this.managedGroupIds.includes(groupId)
    },
    updateUser() {
      this.isSaving = true
      this.errorMessage = ''
      UserService.updateUser(this.user.userId, this.userUpdate)
        .then(() => this.$emit('event-user-updated'))
        .catch((error) => this.handleUpdateUserError(error))
        .finally(() => (this.isSaving = false))
    },
    handleUpdateUserError(error) {
      this.errorMessage = error.response?.data?.message ?? 'Kasutaja muutmine ebaõnnestus'
    },
    resetForm() {
      this.errorMessage = ''
      this.userUpdate = {
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        roleId: this.user.roleId,
        groupIds: [...this.user.groupIds],
      }
    },
  },

  beforeMount() {
    this.getAllRoles()
    this.getActiveGroups()
    if (!this.isAdmin) {
      this.getManagedGroupIds()
    }
  },
}
</script>

<template>
  <BaseModal
    :is-open="user !== null"
    :is-backdrop-closable="false"
    @event-modal-closed="$emit('event-close')"
  >
    <template #title>Muuda kasutajat</template>
    <template #body>
      <form id="edit-user-form" @submit.prevent="updateUser">
        <AlertDanger :error-message="errorMessage" />
        <div class="row g-3 mb-3">
          <div class="col-sm-6">
            <label for="edit-user-first-name" class="form-label fw-semibold">Eesnimi</label>
            <input
              id="edit-user-first-name"
              v-model="userUpdate.firstName"
              type="text"
              class="form-control"
              maxlength="255"
              :disabled="isNameLocked"
              required
            />
          </div>
          <div class="col-sm-6">
            <label for="edit-user-last-name" class="form-label fw-semibold">Perekonnanimi</label>
            <input
              id="edit-user-last-name"
              v-model="userUpdate.lastName"
              type="text"
              class="form-control"
              maxlength="255"
              :disabled="isNameLocked"
              required
            />
          </div>
        </div>
        <div class="mb-3">
          <label for="edit-user-email" class="form-label fw-semibold">E-post</label>
          <input
            id="edit-user-email"
            :value="user.email"
            type="email"
            class="form-control"
            disabled
          />
          <div class="form-text">E-posti aadressi ei saa muuta.</div>
        </div>
        <div class="mb-3">
          <label for="edit-user-role" class="form-label fw-semibold">Roll</label>
          <select
            id="edit-user-role"
            v-model="userUpdate.roleId"
            class="form-select"
            :disabled="isRoleLocked"
            required
          >
            <option v-for="role in roles" :key="role.roleId" :value="role.roleId">
              {{ role.roleName }}
            </option>
          </select>
          <div v-if="isRoleLocked" class="form-text">
            {{ isAdmin ? 'Enda rolli ei saa muuta.' : 'Rolli saab muuta ainult admin.' }}
          </div>
        </div>
        <div class="mt-3">
          <span class="form-label fw-semibold d-block">Grupid</span>
          <div v-if="groups.length > 0" class="d-flex flex-wrap gap-2">
            <label
              v-for="group in groups"
              :key="group.groupId"
              class="group-option rounded-pill d-inline-flex align-items-center gap-1"
              :class="{
                selected: userUpdate.groupIds.includes(group.groupId),
                locked: isGroupLocked(group.groupId),
              }"
            >
              <input
                v-model="userUpdate.groupIds"
                type="checkbox"
                class="visually-hidden"
                :value="group.groupId"
                :disabled="isGroupLocked(group.groupId)"
              />
              <PhCheck v-if="userUpdate.groupIds.includes(group.groupId)" :size="14" />
              {{ group.groupName }}
            </label>
          </div>
          <div v-else class="text-body-secondary">Aktiivseid gruppe pole</div>
          <div class="form-text">
            Vali vähemalt üks grupp.
            <template v-if="!isAdmin">Muuta saad ainult enda hallatavaid gruppe.</template>
          </div>
        </div>
      </form>
    </template>
    <template #buttons>
      <button
        type="button"
        class="btn btn-light fw-bold rounded-2"
        :disabled="isSaving"
        @click="$emit('event-close')"
      >
        Tühista
      </button>
      <button
        type="submit"
        form="edit-user-form"
        class="btn btn-primary fw-bold rounded-2"
        :disabled="isSaving || userUpdate.groupIds.length === 0"
      >
        {{ isSaving ? 'Salvestan...' : 'Salvesta' }}
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
  border-color: var(--bs-primary);
  background-color: var(--bs-primary-bg-subtle);
  color: var(--bs-primary-text-emphasis);
}

.group-option.locked {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
