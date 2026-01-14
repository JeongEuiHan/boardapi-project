import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";

export default function Home() {
  const [userCnt, setUserCnt] = useState(null);
  const [boardCnt, setBoardCnt] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get("/api/home")
      .then((res) => {
        setUserCnt(res.data.userCnt);
        setBoardCnt(res.data.boardCnt);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const quickCards = useMemo(
    () => [
      {
        title: "가입인사",
        desc: "BRONZE 전용. 가입 인사 작성 시 SILVER로 승급됩니다.",
        to: "/boards/GREETING",
        badge: "BRONZE",
      },
      {
        title: "자유게시판",
        desc: "SILVER 이상만 작성 가능. 다양한 주제로 이야기해요.",
        to: "/boards/FREE",
        badge: "SILVER+",
      },
      {
        title: "골드게시판",
        desc: "GOLD 이상만 접근 가능. 심화 게시판입니다.",
        to: "/boards/GOLD",
        badge: "GOLD+",
      },
    ],
    []
  );

  if (loading) return <div className="page"><div className="muted">로딩중...</div></div>;

  return (
    <div className="page">
      {/* Hero */}
      <section className="hero">
        <div className="hero-left">
          <div className="chip">Spring Boot · React · JWT</div>
          <h1 className="hero-title">깔끔하게 운영되는 게시판</h1>
          <p className="hero-sub">
            권한 기반 작성/조회, 좋아요 승급 로직, 관리자 기능까지 포함한 개인 프로젝트입니다.
          </p>
          <div className="hero-cta">
            <Link className="btn primary-btn" to="/boards/FREE">
              게시판 둘러보기
            </Link>
            <Link className="btn ghost-btn" to="/mypage">
              마이페이지
            </Link>
          </div>
          <div className="hero-meta">
            <div className="meta-item">
              <div className="meta-k">전체 유저</div>
              <div className="meta-v">{userCnt?.totalUserCnt ?? "-"}명</div>
            </div>
            <div className="meta-item">
              <div className="meta-k">전체 글</div>
              <div className="meta-v">{boardCnt?.totalBoardCnt ?? "-"}개</div>
            </div>
            <div className="meta-item">
              <div className="meta-k">공지</div>
              <div className="meta-v">{boardCnt?.totalNoticeCnt ?? "-"}개</div>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="surface">
            <h3 className="surface-title">핵심 규칙</h3>
            <ul className="rule-list">
              <li>회원가입 시 BRONZE</li>
              <li>가입인사 작성 시 SILVER 승급</li>
              <li>SILVER 이상만 자유게시판 작성 가능</li>
              <li>받은 좋아요 합계 10개 이상 → GOLD 승급</li>
              <li>GOLD 이상만 골드게시판 접근</li>
              <li>BLACKLIST는 글/댓글/좋아요 제한</li>
              <li>관리자는 글/댓글 관리 가능</li>
            </ul>
          </div>
        </div>
      </section>

      {/* Quick menu cards */}
      <section className="section">
        <div className="section-head">
          <h2 className="section-title">바로가기</h2>
          <div className="muted">게시판을 선택해 빠르게 이동하세요.</div>
        </div>

        <div className="card-grid">
          {quickCards.map((c) => (
            <Link key={c.title} className="card-link" to={c.to}>
              <div className="card">
                <div className="card-top">
                  <div className="card-title">{c.title}</div>
                  <div className="badge">{c.badge}</div>
                </div>
                <div className="card-desc">{c.desc}</div>
                <div className="card-cta">열기 →</div>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Stats */}
      <section className="section">
        <div className="section-head">
          <h2 className="section-title">현황</h2>
          <div className="muted">현재 서비스 데이터 요약입니다.</div>
        </div>

        <div className="two-col">
          <div className="surface">
            <h3 className="surface-title">유저</h3>
            <div className="kv">
              <div className="kv-row"><span>관리자</span><b>{userCnt?.totalAdminCnt ?? "-"}</b></div>
              <div className="kv-row"><span>BRONZE</span><b>{userCnt?.totalBronzeCnt ?? "-"}</b></div>
              <div className="kv-row"><span>SILVER</span><b>{userCnt?.totalSilverCnt ?? "-"}</b></div>
              <div className="kv-row"><span>GOLD</span><b>{userCnt?.totalGoldCnt ?? "-"}</b></div>
              <div className="kv-row"><span>BLACKLIST</span><b>{userCnt?.totalBlacklistCnt ?? "-"}</b></div>
            </div>
          </div>

          <div className="surface">
            <h3 className="surface-title">게시글</h3>
            <div className="kv">
              <div className="kv-row"><span>공지</span><b>{boardCnt?.totalNoticeCnt ?? "-"}</b></div>
              <div className="kv-row"><span>가입인사</span><b>{boardCnt?.totalGreetingCnt ?? "-"}</b></div>
              <div className="kv-row"><span>자유게시판</span><b>{boardCnt?.totalFreeCnt ?? "-"}</b></div>
              <div className="kv-row"><span>골드게시판</span><b>{boardCnt?.totalGoldCnt ?? "-"}</b></div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
