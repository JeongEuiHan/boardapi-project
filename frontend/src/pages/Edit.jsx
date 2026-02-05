import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function Edit() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    nowPassword: "",
    newPassword: "",
    newPasswordCheck: "",
    nickname: "",
  });

  const [errors, setErrors] = useState({}); // { field: message }
  const [loading, setLoading] = useState(false);

  // 내 정보 불러와서 nickname 채우기
  useEffect(() => {
    api
      .get("/api/users/me")
      .then((res) => {
        // 응답이 엔티티 그대로면 nickname이 res.data.nickname
        // DTO면 res.data.user.nickname 일 수도 있어서 방어
        const nick = res?.data?.nickname ?? res?.data?.user?.nickname ?? "";
        setForm((prev) => ({ ...prev, nickname: nick }));
      })
      .catch((err) => {
        if (err.response?.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
        } else {
          console.error(err);
        }
      });
  }, [navigate]);

  const onChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  // 백엔드가 getAllErrors() (배열)로 내려주는 경우도 대응
  const normalizeErrors = (data) => {
    // 1) 이미 {field: message} 형태면 그대로
    if (data && !Array.isArray(data) && typeof data === "object") return data;

    // 2) 배열이면 FieldError 리스트일 가능성
    const mapped = {};
    if (Array.isArray(data)) {
      data.forEach((e) => {
        const field = e?.field; // FieldError면 있음
        const msg = e?.defaultMessage;
        if (field && msg && !mapped[field]) mapped[field] = msg;
      });
    }

    // 3) 그래도 비었으면 global로 한 줄
    if (Object.keys(mapped).length === 0 && data) {
      mapped.global = typeof data === "string" ? data : "요청 값이 올바르지 않습니다.";
    }
    return mapped;
  };

  const submit = async (e) => {
    e.preventDefault();
    setErrors({});

    // 프론트 1차 검증(필수는 nowPassword, nickname 정도)
    if (!form.nowPassword.trim()) {
      setErrors({ nowPassword: "현재 비밀번호를 입력해주세요." });
      return;
    }
    if (!form.nickname.trim()) {
      setErrors({ nickname: "닉네임을 입력해주세요." });
      return;
    }
    if (form.newPassword || form.newPasswordCheck) {
      if (form.newPassword !== form.newPasswordCheck) {
        setErrors({ newPasswordCheck: "비밀번호가 일치하지 않습니다." });
        return;
      }
    }

    setLoading(true);
    try {
      await api.post("/api/users/edit", form);

      alert("회원 정보가 수정되었습니다.");
      navigate("/mypage");
    } catch (err) {
      console.error(err);
      const status = err?.response?.status;

      if (status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login");
        return;
      }

      if (status === 400) {
        setErrors(normalizeErrors(err.response?.data));
        return;
      }

      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        (typeof err?.response?.data === "string" ? err.response.data : "");

      if (status === 403) {
        setErrors({ global: msg || "권한이 없습니다." });
        return;
      }

      setErrors({ global: msg || "수정 중 오류가 발생했습니다." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="row">
      <form className="offset-4 col-4 form-group user-form" onSubmit={submit} align="left">
        <h3 style={{ marginBottom: 16 }}>회원정보 수정</h3>

        {errors.global && (
          <div style={{ color: "crimson", marginBottom: 10 }}>{errors.global}</div>
        )}

        {/* 현재 비밀번호 */}
        <div>
          <label>현재 비밀번호</label>
          <input
            type="password"
            name="nowPassword"
            value={form.nowPassword}
            onChange={onChange}
            placeholder="현재 비밀번호를 입력해주세요."
            className={errors.nowPassword ? "error-input" : ""}
            disabled={loading}
          />
          {errors.nowPassword && <div className="error-div">{errors.nowPassword}</div>}
        </div>

        <br />

        {/* 새 비밀번호 */}
        <div>
          <label>새 비밀번호 (선택)</label>
          <input
            type="password"
            name="newPassword"
            value={form.newPassword}
            onChange={onChange}
            placeholder="새 비밀번호를 입력해주세요."
            className={errors.newPassword ? "error-input" : ""}
            disabled={loading}
          />
          {errors.newPassword && <div className="error-div">{errors.newPassword}</div>}
        </div>

        <br />

        {/* 새 비밀번호 확인 */}
        <div>
          <label>새 비밀번호 확인</label>
          <input
            type="password"
            name="newPasswordCheck"
            value={form.newPasswordCheck}
            onChange={onChange}
            placeholder="새 비밀번호를 한 번 더 입력해주세요."
            className={errors.newPasswordCheck ? "error-input" : ""}
            disabled={loading}
          />
          {errors.newPasswordCheck && (
            <div className="error-div">{errors.newPasswordCheck}</div>
          )}
        </div>

        <br />

        {/* 닉네임 */}
        <div>
          <label>닉네임</label>
          <input
            type="text"
            name="nickname"
            value={form.nickname}
            onChange={onChange}
            placeholder="닉네임을 입력해주세요.(최대 10자)"
            className={errors.nickname ? "error-input" : ""}
            disabled={loading}
          />
          {errors.nickname && <div className="error-div">{errors.nickname}</div>}
        </div>

        <br />

        <div align="center">
          <button className="btn user-btn" type="submit" disabled={loading}>
            {loading ? "수정 중..." : "수정하기"}
          </button>

          <button
            type="button"
            className="btn"
            style={{ marginLeft: 10 }}
            onClick={() => navigate(-1)}
            disabled={loading}
          >
            뒤로
          </button>
        </div>
      </form>
    </div>
  );
}
