import * as React from 'react';

import action from 'corla/action/';
import { Dialog, Button, Intent } from '@blueprintjs/core'

interface Props {
    auditBoardIndex: number;
    cvrsToAudit: object[];
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
            <td><ReauditDialog cvr={cvr} /></td>
        </tr>
    );
}

interface ReauditDialogState {
    isOpen: boolean;
    comment: string;
}

interface ReauditDialogProps {
    cvr: JSON.CVR;
}


class ReauditDialog extends React.Component<ReauditDialogProps, ReauditDialogState>{

    public state = { isOpen: false, comment: "" };

    public render() {

        const {cvr: CVR} = this.props;

        return (
            <div>
                <Button onClick={this.toggleDialog.bind(this)} text="Reaudit" />
                <Dialog
                    iconName="inbox"
                    isOpen={this.state.isOpen}
                    onClose={this.toggleDialog.bind(this)}
                    title="Add a comment in order to audit again">
                    <div className="pt-dialog-body">
                       <textarea value={this.state.comment} onChange={this.updateComment.bind(this)}/>
                    </div>
                    <div className="pt-dialog-footer">
                        <div className="pt-dialog-footer-actions">
                            <Button text="Cancel" onClick={this.toggleDialog.bind(this)} />
                            <Button
                                intent={Intent.PRIMARY}
                                onClick={this.reaudit.bind(this)}
                                text="Reaudit" />
                        </div>
                    </div>
                </Dialog>
            </div>
        );
    }

    private updateComment(event: any){
        this.setState({ comment: event.target.value});
    }

    private reaudit() {
        console.log(this.state.comment);
        this.toggleDialog();
    }

    private toggleDialog() {
        this.setState({ isOpen: !this.state.isOpen });
    }
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
