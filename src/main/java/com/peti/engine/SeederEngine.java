package com.peti.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SeederEngine implements ApplicationListener<ContextRefreshedEvent>
{
    @Value("${peti.seeder.engine}")
    private boolean isEngineEnable;

    @Autowired
    private AdminUserSeeder adminUserSeeder;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        if (isEngineEnable)
        {
            adminUserSeeder.loadSuperAdmin();

        }
    }
}
