import * as React from 'react';

import * as _ from 'lodash';

import { connect } from 'react-redux';

import { Redirect } from 'react-router-dom';

import { History } from 'history';

import Nav from '../Nav';

import standardizeChoices from 'corla/action/dos/standardizeChoices';

import StandardizeChoicesPage from './StandardizeChoicesPage';

import withDOSState from 'corla/component/withDOSState';
import withPoll from 'corla/component/withPoll';

import counties from 'corla/data/counties';

// The next URL path to transition to.
const NEXT_PATH = '/sos/audit/select-contests';

// The previous URL path to transition to.
const PREV_PATH = '/sos/audit/standardize-contests';

/**
 * Denormalize the DOS.Contests data structure from the application state into
 * something that can be easily displayed in a tabular format.
 */
const flattenContests = (
    contests: DOS.Contests,
    canonicalChoices: DOS.CanonicalChoices,
): DOS.Form.StandardizeChoices.Row[] => {
    return _.flatMap(contests, (contest: Contest) => {
        return _.map(contest.choices, (choice: ContestChoice, idx) => {
            return {
                choiceIndex: idx,
                choiceName: choice.name,
                contestId: contest.id,
                contestName: contest.name,
                choices: canonicalChoices[contest.name],
            };
        });
    });
};

/**
 * Remove rows with no canonical choices.
 */
const filterRows = (
    rows: DOS.Form.StandardizeChoices.Row[],
): DOS.Form.StandardizeChoices.Row[] => {
    return _.filter(rows, row => !_.isEmpty(row.choices));
};

/**
 * Merge the corrected choices in `data` into `contests`.
 */
const mergeNewChoices = (
    contests: DOS.Contests,
    data: DOS.Form.StandardizeChoices.FormData,
): DOS.Contests => {
    return _.map(contests, (contest: Contest) => {
        if (!_.isNil(data[contest.id])) {
            const newChoices = data[contest.id];
            contest.choices = _.map(contest.choices, (choice, idx) => {
                const newChoice = newChoices[idx];
                if (!_.isNil(newChoice)) {
                    /* choice.name = newChoice; */
                    choice.canonicalName = newChoice;
                }

                return choice;
            });
        }

        return contest;
    });
}

interface Props {
    areChoicesLoaded: boolean;
    asm: DOS.ASMState;
    contests: DOS.Contests;
    canonicalChoices: DOS.CanonicalChoices;
    history: History;
}

const PageContainer = (props: Props) => {
    const {
        areChoicesLoaded,
        asm,
        canonicalChoices,
        contests,
        history,
    } = props;

    const nextPage = (data: DOS.Form.StandardizeChoices.FormData) => {
        standardizeChoices(mergeNewChoices(contests, data));
        history.push(NEXT_PATH);
    };

    const previousPage = () => {
        history.push(PREV_PATH);
    };

    if (asm === 'DOS_AUDIT_ONGOING') {
        return <Redirect to='/sos' />;
    }

    let rows: DOS.Form.StandardizeChoices.Row[] = [];

    if (areChoicesLoaded) {
        rows = filterRows(flattenContests(contests, canonicalChoices));

        if (_.isEmpty(rows)) {
            return <Redirect to={ NEXT_PATH } />;
        }
    }

    return <StandardizeChoicesPage areChoicesLoaded={ areChoicesLoaded }
                                   back={ previousPage }
                                   contests={ contests }
                                   rows={ rows }
                                   forward={ nextPage } />;
};

const mapStateToProps = (state: DOS.AppState) => {
    const contests = state.contests;
    const canonicalChoices = state.canonicalChoices;
    const areChoicesLoaded = !_.isEmpty(contests)
        && !_.isEmpty(canonicalChoices);

    return {
        areChoicesLoaded,
        asm: state.asm,
        canonicalChoices,
        contests,
    };
};

export default withPoll(
    withDOSState(PageContainer),
    'DOS_SELECT_CONTESTS_POLL_START',
    'DOS_SELECT_CONTESTS_POLL_STOP',
    mapStateToProps,
);
