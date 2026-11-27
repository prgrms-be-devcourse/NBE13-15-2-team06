import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ingredientApi, memberApi } from '../api/endpoints';
import { useLoad } from '../api/useLoad';
import ExpiryTape from '../components/ExpiryTape';
import { Empty, ErrorNote, Field, Loading } from '../components/ui';
import { ACTION_LABEL, formatDate, formatDateTime, STATUS_LABEL } from '../components/format';

export default function IngredientPage() {
  const { fridgeId, ingredientId } = useParams();
  const { data: item, error, loading, setData } = useLoad(
    () => ingredientApi.get(fridgeId, ingredientId),
    [fridgeId, ingredientId],
  );
  const { data: members } = useLoad(() => memberApi.list(fridgeId), [fridgeId]);
  const histories = useLoad(() => ingredientApi.histories(fridgeId, ingredientId), [fridgeId, ingredientId]);

  if (loading && !item) return <Loading />;
  if (error) return <ErrorNote error={error} />;

  const owner = members?.find((m) => m.userId === item.ownerId);
  // 변경 API는 바뀐 식재료를 돌려주니, 화면을 그 값으로 바로 갱신하고 기록도 다시 불러온다.
  const applyChange = (updated) => {
    setData(updated);
    histories.reload();
  };
  const isStored = item.status === 'STORED';

  return (
    <>
      <Link to={`/fridges/${fridgeId}`} className="back-link">
        냉장고로 돌아가기
      </Link>

      <div className="ingredient-head">
        <ExpiryTape expiresAt={item.expiresAt} status={item.status} size="large" />
        <h1 className="page__title">{item.name}</h1>
      </div>

      <dl className="facts">
        <dt>남은 양</dt>
        <dd>
          {item.quantity}
          {item.unit ?? ''}
        </dd>
        <dt>상태</dt>
        <dd>{STATUS_LABEL[item.status] ?? item.status}</dd>
        <dt>소비기한</dt>
        <dd>{formatDate(item.expiresAt)}</dd>
        <dt>구매일</dt>
        <dd>{formatDate(item.purchasedAt)}</dd>
        <dt>보관</dt>
        <dd>{item.storageType ?? '-'}</dd>
        <dt>주인</dt>
        <dd>{item.shared ? '모두 같이' : owner?.nickname ?? '알 수 없음'}</dd>
        <dt>알림</dt>
        <dd>{item.notifyDaysBefore != null ? `기한 ${item.notifyDaysBefore}일 전` : '-'}</dd>
      </dl>

      {isStored && <ConsumeSection fridgeId={fridgeId} item={item} onChange={applyChange} />}
      {isStored && <StatusSection fridgeId={fridgeId} item={item} onChange={applyChange} />}
      <MemoSection fridgeId={fridgeId} item={item} onChange={applyChange} />
      <OwnershipSection fridgeId={fridgeId} item={item} onChange={applyChange} />
      <EditSection fridgeId={fridgeId} item={item} onChange={applyChange} />

      <section className="section">
        <h2 className="section__title">기록</h2>
        <ErrorNote error={histories.error} />
        {histories.data?.length === 0 && <Empty>아직 기록이 없어요.</Empty>}
        <ol className="timeline">
          {histories.data?.map((h) => (
            <li key={h.id}>
              <time className="timeline__time">{formatDateTime(h.createdAt)}</time>
              <span>
                {members?.find((m) => m.userId === h.userId)?.nickname ?? '알 수 없음'}님이{' '}
                {ACTION_LABEL[h.action] ?? h.action}
                {h.quantityChange != null && h.quantityChange !== 0 && (
                  <span className="muted"> ({h.quantityChange})</span>
                )}
              </span>
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}

function useAction(onChange) {
  const [error, setError] = useState(null);
  const run = async (action) => {
    setError(null);
    try {
      onChange(await action());
      return true;
    } catch (e) {
      setError(e);
      return false;
    }
  };
  return { error, run };
}

function ConsumeSection({ fridgeId, item, onChange }) {
  const [quantity, setQuantity] = useState(1);
  const { error, run } = useAction(onChange);

  const handleSubmit = (event) => {
    event.preventDefault();
    run(() => ingredientApi.consume(fridgeId, item.id, Number(quantity)));
  };

  return (
    <section className="section">
      <h2 className="section__title">먹었어요</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input
          type="number"
          min="1"
          max={item.quantity}
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          aria-label="먹은 양"
        />
        <span className="muted">{item.unit ?? ''}</span>
        <button type="submit" className="button">
          먹은 만큼 빼기
        </button>
        <button
          type="button"
          className="button button--quiet"
          onClick={() => run(() => ingredientApi.consume(fridgeId, item.id, item.quantity))}
        >
          전부 먹음
        </button>
      </form>
      <ErrorNote error={error} />
    </section>
  );
}

function StatusSection({ fridgeId, item, onChange }) {
  const { error, run } = useAction(onChange);

  const handleDiscard = () => {
    if (!window.confirm(`${item.name}을(를) 버린 것으로 처리할까요?`)) return;
    run(() => ingredientApi.updateStatus(fridgeId, item.id, 'DISCARDED'));
  };

  return (
    <section className="section">
      <h2 className="section__title">버리기</h2>
      <p className="muted">상했거나 기한이 지나 버렸다면 기록해 두세요. 되돌릴 수 없어요.</p>
      <button type="button" className="button button--danger-quiet" onClick={handleDiscard}>
        버림으로 처리
      </button>
      <ErrorNote error={error} />
    </section>
  );
}

function MemoSection({ fridgeId, item, onChange }) {
  const [memo, setMemo] = useState(item.memo ?? '');
  const { error, run } = useAction(onChange);

  const handleSubmit = (event) => {
    event.preventDefault();
    run(() => ingredientApi.updateMemo(fridgeId, item.id, memo));
  };

  return (
    <section className="section">
      <h2 className="section__title">메모</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input
          value={memo}
          onChange={(e) => setMemo(e.target.value)}
          maxLength={255}
          placeholder="예: 반은 찌개용"
          aria-label="메모"
        />
        <button type="submit" className="button button--quiet">
          메모 저장
        </button>
      </form>
      <ErrorNote error={error} />
    </section>
  );
}

function OwnershipSection({ fridgeId, item, onChange }) {
  const { error, run } = useAction(onChange);

  return (
    <section className="section">
      <h2 className="section__title">누구 거예요?</h2>
      <label className="check">
        <input
          type="checkbox"
          checked={item.shared}
          onChange={(e) => run(() => ingredientApi.updateOwnership(fridgeId, item.id, e.target.checked))}
        />
        모두 같이 먹는 식재료
      </label>
      <ErrorNote error={error} />
    </section>
  );
}

function EditSection({ fridgeId, item, onChange }) {
  const [form, setForm] = useState({
    name: item.name,
    unit: item.unit ?? '',
    storageType: item.storageType ?? '',
    purchasedAt: item.purchasedAt ?? '',
    expiresAt: item.expiresAt ?? '',
    notifyDaysBefore: item.notifyDaysBefore ?? '',
  });
  const { error, run } = useAction(onChange);
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const handleSubmit = (event) => {
    event.preventDefault();
    run(() =>
      ingredientApi.update(fridgeId, item.id, {
        name: form.name.trim(),
        unit: form.unit || null,
        storageType: form.storageType || null,
        purchasedAt: form.purchasedAt || null,
        expiresAt: form.expiresAt || null,
        notifyDaysBefore: form.notifyDaysBefore === '' ? null : Number(form.notifyDaysBefore),
      }),
    );
  };

  return (
    <details className="section disclosure">
      <summary>정보 고치기</summary>
      <form className="grid-form" onSubmit={handleSubmit}>
        <Field label="이름">
          <input value={form.name} onChange={set('name')} maxLength={50} required />
        </Field>
        <Field label="단위">
          <input value={form.unit} onChange={set('unit')} maxLength={10} />
        </Field>
        <Field label="보관">
          <input value={form.storageType} onChange={set('storageType')} maxLength={20} />
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
        <div className="grid-form__actions">
          <ErrorNote error={error} />
          <button type="submit" className="button">
            변경 저장
          </button>
        </div>
      </form>
    </details>
  );
}
