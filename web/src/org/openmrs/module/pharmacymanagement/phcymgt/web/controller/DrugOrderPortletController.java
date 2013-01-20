/**
 * Auto generated file comment
 */
package org.openmrs.module.pharmacymanagement.phcymgt.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.PharmacyConstants;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.module.pharmacymanagement.utils.Utils;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.controller.PortletController;

/**
 *
 */
public class DrugOrderPortletController extends PortletController {	
	
	@Override
	protected void populateModel(HttpServletRequest request,
			Map<String, Object> model){
		ConceptService conceptService = Context.getConceptService();
		OrderService orderService = Context.getOrderService();
		List<Drug> drugs = Context.getConceptService().getAllDrugs();
		Map<Integer, String> drugMap = new HashMap<Integer, String>();
		DrugOrderService dos = Context.getService(DrugOrderService.class);
		LocationService locationService = Context.getLocationService();
		ConceptClass cc = conceptService.getConceptClass(9);
		List<Concept> medSet = conceptService.getConceptsByClass(cc);
		List<Concept> concMedset = Utils.getMedsets(medSet);
		String[] possibleFrequency = Utils.getPossibleFrequencyFromGlobalProperty("pharmacymanagement.possibleFrequency");
		int currSolde = 0;
		
		Location dftLoc = null;
		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
			model.put("dftLoc", dftLoc);
		} catch (Exception e) {
			model.put("msg", "Missing Profile Default Location");
		}		

		Patient patient = null;
		if (request.getParameter("patientId") != null
				&& !request.getParameter("patientId").equals("")) {
			patient = Context.getPatientService().getPatient(
					Integer.valueOf(request.getParameter("patientId")));
		}

		List<DrugOrder> drugOrders = new ArrayList<DrugOrder>();
		Collection<DrugProduct> dpList = dos.getAllProducts();
		if (patient != null) {
			drugOrders = orderService.getDrugOrdersByPatient(patient);
			model.put("patient", patient);
		}
		for(Drug drg : drugs) {
			drugMap.put(drg.getDrugId(), drg.getName().toString());
		}
		
//		System.out.println("The loop A Start: ********************* " + new Date());
//		for(Drug drug : drugs) {
//				currSolde = dos.getSoldeByToDrugLocation(new Date() + "", drug.getDrugId() + "", null,dftLoc.getLocationId() + "");				
//				if(currSolde > 0) {
//					drugMap.put(drug.getDrugId(), drug.getName()+" ["+currSolde+"]");
//				}
//		}		
//		System.out.println("The loop A End: ********************* " + new Date());
		
		
		Map<Date, List<DrugOrder>> map = new HashMap<Date, List<DrugOrder>>();		
		
		Date dat1 = null;
		for(DrugOrder o : drugOrders) {
			List<DrugOrder> ordList = new ArrayList<DrugOrder>();
			for(DrugOrder o1 : drugOrders) {
				if(o1.getStartDate().equals(o.getStartDate())) {
					ordList.add(o1);
					dat1 = o1.getStartDate();
				}
			}
			map.put(dat1, ordList);
		}
		
		model.put("map", map);
		model.put("drugOrders", drugOrders);
		model.put("reasonStoppedOptions", Utils
				.createCodedOptions(PharmacyConstants.REASON_ORDER_STOPPED));
		model.put("drugMap", drugMap);
		model.put("medSet", concMedset);
		model.put("drugs", drugs);
		model.put("patientId", patient.getPatientId());
		model.put("patient", patient);
		model.put("provider", Context.getAuthenticatedUser());
		model.put("drugsAtaTime", possibleFrequency[0]);
		model.put("timesAday", possibleFrequency[1]);
		model.put("days", possibleFrequency[2]);
		System.out.println("Drug Order End ******************************** " + new Date());
		super.populateModel(request, model);
	}
	
}
