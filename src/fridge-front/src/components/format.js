export function daysUntil(dateString) {
  if (!dateString) return null;
  const [y, m, d] = dateString.split('-').map(Number);
  const target = new Date(y, m - 1, d);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.round((target - today) / 86400000);
}

export function formatDate(value) {
  if (!value) return '-';
  return value.slice(0, 10).replaceAll('-', '.');
}

export function formatDateTime(value) {
  if (!value) return '-';
  return `${formatDate(value)} ${value.slice(11, 16)}`;
}

export const STATUS_LABEL = { STORED: '보관 중', CONSUMED: '다 먹음', DISCARDED: '버림' };
export const ROLE_LABEL = { ADMIN: '관리자', MEMBER: '구성원' };
export const ACTION_LABEL = {
  CREATE: '등록',
  CONSUME: '소비',
  UPDATE: '수정',
  DISCARD: '버림',
  STATUS_CHANGE: '상태 변경',
};
