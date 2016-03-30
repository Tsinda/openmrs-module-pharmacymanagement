package org.openmrs.module.pharmacymanagement.stock.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.module.pharmacymanagement.view.extn.AjaxView;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class LotNumbersController implements Controller {
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	public ModelAndView handleRequest(HttpServletRequest request,

	HttpServletResponse response) throws Exception {
		AjaxView av = null;
		log.info("Returning ajax view");
		LocationService locationService = Context.getLocationService();
		Location dftLoc = null;

		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
		} catch (Exception e) {
			log.info("No Default Location Set");
		}

		Map<Integer, List<Object[]>> dpLot = new HashMap<Integer, List<Object[]>>();
		DrugOrderService service = Context.getService(DrugOrderService.class);

		if (request.getParameter("drugId") != null
				&& !request.getParameter("drugId").equals("")) {
			List<Object[]> obj = service.getLotNumbersExpirationDates(request
					.getParameter("drugId"), null, dftLoc.getLocationId().toString(),
					null);
			
			av = new AjaxView();			
			dpLot.put(1, obj);
			return new ModelAndView(av,dpLot);
		}		
		
		if (request.getParameter("consumableId") != null
				&& !request.getParameter("consumableId").equals("")) {
			List<Object[]> obj = service.getLotNumbersExpirationDates(null, request
					.getParameter("consumableId"), dftLoc.getLocationId().toString(),
					null);
			
			av = new AjaxView();			
			dpLot.put(1, obj);
			return new ModelAndView(av,dpLot);
		}	
		
		
		return new ModelAndView(av,dpLot);
	}
}
