import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";

export default function BoardWrite() {
  const { category } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [uploadImage, setUploadImage] = useState(null);
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (!title.trim()) return alert("제목을 입력해주세요.");
    if (!body.trim()) return alert("내용을 입력해주세요.");

    try {
      setLoading(true);

      const formData = new FormData();
      formData.append(
        "request",
        new Blob([JSON.stringify({ title: title.trim(), body: body.trim() })], {
          type: "application/json",
        })
      );
      if (uploadImage) formData.append("uploadImage", uploadImage);

      await api.post("/api/boards", formData, { params: { category } });

      navigate(`/boards/${category}`);
    } catch (err) {
      console.error(err);
      const status = err?.response?.status;
      if (status === 401) return alert("로그인이 필요합니다.");
      if (status === 403) return alert("권한이 없습니다.");
      alert("글 작성 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="section-head" style={{ marginBottom: 12 }}>
        <h2 className="section-title">글 작성</h2>
        <div className="muted">{String(category).toUpperCase()} 게시판</div>
      </div>

      <form className="surface form-card" onSubmit={submit}>
        <div className="form-row">
          <label className="label">제목</label>
          <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="제목을 입력하세요" />
        </div>

        <div className="form-row">
          <label className="label">내용</label>
          <textarea
            className="textarea"
            rows={10}
            value={body}
            onChange={(e) => setBody(e.target.value)}
            placeholder="내용을 입력하세요"
          />
        </div>

        <div className="form-row">
          <label className="label">이미지</label>
          <input
            className="input"
            type="file"
            accept="image/*"
            onChange={(e) => setUploadImage(e.target.files?.[0] ?? null)}
          />
          <div className="muted" style={{ marginTop: 6 }}>
            (선택) 이미지를 첨부할 수 있습니다.
          </div>
        </div>

        <div className="form-actions">
          <button type="button" className="btn ghost-btn" onClick={() => navigate(-1)} disabled={loading}>
            취소
          </button>
          <button type="submit" className="btn primary-btn" disabled={loading}>
            {loading ? "등록 중..." : "등록"}
          </button>
        </div>
      </form>
    </div>
  );
}
