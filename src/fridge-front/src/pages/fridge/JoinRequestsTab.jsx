import { useState } from 'react';
import { joinRequestApi } from '../../api/endpoints';
import { useLoad } from '../../api/useLoad';
import { Empty, ErrorNote, Loading } from '../../components/ui';
import { formatDateTime } from '../../components/format';

export default function JoinRequestsTab({ fridgeId }) {
  const { data: requests, error, loading, reload } = useLoad(() => joinRequestApi.pending(fridgeId), [fridgeId]);
  const [actionError, setActionError] = useState(null);

  const decide = async (action, requestId) => {
    setActionError(null);
    try {
      await action(fridgeId, requestId);
      reload();
    } catch (e) {
      setActionError(e);
    }
  };

  if (loading && !requests) return <Loading />;

  return (
    <>
      <ErrorNote error={error} />
      <ErrorNote error={actionError} />
      {requests?.length === 0 && <Empty>기다리는 참여 신청이 없어요. 설정 탭에서 초대 코드를 확인해 공유해 보세요.</Empty>}
      <ul className="rows">
        {requests?.map((req) => (
          <li key={req.requestId} className="row">
            <span className="row__main">{req.nickname ?? '닉네임 미설정'}</span>
            <span className="row__meta">
              <span>{formatDateTime(req.requestedAt)} 신청</span>
            </span>
            <span className="row__actions">
              <button type="button" className="button" onClick={() => decide(joinRequestApi.approve, req.requestId)}>
                승인
              </button>
              <button
                type="button"
                className="button button--quiet"
                onClick={() => decide(joinRequestApi.reject, req.requestId)}
              >
                거절
              </button>
            </span>
          </li>
        ))}
      </ul>
    </>
  );
}
