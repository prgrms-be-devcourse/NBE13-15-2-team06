import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { me, signOut } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await signOut();
    navigate('/login');
  };

  return (
    <div className="shell">
      <header className="topbar">
        <NavLink to="/fridges" className="brand">
          냉장고를 부탁해
        </NavLink>
        <nav className="topbar__nav" aria-label="주요 메뉴">
          <NavLink to="/fridges">내 냉장고</NavLink>
          <NavLink to="/notifications">알림</NavLink>
          <NavLink to="/me">{me?.nickname ?? '내 정보'}</NavLink>
          <button type="button" className="link-button" onClick={handleLogout}>
            로그아웃
          </button>
        </nav>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
