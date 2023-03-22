const axios = require('axios');
const oauth = require('axios-oauth-client');
const {tokenProvider} = require('axios-token-interceptor');

class ClientConnection {

  constructor({tokenUrl, clientId, clientSecret}) {

    if (!tokenUrl || !clientId || !clientSecret) {
      console.log('Invalid configuration.', {function: 'constructor'});
      throw new Error('ClientConnection is not configured. Check configuration.');
    }

    this.tokenUrl = tokenUrl;

    this.axios = axios.create();
    this.clientCreds = oauth.clientCredentials(axios.create(), this.tokenUrl, clientId, clientSecret);
    this.axios.interceptors.request.use(async config => {
      const auth = await this.clientCreds('');
      config.headers = {
        'Authorization': `Bearer ${auth.access_token}`,
        'Accept': 'application/json',
      };
      return config;
    }, error => {
      Promise.reject(error);
    });
  }
}

module.exports = ClientConnection;
