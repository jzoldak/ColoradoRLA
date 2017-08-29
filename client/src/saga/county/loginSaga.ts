import {
    put,
    select,
    takeLatest,
} from 'redux-saga/effects';

import countyDashboardRefresh from 'corla/action/county/dashboardRefresh';
import fetchAuditBoardAsmState from 'corla/action/county/fetchAuditBoardAsmState';
import fetchCountyAsmState from 'corla/action/county/fetchCountyAsmState';

import notice from 'corla/notice';


function* countyLoginOk() {
    countyDashboardRefresh();
    fetchAuditBoardAsmState();
    fetchCountyAsmState();

    yield put({ type: 'COUNTY_POLL' });
}

function* countyLoginFail(): IterableIterator<void> {
    notice.danger('Invalid credentials.');
}

function* countyLoginNetworkFail(): IterableIterator<void> {
    notice.danger('Unable to log in due to network error.');
}


export default function* countyLoginSaga() {
    yield takeLatest('COUNTY_LOGIN_FAIL', countyLoginFail);
    yield takeLatest('COUNTY_LOGIN_NETWORK_FAIL', countyLoginNetworkFail);
    yield takeLatest('COUNTY_LOGIN_OK', countyLoginOk);
}