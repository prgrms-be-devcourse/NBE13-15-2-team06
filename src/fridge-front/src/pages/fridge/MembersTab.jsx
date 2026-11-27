import { useState } from 'react';
import { memberApi } from '../../api/endpoints';
import { useLoad } from '../../api/useLoad';
import { useAuth } from '../../auth/AuthContext';
import { ErrorNote, Loading } from '../../components/ui';
import { formatDate, ROLE_LABEL } from '../../components/format';

export default function MembersTab({ fridgeId, isAdmin }) {
  const { me } = useAuth();
  const { data: members, error, loading, reload } = useLoad(() => memberApi.list(fridgeId), [fridgeId]);
  const [actionError, setActionError] = useState(null);

  const run = async (action) => {
    setActionError(null);
    try {
      await action();
      reload();
    } catch (e) {
      setActionError(e);
    }
  };

  const handleKick = (member) => {
    if (!window.confirm(`${member.nickname}님을 냉장고에서 내보낼까요?`)) return;
    run(() => memberApi.kick(fridgeId, member.memberId));
  };

  if (loading && !members) return <Loading />;

  return (
    <>
      <ErrorNote error={error} />
      <ErrorNote error={actionError} />
      <ul className="rows">
        {members?.map((member) => {
          const isMe = member.userId === me?.userId;
          return (
            <li key={member.memberId} className="row">
              <span className="row__main">
                {member.nickname ?? '닉네임 미설정'}
                {isMe && <span className="badge">나</span>}
              </span>
              <span className="row__meta">
                <span>{ROLE_LABEL[member.role]}</span>
                <span>{formatDate(member.joinedAt)} 참여</span>
              </span>
              {isAdmin && !isMe && (
                <span className="row__actions">
                  <select
                    value={member.role}
                    onChange={(e) => run(() => memberApi.changeRole(fridgeId, member.memberId, e.target.value))}
                    aria-label={`${member.nickname} 역할`}
                  >
                    <option value="ADMIN">관리자</option>
                    <option value="MEMBER">구성원</option>
                  </select>
                  <button type="button" className="button button--danger-quiet" onClick={() => handleKick(member)}>
                    내보내기
                  </button>
                </span>
              )}
            </li>
          );
        })}
      </ul>
    </>
  );
}
