// https://stackoverflow.com/questions/46155/how-to-validate-an-email-address-in-javascript

document.getElementById("email").addEventListener("keyup", function() {
	let email = document.getElementById("email").value;
    let emailPattern = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

    let messageElement = document.getElementById("emailValidation");
    if (emailPattern.test(email)) {
        messageElement.innerHTML = "";
        document.getElementById("submitButton").disabled = false;
    } else {
        messageElement.innerHTML = "You have to enter a valid email address!";
        document.getElementById("submitButton").disabled = true;
    }
});
