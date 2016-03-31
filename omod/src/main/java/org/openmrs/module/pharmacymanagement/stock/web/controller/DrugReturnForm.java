/**
 * Auto generated file comment
 */
package org.openmrs.module.pharmacymanagement.stock.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.DrugProductInventory;
import org.openmrs.module.pharmacymanagement.Pharmacy;
import org.openmrs.module.pharmacymanagement.PharmacyConstants;
import org.openmrs.module.pharmacymanagement.PharmacyInventory;
import org.openmrs.module.pharmacymanagement.ProductReturnStore;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 *
 */
public class DrugReturnForm extends ParameterizableViewController {
	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		LocationService locationService = Context.getLocationService();
		ConceptService conceptService = Context.getConceptService();
		Location dftLoc = null, origin = null, destination = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		ProductReturnStore ars = null;
		DrugProductInventory dpi = null;
		PharmacyInventory pi = null;
		User user = Context.getAuthenticatedUser();
		DrugProduct dp = null;
		Collection<Pharmacy> pharmacies = null;
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Pharmacy pharmacy = null;
		List<Drug> drugs = conceptService.getAllDrugs();
		Drug drug = null;
		Concept consumable = null;
		Date expDate = null;
		String expDateStr = null, lot = null, to = "", from = "";
		boolean isUpdate = false;

		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
		
		Collection<ConceptAnswer> adjustmentReasons = conceptService.getConcept(PharmacyConstants.ADJUSTMENT_REASON) != null ? conceptService.getConcept(PharmacyConstants.ADJUSTMENT_REASON).getAnswers() : new HashSet<ConceptAnswer>();
		mav.addObject("adjustmentReasons", adjustmentReasons);
		

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
			pharmacies = service.getAllPharmacies();
		} catch (Exception e) {
			mav.addObject("msg", "pharmacymanagement.missingDftLoc");
		}		

		if (request.getParameter("to") != null && !request.getParameter("to").equals("")
				&& request.getParameter("from") != null && !request.getParameter("from").equals("")) {
			to = request.getParameter("to");
			from = request.getParameter("from");
		}

		if (request.getParameter("drug") != null
				&& !request.getParameter("drug").equals(""))
			drug = conceptService.getDrug(Integer.valueOf(request
					.getParameter("drug")));
		
		if (request.getParameter("consumable") != null
				&& !request.getParameter("consumable").equals(""))
			consumable = conceptService.getConcept(Integer.valueOf(request
					.getParameter("consumable")));
		
		if (request.getParameter("lot") != null && !request.getParameter("lot").equals("")) {
			String[] lotArr = request.getParameter("lot").split("_");
			if (request.getParameter("retType") != null && !request.getParameter("retType").equals("") ) {
				String returnType = request.getParameter("retType");
				if(lotArr.length > 1) {
					System.out.println("Expiration Date: " + lotArr[0] + " ************************************************");
					if((returnType.equals("external") && from.equals(locationStr)) || returnType.equals("internal")) {
						System.out.println("return type is external or internal and from is the default location: " + locationStr + " ************************************************");
						expDateStr = lotArr[0];
						expDate = sdf.parse(expDateStr);
						lot = service.getDrugProductById(Integer.valueOf(lotArr[1].toString())).getLotNo();
					}		
				} else {
					lot = request.getParameter("lot");
				}
							
				if(returnType.equals("external") && !from.equals(locationStr) && request.getParameter("expDate") != null) {
					System.out.println("return type is external and from is not the default location: " + locationStr + " ************************************************");
					expDateStr = request.getParameter("expDate");
					expDate = sdf.parse(expDateStr);
					lot = request.getParameter("lot");
				}
			}
			
			
		}

		Map<Integer, String> storeDrugMap = new HashMap<Integer, String>();
		Map<Integer, String> pharmaDrugMap = new HashMap<Integer, String>();
		Map<Integer, String> pharmaConsumableMap = new HashMap<Integer, String>();
		List<DrugProduct> pharmacyProducts = (List<DrugProduct>) service.getAllProducts();
		List<DrugProduct> dispensingProducts = new ArrayList<DrugProduct>();

		for (DrugProduct dps : pharmacyProducts) {
			if(dps.getCmddrugId() != null) {
				if(dps.getCmddrugId().getPharmacy() != null) {
					dispensingProducts.add(dps);
				}
			}
		}

		for (DrugProduct drPr : dispensingProducts) {
			if (!to.equals(locationStr)) {
				if (drPr.getDrugId() != null) {
					if (service.getCurrSolde(drPr.getDrugId().getDrugId() + "",
							null, dftLoc.getLocationId() + "",
							drPr.getExpiryDate() + "", drPr.getLotNo(), drPr
									.getCmddrugId().getCmddrugId() + "") > 0)
						
						storeDrugMap.put(drPr.getDrugId().getDrugId(), drPr.getDrugId().getName());

					if (drPr.getCmddrugId().getPharmacy() != null
							&& service.getCurrSoldeDisp(drPr.getDrugId()
									.getDrugId()
									+ "", null, drPr.getCmddrugId()
									.getPharmacy().getPharmacyId()
									+ "", drPr.getExpiryDate() + "", drPr
									.getLotNo(), null) > 0)
						pharmaDrugMap.put(drPr.getDrugId().getDrugId(), drPr
								.getDrugId().getName());
				} else {
					if (service.getCurrSolde(null, drPr.getConceptId()
							.getConceptId()
							+ "", dftLoc.getLocationId() + "", drPr
							.getExpiryDate()
							+ "", drPr.getLotNo(), drPr.getCmddrugId()
							.getCmddrugId()
							+ "") > 0)
						storeDrugMap.put(drPr.getConceptId().getConceptId(),
								drPr.getConceptId().getName().getName());

					if (drPr.getCmddrugId().getPharmacy() != null
							&& service.getCurrSoldeDisp(null, drPr
									.getConceptId().getConceptId()
									+ "", drPr.getCmddrugId().getPharmacy()
									.getPharmacyId()
									+ "", drPr.getExpiryDate() + "", drPr
									.getLotNo(), null) > 0)
						pharmaConsumableMap.put(drPr.getConceptId().getConceptId(),
								drPr.getConceptId().getName().getName());
				}
			}
		}
		/**
		 * In case you want to update the form, uncomment the following codes
		 */
//		if (request.getParameter("ars") != null
//				&& !request.getParameter("ars").equals("")) {
//			isUpdate = true;
//			ars = service.getReturnStockById(Integer.valueOf(request
//					.getParameter("ars")));
//			int dpiId = 0;
//			int piId = 0;
//			if (ars != null) {
//				dpiId = ars.getDpInventory().getInventoryId();
//				piId = ars.getPhInventory().getPharmacyInventoryId();
//
//				dpi = service.getDrugProductInventoryById(dpiId);
//				pi = Utils.getPharmacyInventoryById(piId);
//				dp = ars.getDrugproductId();
//				lot = dp.getLotNo();
//			}
//		}

		if (ars == null) {
			ars = new ProductReturnStore();
			dpi = new DrugProductInventory();
			pi = new PharmacyInventory();
			dp = new DrugProduct();
		}		
		
		if (request.getParameter("returnDate") != null
				&& !request.getParameter("returnDate").equals("")
				&& request.getParameter("qtyRet") != null
				&& !request.getParameter("qtyRet").equals("")
				&& request.getParameter("observation") != null
				&& !request.getParameter("observation").equals("")
				&& request.getParameter("retType") != null
				&& !request.getParameter("retType").equals("")
				&& request.getParameter("productType") != null
				&& !request.getParameter("productType").equals("")) {
			
			//expDate = service.getDrugProductById(Integer.valueOf(request.getParameter("dp"))).getExpiryDate();
			
			System.out.println("Return date: *************************: " + request.getParameter("returnDate"));
			System.out.println("qtyRet: *************************: " + request.getParameter("qtyRet"));
			System.out.println("observation: *************************: " + request.getParameter("observation"));
			System.out.println("retType: *************************: " + request.getParameter("retType"));
			System.out.println("productType: *************************: " + request.getParameter("productType"));
			System.out.println("Expiration Date: **********************: " + expDateStr);
			
			Date date = sdf.parse(request.getParameter("returnDate"));
			ars.setRetDate(date);
			ars.setReturnedBy(user);

			int retQty = Integer.valueOf(request.getParameter("qtyRet"));
			ars.setRetQnty(retQty);
			String[] arr = expDateStr.split("/");
			String expireStr = arr[2] + "-" + arr[1] + "-" + arr[0];
			
			int currSolde = 0;
			if (drug != null) {
				if(isUpdate) {
					currSolde = service.getCurrSolde(drug.getDrugId()+"", null, dftLoc.getLocationId() + "", expireStr , lot, 1+"");
				}
				else {
					currSolde = service.getCurrSolde(drug.getDrugId()+"", null, dftLoc.getLocationId() + "", expireStr , lot, null);
				}
			}
			
			int solde = 0;
			
			dp.setDeliveredQnty(retQty);
			dp.setDrugId(drug);
			dp.setExpiryDate(expDate);
			dp.setLotNo(lot);
			dp.setIsDelivered(true);
			dp.setStoreQnty(0);
			
			if (request.getParameter("retType").equals("internal")) {
				solde = currSolde + retQty;				
				
				if(solde >= 0)
					service.saveDrugProduct(dp);
				
				ars.setDrugproductId(dp);
				ars.setObservation(request.getParameter("observation"));

				dpi.setInventoryDate(date);
				dpi.setIsStore(true);
				dpi.setDrugproductId(dp);
				
				String pharmacyStr = from;
				pharmacy = service.getPharmacyById(Integer
						.valueOf(pharmacyStr));
				origin = pharmacy.getLocationId();

				ars.setOriginPharmacy(pharmacy);

				destination = locationService.getLocation(Integer
						.valueOf(to));
				ars.setDestination(destination);

				// add the returned drug from the dispensing pharmacy into
				// store.
				dpi.setEntree(retQty);
				dpi.setSortie(0);

				// Subtracting into the dispensing pharmacy
				pi.setDate(date);
				pi.setDrugproductId(dp);
				pi.setEntree(0);
				pi.setSortie(retQty);
				int currSoldeDisp = 0;


				if(isUpdate) {
					currSoldeDisp = service.getCurrSoldeDisp(drug.getDrugId()
							+ "", null, pharmacyStr, expireStr
							, dp.getLotNo(), 1+"");
				} else {
					currSoldeDisp = service.getCurrSoldeDisp(drug.getDrugId()
							+ "", null, pharmacyStr, expireStr
							, dp.getLotNo(), null);
				}
				
				int pharmaSolde = currSoldeDisp - retQty;
				if (pharmaSolde >= 0) {
					pi.setSolde(pharmaSolde);
					service.savePharmacyInventory(pi);
					ars.setPhInventory(pi);
				}

			} else {
				
				origin = locationService.getLocation(Integer
						.valueOf(from));
				destination = locationService.getLocation(Integer
						.valueOf(to));
				
				if (origin.getLocationId() == dftLoc.getLocationId()) {
					// Subtracting into the main store
					dpi.setEntree(0);
					dpi.setSortie(retQty);
					solde = currSolde - retQty;
				} else if (destination.getLocationId() == dftLoc
						.getLocationId()) {
					// adding to the main store
					dpi.setEntree(retQty);
					dpi.setSortie(0);
					solde = currSolde + retQty;
				}
				
				if(solde >= 0)
					service.saveDrugProduct(dp);
				
				ars.setDrugproductId(dp);
				ars.setObservation(request.getParameter("observation"));

				dpi.setInventoryDate(date);
				dpi.setIsStore(true);
				dpi.setDrugproductId(dp);
				
				
				ars.setOriginLocation(origin);				
				ars.setDestination(destination);			
			}

			if (solde >= 0) {
				dpi.setSolde(solde);
				service.saveInventory(dpi);
				ars.setDpInventory(dpi);
				service.saveReturnStock(ars);
				mav.addObject("msg", "Saved!");
			}
		}

		mav.addObject("locations", locationService.getAllLocations());
		mav.addObject("drugMapStore", storeDrugMap);
		mav.addObject("drugMapPharma", pharmaDrugMap);
		mav.addObject("dftLoc", dftLoc);
		mav.addObject("pharmacies", pharmacies);
		mav.addObject("drugs", drugs);

		mav.setViewName(getViewName());
		return mav;

	}
}
