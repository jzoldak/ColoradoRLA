import * as React from 'react';

import { connect, MapStateToProps } from 'react-redux';

import action from 'corla/action';

function withSync<P, SelectP, TOwnProps, BindP, BindS>(
    Wrapped: React.ComponentType<P>,
    didMount: string,
    select: MapStateToProps<SelectP, TOwnProps>,
    bind?: Bind<BindP, BindS>,
) {
    type Props = P & SelectP & TOwnProps & BindP;

    class Wrapper extends React.Component<Props> {
        public componentDidMount() {
            action(didMount);
        }

        public render() {
            return <Wrapped { ...this.props } />;
        }
    }

    if (bind) {
        return connect(select, bind)(Wrapper);
    } else {
        return connect(select)(Wrapper);
    }
}

export default withSync;
