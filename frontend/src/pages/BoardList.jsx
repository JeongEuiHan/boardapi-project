import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import api from "../api/axios";

export default function BoardList() {
  const { category } = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // ✅ URL → state 초기값 (UI는 1-base 유지)
  const initialSearch = useMemo(() => {
    const page = Number(searchParams.get("page")) || 1; // 1-base (UI)
    return {
      page: page < 1 ? 1 : page,
      sortType: searchParams.get("sortType") || "date", // date | like | comment
      searchType: searchParams.get("searchType") || "title", // title | nickname
      keyword: searchParams.get("keyword") || "",
    };
  }, [searchParams]);

  const [form, setForm] = useState(initialSearch);
  const [query, setQuery] = useState(initialSearch);

  const [boards, setBoards] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // ✅ category 바뀌면 page=1로 초기화 + URL도 초기화
  useEffect(() => {
    const next = { ...initialSearch, page: 1, keyword: "" };
    setForm(next);
    setQuery(next);
    setSearchParams({
      page: "1",
      sortType: next.sortType,
      searchType: next.searchType,
      keyword: next.keyword,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category]);

  // ✅ query 또는 category가 바뀔 때만 목록 조회
  useEffect(() => {
    fetchBoards();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, category]);

  // ✅ REST 표준 sort 파라미터로 변환
  const toSortParam = (sortType) => {
    switch (sortType) {
      case "like":
        return "likeCnt,desc";
      case "comment":
        return "commentCnt,desc";
      case "date":
      default:
        return "createdAt,desc";
    }
  };

  const fetchBoards = async () => {
    setLoading(true);
    setError("");

    try {
      const kw = query.keyword?.trim();

      const res = await api.get("/api/boards", {
        params: {
          category,
          page: Math.max(0, (query.page ?? 1) - 1),
          size: 10,
          sort: toSortParam(query.sortType),

          // ✅ 키워드 있을 때만
          searchType: kw ? query.searchType : undefined,
          keyword: kw ? kw : undefined,
        },
        headers: {
          "Cache-Control": "no-cache",
          Pragma: "no-cache",
        },
      });

      const data = res.data; // Page<BoardDto>

      setBoards(data?.content ?? []);
      setPageInfo({
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        number: data?.number ?? 0,
        size: data?.size ?? 10,
      });
    } catch (e) {
      console.error(e);

      setBoards([]);
      setPageInfo({ totalElements: 0, totalPages: 0, number: 0, size: 10 });

      const status = e?.response?.status;
      const msg = e?.response?.data?.message;

      if (status === 401) setError("로그인이 필요합니다.");
      else if (status === 403) setError("접근 권한이 없습니다.");
      else if (msg) setError(msg);
      else setError("게시글 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const next = { ...form, page: 1 };
    setQuery(next);
    setSearchParams({
      page: String(next.page),
      sortType: next.sortType,
      searchType: next.searchType,
      keyword: next.keyword,
    });
  };

  const movePage = (page) => {
    const next = { ...query, page };
    setQuery(next);
    setSearchParams({
      page: String(next.page),
      sortType: next.sortType,
      searchType: next.searchType,
      keyword: next.keyword,
    });
  };

  const formatDate = (value) => {
    if (!value) return "";
    const s = String(value).replace("T", " ");
    return s.length >= 16 ? s.slice(2, 16) : s;
  };

  const totalElements = pageInfo.totalElements ?? 0;
  const totalPages = pageInfo.totalPages ?? 0;
  const currentIndex0 = pageInfo.number ?? 0;
  const currentPage1 = currentIndex0 + 1;

  return (
    <>
      {/* 상단 영역 */}
      <div className="row mb-4">
        <div className="offset-1 col-2">
          <h5 style={{ height: "40px", display: "flex", alignItems: "center" }}>
            전체: {totalElements}개
          </h5>
        </div>

        {/* 검색 */}
        <div className="col-6 text-center">
          <form onSubmit={handleSearch}>
            <select
              value={form.sortType}
              onChange={(e) => setForm((prev) => ({ ...prev, sortType: e.target.value }))}
            >
              <option value="date">최신순</option>
              <option value="like">좋아요순</option>
              <option value="comment">댓글순</option>
            </select>

            <select
              value={form.searchType}
              onChange={(e) => setForm((prev) => ({ ...prev, searchType: e.target.value }))}
            >
              <option value="title">제목</option>
              <option value="nickname">작성자</option>
            </select>

            <input
              type="text"
              placeholder="검색 키워드를 입력해주세요"
              value={form.keyword}
              onChange={(e) => setForm((prev) => ({ ...prev, keyword: e.target.value }))}
            />

            <button className="btn search-btn" type="submit">
              검색
            </button>
          </form>
        </div>

        {/* 글 작성 */}
        <div className="col-1 text-end">
          <button className="btn post-btn" onClick={() => navigate(`/boards/${category}/write`)}>
            글 작성
          </button>
        </div>
      </div>

      {/* 상태 표시 */}
      {loading && (
        <div className="row">
          <div className="offset-2 col-8">불러오는 중...</div>
        </div>
      )}

      {error && !loading && (
        <div className="row">
          <div className="offset-2 col-8" style={{ color: "crimson" }}>
            {error}
          </div>
        </div>
      )}

      {/* 게시글 테이블 */}
      {!loading && !error && (
        <div className="row">
          <div className="offset-2 col-8">
            <div style={{ marginBottom: 8, opacity: 0.8 }}>
              현재 페이지: {currentPage1} / {Math.max(1, totalPages)}
            </div>

            <table className="table table-hover text-center">
              <thead style={{ backgroundColor: "#A5F1E9" }}>
                <tr>
                  <th style={{ width: "20%" }}>작성자</th>
                  <th style={{ width: "45%" }}>제목</th>
                  <th style={{ width: "10%" }}>좋아요</th>
                  <th style={{ width: "15%" }}>작성일</th>
                </tr>
              </thead>

              <tbody>
                {boards.length === 0 ? (
                  <tr>
                    <td colSpan={4}>게시글이 없습니다.</td>
                  </tr>
                ) : (
                  boards.map((b) => {
                    const isNotice = !!b.notice; // ✅ 백엔드에서 notice 내려준다고 가정

                    return (
                      <tr
                        key={b.id}
                        onClick={() => navigate(`/boards/${category}/${b.id}`)}
                        style={{
                          cursor: "pointer",
                          background: isNotice ? "rgba(255, 230, 0, 0.10)" : undefined,
                        }}
                      >
                        <td>{b.userNickname}</td>

                        <td style={{ textAlign: "left" }}>
                          {/* ✅ 공지 뱃지 */}
                          {isNotice && (
                            <span
                              style={{
                                display: "inline-flex",
                                alignItems: "center",
                                gap: 6,
                                padding: "2px 10px",
                                borderRadius: 999,
                                fontSize: 12,
                                fontWeight: 700,
                                marginRight: 8,
                                border: "1px solid rgba(255, 180, 0, 0.7)",
                                background: "rgba(255, 180, 0, 0.18)",
                              }}
                            >
                              📌 공지
                            </span>
                          )}

                          <span style={{ fontWeight: isNotice ? 700 : 500 }}>{b.title}</span>
                        </td>

                        <td>{b.likeCnt ?? 0}</td>
                        <td>{formatDate(b.createdAt)}</td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* 페이징 */}
          {totalPages > 1 && (
            <ul className="pagination justify-content-center">
              {[...Array(totalPages)].map((_, i) => {
                const page1 = i + 1;
                const active = currentIndex0 === i;
                return (
                  <li key={i} className={`page-item ${active ? "active" : ""}`}>
                    <button className="page-link" onClick={() => movePage(page1)}>
                      {page1}
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      )}
    </>
  );
}
