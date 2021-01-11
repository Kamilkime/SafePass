// https://www.cssscript.com/javascript-plugin-rate-password-strength-passwordmeter-js/
// https://en.wikipedia.org/wiki/Password_strength#Random_passwords

document.getElementById("password").addEventListener("keyup", function() {
	let passwordArray = password.value.split("");

	let occurrence = {
		number: false,
		lowercase: false,
		uppercase: false,
		specialChar: false,
	}

	var validation = {
		isNumber: function(val){
			let pattern = /^\d+$/;
			return pattern.test(val);
		},
		isLowercase: function(val){
			let pattern = /[a-z]/;
			return pattern.test(val);
		},
		isUppercase: function(val){
			let pattern = /[A-Z]/;
			return pattern.test(val);
		},
		isSpecialChar: function(val){
			let pattern = /^[!@#\$%\^\&*\)\(+=._-]+$/g;
			return pattern.test(val);
		}
	}

	for (let i = 0; i < passwordArray.length; i++){
		if (!occurrence.number && validation.isNumber(passwordArray[i])){
			occurrence.number = true;
		} else if (!occurrence.lowercase && validation.isLowercase(passwordArray[i])){
			occurrence.lowercase = true;
		} else if (!occurrence.uppercase && validation.isUppercase(passwordArray[i])){
			occurrence.uppercase = true;
		} else if (!occurrence.specialChar && validation.isSpecialChar(passwordArray[i])){
			occurrence.specialChar = true;
		}
	}

	function assessTotalScore(){
		let ratingElement = document.querySelector(".rating");

		let poolSize = 0;
		if (occurrence.number) {
		    poolSize += 10;
		}

		if (occurrence.lowercase) {
        	poolSize += 26;
        }

        if (occurrence.uppercase) {
            poolSize += 26;
        }

        if (occurrence.specialChar) {
            poolSize += 15;
        }

        let ratingValue = "";
        let ratingClass = "";

        let entropy = Math.log2(poolSize ** passwordArray.length);
        console.log(entropy);
        if (entropy < 40) {
            ratingValue = "Very weak password"
            ratingClass = "veryweakPassword";
        } else if (entropy < 60) {
            ratingValue = "Weak password"
            ratingClass = "weakPassword"
        } else if (entropy < 90) {
            ratingValue = "Moderate password"
            ratingClass = "moderatePassword"
        } else if (entropy < 120) {
            ratingValue = "Strong password"
            ratingClass = "strongPassword"
        } else {
            ratingValue = "Very strong password"
            ratingClass = "verystrongPassword"
        }

        let classList = ratingElement.classList;
        while (classList.length > 0) {
           classList.remove(classList.item(0));
        }

        ratingElement.classList.add(ratingClass);
        ratingElement.classList.add("rating");
        ratingElement.innerHTML = ratingValue;
	}

	assessTotalScore();
});
