import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function Join() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    loginId: "",
    password: "",
    passwordCheck: "",
    nickname: "",
  });

  const [errors, setErrors] = useState({}); // { loginId: "...", password: "..." }

  const onChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const submitJoin = async (e) => {
    e.preventDefault();
    setErrors({}); // 기존 에러 초기화

    try {
      await axios.post(
        "http://localhost:8084/api/users/join",
        form,
        { withCredentials: true }
      );

      alert("회원가입에 성공했습니다!");
      navigate("/login");
    } catch (err) {
      // 🔥 Validation 에러 처리
      if (err.response && err.response.status === 400) {
        setErrors(err.response.data); // { field: message }
      } else {
        alert("회원가입 중 오류가 발생했습니다.");
      }
    }
  };

  return (
    <div className="row">
      <form
        className="offset-4 col-4 form-group user-form"
        onSubmit={submitJoin}
        align="left"
      >
        {/* 아이디 */}
        <div>
          <label>아이디 (아이디는 변경할 수 없습니다)</label>
          <input
            type="text"
            name="loginId"
            value={form.loginId}
            onChange={onChange}
            placeholder="아이디를 입력해주세요.(최대 10자)"
            className={errors.loginId ? "error-input" : ""}
          />
          {errors.loginId && (
            <div className="error-div">{errors.loginId}</div>
          )}
        </div>

        <br />

        {/* 비밀번호 */}
        <div>
          <label>비밀번호</label>
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={onChange}
            placeholder="비밀번호를 입력해주세요."
            className={errors.password ? "error-input" : ""}
          />
          {errors.password && (
            <div className="error-div">{errors.password}</div>
          )}
        </div>

        <br />

        {/* 비밀번호 체크 */}
        <div>
          <label>비밀번호 체크</label>
          <input
            type="password"
            name="passwordCheck"
            value={form.passwordCheck}
            onChange={onChange}
            placeholder="비밀번호를 한 번 더 입력해주세요."
            className={errors.passwordCheck ? "error-input" : ""}
          />
          {errors.passwordCheck && (
            <div className="error-div">{errors.passwordCheck}</div>
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
            placeholder="사용할 닉네임을 입력해주세요.(최대 10자)"
            className={errors.nickname ? "error-input" : ""}
          />
          {errors.nickname && (
            <div className="error-div">{errors.nickname}</div>
          )}
        </div>

        <br /><br />

        <div align="center">
          <button className="btn user-btn" type="submit">
            회원가입
          </button>
        </div>
      </form>
    </div>
  );
}
