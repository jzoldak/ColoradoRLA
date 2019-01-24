import * as React from 'react';

import { Button, Dialog, FormGroup, Intent, TextArea } from '@blueprintjs/core';

import action from 'corla/action/';


interface FinalReviewDialogProps {
    cvr?: JSON.CVR;
    onClose: () => void;
    isOpen: boolean;
}

interface FinalReviewDialogState {
    comment: string;
}

class FinalReviewDialog extends React.Component<FinalReviewDialogProps, FinalReviewDialogState> {
    constructor(props: FinalReviewDialogProps) {
        super(props);

        this.state = { comment: '' };
    }

    public render() {
        const cvr = this.props.cvr;

        const title = cvr ? 'Re-audit ' + cvr.imprinted_id : 'Re-audit';

        return (
            <Dialog iconName='confirm'
                    isOpen={ this.props.isOpen }
                    onClose={ this.handleCancel }
                    title={ title }>
                <div className='pt-dialog-body'>
                    <p>
                        To re-audit this ballot, please explain your reason for
                        re-auditing this ballot in the space below and click
                        "Next". To go back, click "Back".
                    </p>
                    <FormGroup label='Reason for re-audit '
                               labelFor='review-dialog-input'
                               requiredLabel={ true }>
                        <TextArea id='review-dialog-input'
                                  className='pt-fill'
                                  value={ this.state.comment }
                                  onChange={ this.handleCommentChange } />
                    </FormGroup>
                </div>
                <div className='pt-dialog-footer'>
                    <div className='pt-dialog-footer-actions'>
                        <Button text='Cancel'
                                onClick={ this.handleCancel } />
                        <Button intent={ Intent.PRIMARY }
                                onClick={ this.handleReAudit }
                                text='Next' />
                    </div>
                </div>
            </Dialog>
        );
    }

    private handleCancel = () => {
        this.props.onClose();
    }

    private handleCommentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        this.setState({ comment: e.target.value });
    }

    private handleReAudit = () => {
        const cvr = this.props.cvr;

        if (cvr) {
            action('RE_AUDIT_CVR', {
                comment: this.state.comment,
                cvrId: cvr.db_id,
            });
        }
    }
}

export default FinalReviewDialog;
