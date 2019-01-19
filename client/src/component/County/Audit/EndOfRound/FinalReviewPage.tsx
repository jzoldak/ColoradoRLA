import * as React from 'react';

import action from 'corla/action/';
import { Dialog, Button, Intent } from '@blueprintjs/core'

interface Props {
    auditBoardIndex: number;
    cvrsToAudit: JSON.CVR[];
}

function linkTo(text: String) {
    return (
        <a href="">{ text }</a>
    );
}

function row(cvr: JSON.CVR) {
    return (
        <tr key={ cvr.imprinted_id }>
            <td>{ cvr.storage_location }</td>
            <td>{ cvr.scanner_id }</td>
            <td>{ cvr.batch_id }</td>
            <td>{ cvr.record_id }</td>
            <td>{ cvr.ballot_type }</td>
            <td>{ linkTo("Reaudit") }</td>
        </tr>
    );
}

interface IDialogExampleState {
    isOpen: boolean;
}


class DialogExample extends React.Component<{}, IDialogExampleState> {
    public state = { isOpen: false };

    public render() {
        return (
            <div>
                <Button onClick={this.toggleDialog} text="Show dialog" />
                <Dialog
                    iconName="inbox"
                    isOpen={this.state.isOpen}
                    onClose={this.toggleDialog}
                    title="Dialog header">
                    <div className="pt-dialog-body">
                        Some content
                    </div>
                    <div className="pt-dialog-footer">
                        <div className="pt-dialog-footer-actions">
                            <Button text="Secondary" />
                            <Button
                                intent={Intent.PRIMARY}
                                onClick={this.toggleDialog}
                                text="Primary" />
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }

    private toggleDialog = () => this.setState({ isOpen: !this.state.isOpen });
}

const FinalReviewPage = (props: Props) => {
    const {
        auditBoardIndex,
        cvrsToAudit
    } = props;

    return (
        <div className='pt-card'>
            <h3>Final Review</h3>
            <div className='pt-card'>
                <p>Hello</p>
            </div>
            <button onClick={ () => action('FINAL_REVIEW_COMPLETE', { auditBoardIndex }) }> Continue </button>
            <DialogExample />

            <div className='pt-card'>
                <table className='pt-table pt-bordered pt-condensed'>
                    <thead>
                        <tr>
                            <th>Storage bin</th>
                            <th>Scanner</th>
                            <th>Batch</th>
                            <th>Ballot position</th>
                            <th>Ballot type</th>
                            <th>links?</th>
                        </tr>
                    </thead>
                    <tbody>
                        { cvrsToAudit.map( row ) }
                    </tbody>
                </table>
            </div>
        </div>
    );
};


export default FinalReviewPage;
