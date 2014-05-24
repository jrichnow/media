function maskInput(e) {
	// check if we have "e" or "window.event" and use them as "event"
	// Firefox doesn't have window.event
	var event = e || window.event

	var key_code = event.keyCode;
	var oElement = e ? e.target : window.event.srcElement;
	if (!event.shiftKey && !event.ctrlKey && !event.altKey) {
		if ((key_code > 47 && key_code < 58)
				|| (key_code > 95 && key_code < 106)) {

			if (key_code > 95)
				key_code -= (95 - 47);
			oElement.value = oElement.value;
		} else if (key_code == 8) {
			oElement.value = oElement.value;
		} else if (key_code != 9) {
			event.returnValue = false;
		}
	}
}