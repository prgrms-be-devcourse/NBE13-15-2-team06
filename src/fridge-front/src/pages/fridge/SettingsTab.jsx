import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fridgeApi, memberApi, notificationApi } from '../../api/endpoints';
import { ErrorNote, Notice } from '../../components/ui';

export default function SettingsTab({ fridge, isAdmin, onRenamed }) {
  return (
    <>
      {isAdmin && <RenameSection fridge={fridge} onRenamed={onRenamed} />}
      {isAdmin && <InviteCodeSection fridgeId={fridge.id} />}
      <NotificationSettingSection fridgeId={fridge.id} />
      <LeaveSection fridge={fridge} />
    </>
  );
}

function RenameSection({ fridge, onRenamed }) {
  const [name, setName] = useState(fridge.name);
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      await fridgeApi.rename(fridge.id, name.trim());
      onRenamed();
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">냉장고 이름</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <input value={name} onChange={(e) => setName(e.target.value)} maxLength={50} required aria-label="냉장고 이름" />
        <button type="submit" className="button button--quiet">
          이름 저장
        </button>
      </form>
      <ErrorNote error={error} />
    </section>
  );
}

function InviteCodeSection({ fridgeId }) {
  const [code, setCode] = useState(null);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  const show = async () => {
    setError(null);
    try {
      const data = await fridgeApi.inviteCode(fridgeId);
      setCode(data.inviteCode);
    } catch (e) {
      setError(e);
    }
  };

  const copy = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
  };

  return (
    <section className="section">
      <h2 className="section__title">초대 코드</h2>
      <p className="muted">같이 쓸 사람에게 이 코드를 보내면, 참여 신청 탭에서 승인할 수 있어요.</p>
      {code ? (
        <div className="inline-form">
          <code className="invite-code">{code}</code>
          <button type="button" className="button button--quiet" onClick={copy}>
            {copied ? '복사됨' : '복사'}
          </button>
        </div>
      ) : (
        <button type="button" className="button button--quiet" onClick={show}>
          초대 코드 보기
        </button>
      )}
      <ErrorNote error={error} />
    </section>
  );
}

function NotificationSettingSection({ fridgeId }) {
  const [setting, setSetting] = useState(null);
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    notificationApi.getSetting(fridgeId).then(setSetting).catch(setError);
  }, [fridgeId]);

  const update = async (next) => {
    setError(null);
    setSaved(false);
    try {
      const result = await notificationApi.updateSetting(fridgeId, next.enabled, next.receiveOthers);
      setSetting(result);
      setSaved(true);
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section">
      <h2 className="section__title">이 냉장고 알림</h2>
      {setting && (
        <div className="stack">
          <label className="check">
            <input
              type="checkbox"
              checked={setting.enabled}
              onChange={(e) => update({ ...setting, enabled: e.target.checked })}
            />
            소비기한 알림 받기
          </label>
          <label className="check">
            <input
              type="checkbox"
              checked={setting.receiveOthers}
              disabled={!setting.enabled}
              onChange={(e) => update({ ...setting, receiveOthers: e.target.checked })}
            />
            다른 사람 개인 식재료도 알림 받기
          </label>
        </div>
      )}
      <ErrorNote error={error} />
      {saved && <Notice>알림 설정을 저장했습니다.</Notice>}
    </section>
  );
}

function LeaveSection({ fridge }) {
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  const handleLeave = async () => {
    if (!window.confirm(`${fridge.name}에서 나갈까요? 다시 들어오려면 초대 코드가 필요해요.`)) return;
    setError(null);
    try {
      await memberApi.leave(fridge.id);
      navigate('/fridges', { replace: true });
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="section section--danger">
      <h2 className="section__title">냉장고 나가기</h2>
      <button type="button" className="button button--danger" onClick={handleLeave}>
        이 냉장고에서 나가기
      </button>
      <ErrorNote error={error} />
    </section>
  );
}
