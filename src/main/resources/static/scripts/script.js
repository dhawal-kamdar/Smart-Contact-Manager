console.log("Smart Contact Manager - Javascript Working");

// Sidebar
const toggleSidebar = () => {
	if($(".sidebar").is(':visible')) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
}

// View Contact - Search
const search = () => {	
	let query = $("#search-input").val();
	
	if(query == '') {
		$(".search-result").hide();
	} else {
		console.log("Searching: " + query);
		
		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			// Insert data
			let htmlCode = `<div class='list-group'>`;
			data.forEach((contact) => {
				htmlCode += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'>${contact.name}</a>`;
			});
			htmlCode += `</div>`;
			
			$(".search-result").html(htmlCode);
			$(".search-result").show();
		});
	}
}