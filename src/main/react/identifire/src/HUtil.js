
import CryptoJS from "react-native-crypto-js";

export const encrypt = (plainText,key) => {
	if (plainText.trim() === "") {
		return "";
	}
	
	key = key.replaceAll("-","");
	//var ciphertext = C.AES.encrypt(C.enc.Hex.parse(plainText), C.enc.Hex.parse(key), { mode: C.mode.ECB, padding: C.pad.NoPadding }).ciphertext.toString();
	
	//key = key.replaceAll("-","");
	let ciphertext = CryptoJS.AES.encrypt(plainText, key).toString();
	
	return ciphertext;
	
}

export const decrypt = (text,key) => {
	let bytes  = CryptoJS.AES.decrypt(text, key);
	let originalText = bytes.toString(CryptoJS.enc.Utf8);
	return originalText;
} 

export const getSessionId = () => {
	let sessionid = sessionStorage.getItem("session_id");
	if (typeof sessionid === "undefined" || sessionid === null) {
		sessionid = "aaaa-bbbb-cccc-dddd";
		sessionStorage.setItem("session_id",sessionid);
	}
	return sessionid;
}

export const clearSession = (t) => {
	 sessionStorage.setItem("session_id",null);
 	 sessionStorage.setItem("name",t("_guest"));
}

export const isUndefined = (value) => {
	if (typeof value === "undefined") {
		return true;		
	}
	return false;
}

export const isSessionInProgress = () => {
	let sessionid = getSessionId();
	if (sessionid === "aaaa-bbbb-cccc-dddd") {
		return false;
	}
	return true;
}

export const isEmpty = (text) => {
	if (!text) {
		return true;
	}

	if (text === null) {
		return true;
	}

	if (typeof text == "undefined") {
		return true;
	}

	if (text.trim() === "") {
		return true;
	}
	return false;
}

export const doTranslate = (t, key) => {
	if (!t) {
		return key;
	}
	return t(key);
}

export const sendMail = () => {
	window.open('mailto:office@HOPL.org.il');
}

export const sortJSON = (json, prop, asc) => {
	if (typeof json === "undefined") {
		return json;
	}
	json.sort(function(a, b) {
		if (asc) {
			return (a[prop] > b[prop]) ? 1 : ((a[prop] < b[prop]) ? -1 : 0);
		} else {
			return (b[prop] > a[prop]) ? 1 : ((b[prop] < a[prop]) ? -1 : 0);
		}
	});
	return json
}

export const stringFromUTF8Array = (data, onSuccess, onFailure) => {
	const extraByteMap = [1, 1, 1, 1, 2, 2, 3, 0];
	var count = data.length;
	var str = "";

	for (var index = 0; index < count;) {
		var ch = data[index++];
		if (ch & 0x80) {
			var extra = extraByteMap[(ch >> 3) & 0x07];
			if (!(ch & 0x40) || !extra || ((index + extra) > count))
				return null;

			ch = ch & (0x3F >> extra);
			for (; extra > 0; extra -= 1) {
				var chx = data[index++];
				if ((chx & 0xC0) !== 0x80)
					return null;

				ch = (ch << 6) | (chx & 0x3F);
			}
		}

		str += String.fromCharCode(ch);
	}

	if (str != null) {
		if (onSuccess)
			onSuccess(str);

	} else {
		if (onFailure)
			onFailure();
	}

	return str;
}

export const readStream = (stream, onSuccess, onFailure) => {
	if (stream === null) {
		onFailure();
		return;
	}
	let result = "";
	let reader = stream.getReader();

	// read() returns a promise that resolves
	// when a value has been received
	reader.read().then(function processText({ done, value }) {
		// Result objects contain two properties:
		// done  - true if the stream has already given you all its data.
		// value - some data. Always undefined when done is true.
		if (done) {
			// Stream complete;
			let list = result.split(",");
			let str = stringFromUTF8Array(list, onSuccess, onFailure);
			return str;
		}
		const chunk = value;

		result += chunk;
		// Read some more, and call this function again
		return reader.read().then(processText);
	});
	return "";
}

export const fetchObject = (url, onSuccess, onFailure, body) => {
	let req = {
			body: body, // must match 'Content-Type' header
            credentials: 'same-origin', //pass cookies, for authentication
            method: 'POST',
            /*headers: {
                'Content-Type': 'multipart/form-data; boundary=—-WebKitFormBoundaryfgtsKTYLsT7PNUVD'
            },*/
	}
	 
	let isCanceled = false;
	let restPromise = fetch(process.env.REACT_APP_SERVER_ADDRESS + url, req)
		.then((response) => {
			if (isCanceled) {
				return;
			}
			if (response.status === 200) {
				readStream(response.body, onSuccess, onFailure);
			} else {
				onFailure(response);
			}
		})
		.catch((e) => {
			if (isCanceled) {
				return;
			}
			console.log(" e " + e);
			if (onFailure) {
				onFailure(e);
			}
		})

	return {
		promise: restPromise,
		cancel: () => {
			isCanceled = true;
		}
	}
}

export const fetchFromNode = async (url, onSuccess, onFailure, body, method) => {
	let req = {
			method: "POST",			
			headers: { 'Content-Type': 'application/json'},
			}
	//process.env.REACT_NODE_APP_SERVER_ADDRESS
	let uri = "http://localhost:8081/messages";	
	let response = await fetch(uri+"?data="+JSON.stringify(body), req)
	let json = await response.json();
	if (json.status === "200") {
		onSuccess(json.machineID);
	} else if (json.status === "500") {
		onFailure(json);
	}
}

export  const fetchData = async (url, onSuccess, onFailure, body, method) => {

	let session_id = getSessionId();
	body = encrypt(body,session_id);
	if (body.trim() !== "") {
		body = "data|" + body+"|";
		body += ",";	
	}
	body += "session_id%'"+session_id+"'";
	session_id=session_id.replace('-','');
	
	let req = {};
	if (method === "DOWNLOAD") {
		req = {
			method: "GET",
			headers: { 'Content-Type': 'application/json' }
		};
	} 
	if (method === "GET") {
		req = {
			method: method,
			headers: { 'Content-Type': 'application/json' }
		};
	}
	if (method === "POST") {
		req = {
			method: method,
			credentials: 'include',
			headers: { 'Content-Type': 'application/json' },
			body: '"' + body + '"'
		}
	}
	let isCanceled = false;
	let restPromise = await fetch(process.env.REACT_APP_SERVER_ADDRESS + url, req)
		.then((response) => {

			if (isCanceled) {
				return;
			}
			if (response.status === 200) {
				if (method === "GET") {
					readStream(response.body, onSuccess, onFailure);
				}
				if (method === "POST") {
					readStream(response.body, onSuccess, onFailure);
				}
			} else {
				onFailure(response);
			}
		})
		.catch((e) => {
			if (isCanceled) {
				return;
			}
			console.log(" e " + e);
			if (onFailure) {
				onFailure(e);
			}
		})

	return {
		promise: restPromise,
		cancel: () => {
			isCanceled = true;
		}
	}
}

export const generateID = () => {
	let s4 = () => {
		return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
	}
	 
	return s4()+"-"+s4()+"-"+s4()+"-"+s4();
}

export const is_israeli_id_number = (id) => {
	id = String(id).trim();
	if (id.length > 9 || isNaN(id)) {
		return false;	
	}
	id = id.length < 9 ? ("00000000" + id).slice(-9) : id;
	let sumdig =
	Array.from(id, Number).reduce((counter, digit, i) => {
		const step = digit * ((i % 2) + 1);
		return counter + (step > 9 ? step - 9 : step);
	}); 
	let mod = (sumdig % 10) === 0; 
	return mod;
}

export const verifyEmail = (mail) => {

	if (mail === "") {
		return false;
	}
	if (/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(.\w{2,3})+$/.test(mail)) {
		return true;
	}
	return false;
}

