export type AuditBoardState
    = 'AUDIT_INITIAL_STATE'
    | 'WAITING_FOR_ROUND_START'
    | 'WAITING_FOR_ROUND_START_NO_AUDIT_BOARD'
    | 'ROUND_IN_PROGRESS'
    | 'ROUND_IN_PROGRESS_NO_AUDIT_BOARD'
    | 'WAITING_FOR_ROUND_SIGN_OFF'
    | 'WAITING_FOR_ROUND_SIGN_OFF_NO_AUDIT_BOARD'
    | 'AUDIT_COMPLETE'
    | 'UNABLE_TO_AUDIT'
    | 'AUDIT_ABORTED';

export type CountyState
    = 'COUNTY_INITIAL_STATE'
    | 'COUNTY_AUTHENTICATED'
    | 'BALLOT_MANIFEST_OK'
    | 'CVRS_OK'
    | 'BALLOT_MANIFEST_AND_CVRS_OK'
    | 'COUNTY_AUDIT_UNDERWAY'
    | 'COUNTY_AUDIT_COMPLETE'
    | 'DEADLINE_MISSED';

export type DOSState
    = 'DOS_INITIAL_STATE'
    | 'DOS_AUTHENTICATED'
    | 'RISK_LIMITS_SET'
    | 'CONTESTS_TO_AUDIT_IDENTIFIED'
    | 'DATA_TO_AUDIT_PUBLISHED'
    | 'RANDOM_SEED_PUBLISHED'
    | 'BALLOT_ORDER_DEFINED'
    | 'AUDIT_READY_TO_START'
    | 'DOS_AUDIT_ONGOING'
    | 'DOS_ROUND_COMPLETE'
    | 'DOS_AUDIT_COMPLETE'
    | 'AUDIT_RESULTS_PUBLISHED';