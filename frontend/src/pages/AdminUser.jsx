import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/axios";

function formatDate(str) {
  return str ?? "";
}

export default function AdminUser() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [me, setMe] = useState(null);
  const [keyword, setKeyword] = useState(searchParams.get("keyword") || "");
  const [page, setPage] = useState(Number(searchParams.get("page")) || 1);

  const [users, setUsers] = useState([]); // content
  const [pageInfo, setPageInfo] = useState({}); // Page metadata
  const [loading, setLoading] = useState(true);

  const isAdmin = useMemo(() => me?.userRole === "ADMIN", [me]);

  // 로그인(토큰) 확인 + 내 정보 가져오기
  useEffect(() => {
    api
      .get("/api/users/me")
      .then((res) => {
        setMe(res.data);
      })
      .catch((err) => {
        console.error(err);
        alert("로그인이 필요합니다.");
        navigate("/login");
      });
  }, [navigate]);

  // 유저 목록 조회 (GET /api/users/admin)
  useEffect(() => {
    setLoading(true);
    setSearchParams({ page: String(page), keyword });

    api
      .get("/api/users/admin", {
        params: {
          page, // 서버가 page-1 처리하면 1-base 그대로 보냄
          keyword: keyword || "",
        },
      })
      .then((res) => {
        // 백엔드가 Page 자체를 리턴하는 형태: { content, totalPages, number, ... }
        setUsers(res.data.content || []);
        setPageInfo(res.data || {});
      })
      .catch((err) => {
        console.error(err);

        const status = err?.response?.status;
        if (status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
          return;
        }
        if (status === 403) {
          alert("권한이 없습니다.");
          navigate("/");
          return;
        }

        alert("유저 목록 조회 실패");
      })
      .finally(() => setLoading(false));
  }, [page, keyword, setSearchParams, navigate]);

  const onSearch = (e) => {
    e.preventDefault();
    setPage(1);
  };

  // 등급 변경 (PUT /api/users/admin/{userId}/role)
const toggleRole = async (userId) => {
  if (!me) {
    alert("로그인 정보 로딩중...");
    return;
  }
  if (!isAdmin) {
    alert("권한이 없습니다.");
    return;
  }

  const ok = window.confirm("해당 유저의 등급을 변경하시겠습니까?");
  if (!ok) return;

  try {
    const resp = await api.patch(`/api/users/admin/${userId}/role`, {});
    console.log("patch success:", resp.status, resp.data);

    const res = await api.get("/api/users/admin", {
      params: { page, keyword: keyword || "" },
    });
    setUsers(res.data.content || []);
    setPageInfo(res.data || {});
  } catch (err) {
    console.error("patch error:", err);

    if (err.response) {
      alert(`등급 변경 실패: ${err.response.status}\n${JSON.stringify(err.response.data)}`);
    } else {
      alert("등급 변경 실패: 서버 응답 없음(CORS/네트워크)");
    }
  }
};

  // 페이징(5개 묶음)
  const pagination = useMemo(() => {
    const totalPages = pageInfo.totalPages || 0;
    const nowPage = (pageInfo.number ?? page - 1) + 1; // pageInfo.number는 0-base

    if (totalPages === 0) {
      return {
        pages: [],
        nowPage,
        totalPages,
        hasPrev: false,
        hasNext: false,
        firstPage: 1,
        lastPage: 1,
      };
    }

    let firstPage = 1;
    for (let i = nowPage; i >= 1; i--) {
      if (i % 5 === 1) {
        firstPage = i;
        break;
      }
    }

    let lastPage = firstPage + 4;
    let hasNext = true;
    if (lastPage >= totalPages) {
      lastPage = totalPages;
      hasNext = false;
    }

    const hasPrev = firstPage !== 1;
    const pages = [];
    for (let i = firstPage; i <= lastPage; i++) pages.push(i);

    return { pages, nowPage, totalPages, hasPrev, hasNext, firstPage, lastPage };
  }, [pageInfo, page]);

  return (
    <>

      <div className="row">
        <div className="offset-1 col-10">
          {/* 검색 */}
          <form
            className="form-group"
            align="center"
            style={{ height: "40px" }}
            onSubmit={onSearch}
          >
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="닉네임을 입력해주세요"
              style={{ height: "100%", width: "30%" }}
            />
            <button className="btn search-btn" type="submit">
              검색
            </button>
          </form>

          <br />
          <br />

          {/* 테이블 */}
          {loading ? (
            <div>로딩중...</div>
          ) : (
            <table className="table list-table table-hover text-center">
              <thead style={{ backgroundColor: "#A5F1E9" }}>
                <tr>
                  <th style={{ width: "5%" }}>#</th>
                  <th style={{ width: "15%" }}>아이디</th>
                  <th style={{ width: "15%" }}>닉네임</th>
                  <th style={{ width: "10%" }}>등급</th>
                  <th style={{ width: "15%" }}>가입일</th>
                  <th style={{ width: "10%" }}>작성 글 수</th>
                  <th style={{ width: "10%" }}>작성 댓글 수</th>
                  <th style={{ width: "10%" }}>누른 좋아요 수</th>
                  <th style={{ width: "10%" }}>받은 좋아요 수</th>
                </tr>
              </thead>

              <tbody>
                {users.map((u) => (
                  <tr
                    key={u.id}
                    style={{ cursor: isAdmin ? "pointer" : "default" }}
                    onClick={() => toggleRole(u.id)}
                  >
                    <th style={{ width: "5%" }}>{u.id}</th>
                    <td style={{ width: "15%" }}>{u.loginId}</td>
                    <td style={{ width: "15%" }}>{u.nickname}</td>
                    <td style={{ width: "10%" }}>{u.userRole}</td>
                    <td style={{ width: "15%" }}>{formatDate(u.createdAt)}</td>
                    <td style={{ width: "10%" }}>{u.boardCount}</td>
                    <td style={{ width: "10%" }}>{u.commentCount}</td>
                    <td style={{ width: "10%" }}>{u.likeCount}</td>
                    <td style={{ width: "10%" }}>{u.receivedLikeCnt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* 페이징 */}
        <ul className="pagination justify-content-center">
          <li className="page-item">
            <button
              className="page-link"
              onClick={() => setPage(1)}
              disabled={pagination.totalPages === 0}
            >
              «
            </button>
          </li>

          {pagination.hasPrev && (
            <li className="page-item">
              <button
                className="page-link"
                onClick={() => setPage(pagination.firstPage - 1)}
              >
                ‹
              </button>
            </li>
          )}

          {pagination.pages.map((p) => (
            <li
              key={p}
              className={`page-item ${p === pagination.nowPage ? "active" : ""}`}
            >
              <button className="page-link" onClick={() => setPage(p)}>
                {p}
              </button>
            </li>
          ))}

          {pagination.hasNext && (
            <li className="page-item">
              <button
                className="page-link"
                onClick={() => setPage(pagination.lastPage + 1)}
              >
                ›
              </button>
            </li>
          )}

          <li className="page-item">
            <button
              className="page-link"
              onClick={() => setPage(pagination.totalPages)}
              disabled={pagination.totalPages === 0}
            >
              »
            </button>
          </li>
        </ul>
      </div>
    </>
  );
}
