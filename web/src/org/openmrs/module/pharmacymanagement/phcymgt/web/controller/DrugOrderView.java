/**
 * Auto generated file comment
 */
package org.openmrs.module.pharmacymanagement.phcymgt.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.ProductReturnStore;
import org.openmrs.module.pharmacymanagement.Adherance;
import org.openmrs.module.pharmacymanagement.PharmacyConstants;
import org.openmrs.module.pharmacymanagement.DrugOrderPrescription;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author DUSABE E.
 */
public class DrugOrderView extends ParameterizableViewController {

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();

		Location dftLoc = null;
		LocationService locationService = Context.getLocationService();

		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
		} catch (Exception e) {
			mav.addObject("msg", "pharmacymanagement.missingDftLoc");
		}
		Collection<DrugOrderPrescription> dops = new ArrayList<DrugOrderPrescription>();
		Collection<DrugOrderPrescription> dopList = null;
		Patient patient = null;
		List<Adherance> adheranceList = new ArrayList<Adherance>();
		List<ProductReturnStore> returnStoreList = new ArrayList<ProductReturnStore>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		DrugOrderService service = Context.getService(DrugOrderService.class);

		if (request.getParameter("isChecked") != null
				&& !request.getParameter("isChecked").equals("")) {
			if (request.getParameter("isChecked").equals("1")) {
				if (request.getParameter("retDate") != null
						&& !request.getParameter("retDate").equals("")) {
					Date date = sdf.parse(request.getParameter("retDate"));
					List<ProductReturnStore> lentListProduct = service.getReturnStockByDate(date, "Lend");
					List<ProductReturnStore> borrowedListProduct = service.getReturnStockByDate(date, "Borrow");
					
					returnStoreList.addAll(lentListProduct);
					returnStoreList.addAll(borrowedListProduct);
					
					System.out.println("Lend: " + lentListProduct.size() + " Borrow: " + borrowedListProduct.size() + " return: " + returnStoreList.size());
					
					mav.addObject("returnStoreList", returnStoreList);
				}
			}
		}

		if (request.getParameter("patientId") != null
				&& !request.getParameter("patientId").equals("")) {
			patient = Context.getPatientService().getPatient(
					Integer.valueOf(request.getParameter("patientId")));
			dopList = service.getDOPByPatientId(patient);
		} else {
			dopList = service.getAllDrugOrderPrescription();
		}
		Set<Obs> obsList = null;

		for (DrugOrderPrescription dop : dopList) {
			if (dop.getDrugproductId().getCmddrugId().getDestination() != null
					&& dop.getDrugproductId().getCmddrugId().getDestination()
							.equals(dftLoc))
				dops.add(dop);
			else if(dop.getDrugproductId().getCmddrugId().getLocationId() != null && dop.getDrugproductId().getCmddrugId().getLocationId().equals(dftLoc))
				dops.add(dop);
		}

		for (DrugOrderPrescription dop : dops) {
			Adherance adherance = new Adherance();
			adherance.setDop(dop);
			try {
				adherance.setEncDate(dop.getEncounterId()
						.getEncounterDatetime());
				obsList = dop.getEncounterId().getObs();
			} catch (NullPointerException npe) {
			}

			if (obsList != null) {
				for (Obs obs : obsList) {
					if (obs.getConcept().getConceptId() == PharmacyConstants.WEIGHT) {
						adherance.setWeight(obs.getValueNumeric());
					}
					if (obs.getConcept().getConceptId() == PharmacyConstants.NEXT_VISIT_DATE) {
						adherance.setNvDate(obs.getValueDatetime());
					}
				}
			}
			adheranceList.add(adherance);
		}

		mav.addObject("adheranceList", adheranceList);
		mav.addObject("dopList", dopList);

		mav.setViewName(getViewName());

		return mav;
	}
}
