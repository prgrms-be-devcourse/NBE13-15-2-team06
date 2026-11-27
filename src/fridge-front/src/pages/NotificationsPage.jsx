import { Link } from 'react-router-dom';
import { notificationApi } from '../api/endpoints';
import { useLoad } from '../api/useLoad';
import { Empty, ErrorNote, Loading } from '../components/ui';
import { formatDateTime } from '../components/format';

const TYPE_LABEL = { EXPIRING_SOON: '기한 임박', EXPIRED: '기한 지남' };

export default function NotificationsPage() {
  const { data: notifications, error, loading, setData } = useLoad(() => notificationApi.list(), []);

  const markRead = async (id) => {
    try {
      await notificationApi.read(id);
      setData((list) => list.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n)));
    } catch {
      // 읽음 처리에 실패해도 이동은 계속한다.
    }
  };

  return (
    <>
      <h1 className="page__title">알림</h1>
      {loading && <Loading />}
      <ErrorNote error={error} />
      {notifications?.length === 0 && <Empty>새 알림이 없어요. 기한이 다가오면 여기로 알려 드릴게요.</Empty>}
      <ul className="rows">
        {notifications?.map((n) => (
          <li key={n.notificationId}>
            <Link
              to={n.ingredientId ? `/fridges/${n.groupId}/ingredients/${n.ingredientId}` : `/fridges/${n.groupId}`}
              className={`row row--link${n.isRead ? ' is-read' : ''}`}
              onClick={() => !n.isRead && markRead(n.notificationId)}
            >
              <span className={`badge badge--${n.type === 'EXPIRED' ? 'gone' : 'soon'}`}>
                {TYPE_LABEL[n.type] ?? n.type}
              </span>
              <span className="row__main">{n.message}</span>
              <span className="row__meta">
                <span>{formatDateTime(n.createdAt)}</span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </>
  );
}
