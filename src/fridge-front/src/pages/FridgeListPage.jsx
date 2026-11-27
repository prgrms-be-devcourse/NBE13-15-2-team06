import { useState } from 'react';
import { Link } from 'react-router-dom';
import { fridgeApi, joinRequestApi } from '../api/endpoints';
import { useLoad } from '../api/useLoad';
import { Empty, ErrorNote, Loading, Notice } from '../components/ui';
import { ROLE_LABEL } from '../components/format';

export default function FridgeListPage() {
  const { data: fridges, error, loading, reload } = useLoad(() => fridgeApi.list(), []);

  return (
    <>
      <h1 className="page__title">내 냉장고</h1>

      {loading && <Loading />}
      <ErrorNote error={error} />
      {fridges && fridges.length === 0 && (
        <Empty>아직 냉장고가 없어요. 새로 만들거나, 받은 초대 코드로 참여해 보세요.</Empty>
      )}
      {fridges && fridges.length > 0 && (
        <ul className="rows">
          {fridges.map((fridge) => (
            <li key={fridge.id}>
              <Link to={`/fridges/${fridge.id}`} className="row row--link">
                <span className="row__main">{fridge.name}</span>
                <span className="row__meta">
                  <span>{ROLE_LABEL[fridge.myRole] ?? fridge.myRole}</span>
                  <span>{fridge.memberCount}명</span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      <div className="split">
        <CreateFridgeForm onCreated={reload} />
        <JoinFridgeForm />
      </div>
    </>
  );
}

function CreateFridgeForm({ onCreated }) {
  const [name, setName] = useState('');
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      await fridgeApi.create(name.trim());
      setName('');
      onCreated();
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">새 냉장고 만들기</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="예: 우리집 냉장고"
          maxLength={50}
          required
          aria-label="냉장고 이름"
        />
        <button type="submit" className="button">
          만들기
        </button>
      </form>
      <ErrorNote error={error} />
    </section>
  );
}

function JoinFridgeForm() {
  const [code, setCode] = useState('');
  const [error, setError] = useState(null);
  const [requested, setRequested] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setRequested(null);
    try {
      const result = await joinRequestApi.request(code.trim());
      setRequested(result);
      setCode('');
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">초대 코드로 참여하기</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="초대 코드 8자리"
          required
          aria-label="초대 코드"
        />
        <button type="submit" className="button button--quiet">
          참여 신청
        </button>
      </form>
      <ErrorNote error={error} />
      {requested && (
        <Notice>
          {requested.fridgeName}에 참여를 신청했어요. 관리자가 승인하면 목록에 나타납니다.
        </Notice>
      )}
    </section>
  );
}
