import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";
import MyPageHeader from "../components/MyPageHeader";

export default function MyPage() {
  const navigate = useNavigate();
  const { category: rawCategory } = useParams();
  const category = rawCategory || "board"; // board | like | comment

  const [me, setMe] = useState(null);
  const [boards, setBoards] = useState([]);
  const [loading, setLoading] = useState(true);

  // ✅ UI filter (client-side)
  const [filter, setFilter] = useState("");

  const normalizeBoards = useMemo(() => {
    return (data) => {
      const b = data?.boards ?? data;
      if (Array.isArray(b)) return b;
      if (b && Array.isArray(b.content)) return b.content;
      if (data?.boards?.content && Array.isArray(data.boards.content)) return data.boards.content;
      return [];
    };
  }, []);

  useEffect(() => {
    api
      .get("/api/users/me")
      .then((res) => setMe(res.data))
      .catch((err) => {
        if (err.response?.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
        } else {
          console.error(err);
        }
      });
  }, [navigate]);

  useEffect(() => {
    setLoading(true);
    api
      .get(`/api/users/mypage/${category}`)
      .then((res) => setBoards(normalizeBoards(res.data)))
      .catch((err) => {
        if (err.response?.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
        } else {
          console.error(err);
        }
      })
      .finally(() => setLoading(false));
  }, [category, navigate, normalizeBoards]);

  const goTab = (tab) => navigate(`/mypage/${tab}`);

  const tabLabel = (tab) => {
    if (tab === "board") return "작성한 글";
    if (tab === "like") return "좋아요 누른 글";
    if (tab === "comment") return "댓글 단 글";
    return tab;
  };

  const toCategoryName = (c) => {
    const lower = (c || "").toLowerCase();
    if (lower === "greeting") return "가입인사";
    if (lower === "free") return "자유게시판";
    if (lower === "gold") return "골드게시판";
    return lower || "-";
  };

  const goBoardDetail = (board) => {
    if (!board?.id) return;
    const boardCategory = (board.category || "").toLowerCase();
    navigate(`/boards/${boardCategory}/${board.id}`);
  };

  const filteredBoards = useMemo(() => {
    const q = filter.trim().toLowerCase();
    if (!q) return boards ?? [];
    return (boards ?? []).filter((b) => {
      const t = (b.title ?? "").toLowerCase();
      const n = (b.userNickname ?? b.userLoginId ?? "").toLowerCase();
      return t.includes(q) || n.includes(q);
    });
  }, [boards, filter]);

  return (
    <div className="page">
      <MyPageHeader user={me} />

      {/* Tabs + Filter */}
      <div className="surface" style={{ marginTop: 16 }}>
        <div className="mypage-toolbar">
          <div className="segmented">
            {["board", "like", "comment"].map((t) => (
              <button
                key={t}
                className={t === category ? "segmented-btn active" : "segmented-btn"}
                onClick={() => goTab(t)}
                type="button"
              >
                {tabLabel(t)}
              </button>
            ))}
          </div>

          <div className="mypage-right">
            <div className="count-chip">총 {filteredBoards.length}개</div>
            <input
              className="input"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              placeholder="제목/작성자 검색"
            />
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="surface" style={{ marginTop: 16, padding: 0 }}>
        {loading ? (
          <div style={{ padding: 16 }} className="muted">
            로딩중...
          </div>
        ) : (
          <table className="table list-table table-hover text-center" style={{ marginBottom: 0 }}>
            <thead>
              <tr>
                <th style={{ width: "16%" }}>게시판</th>
                <th style={{ width: "20%" }}>작성자</th>
                <th style={{ width: "35%" }}>제목</th>
                <th style={{ width: "7%" }}>좋아요</th>
                <th style={{ width: "22%" }}>작성일</th>
              </tr>
            </thead>
            <tbody>
              {filteredBoards.map((b) => (
                <tr key={b.id} style={{ cursor: "pointer" }} onClick={() => goBoardDetail(b)}>
                  <td>{toCategoryName(b.category)}</td>
                  <td>{b.userNickname ?? b.userLoginId ?? "-"}</td>
                  <td className="text-start">{b.title}</td>
                  <td>{b.likeCnt ?? 0}</td>
                  <td>{b.createdAt ?? ""}</td>
                </tr>
              ))}
              {filteredBoards.length === 0 && (
                <tr>
                  <td colSpan={5} className="muted" style={{ padding: 20 }}>
                    표시할 항목이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
