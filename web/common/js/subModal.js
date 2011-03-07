/**
 * SUBMODAL v1.6
 * Used for displaying DHTML only popups instead of using buggy modal windows.
 *
 * By Subimage LLC
 * http://www.subimage.com
 *
 * Contributions by:
 * 	Eric Angel - tab index code
 * 	Scott - hiding/showing selects for IE users
 *	Todd Huss - inserting modal dynamically and anchor classes
 *
 * Up to date code can be found at http://submodal.googlecode.com
 */








/**
 * COMMON DHTML FUNCTIONS
 * These are handy functions I use all the time.
 *
 * By Seth Banks (webmaster at subimage dot com)
 * http://www.subimage.com/
 *
 * Up to date code can be found at http://www.subimage.com/dhtml/
 *
 * This code is free for you to use anywhere, just keep this comment block.
 */

/**
 * X-browser event handler attachment and detachment
 * TH: Switched first true to false per http://www.onlinetools.org/articles/unobtrusivejavascript/chapter4.html
 *
 * @argument obj - the object to attach event to
 * @argument evType - name of the event - DONT ADD "on", pass only "mouseover", etc
 * @argument fn - function to call
 */
function smAddEvent(obj, evType, fn){
 if (obj.addEventListener){
    obj.addEventListener(evType, fn, false);
    return true;
 } else if (obj.attachEvent){
    var r = obj.attachEvent("on"+evType, fn);
    return r;
 } else {
    return false;
 }
}
function smRemoveEvent(obj, evType, fn, useCapture){
  if (obj.removeEventListener){
    obj.removeEventListener(evType, fn, useCapture);
    return true;
  } else if (obj.detachEvent){
    var r = obj.detachEvent("on"+evType, fn);
    return r;
  } else {
    alert("Handler could not be removed");
  }
}

/**
 * Code below taken from - http://www.evolt.org/article/document_body_doctype_switching_and_more/17/30655/
 *
 * Modified 4/22/04 to work with Opera/Moz (by webmaster at subimage dot com)
 *
 * Gets the full width/height because it's different for most browsers.
 */
function svGetViewportHeight() {
	if (window.innerHeight!=window.undefined) return window.innerHeight;
	if (document.compatMode=='CSS1Compat') return document.documentElement.clientHeight;
	if (document.body) return document.body.clientHeight; 

	return window.undefined; 
}
function smGetViewportWidth() {
	var offset = 17;
	var width = null;
	if (window.innerWidth!=window.undefined) return window.innerWidth; 
	if (document.compatMode=='CSS1Compat') return document.documentElement.clientWidth; 
	if (document.body) return document.body.clientWidth; 
}

/**
 * Gets the real scroll top
 */
function smGetScrollTop() {
	if (self.pageYOffset) // all except Explorer
	{
		return self.pageYOffset;
	}
	else if (document.documentElement && document.documentElement.scrollTop)
		// Explorer 6 Strict
	{
		return document.documentElement.scrollTop;
	}
	else if (document.body) // all other Explorers
	{
		return document.body.scrollTop;
	}
}
function smGetScrollLeft() {
	if (self.pageXOffset) // all except Explorer
	{
		return self.pageXOffset;
	}
	else if (document.documentElement && document.documentElement.scrollLeft)
		// Explorer 6 Strict
	{
		return document.documentElement.scrollLeft;
	}
	else if (document.body) // all other Explorers
	{
		return document.body.scrollLeft;
	}
}









// BEgSMiN SUBMODAL STUFF

// Popup code
var gSMPopupMask = null;
var gSMPopupContainer = null;
var gSMPopFrame = null;
var gSMReturnFunc;
var gSMReturnFuncWindow;
var gSMPopupIsShown = false;
var gSMDefaultPage = svWebContext+"/common/loading.html";
var gSMHideSelects = false;
var gSMReturnVal = null;

var gSMTabIndexes = new Array();
// Pre-defined list of tags we want to disable/enable tabbing into
var gSMTabbableTags = new Array("A","BUTTON","TEXTAREA","INPUT","IFRAME");	

// If using Mozilla or Firefox, use Tab-key trap.
if (!document.all) {
	document.onkeypress = smKeyDownHandler;
}

/**
 * Initializes popup code on load.	
 */
function smInitPopUp() {
	// Add the HTML to the body
	theBody = document.getElementsByTagName('BODY')[0];
	popmask = document.createElement('div');
	popmask.id = 'smPopupMask';
	popcont = document.createElement('div');
	popcont.id = 'smPopupContainer';
	popcont.innerHTML = '' +
		'<div id="smPopupInner">' +
			'<div id="smPopupTitleBar">' +
				'<div id="smPopupTitle"></div>' +
				'<div id="smPopupControls">' +
					'<img src="close.gSMif" onclick="smHidePopWin(false);" id="smPopCloseBox" />' +
				'</div>' +
			'</div>' +
			'<iframe src="'+ gSMDefaultPage +'" style="width:100%;height:100%;background-color:transparent;" scrolling="auto" frameborder="0" allowtransparency="true" id="smPopupFrame" name="smPopupFrame" width="100%" height="100%"></iframe>' +
		'</div>';
	theBody.appendChild(popmask);
	theBody.appendChild(popcont);
	
	gSMPopupMask = document.getElementById("smPopupMask");
	gSMPopupContainer = document.getElementById("smPopupContainer");
	gSMPopFrame = document.getElementById("smPopupFrame");	
	
	// check to see if this is IE version 6 or lower. hide select boxes if so
	// maybe they'll fix this in version 7?
	var brsVersion = parseInt(window.navigator.appVersion.charAt(0), 10);
	if (brsVersion <= 6 && window.navigator.userAgent.indexOf("MSIE") > -1) {
		gSMHideSelects = true;
	}
	
	// Add onclick handlers to 'a' elements of class submodal or submodal-width-height
	var elms = document.getElementsByTagName('a');
	for (i = 0; i < elms.length; i++) {
		if (elms[i].className.indexOf("submodal") == 0) { 
			// var onclick = 'function (){smShowPopWin(\''+elms[i].href+'\','+width+', '+height+', null);return false;};';
			// elms[i].onclick = eval(onclick);
			elms[i].onclick = function(){
				// default width and height
				var width = 400;
				var height = 200;
				// Parse out optional width and height from className
				params = this.className.split('-');
				if (params.length == 3) {
					width = parseInt(params[1]);
					height = parseInt(params[2]);
				}
				smShowPopWin(this.href,width,height,null); return false;
			}
		}
	}
}
smAddEvent(window, "load", smInitPopUp);

 /**
	* @argument width - int in pixels
	* @argument height - int in pixels
	* @argument url - url to display
	* @argument returnFunc - function to call when returning true from the window.
	* @argument showCloseBox - show the close box - default true
	*/
function smShowPopWin(url, width, height, returnFunc, showCloseBox, returnFuncWindow) {
	// show or hide the window close widget
	if (showCloseBox == null || showCloseBox == true) {
		document.getElementById("smPopCloseBox").style.display = "block";
	} else {
		document.getElementById("smPopCloseBox").style.display = "none";
	}
	gSMPopupIsShown = true;
	smDisableTabIndexes();
	gSMPopupMask.style.display = "block";
	gSMPopupContainer.style.display = "block";
	// calculate where to place the window on screen
	smCenterPopWin(width, height);
	
	var titleBarHeight = parseInt(document.getElementById("smPopupTitleBar").offsetHeight, 10);


	gSMPopupContainer.style.width = width + "px";
	gSMPopupContainer.style.height = (height+titleBarHeight) + "px";
	
	smSetMaskSize();

	// need to set the width of the iframe to the title bar width because of the dropshadow
	// some oddness was occuring and causing the frame to poke outside the border in IE6
	gSMPopFrame.style.width = parseInt(document.getElementById("smPopupTitleBar").offsetWidth, 10) + "px";
	gSMPopFrame.style.height = (height) + "px";
	
	// set the url
	gSMPopFrame.src = url;
	
	gSMReturnFunc = returnFunc;
	gSMReturnFuncWindow = returnFuncWindow;
	// for IE
	if (gSMHideSelects == true) {
		smHideSelectBoxes();
	}
	
	svHide("smPopupTitleBar");
	
	window.setTimeout("smSetPopTitle();", 600);
}

//
var gSMi = 0;
function smCenterPopWin(width, height) {
	if (gSMPopupIsShown == true) {
		if (width == null || isNaN(width)) {
			width = gSMPopupContainer.offsetWidth;
		}
		if (height == null) {
			height = gSMPopupContainer.offsetHeight;
		}
		
		//var theBody = document.documentElement;
		var theBody = document.getElementsByTagName("BODY")[0];
		//theBody.style.overflow = "hidden";
		var scTop = parseInt(smGetScrollTop(),10);
		var scLeft = parseInt(theBody.scrollLeft,10);
	
		smSetMaskSize();
		
		//window.status = gSMPopupMask.style.top + " " + gSMPopupMask.style.left + " " + gSMi++;
		
		var titleBarHeight = parseInt(document.getElementById("smPopupTitleBar").offsetHeight, 10);
		
		var fullHeight = svGetViewportHeight();
		var fullWidth = smGetViewportWidth();
		
		gSMPopupContainer.style.top = (scTop + ((fullHeight - (height+titleBarHeight)) / 2)) + "px";
		gSMPopupContainer.style.left =  (scLeft + ((fullWidth - width) / 2)) + "px";
		//alert(fullWidth + " " + width + " " + gSMPopupContainer.style.left);
	}
}
smAddEvent(window, "resize", smCenterPopWin);
smAddEvent(window, "scroll", smCenterPopWin);
window.onscroll = smCenterPopWin;


/**
 * Sets the size of the popup mask.
 *
 */
function smSetMaskSize() {
	var theBody = document.getElementsByTagName("BODY")[0];
			
	var fullHeight = svGetViewportHeight();
	var fullWidth = smGetViewportWidth();
	
	// Determine what's bigger, scrollHeight or fullHeight / width
	if (fullHeight > theBody.scrollHeight) {
		popHeight = fullHeight;
	} else {
		popHeight = theBody.scrollHeight;
	}
	
	if (fullWidth > theBody.scrollWidth) {
		popWidth = fullWidth;
	} else {
		popWidth = theBody.scrollWidth;
	}
	
	gSMPopupMask.style.height = popHeight + "px";
	gSMPopupMask.style.width = popWidth + "px";
}

/**
 * @argument callReturnFunc - bool - determines if we call the return function specified
 * @argument returnVal - anything - return value 
 */
function smHidePopWin(callReturnFunc, returnVal) {
	gSMPopupIsShown = false;
	var theBody = document.getElementsByTagName("BODY")[0];
	theBody.style.overflow = "";
	smRestoreTabIndexes();
	if (gSMPopupMask == null) {
		return;
	}
	gSMPopupMask.style.display = "none";
	gSMPopupContainer.style.display = "none";
	if (callReturnFunc == true && gSMReturnFunc != null) {
		// Set the return code to run in a timeout.
		// Was having issues using with an Ajax.Request();
		gSMReturnVal = returnVal;
		if(gSMReturnFuncWindow == null) {
			//gSMReturnVal = window.frames["smPopupFrame"].returnVal;
			window.setTimeout('gSMReturnFunc(gSMReturnVal);', 1);
		} else {
			//gSMReturnVal = window.frames["smPopupFrame"].returnVal;
			gSMReturnFuncWindow.setTimeout('top.gSMReturnFunc(top.gSMReturnVal);', 1);
		}
	}
	gSMPopFrame.src = gSMDefaultPage;
	// display all select boxes
	if (gSMHideSelects == true) {
		smDisplaySelectBoxes();
	}
	svShow("smPopupTitleBar");
}

/**
 * Sets the popup title based on the title of the html document it contains.
 * Uses a timeout to keep checking until the title is valid.
 */
function smSetPopTitle() {
	return;
	if (window.frames["smPopupFrame"].document.title == null) {
		window.setTimeout("smSetPopTitle();", 10);
	} else {
		document.getElementById("smPopupTitle").innerHTML = window.frames["smPopupFrame"].document.title;
	}
}

// Tab key trap. iff popup is shown and key was [TAB], suppress it.
// @argument e - event - keyboard event that caused this function to be called.
function smKeyDownHandler(e) {
    if (gSMPopupIsShown && e.keyCode == 9)  return false;
}

// For IE.  Go through predefined tags and disable tabbing into them.
function smDisableTabIndexes() {
	if (document.all) {
		var i = 0;
		for (var j = 0; j < gSMTabbableTags.length; j++) {
			var tagElements = document.getElementsByTagName(gSMTabbableTags[j]);
			for (var k = 0 ; k < tagElements.length; k++) {
				gSMTabIndexes[i] = tagElements[k].tabIndex;
				tagElements[k].tabIndex="-1";
				i++;
			}
		}
	}
}

// For IE. Restore tab-indexes.
function smRestoreTabIndexes() {
	if (document.all) {
		var i = 0;
		for (var j = 0; j < gSMTabbableTags.length; j++) {
			var tagElements = document.getElementsByTagName(gSMTabbableTags[j]);
			for (var k = 0 ; k < tagElements.length; k++) {
				tagElements[k].tabIndex = gSMTabIndexes[i];
				tagElements[k].tabEnabled = true;
				i++;
			}
		}
	}
}


/**
 * Hides all drop down form select boxes on the screen so they do not appear above the mask layer.
 * IE has a problem with wanted select form tags to always be the topmost z-index or layer
 *
 * Thanks for the code Scott!
 */
function smHideSelectBoxes() {
  var x = document.getElementsByTagName("SELECT");
  for (i=0;x && i < x.length; i++) {
	  x[i].style.visibility = "hidden";
  }
  
  var x = document.getElementsByTagName("BUTTON");
  for (i=0;x && i < x.length; i++) {
	  x[i].style.visibility = "hidden";
  }
  
  x = document.getElementsByTagName("IFRAME");
  for (i=0;x && i < x.length; i++) {
	  if(x[i].contentWindow.hideSelectBoxes)
		  x[i].contentWindow.smHideSelectBoxes();
  }
}

/**
 * Makes all drop down form select boxes on the screen visible so they do not 
 * reappear after the dialog is closed.
 * 
 * IE has a problem with wanting select form tags to always be the 
 * topmost z-index or layer.
 */
function smDisplaySelectBoxes() {
  var x = document.getElementsByTagName("SELECT");
  for (i=0;x && i < x.length; i++){
	  x[i].style.visibility = "visible";
  }
  
  var x = document.getElementsByTagName("BUTTON");
  for (i=0;x && i < x.length; i++) {
	  x[i].style.visibility = "visible";
  }
  
  x = document.getElementsByTagName("IFRAME");
  for (i=0;x && i < x.length; i++) {
	  if(x[i].contentWindow.displaySelectBoxes)
		  x[i].contentWindow.smDisplaySelectBoxes();
  }
}