import { request } from './client';

// 백엔드 컨트롤러와 1:1로 대응한다. 주소가 바뀌면 여기만 고치면 된다.

export const authApi = {
  signup: (email, password) => request('/auth/signup', { method: 'POST', body: { email, password } }),
  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password } }),
  kakao: (authorizationCode) => request('/auth/kakao', { method: 'POST', body: { authorizationCode } }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};

export const userApi = {
  me: () => request('/users/me'),
  updatePassword: (currentPassword, newPassword) =>
    request('/users/me', { method: 'PATCH', body: { currentPassword, newPassword } }),
  updateNickname: (nickname) => request('/users/me/nickname', { method: 'PATCH', body: { nickname } }),
  getNotificationTime: () => request('/users/me/notification-time'),
  updateNotificationTime: (notifyTime) =>
    request('/users/me/notification-time', { method: 'PATCH', body: { notifyTime } }),
};

export const fridgeApi = {
  list: () => request('/fridges'),
  create: (name) => request('/fridges', { method: 'POST', body: { name } }),
  get: (fridgeId) => request(`/fridges/${fridgeId}`),
  rename: (fridgeId, name) => request(`/fridges/${fridgeId}`, { method: 'PATCH', body: { name } }),
  inviteCode: (fridgeId) => request(`/fridges/${fridgeId}/invite-code`),
};

export const memberApi = {
  list: (fridgeId) => request(`/fridges/${fridgeId}/members`),
  leave: (fridgeId) => request(`/fridges/${fridgeId}/members/me`, { method: 'DELETE' }),
  kick: (fridgeId, memberId) => request(`/fridges/${fridgeId}/members/${memberId}`, { method: 'DELETE' }),
  changeRole: (fridgeId, memberId, role) =>
    request(`/fridges/${fridgeId}/members/${memberId}/role`, { method: 'PATCH', body: { role } }),
};

export const joinRequestApi = {
  request: (inviteCode) => request('/fridge-join-requests', { method: 'POST', body: { inviteCode } }),
  pending: (fridgeId) => request(`/fridges/${fridgeId}/join-requests`),
  approve: (fridgeId, requestId) =>
    request(`/fridges/${fridgeId}/join-requests/${requestId}/approve`, { method: 'POST' }),
  reject: (fridgeId, requestId) =>
    request(`/fridges/${fridgeId}/join-requests/${requestId}/reject`, { method: 'POST' }),
};

// 식재료 API는 /groups/{groupId}를 쓴다. 냉장고 id와 같은 값이다.
export const ingredientApi = {
  list: (groupId, { keyword, expiringWithinDays, sort } = {}) =>
    request(`/groups/${groupId}/ingredients`, { params: { keyword, expiringWithinDays, sort } }),
  create: (groupId, body) => request(`/groups/${groupId}/ingredients`, { method: 'POST', body }),
  get: (groupId, id) => request(`/groups/${groupId}/ingredients/${id}`),
  update: (groupId, id, body) => request(`/groups/${groupId}/ingredients/${id}`, { method: 'PATCH', body }),
  consume: (groupId, id, quantity) =>
    request(`/groups/${groupId}/ingredients/${id}/consumptions`, { method: 'POST', body: { quantity } }),
  updateStatus: (groupId, id, status) =>
    request(`/groups/${groupId}/ingredients/${id}/status`, { method: 'PATCH', body: { status } }),
  histories: (groupId, id) => request(`/groups/${groupId}/ingredients/${id}/histories`),
  updateMemo: (groupId, id, memo) =>
    request(`/groups/${groupId}/ingredients/${id}/memo`, { method: 'PATCH', body: { memo } }),
  updateOwnership: (groupId, id, shared) =>
    request(`/groups/${groupId}/ingredients/${id}/ownership`, { method: 'PATCH', body: { shared } }),
};

export const historyApi = {
  byGroup: (groupId) => request(`/groups/${groupId}/histories`),
};

export const notificationApi = {
  list: () => request('/notifications'),
  byFridge: (fridgeId) => request(`/fridges/${fridgeId}/notifications`),
  read: (notificationId) => request(`/notifications/${notificationId}/read`, { method: 'PATCH' }),
  getSetting: (fridgeId) => request(`/fridges/${fridgeId}/members/me/notification-settings`),
  updateSetting: (fridgeId, enabled, receiveOthers) =>
    request(`/fridges/${fridgeId}/members/me/notification-settings`, {
      method: 'PATCH',
      body: { enabled, receiveOthers },
    }),
};
