/*
 * SimpleModal Basic Modal Dialog
 * http://www.ericmmartin.com/projects/simplemodal/
 * http://code.google.com/p/simplemodal/
 *
 * Copyright (c) 2010 Eric Martin - http://ericmmartin.com
 *
 * Licensed under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 *
 * Revision: $Id: basic.js 243 2010-03-15 14:23:14Z emartin24 $
 *
 */

jQuery(function ($) {
	$('.basic').click(function (e) {
		$('#basic-modal-content').modal();
		$('#simplemodal-container').height(150);
		$('#simplemodal-container').width(420);
		  return false;
	});
	$('.edit-basic').click(function (e) {
		$('#edit-modal-content').modal();
		$('#simplemodal-container').height(180);
		  return false;
	});
	
});