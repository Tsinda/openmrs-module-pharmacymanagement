/**
 * Auto generated file comment
 */
package org.openmrs.module.pharmacymanagement.stock.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacymanagement.CmdDrug;
import org.openmrs.module.pharmacymanagement.Consommation;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.DrugProductInventory;
import org.openmrs.module.pharmacymanagement.Pharmacy;
import org.openmrs.module.pharmacymanagement.PharmacyInventory;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;
import org.openmrs.module.pharmacymanagement.utils.Utils;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 *
 */
public class DisplayDrugOrders extends ParameterizableViewController {
	private Log log = LogFactory.getLog(this.getClass());

	@SuppressWarnings("deprecation")
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		HttpSession httpSession = request.getSession();
		ModelAndView mav = new ModelAndView();
		
		LocationService locationService = Context.getLocationService();
		ConceptService conceptService = Context.getConceptService();
		DrugOrderService service = Context.getService(DrugOrderService.class);
		
		
		String dispConf = Context.getAdministrationService().getGlobalProperty(
				"pharmacymanagement.periodDispense");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");		
		
		int total = 0, total1 = 0, currSolde = 0, currentSolde = 0, lentProd = 0, borrowedProd = 0;		
		
		List<Drug> drugs = conceptService.getAllDrugs();
		List<Location> locations = locationService.getAllLocations();
		Map<String, Consommation> drugMap = new HashMap<String, Consommation>();
		Map<String, Consommation> consommationMap = new HashMap<String, Consommation>();

		Collection<ConceptAnswer> consumables = null;
		List<ConceptAnswer> consumableList = null;		
		
		DrugProduct prodFromLot = null;
		Object obQntyRec = null;
		Object obQntyConsomMens = null;
		DrugProduct dpStockout = null;
		Location dftLoc = null;
		Date invDate = null;
		String noLot = null;

		
		try {
			consumables = conceptService.getConcept(7988).getAnswers();
			consumableList = new ArrayList<ConceptAnswer>(consumables);
		} catch (NullPointerException npe) {
			mav.addObject("msg", "No consumable in the system");
		}

		
		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
			mav.addObject("dftLoc", dftLoc);
		} catch (Exception e) {
			mav.addObject("msg", "pharmacymanagement.missingDftLoc");
		}
		

		if (request.getParameter("givenQnty") != null && !request.getParameter("givenQnty").equals("")
				&& request.getParameter("pharmacyProduct") != null && !request.getParameter("pharmacyProduct").equals("")
				&& request.getParameter("orderId") != null && !request.getParameter("orderId").equals("")
				&& request.getParameter("prodFromLot") != null && !request.getParameter("prodFromLot").equals("")
				&& request.getParameter("expDate") != null && !request.getParameter("expDate").equals("")) {

			DrugProductInventory dpi = new DrugProductInventory();
			DrugProductInventory dpiCurrSortie = new DrugProductInventory();

			int prodId = Integer.valueOf(request.getParameter("pharmacyProduct").toString());
			int orderId = Integer.valueOf(request.getParameter("orderId"));
			int givenQnty = Integer.parseInt(request.getParameter("givenQnty"));

			DrugProduct dp = service.getDrugProductById(prodId);

			noLot = request.getParameter("prodFromLot");
			
			String strDate = request.getParameter("expDate");
			String[] strDateArr = strDate.split("/");
			String dateStr = strDateArr[2] + "-" + strDateArr[1] + "-" + strDateArr[0];
		

			CmdDrug cmddrug = service.getCmdDrugById(orderId);

			Location lcation = null;

			// Store
			if (cmddrug.getLocationId() != null) {
				lcation = cmddrug.getLocationId();
				if (dp.getDrugId() != null) {
					currSolde = service.getCurrSolde(dp.getDrugId().getDrugId()
							+ "", null, cmddrug.getLocationId().getLocationId()
							+ "", dateStr, noLot, null);

					currentSolde = service.getCurrSolde(dp.getDrugId()
							.getDrugId()
							+ "", null, cmddrug.getDestination()
							.getLocationId()
							+ "", dateStr, noLot, null);
				}
				else {
					currSolde = service.getCurrSolde(null, dp.getConceptId()
							.getConceptId()
							+ "", cmddrug.getLocationId().getLocationId() + "",
							dateStr, noLot, null);

					currentSolde = service.getCurrSolde(null, dp.getConceptId()
							.getConceptId()
							+ "", cmddrug.getDestination().getLocationId()
							.toString(), dateStr, noLot, null);
				}
				
			} else { // Dispensing Pharmacy
				DrugProduct dproduct = service.getDrugProductById(Integer.valueOf(noLot));
				noLot = dproduct.getLotNo();
				if (dp.getDrugId() != null) {
					currSolde = service.getCurrSolde(dp.getDrugId().getDrugId()
							+ "", null, cmddrug.getDestination().getLocationId()
							+ "", dateStr, noLot, null);
				} else {
					currSolde = service.getCurrSolde(null, dp.getConceptId()
							.getConceptId()
							+ "", cmddrug.getDestination().getLocationId() + "", dateStr, noLot, null);
				}

			}

			if (givenQnty <= dp.getQntyReq()) {	
				if (request.getParameter("invDate") != null
						&& !request.getParameter("invDate").equals("")) {
					String inventoryDateStr = request.getParameter("invDate");
					invDate = sdf.parse(inventoryDateStr);
				} else {
					httpSession
						.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"Inventory date is missing");
				}

				dpi.setInventoryDate(invDate);

				
				
				// if (dp.getCmddrugId() == null) {
				// 	lcation = service.getReturnStockByDP(dp).get(0).getDestination();
				// }

				// when operating on the level of the main store
				if (lcation != null) {
					dpi.setEntree(givenQnty);
					dpi.setIsStore(true);
					total = currSolde + givenQnty;
					
					if (cmddrug.getDestination().getLocationId() == dftLoc.getLocationId()
							&& cmddrug.getLocationId() != null) {
						dpiCurrSortie.setInventoryDate(invDate);
						dpiCurrSortie.setSortie(givenQnty);
						dpiCurrSortie.setIsStore(true);
						total1 = currentSolde - givenQnty;
					}	
				} else {
					// operating on the level of the pharmacy(dispensing)

					lcation = cmddrug.getDestination();
					int currStat = 0;
					if (dp.getDrugId() != null)
						currStat = service.getCurrSoldeDisp(dp.getDrugId()
								.getDrugId()
								+ "", null, cmddrug.getPharmacy()
								.getPharmacyId()
								+ "", dateStr, noLot, null);
					else
						currStat = service.getCurrSoldeDisp(null, dp
								.getConceptId().getConceptId()
								+ "", cmddrug.getPharmacy().getPharmacyId()
								+ "", dateStr, noLot, null);

					dpi.setSortie(givenQnty);
					dpi.setIsStore(true);
					total = currSolde - givenQnty;
					int solde = givenQnty + currStat;

					// saving in the pharmacy inventory table
					if (solde >= 0 && dp.getCmddrugId().getPharmacy() != null) {
						PharmacyInventory pi = new PharmacyInventory();
						pi.setDate(invDate);
						pi.setEntree(givenQnty);
						pi.setSortie(0);
						pi.setDrugproductId(dp);
						pi.setSolde(solde);
						if (total >= 0) {
							service.savePharmacyInventory(pi);
						}
					}


					System.out.println("Store::destination not null ***************************** " + lcation.getName() + " disp solde: " + solde + " Total" + total + " previous drug solde: " + currSolde + " previous cons sold: " + currStat + " given quantity: " + givenQnty);
				}

				if (total >= 0) {
					dp.setDeliveredQnty(givenQnty);
					dp.setLotNo(noLot);
					if (strDate != null) {
						Date date = sdf.parse(strDate);
						dp.setExpiryDate(date);
					}

					dp.setIsDelivered(true);
					dp.setCmddrugId(cmddrug);

					dpi.setDrugproductId(dp);
					dpi.setSolde(total);
					service.saveDrugProduct(dp);
					service.saveInventory(dpi);
					mav.addObject("msg", "The order has been updated successfully");

				} else if (total1 >= 0
						&& cmddrug.getDestination().getLocationId() == dftLoc
								.getLocationId()
						&& cmddrug.getLocationId() != null) {
					dpiCurrSortie.setDrugproductId(dp);
					dpiCurrSortie.setSolde(total1);
					service.saveInventory(dpiCurrSortie);
				} else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"pharmacymanagement.stock.noenough");
				}
			} else {
				httpSession
						.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"The Given Quantity has to be less or equal to the number requested");
			}
		}

		// displaying a particular order
		if (request.getParameter("orderId") != null) {
			
			int id = Integer.parseInt(request.getParameter("orderId"));
			CmdDrug cmddrug = service.getCmdDrugById(id);
			String from = cmddrug.getMonthPeriod() + "";

			int gregMonth = cmddrug.getMonthPeriod().getMonth();
			int month = cmddrug.getMonthPeriod().getMonth() + 1;
			int year = cmddrug.getMonthPeriod().getYear() + 1900;
			int lastDay = Utils.getLastDayOfMonth(year, gregMonth);
			String toString = year + "-" + month + "-" + lastDay;

			mav.addObject("cmdDrug", cmddrug);
			Collection<DrugProduct> drugProducts = service.getDrugProductByCmdDrugId(cmddrug);

			if (isFalseIn(drugProducts)) {
				cmddrug.setIsAchieved(true);
				service.saveCmdDrug(cmddrug);
			}

			for (DrugProduct dp : drugProducts) {

				log.info("Start loop *********************************************: "+new Date());
				
				Consommation drugReq = new Consommation();
				Consommation consReq = new Consommation();
				log.info("Start getLentDrugsDuringTheMonth(): " + new Date());
				lentProd = Utils.getLentDrugsDuringTheMonth(cmddrug.getMonthPeriod(), dp);
				log.info("End getLentDrugsDuringTheMonth(): " + new Date());
				

				log.info("Start getBorrowedDrugsDuringTheMonth(): " + new Date());
				borrowedProd = Utils.getBorrowedDrugsDuringTheMonth(cmddrug.getMonthPeriod(), dp);
				log.info("End getBorrowedDrugsDuringTheMonth(): " + new Date());

				if (dp.getDrugId() != null) {
					drugReq.setLentProduct(lentProd);
					drugReq.setborrowedProduct(borrowedProd);
					drugReq.setDrugName(dp.getDrugId().getName());
					drugReq.setDrugId(dp.getDrugId().getDrugId() + "");
					drugReq.setConditUnitaire(dp.getDrugId().getUnits() + "");
					if (dp.getExpiryDate() != null)
						drugReq.setExpirationDate(dp.getExpiryDate());
				} else {
					consReq.setLentProduct(0);
					consReq.setborrowedProduct(0);
					consReq.setDrugName(dp.getConceptId().getName().getName());
					consReq.setConceptId(dp.getConceptId().getConceptId() + "");
					consReq.setConditUnitaire("");
				}

				if (cmddrug.getLocationId() != null) {
					if (dp.getDrugId() != null) {
						dpStockout = dp;
					} else {
						dpStockout = dp;
					}

					List<Pharmacy> pharmaList = service.getPharmacyByLocation(dftLoc);
					String pharmaStr = "";
					
					for (int i = 0; i < pharmaList.size(); i++) {
						pharmaStr += pharmaList.get(i).getPharmacyId();
						if (i != (pharmaList.size() - 1)) {
							pharmaStr += ",";
						}
					}

					if (dp.getDrugId() != null) {
						String fromStr = Utils.DispensingConfig(Integer.valueOf(dispConf), toString);
						
						log.info("Start Quantity Consumed Mensually: "
								+ new Date());
						obQntyConsomMens = service.getReceivedDispensedDrug(
								fromStr, toString, dp.getDrugId().getDrugId()
										+ "", pharmaStr)[1];
						log.info("End Quantity Consumed Mensually: "
								+ new Date());

						if (obQntyConsomMens != null)
							drugReq.setQntConsomMens(obQntyConsomMens);
						else
							drugReq.setQntConsomMens(0);
					} else {
						consReq.setQntConsomMens(0);
					}

					int e = 0;
					if (dp.getDrugId() != null) {
						log.info("Start Physical Count: " + new Date());
						e = service.getSoldeByToDrugLocation(toString, dp
								.getDrugId().getDrugId()
								+ "", null, cmddrug.getLocationId()
								.getLocationId()
								+ "");
						log.info("End Physical Count: " + new Date());
					} else {
						log.info("Start Physical Count cons: " + new Date());
						e = service.getSoldeByToDrugLocation(toString, null, dp
								.getConceptId().getConceptId()
								+ "", cmddrug.getLocationId().getLocationId()
								+ "");
						log.info("End Physical Count cons: " + new Date());
					}

					log.info("Start logic: "+new Date());
					int c = 0;
					if (dp.getDrugId() != null)
						c = Integer.parseInt(drugReq.getQntConsomMens() + "");
					else
						c = Integer.parseInt(consReq.getQntConsomMens() + "");
				
					log.info("Start f: " + new Date());
					int f = Utils.stockOut(dpStockout, year, month, dftLoc.getLocationId() + "");
					log.info("Start f: " + new Date());
					
					int g = 0;
					try {
						g = (c * 30) / (30 - f);
					} catch (ArithmeticException ae) {
						log.error(ae.getStackTrace());
					}
					int h = 2 * g;
					int i = h - e;
					
					log.info("End logic: "+new Date());

					if (dp.getDrugId() != null) {
						log.info("Start Solde 1st day: " + new Date());
						drugReq.setQntPremJour(service.getSoldeByFromDrugLocation(from, dp
										.getDrugId().getDrugId()
										+ "", null, cmddrug.getLocationId()
										.getLocationId()
										+ ""));
						log.info("End Solde 1st day: " + new Date());
						drugReq.setQntRestMens(e);
					} else {
						log.info("Start Solde cons 1st day cons : " + new Date());
						consReq.setQntPremJour(service
								.getSoldeByFromDrugLocation(from, null, dp
										.getConceptId().getConceptId()
										+ "", cmddrug.getLocationId()
										.getLocationId()
										+ ""));
						log.info("End Solde cons 1st day cons : " + new Date());
						consReq.setQntRestMens(e);
					}

					if (dp.getDrugId() != null) {
						
						log.info("Start Quantity Received Mensually: " + new Date());
						obQntyRec = service.getSumEntreeSortieByFromToDrugLocation(from,
										toString, dp.getDrugId().getDrugId()
												+ "", null, cmddrug
												.getLocationId()
												.getLocationId()
												+ "")[0];
						log.info("End Quantity Received Mensually: " + new Date());
						
						if (obQntyRec != null)
							drugReq.setQntRecuMens(obQntyRec);
						else
							drugReq.setQntRecuMens(0);

						drugReq.setLocationId(cmddrug.getLocationId()
								.getLocationId());
						drugReq.setDrugProduct(dp);

						drugReq.setStockOut(Utils.stockOut(dpStockout, year,
								month, dftLoc.getLocationId() + ""));
						drugReq.setAdjustMonthlyConsumption(g);
						drugReq.setMaxQnty(h);
						drugReq.setQntyToOrder(i);
					} else {
						log.info("Start Quantity Received Mensually Cons: " + new Date());
						obQntyRec = service.getSumEntreeSortieByFromToDrugLocation(from,
										toString, null, dp.getConceptId()
												.getConceptId()
												+ "", cmddrug.getLocationId()
												.getLocationId()
												+ "")[0];
						log.info("End Quantity Received Mensually Cons: " + new Date());
						
//						if (obQntyRec != null)
							consReq.setQntRecuMens(obQntyRec != null ? obQntyRec : 0);
//						else
//							consReq.setQntRecuMens(0);

						consReq.setLocationId(cmddrug.getLocationId().getLocationId());
						consReq.setDrugProduct(dp);
						consReq.setStockOut(Utils.stockOut(dpStockout, year, month, dftLoc.getLocationId() + ""));
						consReq.setAdjustMonthlyConsumption(g);
						consReq.setMaxQnty(h);
						consReq.setQntyToOrder(i);
					}
				} else {
					if (dp.getDrugId() != null) {
						dpStockout = dp;
					} else {
						dpStockout = dp;
					}
					if (dp.getDrugId() != null) {
						String fromStr = Utils.DispensingConfig(Integer.valueOf(dispConf), toString);
						log.info("Start Quantity Received Mensually pharma: " + new Date());
						obQntyConsomMens = service.getReceivedDispensedDrug(
								fromStr, toString, dp.getDrugId().getDrugId()
										+ "", cmddrug.getPharmacy()
										.getPharmacyId()
										+ "")[1];
						log.info("End Quantity Received Mensually pharma: "+new Date());
						
						if (obQntyConsomMens != null)
							drugReq.setQntConsomMens(obQntyConsomMens);
						else
							drugReq.setQntConsomMens(0);
					} else {
						consReq.setQntConsomMens(0);
					}

					int e = 0;
					int c = 0;
					if (dp.getDrugId() != null) {
						log.info("Start Physical Count pharma: "+new Date());
						e = service.getPharmacySoldeLastDayOfWeek(toString, dp
								.getDrugId().getDrugId()
								+ "", null, dp.getCmddrugId().getPharmacy()
								.getPharmacyId()
								+ "");
						log.info("End Physical Count pharma: "+new Date());
						c = Integer.parseInt(drugReq.getQntConsomMens()
								.toString());
					} else {
						log.info("Start Physical Count pharma cons: "+new Date());
						e = service.getPharmacySoldeLastDayOfWeek(toString,
								null, dp.getConceptId().getConceptId() + "", dp
										.getCmddrugId().getPharmacy()
										.getPharmacyId()
										+ "");
						log.info("End Physical Count pharma cons: "+new Date());
						c = Integer.parseInt(consReq.getQntConsomMens()
								.toString());
					}
					log.info("Start Logic: " + new Date());
					log.info("Start Stock out: " + new Date());
					int f = Utils.stockOut(dpStockout, year, month, dftLoc
							.getLocationId()
							+ "");					
					log.info("End Stock out: " + new Date());
					
					int g = 0;
					try {
						g = (c * 30) / (30 - f);
					} catch (ArithmeticException ae) {
						log.error(ae.getStackTrace());
					}
					int h = 2 * g;
					int i = h - e;
					log.info("End Logic: " + new Date());
					if (dp.getDrugId() != null) {
						log.info("Start Quantite Premiere Jour pharma: "+new Date());
						drugReq.setQntPremJour(service.getPharmacySoldeFirstDayOfWeek(from, dp
										.getDrugId().getDrugId()
										+ "", null, dp.getCmddrugId()
										.getPharmacy().getPharmacyId()
										+ ""));
						log.info("End Quantite Premiere Jour pharma: "+new Date());
						
						drugReq.setQntRestMens(e);
					} else {
						log.info("Start Quantite Premiere Jour pharma cons: "+new Date());
						consReq.setQntPremJour(service.getPharmacySoldeFirstDayOfWeek(from, null, dp
										.getConceptId().getConceptId()
										+ "", dp.getCmddrugId().getPharmacy()
										.getPharmacyId()
										+ ""));
						log.info("End Quantite Premiere Jour pharma cons: "+new Date());
						consReq.setQntRestMens(e);
					}

					if (dp.getDrugId() != null) {
						log.info("Start Quantite Recu Mensuelle pharma: "+new Date());
						obQntyRec = service.getReceivedDispensedDrug(from,
								toString,
								dp.getDrugId().getDrugId().toString(), dp
										.getCmddrugId().getPharmacy()
										.getPharmacyId()
										+ "")[0];
						log.info("End Quantite Recu Mensuelle pharma: "+new Date());

						if (obQntyRec != null)
							drugReq.setQntRecuMens(obQntyRec);
						else
							drugReq.setQntRecuMens(0);

						drugReq.setLocationId(cmddrug.getPharmacy()
								.getLocationId().getLocationId());
						drugReq.setDrugProduct(dp);
						drugReq.setStockOut(Utils.stockOut(dpStockout, year,
								month, dftLoc.getLocationId() + ""));
						drugReq.setAdjustMonthlyConsumption(g);
						drugReq.setMaxQnty(h);
						drugReq.setQntyToOrder(i);
					} else {
						log.info("Start Quantite Recu Mensuelle pharma cons: "+new Date());
						obQntyRec = service.getReceivedDispensedDrug(from,
								toString,
								dp.getConceptId().getConceptId() + "", dp
										.getCmddrugId().getPharmacy()
										.getPharmacyId()
										+ "")[0];
						log.info("End Quantite Recu Mensuelle pharma cons: "+new Date());

						if (obQntyRec != null)
							consReq.setQntRecuMens(obQntyRec);
						else
							consReq.setQntRecuMens(0);

						consReq.setLocationId(cmddrug.getPharmacy().getLocationId().getLocationId());
						consReq.setDrugProduct(dp);
						consReq.setStockOut(Utils.stockOut(dpStockout, year, month, dftLoc.getLocationId() + ""));
						consReq.setAdjustMonthlyConsumption(g);
						consReq.setMaxQnty(h);
						consReq.setQntyToOrder(i);
					}
				}

				String key = "";
				if (cmddrug.getLocationId() != null) {
					if (drugReq.getDrugId() != null) {
						key = drugReq.getDrugId().toString() + "_"
								+ cmddrug.getLocationId().getLocationId();
						drugMap.put(key, drugReq);
					} else {
						key = consReq.getConceptId().toString() + "_"
								+ cmddrug.getLocationId().getLocationId();
						consommationMap.put(key, consReq);
					}
				} else {
					if (drugReq.getDrugId() != null) {
						key = drugReq.getDrugId().toString()
								+ "_"
								+ cmddrug.getPharmacy().getLocationId()
										.getLocationId();
						drugMap.put(key, drugReq);
					} else {
						key = consReq.getConceptId().toString()
								+ "_"
								+ cmddrug.getPharmacy().getLocationId()
										.getLocationId();
						consommationMap.put(key, consReq);
					}
				}
				log.info("End loop *****************************************: "+new Date());
			}
		}

		mav.addObject("drugMap", drugMap);
		mav.addObject("consommationMap", consommationMap);
		mav.setViewName(getViewName());
		mav.addObject("now", new Date());
		String drugIdName = "";
		String locIdName = "";
		String consIdName = "";
		if (drugs.size() > 0)
			for (int i = 0; i < drugs.size(); i++) {
				drugIdName += drugs.get(i).getDrugId() + "_"
						+ drugs.get(i).getName();
				if (drugs.size() != (i + 1))
					drugIdName += ",";
			}

		for (int j = 0; j < locations.size(); j++) {
			locIdName += locations.get(j).getLocationId() + "_"
					+ locations.get(j).getName();
			if (locations.size() != (j + 1))
				locIdName += ",";
		}

		try {
			for (int k = 0; k < consumableList.size(); k++) {
				consIdName += consumableList.get(k).getAnswerConcept()
						.getConceptId()
						+ "_"
						+ consumableList.get(k).getAnswerConcept().getName()
								.getName();
				if (consumableList.size() != (k + 1))
					consIdName += ",";

				mav.addObject("consumerList", consIdName);
			}
		} catch (NullPointerException npe) {
			mav.addObject("msg", "No Consumables in the concept dictionary");
		}

		mav.addObject("drugs", drugIdName);
		mav.addObject("locations", locIdName);
		return mav;

	}

	public boolean isFalseIn(Collection<DrugProduct> pros) {
		boolean isContained = true;
		for (DrugProduct dp : pros) {
			if (!dp.getIsDelivered()) {
				isContained = false;
			}
		}
		return isContained;
	}
}