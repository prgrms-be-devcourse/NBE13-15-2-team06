import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ingredientApi } from '../../api/endpoints';
import { useLoad } from '../../api/useLoad';
import ExpiryTape from '../../components/ExpiryTape';
import { Empty, ErrorNote, Field, Loading } from '../../components/ui';

const STORAGE_TYPES = ['냉장', '냉동', '실온'];

export default function IngredientsTab({ fridgeId }) {
  const [filters, setFilters] = useState({ keyword: '', expiringWithinDays: '', sort: 'expiresAt,asc' });
  const [applied, setApplied] = useState(filters);
  const { data: items, error, loading, reload } = useLoad(
    () => ingredientApi.list(fridgeId, applied),
    [fridgeId, applied],
  );

  const handleSearch = (event) => {
    event.preventDefault();
    setApplied(filters);
  };

  return (
    <>
      <form className="filters" onSubmit={handleSearch}>
        <input
          value={filters.keyword}
          onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
          placeholder="이름으로 찾기"
          aria-label="식재료 이름 검색"
        />
        <select
          value={filters.expiringWithinDays}
          onChange={(e) => setFilters({ ...filters, expiringWithinDays: e.target.value })}
          aria-label="소비기한 범위"
        >
          <option value="">전체 기한</option>
          <option value="0">오늘까지</option>
          <option value="3">3일 이내</option>
          <option value="7">7일 이내</option>
        </select>
        <select
          value={filters.sort}
          onChange={(e) => setFilters({ ...filters, sort: e.target.value })}
          aria-label="정렬"
        >
          <option value="expiresAt,asc">기한 임박순</option>
          <option value="expiresAt,desc">기한 넉넉한순</option>
          <option value="createdAt,desc">최근 등록순</option>
        </select>
        <button type="submit" className="button button--quiet">
          적용
        </button>
      </form>

      {loading && <Loading />}
      <ErrorNote error={error} />
      {items && items.length === 0 && <Empty>조건에 맞는 식재료가 없어요. 아래에서 새로 넣어 보세요.</Empty>}
      {items && items.length > 0 && (
        <ul className="rows">
          {items.map((item) => (
            <li key={item.id}>
              <Link to={`/fridges/${fridgeId}/ingredients/${item.id}`} className="row row--link row--ingredient">
                <ExpiryTape expiresAt={item.expiresAt} status={item.status} />
                <span className="row__main">{item.name}</span>
                <span className="row__meta">
                  <span>
                    {item.quantity}
                    {item.unit ?? ''}
                  </span>
                  {item.storageType && <span>{item.storageType}</span>}
                  <span>{item.shared ? '공동' : '개인'}</span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      <AddIngredientForm fridgeId={fridgeId} onAdded={reload} />
    </>
  );
}

const EMPTY_FORM = {
  name: '',
  quantity: 1,
  unit: '개',
  storageType: '냉장',
  shared: true,
  purchasedAt: '',
  expiresAt: '',
  notifyDaysBefore: 3,
  memo: '',
};

function AddIngredientForm({ fridgeId, onAdded }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState(null);
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      await ingredientApi.create(fridgeId, {
        ...form,
        name: form.name.trim(),
        quantity: Number(form.quantity),
        notifyDaysBefore: form.notifyDaysBefore === '' ? null : Number(form.notifyDaysBefore),
        purchasedAt: form.purchasedAt || null,
        expiresAt: form.expiresAt || null,
        memo: form.memo || null,
      });
      setForm(EMPTY_FORM);
      onAdded();
    } catch (e) {
      setError(e);
    }
  };

  return (
    <details className="section disclosure">
      <summary>식재료 넣기</summary>
      <form className="grid-form" onSubmit={handleSubmit}>
        <Field label="이름">
          <input value={form.name} onChange={set('name')} maxLength={50} required />
        </Field>
        <Field label="수량">
          <input type="number" min="1" value={form.quantity} onChange={set('quantity')} required />
        </Field>
        <Field label="단위">
          <input value={form.unit} onChange={set('unit')} maxLength={10} />
        </Field>
        <Field label="보관">
          <select value={form.storageType} onChange={set('storageType')}>
            {STORAGE_TYPES.map((t) => (
              <option key={t}>{t}</option>
            ))}
          </select>
        </Field>
        <Field label="구매일">
          <input type="date" value={form.purchasedAt} onChange={set('purchasedAt')} />
        </Field>
        <Field label="소비기한">
          <input type="date" value={form.expiresAt} onChange={set('expiresAt')} />
        </Field>
        <Field label="며칠 전에 알림">
          <input type="number" min="0" value={form.notifyDaysBefore} onChange={set('notifyDaysBefore')} />
        </Field>
        <label className="check">
          <input type="checkbox" checked={form.shared} onChange={set('shared')} />
          모두 같이 먹는 식재료
        </label>
        <Field label="메모">
          <input value={form.memo} onChange={set('memo')} maxLength={255} />
        </Field>
        <div className="grid-form__actions">
          <ErrorNote error={error} />
          <button type="submit" className="button">
            넣기
          </button>
        </div>
      </form>
    </details>
  );
}
