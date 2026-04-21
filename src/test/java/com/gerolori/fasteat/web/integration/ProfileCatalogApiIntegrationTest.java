package com.gerolori.fasteat.web.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.domain.entity.Ingredient;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.IngredientRepository;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "fasteat.seed.demo.enabled=false",
        "fasteat.security.admin-bootstrap.enabled=false"
})
class ProfileCatalogApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private UUID visibleMenuId;

    @BeforeEach
    void setUpCatalogData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        menuRepository.deleteAllInBatch();
        ingredientRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();

        Restaurant visibleA = new Restaurant();
        visibleA.setOwnerUserId(UUID.randomUUID());
        visibleA.setName("Alpha Kitchen");
        visibleA.setSummary("Fast casual");
        visibleA.setDescription("Fresh meals");
        visibleA.setCategory("CASUAL_DINING");
        visibleA.setAvailable(true);
        visibleA.setVisible(true);
        visibleA.setRating(new BigDecimal("4.60"));
        visibleA.setRatingCount(12L);
        visibleA.setCity("Makati");
        visibleA.setState("NCR");
        visibleA.setCountry("Philippines");
        visibleA.setLatitude(14.5547);
        visibleA.setLongitude(121.0244);

        Restaurant visibleB = new Restaurant();
        visibleB.setOwnerUserId(UUID.randomUUID());
        visibleB.setName("Bravo Kitchen");
        visibleB.setSummary("Comfort food");
        visibleB.setDescription("Daily specials");
        visibleB.setCategory("QUICK_SERVICE");
        visibleB.setAvailable(true);
        visibleB.setVisible(true);
        visibleB.setRating(new BigDecimal("4.20"));
        visibleB.setRatingCount(7L);
        visibleB.setCity("Taguig");
        visibleB.setState("NCR");
        visibleB.setCountry("Philippines");

        Restaurant hidden = new Restaurant();
        hidden.setOwnerUserId(UUID.randomUUID());
        hidden.setName("Hidden Kitchen");
        hidden.setSummary("Not discoverable");
        hidden.setDescription("Hidden listing");
        hidden.setCategory("TEST_ONLY");
        hidden.setAvailable(true);
        hidden.setVisible(false);

        visibleA = restaurantRepository.save(visibleA);
        visibleB = restaurantRepository.save(visibleB);
        hidden = restaurantRepository.save(hidden);
        Ingredient chicken = new Ingredient();
        chicken.setName("Chicken");
        chicken.setSummary("Lean protein");
        chicken.setAvailable(true);

        Ingredient basil = new Ingredient();
        basil.setName("Basil");
        basil.setSummary("Fresh herb");
        basil.setAvailable(false);

        chicken = ingredientRepository.save(chicken);
        basil = ingredientRepository.save(basil);

        Menu alphaMenu = new Menu();
        alphaMenu.setRestaurant(visibleA);
        alphaMenu.setName("Chicken Bowl");
        alphaMenu.setSummary("Balanced meal");
        alphaMenu.setDescription("Protein and greens");
        alphaMenu.setCategory("MAIN");
        alphaMenu.setPrice(new BigDecimal("8.99"));
        alphaMenu.setAvailable(true);
        alphaMenu.setActive(true);
        alphaMenu.setImageUrl("https://cdn.example.com/chicken-bowl.png");
        alphaMenu.setIngredients(new LinkedHashSet<>());
        alphaMenu.getIngredients().add(chicken);
        alphaMenu.getIngredients().add(basil);

        Menu betaMenu = new Menu();
        betaMenu.setRestaurant(visibleB);
        betaMenu.setName("Veggie Wrap");
        betaMenu.setSummary("Light lunch");
        betaMenu.setDescription("Crisp vegetables and sauce");
        betaMenu.setCategory("MAIN");
        betaMenu.setPrice(new BigDecimal("7.49"));
        betaMenu.setAvailable(true);
        betaMenu.setActive(true);

        Menu hiddenMenu = new Menu();
        hiddenMenu.setRestaurant(hidden);
        hiddenMenu.setName("Invisible Burger");
        hiddenMenu.setSummary("Should not be listed");
        hiddenMenu.setDescription("Hidden because restaurant is hidden");
        hiddenMenu.setCategory("MAIN");
        hiddenMenu.setPrice(new BigDecimal("9.99"));
        hiddenMenu.setAvailable(true);
        hiddenMenu.setActive(true);

        alphaMenu = menuRepository.save(alphaMenu);
        menuRepository.save(betaMenu);
        menuRepository.save(hiddenMenu);
        visibleMenuId = alphaMenu.getId();
    }

    @Test
    void usersMeRequiresAuthenticationWithCanonicalUnauthorizedEnvelope() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_MISSING_TOKEN"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/users/me"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void usersMeReturnsCurrentPrincipalProfileWhenAuthenticated() throws Exception {
        String email = "profile-int-" + UUID.randomUUID() + "@fasteat.test";
        String accessToken = registerAndGetAccessToken(email, "password123");

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.subscription.enabled").value(false));
    }

    @Test
    void restaurantsEndpointIsPublicAndReturnsCanonicalPaginationEnvelope() throws Exception {
        mockMvc.perform(get("/restaurants").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void missingRestaurantMapsToCanonicalNotFoundEnvelope() throws Exception {
        UUID missingRestaurantId = UUID.randomUUID();

        mockMvc.perform(get("/restaurants/{restaurantId}", missingRestaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/restaurants/" + missingRestaurantId))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void menusEndpointIsPublicAndReturnsCanonicalPaginationEnvelope() throws Exception {
        mockMvc.perform(get("/menus").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void menuIngredientReadFlowReturnsProjectedIngredients() throws Exception {
        mockMvc.perform(get("/menus/{menuId}/ingredients", visibleMenuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Basil"))
                .andExpect(jsonPath("$[0].isAvailable").value(false))
                .andExpect(jsonPath("$[1].name").value("Chicken"))
                .andExpect(jsonPath("$[1].isAvailable").value(true));
    }

    @Test
    void missingMenuAndIngredientReadsMapToCanonicalNotFoundEnvelope() throws Exception {
        UUID missingMenuId = UUID.randomUUID();

        mockMvc.perform(get("/menus/{menuId}", missingMenuId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/menus/" + missingMenuId))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));

        mockMvc.perform(get("/menus/{menuId}/ingredients", missingMenuId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/menus/" + missingMenuId + "/ingredients"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    private String registerAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode payload = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return payload.path("accessToken").asText();
    }
}
