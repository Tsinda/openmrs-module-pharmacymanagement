<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="View Drug Store management"
	otherwise="/login.htm"
	redirect="/module/pharmacymanagement/return.form" />

<div>
<div id="outer"><%@ include file="template/leftMenu.jsp"%>
</div>
<div id="middle"><%@ include file="template/localHeader.jsp"%>

<openmrs:htmlInclude
	file="/moduleResources/pharmacymanagement/dataentrystyle.css" /> <openmrs:htmlInclude
	file="/moduleResources/pharmacymanagement/jquery.validate.js" /> <openmrs:htmlInclude
	file="/moduleResources/pharmacymanagement/create_dynamic_field.js" />
<openmrs:htmlInclude
	file="/moduleResources/pharmacymanagement/validator.js" /> <script
	type="text/javascript">
var $ = jQuery.noConflict();

var returnType = null;

var dftLocId = "<c:out value="${dftLoc.locationId}"/>";
var dftLocName = "<c:out value="${dftLoc.name}"/>";

//populating pharmacies into an array
var pharmaNameArray = new Array();
var pharmaIdArray = new Array();
<c:forEach var="pharmacy" items="${pharmacies}">
	pharmaNameArray.push("<c:out value="${pharmacy.name}"/>");
	pharmaIdArray.push("<c:out value="${pharmacy.pharmacyId}"/>");
</c:forEach> 

//populating locations into an array
var locNameArray = new Array();
var locIdArray = new Array();
<c:forEach var="location" items="${locations}">
	locNameArray.push("<c:out value="${location.name}"/>");
	locIdArray.push("<c:out value="${location.locationId}"/>");
</c:forEach>

//populating drugs from the store into an array
var storeDrugArray = new Array();
var storeDrugIdArray = new Array();
<c:forEach var="drug" items="${drugMapStore}">
	storeDrugArray.push("<c:out value="${drug.value}"/>");
	storeDrugIdArray.push("<c:out value="${drug.key}"/>");
</c:forEach> 


//populating drugs from the pharmacy into an array
var pharmaDrugArray = new Array();
var pharmaDrugIdArray = new Array();
<c:forEach var="drug" items="${drugMapPharma}">
	pharmaDrugArray.push("<c:out value="${drug.value}"/>");
	pharmaDrugIdArray.push("<c:out value="${drug.key}"/>");
</c:forEach> 

//populating all kind of drugs into an array
var drugArray = new Array();
var drugIdArray = new Array();
<c:forEach var="drug" items="${drugs}">
	drugArray.push("<c:out value="${drug.name}"/>");
	drugIdArray.push("<c:out value="${drug.drugId}"/>");
</c:forEach> 

$(document).ready(function() {
	$('#productCategory').tabs();
	
	$("#fromId").hide();
	$("#cons_fromId").hide();
	$("#toId").hide();
	$("#cons_toId").hide();	
	$("#lotId").attr("disabled", "disabled");	
	$("#cons_lotId").attr("disabled", "disabled");
	
	$('#drugId').change(function() {		
		var drFromId = $('#fromId').val();
		var retVar = $("#drug input[name=retType]:checked").val();
		
		$("#lotId").attr("disabled", "");
		$("#qtyId").val("");
		$("#expDateId").val("");
		
		if(retVar == 'internal') {
            fillOptions('drugId', 'lotId', '');
		} else if(retVar == 'external' && drFromId == dftLocId) {
			fillOptions('drugId', 'lotId', '');
		}
	});	
	
	$('#consumableId').change(function() {		
		var drFromId = $('#cons_fromId').val();
		var retVar = $("#consumable input[name=retType]:checked").val();
		
		$("#cons_lotId").attr("disabled", "");
		$("#cons_qtyId").val("");
		$("#cons_expDateId").val("");
		
		if(retVar == 'internal') {
            fillOptions('consumableId', 'cons_lotId', '');
		} else if(retVar == 'external' && drFromId == dftLocId) {
			fillOptions('consumableId', 'cons_lotId', '');
		}
	});	

	$("#showStockId").click(function() {
		var dateArr = new Array();
		var dateVal = $("#returnDateId").val()
		dateArr = dateVal.split("/");
		var date = dateArr[2]+"-"+dateArr[1]+"-"+dateArr[0];
		var isChecked;
		if($("#showStockId").attr("checked")) {
			isChecked = 1;
		} else {
			isChecked = 0;
		}
		$("#storeReturnId").load("printReturnStock.list?isChecked="+isChecked+"&retDate="+date+" #return_stock", function() {
			var LocOptions = '';
			var pharmaOptions = '';
			$('#example').dataTable();
			$('#example_filter').html("<a href='${pageContext.request.contextPath}/module/pharmacymanagement/printReturnStock.list?isChecked=1&retDate="+date+"'>Print page</a>");

			/** The class was ars and I changed it to ars1 to avoid the event to be triggered. 
			  * In case you want to update just rename the class to the original "ars"
			**/
			$('.ars1').click(function() {		
				var trId = $(this).attr('id');
				var trIdArr = new Array();
				trIdArr = trId.split("_");
				var suffix = trIdArr[1];
				$('#arsId').val(suffix);
				
				//getting values from the table
				  var returnDate = $(this).children("td:nth-child(2)").text();
				  var drug_name = $(this).children("td:nth-child(3)").text();
				  var origin = $(this).children("td:nth-child(5)").text();
				  var pharmacy = $(this).children("td:nth-child(6)").text();
				  var destination = $(this).children("td:nth-child(7)").text();
				  var qty = $(this).children("td:nth-child(8)").text();
				  var lot = $(this).children("td:nth-child(9)").text();
				  var expDate = $(this).children("td:nth-child(10)").text();
				  var obs = $(this).children("td:nth-child(11)").text();
										  
				//populating the fields from table values
					$('input[name=returnDate]').val(returnDate);
					$('input[name=lot]').val(lot);
					$('input[name=expDate]').val(expDate);
					$('#obsId').text(obs);
					
					
					if(qty) {
						$('#qtyId').attr("disabled", "");
						$('#qtyId').val(qty);
					}

					var locOptions = '<option value="">-- select --</option>';
					for(var i in locNameArray) {
						locOptions += '<option value="'+locIdArray[i]+'">'+locNameArray[i]+'</option>';
					}

					if(pharmacy) {
						returnType = 'internal';
							
						$('#intId').attr("checked", "checked");
						
						pharmaOptions += '<option value="">-- select --</option>';
						for(var i in pharmaNameArray) {
							pharmaOptions += '<option value="'+pharmaIdArray[i]+'">'+pharmaNameArray[i]+'</option>';
						}

						$("#fromId").html(pharmaOptions);
						$("#toId").html(locOptions);
						$("#fromId option[text=" + pharmacy +"]").attr("selected","selected");
						$("#toId option[text=" + destination +"]").attr("selected","selected");

						$("#fromId").show();
						$("#toId").show();
					} else {
						returnType = 'external';
						$('#extId').attr("checked", "checked");	
						
						$("#toId").html(locOptions);
						$("#fromId").html(locOptions);
						$("#toId").show();
						$("#fromId").show();
						$("#toId option[text='" + destination + "']").attr("selected","selected");
						$("#fromId option[text='" + origin + "']").attr("selected","selected");						
					}
					//populating drugs
					var fromId = $('#fromId');
					var toId = $('#toId');
					var sb = '<option value="">-- select --</option>';
					/**
					$.getJSON('json.htm?fromId=' + fromId.val() + '&toId=' + toId.val() + '&retType=' + returnType, function(data) {
						for(var i in data) {
							if(drug_name === data[i].drugName) {
								alert("drug_name: "+drug_name+" drugName: "+data[i].drugName);
								sb += '<option value="'+data[i].drugId+'" selected="selected">'+data[i].drugName+'</option>';
							} else {
								sb += '<option value="'+data[i].drugId+'">'+data[i].drugName+'</option>';
							}
									
						}
						$("#drugId").html(sb);
					}); **/

					for(var i in drugArray) {
						if(drug_name === drugArray[i]) {
							sb += '<option value="'+drugIdArray[i]+'" selected="selected">'+drugArray[i]+'</option>';
						} else {
							sb += '<option value="'+drugIdArray[i]+'">'+drugArray[i]+'</option>';
						}
					}
					$("#drugId").html(sb);
						
					if(lot) {
						var drugId = $("#drugId");
						var items = '';
						if(origin != dftLocName) {
							$.getJSON('lot.htm?drugId=' + drugId.val(), function(data) {
								$.each(data, function(key, value) {
									items += '<option value="">-- Select --</option>';
									for(var i in value) {
										var dateArr = value[i].columnIndex_1.split('-');
										var dat = dateArr[2]+'/'+dateArr[1]+'/'+dateArr[0]
										if(lot === value[i].columnIndex_0) {
											items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" selected="selected" >' + value[i].columnIndex_0 + '</option>';
										} else {
											items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" >' + value[i].columnIndex_0 + '</option>'
										}
									}				
								});
							$("select#lotId").html(items);
							$("#lotId").attr("disabled", "");
							});
						} else {
							$("#toReplace").html('<input type="text" name="lot" id="lotId" size="11" />');
							$("#expDateId").attr("disabled", "");
							$("#qtyId").attr("disabled", "");
						}
					}
			});
		});
	});
	
	$("#drug input[name=retType]").change(function() {
		var productType = $('#productCategory').activeTab() == 1 ? '' : 'cons_';
		populateLocations(productType);
	});
	
	$("#consumable input[name=retType]").change(function() {
		var productType = $('#productCategory').activeTab() == 1 ? '' : 'cons_';
		populateLocations(productType);
	});
	
	$("#fromId").change(function() {
		var retVar = $("#drug input[name=retType]:checked").val();
		var productType = $('#productCategory').activeTab() == 1 ? 'drug' : 'consumable';
		var fromId = $('#fromId');
		var toId = $('#toId');
		var sb = '<option value="">-- select --</option>';
		$("#toId").val(dftLocId).attr("selected","selected");

		$('.drug_disp_load').empty().html("Loading...");
		
		$.getJSON('json.htm?fromId=' + fromId.val() + '&toId=' + toId.val() + '&retType=' + retVar + '&productType=' + productType, function(data) {
			if(retVar == 'external') {
				for(var i in drugArray) {
					sb += '<option value="'+drugIdArray[i]+'">'+drugArray[i]+'</option>';
				}
				$("#toReplace").html('<input type="text" name="lot" id="lotId" size="11" value="" />');
				$("#expDateId").val("");
				$('#expDateId').focus(function() {
					showCalendar(this);
				});
				$("#qtyId").val("").attr("disabled", "");
			} else {
				$("#toReplace").html('<select name="lot" id="lotId"><option value="">-- select --</option></select>');
				$("#expDateId").val("").attr("disabled", "disabled");
				$("#qtyId").val("").attr("disabled", "disabled");
				for(var i in data) {
					sb += '<option value="'+data[i].id+'">'+data[i].name+'</option>';
				}
			}
			
			$("#drugId").html(sb);
			$('.drug_disp_load').empty().html("");
		});
	});
	
	$("#cons_fromId").change(function() {
		var retVar = $("#consumable input[name=retType]:checked").val();
		var productType = $('#productCategory').activeTab() == 1 ? 'drug' : 'consumable';
		var fromId = $('#cons_fromId');
		var toId = $('#cons_toId');
		var sb = '<option value="">-- select --</option>';
		
		$("#cons_toId").val(dftLocId).attr("selected","selected");

		$('.cons_disp_load').empty().html("Loading...");

		$.getJSON('json.htm?fromId=' + fromId.val() + '&toId=' + toId.val() + '&retType=' + retVar + '&productType=' + productType, function(data) {
			if(retVar == 'external') {
				for(var i in drugArray) {
					sb += '<option value="'+drugIdArray[i]+'">'+drugArray[i]+'</option>';
				}
				$("#cons_toReplace").html('<input type="text" name="lot" id="cons_lotId" size="11" value="" />');
				$("#cons_expDateId").val("");
				$('#cons_expDateId').focus(function() {
					showCalendar(this);
				});
				$("#cons_qtyId").val("").attr("disabled", "");
			} else {
				$("#cons_toReplace").html('<select name="lot" id="cons_lotId"><option value="">-- select --</option></select>');
				$("#cons_expDateId").val("");
				$("#cons_qtyId").val("").attr("disabled", "disabled");
				for(var i in data) {
					sb += '<option value="'+data[i].id+'">'+data[i].name+'</option>';
				}
			}
			
			$("#consumableId").html(sb);
			$('.cons_disp_load').empty().html("");
		});
	});
	
	$("#toId").change(function() {
		var retVar = $("#drug input[name=retType]:checked").val();
		var productType = $('#productCategory').activeTab() == 1 ? 'drug' : 'consumable';
		var fromId = $('#fromId');
		var toId = $('#toId');
		var sb = '<option value="">-- select --</option>';
		$("#fromId").val(dftLocId).attr("selected","selected");

		$('.drug_disp_load').empty().html("Loading...");
		
		$.getJSON('json.htm?fromId=' + fromId.val() + '&toId=' + toId.val() + '&retType=' + retVar + '&productType=' + productType, function(data) {
			for(var i in data) {
				sb += '<option value="'+data[i].id+'">'+data[i].name+'</option>';
			}
			$("#drugId").html(sb);
			$('.drug_disp_load').empty().html("");
			$("#toReplace").html('<select name="lot" id="lotId"><option value="">-- select --</option></select>');
			$("#expDateId").val("");
			$("#qtyId").val("").attr("disabled", "disabled");
		});
	});	
	
	$("#cons_toId").change(function() {
		var retVar = $("#consumable input[name=retType]:checked").val();
		var productType = $('#productCategory').activeTab() == 1 ? 'drug' : 'consumable';
		var fromId = $('#cons_fromId');
		var toId = $('#cons_toId');
		var sb = '<option value="">-- select --</option>';
		$("#cons_fromId").val(dftLocId).attr("selected","selected");

		$('.cons_disp_load').empty().html("Loading...");

		$.getJSON('json.htm?fromId=' + fromIdype.val() + '&toId=' + toId.val() + '&retType=' + retVar + '&productType=' + productType, function(data) {
						
			for(var i in data) {
				sb += '<option value="'+data[i].id+'">'+data[i].name+'</option>';
			}
			$("#consumableId").html(sb);
			$('.cons_disp_load').empty().html("");
			$('#loadingMsg').empty().html("");
			$("#cons_toReplace").html('<select name="lot" id="cons_lotId"><option value="">-- select --</option></select>');
			$("#cons_expDateId").val("");
			$("#cons_qtyId").val("").attr("disabled", "disabled");
		});
	});
});

function fillOptions(productId, lotId, c) {
	var product= $('#' + productId);
	var lot = $('#' + lotId);
		
	var items = '';
	if(productId == "drugId") {
		$.getJSON('lot.htm?drugId=' + product.val(), function(data) {
			$.each(data, function(key, value) {
				items += '<option value="">-- Select --</option>';
				for(var i in value) {
					var dateArr = value[i].columnIndex_1.split('-');
					var dat = dateArr[2]+'/'+dateArr[1]+'/'+dateArr[0]
					if(c == value[i].columnIndex_0) {
						items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" selected="selected" >' + value[i].columnIndex_0 + '</option>';
					} else {
						items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" >' + value[i].columnIndex_0 + '</option>'
					}
				}				
			});
			
			$('select#' + lotId).html(items);

		    // the input field is not a select
			if($('#' + lotId).is("select")) {
				$('#' + lotId).change(function() {
					var lotVal = lot.val();
					var arr = new Array();
					arr = lotVal.split("_");
					if(arr.length > 1) {
						$("#expDateId").val("");
						$("#expDateId").val(arr[0]);
						$("#expDateId").attr("readonly", "readonly");
						$("#dpId").val(arr[1]);
						$("#qtyId").attr("disabled", "");
					}
				});
			}
		});
	} else {	
		$.getJSON('lot.htm?consumableId=' + product.val(), function(data) {
			$.each(data, function(key, value) {
				items += '<option value="">-- Select --</option>';
				for(var i in value) {
					var dateArr = value[i].columnIndex_1.split('-');
					var dat = dateArr[2]+'/'+dateArr[1]+'/'+dateArr[0]
					if(c == value[i].columnIndex_0) {
						items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" selected="selected" >' + value[i].columnIndex_0 + '</option>';
					} else {
						items += '<option value="' + dat + '_' + value[i].columnIndex_2 + '" >' + value[i].columnIndex_0 + '</option>'
					}
				}			
			});
			
			$('select#' + lotId).html(items);

		    // the input field is not a select
			if($('#' + lotId).is("select")) {			
				$('#' + lotId).change(function() {
					var lotVal = lot.val();
					var arr = new Array();
					arr = lotVal.split("_");
					if(arr.length > 1) {
						$("#cons_expDateId").val("");
						$("#cons_expDateId").val(arr[0]);
						$("#cons_expDateId").attr("readonly", "readonly");
						$("#cons_dpId").val(arr[1]);
						$("#cons_qtyId").attr("disabled", "");
					} 
				});
			}
		});
	}
		
		
}

function populateLocations(type) {
	
	var retVar = '';

	if(type == 'cons_') {
		retVar = $("#consumable input[name=retType]:checked").val();
	} else {
		retVar = $("#drug input[name=retType]:checked").val();		
	}
	var options = '';
	var pharmaOptions = '';
	var drugFromStore = '';
	var drugFromPharma = '';
	
	if(retVar == 'internal') {
		/** $("#" + type + "expDateId").val("").attr("disabled", "disabled"); **/
		$("#" + type + "qtyId").val("").attr("disabled", "disabled");
		$("#" + type + "lotId").val("");
		
		if(type == 'cons_') {
			$("#consumableId").val("");
		} else { 
			$("#drugId").val("");
		}
		returnType = 'internal';
		options += '<option value="">-- Select --</option>';
		drugFromPharma += '<option value="">-- Select --</option>';
		
		for(var i in pharmaIdArray) {
			options += '<option value="'+pharmaIdArray[i]+'">'+pharmaNameArray[i]+'</option>';
		}
	
		for(var i in pharmaDrugArray) {
			drugFromPharma += '<option value="'+pharmaDrugIdArray[i]+'">'+pharmaDrugArray[i]+'</option>';
		}

		pharmaOptions += '<option value="">-- Select --</option>';
		for(var i in locIdArray) {
			if(locIdArray[i] == dftLocId) {
				pharmaOptions += '<option value="'+locIdArray[i]+'" selected="selected">'+locNameArray[i]+'</option>';
			} else {
				pharmaOptions += '<option value="'+locIdArray[i]+'">'+locNameArray[i]+'</option>';
			}			
		}
		
		$("#" + type + "fromId").html(options);
		$("#" + type + "toId").html(pharmaOptions);
		//$("#drugId").html(drugFromPharma);


		$("#" + type + "fromId").show();
		$("#" + type + "toId").show();	
	}

	if(retVar == 'external') {
		/** $("#" + type + "expDateId").val("").attr("disabled", "disabled"); **/
		$("#" + type + "qtyId").val("").attr("disabled", "disabled");
		$("#" + type + "lotId").val("");
		
		if(type == 'cons_') {
			$("#consumableId").val("");
		} else { 
			$("#drugId").val("");
		}
		
		returnType = 'external';
		options += '<option value="">-- Select --</option>';
		drugFromStore += '<option value="">-- Select --</option>';
		for(var i in locIdArray) {
			options += '<option value="'+locIdArray[i]+'">'+locNameArray[i]+'</option>';
		}
		$("#" + type + "fromId").html(options);
		$("#" + type + "toId").html(options);

		$("#" + type + "fromId").show();
		$("#" + type + "toId").show();
	}
}

</script>

<fieldset><legend><spring:message code="pharmacymanagement.returnForm" /></legend>
	

<div id="productCategory">

<ul>
	<li><a href="#fragment-1"><span><spring:message code="Drug Products" /></span></a></li>
	<li><a href="#fragment-2"><span><spring:message code="Consumable Products" /></span></a></li>
</ul>

<div id="fragment-1">
<div id="drug">
<form method="post" id="drugstore" action="return.form?productType=drug">
<table>
	<tr>
		<td><spring:message code="pharmacymanagement.returnDate" /></td>
		<td><input type="text" name="returnDate" onfocus="showCalendar(this)" size="10" id="returnDateId" /></td>
		<td colspan="2"><input type="checkbox" name="checkStock" id="showStockId" value="showStock" /><spring:message code="pharmacymanagement.checkToC" /></td>
	</tr>
	<tr>
		<td>Return type</td>
		<td><input id="intId" type="radio" name="retType" value="internal" />Internal</td>
		<td colspan="2"><input id="extId" type="radio" name="retType" value="external" />External</td>
	</tr>
	<tr>
		<td><spring:message code="pharmacymanagement.from" /></td>
		<td>
			<select name="from" id="fromId">
			</select>
		</td>
		<td><spring:message code="pharmacymanagement.to" /></td>
		<td>
			<select name="to" id="toId">
			</select>
			<div class="drug_disp_load"></div>
		</td>
	</tr>
	<tr>
		<td colspan="6">
		<table class="return">
			<thead>
				<tr>
					<th><spring:message code="pharmacymanagement.designation" /></th>
					<th><spring:message code="pharmacymanagement.lotNr" /></th>
					<th><spring:message code="pharmacymanagement.expDate" /></th>
					<th><spring:message code="pharmacymanagement.qntyReturned" /></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>
						<center>
							<select name="drug" id="drugId">
								<option value="">-- Select --</option>
								<c:forEach items="${drugMap}" var="drug">
									<option value="${drug.key}">${drug.value}</option>
								</c:forEach>
							</select>
						</center>
					</td>
					<td><span id="toReplace"> <select name="lot"
						id="lotId">
						<option>-- Select --</option>
					</select> </span>
					</td>
					<td>
					<center>
						<input type="hidden" name="ars" id="arsId" value="" /> 
						<input type="hidden" name="dp" id="dpId" value="" />
						<input type="text" name="expDate" id="expDateId" size="11" value="" />
					</center>
					</td>
					<td>
					<center><input type="text" name="qtyRet"
						disabled="disabled" size="6" id="qtyId" value="" /></center>
					</td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
	<tr>
		<td valign="top">Observation</td>
		<td colspan="6">
			<select name="observation" id="obsId">
				<option value="">--Select--</option>
				<c:forEach items="${adjustmentReasons}" var="adjustmentReason">
					<option value="${adjustmentReason.answerConcept.name.name}">${adjustmentReason.answerConcept.name.name}</option>			
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr>
		<td><input id="formSubmitId" type="submit" value="Add"
			class="send" /></td>
	</tr>
</table>
</form>
</div>
</div>

<div id="fragment-2">
<div id="consumable">
<form method="post" id="consumablestore" action="consumableReturn.form?productType=consumable">
<table>
	<tr>
		<td><spring:message code="pharmacymanagement.returnDate" /></td>
		<td><input type="text" name="returnDate"
			onfocus="showCalendar(this)" size="10" id="returnDateId" /></td>
		<td colspan="2"><input type="checkbox" name="checkStock"
			id="showStockId" value="showStock" /><spring:message
			code="pharmacymanagement.checkToC" /></td>
	</tr>
	<tr>
		<td>Return type</td>
		<td>
			<input id="intId" type="radio" name="retType" value="internal" />Internal
		</td>
		<td colspan="2">
			<input id="extId" type="radio" name="retType" value="external" />External
		</td>
	</tr>
	<tr>
		<td><spring:message code="pharmacymanagement.from" /></td>
		<td>
			<select name="from" id="cons_fromId">
			</select>
		</td>
		<td><spring:message code="pharmacymanagement.to" /></td>
		<td>
			<select name="to" id="cons_toId">
			</select>
			<div class="cons_disp_load"></div>
		</td>
	</tr>
	<tr>
		<td colspan="6">
		<table class="return">
			<thead>
				<tr>
					<th><spring:message code="pharmacymanagement.designation" /></th>
					<th><spring:message code="pharmacymanagement.lotNr" /></th>
					<th><spring:message code="pharmacymanagement.expDate" /></th>
					<th><spring:message code="pharmacymanagement.qntyReturned" /></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>
					<center><select name="consumable" id="consumableId">
						<option value="">-- Select --</option>
						<c:forEach items="${consumableMap}" var="consumable">
							<option value="${consumable.key}">${consumable.value}</option>
						</c:forEach>
					</select></center>
					</td>
					<td>
						<span id="cons_toReplace"> 
							<select name="lot" id="cons_lotId">
								<option>-- Select --</option>
							</select>
						</span>
					</td>
					<td>
						<center>
							<input type="hidden" name="ars" id="cons_arsId" value="" /> 
							<input type="hidden" name="cons_dp" id="cons_dpId" value="" /> 
							<input type="text" name="expDate" id="cons_expDateId" size="11" value="" />
						</center>
					</td>
					<td>
					<center><input type="text" name="qtyRet"
						disabled="disabled" size="6" id="cons_qtyId" value="" /></center>
					</td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
	<tr>
		<td valign="top">Observation</td>
		<td colspan="6">
			<select name="observation" id="obsId">
				<option value="">--Select--</option>
				<c:forEach items="${adjustmentReasons}" var="adjustmentReason">
					<option value="${adjustmentReason.answerConcept.name.name}">${adjustmentReason.answerConcept.name.name}</option>			
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr>
		<td><input id="formSubmitId" type="submit" value="Add"
			class="send" /></td>
	</tr>
</table>
</form>
</div>
</div>
</fieldset>
<br />
<br />
<div id="storeReturnId"></div>

</div>
<div style="clear: both;"></div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>