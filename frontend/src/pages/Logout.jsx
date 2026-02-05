import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function Logout() {
  const navigate = useNavigate();

  useEffect(() => {
    // JWT 로그아웃 핵심: 토큰 제거
    localStorage.removeItem("accessToken");

    // (선택) axios 기본 헤더에 Authorization을 따로 넣는 방식이면 같이 제거
    // delete api.defaults.headers.common["Authorization"];

    navigate("/login", { replace: true });
  }, [navigate]);

  return <div>로그아웃 중...</div>;
}
