import * as React from 'react';

import Nav from '../Nav';

import SeedForm from './SeedForm';

import * as corlaDate from 'corla/date';


const Breadcrumb = () => (
    <ul className='pt-breadcrumbs'>
        <li>
            <a className='pt-breadcrumb' href='/sos'>
                SoS
            </a>
        </li>
        <li>
            <a className='pt-breadcrumb' href='/sos/audit'>
                Audit Admin
            </a>
        </li>
        <li>
            <a className='pt-breadcrumb pt-breadcrumb-current'>
                Seed
            </a>
        </li>
    </ul>
);

interface PageProps {
    back: OnClick;
    nextPage: OnClick;
    formattedPublicMeetingDate: string;
    seed: string;
    uploadRandomSeed: OnClick;
}


class AuditSeedPage extends React.Component<PageProps> {
    /* const AuditSeedPage = (props: PageProps) => { */
    /* const { back, nextPage, publicMeetingDate, seed, valid, uploadRandomSeed } = props; */

    public state: any;

    constructor(props: PageProps) {
        super(props);
        this.props = props;
        this.state = {
            form: {seed: props.seed},
            formValid: false,
        };
    }

    public setValid(valid: boolean)  {
        this.setState({formValid: valid});
    }

    public render() {
        return (
            <div>
                <Nav />
                <Breadcrumb />
                <div className='pt-card'>
                    <h3>Audit Definition - Enter Random Seed</h3>
                    <div className='pt-card'>
                        Enter the random seed generated from the public meeting on { this.props.formattedPublicMeetingDate }.
                    </div>
                    <div className='pt-card'>
                        <SeedForm initSeed={ this.state.form.seed }
                                  updateForm={ (seed: string) => { this.state.form.seed = seed; } }
                                  setValid={ (v: boolean) => { this.setValid(v); } } />
                    </div>
                </div>
                <div>
                    <button onClick={ this.props.back } className='pt-button pt-breadcrumb'>
                        Back
                    </button>
                    <button onClick={ this.onSaveAndNext } disabled={!this.state.formValid} className='pt-button pt-intent-primary pt-breadcrumb'>
                        Save & Next
                    </button>
                </div>
            </div>
        );
    }

    private onSaveAndNext = () => {
        this.props.uploadRandomSeed(this.state.form.seed);
        this.props.nextPage();
    }

}


export default AuditSeedPage;
