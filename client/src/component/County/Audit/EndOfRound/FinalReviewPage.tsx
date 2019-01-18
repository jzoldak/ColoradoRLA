import * as React from 'react';

import action from 'corla/action/';

interface Props {
    auditBoardIndex: number;
}

const FinalReviewPage = (props: Props) => {
    const {
        auditBoardIndex
    } = props;

    return (
        <div className='pt-card'>
            <h3>Final Review</h3>
            <div className='pt-card'>
                <p>Hello</p>
            </div>
            <button onClick={ () => action('FINAL_REVIEW_COMPLETE', { auditBoardIndex }) }></button>
        </div>
    );
};


export default FinalReviewPage;
