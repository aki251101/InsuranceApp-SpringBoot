package jp.yoshiaki.insuranceapp.controller;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.service.AccidentService;
import jp.yoshiaki.insuranceapp.service.AiService;
import jp.yoshiaki.insuranceapp.service.AiUsageLimitService;
import jp.yoshiaki.insuranceapp.service.ListSortService;
import jp.yoshiaki.insuranceapp.service.PolicyService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AccidentPageControllerTest {

    @Test
    void aiSuggestRejectsResolvedAccidentBeforeAuthenticationCheck() {
        AccidentService accidentService = mock(AccidentService.class);
        AiService aiService = mock(AiService.class);
        AiUsageLimitService aiUsageLimitService = mock(AiUsageLimitService.class);
        ListSortService listSortService = mock(ListSortService.class);
        PolicyService policyService = mock(PolicyService.class);
        Accident accident = mock(Accident.class);
        given(accident.getStatus()).willReturn("RESOLVED");
        given(accidentService.getAccidentById(3L)).willReturn(Optional.of(accident));

        AccidentPageController controller = new AccidentPageController(
                accidentService, aiService, aiUsageLimitService, listSortService, policyService);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> controller.aiSuggest(3L, null, null));

        assertEquals("対応が完了した事故ではAI提案を利用できません。", exception.getMessage());
        verifyNoInteractions(aiUsageLimitService, aiService);
    }
}
