// 모든 API 요청이 지나가는 곳.
// 1) 저장된 토큰을 Authorization 헤더에 붙이고
// 2) 서버의 ApiResponse { status, message, data }에서 data만 꺼내 돌려주고
// 3) 실패하면 ErrorResponse { status, code, message }를 ApiError로 바꿔 던진다.

const TOKEN_KEY = 'accessToken';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

function buildUrl(path, params) {
  const url = `/api/v1${path}`;
  if (!params) return url;
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.append(key, value);
  });
  const qs = query.toString();
  return qs ? `${url}?${qs}` : url;
}

function parseJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function request(path, { method = 'GET', body, params } = {}) {
  const token = tokenStore.get();
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(buildUrl(path, params), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', '서버에 연결할 수 없습니다. 스프링 서버가 8080 포트에서 실행 중인지 확인하세요.');
  }

  // 204(로그아웃, 탈퇴)나 본문 없는 200(알림 읽음)도 있어서 텍스트로 먼저 읽는다.
  const text = await response.text();
  const json = text ? parseJson(text) : null;

  if (!response.ok) {
    // 토큰을 보냈는데 401이면 만료된 것. 로그인 실패(토큰 없이 보낸 401)와 구분한다.
    if (response.status === 401 && token) {
      tokenStore.clear();
      window.dispatchEvent(new Event('auth:expired'));
    }
    throw new ApiError(
      response.status,
      json?.code ?? 'UNKNOWN',
      json?.message ?? `요청을 처리하지 못했습니다. (${response.status})`,
    );
  }

  return json?.data ?? null;
}
