import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";

const BACKEND_BASE = "http://localhost:8084"; // ✅ 너 백엔드 포트에 맞춰

function formatDate(str) {
  return str ?? "";
}

export default function BoardDetail() {
  const { category, boardId } = useParams();
  const navigate = useNavigate();

  const [me, setMe] = useState(null);
  const [loading, setLoading] = useState(true);

  const [board, setBoard] = useState(null);
  const [comments, setComments] = useState([]);
  const [likeCheck, setLikeCheck] = useState(false);

  const [commentBody, setCommentBody] = useState("");

  const [editMode, setEditMode] = useState(false);
  const [edit, setEdit] = useState({ title: "", body: "", newImage: null });

  // ✅ 댓글 수정 UI state
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingBody, setEditingBody] = useState("");

  const isAuthed = useMemo(() => me !== null, [me]);
  const isAdmin = useMemo(() => me?.userRole === "ADMIN", [me]);
  const isOwner = useMemo(() => {
    if (!board || !me?.loginId) return false;
    return board.userLoginId === me.loginId;
  }, [board, me]);
  const canManage = isAdmin || isOwner;

  // ✅ 이미지 URL 만들기 (방법 A)
  const imageUrl = useMemo(() => {
    const img = board?.uploadImage;
    if (!img) return null;

    // uploadImage가 이미 URL 문자열로 오는 경우
    if (typeof img === "string") {
      if (img.startsWith("http")) return img;
      return `${BACKEND_BASE}/api/boards/images/${img}`;
    }

    // uploadImage가 객체로 오는 경우 (UploadImageDto)
    const saved = img?.savedFilename;
    if (saved) return `${BACKEND_BASE}/api/boards/images/${saved}`;

    return null;
  }, [board]);

  const downloadUrl = useMemo(() => {
    const img = board?.uploadImage;
    if (!img || typeof img !== "object") return null;
    if (!img?.savedFilename) return null;
    return `${BACKEND_BASE}/api/boards/images/download/${board.id}`;
  }, [board]);

  const fetchDetail = async () => {
    const res = await api.get(`/api/boards/${boardId}`, { params: { category } });
    const data = res.data;

    setBoard(data.board);
    setComments(data.comments || []);
    setLikeCheck(!!data.likeCheck);

    setEdit({
      title: data.board?.title || "",
      body: data.board?.body || "",
      newImage: null,
    });

    // 댓글 수정 중이었다면(저장/삭제 등) 초기화
    setEditingCommentId(null);
    setEditingBody("");
  };

  useEffect(() => {
    (async () => {
      try {
        await api.get("/api/users/me").then((r) => setMe(r.data)).catch(() => setMe(null));
        await fetchDetail();
      } catch (e) {
        console.error(e);
        const status = e?.response?.status;

        if (status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
          return;
        }
        if (status === 403) {
          alert("권한이 없습니다.");
          navigate(`/boards/${category}`);
          return;
        }
        if (status === 404) {
          alert("게시글을 찾을 수 없습니다.");
          navigate(`/boards/${category}`);
          return;
        }

        alert("게시글을 불러오지 못했습니다.");
        navigate(`/boards/${category}`);
      } finally {
        setLoading(false);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category, boardId]);

  const toggleLike = async () => {
    if (!isAuthed) return alert("로그인이 필요합니다.");

    try {
      if (likeCheck) {
        await api.delete(`/api/likes/${boardId}`);
      } else {
        await api.post(`/api/likes/${boardId}`);
      }
      await fetchDetail();
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("좋아요 처리 실패");
    }
  };

  const addComment = async (e) => {
    e.preventDefault();
    if (!isAuthed) return alert("로그인이 필요합니다.");
    if (!commentBody.trim()) return;

    try {
      await api.post(`/api/comments/${boardId}`, { body: commentBody.trim() });
      setCommentBody("");
      await fetchDetail();
    } catch (e2) {
      console.error(e2);
      const status = e2?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("댓글 작성 실패");
    }
  };

  const deleteComment = async (commentId) => {
    const ok = window.confirm("댓글을 삭제하시겠습니까?");
    if (!ok) return;

    try {
      await api.delete(`/api/comments/${commentId}`);
      await fetchDetail();
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("댓글 삭제 실패");
    }
  };

  // ✅ 댓글 수정 시작/취소/저장
  const startCommentEdit = (c) => {
    setEditingCommentId(c.id);
    setEditingBody(c.body ?? "");
  };

  const cancelCommentEdit = () => {
    setEditingCommentId(null);
    setEditingBody("");
  };

  const saveCommentEdit = async (commentId) => {
    const trimmed = editingBody.trim();
    if (!trimmed) return alert("댓글 내용을 입력해주세요.");

    try {
      await api.put(`/api/comments/${commentId}`, { body: trimmed });

      // 즉시 화면 반영(굳이 fetchDetail 안 해도 됨)
      setComments((prev) => prev.map((x) => (x.id === commentId ? { ...x, body: trimmed } : x)));

      cancelCommentEdit();
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("댓글 수정 실패");
    }
  };

  const startEdit = () => {
    if (!canManage) return alert("권한이 없습니다.");
    setEditMode(true);
  };

  const cancelEdit = () => {
    setEditMode(false);
    setEdit({ title: board?.title || "", body: board?.body || "", newImage: null });
  };

  const saveEdit = async (e) => {
    e.preventDefault();
    try {
      const form = new FormData();
      form.append(
        "request",
        new Blob([JSON.stringify({ title: edit.title, body: edit.body })], {
          type: "application/json",
        })
      );
      if (edit.newImage) form.append("uploadImage", edit.newImage);

      await api.put(`/api/boards/${boardId}`, form, { params: { category } });

      alert("수정되었습니다.");
      await fetchDetail();
      setEditMode(false);
    } catch (e2) {
      console.error(e2);
      const status = e2?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("수정 실패");
    }
  };

  const deleteBoard = async () => {
    if (!canManage) return alert("권한이 없습니다.");
    const ok = window.confirm("게시글을 삭제하시겠습니까?");
    if (!ok) return;

    try {
      await api.delete(`/api/boards/${boardId}`, { params: { category } });
      alert("삭제되었습니다.");
      navigate(`/boards/${category}`);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("삭제 실패");
    }
  };

  if (loading) {
    return (
      <div className="page">
        <div className="muted">로딩중...</div>
      </div>
    );
  }

  if (!board) {
    return (
      <div className="page">
        <div className="muted">게시글을 불러오지 못했습니다.</div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="detail-header">
        <button className="btn ghost-btn" onClick={() => navigate(-1)}>
          ← 목록
        </button>

        <div className="detail-actions">
          <button className="btn ghost-btn" onClick={toggleLike}>
            <img
              alt="like"
              className="icon"
              src={likeCheck ? "/images/fill-hearts.svg" : "/images/empty-hearts.svg"}
            />
            좋아요 {board.likeCnt ?? 0}
          </button>

          {canManage && !editMode && (
            <>
              <button className="btn ghost-btn" onClick={startEdit}>
                수정
              </button>
              <button className="btn danger-btn" onClick={deleteBoard}>
                삭제
              </button>
            </>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="surface">
        {!editMode ? (
          <>
            <div className="detail-title-row">
              <h2 className="detail-title">{board.title}</h2>
              <div className="muted">{toWriter(board)}</div>
            </div>

            <div className="detail-meta">
              <span className="pill">{String(category).toUpperCase()}</span>
              <span className="muted">작성일 {formatDate(board.createdAt)}</span>
              <span className="muted">수정일 {formatDate(board.lastModifiedAt)}</span>
            </div>

            {imageUrl && (
              <div className="detail-image" style={{ marginTop: 12 }}>
                {downloadUrl && (
                  <div style={{ marginBottom: 8 }}>
                    이미지 :{" "}
                    <a href={downloadUrl} target="_blank" rel="noreferrer">
                      {board?.uploadImage?.originalFilename ?? "download"}
                    </a>
                  </div>
                )}
                <img
                  src={imageUrl}
                  alt="upload"
                  style={{
                    maxWidth: "100%",
                    borderRadius: 12,
                    border: "1px solid #e5e7eb",
                  }}
                  onError={(e) => {
                    console.error("이미지 로드 실패:", imageUrl);
                    e.currentTarget.style.display = "none";
                  }}
                />
              </div>
            )}

            <div className="detail-body">{board.body}</div>
          </>
        ) : (
          <form onSubmit={saveEdit} className="form-card">
            <div className="form-row">
              <label className="label">제목</label>
              <input
                className="input"
                value={edit.title}
                onChange={(e) => setEdit((p) => ({ ...p, title: e.target.value }))}
              />
            </div>

            <div className="form-row">
              <label className="label">내용</label>
              <textarea
                className="textarea"
                rows={10}
                value={edit.body}
                onChange={(e) => setEdit((p) => ({ ...p, body: e.target.value }))}
              />
            </div>

            <div className="form-row">
              <label className="label">이미지 변경</label>
              <input
                className="input"
                type="file"
                accept="image/*"
                onChange={(e) =>
                  setEdit((p) => ({ ...p, newImage: e.target.files?.[0] ?? null }))
                }
              />
            </div>

            <div className="form-actions">
              <button type="button" className="btn ghost-btn" onClick={cancelEdit}>
                취소
              </button>
              <button type="submit" className="btn primary-btn">
                저장
              </button>
            </div>
          </form>
        )}
      </div>

      {/* Comments */}
      <div className="surface" style={{ marginTop: 16 }}>
        <div className="section-head" style={{ marginBottom: 12 }}>
          <h3 className="section-title" style={{ fontSize: 18 }}>
            댓글
          </h3>
          <div className="muted">{comments.length}개</div>
        </div>

        <form className="comment-form" onSubmit={addComment}>
          <input
            className="input"
            value={commentBody}
            onChange={(e) => setCommentBody(e.target.value)}
            placeholder={isAuthed ? "댓글을 입력하세요" : "로그인 후 댓글을 작성할 수 있습니다"}
            disabled={!isAuthed}
          />
          <button
            className="btn primary-btn"
            type="submit"
            disabled={!isAuthed || !commentBody.trim()}
          >
            등록
          </button>
        </form>

        <div className="comment-list">
          {comments.map((c) => {
            const isCommentOwner = !!(me?.loginId && c.userLoginId === me.loginId);

            const canDelete = isAdmin || isCommentOwner;
            const canEdit = isCommentOwner; // ✅ 작성자만 수정. 관리자도 수정하게 하려면: isAdmin || isCommentOwner

            const isEditing = editingCommentId === c.id;

            return (
              <div key={c.id} className="comment-item">
                <div className="comment-top">
                  <div className="comment-writer">{c.userNickname ?? c.userLoginId ?? "-"}</div>
                  <div className="muted">{formatDate(c.createdAt)}</div>
                </div>

                {!isEditing ? (
                  <div className="comment-body">{c.body}</div>
                ) : (
                  <div style={{ marginTop: 8 }}>
                    <input
                      className="input"
                      value={editingBody}
                      onChange={(e) => setEditingBody(e.target.value)}
                      placeholder="댓글 수정 내용을 입력하세요"
                    />
                  </div>
                )}

                {(canEdit || canDelete) && (
                  <div
                    className="comment-actions"
                    style={{ display: "flex", gap: 8, marginTop: 8 }}
                  >
                    {!isEditing ? (
                      <>
                        {canEdit && (
                          <button
                            className="btn link-btn"
                            type="button"
                            onClick={() => startCommentEdit(c)}
                          >
                            수정
                          </button>
                        )}
                        {canDelete && (
                          <button
                            className="btn link-btn"
                            type="button"
                            onClick={() => deleteComment(c.id)}
                          >
                            삭제
                          </button>
                        )}
                      </>
                    ) : (
                      <>
                        <button
                          className="btn link-btn"
                          type="button"
                          onClick={() => saveCommentEdit(c.id)}
                          disabled={!editingBody.trim()}
                        >
                          저장
                        </button>
                        <button className="btn link-btn" type="button" onClick={cancelCommentEdit}>
                          취소
                        </button>
                      </>
                    )}
                  </div>
                )}
              </div>
            );
          })}

          {comments.length === 0 && <div className="muted">아직 댓글이 없습니다.</div>}
        </div>
      </div>
    </div>
  );
}

function toWriter(board) {
  const nick = board?.userNickname ?? "";
  const id = board?.userLoginId ?? "";
  if (nick && id) return `${nick} (${id})`;
  return nick || id || "-";
}
