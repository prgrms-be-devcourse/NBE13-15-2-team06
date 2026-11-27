import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

// 토큰이 없으면 로그인으로, 닉네임이 없으면 닉네임 설정으로 보낸다.
export default function RequireAuth({ children }) {
  const { token, me } = useAuth();
  const location = useLocation();

  if (!token) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (me && me.nickname == null && location.pathname !== '/nickname') {
    return <Navigate to="/nickname" replace />;
  }
  return children;
}
