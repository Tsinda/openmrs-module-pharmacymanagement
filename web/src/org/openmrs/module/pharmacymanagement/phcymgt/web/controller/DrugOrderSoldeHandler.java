package org.openmrs.module.pharmacymanagement.phcymgt.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.module.pharmacymanagement.view.extn.AjaxSoldeRenderer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class DrugOrderSoldeHandler extends ParameterizableViewController {
	protected AjaxSoldeRenderer itemSoldeViewRenderer;
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();
		String drugIdParam = null;
		String consumableIdParam = null;
		String fromIdParam = null;
		String pharmacyIdParam = null;
		String drugProductIdParam = null;
		String drugProductConsIdParam = null;
		
		DrugOrderService dos = Context.getService(DrugOrderService.class);
		
		Drug drug = null;
		Concept consumable = null;
		
		if(request.getParameter("drugId") != null && !request.getParameter("drugId").equals("")) {
			drugIdParam = request.getParameter("drugId");
			drug = Context.getConceptService().getDrug(Integer.valueOf(drugIdParam));
			model.put("drugSource", drug);
		}
		if(request.getParameter("consumableId") != null && !request.getParameter("consumableId").equals("")) {
			consumableIdParam = request.getParameter("consumableId");
			consumable = Context.getConceptService().getConcept(Integer.valueOf(consumableIdParam));
			model.put("consumableSource", consumable);
		}
		
		if(request.getParameter("drugProductId") != null && !request.getParameter("drugProductId").equals("")) {
			drugProductIdParam = request.getParameter("drugProductId");
			DrugProduct drugProduct = dos.getDrugProductById(Integer.valueOf(drugProductIdParam));
			drug = Context.getConceptService().getDrug(drugProduct.getDrugId().getDrugId());
			model.put("drugSource", drug);
		}
		
		if(request.getParameter("drugProductConsId") != null && !request.getParameter("drugProductConsId").equals("")) {
			drugProductConsIdParam = request.getParameter("drugProductConsId");
			DrugProduct drugProduct = dos.getDrugProductById(Integer.valueOf(drugProductConsIdParam));
			consumable = Context.getConceptService().getConcept(drugProduct.getConceptId().getConceptId());
			model.put("consumableSource", consumable);
		}
		
		if(request.getParameter("fromId") != null && !request.getParameter("fromId").equals("")) {
			fromIdParam = request.getParameter("fromId");
			model.put("fromSource", fromIdParam);
		}
		
		if(request.getParameter("pharmacyId") != null && !request.getParameter("pharmacyId").equals("")) {
			pharmacyIdParam = request.getParameter("pharmacyId");
			model.put("pharmacySource", pharmacyIdParam);
		}
		
		return new ModelAndView(itemSoldeViewRenderer, model);
	}
	/**
	 * @return the itemSoldeViewRenderer
	 */
	public AjaxSoldeRenderer getItemSoldeViewRenderer() {
		return itemSoldeViewRenderer;
	}
	/**
	 * @param itemSoldeViewRenderer the itemSoldeViewRenderer to set
	 */
	public void setItemSoldeViewRenderer(AjaxSoldeRenderer itemSoldeViewRenderer) {
		this.itemSoldeViewRenderer = itemSoldeViewRenderer;
	}

}
