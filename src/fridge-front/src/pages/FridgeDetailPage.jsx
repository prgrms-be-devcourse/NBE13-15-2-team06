import { Link, useParams, useSearchParams } from 'react-router-dom';
import { fridgeApi } from '../api/endpoints';
import { useLoad } from '../api/useLoad';
import { ErrorNote, Loading } from '../components/ui';
import { ROLE_LABEL } from '../components/format';
import IngredientsTab from './fridge/IngredientsTab';
import MembersTab from './fridge/MembersTab';
import JoinRequestsTab from './fridge/JoinRequestsTab';
import HistoryTab from './fridge/HistoryTab';
import SettingsTab from './fridge/SettingsTab';

const TABS = [
  { key: 'ingredients', label: '식재료' },
  { key: 'members', label: '구성원' },
  { key: 'requests', label: '참여 신청', adminOnly: true },
  { key: 'history', label: '기록' },
  { key: 'settings', label: '설정' },
];

export default function FridgeDetailPage() {
  const { fridgeId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = searchParams.get('tab') ?? 'ingredients';
  const { data: fridge, error, loading, reload } = useLoad(() => fridgeApi.get(fridgeId), [fridgeId]);

  if (loading && !fridge) return <Loading />;
  if (error) return <ErrorNote error={error} />;

  const isAdmin = fridge.myRole === 'ADMIN';
  const visibleTabs = TABS.filter((t) => !t.adminOnly || isAdmin);

  return (
    <>
      <Link to="/fridges" className="back-link">
        내 냉장고 목록
      </Link>
      <div className="page__head">
        <h1 className="page__title">{fridge.name}</h1>
        <span className="row__meta">
          <span>{ROLE_LABEL[fridge.myRole] ?? fridge.myRole}</span>
          <span>{fridge.memberCount}명</span>
        </span>
      </div>

      <div className="tabs" role="tablist">
        {visibleTabs.map((t) => (
          <button
            key={t.key}
            type="button"
            role="tab"
            aria-selected={tab === t.key}
            className={`tabs__item${tab === t.key ? ' is-active' : ''}`}
            onClick={() => setSearchParams({ tab: t.key })}
          >
            {t.label}
          </button>
        ))}
      </div>

      <div role="tabpanel">
        {tab === 'ingredients' && <IngredientsTab fridgeId={fridgeId} />}
        {tab === 'members' && <MembersTab fridgeId={fridgeId} isAdmin={isAdmin} />}
        {tab === 'requests' && isAdmin && <JoinRequestsTab fridgeId={fridgeId} />}
        {tab === 'history' && <HistoryTab fridgeId={fridgeId} />}
        {tab === 'settings' && <SettingsTab fridge={fridge} isAdmin={isAdmin} onRenamed={reload} />}
      </div>
    </>
  );
}
