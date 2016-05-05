package org.openmrs.module.pharmacymanagement.phcymgt.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohorderentrybridge.api.MoHOrderEntryBridgeService;
import org.openmrs.module.pharmacymanagement.PharmacyConstants;
import org.openmrs.module.pharmacymanagement.utils.Utils;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class DrugOrderPrescriptionController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();

		HttpSession httpSession = request.getSession();
		String qtyStr = null;
		
		/** This is created here, so that it can't be declared anywhere else... (KAMONYO) */
		Integer appointmentId = null;
		/** ... */
		
		String patientId = request.getParameter("patientId");
		Patient patient = Context.getPatientService().getPatient(
					Integer.valueOf(patientId));

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		ConceptService conceptService = Context.getConceptService();
		OrderService orderService = Context.getOrderService();
		LocationService locationService = Context.getLocationService();

		Location dftLoc = null;
		String locationStr = Context.getAuthenticatedUser().getUserProperties()
				.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		try {
			dftLoc = locationService.getLocation(Integer.valueOf(locationStr));
		} catch (Exception e) {
		}

		if (request.getParameter("editcreate") != null
				&& !request.getParameter("editcreate").equals("")
				&& patient != null) {
			if (request.getParameter("editcreate").equals("create")) {

				/** Creating an Pharmacy appointment if not exists (KAMONYO) */
				/** ... END of Appointment ... */
				
				Utils.createWaitingPharmacyAppointment(patient, null);

				DrugOrder drugOrder = new DrugOrder();
				Drug drug = conceptService.getDrug(Integer.valueOf(request
						.getParameter("drugs")));
				String startDateStr = ServletRequestUtils.getStringParameter(
						request, "startdate", null);
				String stopDateStr = ServletRequestUtils.getStringParameter(
						request, "stopdate", null);

				OrderType orderType = orderService
						.getOrderType(PharmacyConstants.DRUG_ORDER_TYPE);
				if (request.getParameter("quantity") != null
						&& !request.getParameter("quantity").equals("")) {
					qtyStr = request.getParameter("quantity");
					drugOrder.setQuantity(Double.valueOf(qtyStr));
				}

				drugOrder.setCreator(Context.getAuthenticatedUser());
				drugOrder.setConcept(drug.getConcept());
				drugOrder.setOrderType(orderType);
				drugOrder.setInstructions(request.getParameter("instructions"));
				drugOrder.setDateCreated(new Date());
				drugOrder.setPatient(patient);
				drugOrder.setDrug(drug);
				drugOrder.setOrderer(Context.getService(MoHOrderEntryBridgeService.class).getFirstCurrentProvider());
				drugOrder.setCareSetting(Context.getOrderService().getCareSettingByName("Inpatient"));//TODO how can i do this better instead of hard coding it to inpatient
				
				String drugRoute = request.getParameter("drugRoute");
				String units = request.getParameter("units");
				String frequency = request.getParameter("frequency");
				Concept route = Context.getConceptService().getConceptByUuid(drugRoute);
				Concept doseUnit = Context.getConceptService().getConceptByUuid(units);
				OrderFrequency freq = Context.getOrderService().getOrderFrequencyByUuid(frequency);
				
				drugOrder.setRoute(route );
				drugOrder.setFrequency(freq );
				drugOrder.setDoseUnits(doseUnit );
				drugOrder.setDose(Double.parseDouble(request.getParameter("dose")));
				drugOrder.setEncounter(setDrugOrderEncounterAndOrdererAndSaveDrugOrder(patient, drugOrder, sdf.parse(startDateStr), Context.getEncounterService().getEncounterType(2)));//encounter type hard codede to adult return
				drugOrder.setQuantityUnits(Context.getConceptService().getConceptByUuid(request.getParameter("quantityUnits")));
				
				if (request.getParameter("dose") != null
						&& !request.getParameter("dose").equals(""))
					drugOrder.setDose(Double.valueOf(request
							.getParameter("dose")));
				if (request.getParameter("quantity") != null
						&& !request.getParameter("quantity").equals(""))
					drugOrder.setQuantity(Double.valueOf(request
							.getParameter("quantity")));
				if (!startDateStr.equals("") && startDateStr != null) {
					Date startDate = null;
					Date stopDate = null;
					try {
						startDate = sdf.parse(startDateStr);
						if(stopDateStr.equals("") && stopDateStr != null)
						stopDate = sdf.parse(stopDateStr);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					drugOrder.setDateActivated(startDate);
					drugOrder.setAutoExpireDate(stopDate);

					try {
						orderService.saveOrder(drugOrder, null);
					} catch (APIException e) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
					}
					
					/*
					 * To be uncommented whenever the commented lines are
					 * validated
					 * 
					 * Concept c = conceptService.getConcept(6189); Concept
					 * obsValue = conceptService.getConcept(6191); Obs o =
					 * Utils.createObservation(startDate, dftLoc, patient, c,
					 * obsValue, 4); os.saveObs(o, null);
					 */

					//mav.addObject("msg", "An order has been created successfully!");
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "An order has been Created successfully!");
				} else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"You need to enter the start date!");
				}

			}

		}

		// Editing an order
		if (request.getParameter("editcreate") != null
				&& !request.getParameter("editcreate").equals("")
				&& patient != null) {
			if (request.getParameter("editcreate").equals("edit")) {
				DrugOrder drugOrder = (DrugOrder) orderService.getOrder(
						Integer.valueOf(request.getParameter("orderId")));
				// Order order = orderService.getOrder(Integer.valueOf(request
				// .getParameter("orderId")));

				Drug drug = conceptService.getDrug(Integer.valueOf(request
						.getParameter("drugs")));

				// editing the date from the form to get this format dd/MM/yyyy
				if (request.getParameter("startdate") != null
						&& !request.getParameter("startdate").equals("")) {
					String strDate1 = request.getParameter("startdate");

					Date startDate = null;
					try {
						startDate = sdf.parse(strDate1);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					drugOrder.setDateActivated(startDate);
				}
				if (request.getParameter("quantity") != null
						&& !request.getParameter("quantity").equals("")) {
					qtyStr = request.getParameter("quantity");
					drugOrder.setQuantity(Double.valueOf(qtyStr));
				}

				drugOrder.setConcept(drug.getConcept());
				drugOrder.setInstructions(request.getParameter("instructions"));
				drugOrder.setDateCreated(new Date());
				drugOrder.setPatient(patient);
				drugOrder.setDrug(drug);
				drugOrder.setEncounter(setDrugOrderEncounterAndOrdererAndSaveDrugOrder(patient, drugOrder, sdf.parse(request.getParameter("startdate")), Context.getEncounterService().getEncounterType(2)));//encounter type hard codede to adult return
				drugOrder.setOrderer(Context.getService(MoHOrderEntryBridgeService.class).getFirstCurrentProvider());
				drugOrder.setCareSetting(Context.getOrderService().getCareSettingByName("Inpatient"));//TODO how can i do this better instead of hard coding it to inpatient
				
				String drugRoute = request.getParameter("drugRoute");
				String units = request.getParameter("units");
				String frequency = request.getParameter("frequency");
				Concept route = Context.getConceptService().getConceptByUuid(drugRoute);
				Concept doseUnit = Context.getConceptService().getConceptByUuid(units);
				OrderFrequency freq = Context.getOrderService().getOrderFrequencyByUuid(frequency);
				
				drugOrder.setRoute(route );
				drugOrder.setFrequency(freq );
				drugOrder.setDoseUnits(doseUnit );
				drugOrder.setDose(Double.parseDouble(request.getParameter("dose")));
				drugOrder.setQuantityUnits(Context.getConceptService().getConceptByUuid(request.getParameter("quantityUnits")));
				
				if (request.getParameter("dose") != null
						&& !request.getParameter("dose").equals(""))
					drugOrder.setDose(Double.valueOf(request
							.getParameter("dose")));
				
				if (request.getParameter("quantity") != null
						&& !request.getParameter("quantity").equals(""))
					drugOrder.setQuantity(Double.valueOf(request
							.getParameter("quantity")));

				try {
					orderService.saveOrder(drugOrder, null);
				} catch (APIException e) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				}
				//mav.addObject("msg", "An order has been updated successfully!");
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "An order has been Revised successfully!");
			}
		}

		if (request.getParameter("delete") != null
				&& !request.getParameter("delete").equals("")
				&& patient != null) {
			Order order = orderService.getOrder(Integer.valueOf(request
					.getParameter("orderToDel")));
			order.setVoided(true);
			order.setVoidedBy(Context.getAuthenticatedUser());
			order.setVoidReason(request.getParameter("deleteReason"));
			try {
				orderService.saveOrder(order, null);
			} catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			}
			//mav.addObject("msg", "An order has been deleted successfully!");
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "An order has been deleted successfully!");
		}

		// Stopping an order
		if (request.getParameter("stopping") != null
				&& !request.getParameter("stopping").equals("")) {
			if (request.getParameter("stopping").equals("stop")) {
				Order order = orderService.getOrder(Integer.valueOf(request
						.getParameter("orderId")));
				Concept concept = conceptService.getConcept(Integer
						.valueOf(request.getParameter("reasons")));
				Date date = null;
				try {
					date = sdf.parse(request.getParameter("stopDate"));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				orderService.discontinueOrder(order, concept, date, Context.getService(MoHOrderEntryBridgeService.class).getFirstCurrentProvider(), order.getEncounter());//TODO must this be a new encounter
				
				try {
					orderService.saveOrder(order, null);
				} catch (APIException e) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				}
				//mav.addObject("msg", "An order has been stopped successfully!");
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "An order has been Stopped successfully!");
			}
		}

		return new ModelAndView(new RedirectView(
				"../../patientDashboard.form?patientId=" + patientId));
	}
	
	private Encounter setDrugOrderEncounterAndOrdererAndSaveDrugOrder(Patient patient, DrugOrder drugOrder, Date startDate, EncounterType encounterType) {
		Encounter enc = createEncounter(startDate, Context.getAuthenticatedUser(),
				Context.getLocationService().getDefaultLocation(), patient, encounterType, new ArrayList<Obs>());
		if(enc != null) {
			Context.getEncounterService().saveEncounter(enc);
		}
		
		return enc;
	}
	
	private Encounter createEncounter(Date encounterDate, User provider,
			Location location, Patient patient, EncounterType encounterType,
			List<Obs> obsList) {
		Encounter enc = new Encounter();
		
		if(encounterDate.after(new Date()))
			encounterDate = new Date();
		try {
			enc.setDateCreated(new Date());
			enc.setEncounterDatetime(encounterDate);
			enc.setProvider(provider);
			enc.setLocation(location);
			enc.setPatient(patient);
			enc.setEncounterType(encounterType);

			for (Obs o : obsList) {
				if (null != o)
					enc.addObs(o);
				else
					System.out
							.println("An observation has not been saved because it was null.");
			}
		} catch (Exception e) {
			System.out
					.println("An Error occured when trying to create an encounter :\n");
			e.printStackTrace();
			enc = null;
		}
		return enc;
	}
	
	/**
	 * Creates Pharmacy Waiting Appointment, and sets existing Consultation
	 * waiting appointments as they are now no longer needed...
	 * 
	 * @param request
	 *            HttpServletRequest parameter to be used
	 * @param patient
	 *            the patient that is going to pharmacy services
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
//	private void createPharmacyAppointment(Integer appointmentId,
//			HttpServletRequest request, Patient patient, Encounter encounter)
//			throws NumberFormatException, ParseException {
//
//		if (appointmentId != null) {
//
//			Appointment appointment = AppointmentUtil
//					.getWaitingAppointmentById(Integer.valueOf(request
//							.getParameter("appointmentId")));
//			Utils.setConsultationAppointmentAsAttended(appointment);
//
//			// Create Pharmacy waiting appointment here:
////			Utils.createWaitingPharmacyAppointment(patient, encounter);
//		}
//
//		for (Appointment appointment : AppointmentUtil
//				.getAllWaitingAppointmentsByPatientAtService(patient,
//						new AppointmentState(4, "WAITING"), new Date(),
//						AppointmentUtil.getServiceByConcept(Context
//								.getConceptService().getConcept(8053)))) {
//
//			Utils.setConsultationAppointmentAsAttended(appointment);
//		}
//	}
}
