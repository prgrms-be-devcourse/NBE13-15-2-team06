import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { ErrorNote, Field } from '../components/ui';

export default function NicknamePage() {
  const { me, refreshMe } = useAuth();
  const navigate = useNavigate();
  const [nickname, setNickname] = useState(me?.nickname ?? '');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const isFirstTime = me?.nickname == null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await userApi.updateNickname(nickname.trim());
      await refreshMe();
      navigate(isFirstTime ? '/fridges' : '/me', { replace: true });
    } catch (e) {
      setError(e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <h1 className="auth__title">{isFirstTime ? '닉네임을 정해 주세요' : '닉네임 변경'}</h1>
      {isFirstTime && <p className="auth__lead">같은 냉장고를 쓰는 사람들에게 이 이름으로 보여요.</p>}
      <form className="stack" onSubmit={handleSubmit}>
        <Field label="닉네임" hint="2~10자, 한글·영문·숫자">
          <input value={nickname} onChange={(e) => setNickname(e.target.value)} maxLength={10} required autoFocus />
        </Field>
        <ErrorNote error={error} />
        <button type="submit" className="button" disabled={submitting}>
          {submitting ? '저장 중…' : '저장'}
        </button>
      </form>
    </div>
  );
}
