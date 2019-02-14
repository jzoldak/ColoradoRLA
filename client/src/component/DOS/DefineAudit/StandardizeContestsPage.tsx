import * as React from 'react';

import * as _ from 'lodash';

import { MenuItem } from '@blueprintjs/core';
import { Select } from '@blueprintjs/labs';

import Nav from '../Nav';

import counties from 'corla/data/counties';

import { findBestMatch } from 'string-similarity';

/**
 * The maximum percentage match at or above which a contest will be assumed to
 * match a given canonical contest.
 *
 * The algorithm used is not defined, so this may need to change if the
 * algorithm is changed.
 */
const MIN_MATCH_THRESHOLD = 0.67;

/**
 * Returns the default selection for `name` given `canonicalNames` to choose
 * from.
 *
 * The default selection will be the empty string if there was not a better
 * choice in `canonicalNames` for the given contest name.
 */
const defaultCanonicalName = (
    name: string,
    canonicalNames: string[],
): string => {
    const loweredName = name.toLowerCase();
    const loweredCanonicals = _.map(canonicalNames, s => s.toLowerCase());

    const { bestMatch, bestMatchIndex } = findBestMatch(
        loweredName,
        loweredCanonicals,
    );

    if (bestMatch.rating < MIN_MATCH_THRESHOLD) {
        return '';
    }

    return canonicalNames[bestMatchIndex];
};

const Breadcrumb = () => (
    <ul className='pt-breadcrumbs'>
        <li>
            <a className='pt-breadcrumb' href='/sos'>SoS</a>
        </li>
        <li>
            <a className='pt-breadcrumb' href='/sos/audit'>Audit Admin</a>
        </li>
        <li>
            <a className='pt-breadcrumb pt-breadcrumb-current'>Standardize Contest Names</a>
        </li>
    </ul>
);

interface UpdateFormMessage {
    id: number;
    name: string;
}

interface TableProps {
    contests: DOS.Contests;
    canonicalContests: DOS.CanonicalContests;
    updateFormData: (msg: UpdateFormMessage) => void;
}

const StandardizeContestsTable = (props: TableProps) => {
    const { canonicalContests, contests, updateFormData } = props;

    return (
        <table className='pt-table pt-striped'>
            <thead>
                <tr>
                    <th>County</th>
                    <th>Current Contest Name</th>
                    <th>Standardized Contest Name</th>
                </tr>
            </thead>
            <ContestBody contests={ contests }
                         canonicalContests={ canonicalContests }
                         updateFormData={ updateFormData } />
        </table>
    );
};

interface BodyProps {
    contests: DOS.Contests;
    canonicalContests: DOS.CanonicalContests;
    updateFormData: (msg: UpdateFormMessage) => void;
}

const ContestBody = (props: BodyProps) => {
    const { canonicalContests, contests, updateFormData } = props;

    const rows = _.map(contests, c => {
        return <ContestRow key={ c.id }
                           contest={ c }
                           canonicalContests={ canonicalContests }
                           updateFormData={ updateFormData } />;
    });

    return (
      <tbody>{ rows }</tbody>
    );
};

interface ContestRowProps {
    contest: Contest;
    canonicalContests: DOS.CanonicalContests;
    updateFormData: (msg: UpdateFormMessage) => void;
}

const ContestRow = (props: ContestRowProps) => {
    const { canonicalContests, contest, updateFormData } = props;
    const countyName = counties[contest.countyId].name;

    const standards = canonicalContests[countyName];

    const defaultName = defaultCanonicalName(contest.name, standards);

    const changeHandler = (e: React.FormEvent<HTMLSelectElement>) => {
        const v = e.currentTarget.value;

        updateFormData({id: contest.id, name: v});
    };

    return (
        <tr>
            <td>{ counties[contest.countyId].name }</td>
            <td>{ contest.name }</td>
            <td>
                <form>
                    <select name={ String(contest.id) }
                            onChange={ changeHandler }
                            defaultValue={ defaultName }>
                        <option value=''>-- No change --</option>
                        {
                          _.map(standards, n => <option value={ n }>{ n }</option>)
                        }
                    </select>
                </form>
            </td>
        </tr>
    );
};

interface PageProps {
    areContestsLoaded: boolean;
    canonicalContests: DOS.CanonicalContests;
    contests: DOS.Contests;
    forward: (x: DOS.Form.StandardizeContests.FormData) => void;
    back: () => void;
}

class StandardizeContestsPage extends React.Component<PageProps> {
    public formData: DOS.Form.StandardizeContests.FormData = {};

    public constructor(props: PageProps) {
        super(props);

        this.updateFormData = this.updateFormData.bind(this);
    }

    public render() {
        const {
            areContestsLoaded,
            back,
            canonicalContests,
            contests,
            forward,
        } = this.props;

        if (areContestsLoaded) {
            return (
                <div>
                    <Nav />
                    <Breadcrumb />
                    <h2>Standardize Contest Names</h2>
                    <div className='pt-card'>
                        <p>
                            Contest names must be standardized to group records
                            correctly across jurisdictions. Below is a list of
                            contests that do not match the standardized contest
                            names provided by the state. For each of the contests
                            listed, please choose the appropriate standardized
                            version from the options provided, then save your
                            choices and move forward.
                        </p>

                        <StandardizeContestsTable canonicalContests={ canonicalContests }
                                                  contests={ contests }
                                                  updateFormData={ this.updateFormData } />
                    </div>
                    <div>
                        <button onClick={ back }
                                className='pt-button pt-breadcrumb'>
                            Back
                        </button>
                        <button onClick={ () => forward(this.formData) }
                                className='pt-button pt-intent-primary pt-breadcrumb'>
                            Save & Next
                        </button>
                    </div>
                </div>
            );
        } else {
            return (
                <div>
                    <Nav />
                    <Breadcrumb />
                    <h2>Standardize Contest Names</h2>
                    <div className='pt-card'>
                        Waiting for counties to upload contest data.
                    </div>
                    <div>
                        <button onClick={ back }
                                className='pt-button pt-breadcrumb'>
                            Back
                        </button>
                        <button disabled
                                className='pt-button pt-intent-primary pt-breadcrumb'>
                            Save & Next
                        </button>
                    </div>
                </div>
            );
        }
    }

    private updateFormData(msg: UpdateFormMessage) {
        const { id, name } = msg;

        if (_.isEmpty(name)) {
            delete this.formData[id];
        } else {
            this.formData[id] = { name, contestId: id };
        }
    }
}

export default StandardizeContestsPage;
