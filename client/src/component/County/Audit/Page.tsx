import * as React from 'react';

import CountyNav from '../Nav';

import CountyAuditWizardContainer from './Wizard/Container';

interface Props {
    reviewingBallotId?: number;
}

const CountyAuditPage = (props: Props) => {
    const { reviewingBallotId } = props;

    return (
        <div>
            <CountyNav />
            <CountyAuditWizardContainer reviewingBallotId={ reviewingBallotId } />
        </div>
    );
};


export default CountyAuditPage;
