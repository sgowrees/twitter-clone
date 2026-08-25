import http from 'k6/http';
import { check, sleep } from 'k6';

// This test proves Redis/Kafka are working by their EFFECTS, not by
// speaking their protocols directly - k6 doesn't do that out of the box.
//
// - GET /posts/{id} repeatedly -> should stay fast even under load,
//   because Redis is serving the like count instead of hitting Postgres
//   every time.
// - POST /like/toggle repeatedly -> should stay fast and never fail,
//   because it publishes to Kafka and returns immediately instead of
//   waiting for the notification to be created.

export const options = {
    scenarios: {
        view_post: {
            executor: 'constant-vus',
            vus: 20,
            duration: '30s',
            exec: 'viewPost',
        },
        toggle_like: {
            executor: 'constant-vus',
            vus: 10,
            duration: '30s',
            exec: 'toggleLike',
        },
    },
    thresholds: {
        // If Redis weren't caching, this would climb under load as every
        // view triggers a fresh COUNT query against Postgres.
        'http_req_duration{endpoint:view_post}': ['p(95)<200'],
        // If Kafka publishing blocked the request (or notification creation
        // ran inline), this would climb as load increases.
        'http_req_duration{endpoint:toggle_like}': ['p(95)<300'],
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Run once before the load test starts: register two users and a post.
export function setup() {
    const ts = Date.now();

    const regA = http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
        username: `k6userA_${ts}`,
        email: `k6userA_${ts}@test.com`,
        displayName: 'K6 User A',
        password: 'Password123!',
    }), { headers: { 'Content-Type': 'application/json' } });
    const userA = regA.json();

    const regB = http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
        username: `k6userB_${ts}`,
        email: `k6userB_${ts}@test.com`,
        displayName: 'K6 User B',
        password: 'Password123!',
    }), { headers: { 'Content-Type': 'application/json' } });
    const userB = regB.json();

    const post = http.post(
        `${BASE_URL}/api/users/${userA.userId}/posts/create`,
        JSON.stringify({ content: 'k6 load test post' }),
        { headers: {
            'Authorization': `Bearer ${userA.token}`,
            'Content-Type': 'application/json',
        }}
    ).json();

    return {
        userAId: userA.userId,
        tokenA: userA.token,
        tokenB: userB.token,
        postId: post.id,
    };
}

// Scenario 1: repeatedly view the post - exercises the Redis-cached path
export function viewPost(data) {
    const res = http.get(
        `${BASE_URL}/api/users/${data.userAId}/posts/${data.postId}`,
        {
            headers: { 'Authorization': `Bearer ${data.tokenA}` },
            tags: { endpoint: 'view_post' },
        }
    );

    check(res, { 'view post: status 200': (r) => r.status === 200 });
    sleep(0.5);
}

// Scenario 2: repeatedly toggle like - exercises the Kafka publish path
export function toggleLike(data) {
    const res = http.post(
        `${BASE_URL}/api/users/${data.userAId}/posts/${data.postId}/like/toggle`,
        null,
        {
            headers: { 'Authorization': `Bearer ${data.tokenB}` },
            tags: { endpoint: 'toggle_like' },
        }
    );

    check(res, { 'toggle like: status 200': (r) => r.status === 200 });
    sleep(1);
}