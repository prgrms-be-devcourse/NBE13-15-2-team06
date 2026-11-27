import { historyApi } from '../../api/endpoints';
import { useLoad } from '../../api/useLoad';
import { Empty, ErrorNote, Loading } from '../../components/ui';
import { ACTION_LABEL, formatDateTime } from '../../components/format';

export default function HistoryTab({ fridgeId }) {
  const { data: histories, error, loading } = useLoad(() => historyApi.byGroup(fridgeId), [fridgeId]);

  if (loading && !histories) return <Loading />;

  return (
    <>
      <ErrorNote error={error} />
      {histories?.length === 0 && <Empty>아직 기록이 없어요. 식재료를 먹거나 버리면 여기에 남아요.</Empty>}
      <ol className="timeline">
        {histories?.map((h) => (
          <li key={h.id}>
            <time className="timeline__time">{formatDateTime(h.createdAt)}</time>
            <span>
              <strong>{h.nickname ?? '알 수 없음'}</strong>님이 {h.ingredientName}을(를){' '}
              {ACTION_LABEL[h.action] ?? h.action}
              {h.quantityChange != null && h.quantityChange !== 0 && (
                <span className="muted"> ({h.quantityChange > 0 ? `+${h.quantityChange}` : h.quantityChange})</span>
              )}
            </span>
          </li>
        ))}
      </ol>
    </>
  );
}
