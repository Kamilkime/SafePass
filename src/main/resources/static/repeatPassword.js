document.getElementById("repeatPassword").addEventListener("keyup", function() {
	let password = document.getElementById("password").value;
	let repeatPassword = document.getElementById("repeatPassword").value;

    let messageElement = document.getElementById("message");
    if (password == repeatPassword) {
        messageElement.innerHTML = "";
        document.getElementById("submitButton").disabled = false;
    } else {
        messageElement.innerHTML = "Passwords do not match!";
        document.getElementById("submitButton").disabled = true;
    }

    if (password.length > 50) {
        messageElement.innerHTML = "Your password can't be longer than 50 characters!";
        document.getElementById("submitButton").disabled = true;
    }
});

document.getElementById("password").addEventListener("keyup", function() {
	let password = document.getElementById("password").value;
	let repeatPassword = document.getElementById("repeatPassword").value;

    let messageElement = document.getElementById("message");
    if (password == repeatPassword) {
        messageElement.innerHTML = "";
        document.getElementById("submitButton").disabled = false;
    } else {
        messageElement.innerHTML = "Passwords do not match!";
        document.getElementById("submitButton").disabled = true;
    }

    if (password.length > 50) {
        messageElement.innerHTML = "Your password can't be longer than 50 characters!";
        document.getElementById("submitButton").disabled = true;
    }
});

