import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8001";
const TEST_TYPE = (__ENV.TEST_TYPE || "smoke").toLowerCase();
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || "1");
const RUN_USER_MANAGEMENT = (__ENV.RUN_USER_MANAGEMENT || "true").toLowerCase() === "true";
const RUN_ASSIGNMENTS = (__ENV.RUN_ASSIGNMENTS || "true").toLowerCase() === "true";

const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "loadtest-user@example.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "SecureP@ss123";
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || "admin@mysawit.local";
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || "change-this-to-a-strong-secret";

const loginDuration = new Trend("login_duration_ms");
const meDuration = new Trend("me_duration_ms");
const usersDuration = new Trend("users_duration_ms");
const assignmentsDuration = new Trend("assignments_duration_ms");
const authFailures = new Counter("auth_failures");
const managementFailures = new Counter("management_failures");

function stagesByType(type) {
  if (type === "normal") {
    return [
      { duration: "1m", target: 20 },
      { duration: "5m", target: 80 },
      { duration: "1m", target: 0 },
    ];
  }

  if (type === "stress") {
    return [
      { duration: "2m", target: 50 },
      { duration: "5m", target: 150 },
      { duration: "5m", target: 300 },
      { duration: "2m", target: 0 },
    ];
  }

  return [
    { duration: "30s", target: 10 },
    { duration: "2m", target: 20 },
    { duration: "30s", target: 0 },
  ];
}

export const options = {
  discardResponseBodies: false,
  scenarios: {
    auth_flow: {
      executor: "ramping-vus",
      exec: "authFlow",
      startVUs: 1,
      stages: stagesByType(TEST_TYPE),
      gracefulRampDown: "30s",
    },
    user_management_read: {
      executor: "ramping-vus",
      exec: "userManagementFlow",
      startVUs: 0,
      stages: RUN_USER_MANAGEMENT ? stagesByType(TEST_TYPE) : [{ duration: "10s", target: 0 }],
      gracefulRampDown: "30s",
    },
    assignment_read: {
      executor: "ramping-vus",
      exec: "assignmentFlow",
      startVUs: 0,
      stages: RUN_ASSIGNMENTS ? stagesByType(TEST_TYPE) : [{ duration: "10s", target: 0 }],
      gracefulRampDown: "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<800"],
    login_duration_ms: ["p(95)<700"],
    me_duration_ms: ["p(95)<500"],
    users_duration_ms: ["p(95)<700"],
    assignments_duration_ms: ["p(95)<700"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "max"],
};

export function setup() {
  const registerPayload = JSON.stringify({
    name: "Load Test User",
    email: LOGIN_EMAIL,
    password: LOGIN_PASSWORD,
    role: "BURUH",
  });

  const registerRes = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    registerPayload,
    { headers: { "Content-Type": "application/json" } },
  );

  check(registerRes, {
    "register ok/conflict": (r) => r.status === 201 || r.status === 409,
  });

  let adminToken = "";
  if (RUN_USER_MANAGEMENT || RUN_ASSIGNMENTS) {
    const adminLoginRes = http.post(
      `${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
      { headers: { "Content-Type": "application/json" } },
    );

    if (adminLoginRes.status === 200) {
      adminToken = adminLoginRes.json("data.accessToken") || "";
    }
  }

  return { adminToken };
}

export function authFlow() {
  const loginPayload = JSON.stringify({
    email: LOGIN_EMAIL,
    password: LOGIN_PASSWORD,
  });

  const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });
  loginDuration.add(loginRes.timings.duration);

  const loginOk = check(loginRes, {
    "login status 200": (r) => r.status === 200,
    "login has token": (r) => {
      try {
        const token = r.json("data.accessToken");
        return typeof token === "string" && token.length > 20;
      } catch (_) {
        return false;
      }
    },
  });

  if (!loginOk) {
    authFailures.add(1);
    sleep(SLEEP_SECONDS);
    return;
  }

  const token = loginRes.json("data.accessToken");
  const meRes = http.get(`${BASE_URL}/api/v1/users/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  meDuration.add(meRes.timings.duration);

  const meOk = check(meRes, {
    "me status 200": (r) => r.status === 200,
  });

  if (!meOk) {
    authFailures.add(1);
  }

  sleep(SLEEP_SECONDS);
}

export function userManagementFlow(data) {
  if (!RUN_USER_MANAGEMENT) {
    sleep(SLEEP_SECONDS);
    return;
  }

  const token = data && data.adminToken ? data.adminToken : "";
  if (!token) {
    managementFailures.add(1);
    sleep(SLEEP_SECONDS);
    return;
  }

  const usersRes = http.get(
    `${BASE_URL}/api/v1/users?page=0&size=20&sort=createdAt,desc`,
    { headers: { Authorization: `Bearer ${token}` } },
  );
  usersDuration.add(usersRes.timings.duration);

  const usersOk = check(usersRes, {
    "users status 200": (r) => r.status === 200,
  });
  if (!usersOk) {
    managementFailures.add(1);
  }

  sleep(SLEEP_SECONDS);
}

export function assignmentFlow(data) {
  if (!RUN_ASSIGNMENTS) {
    sleep(SLEEP_SECONDS);
    return;
  }

  const token = data && data.adminToken ? data.adminToken : "";
  if (!token) {
    managementFailures.add(1);
    sleep(SLEEP_SECONDS);
    return;
  }

  const assignmentsRes = http.get(
    `${BASE_URL}/api/v1/assignments/buruh-mandor?page=0&size=20`,
    { headers: { Authorization: `Bearer ${token}` } },
  );
  assignmentsDuration.add(assignmentsRes.timings.duration);

  const assignmentsOk = check(assignmentsRes, {
    "assignments status 200": (r) => r.status === 200,
  });
  if (!assignmentsOk) {
    managementFailures.add(1);
  }

  sleep(SLEEP_SECONDS);
}
