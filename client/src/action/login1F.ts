import { endpoint } from '../config';

import createSubmitAction from './createSubmitAction';


const url = endpoint('auth-admin');

const login1F = createSubmitAction({
    failType: 'LOGIN_1F_FAIL',
    networkFailType: 'LOGIN_1F_NETWORK_FAIL',
    okType: 'LOGIN_1F_OK',
    sendType: 'LOGIN_1F_SEND',
    url,
});


export default (username: string, password: string) =>
    login1F({ username, password });