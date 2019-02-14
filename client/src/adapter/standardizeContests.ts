import * as _ from 'lodash';

export function format(
    contests: DOS.Form.StandardizeContests.FormData,
): JSON.Standardize[] {
    // turn an object into an array
    return _.map(contests, (contest) => {
        return contest;
    });
}
