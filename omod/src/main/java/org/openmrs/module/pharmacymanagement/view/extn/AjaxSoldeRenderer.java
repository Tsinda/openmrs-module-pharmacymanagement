package org.openmrs.module.pharmacymanagement.view.extn;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.view.AbstractView;

public class AjaxSoldeRenderer extends AbstractView {
	private static final Log log = LogFactory.getLog(AjaxViewRenderer.class);
	protected String drugSourceKey = "drugSource";
	protected String consumableSourceKey = "consumableSource";
	protected String fromSourceKey = "fromSource";
	protected String pharmacySourceKey = "pharmacySource";

	@Override
	protected void renderMergedOutputModel(Map map, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		log.info("Map from the returned form: " + map);
		PrintWriter writer = response.getWriter();
		Object drugSource = map.get(drugSourceKey);
		Object consumableSource = map.get(consumableSourceKey);
		Object fromSource = map.get(fromSourceKey);
		Object pharmacySource = map.get(pharmacySourceKey);
		
		DrugOrderService dos = Context
				.getService(DrugOrderService.class);
		LocationService locationService = Context.getLocationService();
		Location dftLoc = null;
		String locationStr = Context.getAuthenticatedUser()
				.getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
				
		try {
			dftLoc = locationService.getLocation(Integer
					.valueOf(locationStr));
		} catch (Exception e) { }
		

		int currSolde = 0;
		
		// Disable caching
		response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("application/json");

		writer.write("[");

		if (drugSource != null) {
			if (drugSource instanceof Drug) {
				Drug drug = (Drug) drugSource;
				// Object[] items = list.toArray();			
				
				if(fromSource != null) {
					log.info("Into distribution *****************************");
					if(fromSource instanceof String) {
						String fromIdStr = (String) fromSource;
						if(locationStr.equals(fromIdStr)) {
							log.info("into location distribution **************************************");
							currSolde = dos.getCurrSolde(drug.getDrugId() + "", null, dftLoc.getLocationId() + "", null, null, null);
						}
					}
				} else {
					log.info("When dispensing and distribution are null ********************************");
					currSolde = dos.getCurrSolde(drug.getDrugId() + "", null, dftLoc.getLocationId() + "", null, null, null);
				}
				
				if(pharmacySource != null) {
					log.info("Into dispensing ******************************");
					if(pharmacySource instanceof String) {
						String pharmacyIdStr = (String) pharmacySource;
						currSolde = dos.getCurrSoldeDisp(drug.getDrugId() + "", null, pharmacyIdStr, null, null, null);
						log.info("Soldeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee: " + currSolde);
					}
					
				}
				
				writer.write("{\"solde\":\"" + currSolde + "\"}");
			} else
				writer.write("\"ERROR: Source object is null\"");

			writer.write("]");
		}
		
		if (consumableSource != null) {
			if (consumableSource instanceof Concept) {
				Concept consumable = (Concept) consumableSource;
								
				if(fromSource != null) {
					if(fromSource instanceof String) {
						String fromIdStr = (String) fromSource;
						if(locationStr.equals(fromIdStr)) {
							currSolde = dos.getCurrSolde(null, consumable.getConceptId() + "", dftLoc.getLocationId() + "", null, null, null);
						}
					}
				} else if(pharmacySource != null) {
					if(pharmacySource instanceof String) {
						String pharmacyIdStr = (String) pharmacySource;
						currSolde = dos.getCurrSoldeDisp(null, consumable.getConceptId() + "", pharmacyIdStr, null, null, null);
					}
					
				} else {
					currSolde = dos.getCurrSolde(null, consumable.getConceptId() + "", dftLoc.getLocationId() + "", null, null, null);
				}
				
				writer.write("{\"solde\":\"" + currSolde + "\"}");
			} else
				writer.write("\"ERROR: Source object is null\"");

			writer.write("]");
		}
	}

	/**
	 * @return the drugSourceKey
	 */
	public String getDrugSourceKey() {
		return drugSourceKey;
	}

	/**
	 * @param drugSourceKey
	 *            the drugSourceKey to set
	 */
	public void setDrugSourceKey(String drugSourceKey) {
		this.drugSourceKey = drugSourceKey;
	}
	
	
	
	/**
	 * @return the consumableSourceKey
	 */
	public String getConsumableSourceKey() {
		return consumableSourceKey;
	}

	/**
	 * @param consumableSourceKey the consumableSourceKey to set
	 */
	public void setConsumableSourceKey(String consumableSourceKey) {
		this.consumableSourceKey = consumableSourceKey;
	}

	/**
	 * @return the fromSourceKey
	 */
	public String getFromSourceKey() {
		return fromSourceKey;
	}

	/**
	 * @param fromSourceKey
	 *            the fromSourceKey to set
	 */
	public void setFromSourceKey(String fromSourceKey) {
		this.fromSourceKey = fromSourceKey;
	}

	/**
	 * @return the pharmacySourceKey
	 */
	public String getPharmacySourceKey() {
		return pharmacySourceKey;
	}

	/**
	 * @param pharmacySourceKey the pharmacySourceKey to set
	 */
	public void setPharmacySourceKey(String pharmacySourceKey) {
		this.pharmacySourceKey = pharmacySourceKey;
	}

}
