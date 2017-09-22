import * as _ from 'lodash';


interface Elector {
    first_name: string;
    last_name: string;
    political_party: string;
}

type Status = 'NO_DATA' | 'CVRS_UPLOADED_SUCCESSFULLY' | 'ERROR_IN_UPLOADED_DATA';

interface Round {
    actual_count: number;
    disagreements: number;
    discrepancies: number;
    expected_count: number;
    number: number;
    signatories: any;
    start_audit_prefix_length: number;
    start_index: number;
    start_time: Date;
}

interface CountyDashboard {
    asm_state: string;
    audit_time: string;
    audit_board: any;
    audited_ballot_count: number;
    audited_prefix_length: number;
    ballot_manifest_count: number;
    ballot_manifest_file: any;
    ballot_under_audit_id: number;
    ballots_remaining_in_round: number;
    current_round: Round;
    cvr_export_count: number;
    cvr_export_file: any;
    contests: number[];
    contests_under_audit: number[];
    disagreement_count: number;
    discrepancy_count: number;
    estimated_ballots_to_audit: number;
    general_information: string;
    id: number;
    risk_limit: number;
    rounds: Round[];
    status: Status;
}

const parseBoardMember = (e: Elector): any => ({
    firstName: e.first_name,
    lastName: e.last_name,
    party: e.political_party,
});

const parseAuditBoard = (board: any) => {
    if (!board) {
        return [];
    }

    return board.members.map(parseBoardMember);
};

const parseTimestamp = (ts: string): Date => new Date(ts);

const pivot = (a: any) => {
    const o: any = {};

    a.forEach((v: any) => {
        o[v.id] = v;
    });

    return o;
};

export const parseContests = (contestIds: any, state: any): any => {
    if (!state.county.contestDefs) {
        return [];
    }

    if (_.isEmpty(state.county.contestDefs)) {
        return [];
    }

    const { contestDefs } = state.county;

    return _.map(contestIds, (id: any) => contestDefs[id]);
};

const parseContestsUnderAudit = (contestIds: any, state: any): any => {
    if (!state.county.contestDefs) {
        return [];
    }

    if (_.isEmpty(state.county.contestDefs)) {
        return [];
    }

    const { contestDefs } = state.county;

    return _.map(contestIds, (reason: any, id: any) => {
        const def = state.county.contestDefs[id];
        return { ...def, reason };
    });
};

function parseRound(data: Round) {
    if (!data) {
        return {};
    }

    return {
        actualCount: data.actual_count,
        disagreements: data.disagreements,
        discrepancies: data.discrepancies,
        expectedCount: data.expected_count,
        number: data.number,
        signatories: data.signatories || [],
        startAuditPrefixLength: data.start_audit_prefix_length,
        startIndex: data.start_index,
        startTime: data.start_time,
    };
}

function parseRounds(rounds: Round[]) {
    if (!rounds) {
        return [];
    }

    return rounds.map(parseRound);
}

function parseElection(data: any): any {
    return {
        date: new Date(data.audit_info.election_date),
        type: data.audit_info.election_type,
    };
}

function parseRiskLimit(data: any): number {
    return _.get(data, 'audit_info.risk_limit');
}

function parseDisCount(data: any): number {
    return _.sum(_.values(data));
}

function parseFile(file: any): any {
    if (!file) { return null; }

    return {
        countyId: file.county_id,
        hash: file.hash,
        hashStatus: file.hash_status,
        id: file.file_id,
        name: file.filename,
        size: file.size,
        status: file.status,
        timestamp: new Date(file.timestamp),
    };
}

export const parse = (data: CountyDashboard, state: any): any => {
    const findContest = (id: any) => state.county.contestDefs[id];

    return {
        asm_state: data.asm_state,
        auditBoard: parseAuditBoard(data.audit_board),
        auditTime: data.audit_time ? parseTimestamp(data.audit_time) : null,
        auditedBallotCount: data.audited_ballot_count,
        auditedPrefixLength: data.audited_prefix_length,
        ballotManifest: parseFile(data.ballot_manifest_file),
        ballotManifestCount: data.ballot_manifest_count,
        ballotUnderAuditId: data.ballot_under_audit_id,
        ballotsRemainingInRound: data.ballots_remaining_in_round,
        contests: parseContests(data.contests, state),
        contestsUnderAudit: parseContestsUnderAudit(data.contests_under_audit, state),
        currentRound: parseRound(data.current_round),
        cvrExport: parseFile(data.cvr_export_file),
        cvrExportCount: data.cvr_export_count,
        disagreementCount: parseDisCount(data.disagreement_count),
        discrepancyCount: parseDisCount(data.discrepancy_count),
        election: parseElection(data),
        estimatedBallotsToAudit: data.estimated_ballots_to_audit,
        generalInformation: data.general_information,
        id: data.id,
        riskLimit: parseRiskLimit(data),
        rounds: parseRounds(data.rounds),
        status: data.status,
    };
};
