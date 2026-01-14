import { Outlet, useLocation } from "react-router-dom";
import Header from "../components/Header";

/**
 * 페이지별 active 메뉴 표시를 위해
 * URL → pageName으로 매핑
 */
function getPageName(pathname) {
  if (pathname === "/") return "home";
  if (pathname.startsWith("/boards/greeting")) return "greeting";
  if (pathname.startsWith("/boards/free")) return "free";
  if (pathname.startsWith("/boards/gold")) return "gold";
  return "";
}

export default function MainLayout() {
  const { pathname } = useLocation();
  const pageName = getPageName(pathname);

  return (
    <>
      {/* ✅ 공통 Header */}
      <Header pageName={pageName} />

      {/* ✅ 페이지 내용 */}
      <div className="container">
        <Outlet />
      </div>
    </>
  );
}
