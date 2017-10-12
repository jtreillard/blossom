package fr.mgargadennec.blossom.autoconfigure.ui.web.system;

import fr.mgargadennec.blossom.autoconfigure.core.CamundaAutoConfiguration;
import fr.mgargadennec.blossom.core.cache.BlossomCacheManager;
import fr.mgargadennec.blossom.core.common.utils.privilege.Privilege;
import fr.mgargadennec.blossom.core.common.utils.privilege.SimplePrivilege;
import fr.mgargadennec.blossom.ui.menu.MenuItem;
import fr.mgargadennec.blossom.ui.menu.MenuItemBuilder;
import fr.mgargadennec.blossom.ui.web.system.bpmn.BPMNManagerController;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Maël Gargadennnec on 04/05/2017.
 */
@Configuration
@AutoConfigureAfter({CamundaBpmAutoConfiguration.class})
@ConditionalOnClass(BPMNManagerController.class)
@ConditionalOnBean(ProcessEngine.class)
public class WebSystemBPMNAutoConfiguration {

  @Bean
  public MenuItem systemBPMNMenuItem(MenuItemBuilder builder, @Qualifier("systemMenuItem") MenuItem systemMenuItem) {
    return builder
      .key("bpmnManager")
      .label("menu.system.bpmn", true)
      .link("/blossom/system/bpmn")
      .icon("fa fa-sitemap")
      .order(4)
      .privilege(bpmnManagerPrivilegePlugin())
      .parent(systemMenuItem)
      .build();
  }


  @Bean
  public BPMNManagerController bpmnManagerController(ProcessEngine processEngine) {
    return new BPMNManagerController(processEngine);
  }


  @Bean
  public Privilege bpmnManagerPrivilegePlugin() {
    return new SimplePrivilege("system","bpmn", "manager");
  }

}
