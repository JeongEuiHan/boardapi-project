import { Link, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import api from "../api/axios";

export default function Header({ pageName }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [me, setMe] = useState(null);
  const [loading, setLoading] = useState(true);

  const query = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const loginSuccess = query.get("loginSuccess");
  const blacklist = query.get("blacklist");

  useEffect(() => {
    if (loginSuccess) alert("반갑습니다!");
    if (blacklist) alert("블랙리스트 계정입니다. 글·댓글·좋아요 기능이 제한됩니다.");
  }, [loginSuccess, blacklist]);

  useEffect(() => {
    api
      .get("/api/users/me")
      .then((res) => setMe(res.data))
      .catch(() => setMe(null))
      .finally(() => setLoading(false));
  }, [location.pathname]);

  const isAuthed = !loading && me !== null;
  const isAdmin = isAuthed && me.userRole === "ADMIN";

  const active = (key) => (pageName === key ? "nav-link active" : "nav-link");

  const logout = () => {
    localStorage.removeItem("accessToken");
    setMe(null);
    navigate("/login");
  };

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <Link className="brand" to="/">
          Basic Board
        </Link>

        <nav className="topnav" aria-label="Primary">
          <Link className={active("home")} to="/">
            Home
          </Link>
          <Link className={active("greeting")} to="/boards/GREETING">
            가입인사
          </Link>
          <Link className={active("free")} to="/boards/FREE">
            자유게시판
          </Link>
          <Link className={active("gold")} to="/boards/GOLD">
            골드게시판
          </Link>
        </nav>

        <div className="topbar-actions">
          {!isAuthed && (
            <>
              <button className="btn nav-btn" onClick={() => navigate("/login")}>
                로그인
              </button>
              <button className="btn nav-btn" onClick={() => navigate("/join")}>
                회원가입
              </button>
            </>
          )}

          {isAuthed && (
            <>
              {isAdmin && (
                <button className="btn nav-btn" onClick={() => navigate("/admin/user")}>
                  관리자
                </button>
              )}
              <button className="btn nav-btn" onClick={() => navigate("/mypage")}>
                마이페이지
              </button>
              <button className="btn nav-btn" onClick={logout}>
                로그아웃
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
