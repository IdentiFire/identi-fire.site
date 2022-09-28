import React from 'react';
import { render } from 'react-dom';



import "./style.css"

class HoplonConfirm extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			...props,
			isOpen: true,
			customButtons: [],
			customLinks: []
		};

		this.resolve = (e) => { return e };
	}

	static create() {
		const ele = document.getElementById("root");
		const containerElement = document.createElement('div');
		ele.appendChild(containerElement);
		return render(<HoplonConfirm />, containerElement);
	}

	show = (image) => {
		this.setState({ isOpen: true, image: image });
		return new Promise((res) => {
			this.resolve = res;
		});
	}

	close = () => {
		this.setState({ isOpen: false });
	}

	getClass = () => {
		if (this.state.isOpen === true) {
			return "HConfirmDialog"
		}
		return "HOPHide";
	}

	addButton = (text, callback, position) => {
		let customButtons = this.state.customButtons;
		customButtons.push({ text: text, callback: callback, position: position });
		this.setState({ customButtons: customButtons });
	}

	addLink = (text, callback, position) => {
		let customLinks = this.state.customLinks;
		customLinks.push({ text: text, callback: callback, position: position });
		this.setState({ customLinks: customLinks });
	}

	renderButtons = () => {
		let map = [];
		this.state.customButtons.forEach((button, index) => {
			map.push(<button key={"button" + index} className="HDialogButton HExecutionButton" onClick={(e) => button.callback(e)}>{button.text}</button>);
		});
		return map;
	}

	renderLinks = () => {
		let map = [];
		this.state.customLinks.forEach((link, index) => {
			map.push(<label key={"button" + index}
				className="HDialogLink HExecuteLink"
				onClick={(e) => link.callback(e)}>{link.text}</label>);
		});
		return map;
	}

	HighlightedJSON = (json) => {
		const highlightedJSON = jsonObj =>
			Object.keys(jsonObj).map(key => {
				const value = jsonObj[key];
				let valueType = typeof value;
				const isSimpleValue =
					["string", "number", "boolean"].includes(valueType) || !value;
				if (isSimpleValue && valueType === "object") {
					valueType = "null";
				}
				return (
					<div key={key} className="line">
						<span className="HJsonkey">{key}:</span>
						{isSimpleValue ? (
							<span className={valueType}>{`${value}`}</span>
						) : (
							highlightedJSON(value)
						)}
					</div>
				);
			});
		return <div className="JSONContainer"> <div className="json">{highlightedJSON(json)}</div></div>;
	};

	render = () => {
		if (!this.state.image) {
			return <span></span>;
		}
		let image = this.state.image;
		return (<div className="HConfirmDialog">
				<div className="HDialogFrameHeader">
					<label className="HDialogFrameHeaderLabel">IdentiFire</label>
				</div>
				<div>
					<img src={image} className="FPopupImg" />
				</div>
		</div>);
	}
}

export default HoplonConfirm;