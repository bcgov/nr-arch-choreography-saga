import logging
import requests
import os
import json
from requests.exceptions import Timeout

CHES_API_OAUTH_SECRET = os.getenv('CHES_API_OAUTH_SECRET')
CHES_URL = os.getenv('CHES_URL')
AUTH_URL = os.getenv('AUTH_URL')


def get_ches_token():

    """Get CHES Token"""
    _auth_response = None
    try:
        _auth_pay_load = 'grant_type=client_credentials'
        _auth_headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Basic ' + CHES_API_OAUTH_SECRET
        }
        _auth_response = requests.request("POST", AUTH_URL, headers=_auth_headers, data=_auth_pay_load,
                                          timeout=5)  # timeout in seconds
        _auth_response_json = json.loads(_auth_response.content)
        if _auth_response_json.get('access_token'):
            return _auth_response_json['access_token']
        else:
            raise KeyError(_auth_response_json.get('error_description') + ", "
                           + _auth_response_json.get('error') + ", status code:"
                           + str(_auth_response.status_code) + ", reason:" + _auth_response.reason)
    except KeyError as _ke:
        logging.exception("Email could not be sent due to an authorization issue:%s", _ke)
    except Timeout:
        logging.error('The request timed out to get CHES token! - %s', AUTH_URL)
    return _auth_response


def check_ches_health():

    """Returns health checks of external service dependencies"""
    _access_token = get_ches_token()
    ches_headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + _access_token
    }
    _ches_api_health_endpoint = CHES_URL + '/api/v1/health'
    try:
        _ches_response = requests.request("GET", _ches_api_health_endpoint, headers=ches_headers,
                                          timeout=5)  # timeout in seconds
        if _ches_response.status_code == 200:
            logging.info(r'The status of CHES returned the healthy.')
        elif _ches_response.status_code == 401:
            logging.error(r'Access token is mission or invalid.')
        elif _ches_response.status_code == 403:
            logging.error(r'Lack required role to perform action.')
        else:
            logging.error("CHES Health returned status code:%s, text:%s", str(_ches_response.status_code),
                          _ches_response.text)
    except Timeout:
        logging.error('The request timed out to check CHES Health! - %s', _ches_api_health_endpoint)


def send_email(message):
    """Send email via CHES API"""
    _ches_response = None
    subject = "Event Received"
    _access_token = get_ches_token()
    to_email = "omprakash.2.mishra@gov.bc.ca"
    if _access_token is not None:
        from_email = "omprakash.2.mishra@gov.bc.ca"
        ches_payload = "{\n \"bodyType\": \"html\",\n \"body\": \""+message+"\",\n \"delayTS\": 0,\n \"encoding\": \"utf-8\",\n \"from\": \""+from_email+"\",\n \"priority\": \"normal\",\n  \"subject\": \""+subject+"\",\n  \"to\": [\""+to_email+"\"]\n }\n"
        ches_headers = {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + _access_token
        }
        _ches_api_single_email_endpoint = CHES_URL + '/api/v1/email'
        try:
            _ches_response = requests.request("POST", _ches_api_single_email_endpoint, headers=ches_headers, data=ches_payload, timeout=5) # timeout in seconds
        except Timeout:
            logging.error('The request timed out to send email! - %s', _ches_api_single_email_endpoint)
    return _ches_response
