package org.example.usermanagement.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.example.usermanagement.clients.UsersClient;
import org.example.usermanagement.common.api.BaseApiTest;
import org.example.usermanagement.common.http.AuthHeaders;
import org.example.usermanagement.performance.TtfbAssertions;
import org.example.usermanagement.performance.TtfbConfig;
import org.example.usermanagement.performance.TtfbProbe;
import org.example.usermanagement.performance.TtfbResult;
import org.example.usermanagement.performance.TtfbRunner;
import org.example.usermanagement.routes.UsersRoutes;
import org.example.usermanagement.support.UsersFixtures;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Epic("User Management API")
@Feature("TTFB")
public class UsersTtfbTests extends BaseApiTest {

  private static final String GROUP_PERFORMANCE = "performance";
  private static final String GROUP_TTFB = "ttfb";

  private UsersClient usersClient;
  private String apiBaseUrl;
  private String readOnlyEmail;
  private String[] updateEmails;
  private String[] deleteEmails;
  private int concurrency;

  @BeforeClass(alwaysRun = true)
  public void seedTtfbFixtures() {
    ensureApiInitialized();
    usersClient = new UsersClient(this::givenBase, auth());
    apiBaseUrl = config().requireForEnvironment(environment(), "API_BASE_URL");
    concurrency = TtfbConfig.defaults().getConcurrency();

    readOnlyEmail = UsersFixtures.uniqueEmail("ttfb-get");
    usersClient.createUser(UsersFixtures.validUser(readOnlyEmail)).assertCreated().assertAll();

    updateEmails = new String[concurrency];
    deleteEmails = new String[concurrency];
    for (int slot = 0; slot < concurrency; slot++) {
      updateEmails[slot] = UsersFixtures.uniqueEmail("ttfb-put-" + slot);
      deleteEmails[slot] = UsersFixtures.uniqueEmail("ttfb-del-" + slot);
      usersClient
          .createUser(UsersFixtures.validUser(updateEmails[slot]))
          .assertCreated()
          .assertAll();
      usersClient
          .createUser(UsersFixtures.validUser(deleteEmails[slot]))
          .assertCreated()
          .assertAll();
    }
  }

  @Test(groups = {GROUP_PERFORMANCE, GROUP_TTFB})
  @Issue("BUG-005")
  @Description("GET /users TTFB: parallel requests, p90 under threshold, zero transport failures")
  public void listUsersTtfbTest() {
    TtfbResult result =
        TtfbRunner.run(TtfbProbe.get(apiBaseUrl + UsersRoutes.users()).label("GET /users"));
    TtfbAssertions.assertSuccessful(result);
  }

  @Test(groups = {GROUP_PERFORMANCE, GROUP_TTFB})
  @Description("POST /users TTFB: parallel creates with unique emails, p90 under threshold")
  public void createUserTtfbTest() {
    TtfbResult result =
        TtfbRunner.run(
            TtfbConfig.defaults(),
            "POST /users",
            slot -> {
              String email = "ttfb-create-" + slot + "-" + UUID.randomUUID() + "@example.com";
              String body =
                  String.format("{\"name\":\"TTFB User\",\"email\":\"%s\",\"age\":30}", email);
              return TtfbProbe.post(apiBaseUrl + UsersRoutes.users())
                  .label("POST /users slot " + slot)
                  .jsonBody(body);
            });
    TtfbAssertions.assertSuccessful(result);
  }

  @Test(groups = {GROUP_PERFORMANCE, GROUP_TTFB})
  @Description("GET /users/{email} TTFB: parallel reads of a pre-created user")
  public void getUserTtfbTest() {
    String url = userUrl(readOnlyEmail);
    TtfbResult result = TtfbRunner.run(TtfbProbe.get(url).label("GET /users/{email}"));
    TtfbAssertions.assertSuccessful(result);
  }

  @Test(groups = {GROUP_PERFORMANCE, GROUP_TTFB})
  @Description("PUT /users/{email} TTFB: parallel updates on pre-created users")
  public void updateUserTtfbTest() {
    TtfbResult result =
        TtfbRunner.run(
            TtfbConfig.defaults(),
            "PUT /users/{email}",
            slot -> {
              String email = updateEmails[slot];
              String url = userUrl(email);
              String body =
                  String.format(
                      "{\"name\":\"TTFB Updated %d\",\"email\":\"%s\",\"age\":31}", slot, email);
              return TtfbProbe.put(url).label("PUT /users/{email} slot " + slot).jsonBody(body);
            });
    TtfbAssertions.assertSuccessful(result);
  }

  @Test(groups = {GROUP_PERFORMANCE, GROUP_TTFB})
  @Description("DELETE /users/{email} TTFB: parallel deletes with Authentication header")
  public void deleteUserTtfbTest() {
    String token = auth().require("AUTH_TOKEN");
    TtfbResult result =
        TtfbRunner.run(
            TtfbConfig.defaults(),
            "DELETE /users/{email}",
            slot -> {
              String email = deleteEmails[slot];
              String url = userUrl(email);
              return TtfbProbe.delete(url)
                  .label("DELETE /users/{email} slot " + slot)
                  .header(AuthHeaders.AUTHENTICATION, token);
            });
    TtfbAssertions.assertSuccessful(result);
  }

  private String userUrl(String email) {
    String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
    return apiBaseUrl + UsersRoutes.userByEmail().replace("{email}", encoded);
  }
}
