import { getGoogleRedirectUri as resolveGoogleRedirectUri } from './env';
const GOOGLE_CLIENT_ID = process.env.VUE_APP_GOOGLE_CLIENT_ID || '42722722873-qr08vjl9t9fu498fi6p6s1jv9ooj9q0e.apps.googleusercontent.com';
const GOOGLE_AUTH_BASE = 'https://accounts.google.com/o/oauth2/v2/auth';
const GOOGLE_REDIRECT_URI = resolveGoogleRedirectUri();
export function getGoogleRedirectUri() {
    /*if (typeof window !== 'undefined' && window.location) {
        return `${window.location.origin}/oauth2/callback`;
    }
    return 'http://localhost:8080/oauth2/callback';*/
    return GOOGLE_REDIRECT_URI;
}

export function buildGoogleOAuthUrl(statePath = '/') {
    const params = new URLSearchParams();
    params.set('client_id', GOOGLE_CLIENT_ID);
    params.set('redirect_uri', getGoogleRedirectUri());
    params.set('response_type', 'code');
    params.set('scope', 'openid email profile');
    params.set('access_type', 'online');
    params.set('include_granted_scopes', 'true');
    params.set('prompt', 'select_account');
    if (statePath) {
        params.set('state', statePath);
    }
    return `${GOOGLE_AUTH_BASE}?${params.toString()}`;
}

export function startGoogleOAuth(statePath = '/') {
    const url = buildGoogleOAuthUrl(statePath);
    if (typeof window !== 'undefined') {
        window.location.href = url;
    }
    return url;
}

export function getGoogleClientId() {
    return GOOGLE_CLIENT_ID;
}