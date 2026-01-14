import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function DeleteAccount() {
  const navigate = useNavigate();
  const [nowPassword, setNowPassword] = useState("");
  const [error, setError] = useState(null);

  const submitDelete = async () => {
    setError(null);

    if (!nowPassword) {
      setError("현재 비밀번호를 입력해주세요.");
      return;
    }

    const ok = confirm(
      "탈퇴하면 작성한 글, 댓글, 좋아요가 모두 삭제되고 계정을 복구할 수 없습니다.\n정말 탈퇴하시겠습니까?"
    );
    if (!ok) return;

    try {
      await api.post(
        "/api/users/delete",
        { nowPassword },
        { withCredentials: true }
      );

      localStorage.removeItem("accessToken");
      alert("탈퇴 되었습니다.");
      // 세션 기반이면 탈퇴 후 로그아웃 처리까지 해주는 게 제일 깔끔
      // 일단 홈으로 이동
      navigate("/");
    } catch (err) {
      console.error(err);
      if (err.response?.status === 400) {
        setError(err.response.data?.message ?? "탈퇴에 실패했습니다.");
      } else if (err.response?.status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login");
      } else {
        setError("서버 오류가 발생했습니다.");
      }
    }
  };

  return (
    <div className="row">
      <div className="offset-4 col-4 form-group user-form" align="left">
        <div>
          <label>현재 비밀번호</label>
          <br />
          <input
            type="password"
            value={nowPassword}
            onChange={(e) => setNowPassword(e.target.value)}
            placeholder="현재 비밀번호를 입력해주세요."
          />
        </div>

        <br />

        {error && <div className="error-div">{error}</div>}

        <div align="center">
          <button className="btn del-btn" type="button" onClick={submitDelete}>
            회원 탈퇴
          </button>
        </div>
      </div>
    </div>
  );
}
