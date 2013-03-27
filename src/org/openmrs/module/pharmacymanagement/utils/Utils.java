package org.openmrs.module.pharmacymanagement.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.module.pharmacymanagement.ConsumableDispense;
import org.openmrs.module.pharmacymanagement.DrugOrderPrescription;
import org.openmrs.module.pharmacymanagement.DrugProduct;
import org.openmrs.module.pharmacymanagement.DrugProductInventory;
import org.openmrs.module.pharmacymanagement.PharmacyInventory;
import org.openmrs.module.pharmacymanagement.ProductReturnStore;
import org.openmrs.module.pharmacymanagement.service.DrugOrderService;

public class Utils {
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * returns the current entered obs from a list of specific <code>Obs</code>
	 * 
	 * @param numbers
	 * @return
	 */
	public static Integer biggestObsIdNubmer(List<Obs> numbers) {

		int tmp = 0, big = 0;
		for (Obs obs : numbers) {
			if (tmp < obs.getObsId()) {
				big = obs.getObsId();
				tmp = big;
			} else {
				big = tmp;
				tmp = big;
			}
		}
		return big;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param obsDatetime
	 *            The obs datetime
	 * @param loc
	 *            The location
	 * @param p
	 *            The Patient
	 * @param c
	 *            The concept concerned
	 * @param obsValue
	 *            The value of the obs
	 * @param obsValueType
	 *            The type of obs value - 1 Numeric; 2 Datetime; 3 Text; 4 Coded
	 * @return
	 */
	public static Obs createObservation(Date obsDatetime, Location loc,
			Person p, Concept c, Object obsValue, int obsValueType) {
		Obs o = new Obs();

		try {
			o.setDateCreated(new Date());
			o.setObsDatetime(obsDatetime);
			o.setLocation(loc);
			o.setPerson(p);
			o.setConcept(c);

			if (obsValueType == 1) {
				o.setValueNumeric((Double) obsValue);
			} else if (obsValueType == 2) {
				o.setValueDatetime((Date) obsValue);
			} else if (obsValueType == 3) {
				o.setValueText("" + obsValue);
			} else if (obsValueType == 4) {
				o.setValueCoded((Concept) obsValue);
			}
		} catch (Exception e) {
			System.out
					.println("An Error occured when trying to create an observation :\n");
			e.printStackTrace();
			o = null;
		}
		return o;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param encounterDate
	 *            Date of the encounter
	 * @param provider
	 *            Provider
	 * @param location
	 *            Location
	 * @param patient
	 *            Patient
	 * @param encounterType
	 *            The type of that encounter
	 * @param obsList
	 *            The list of obs related to that encounter
	 * @return
	 * @return
	 */
	public static Encounter createEncounter(Date encounterDate, User provider,
			Location location, Patient patient, EncounterType encounterType,
			List<Obs> obsList) {
		Encounter enc = new Encounter();

		try {
			enc.setDateCreated(new Date());
			enc.setEncounterDatetime(encounterDate);
			enc.setProvider(provider.getPerson());
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
	 * get Global property values
	 * 
	 * @param id
	 * @return
	 */
	public static int getGP(String id) {
		return Integer.valueOf(Context.getAdministrationService()
				.getGlobalProperty(id));
	}

	public static String[] getPossibleFrequencyFromGlobalProperty(String id) {
		return Context.getAdministrationService().getGlobalProperty(id)
				.split(",");
	}

	/**
	 * check the days where solde was zero.
	 * 
	 * @param date
	 * @param drugId
	 * @param conceptId
	 * @param locationId
	 * @return
	 */
	public static boolean emptyDays(String date, String drugId,
			String conceptId, String locationId) {
		boolean isTrue = false;
		int soldeOnDate = 0;
		soldeOnDate = Context
				.getService(DrugOrderService.class)
				.getSoldeByFromDrugLocation(date, drugId, conceptId, locationId);
		if (soldeOnDate == 0) {
			isTrue = true;
		}
		return isTrue;
	}

	@SuppressWarnings("deprecation")
	public static int getLastDayOfMonth(int year, int month) {

		// Setup a Calendar instance.
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);
		// Set the year as year
		cal.set(Calendar.YEAR, year);
		// Set the month as month(can be set as 1 or Calendar.FEBRUARY)
		cal.set(Calendar.MONTH, month);
		// Set the date as 1st - optional
		cal.set(Calendar.DATE, 1);
		int lastDateOfMonth = cal.getActualMaximum(Calendar.DATE);
		cal.set(Calendar.DATE, lastDateOfMonth);
		return cal.getTime().getDate();
	}

	public static Integer stockOut(DrugProduct dp, int year, int month,
			String locationId) {
		int month1 = month - 1;
		int daysOfMonth = getLastDayOfMonth(year, month1);
		int count = 0;
		for (int i = 1; i <= daysOfMonth; i++) {
			String date = year + "" + month1 + "" + i;
			if (dp.getDrugId() != null) {
				if (emptyDays(date, dp.getDrugId().getDrugId() + "", null,
						locationId))
					count++;
			} else {
				if (emptyDays(date, null,
						dp.getConceptId().getConceptId() + "", locationId))
					count++;
			}
		}
		return count;
	}

	/**
	 * gets sum of all drug physical count with the same expiration date and lot
	 * number in the main stock
	 * 
	 * @param drugId
	 * @param locationId
	 * @return the sum
	 */
	public static Integer getSumOfSoldesInStock(String drugId,
			String conceptId, String locationId, String pharmacyId,
			String cmddrug) {
		int sum = 0;
		DrugOrderService service = Context.getService(DrugOrderService.class);

		List<Object[]> list = null;
		if (drugId != null)
			list = service.getLotNumbersExpirationDates(drugId, null,
					locationId, pharmacyId);
		else
			list = service.getLotNumbersExpirationDates(null, conceptId,
					locationId, pharmacyId);

		for (Object[] obj : list) {
			if (list.size() > 0) {
				sum += service.getCurrSolde(drugId, conceptId, locationId,
						obj[1].toString(), obj[0].toString(), cmddrug);
			}
		}
		return sum;
	}

	/**
	 * gets sum of all drug physical count with the same expiration date and lot
	 * number in the internal pharmacy
	 * 
	 * @param drugId
	 * @param locationId
	 * @return
	 */
	public static Integer getSumOfSoldesInPharmacy(String drugId,
			String conceptId, String locationId, String pharmacyId) {
		int sum = 0;
		DrugOrderService service = Context.getService(DrugOrderService.class);

		List<Object[]> list = null;
		if (drugId != null)
			list = service.getLotNumbersExpirationDates(drugId, null,
					locationId, pharmacyId);
		else
			list = service.getLotNumbersExpirationDates(null, conceptId,
					locationId, pharmacyId);

		for (Object[] obj : list) {
			if (list.size() > 0) {
				sum += service.getCurrSoldeDisp(drugId, conceptId, pharmacyId,
						obj[1].toString(), obj[0].toString(), null);
			}
		}
		return sum;
	}

	/**
	 * gets drugs that have been returned to other facilities
	 * 
	 * @param date
	 * @param drugProduct
	 * @return the sum of what has been return on the date
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static Integer getReturnedProductDuringTheMonth(Date date,
			DrugProduct drugProduct) throws ParseException {
		int sum = 0;
		Date dateCheck = null;
		String dateStr = null;
		DrugOrderService service = Context.getService(DrugOrderService.class);
		int month = date.getMonth() + 1;
		int gregMonth = date.getMonth();
		int year = date.getYear() + 1900;
		int lastDayOfMonth = getLastDayOfMonth(year, gregMonth);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		for (int i = 1; i <= lastDayOfMonth; i++) {
			dateStr = i + "/" + month + "/" + year;
			dateCheck = sdf.parse(dateStr);
			List<ProductReturnStore> rss = service
					.getReturnStockByDate(dateCheck);
			if (rss != null)
				for (ProductReturnStore rs : rss) {
					if (rs.getDrugproductId().getDrugproductId() == drugProduct
							.getDrugproductId()) {
						sum += rs.getRetQnty();
					}
				}
		}
		return sum;
	}

	/**
	 * Retrieve a hashmap of concept answers (concept id, bestname) given the id
	 * of the coded concept question
	 * 
	 * @param codedConceptQuestionId
	 * @return
	 */
	public static HashMap<Integer, String> createCodedOptions(
			int codedConceptQuestionId) {
		HashMap<Integer, String> answersMap = new HashMap<Integer, String>();
		Concept questionConcept = Context.getConceptService().getConcept(
				codedConceptQuestionId);
		if (questionConcept != null) {
			for (ConceptAnswer ca : questionConcept.getAnswers()) {
				answersMap.put(
						ca.getAnswerConcept().getConceptId(),
						ca.getAnswerConcept().getDisplayString()
								.toLowerCase(Context.getLocale()));
			}
		}
		return answersMap;
	}

	//
	public static Integer getTheConsommationMaximumMoyenne(String hc,
			DrugProduct dp) {
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Collection<DrugOrderPrescription> dopList = service
				.getAllDrugOrderPrescription();
		List<ConsumableDispense> consDisp = service.getAllConsumableDipsense();

		int cmm = 0;
		Calendar cal = Calendar.getInstance();
		Calendar then = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		int sum = 0;
		if (dp.getDrugId() != null && dp.getLotNo() != null) {
			for (DrugOrderPrescription dop : dopList) {
				if (dp.getDrugId().getDrugId() == dop.getDrugproductId()
						.getDrugId().getDrugId()
						&& dp.getLotNo().equals(
								dop.getDrugproductId().getLotNo())) {
					then.setTime(dop.getDate());
					if (then.after(cal))
						sum += dop.getQuantity();
					else if (then.before(cal))
						continue;
					else
						sum += dop.getQuantity();
				}
			}
		} else {
			for (ConsumableDispense cd : consDisp) {
				if (dp.getConceptId().getConceptId() == cd.getDrugproductId()
						.getConceptId().getConceptId()) {
					then.setTime(cd.getDate());
					if (then.after(cal))
						sum += cd.getQnty();
					else if (then.before(cal))
						continue;
					else
						sum += cd.getQnty();
				}
			}
		}
		cmm = sum / 3;
		if (hc.equals("HC")) {
			cmm = (sum / 3) / 4;
		}
		return cmm;
	}

	/**
	 * gets the pharmacy inventory by its Id
	 * 
	 * @param piId
	 * @return
	 */
	public static PharmacyInventory getPharmacyInventoryById(int piId) {
		PharmacyInventory pharmacyInv = null;
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Collection<PharmacyInventory> piList = service
				.getAllPharmacyInventory();
		for (PharmacyInventory pi : piList) {
			if (pi.getPharmacyInventoryId() == piId) {
				pharmacyInv = pi;
				break;
			}
		}
		return pharmacyInv;
	}

	public static void updatePharmacyInventory(PharmacyInventory pi) {

	}

	/**
	 * Gets lot numbers and expiration date of a product
	 * 
	 * @param drugId
	 * @param conceptId
	 * @param pharmacy
	 * @return <code>PharmacyInventory</code>
	 */
	public static PharmacyInventory getPharmaLotExpDP(String drugId,
			String conceptId, String pharmacy) {
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Collection<PharmacyInventory> pharmaList = service
				.getAllPharmacyInventory();
		PharmacyInventory pharmaInv = null;
		List<DrugProduct> tmpList = new ArrayList<DrugProduct>();
		for (PharmacyInventory pi : pharmaList) {
			if (conceptId != null) {
				if (pi.getDrugproductId().getCmddrugId().getPharmacy()
						.getPharmacyId() == Integer.valueOf(pharmacy)
						&& pi.getDrugproductId().getConceptId().getConceptId() == Integer
								.valueOf(conceptId)) {
					if (!tmpList.contains(pi.getDrugproductId())) {
						pharmaInv = pi;
					} else {
						tmpList.add(pi.getDrugproductId());
					}
				}
			}

			if (drugId != null) {
				if (pi.getDrugproductId().getCmddrugId().getPharmacy()
						.getPharmacyId() == Integer.valueOf(pharmacy)
						&& pi.getDrugproductId().getDrugId().getDrugId() == Integer
								.valueOf(drugId)) {
					if (!tmpList.contains(pi.getDrugproductId())) {
						pharmaInv = pi;
					} else {
						tmpList.add(pi.getDrugproductId());
					}
				}
			}
		}
		return pharmaInv;
	}

	/**
	 * Gets lot numbers and expiration date of a product
	 * 
	 * @param drugId
	 * @param conceptId
	 * @param pharmacy
	 * @return <code>Array</code> of <code>String</code>
	 */
	public static List<Object[]> getStoreLotExpDP(String drugId,
			String conceptId, String location, String pharmacy) {
		List<Object[]> prodIdentification = new ArrayList<Object[]>();

		DrugOrderService service = Context.getService(DrugOrderService.class);
		List<ProductReturnStore> ars = null;
		Collection<DrugProduct> dpList = service.getAllProducts();

		// DrugProduct drugProduct = new DrugProduct();
		List<DrugProduct> tmpList = new ArrayList<DrugProduct>();
		for (DrugProduct dp : dpList) {
			if (dp.getCmddrugId() != null) {
				if (dp.getCmddrugId().getLocationId() != null) {
					if (dp.getCmddrugId().getLocationId().getLocationId() == Integer
							.valueOf(location)) {
						if (conceptId != null) {
							if (!tmpList.contains(dp)) {
								Object[] obj = new Object[3];
								obj[0] = dp.getLotNo();
								obj[1] = dp.getExpiryDate();
								obj[2] = dp;
								prodIdentification.add(obj);
							} else {
								tmpList.add(dp);
							}
						}
					}
				}
			} else {
				ars = service.getReturnStockByDP(dp);
				if (ars.get(0).getDestination().getLocationId() == Integer
						.valueOf(location)) {
					if (conceptId != null) {
						if (!tmpList.contains(dp)) {
							Object[] obj = new Object[3];
							obj[0] = dp.getLotNo();
							obj[1] = dp.getExpiryDate();
							obj[2] = dp;
							prodIdentification.add(obj);
						} else {
							tmpList.add(dp);
						}
					}
				}
			}

			if (drugId != null && dp.getCmddrugId() != null) {
				if (dp.getCmddrugId() != null) {
					if (dp.getCmddrugId().getLocationId() != null) {
						if (dp.getCmddrugId().getLocationId().getLocationId() == Integer
								.valueOf(location)) {
							if (!tmpList.contains(dp)) {
								Object[] obj = new Object[3];
								obj[0] = dp.getLotNo();
								obj[1] = dp.getExpiryDate();
								obj[2] = dp;
								prodIdentification.add(obj);
							} else {
								tmpList.add(dp);
							}
						}
					}
				} else {
					ars = service.getReturnStockByDP(dp);
					if (ars.get(0).getDestination().getLocationId() == Integer
							.valueOf(location)) {
						if (!tmpList.contains(dp)) {
							Object[] obj = new Object[3];
							obj[0] = dp.getLotNo();
							obj[1] = dp.getExpiryDate();
							obj[2] = dp;
							prodIdentification.add(obj);
						} else {
							tmpList.add(dp);
						}
					}

				}
			}
		}
		return prodIdentification;
	}

	/**
	 * gets a <code>Set</code> of <code>DrugProduct</code>
	 * 
	 * @param conceptId
	 * @param drugId
	 * @param locationId
	 * @param pharmacyId
	 * @return
	 */
	public static Set<DrugProduct> getLotsExpDp(String conceptId,
			String drugId, String locationId, String pharmacyId) {
		DrugOrderService service = Context.getService(DrugOrderService.class);
		Set<DrugProduct> dpSet = new HashSet<DrugProduct>();

		if (pharmacyId != null) {
			Collection<PharmacyInventory> piList = service
					.getAllPharmacyInventory();
			for (PharmacyInventory pi : piList) {
				if (pi.getDrugproductId().getCmddrugId().getPharmacy()
						.getPharmacyId() == Integer.valueOf(pharmacyId)) {
					if (drugId != null
							&& pi.getDrugproductId().getDrugId() != null) {
						if (pi.getDrugproductId().getDrugId().getDrugId()
								.toString().equals(drugId)) {
							dpSet.add(pi.getDrugproductId());
						}
					} else if (conceptId != null) {
						if (conceptId.equals(pi.getDrugproductId()
								.getConceptId().getConceptId()
								+ "")) {
							dpSet.add(pi.getDrugproductId());
						}
					}
				}
			}
		}
		return dpSet;
	}

	/**
	 * configures the Dispensed to be dynamic, if there is no dispensing in the
	 * current month, the go back until where you had dispensing
	 * 
	 * @param goBackXMonth
	 * @param to
	 * @return
	 * @throws ParseException
	 */
	public static String DispensingConfig(int goBackXMonth, String to)
			throws ParseException {
		String from = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date toDate = sdf.parse(to);
		Calendar cal = Calendar.getInstance();
		cal.setTime(toDate);
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.MONTH, -goBackXMonth);
		from = sdf.format(cal.getTime());
		return from;
	}

	public static PharmacyInventory getPharmacyInventoryByDrugProduct(int dpId) {
		DrugOrderService dos = Context.getService(DrugOrderService.class);
		Collection<PharmacyInventory> piList = dos.getAllPharmacyInventory();
		List<PharmacyInventory> piList1 = new ArrayList<PharmacyInventory>();
		for (PharmacyInventory pharmaInv : piList) {
			if (pharmaInv.getDrugproductId().getDrugproductId() == dpId) {
				piList1.add(pharmaInv);
			}
		}
		PharmacyInventory pi = null;
		int tmpId = 0;
		for (PharmacyInventory phInv : piList1) {
			if (tmpId < phInv.getPharmacyInventoryId()) {
				pi = phInv;
			}
		}
		return pi;
	}

	public static DrugProductInventory getDrugProductInventoryByDrugProduct(
			int dpId) {
		DrugOrderService dos = Context.getService(DrugOrderService.class);
		List<DrugProductInventory> dpList = dos.getAllDrugProductInventory();
		List<DrugProductInventory> dpList1 = new ArrayList<DrugProductInventory>();
		for (DrugProductInventory dpInv : dpList) {
			if (dpInv.getDrugproductId().getDrugproductId() == dpId) {
				dpList1.add(dpInv);
			}
		}
		DrugProductInventory dp = null;
		int tmpId = 0;
		for (DrugProductInventory dpInv : dpList1) {
			if (tmpId < dpInv.getInventoryId()) {
				dp = dpInv;
			}
		}
		return dp;
	}

	public static ConsumableDispense getConsumableDispense(int cdId) {
		Collection<ConsumableDispense> cdList = Context.getService(
				DrugOrderService.class).getAllConsumableDipsense();
		ConsumableDispense consumableDispense = null;
		for (ConsumableDispense cd : cdList) {
			if (cd.getConsumabledispenseId() == cdId) {
				consumableDispense = cd;
				break;
			}
		}
		return consumableDispense;
	}

	private static Comparator<Object> OPENMRS_OBJECT_COMPARATOR = new Comparator<Object>() {
		// This is where the sorting happens.
		public int compare(Object obj1, Object obj2) {
			int compareInt = 0;
			if (obj1 instanceof Concept && obj2 instanceof Concept)
				compareInt = ((Concept) obj1)
						.getName()
						.getName()
						.toLowerCase()
						.compareTo(
								((Concept) obj2).getName().getName()
										.toLowerCase());

			else if (obj1 instanceof Drug && obj2 instanceof Drug)
				compareInt = ((Drug) obj1).getName().toLowerCase()
						.compareTo(((Drug) obj2).getName().toLowerCase());

			else if (obj1 instanceof ConceptAnswer
					&& obj2 instanceof ConceptAnswer)
				compareInt = ((ConceptAnswer) obj1)
						.getAnswerConcept()
						.getName()
						.getName()
						.toLowerCase()
						.compareTo(
								((ConceptAnswer) obj2).getAnswerConcept()
										.getName().getName().toLowerCase());

			return compareInt;
		}
	};

	private static Comparator<Map.Entry<Integer, String>> comparator = new Comparator<Map.Entry<Integer, String>>() {
		public int compare(Map.Entry<Integer, String> obj1,
				Map.Entry<Integer, String> obj2) {
			// do comparing on values first, keys second
			return obj1.getValue().toLowerCase()
					.compareTo(obj2.getValue().toLowerCase());
		}
	};

	public static SortedSet<Map.Entry<Integer, String>> SortMapValues(
			Map<Integer, String> map) {
		SortedSet<Map.Entry<Integer, String>> entries = new TreeSet<Map.Entry<Integer, String>>(
				comparator);
		entries.addAll(map.entrySet());
		return entries;
	}

	public static List<Concept> getMedsets(List<Concept> medset) {
		List<Concept> sortedMedset = new ArrayList<Concept>();

		for (Concept concept : medset)
			sortedMedset.add(concept);

		// Sorting Concept with medset as class by Name
		Collections.sort(sortedMedset, OPENMRS_OBJECT_COMPARATOR);

		return sortedMedset;
	}

	public static List<ConceptAnswer> sortConsumable(
			List<ConceptAnswer> consumable) {
		List<ConceptAnswer> sortedConsumable = new ArrayList<ConceptAnswer>();

		for (ConceptAnswer conceptAnswer : consumable)
			sortedConsumable.add(conceptAnswer);

		Collections.sort(sortedConsumable, OPENMRS_OBJECT_COMPARATOR);

		return sortedConsumable;
	}

	public static List<Drug> getDrugs(List<Drug> drugs) {
		List<Drug> sortedDrugs = new ArrayList<Drug>();

		for (Drug drug : drugs)
			sortedDrugs.add(drug);

		// Sorting Drugs by Name
		Collections.sort(sortedDrugs, OPENMRS_OBJECT_COMPARATOR);

		return sortedDrugs;
	}

	public static void setConsultationAppointmentAsAttended(
			Appointment appointment) {
		AppointmentUtil.saveAttendedAppointment(appointment);
	}

	/**
	 * Creates waiting appointment in Pharmacy service
	 * 
	 * @param patient
	 * @throws ParseException 
	 */
	public static void createWaitingPharmacyAppointment(Patient patient,
			Encounter encounter) throws ParseException {
		Appointment waitingAppointment = new Appointment();
		Services service = AppointmentUtil.getServiceByConcept(Context
				.getConceptService().getConcept(6711));

		// Setting appointment attributes
		waitingAppointment.setAppointmentDate(new Date());
		waitingAppointment.setAttended(false);
		waitingAppointment.setVoided(false);
		waitingAppointment.setCreatedDate(new Date());
		waitingAppointment.setCreator(Context.getAuthenticatedUser());
		waitingAppointment.setProvider(Context.getAuthenticatedUser()
				.getPerson());
		waitingAppointment.setNote("This is a waiting patient to the Pharmacy");
		waitingAppointment.setPatient(patient);
		waitingAppointment.setLocation(Context.getLocationService()
				.getDefaultLocation());

		waitingAppointment.setService(service);

		if (encounter != null)
			waitingAppointment.setEncounter(encounter);

		if (!AppointmentUtil.isPatientAlreadyWaitingThere(patient,
				new AppointmentState(4, "WAITING"), service, new Date()))
			AppointmentUtil.saveWaitingAppointment(waitingAppointment);

	}

	public static void setPharmacyAppointmentAsAttended(Appointment appointment) {
		AppointmentUtil.saveAttendedAppointment(appointment);
	}

}