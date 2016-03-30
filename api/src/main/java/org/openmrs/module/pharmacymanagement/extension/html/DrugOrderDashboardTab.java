/**
 * Auto generated file comment
 */
package org.openmrs.module.pharmacymanagement.extension.html;

import org.openmrs.module.web.extension.PatientDashboardTabExt;

/**
 *
 */
public class DrugOrderDashboardTab extends PatientDashboardTabExt {
	/**
     * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getPortletUrl()
     */
    @Override
    public String getPortletUrl() {
	    return "drugOrderPortlet";
    }

	/**
     * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getRequiredPrivilege()
     */
    @Override
    public String getRequiredPrivilege() {
	    return "Patient Dashboard - View Drug Order Section";
    }

	/**
     * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabId()
     */
    @Override
    public String getTabId() {
	    return "DrugOrderTabId";
    }

	/**
     * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabName()
     */
    @Override
    public String getTabName() {
	    return "Drug Order";
    }
}
