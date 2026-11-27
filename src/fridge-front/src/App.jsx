import { Navigate, Route, Routes } from 'react-router-dom';
import RequireAuth from './auth/RequireAuth';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import KakaoCallbackPage from './pages/KakaoCallbackPage';
import NicknamePage from './pages/NicknamePage';
import MyPage from './pages/MyPage';
import FridgeListPage from './pages/FridgeListPage';
import FridgeDetailPage from './pages/FridgeDetailPage';
import IngredientPage from './pages/IngredientPage';
import NotificationsPage from './pages/NotificationsPage';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/oauth/kakao/callback" element={<KakaoCallbackPage />} />
      <Route
        path="/nickname"
        element={
          <RequireAuth>
            <NicknamePage />
          </RequireAuth>
        }
      />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/fridges" element={<FridgeListPage />} />
        <Route path="/fridges/:fridgeId" element={<FridgeDetailPage />} />
        <Route path="/fridges/:fridgeId/ingredients/:ingredientId" element={<IngredientPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/me" element={<MyPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/fridges" replace />} />
    </Routes>
  );
}
