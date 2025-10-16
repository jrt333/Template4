const runtimeOrigin = (typeof window !== 'undefined' && window.location && window.location.origin)
    ? window.location.origin.replace(/\/$/, '')
    : '';

const hasProcess = typeof process !== 'undefined' && process.env;
const isProduction = hasProcess && process.env.NODE_ENV === 'production';

function computeApiBaseUrl() {
    if (hasProcess && process.env.VUE_APP_API_BASE_URL) {
        return process.env.VUE_APP_API_BASE_URL.replace(/\/$/, '');
    }
    if (isProduction && runtimeOrigin) {
        return `${runtimeOrigin}/api`;
    }
    return 'http://localhost:8080';
}

function computeWsBaseUrl() {
    if (hasProcess && process.env.VUE_APP_WS_BASE_URL) {
        return process.env.VUE_APP_WS_BASE_URL.replace(/\/$/, '');
    }
    if (runtimeOrigin) {
        try {
            const url = new URL(runtimeOrigin);
            const protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
            return `${protocol}//${url.host}/ws/webSocketServer`;
        } catch (err) {
            console.warn('[env] failed to parse runtime origin for ws:', err);
        }
    }
    return 'ws://localhost:3001/webSocketServer';
}

function computeGoogleRedirectUri() {
    if (hasProcess && process.env.VUE_APP_GOOGLE_REDIRECT_URI) {
        return process.env.VUE_APP_GOOGLE_REDIRECT_URI;
    }
    if (isProduction && runtimeOrigin) {
        return `${runtimeOrigin}/oauth2/callback`;
    }
    return 'http://localhost:8080/oauth2/callback';
}

export function getApiBaseUrl() {
    return computeApiBaseUrl();
}

export function getWsBaseUrl() {
    return computeWsBaseUrl();
}

export function getGoogleRedirectUri() {
    return computeGoogleRedirectUri();
}

export function getRuntimeOrigin() {
    return runtimeOrigin;
}