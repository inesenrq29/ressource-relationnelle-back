import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL =
    __ENV.BASE_URL || 'http://localhost:8080';

export const options = {

    stages: [

        {
            duration: '10s',
            target: 5,
        },

        {
            duration: '20s',
            target: 20,
        },

        {
            duration: '20s',
            target: 20,
        },

        {
            duration: '10s',
            target: 0,
        },

    ],

    thresholds: {

        checks: [
            'rate>0.99',
        ],

        http_req_failed: [
            'rate<0.01',
        ],

        http_req_duration: [
            'p(95)<500',
            'p(99)<1000',
        ],

    },

};

export function setup() {

    const healthResponse =
        http.get(
            `${BASE_URL}/actuator/health`
        );

    const applicationIsUp =
        check(
            healthResponse,
            {
                'application is UP before load test':
                    (response) =>
                        response.status === 200
                        && response.body.includes('UP'),
            }
        );

    if (!applicationIsUp) {

        throw new Error(
            `Application unavailable at ${BASE_URL}`
        );
    }
}

export default function () {

    const response =
        http.get(
            `${BASE_URL}/api/resources`,
            {
                tags: {
                    endpoint:
                        'GET /api/resources',
                },
            }
        );

    check(
        response,
        {

            'GET /api/resources returns 200':
                (result) =>
                    result.status === 200,

            'response is JSON':
                (result) =>
                    (
                        result.headers['Content-Type']
                        || ''
                    ).includes(
                        'application/json'
                    ),
        }
    );

    sleep(0.5);
}