package com.qcadoo.mes.orders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.deviations.constants.DeviationType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.testing.model.EntityTestUtils;

public class CommonReasonTypeModelHooksTest {

    private CommonReasonTypeModelHooks commonReasonTypeModelHooks;

    @Mock
    private Entity reasonTypeEntity;

    @Mock
    private DeviationType deviationType;

    @Mock
    private DataDefinition reasonDD;

    @Captor
    private ArgumentCaptor<Date> dateCaptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        commonReasonTypeModelHooks = new CommonReasonTypeModelHooks();
        given(deviationType.getReasonModelName()).willReturn("reasonModel");
        given(deviationType.getReasonTypeInReasonModelFieldName()).willReturn("reasonField");

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, "reasonModel")).willReturn(reasonDD);

        ReflectionTestUtils.setField(commonReasonTypeModelHooks, "dataDefinitionService", dataDefinitionService);
    }

    private void stubFindResults(final boolean existsMatchingEntity) {
        SearchCriteriaBuilder scb = mock(SearchCriteriaBuilder.class);
        given(reasonDD.find()).willReturn(scb);
        given(scb.setMaxResults(anyInt())).willReturn(scb);
        given(scb.add(any(SearchCriterion.class))).willReturn(scb);
        given(scb.setProjection(any(SearchProjection.class))).willReturn(scb);
        SearchResult searchResult = mock(SearchResult.class);
        given(scb.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(existsMatchingEntity ? 1 : 0);
    }

    @Test
    public final void shouldSetCurrentDateOnUpdate() {
        // given
        Date dateBefore = new Date();
        stubReasonEntityId(1L);
        stubFindResults(false);

        // when
        commonReasonTypeModelHooks.updateDate(reasonTypeEntity, deviationType);

        // then
        verify(reasonTypeEntity).setField(eq(CommonReasonTypeFields.DATE), dateCaptor.capture());
        Date capturedDate = dateCaptor.getValue();
        Date dateAfter = new Date();
        Assert.assertTrue(capturedDate.compareTo(dateBefore) >= 0 && capturedDate.compareTo(dateAfter) <= 0);
    }

    @Test
    public final void shouldSetCurrentDateOnCreate() {
        // given
        Date dateBefore = new Date();
        stubReasonEntityId(null);
        stubFindResults(false);

        // when
        commonReasonTypeModelHooks.updateDate(reasonTypeEntity, deviationType);

        // then
        verify(reasonTypeEntity).setField(eq(CommonReasonTypeFields.DATE), dateCaptor.capture());
        Date capturedDate = dateCaptor.getValue();
        Date dateAfter = new Date();
        Assert.assertTrue(capturedDate.compareTo(dateBefore) >= 0 && capturedDate.compareTo(dateAfter) <= 0);
    }

    @Test
    public final void shouldNotSetCurrentDateIfTypeFieldValueDidNotChange() {
        // given
        stubReasonEntityId(1L);
        stubFindResults(true);

        // when
        commonReasonTypeModelHooks.updateDate(reasonTypeEntity, deviationType);

        // then
        verify(reasonTypeEntity, never()).setField(eq(CommonReasonTypeFields.DATE), any());
    }

    private void stubReasonEntityId(final Long id) {
        EntityTestUtils.stubId(reasonTypeEntity, id);
    }

}
