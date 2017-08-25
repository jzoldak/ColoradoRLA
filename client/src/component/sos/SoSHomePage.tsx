import * as React from 'react';

import * as _ from 'lodash';

import { Tooltip } from '@blueprintjs/core';

import SoSNav from './Nav';

import counties from '../../data/counties';


const SeedInfo = ({ seed }: any) => {
    return (
        <div className='pt-card'>
            <strong>Seed: </strong> { seed }
        </div>
    );
};

const ContestUpdates = ({ contests, seed }: any) => {
    const contestStatuses = _.map(contests, (c: any) => (
        <tr key={ c.id}>
            <td>{ c.id }</td>
            <td>{ c.name }</td>
            <td>{ c.status }</td>
            <td>{ c.riskLimit }</td>
            <td>{ c.riskLevel }</td>
        </tr>
    ));

    return (
        <div className='pt-card'>
            <h3>Contest Updates</h3>
            <div className='pt-card'>
                <table className='pt-table'>
                    <thead>
                        <tr>
                            <td>ID</td>
                            <td>Name</td>
                            <td>Status</td>
                            <td>Target Risk Limit</td>
                            <td>Risk Level</td>
                        </tr>
                    </thead>
                    <tbody>
                        { ...contestStatuses }
                    </tbody>
                </table>
            </div>
            <SeedInfo seed={ seed } />
        </div>
    );
};

function formatStatus(asmState: any) {
    switch (asmState) {
        case 'COUNTY_INITIAL_STATE':
            return 'Not started';
        case 'COUNTY_AUTHENTICATED':
            return 'Logged in';
        case 'AUDIT_BOARD_OK':
            return 'Audit board established';
        case 'BALLOT_MANIFEST_OK':
            return 'Ballot manifest uploaded';
        case 'CVRS_OK':
            return 'CVR export uploaded';
        case 'AUDIT_BOARD_AND_BALLOT_MANIFEST_OK':
            return 'Ballot manifest uploaded';
        case 'AUDIT_BOARD_AND_CVRS_OK':
            return 'CVR export uploaded';
        case 'BALLOT_MANIFEST_AND_CVRS_OK':
            return 'Ballot manifest and CVR export uploaded';
        case 'AUDIT_BOARD_BALLOT_MANIFEST_AND_CVRS_OK':
            return 'Ballot manifest and CVR export uploaded';
        case 'COUNTY_AUDIT_UNDERWAY':
            return 'Audit underway';
        case 'COUNTY_AUDIT_COMPLETE':
            return 'Audit complete';
        case 'DEADLINE_MISSED':
            return 'File upload deadline missed';
        default: return '';
    }
}

const CountyUpdates = ({ countyStatus }: any) => {
    const countyStatusRows = _.map(countyStatus, (c: any) => {
        const county = _.find(counties, (x: any) => x.id === c.id);

        const status = formatStatus(c.asmState);

        return (
            <tr key={ c.id }>
                <td>{ county.name }</td>
                <td>{ status }</td>
                <td>{ c.auditedBallotCount }</td>
                <td>{ c.discrepancyCount }</td>
                <td>{ c.disagreementCount }</td>
                <td>{ c.estimatedBallotsToAudit }</td>
            </tr>
        );
    });

    const remainingToAuditTooltipContent =
        'Estimated number of ballots to audit to meet risk limit.';

    return (
        <div className='pt-card'>
            <h3>County Updates</h3>
            <div className='pt-card'>
                <table className='pt-table pt-bordered pt-condensed '>
                    <thead>
                        <tr>
                            <td>Name</td>
                            <td>Status</td>
                            <td>Submitted</td>
                            <td>Discrepancies</td>
                            <td>Disagreements</td>
                            <td>
                                <Tooltip
                                    className='pt-tooltip-indicator'
                                    content={ remainingToAuditTooltipContent }>
                                    <div>
                                        <span>Remaining to Audit </span>
                                        <span className='pt-icon-standard pt-icon-help' />
                                    </div>
                                </Tooltip>
                            </td>
                        </tr>
                    </thead>
                    <tbody>
                        { ...countyStatusRows }
                    </tbody>
                </table>
            </div>
        </div>
    );
};


const SoSHomePage = (props: any) => {
    const { contests, countyStatus, seed } = props;

    return (
        <div className='sos-home'>
            <SoSNav />
            <div className='sos-notifications pt-card'>
                <em>No notifications.</em>
            </div>
            <div className='sos-info pt-card'>
                <CountyUpdates countyStatus={ countyStatus } />
                <ContestUpdates contests={ contests } seed={ seed } />
            </div>
            <div>
                <button disabled className='pt-button pt-intent-primary'>
                    Final Audit Report
                </button>
            </div>
        </div>
    );
};


export default SoSHomePage;
