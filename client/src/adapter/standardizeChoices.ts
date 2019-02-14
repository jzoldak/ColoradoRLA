import * as _ from 'lodash';

export function format(
    contests: DOS.Contests,
): JSON.Standardize[] {
    return _.map(contests, contest => {
        return {
            choices: _.filter(contest.choices,
                              (choice: DOS.Form.StandardizeChoices.Choice)  => {
                                  return choice.name != choice.canonicalName;})
                .map( (choice: DOS.Form.StandardizeChoices.Choice) => {
                return {oldName: choice.name,
                        newName: choice.canonicalName};}),
            contestId: contest.id,
        };
    });
}
