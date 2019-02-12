import * as _ from 'lodash';

export function format(
    contests: DOS.Form.StandardizeContests.FormData,
): JSON.Standardize[] {
    return _.map(contests, (contest, id) => {
        return {
            contest: id,
            name: contest.name,
        };
    });
}
