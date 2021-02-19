(function() {
	var header,
			sidebarManager, 
			gallerySidebar,
			albumSidebar,
			mainContainer,
			page = new PageOrchestrator();

	window.addEventListener("load", () => {
		page.start();
		page.show();
	});	
	
	function Header(message, username) {
		//Top-right header, contains username and logout button
		//Also changes window title adding username
		this.message = message;
		this.username = username;
		
		this.show = () => {			
			document.title = this.username + " | My Gallery";			
			message.textContent = "Hi, " + this.username;
		}
	}
	
	function SidebarManager(sidebars, pageContent) {
		//Manages generic sidebar events (i.e. open, close, alert)
		this.sidebars = sidebars;
		this.pageContent = pageContent;
		
		this.registerEvents = () => {
			this.sidebars.forEach(sb => sb.addEventListener("click", this.openSidebarListener));
			this.pageContent.addEventListener("click", this.closeSidebars);
		}
		
		this.closeSidebars = () => {
			this.sidebars.forEach(sb => sb.classList.remove("open"));			
		}
		
		this.openSidebarListener = e => {
			//Ignores events not coming directly from a sidebar
			if([...this.sidebars].includes(e.target))
				this.openSidebar(e.target);
		}
		
		this.openSidebar = sidebar => {
			this.closeSidebars();
			sidebar.classList.add("open");		
		}
		
		this.showAlert = (container, message, type) => {
			//Shows an alert that can be type = info_msg or type = error_msg
			container.appendChild(get("h4", type, message));
		}
	}
	
	function GallerySidebar(sidebar, albumsContainer) {
		//Manages gallery events (i.e. d&d, album selection)
		this.sidebar = sidebar;
		this.albumsContainer = albumsContainer;
		this.saveBtn;
		this.order;		//Stored to check whether or not to show the save button
		
		this.reset = () => {
			this.order = null;
			disable(this.sidebar);
			sidebarManager.closeSidebars();
		}
		
		this.registerEvents = () => {
			this.saveBtn = this.sidebar.querySelector(".button");
			this.saveBtn.addEventListener("click", this.saveAlbumOrder);			
		}
		
		this.show = () => {
			//AJAX GET /GetAlbumsList
			makeCall("GET", "GetAlbumsList", null, req => {
				if(req.readyState == 4) {
					var message = req.responseText;
					if(req.status == 200) {
						this.update(JSON.parse(message));
					} else {
						sidebarManager.showAlert(this.albumsContainer, message, "error-msg");
					}
				}
			});
		}
		
		this.update = albumsList => {		
			if(albumsList.length == 0) {
				sidebarManager.showAlert(this.albumsContainer, "There's no album yet", "info-msg");
			} else {			
				this.albumsContainer.innerHTML = "";
				this.albumsContainer.addEventListener("dragover", this.dragOver);
				
				albumsList.forEach(a => this.albumsContainer.appendChild(this.buildAlbum(a)));
			}
			
			this.order = this.getAlbumsOrder();
			enable(this.sidebar);
			sidebarManager.openSidebar(this.sidebar);				
		}
		
		this.buildAlbum = album => {
			var elem = get("h4", "album-name", album.title, {"data-album-id": album.id, "draggable": "true"});
			elem.addEventListener("dragstart", this.dragStart);
			elem.addEventListener("dragend", this.dragEnd);		
			elem.addEventListener("click", () => albumSidebar.show(album));
			return elem;
		}
		
		this.getDragSibling = y => {
			//Returns the closest sibling below the "moving" album
			return [...this.albumsContainer.querySelectorAll(".album-name")].reduce((closest, album) => {
				var box = album.getBoundingClientRect();
				var offset = y - box.top - box.height / 2;
				if(offset < 0 && offset > closest.offset)
					return {offset: offset, element: album};
				else
					return closest;
			}, {offset: -Infinity}).element;
		}
		
		this.dragStart = e => {
			e.target.classList.add("moving");
		}
		
		this.dragEnd = e => {			
			e.target.classList.remove("moving");
			
			//Updates this.order, if changed shows the save button
			var oldOrder = this.order;
			this.order = this.getAlbumsOrder();
			if(this.order != oldOrder)
				enable(this.saveBtn);
		}
		
		this.dragOver = e => {				
			//Appends the "moving" album after the closest sibling (or at the end of the container)
			e.preventDefault();			
			var sibling = this.getDragSibling(e.clientY);
			var moving = document.querySelector(".moving");
			if(sibling == null)
				this.albumsContainer.appendChild(moving);
			else
				this.albumsContainer.insertBefore(moving, sibling);
		}
		
		this.getAlbumsOrder = () => {		
			return [...this.albumsContainer.querySelectorAll(".album-name")].map(a => a.getAttribute("data-album-id")).toString();
		}
		
		this.saveAlbumOrder = () => {			
			//Builds a form containing the order
			var input = get("input", "", "", {"name": "order"});
			input.value = this.order;
			
			var form = append(get("form"), input);
			
			//AJAX POST /UpdateAlbumsOrder this.order
			makeCall("POST", "UpdateAlbumsOrder", form, req => {
				if(req.readyState == 4) {
					var message = req.responseText;
					if(req.status == 200) {
						disable(this.saveBtn);
					} else {
						sidebarManager.showAlert(this.albumsContainer, message, "error-msg");
					}
				}
			});
		}		
	}
	
	function AlbumSidebar(sidebar, info, slider) {
		//Manages album events (i.e. slide, image hovering)
		this.sidebar = sidebar;
		this.info = info;
		this.slider = slider;
		
		this.album;
		this.images;
		
		this.reset = () => {
			disable(this.sidebar);
			this.album = null;
			this.images = {};
		}
		
		this.show = album => {
			this.album = album;
			//AJAX GET /GetImagesList?album=album.id
			makeCall("GET", "GetImagesList?album=" + album.id, null, req => {
				if(req.readyState == 4) {
					var message = req.responseText;
					if(req.status == 200) {
						this.update(JSON.parse(message));
						mainContainer.show(this.album);
					} else {
						sidebarManager.showAlert(this.slider, message, "error-msg");
					}
				}
			});
		}
		
		this.update = imagesList => {		
			this.showInfo();
			
			this.slider.innerHTML = "";
			
			if(imagesList.length == 0) {
				sidebarManager.showAlert(this.slider, "There's no image yet", "info-msg");
			} else {			
				var imageContainer = null;
				for(var i = 0; i < imagesList.length; i++) {
					if(i % 5 == 0) {
						imageContainer = this.buildContainer(imageContainer);
						this.slider.appendChild(imageContainer);
					}
					imageContainer.appendChild(this.buildImage(imagesList[i]));
				}
			}
			
			var container = this.slider.querySelector(".image-container");
			if(container)
				this.addImageListener(container);
			
			var arrow = this.slider.querySelector(".image-container .arrow");
			if(arrow)
				enable(arrow);
			
			enable(this.sidebar);
			sidebarManager.openSidebar(this.sidebar);
		}
		
		this.showInfo = () => {
			this.info.querySelector(".title").textContent = this.album.title;
			this.info.querySelector(".date").textContent = formatDate(this.album.date);
		}
		
		this.buildContainer = oldContainer => {
			var newContainer = get("div", "image-container");
			
			if(oldContainer) {
				var arrowDown = get("i", "fas fa-chevron-down fa-fh arrow next disabled");
				arrowDown.addEventListener("click", () => this.slide(oldContainer, "prev", newContainer, "next"));
				oldContainer.appendChild(arrowDown);
				
				var arrowUp = get("i", "fas fa-chevron-up fa-fh arrow prev disabled");
				arrowUp.addEventListener("click", () => this.slide(newContainer, "next", oldContainer, "prev"));
				newContainer.appendChild(arrowUp);
				
				newContainer.classList.add("next");
			}
			
			return newContainer;
		}
		
		this.buildImage = image => {
			this.images[image.id] = image;
			
			var img = get("img", "", "", {"src": "img/" + image.src});		
			var title = get("h4", "absolute-center", image.title);
			
			return append(get("div", "image-thumb overlay", "", {"data-image-id": image.id}), img, title);
		}
		
		this.slide = (from, fromType, to, toType) => {
			//Removes image listener to avoid hovering while sliding
			this.removeImageListener(from);
			
			from.classList.add(fromType);
			to.classList.remove(toType);
			
			from.querySelectorAll(".arrow").forEach(a => disable(a));
			to.querySelectorAll(".arrow").forEach(a => enable(a));
			
			//Adds image listener after a timeout to avoid hovering while sliding
			this.addImageListener(to);
		}
		
		this.removeImageListener = container => {
			container.querySelectorAll(".image-thumb").forEach(i => i.removeEventListener("mouseenter", this.imageListener));
		}
		
		this.addImageListener = container => {
			//Gets the same delay of the arrows inside the container (default: 800ms)
			var delay = 800;
			var arrow = container.querySelector(".arrow");
			if(arrow) {
				var arrowStyle = window.getComputedStyle(arrow);
				delay = arrowStyle.getPropertyValue("transition-delay").slice(0, -1) * 1000;
			}
			
			setTimeout(() => {
				container.querySelectorAll(".image-thumb").forEach(i => i.addEventListener("mouseenter", this.imageListener));
			}, delay);
		}
		
		this.imageListener = e => {
			mainContainer.show(this.album, this.images[e.target.getAttribute("data-image-id")]);
		}
	}
	
	function MainContainer(main, guide, content) {
		//Manages main container events
		this.main = main;
		this.guide = guide;
		this.content = content;
		
		this.album;
		this.image;
		this.comments;
		
		this.reset = () => {		
			this.album = null;
			this.image = null;
			this.comments = null;
			
			this.showGuide();
		}
		
		this.show = (album, image, forced = false) => {
			//Default forced is false so the MainContainer will not refresh when hovering the same image
			if(!forced && image == this.image)
				return;
			
			this.album = album;
			this.image = image;
			
			if(this.image) {
				//AJAX GET /GetCommentsList?image=image.id
				makeCall("GET", "GetCommentsList?image=" + image.id, null, req => {
					if(req.readyState == 4) {
						var message = req.responseText;
						if(req.status == 200) {
							this.update(JSON.parse(message));
						} else {
							sidebarManager.showAlert(albumSidebar.slider, message, "error-msg");
						}
					}
				});			
			} else {
				this.update();
			}
		}
		
		this.update = comments => {	
			this.comments = comments;
			
			disable(this.main);
			
			var contentStyle = window.getComputedStyle(this.content);
			var delay = contentStyle.getPropertyValue("transition-duration").slice(0, -1) * 1000;
			
			setTimeout(() => {
				if(this.image)
					this.showContent();
				else
					this.showGuide();
				
				enable(this.main);
			}, delay);
		}
		
		this.showGuide = () => {
			disable(this.content);
			enable(this.guide);
			
			var title, parag = [];
			if(this.album) {
				title = "This is " + this.album.title;
				parag.push("Hover an image with your mouse to see it in larger size and discover its details.");
				parag.push("You can also read comments from other users and leave your own!");
			} else {
				title = "Welcome to My Gallery";
				parag.push("Please select an album from the menu on the left to see the images it contains.");
				parag.push("You can drag the album names to create your personal order!");
			}
			
			this.guide.querySelector("h1").textContent = title;
			this.guide.querySelectorAll("p").forEach((p, i) => p.textContent = parag[i]);		
		}
		
		this.showContent = () => {	
			disable(this.guide);
			enable(this.content);
			
			this.content.querySelector(".main-image").setAttribute("src", "img/" + this.image.src);			
			this.showDetails();
			this.showComments();			
			this.content.querySelector(".form-container .button").addEventListener("click", this.sendComment);			
		}
		
		this.showDetails = () => {
			var container = this.content.querySelector(".details-container");
			
			container.querySelector("h2").textContent = this.image.title;
			
			container.querySelector(".details .date .title").textContent = formatDate(this.image.date);			
			container.querySelector(".details .album .title").textContent = this.album.title;
			
			container.querySelector("p").textContent = this.image.description;
		}
		
		this.showComments = () => {
			var container = this.content.querySelector(".comments-container");
			var title = container.querySelector("h2");
			container.innerHTML = "";
			container.append(title);
			
			if(this.comments && this.comments.length > 0) {
				this.comments.forEach(c => {
					container.appendChild(this.buildComment(c));
				});
				enable(container);
			} else {
				disable(container);
			}
		}
		
		this.buildComment = comment => {
			var user = get("h3", "", comment.username);			
			var text = get("p", "", comment.text);
			
			return append(get("div", "comment"), user, text);
		}
		
		this.sendComment = e => {
			var form = e.target.closest("form");
			
			if(!form.checkValidity()) {
				form.reportValidity();
				return;
			}
			
			//AJAX POST /AddComment?image=this.image.id
			makeCall("POST", "AddComment?image=" + this.image.id, form, req => {
				if(req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;
					if(req.status == 200) {
						this.content.querySelector(".form-container .button").blur();
						this.show(this.album, this.image, true);
					} else {
						form.appendChild(get("h4", "error-msg", message));
					}
				}
			});
		}
	}
	
	function PageOrchestrator() {
		this.start = () => {			
			header = new Header(
				document.getElementById("username"),
				sessionStorage.getItem("username")
			);
			
			sidebarManager = new SidebarManager(
				document.querySelectorAll(".sidebar"),
				document.getElementById("main")				
			);
			sidebarManager.registerEvents();
			
			gallerySidebar = new GallerySidebar(
				document.getElementById("gallery"),
				document.getElementById("albums")
			);
			gallerySidebar.registerEvents();
			
			albumSidebar = new AlbumSidebar(
				document.getElementById("album"),
				document.getElementById("album-info"),
				document.getElementById("slider")
			);
			
			mainContainer = new MainContainer(
				document.getElementById("main"),
				document.getElementById("guide"),
				document.getElementById("content")
			);
		}
		
		this.show = () => {
			gallerySidebar.reset();
			albumSidebar.reset();
			mainContainer.reset();

			header.show();
			gallerySidebar.show();
		}
	}
	
	function enable(elem) { elem.classList.remove("disabled"); }
	
	function disable(elem) { elem.classList.add("disabled"); }
	
	function get(tag, classList = "", text = "", attributes = {}) {
		var elem = document.createElement(tag);						
		classList.split(" ").filter(c => c != "").forEach(c => elem.classList.add(c));			
		elem.textContent = text;			
		Object.entries(attributes).forEach(([attr, value]) => elem.setAttribute(attr, value));			
		return elem;
	}

	function append(elem, ...args) {
		args.forEach(a => elem.appendChild(a));
		return elem;
	}
	
	function formatDate(text) {
		return text.split(" ").reverse().join("-");
	}
})();