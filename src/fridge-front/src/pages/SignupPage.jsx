import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { ErrorNote, Field } from '../components/ui';

export default function SignupPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await authApi.signup(email, password);
      // 가입 후 바로 로그인시켜서 닉네임 설정으로 보낸다.
      const result = await authApi.login(email, password);
      signIn(result.accessToken);
      navigate('/nickname', { replace: true });
    } catch (e) {
      setError(e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <h1 className="auth__title">회원가입</h1>
      <form className="stack" onSubmit={handleSubmit}>
        <Field label="이메일">
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required />
        </Field>
        <Field label="비밀번호" hint="8~20자, 영문·숫자·특수문자를 모두 포함">
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            required
          />
        </Field>
        <ErrorNote error={error} />
        <button type="submit" className="button" disabled={submitting}>
          {submitting ? '가입 중…' : '가입하기'}
        </button>
      </form>
      <p className="auth__switch">
        이미 계정이 있나요? <Link to="/login">로그인</Link>
      </p>
    </div>
  );
}
