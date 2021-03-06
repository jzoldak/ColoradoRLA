import * as React from 'react';
import { connect } from 'react-redux';

import FinalReviewPage from './FinalReviewPage';

import allRoundsCompleteSelector from 'corla/selector/county/allRoundsComplete';
import countyInfoSelector from 'corla/selector/county/countyInfo';
import currentRoundNumberSelector from 'corla/selector/county/currentRoundNumber';
import previousRoundSelector from 'corla/selector/county/previousRound';


interface Props {
    auditBoardIndex: number;
    ballotSequenceAssignment?: object[];
    cvrsToAudit?: JSON.CVR[];
}

const Container = (props: Props) => {
    return <FinalReviewPage { ...props } />;
};

const select = (state: County.AppState) => {
    return {
        auditBoardIndex: state.auditBoardIndex || 0,
        ballotSequenceAssignment: state.ballotSequenceAssignment,
        cvrsToAudit: state.cvrsToAudit,
    };
};


export default connect(select)(Container);
