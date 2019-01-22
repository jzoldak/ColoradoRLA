import * as _ from 'lodash';
import * as React from 'react';

import {
    Button,
    Dialog,
    IButtonProps,
    IDialogProps,
    Intent,
    FormGroup,
    TextArea,
} from '@blueprintjs/core';

import action from 'corla/action/';

interface ReviewDialogProps {
    close: () => void;
    isOpen: boolean;
    onClose: IDialogProps['onClose'];
}

interface ReviewDialogState {
    comment: string;
}

class ReviewDialog extends React.Component<ReviewDialogProps, ReviewDialogState> {
    constructor(props: ReviewDialogProps) {
        super(props);

        this.state = { comment: "" };
    }

    private handleClick: IButtonProps['onClick'] = () => {
        this.props.close();
    }

    private handleCommentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        this.setState({ comment: e.target.value });
    };

    render() {
        return (
            <Dialog iconName="confirm"
                    isOpen={ this.props.isOpen }
                    onClose={ this.props.onClose }
                    title="Final review">
                <div className='pt-dialog-body'>
                    <FormGroup label='Why are you re-auditing this ballot?'
                               labelFor='review-dialog-input'>
                        <TextArea id='review-dialog-input'
                                  className='pt-fill'
                                  value={ this.state.comment }
                                  onChange={ this.handleCommentChange } />
                    </FormGroup>
                </div>
                <div className="pt-dialog-footer">
                    <div className="pt-dialog-footer-actions">
                        <Button text="Cancel"
                                onClick={ this.handleClick } />
                        <Button intent={ Intent.PRIMARY }
                                text="Re-audit" />
                    </div>
                </div>
            </Dialog>
        );
    }
}

interface ReviewButtonProps {
    cvrId: number;
    openDialog: () => void;
}

const ReviewButton = (props: ReviewButtonProps) => {
    const handler: IButtonProps['onClick'] = () => {
        props.openDialog();
    };

    return (
        <Button text="Re-audit"
                onClick={ handler }  />
    );
}

interface FinalReviewPageProps {
    auditBoardIndex: number;
    cvrsToAudit: JSON.CVR[];
}

interface FinalReviewPageState {
    dialogIsOpen: boolean;
}

const reviewCompleteHandler = (auditBoardIndex: number) => {
    return () => {
        action('FINAL_REVIEW_COMPLETE', auditBoardIndex);
    }
};

const rowRenderer = (openDialog: () => void) => {
    return (cvr: JSON.CVR) => {
        return (
            <tr key={ cvr.cvr_number }>
                <td>{ cvr.storage_location }</td>
                <td>{ cvr.scanner_id }</td>
                <td>{ cvr.batch_id }</td>
                <td>{ cvr.record_id }</td>
                <td>{ cvr.ballot_type }</td>
                <td><ReviewButton cvrId={ cvr.cvr_number }
                                  openDialog={ openDialog } /></td>
            </tr>
        );
    }
};

class FinalReviewPage extends React.Component<FinalReviewPageProps, FinalReviewPageState> {
    constructor(props: FinalReviewPageProps) {
        super(props);
        this.state = { dialogIsOpen: false };
    }

    private closeDialog: () => void = () => {
        this.setState({ dialogIsOpen: false });
    }

    private openDialog: () => void = () => {
        this.setState({ dialogIsOpen: true });
    }

    render() {
        const { auditBoardIndex, cvrsToAudit } = this.props;

        const row = rowRenderer(this.openDialog);

        return (
            <div className='pt-card'>
                <h3>Final Review</h3>

                <div className='pt-card'>
                    <p>Hello</p>
                </div>

                <Button onClick={ reviewCompleteHandler(auditBoardIndex) }>
                    Continue
                </Button>

                <div className='pt-card'>
                    <table className='pt-table pt-bordered pt-condensed'>
                        <thead>
                            <tr>
                                <th>Storage bin</th>
                                <th>Scanner</th>
                                <th>Batch</th>
                                <th>Ballot position</th>
                                <th>Ballot type</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>{ _.map(cvrsToAudit, row) }</tbody>
                    </table>
                </div>

                <ReviewDialog close={ this.closeDialog }
                              isOpen={ this.state.dialogIsOpen }
                              onClose={ this.closeDialog } />
            </div>
        );
    }
};


export default FinalReviewPage;
