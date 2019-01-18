import { isEmpty, merge } from 'lodash';

import { parse } from 'corla/adapter/countyDashboardRefresh';


export default function dashboardRefreshOk(
    state: County.AppState,
    action: Action.CountyDashboardRefreshOk,
): County.AppState {
    const newState = parse(action.data, state);

    // If it becomes null it will not get overwritten.
    delete state.auditBoardCount;

    const nextState = merge({}, state, newState);

    // We want to overwrite these, not deeply merge, because an empty
    // value indicates a signed-out audit board or that we are between
    // rounds.
    nextState.auditBoards = newState.auditBoards;
    nextState.currentRound = newState.currentRound;

    return nextState;
}
