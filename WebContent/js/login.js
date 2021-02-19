(function() {
	var pageAnimation,
			signinForm, 
			signupForm,
			pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => pageOrchestrator.start());
	
	function PageAnimation(page, button) {
		this.page = page;
		this.button = button;
		
		this.registerEvents = () => {
			this.button.addEventListener("click", () => {
				var delay = 200;
				this.button.animate([
					{ transform: "scale(1)" },
					{ transform: "scale(.9)" },
					{ transform: "scale(1)" }					
				], delay);
				setTimeout(() => {
					this.page.classList.toggle("alt");
				}, delay);
			});
		}
	}		
	
	function Form(button) {
		this.button = button;
		
		this.registerEvents = () => {
			this.button.addEventListener("click", this.submitForm);		
		}
		
		this.submitForm = e => {
			var form = e.target.closest("form");
			var alert = form.querySelector(".error-msg");
			
			if(!form.checkValidity()) {
				form.reportValidity();
				return;
			}
			
			if(form.hasAttribute("data-check")) {
				var pass1 = form.querySelector("input[name='password']").value;
				var pass2 = form.querySelector("input[name='pass-confirm']").value;
				if(pass1 != pass2) {
					alert.textContent = "The two passwords must be identical";
					return;
				}
			}
			
			//AJAX POST /CheckLogin or /AddUser
			makeCall("POST", form.getAttribute("data-src"), form, req => {
				if(req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;
					if(req.status == 200) {
						sessionStorage.setItem("username", message);
						window.location.href = "home.html";
					} else {
						alert.textContent = message;
					}
				}
			});
		}
	}	
	
	function PageOrchestrator() {
		this.start = () => {
			pageAnimation = new PageAnimation(
				document.getElementById("page"),
				document.getElementById("toggler")
			);
			pageAnimation.registerEvents();
			
			signinForm = new Form(
					document.getElementById("signin")
			);
			signinForm.registerEvents();
			
			signupForm = new Form(
					document.getElementById("signup")
			);
			signupForm.registerEvents();
		}
	}
})();