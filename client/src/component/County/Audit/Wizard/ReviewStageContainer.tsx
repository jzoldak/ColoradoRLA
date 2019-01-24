import * as React from 'react';
import { connect } from 'react-redux';

import ReviewStage from './ReviewStage';

import uploadAcvr from 'corla/action/county/uploadAcvr';

import currentBallotNumber from 'corla/selector/county/currentBallotNumber';
import totalBallotsForBoard from 'corla/selector/county/totalBallotsForBoard';


interface ContainerProps {
    comment?: string;
    countyState: County.AppState;
    currentBallot?: County.CurrentBallot;
    currentBallotNumber?: number;
    isReAuditing?: boolean;
    marks?: County.ACVR;
    nextStage: OnClick;
    prevStage: OnClick;
    totalBallotsForBoard?: number;
}

class ReviewStageContainer extends React.Component<ContainerProps> {
    public render() {
        const {
            comment,
            countyState,
            currentBallot,
            currentBallotNumber,
            isReAuditing,
            marks,
            nextStage,
            prevStage,
            totalBallotsForBoard,
        } = this.props;

        if (!currentBallot) {
            return null;
        }

        if (!marks) {
            return null;
        }

        return <ReviewStage comment={ comment }
                            countyState={ countyState }
                            currentBallot={ currentBallot }
                            currentBallotNumber={ currentBallotNumber }
                            isReAuditing={ isReAuditing }
                            marks={ marks }
                            nextStage={ nextStage }
                            prevStage={ prevStage }
                            totalBallotsForBoard={ totalBallotsForBoard }
                            uploadAcvr={ uploadAcvr } />;
    }
}

function select(countyState: County.AppState) {
    const { currentBallot } = countyState;

    const comment = countyState.finalReview.comment;
    const isReAuditing = !!comment;

    if (!currentBallot) {
        return { countyState };
    }

    const marks = countyState.acvrs[currentBallot.id];

    return {
        comment,
        countyState,
        currentBallot,
        currentBallotNumber: currentBallotNumber(countyState),
        isReAuditing,
        marks,
        totalBallotsForBoard: totalBallotsForBoard(countyState),
    };
}


export default connect(select)(ReviewStageContainer);
