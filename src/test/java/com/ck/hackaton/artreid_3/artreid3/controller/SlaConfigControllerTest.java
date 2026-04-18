package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlaConfigController.class)
class SlaConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlaConfig slaConfig;

    @MockitoBean
    private BuildProperties buildProperties;

    /**
     * Verifies that GET /api/sla/config returns 200 with correctly serialized JSON.
     * This test guards against the CGLIB proxy serialization bug (Jackson trying to
     * serialize Spring internal fields like $$beanFactory when a @Configuration bean
     * is returned directly).
     */
    @Test
    void getConfig_returnsSlaConfig() throws Exception {
        SlaConfig.B2c b2c = new SlaConfig.B2c();
        b2c.setReactionMinutes(45);
        b2c.setToAssemblyHours(4);
        b2c.setAssemblyToDeliveryDays(1);
        b2c.setTotalDays(2);

        SlaConfig.Delivery delivery = new SlaConfig.Delivery();
        delivery.setToPvzDays(5);
        delivery.setPvzStorageDays(7);
        delivery.setTotalDays(14);

        SlaConfig.BreachBuckets breachBuckets = new SlaConfig.BreachBuckets();
        breachBuckets.setShortMinutes(new int[]{15, 60});
        breachBuckets.setDays(new int[]{1, 3});

        when(slaConfig.getB2c()).thenReturn(b2c);
        when(slaConfig.getDelivery()).thenReturn(delivery);
        when(slaConfig.getBreachBuckets()).thenReturn(breachBuckets);
        when(slaConfig.getFullCycleDays()).thenReturn(16);

        mockMvc.perform(get("/api/sla/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b2c.reaction_minutes").value(45))
                .andExpect(jsonPath("$.b2c.to_assembly_hours").value(4))
                .andExpect(jsonPath("$.b2c.assembly_to_delivery_days").value(1))
                .andExpect(jsonPath("$.b2c.total_days").value(2))
                .andExpect(jsonPath("$.delivery.to_pvz_days").value(5))
                .andExpect(jsonPath("$.delivery.pvz_storage_days").value(7))
                .andExpect(jsonPath("$.delivery.total_days").value(14))
                .andExpect(jsonPath("$.full_cycle_days").value(16))
                .andExpect(jsonPath("$.breach_buckets.short_minutes[0]").value(15))
                .andExpect(jsonPath("$.breach_buckets.short_minutes[1]").value(60))
                .andExpect(jsonPath("$.breach_buckets.days[0]").value(1))
                .andExpect(jsonPath("$.breach_buckets.days[1]").value(3));
    }

    /**
     * Ensures response does NOT contain Spring internal proxy fields.
     */
    @Test
    void getConfig_doesNotExposeSpringInternals() throws Exception {
        SlaConfig.B2c b2c = new SlaConfig.B2c();
        SlaConfig.Delivery delivery = new SlaConfig.Delivery();
        SlaConfig.BreachBuckets breachBuckets = new SlaConfig.BreachBuckets();

        when(slaConfig.getB2c()).thenReturn(b2c);
        when(slaConfig.getDelivery()).thenReturn(delivery);
        when(slaConfig.getBreachBuckets()).thenReturn(breachBuckets);
        when(slaConfig.getFullCycleDays()).thenReturn(16);

        mockMvc.perform(get("/api/sla/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.$$beanFactory").doesNotExist())
                .andExpect(jsonPath("$.targetSource").doesNotExist());
    }
}
