export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',

  // *** Auth0 credentials — replace with your values from the Auth0 Dashboard ***
  auth0Domain: 'dev-8zl45p543xeh8sg2.us.auth0.com',     // e.g. dev-xxxxxxxx.us.auth0.com
  auth0ClientId: 'QU08tFI6yZOExhlEKyMBF6E4FbTjyJog',      // SPA Application Client ID
  auth0Audience: 'https://dev-8zl45p543xeh8sg2.us.auth0.com/api/v2/'    // API Identifier (e.g. https://finly-api)
};
