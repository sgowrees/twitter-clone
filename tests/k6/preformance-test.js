import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        performance_test: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 5 },
                { duration: '20s', target: 10 },
                { duration: '20s', target: 20 },
                { duration: '10s', target: 0 },
            ],
        },
    },

    thresholds: {
        http_req_duration: ['p(95)<300', 'p(99)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
    const ts = Date.now();

    const register = http.post(
        `${BASE_URL}/api/auth/register`,
        JSON.stringify({
            username: `perfuser_${ts}`,
            email: `perfuser_${ts}@test.com`,
            displayName: 'Performance User',
            password: 'Password123!',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(register, {
        'registration successful': (r) => r.status === 200 || r.status === 201,
    });

    const user = register.json();

    const post = http.post(
        `${BASE_URL}/api/users/${user.userId}/posts/create`,
        JSON.stringify({
            content: 'Performance test post',
        }),
        {
            headers: {
                'Authorization': `Bearer ${user.token}`,
                'Content-Type': 'application/json',
            },
        }
    );

    check(post, {
        'post created successfully': (r) => r.status === 200 || r.status === 201,
    });

    const postData = post.json();

    return {
        userId: user.userId,
        token: user.token,
        postId: postData.id,
    };
}

export default function (data) {
    const response = http.get(
        `${BASE_URL}/api/users/${data.userId}/posts/${data.postId}`,
        {
            headers: {
                'Authorization': `Bearer ${data.token}`,
            },
        }
    );

    check(response, {
        'request successful': (r) => r.status === 200,
    });
}