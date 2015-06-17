/* 
 * Copyright (c) 2012 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 * 
 * This file contains the definition of the jQuery UI element
 * - Drag and drop and so on
 * - Dialogs
 * - Undo / redo
 * - Check the rules when adding a candidate
 * 
 */

function jquery_generate( options ){
	
	options = options || {};
	
	var listsAreSelectable = options.listsAreSelectable || false;
	
	/* Array in which the states are saved */
	var savedStatesList = [];
	var savedStatesCandidates = [];
	var savedStateListTitle = [];
	var savedStatesMapKeys = [];
	var savedStatesMapValues = [];
	var index=0;

	var sortableIn = 0;            //1 if a element is in the created list (used to delete when dragged outside)
	var sortableStopping = false;
	
	// Holds the chosen choices (List and Candidats)
	// key -> choiceId, value -> count
	var chosenChoices = new Map();
		
	////////////////////////////////////////////////////////////////////////////
	// Definition of drag and drop functionalities//
	////////////////////////////////////////////////////////////////////////////

	/* Declare the initial candidates as draggable */
	$(".list > li").draggable({
		helper : "clone",
		connectToSortable : "#created-list",
		revert: "invalid",
		scroll:false,
		start: function(event, ui) {
			//apply a css class to avoid deformation
			ui.helper.addClass("candidateBeingDragged");
		},
		stop: function(event, ui){
			$('#candidates').unblock();
		}
	});
	
	/* Do not allow to drag and drop the placholder itmes */
	$('.placeholder-item').mousedown(function(event){event.stopPropagation();});
		
	/* Declare the created list as sortable */
	$("#created-list").sortable({
		placeholder: 'placeholder ui-state-highlight',
		
		//start: function(event, ui) {},
		//sort: function(event, ui) {},
		receive : function(e, ui) {
			sortableIn = 2;	
			$( this ).find( ".placeholder-item" ).remove();
			addChoice(ui.item);
		},
			
		over : function(e, ui) {
			sortableIn = 1;
			$('#candidates').unblock({fadeOut:  200});
		},
			
		out : function(e, ui) {
			if(sortableIn==1 && !sortableStopping){
				//block the list with an overlay show a trash image
				$('#candidates').block({ 
					message: '',//'<img id="trash" src="img/trash.png" width="50" height="50" style="margin-top:3px" />', 
					fadeIn:  200,
					overlayCSS: {opacity: '0.4'}
				});
			}
			sortableIn = 0;
		},
			
		beforeStop : function(e, ui) {
			sortableStopping = true;
			if (sortableIn == 0) {
				ui.item.remove();
				removeChoice(ui.item);
			}
		},
			
		stop: function(event, ui) {
			sortableStopping = false;
			$('#candidates').unblock({fadeOut:  200});
			
			// Check max rules
			if ( !checkMaxRules(ui.item, true) ){
				return;
			}
			// change appearance
			changeAppearanceOfChosenCandidate(ui.item);
			//save state after modification
			saveState();
		}
		
	}).droppable({
		//accept candidates
		accept: "#lists_content > div > ul > li"
	});
	
	if ( listsAreSelectable ) {
		/* Declare the initial lists (the tabs) as draggable */
		$("#lists > li a").css({'cursor':'move'});
		$("#lists > li").draggable({
			helper : "clone",
			revert: "invalid",
			scroll: false,
			appendTo:$("#candidates"),
			start: function(event, ui) {
				//apply a css class to avoid deformation
				ui.helper.removeClass("ui-corner-top");
				ui.helper.addClass("partyBeingDragged");
			}
		});

		/* Declare the party field as droppable */
		$("#result").droppable({
			activeClass: "ui-state-highlight",
			//accept: list tabs
			accept: "#lists > li",
			drop: function( event, ui ) {
				showDialogCopyList(ui.draggable);
				$("#selected-list li").removeClass("placeholder-item");
			}

		});	
	} else {
		$("#selected-list").css({'opacity':0});
	}


	////////////////////////////////////////////////////////////////////////////
	//Definition of adding choices by clicking on the green plus//
	////////////////////////////////////////////////////////////////////////////


	//Add click listener on the green plus of candidates
	$(".drag_candidate").click(function(){
		//clone the "dragged" li
		var $li = $(this).parent("li").clone(false);
		//remove place holder
		$("#created-list").find(".placeholder-item").remove();
		//append li
		changeAppearanceOfChosenCandidate($li);
		$("#created-list").append($li);
		addChoice($li);
		//control if li has right to be there
		if( !checkMaxRules($li, true) ) {
			return;
		}

		//save state after modification
		saveState();
	});

	//Add a click listener on the green plus of lists
	$(".drag_list").click(function(){
		var $li = $(this).parent("li").clone(false);
		showDialogCopyList($li);
		$("#selected-list li").removeClass("placeholder-item");
	});


	////////////////////////////////////////////////////////////////////////////
	// Definition of the dialogs//
	////////////////////////////////////////////////////////////////////////////

	//dialogs and others
	$( "#dialog-copy-list" ).dialog({
		autoOpen: false
	});

	var dialog_ok_buttons = {};
	dialog_ok_buttons[msg.ok] = function(){
		$( this ).dialog( "close" );
	}

	$( "#dialog-too-many-repetitions" ).dialog({
		autoOpen: false,
		modal:true,
		buttons: dialog_ok_buttons
	});

	$( "#dialog-too-many-candidates" ).dialog({
		autoOpen: false,
		modal:true,
		buttons: dialog_ok_buttons
	});

	$( "#list-copied-with-modification" ).dialog({
		autoOpen: false,
		modal:true,
		width:600,
		buttons: dialog_ok_buttons
	});

	var dialog_video_buttons = {};
	dialog_video_buttons[msg.skip] = function(){
		$( this ).dialog( "close" );
	}

	$( "#dialog-video" ).dialog({
		resizable: false,
		draggable:false,
		autoOpen: false,
		height:600,
		width:900,
		modal: true,
		buttons: dialog_video_buttons
	});


	////////////////////////////////////////////////////////////////////////////
	//Styling //
	////////////////////////////////////////////////////////////////////////////


	//TODO Handle noList -> remove some parts of the GUI
	$( "div.tabs" ).tabs();

	$("#result").addClass("ui-widget ui-widget-content ui-corner-all ");

	$('.tooltip_candidate').each(function(){
		declareTooltip($(this));
	});


	////////////////////////////////////////////////////////////////////////////
	//Undo, Redo, Reset //
	////////////////////////////////////////////////////////////////////////////

	/* Add click listener on reset image */
	$("#reset").click(function() {
		$("#selected-list > li").addClass("placeholder-item ui-state-default").html(msg.list);
		$("#list-title").text("");
		$("#created-list").html('<li class="placeholder-item ui-state-default">'+msg.candidate+'</li>');
		$('.placeholder-item').mousedown(function(event){event.stopPropagation();});
		chosenChoices.removeAll();
		saveState();
	});

	/* Add click listener on undo image */
	$("#undo").click(function() { 
		if(index>1){
			//state at index-1 is actual state
			index-=2;
			$("#selected-list > li").html(savedStatesList[index]);
			$("#list-title").html(savedStateListTitle[index]);
			$("#created-list").html(savedStatesCandidates[index]);
			chosenChoices.removeAll();
			//recreate chosenChoices from saved state
			for(var i=0; i<savedStatesMapKeys[index].length; i++){
				chosenChoices.put(savedStatesMapKeys[index][i],savedStatesMapValues[index][i]);
			}
			index++;
		}
		//recreate the click action on the cross image
		$.each($("#created-list").children(), function(){
			declareRemoveListener($(this).children("img.remove_candidate"));
			declareTooltip($(this).children("img.tooltip_candidate"));
		});
	});

	/* Add click listener on redo image */
	$("#redo").click(function() {
		if(index<savedStatesList.length){
			$("#selected-list > li").html(savedStatesList[index]);
			$("#list-title").html(savedStateListTitle[index]);
			$("#created-list").html(savedStatesCandidates[index]);
			chosenChoices.removeAll();
			//recreate chosenChoices from saved state
			for(var i=0; i<savedStatesMapKeys[index].length; i++){
				chosenChoices.put(savedStatesMapKeys[index][i],savedStatesMapValues[index][i]);
			}
			index++;
		}
		//recreate the click action on the cross image
		$.each($("#created-list").children(), function(){
			declareRemoveListener($(this).children("img.remove_candidate"));
			declareTooltip($(this).children("img.tooltip_candidate"));
		});
	});


	//save initial state
	saveState();

	
	
	////////////////////////////////////////////////////////////////////////////
	//Helper functions//
	////////////////////////////////////////////////////////////////////////////
	
			
			
	/*
	 * Shows the copy list dialog. The voter is asked whether all candidates
	 * of the list should be copied as well, or only the list number.
	 * @param $liItem - Lists jquery li object (containing an anchor with the list id as href).
	 */
	function showDialogCopyList( $liItem ) {
		
		var dialog_add_list_buttons = {};
		dialog_add_list_buttons[msg.copyAllCandidates] = function(){
			
			//copy list nr
			var lilist = $("#selected-list li");
			lilist.html($liItem.html());
			var text = lilist.children("a").html();
			lilist.children("a").remove();
			lilist.children("img").remove();
			lilist.html(text+lilist.html());
			chosenChoices.removeAll();
			addChoice(lilist);

			//Get the dragged list
			var list = $liItem.children("a").attr('href');
			// IE7 Bug!! attr('href') returns the full url, not only the real value!
			var n = list.indexOf("#");
			list = list.substr(n, list.length-n);
			
			var modified = false;
			//Remove all candidate in the actual created list
			$("#created-list").children("li").remove();
			
			//put the title of the list
			$("#list-title").html($(list).children("p").html());
			//Copy candidates from the dragged list
			$(list).children("ul").children("li").clone().appendTo("#created-list");
			$.each($("#created-list").children(), function(k,v){
				var $item = $(this);
				addChoice($item);
				changeAppearanceOfChosenCandidate($item);
				if ( !checkMaxRules($item, false) ) {
					modified = true;
				}
			});

			if(modified){
				$( "#list-copied-with-modification" ).dialog('open');
			}

			$( this ).dialog( "close" );

			//save state after modification
			saveState();
		}
		dialog_add_list_buttons[msg.copyListnumber] = function(){
			//copy list nr
			var lilist = $("#selected-list li");
			var oldElement = lilist.clone();
			lilist.html($liItem.html());
			var text = lilist.children("a").html();
			lilist.children("a").remove();
			lilist.children("img").remove();
			lilist.html(text+lilist.html());
			replaceChoice(oldElement, lilist, 1);

			//Get the dragged list
			var list = $liItem.children("a").attr('href');
			// IE7 Bug!! attr('href') returns the full url, not only the real value!
			var n = list.indexOf("#");
			list = list.substr(n, list.length-n);
			
			//put the title of the list
			$("#list-title").html($(list).children("p").html());
			$( this ).dialog( "close" );

			//save state after modification
			saveState();
		}

		$( "#dialog-copy-list" ).dialog({
			resizable: false,
			draggable:false,
			autoOpen: false,
			width:600,
			modal: true,
			buttons: dialog_add_list_buttons
		}).dialog('open');
		
	}
	
	/*
	 * Checks the max rules for candidates. If a rule is violated then the item
	 * is removed from the GUI and from the chosen candidates list.
	 * @param $item - The just added candidate as jquery li object
	 * @param showDialogs - True if violations should be displyed to the user by dialog. 
	 */
	function checkMaxRules( $item, showDialogs ) {
		showDialogs = showDialogs || true;
		if(uvUtilRuleControl.checkForAllRules(chosenChoices, forAllRules)){
			$item.remove();
			removeChoice($item);
			if ( showDialogs ) {
				$("#dialog-too-many-repetitions").dialog('open');
			}
			return false;
		}
		if(uvUtilRuleControl.checkSumRules(chosenChoices, sumRules)){
			$item.remove();
			removeChoice($item);
			if ( showDialogs ) {
				$("#dialog-too-many-candidates").dialog('open');
			}
			return false;
		}
		return true;
	}

	/* 
	 * Save the state of the results 
	 */
	function saveState(){
		//save state
		savedStatesList.splice(index,savedStatesList.length-index,$("#selected-list > li").html());
		savedStateListTitle.splice(index,savedStatesList.length-index,$("#list-title").html());
		savedStatesCandidates.splice(index,savedStatesList.length-index,$("#created-list").html());
		index++;

		var keys = chosenChoices.listKeys()
		var values = chosenChoices.listValues();
		savedStatesMapKeys.splice(index,savedStatesList.length-index,keys);
		savedStatesMapValues.splice(index,savedStatesList.length-index,values);
	}
	
	/*
	 * Changes the appearance of a chosen candidate.
	 * $item candidates li as jquery object
	 */
	function changeAppearanceOfChosenCandidate( $item ) {
		//change the image
		changeImage($item.children("img.drag_candidate"));
		//set a new listener to img
		declareRemoveListener($item.children("img.remove_candidate"));
		//redeclare the info tooltip 
		declareTooltip($item.children("img.tooltip_candidate"));
	}

	/*
	 * Create the click listener on the red cross
	 * element: img jquery object
	 */
	function declareRemoveListener(element){
		element.click(function(){
			element.parent().remove();
			removeChoice(element.parent());
			saveState();
		});
	}

	/*
	 * Change the green plus in a red cross
	 * img: img jquery object
	 */
	function changeImage(img){
		var text = msg.remove;
		img.attr("src", "img/cross.png").attr("class","remove_candidate icon").attr("alt",text).attr("title",text).unbind();
	}

	/*
	 * Create the tooltip on the given element
	 * element: img jquery object
	 */
	function declareTooltip(element){
		element.qtip({
			content: element.attr('tooltip'),
			position: {
				corner: {
					tooltip: "bottomLeft",
					target: "topMiddle"
				}
			},
			style: {
				border: {
					width: 1,
					radius: 0
				},
				width: {
					min: 100,
					max: 300
				},
				padding: 7, 
				textAlign: 'left',
				tip: true, 
				name: 'light',
				'font-size': 12 
			}
		});
	}

	/*
	 * Update the chosenChoices representing voter's result
	 * Replace oldElement by newElement, putting value = occurences for newElement
	 */
	function replaceChoice(oldElement, newElement, occurences){
		var oldId = oldElement.children("input").val();
		var newId = newElement.children("input").val();
		chosenChoices.remove(oldId);
		chosenChoices.put(newId, occurences);
	}

	/*
	 * Update the chosenChoices representing voter's result
	 * Add 1 to value if element already exists in chosenChoices
	 * Add the element with value 1 otherwise
	 */
	function addChoice(element){
		var id = element.children("input").val()

		var occurences = chosenChoices.get(id.toString());
		if(occurences === undefined){
			occurences = 1;
		}
		else{
			occurences++;
		}
		chosenChoices.put(id, occurences);
	}

	/*
	 * Update the chosenChoices representing voter's result
	 * Remove 1 to value of element in chosenChoices
	 */
	function removeChoice(element){
		var id = element.children("input").val()

		var occurences = chosenChoices.get(id.toString());
		if(occurences === undefined){
			return;
		}
		else if(occurences === 1){
			chosenChoices.remove(id);
		}
		else{
			occurences--;
			chosenChoices.put(id, occurences);
		}
	}

}

