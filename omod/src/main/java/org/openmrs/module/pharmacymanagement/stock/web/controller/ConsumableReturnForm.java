package org.openmrs.module.pharmacymanagement.stock.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.DrugProductInventory;
import org.openmrs.module.pharmacymanagement.Pharmacy;
import org.openmrs.module.pharmacymanagement.PharmacyInventory;
import org.openmrs.module.pharmacymanagement.ProductReturnStore;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class ConsumableReturnForm extends AbstractController {
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
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Pharmacy pharmacy = null;
		Concept consumable = null;
		Date expDate = null;
		String expDateStr = null, lot = null, to = "", from = "";
		boolean isUpdate = false;

		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
		
		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
		} catch (Exception e) {
			mav.addObject("msg", "pharmacymanagement.missingDftLoc");
		}		

		if (request.getParameter("to") != null && !request.getParameter("to").equals("")
				&& request.getParameter("from") != null && !request.getParameter("from").equals("")) {
			to = request.getParameter("to");
			from = request.getParameter("from");
			System.out.println("To: **************************************** " + to);
			System.out.println("From: **************************************** " + from);
		}
		
		if (request.getParameter("consumable") != null
				&& !request.getParameter("consumable").equals(""))
			consumable = conceptService.getConcept(Integer.valueOf(request
					.getParameter("consumable")));
		
		if (request.getParameter("lot") != null && !request.getParameter("lot").equals("")) {
			System.out.println("Lot: **************************************** " + request.getParameter("lot"));
			String[] lotArr = request.getParameter("lot").split("_");
			if (request.getParameter("retType") != null && !request.getParameter("retType").equals("") ) {
				System.out.println("Return Type: **************************************** " + request.getParameter("retyType"));
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

			if(isUpdate) {
				currSolde = service.getCurrSolde(null, consumable.getConceptId()+"", dftLoc.getLocationId() + "", expireStr , lot, 1+"");
			}
			else {
				currSolde = service.getCurrSolde(null, consumable.getConceptId()+"", dftLoc.getLocationId() + "", expireStr , lot, null);
			}
			
			
			int solde = 0;
			
			dp.setDeliveredQnty(retQty);
			dp.setConceptId(consumable);
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

				// add the returned consumable from the dispensing pharmacy into
				// store.
				dpi.setEntree(retQty);
				dpi.setSortie(0);

				// Subtracting into the dispensing pharmacy
				pi.setDate(date);
				pi.setDrugproductId(dp);
				pi.setEntree(0);
				pi.setSortie(retQty);
				int currSoldeDisp = 0;

				if(isUpdate)
					currSoldeDisp = service.getCurrSoldeDisp(null, consumable.getConceptId()
							+ "", pharmacyStr, null, null, null);
				else
					currSoldeDisp = service.getCurrSoldeDisp(null, consumable.getConceptId()
							+ "", pharmacyStr, null, null, null);
				
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
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "::. Saved");
			}
		}     

		return new ModelAndView(new RedirectView("return.form"));
	}


}
