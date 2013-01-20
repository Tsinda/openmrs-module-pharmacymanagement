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
	$('.edit').click(function (e) {
		$('#edit-dialog-content').dialog();
		$('#createditdialog-container').css({'width':'630px', 'height':'352px'});
		  return false;
	});
	
	$('.stop').click(function (e) {
		var index = this.id;
		var suffix = index.substring(index.indexOf("_") + 1);
		var varReason = document.getElementById("stopReasonId");
		var reason = $dm('#discontinuedReason_'+suffix).text();
		for ( var i = 0; i < varReason.options.length; i++) {
			if (varReason.options[i].value == reason) {
				varReason.selectedIndex = i;
				break;
			}
		}
		
		$('#stop-modal-content').modal();
		  return false;
	});
	
	$('#create').click(function(e) {
		$dm('#edit-dialog-content').dialog();
		$dm("#createditdialog-container").css({'width':'660px', 'height':'372px'});
		return false;
	});
	
	$('.popit').click(function(e) {
		$('#rpt-dialog-content').dialog();		
		  return false;
	});
	
	$("#addingi").click(function(e) {
		$("#stop-modal-content").dialog();
	});
	/**
	$("#print_ordonance").click(function(e) {
		$("#ordonance-modal-content").dialog();
		$("#ordonance-modal-content").css({'width':'100%', 'height':'405px'});
		$("#createditdialog-container").css({'width':'650px', 'height':'450px'});
	});
	**/
	$("who").click(function() {
		$("#stop-modal-content").hide();
	});
	
	$(".delete").click(function(e) {
		var deleteBtnId = this.id;
		var orderId = deleteBtnId.substring(deleteBtnId.indexOf("_") + 1);
		$dm('#orderToDelId').val(orderId);
		
		$("#delete-modal-content").dialog();
		$("#delete-modal-content").css({'width':'100%', 'height':'100%'});
		$("#createditdialog-container").css({'height':'50px', 'width':'180px', 'left':'840.5px', 'top':'259px'});
	});
});