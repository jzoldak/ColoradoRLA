import * as React from 'react';
import { Redirect } from 'react-router-dom';

import withCountyState from 'corla/component/withCountyState';
import withPoll from 'corla/component/withPoll';

import EndOfRoundPageContainer from './EndOfRound/PageContainer';
import CountyAuditPage from './Page';

import notice from 'corla/notice';

import allRoundsCompleteSelector from 'corla/selector/county/allRoundsComplete';
import auditCompleteSelector from 'corla/selector/county/auditComplete';
import canAuditSelector from 'corla/selector/county/canAudit';
import isAuditBoardDoneSelector from 'corla/selector/county/isAuditBoardDone';
import roundInProgressSelector from 'corla/selector/county/roundInProgress';


interface ContainerProps {
    auditComplete: boolean;
    canAudit: boolean;
    showEndOfRoundPage: boolean;
}

class CountyAuditPageContainer extends React.Component<ContainerProps> {
    public render() {
        const {
            auditComplete,
            canAudit,
            showEndOfRoundPage
        } = this.props;

        if (auditComplete) {
            notice.ok('The audit is complete.');

            return <Redirect to={ '/county' } />;
        }

        if (!canAudit) {
            return <Redirect to={ '/county' } />;
        }

        if (showEndOfRoundPage) {
            return <EndOfRoundPageContainer />;
        }

        return <CountyAuditPage />;
    }
}

function select(countyState: County.AppState) {
    const isAuditBoardDone = isAuditBoardDoneSelector(countyState);
    const showEndOfRoundPage = true;
    // XXX: RESTORE
    /*
    allRoundsCompleteSelector(countyState)
        || !roundInProgressSelector(countyState)
        || isAuditBoardDone;
    */

    return {
        auditComplete: auditCompleteSelector(countyState),
        canAudit: canAuditSelector(countyState),
        showEndOfRoundPage,
    };
}


export default withPoll(
    withCountyState(CountyAuditPageContainer),
    'COUNTY_AUDIT_POLL_START',
    'COUNTY_AUDIT_POLL_STOP',
    select,
);
