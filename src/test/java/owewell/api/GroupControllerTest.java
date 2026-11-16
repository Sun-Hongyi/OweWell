package owewell.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.jayway.jsonpath.JsonPath;

import owewell.application.GroupService;
import owewell.domain.AutoSettlement;
import owewell.infrastructure.InMemoryGroupRepository;

class GroupControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        GroupService service = new GroupService(new InMemoryGroupRepository(), new AutoSettlement());
        mvc = MockMvcBuilders.standaloneSetup(new GroupController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    // Creates "Bali Trip" with alex, sam and priya, and returns its id
    private String createGroup() throws Exception {
        String body = mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Bali Trip","members":["alex","sam","priya"]}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private void addDinnerPaidByAlex(String groupId) throws Exception {
        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Dinner","paidBy":"alex","amountCents":9000,
                     "currency":"AUD","splitBetween":["alex","sam","priya"]}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.expenseId").value("e1"));
    }

    @Test
    void createsAGroup() throws Exception {
        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Bali Trip","members":["alex","sam"]}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void addingAnExpenseUpdatesBalances() throws Exception {
        String groupId = createGroup();
        addDinnerPaidByAlex(groupId);

        mvc.perform(get("/groups/" + groupId + "/balances"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[?(@.user == 'alex')].amountCents", hasItem(6000)))
            .andExpect(jsonPath("$[?(@.user == 'sam')].amountCents", hasItem(-3000)))
            .andExpect(jsonPath("$[?(@.user == 'priya')].amountCents", hasItem(-3000)));
    }

    @Test
    void settlementSuggestsTwoPayments() throws Exception {
        String groupId = createGroup();
        addDinnerPaidByAlex(groupId);

        mvc.perform(get("/groups/" + groupId + "/settlement"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].to", everyItem(is("alex"))));
    }

    @Test
    void recordingAPaymentUpdatesBalances() throws Exception {
        String groupId = createGroup();
        addDinnerPaidByAlex(groupId);

        mvc.perform(post("/groups/" + groupId + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"from":"sam","to":"alex","amountCents":3000,"currency":"AUD"}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/groups/" + groupId + "/balances"))
            .andExpect(jsonPath("$[?(@.user == 'sam')].amountCents", hasItem(0)))
            .andExpect(jsonPath("$[?(@.user == 'alex')].amountCents", hasItem(3000)));
    }

    @Test
    void unknownGroupReturns404() throws Exception {
        mvc.perform(get("/groups/does-not-exist/balances"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", containsString("does-not-exist")));
    }

    @Test
    void expenseWithNonMemberReturns400() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Taxi","paidBy":"stranger","amountCents":2000,
                     "currency":"AUD","splitBetween":["alex","stranger"]}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("stranger is not a member of Bali Trip"));
    }

    @Test
    void addsAnExpenseWithASharesSplit() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Drinks","paidBy":"alex","amountCents":3000,"currency":"AUD",
                     "split":{"type":"shares","weights":{"alex":2,"sam":1}}}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/groups/" + groupId + "/balances"))
            .andExpect(jsonPath("$[?(@.user == 'alex')].amountCents", hasItem(1000)))
            .andExpect(jsonPath("$[?(@.user == 'sam')].amountCents", hasItem(-1000)));
    }

    @Test
    void addsAnExpenseWithAPercentageSplit() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Rent","paidBy":"sam","amountCents":10000,"currency":"AUD",
                     "split":{"type":"percentage","weights":{"alex":60,"sam":40}}}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/groups/" + groupId + "/balances"))
            .andExpect(jsonPath("$[?(@.user == 'alex')].amountCents", hasItem(-6000)))
            .andExpect(jsonPath("$[?(@.user == 'sam')].amountCents", hasItem(6000)));
    }

    @Test
    void addsAnExpenseWithAnExactSplit() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Taxi","paidBy":"priya","amountCents":5000,"currency":"AUD",
                     "split":{"type":"exact","amountsCents":{"alex":2000,"sam":3000}}}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(get("/groups/" + groupId + "/balances"))
            .andExpect(jsonPath("$[?(@.user == 'priya')].amountCents", hasItem(5000)))
            .andExpect(jsonPath("$[?(@.user == 'alex')].amountCents", hasItem(-2000)))
            .andExpect(jsonPath("$[?(@.user == 'sam')].amountCents", hasItem(-3000)));
    }

    @Test
    void unknownSplitTypeReturns400() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Mystery","paidBy":"alex","amountCents":1000,"currency":"AUD",
                     "split":{"type":"vibes"}}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unknown split type: vibes"));
    }

    @Test
    void missingSplitReturns400() throws Exception {
        String groupId = createGroup();

        mvc.perform(post("/groups/" + groupId + "/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"description":"Lunch","paidBy":"alex","amountCents":1000,"currency":"AUD"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
