import { daysUntil } from './format';

// 라벨기로 뽑은 테이프처럼 생긴 소비기한 표시. 이 앱의 대표 요소.
export default function ExpiryTape({ expiresAt, status, size }) {
  let text;
  let tone;

  if (status === 'CONSUMED') {
    text = '다 먹음';
    tone = 'done';
  } else if (status === 'DISCARDED') {
    text = '버림';
    tone = 'done';
  } else {
    const days = daysUntil(expiresAt);
    if (days === null) {
      text = '기한 없음';
      tone = 'none';
    } else if (days < 0) {
      text = `D+${-days}`;
      tone = 'gone';
    } else if (days === 0) {
      text = 'D-DAY';
      tone = 'gone';
    } else {
      text = `D-${days}`;
      tone = days <= 3 ? 'soon' : 'fresh';
    }
  }

  return (
    <span className={`tape tape--${tone}${size === 'large' ? ' tape--large' : ''}`} title={expiresAt ?? ''}>
      {text}
    </span>
  );
}
