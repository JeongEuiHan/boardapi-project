import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function Login() {
  const navigate = useNavigate();

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [fail, setFail] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setFail(false);

    try {
      const res = await api.post("/api/auth/login", { loginId, password });

      const token = res.data?.accessToken;
      if (!token) {
        console.log("login response:", res.data);
        throw new Error("accessToken이 응답에 없습니다.");
      }

      localStorage.setItem("accessToken", token);

      // 토큰이 실제로 붙는지 확인(선택이지만 디버깅에 좋음)
      await api.get("/api/users/me");

      navigate("/");
    } catch (err) {
      console.error(err);
      setFail(true);
    }
  };

  return (
    <div className="row">
      <form
        className="offset-4 col-4 form-group user-form"
        onSubmit={submit}
        align="left"
      >
        <div>
          <label>아이디</label>
          <br />
          <input
            type="text"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            placeholder="아이디를 입력해주세요."
          />
        </div>

        <br />

        <div>
          <label>비밀번호</label>
          <br />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력해주세요."
          />
        </div>

        <br />

        {fail && (
          <>
            <div className="error-div" align="center">
              아이디 또는 비밀번호가 일치하지 않거나 접근이 제한되었습니다.
            </div>
            <br />
          </>
        )}

        <div align="center">
          <button className="btn user-btn" type="submit">
            로그인
          </button>
        </div>
      </form>
    </div>
  );
}
