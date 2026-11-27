import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { userApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { ErrorNote, Field, Loading, Notice } from '../components/ui';
import { formatDate } from '../components/format';

export default function MyPage() {
  const { me } = useAuth();
  if (!me) return <Loading />;

  return (
    <>
      <h1 className="page__title">내 정보</h1>

      <section className="section">
        <dl className="facts">
          <dt>닉네임</dt>
          <dd>
            {me.nickname} <Link to="/nickname">변경</Link>
          </dd>
          <dt>로그인 방식</dt>
          <dd>{me.loginType === 'KAKAO' ? '카카오' : '이메일'}</dd>
          {me.email && (
            <>
              <dt>이메일</dt>
              <dd>{me.email}</dd>
            </>
          )}
          <dt>가입일</dt>
          <dd>{formatDate(me.createdAt)}</dd>
        </dl>
      </section>

      <NotificationTimeSection />
      {me.loginType === 'LOCAL' && <PasswordSection />}
    </>
  );
}

function NotificationTimeSection() {
  const [time, setTime] = useState('');
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    userApi
      .getNotificationTime()
      .then((data) => setTime(data?.notifyTime?.slice(0, 5) ?? ''))
      .catch(setError);
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    try {
      await userApi.updateNotificationTime(time);
      setSaved(true);
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">알림 받을 시간</h2>
      <p className="muted">소비기한이 다가온 식재료를 매일 이 시간에 알려 드려요.</p>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input type="time" value={time} onChange={(e) => setTime(e.target.value)} required aria-label="알림 시간" />
        <button type="submit" className="button button--quiet">
          시간 저장
        </button>
      </form>
      <ErrorNote error={error} />
      {saved && <Notice>알림 시간을 저장했습니다.</Notice>}
    </section>
  );
}

function PasswordSection() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    try {
      await userApi.updatePassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      setSaved(true);
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">비밀번호 변경</h2>
      <form className="stack stack--narrow" onSubmit={handleSubmit}>
        <Field label="현재 비밀번호">
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </Field>
        <Field label="새 비밀번호" hint="8~20자, 영문·숫자·특수문자를 모두 포함">
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            autoComplete="new-password"
            required
          />
        </Field>
        <ErrorNote error={error} />
        {saved && <Notice>비밀번호를 변경했습니다.</Notice>}
        <button type="submit" className="button">
          비밀번호 변경
        </button>
      </form>
    </section>
  );
}
