import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { ErrorNote } from '../components/ui';

export default function KakaoCallbackPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  // 개발 모드(StrictMode)에선 useEffect가 두 번 실행된다.
  // 인가 코드는 한 번만 쓸 수 있어서, 두 번째 요청은 KAKAO_AUTH_FAILED가 난다. 그래서 한 번만 보낸다.
  const sent = useRef(false);

  useEffect(() => {
    if (sent.current) return;
    sent.current = true;

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    if (!code) {
      setError(new Error(params.get('error_description') ?? '카카오에서 인가 코드를 받지 못했습니다.'));
      return;
    }

    authApi
      .kakao(code)
      .then((result) => {
        signIn(result.accessToken);
        navigate(result.nicknameRequired ? '/nickname' : '/fridges', { replace: true });
      })
      .catch(setError);
  }, [navigate, signIn]);

  return (
    <div className="auth">
      <h1 className="auth__title">카카오 로그인</h1>
      {error ? (
        <>
          <ErrorNote error={error} />
          <Link to="/login" className="button">
            로그인 화면으로
          </Link>
        </>
      ) : (
        <p className="muted">로그인하는 중…</p>
      )}
    </div>
  );
}
