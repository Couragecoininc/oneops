package com.oneops.inductor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.execution.Response;
import com.oneops.cms.execution.Result;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.gslb.GslbProvider;
import com.oneops.inductor.util.ResourceUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/** A standalone gslb migration test. */
public class GslbMigrationTest {

  private static Gson gson = new Gson();

  private static FqdnExecutor fqdnExec;

  @BeforeClass
  public static void setup() {
    WoHelper wh = new WoHelper();
    wh.gson = gson;

    FqdnVerifier fqdnVerifier = new FqdnVerifier();
    fqdnVerifier.woHelper = wh;

    fqdnExec = new FqdnExecutor();
    fqdnExec.gslbProvider = new GslbProvider();
    fqdnExec.jsonParser = new JsonParser();
    fqdnExec.woHelper = wh;
    fqdnExec.fqdnVerifier = fqdnVerifier;
    fqdnExec.gson = gson;
  }

  @Test
  @Ignore
  public void migration() {
    execMigrate("/gslb-migration.json");
  }

  @Test
  @Ignore
  public void rollback() {
    execMigrate("/gslb-rollback.json");
  }

  /** Invoke gslb migrate action */
  private void execMigrate(String actionOrderPath) {
    CmsActionOrderSimple ao =
        gson.fromJson(
            ResourceUtils.readResourceAsString(actionOrderPath), CmsActionOrderSimple.class);
    Response res = fqdnExec.execute(ao, System.getProperty("java.io.tmpdir"));
    assertEquals(res.getResult(), Result.SUCCESS);
    assertNotNull(ao.resultCi);
    assertEquals(OpsActionState.complete, ao.getActionState());
  }

  @Test
  public void gslbDomainTest() {
    String delegationConfig =
        "{\"delegation\": [{\n"
            + "\"base_domain\": \"glb.prod.walmart.com\",\n"
            + "\"excluded_domains\": [\"api.glb.prod.walmart.com\",\n"
            + "\"docker.glb.prod.walmart.com\",\n"
            + "\"qa-custapp-samsclub.glb.prod.walmart.com\",\n"
            + "\"search.glb.prod.walmart.com\",\n"
            + "\"tax.glb.prod.walmart.com\",\n"
            + "\"torbit.glb.prod.walmart.com\"\n"
            + "]}]}";

    Map<String, Boolean> domains = new HashMap<>();
    domains.put("glb.prod.walmart.com", false);
    domains.put("app1.glb.prod.walmart.com", true);
    domains.put("org.assembly.env.glb.prod.walmart.com", true);
    domains.put("api.glb.prod.walmart.com", false);
    domains.put("test.api.glb.prod.walmart.com", false);
    domains.put("docker.glb.prod.walmart.com", false);
    domains.put("app.docker.glb.prod.walmart.com", false);
    domains.put("qa-custapp-samsclub.glb.prod.walmart.com", false);
    domains.put("app1.qa-custapp-samsclub.glb.prod.walmart.com", false);
    domains.put("search.glb.prod.walmart.com", false);
    domains.put("test.search.glb.prod.walmart.com", false);
    domains.put("tax.glb.prod.walmart.com", false);
    domains.put("app1.tax.glb.prod.walmart.com", false);
    domains.put("torbit.glb.prod.walmart.com", false);
    domains.put("app1.torbit.glb.prod.walmart.com", false);

    domains.forEach(
        (domain, valid) -> {
          assertEquals(valid, fqdnExec.isValidDomain(domain, delegationConfig, "12345:12345"));
        });
  }
}