import { Routes, Route, Navigate } from "react-router-dom";
import { useEffect } from "react";
import api from "./api/axios";

import MainLayout from "./layouts/MainLayout";

import Home from "./pages/Home";
import BoardList from "./pages/BoardList";
import BoardDetail from "./pages/BoardDetail";
import BoardWrite from "./pages/BoardWrite";

import Join from "./pages/Join";
import Login from "./pages/Login";
import Logout from "./pages/Logout";
import Edit from "./pages/Edit";
import DeleteAccount from "./pages/DeleteAccount"

import MyPage from "./pages/MyPage";
import AdminUser from "./pages/AdminUser";

function App() {
  // 앱 시작 시 로그인 상태 확인
  useEffect(() => {
    const checkLogin = async () => {
      try {
        await api.get("/api/users/me");
      } catch (e) {
              const status = e?.response?.status;
              if (status === 401) {
                localStorage.removeItem("accessToken");
              }
      }
    };
    checkLogin();
  }, []);

  return (
    <Routes>
      {/* 레이아웃(헤더) 적용 구간 */}
      <Route element={<MainLayout />}>
        {/* 홈 */}
        <Route path="/" element={<Home />} />

        {/* 게시판 */}
        <Route path="/boards/:category" element={<BoardList />} />
        <Route path="/boards/:category/write" element={<BoardWrite />} />
        <Route path="/boards/:category/:boardId" element={<BoardDetail />} />

        <Route path="/edit" element={<Edit />} />
        <Route path="/delete" element={<DeleteAccount />} />

      {/* /mypage로 오면 기본 탭으로 리다이렉트 */}
        <Route path="/mypage" element={<Navigate to="/mypage/board" replace />} />
      {/* 실제 마이페이지 */}
        <Route path="/mypage/:category" element={<MyPage />} />

        {/* 관리자 */}
        <Route path="/admin/user" element={<AdminUser />} />
      </Route>

      {/* 레이아웃(헤더) 없이 쓰고 싶은 페이지들 */}
      <Route path="/join" element={<Join />} />
      <Route path="/login" element={<Login />} />
      <Route path="/logout" element={<Logout />} />

      {/* 404 */}
      <Route path="*" element={<div>페이지 없음</div>} />
    </Routes>
  );
}

export default App;
