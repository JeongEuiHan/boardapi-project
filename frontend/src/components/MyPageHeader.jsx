import { useNavigate } from "react-router-dom";

/**
 * user = {
 *   loginId,
 *   nickname,
 *   userRole,
 *   receivedLikeCnt
 * }
 */
export default function MyPageHeader({ user }) {
  const navigate = useNavigate();

  if (!user) return null; // 아직 로딩 중일 때

  return (
    <div className="row">
      <div className="offset-3 col-6">
        <div className="card">
          <div className="card-body" style={{ overflow: "hidden" }}>
            {/* 왼쪽 유저 정보 */}
            <div style={{ float: "left" }}>
              <h4>Id: {user.loginId}</h4>
              <h4>닉네임: {user.nickname}</h4>
              <h4>등급: {user.userRole}</h4>
              <h4>받은 좋아요: {user.receivedLikeCnt}개</h4>
            </div>

            {/* 오른쪽 버튼 */}
            <div style={{ float: "right" }}>
              <button
                className="btn edit-btn"
                onClick={() => navigate("/edit")}
              >
                정보 수정
              </button>
              <br />
              <br />
              <button
                className="btn del-btn"
                onClick={() => navigate("/delete")}
              >
                회원 탈퇴
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
