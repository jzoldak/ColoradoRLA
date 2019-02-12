import * as _ from 'lodash';

export function format(
    contests: DOS.Contests,
): JSON.Standardize[] {
    return _.map(contests, (contest, id) => {
        return {
            choices: _.map(contest.choices, choice => choice.name),
            contest: id,
        };
    });
}
