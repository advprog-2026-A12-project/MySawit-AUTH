import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8001";
const TEST_TYPE = (__ENV.TEST_TYPE || "smoke").toLowerCase();
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || "1");

const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || "admin@mysawit.local";
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || "change-this-to-a-strong-secret";
const AUTH_EMAIL = __ENV.AUTH_EMAIL || ADMIN_EMAIL;
const AUTH_PASSWORD = __ENV.AUTH_PASSWORD || ADMIN_PASSWORD;

const AUTH_VUS = Number(__ENV.AUTH_VUS || "20");
const USERS_VUS = Number(__ENV.USERS_VUS || "20");
const ASSIGNMENTS_VUS = Number(__ENV.ASSIGNMENTS_VUS || "20");

const loginDuration = new Trend("login_duration_ms");
const meDuration = new Trend("me_duration_ms");
const usersDuration = new Trend("users_duration_ms");
const assignmentsDuration = new Trend("assignments_duration_ms");
const authFailures = new Counter("auth_failures");
const managementFailures = new Counter("management_failures");

function stagesByType(type, target) {
  if (type === "normal") {
    return [
      { duration: "1m", target: Math.max(1, Math.round(target * 0.5)) },
      { duration: "5m", target },
      { duration: "1m", target: 0 },
    ];
  }

  if (type === "stress") {
    return [
      { duration: "2m", target: Math.max(1, Math.round(target * 0.5)) },
      { duration: "5m", target },
      { duration: "5m", target: Math.round(target * 1.5) },
      { duration: "2m", target: 0 },
    ];
  }

  return [
    { duration: "30s", target: Math.max(1, Math.round(target * 0.5)) },
    { duration: "2m", target },
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
      stages: stagesByType(TEST_TYPE, AUTH_VUS),
      gracefulRampDown: "30s",
    },
    user_management_read: {
      executor: "ramping-vus",
      exec: "userManagementFlow",
      startVUs: 1,
      stages: stagesByType(TEST_TYPE, USERS_VUS),
      gracefulRampDown: "30s",
    },
    assignment_read: {
      executor: "ramping-vus",
      exec: "assignmentFlow",
      startVUs: 1,
      stages: stagesByType(TEST_TYPE, ASSIGNMENTS_VUS),
      gracefulRampDown: "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<1200"],
    login_duration_ms: ["p(95)<1000"],
    me_duration_ms: ["p(95)<800"],
    users_duration_ms: ["p(95)<1000"],
    assignments_duration_ms: ["p(95)<1000"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "max"],
};

export function setup() {
  const adminLoginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
    { headers: { "Content-Type": "application/json" } },
  );

  const adminToken = adminLoginRes.status === 200 ? (adminLoginRes.json("data.accessToken") || "") : "";
  return { adminToken };
}

export function authFlow() {
  const loginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: AUTH_EMAIL, password: AUTH_PASSWORD }),
    { headers: { "Content-Type": "application/json" } },
  );
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

  const meOk = check(meRes, { "me status 200": (r) => r.status === 200 });
  if (!meOk) {
    authFailures.add(1);
  }

  sleep(SLEEP_SECONDS);
}

export function userManagementFlow(data) {
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

  const usersOk = check(usersRes, { "users status 200": (r) => r.status === 200 });
  if (!usersOk) {
    managementFailures.add(1);
  }

  sleep(SLEEP_SECONDS);
}

export function assignmentFlow(data) {
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
