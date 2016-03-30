package org.openmrs.module.pharmacymanagement.service;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class MoHDrugOrderServiceTest extends BaseModuleContextSensitiveTest {

	@Test
	public void currentServiceMyBeInialised() {
		Assert.assertNotNull(Context.getService(DrugOrderService.class));
	}
}
