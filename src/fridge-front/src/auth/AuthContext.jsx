import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { tokenStore } from '../api/client';
import { authApi, userApi } from '../api/endpoints';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => tokenStore.get());
  const [me, setMe] = useState(null);
  const [expired, setExpired] = useState(false);

  const refreshMe = useCallback(async () => {
    if (!tokenStore.get()) {
      setMe(null);
      return null;
    }
    const profile = await userApi.me();
    setMe(profile);
    return profile;
  }, []);

  useEffect(() => {
    refreshMe().catch(() => {});
  }, [token, refreshMe]);

  // client.js가 401을 받으면 이 이벤트를 보낸다 → 로그인 화면으로
  useEffect(() => {
    const onExpired = () => {
      setToken(null);
      setMe(null);
      setExpired(true);
    };
    window.addEventListener('auth:expired', onExpired);
    return () => window.removeEventListener('auth:expired', onExpired);
  }, []);

  const signIn = useCallback((accessToken) => {
    tokenStore.set(accessToken);
    setExpired(false);
    setToken(accessToken);
  }, []);

  const signOut = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // 서버 응답과 상관없이 토큰은 버린다. 서버는 토큰을 기억하지 않는다.
    }
    tokenStore.clear();
    setToken(null);
    setMe(null);
  }, []);

  return (
    <AuthContext.Provider value={{ token, me, expired, signIn, signOut, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
