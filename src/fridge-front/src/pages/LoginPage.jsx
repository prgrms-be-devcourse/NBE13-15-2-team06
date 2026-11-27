import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { ErrorNote, Field, Notice } from '../components/ui';

const KAKAO_KEY = import.meta.env.VITE_KAKAO_REST_API_KEY;
const KAKAO_REDIRECT = import.meta.env.VITE_KAKAO_REDIRECT_URI;

export function kakaoAuthorizeUrl() {
  const params = new URLSearchParams({
    response_type: 'code',
    client_id: KAKAO_KEY,
    redirect_uri: KAKAO_REDIRECT,
  });
  return `https://kauth.kakao.com/oauth/authorize?${params}`;
}

export default function LoginPage() {
  const { token, signIn, expired } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  if (token) return <Navigate to="/fridges" replace />;

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const result = await authApi.login(email, password);
      signIn(result.accessToken);
      navigate(result.nicknameRequired ? '/nickname' : location.state?.from ?? '/fridges', { replace: true });
    } catch (e) {
      setError(e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <h1 className="auth__title">냉장고를 부탁해</h1>
      <p className="auth__lead">같이 쓰는 냉장고, 뭐가 언제까지인지 한눈에.</p>

      {expired && <Notice>로그인이 만료됐습니다. 다시 로그인해 주세요.</Notice>}

      <form className="stack" onSubmit={handleSubmit}>
        <Field label="이메일">
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </Field>
        <Field label="비밀번호">
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </Field>
        <ErrorNote error={error} />
        <button type="submit" className="button" disabled={submitting}>
          {submitting ? '로그인 중…' : '로그인'}
        </button>
      </form>

      {KAKAO_KEY ? (
        <a className="button button--kakao" href={kakaoAuthorizeUrl()}>
          카카오로 로그인
        </a>
      ) : (
        <p className="field__hint">카카오 로그인을 쓰려면 .env에 VITE_KAKAO_REST_API_KEY를 넣으세요.</p>
      )}

      <p className="auth__switch">
        처음이신가요? <Link to="/signup">회원가입</Link>
      </p>
    </div>
  );
}
