/**
 * @author Eric
 */
package org.openmrs.module.pharmacymanagement.phcymgt.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.CmdDrug;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.Pharmacy;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 *
 */
public class PharmacyRequestForm extends ParameterizableViewController {
	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		int amountreq = 0;
		DrugProduct drugProduct = null;
		DrugProduct currDP = null;
		Drug drug = null;

		DrugOrderService serviceDrug = Context.getService(DrugOrderService.class);
		LocationService locationService = Context.getLocationService();
		ConceptService cs = Context.getConceptService();

		Collection<Pharmacy> pharmacies = serviceDrug.getAllPharmacies();
		
		List<Location> locations = locationService.getAllLocations();

		Collection<DrugProduct> dpList = serviceDrug.getAllProducts();
		Collection<DrugProduct> dpList1 = new ArrayList<DrugProduct>();
		Collection<DrugProduct> fromReturned = new ArrayList<DrugProduct>();

		for (DrugProduct dp : dpList) {
			try {
				if (dp.getCmddrugId() != null)
					dpList1.add(dp);
				else
					fromReturned.add(dp);
			} catch (NullPointerException npe) {
			}
		}

		HashSet<DrugProduct> dps = new HashSet<DrugProduct>(dpList1);
		HashSet<DrugProduct> dps1 = new HashSet<DrugProduct>(fromReturned);

		List<DrugProduct> dpSet = new ArrayList<DrugProduct>();
		List<DrugProduct> consumableSet = new ArrayList<DrugProduct>();

		Location dftLoc = null;
		List<Pharmacy> pharmacyList = null;

		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
			pharmacyList = serviceDrug.getPharmacyByLocation(dftLoc);
		} catch (Exception e) {
			mav.addObject("msg", "pharmacymanagement.missingDftLoc");
		}

		try {
			if (pharmacyList.size() == 0) {
				mav.addObject("msg", "You need to create Pharmacy first");
			}
		} catch (NullPointerException npe) {
			mav.addObject("msg", "You need to create Pharmacy first");
		}

		CmdDrug cmdDrug = new CmdDrug();

		Map requestMap = request.getParameterMap();

		ArrayList<String> fieldNames = new ArrayList<String>();
		for (Object key : requestMap.keySet()) {
			String keyString = (String) key;
			fieldNames.add(keyString);
		}

		if (request.getParameter("on") != null
				&& request.getParameter("on").equals("true")) {

			// setting and saving Drug order
			if (request.getParameterMap().containsKey("destination")
					|| request.getParameterMap().containsKey("supporter")
					|| request.getParameterMap().containsKey("month")) {
				if (!request.getParameter("destination").equals("")
						|| !request.getParameter("supporter").equals("")
						|| !request.getParameter("month").equals("")) {

					Pharmacy from = serviceDrug.getPharmacyById(Integer
							.valueOf(request.getParameter("pharmacy")));

					Location to = Context.getLocationService().getLocation(
							from.getLocationId().getLocationId());

					cmdDrug.setPharmacy(from);
					cmdDrug.setDestination(to);
					cmdDrug
							.setSupportingProg(request
									.getParameter("supporter"));

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					Date date = null;
					try {
						date = sdf.parse(request.getParameter("month"));
						cmdDrug.setMonthPeriod(date);
					} catch (ParseException e) {
						mav.addObject("msg", "pharmacymanagement.date.missing");
					}

				}
			}

			// setting and saving the drug product;
			if (fieldNames.size() != 0) {
				boolean hasSaved = false;
				int count = 1;

				for (String str : fieldNames) {

					String suffixId = str.substring(str.indexOf("_") + 1);
					String drugSuffix = "drugs_" + suffixId;
					String consSuffix = "consumable_" + suffixId;

					if (drugSuffix.equals(str)) {
						// saving the drug product
						String id = request.getParameter("drugs_" + suffixId);
						String drugneeded = request.getParameter("drugneeded_"
								+ suffixId);
						if (count == 1 && !id.equals("")
								&& !drugneeded.equals(""))
							serviceDrug.saveCmdDrug(cmdDrug);

						drugProduct = new DrugProduct();
						currDP = serviceDrug.getDrugProductById(Integer
								.valueOf(id));
						drug = cs.getDrug(currDP.getDrugId().getDrugId());

						drugProduct.setDrugId(drug);

						amountreq = Integer.parseInt(drugneeded);

						int storeqnty = serviceDrug.getCurrSolde(id, null,
								cmdDrug.getPharmacy().getLocationId() + "",
								currDP.getExpiryDate() + "", currDP.getLotNo(), cmdDrug.getCmddrugId()+"");

						drugProduct.setStoreQnty(storeqnty);
						drugProduct.setQntyReq(amountreq);
						drugProduct.setCmddrugId(cmdDrug);
						serviceDrug.saveDrugProduct(drugProduct);
						hasSaved = true;
						count++;
					} else if (consSuffix.equals(str)) {
						// saving the consumable product
						String id = request.getParameter("consumable_"
								+ suffixId);
						String consneeded = request.getParameter("consneeded_"
								+ suffixId);
						if (count == 1 && !id.equals("")
								&& !consneeded.equals(""))
							serviceDrug.saveCmdDrug(cmdDrug);

						drugProduct = new DrugProduct();
						currDP = serviceDrug.getDrugProductById(Integer
								.valueOf(id));

						drugProduct.setConceptId(currDP.getConceptId());
						int storeqnty = serviceDrug.getCurrSolde(null,
								consneeded, cmdDrug.getPharmacy()
										.getLocationId()
										+ "", null, null, cmdDrug.getCmddrugId()+"");

						drugProduct.setStoreQnty(storeqnty);
						if (consneeded != null && !consneeded.equals("")) {
							amountreq = Integer.parseInt(consneeded);
						}
						drugProduct.setQntyReq(amountreq);

						drugProduct.setCmddrugId(cmdDrug);
						serviceDrug.saveDrugProduct(drugProduct);
						hasSaved = true;
						count++;
					}
				}
				if (hasSaved) {
					serviceDrug.saveCmdDrug(cmdDrug);
					mav.addObject("msg", "pharmacymanagement.drugorder.save");

				} else {
					mav.addObject("msg",
							"pharmacymanagement.drugorder.missingdrug");
				}
			}
		} else {

			int currSolde = 0;
			int currConsumableSolde = 0;
			for (DrugProduct dp : dps) {
				if (dp.getDrugId() != null
						&& dp.getCmddrugId().getLocationId() != null) {
					currSolde = serviceDrug.getCurrSolde(dp.getDrugId()
							.getDrugId()
							+ "", null, dftLoc.getLocationId()
							+ "", dp.getExpiryDate() + "", dp.getLotNo(), null);
					if (currSolde > 0) {
						dpSet.add(dp);
					}
				}

				if (dp.getConceptId() != null
						&& dp.getCmddrugId().getLocationId() != null) {
					
					currConsumableSolde = serviceDrug.getCurrSolde(null, dp
							.getConceptId().getConceptId()
							+ "", dftLoc.getLocationId()
							+ "", null, null, null);
						
					if (currConsumableSolde > 0) {
						consumableSet.add(dp);
					}
				}
			}
			
			for (DrugProduct dp : dps1) {
				if (dp.getDrugId() != null
						&& serviceDrug.getReturnStockByDP(dp).get(0)
								.getDestination().getLocationId() == dftLoc
								.getLocationId()) {
					currSolde = serviceDrug.getCurrSolde(dp.getDrugId()
							.getDrugId()
							+ "", null, serviceDrug.getReturnStockByDP(dp).get(
							0).getDestination().getLocationId()
							+ "", dp.getExpiryDate() + "", dp.getLotNo(), null);
					if (currSolde > 0) {
						dpSet.add(dp);
					}
				}
			}

		}
		
		mav.addObject("pharmacyList", pharmacyList);
		mav.addObject("pharmacies", pharmacies);
		mav.addObject("locations", locations);
		mav.addObject("dftLoc", dftLoc);
		mav.addObject("dpSet", dpSet);
		mav.addObject("consumableSet", consumableSet);
		mav.setViewName(getViewName());
		return mav;
	}
}
