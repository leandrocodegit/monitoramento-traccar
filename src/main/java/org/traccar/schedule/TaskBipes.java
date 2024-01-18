/*
 * Copyright 2020 - 2023 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.schedule;

import io.netty.util.Timeout;
import io.netty.util.Timer;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.enuns.StatusBipe;
import org.traccar.api.services.BipeService;
import org.traccar.broadcast.BroadcastInterface;
import org.traccar.broadcast.BroadcastService;
import org.traccar.config.Config;
import org.traccar.database.DeviceLookupService;
import org.traccar.model.*;
import org.traccar.notification.MessageException;
import org.traccar.notificators.NotificatorFirebase;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.DataBaseSQL;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskBipes implements ScheduleTask, BroadcastInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskBipes.class);
    private static final long CHECK_PERIOD_MINUTES = 1;
    private final DataBaseSQL storage;
    private final BipeService bipeService;
    private final NotificatorFirebase notificatorFirebase;
    private final Config config;


    @Inject
    public TaskBipes(NotificatorFirebase notificatorFirebase, DataBaseSQL storage, BipeService bipeService, Config config) {
        this.notificatorFirebase = notificatorFirebase;
        this.storage = storage;
        this.bipeService = bipeService;
        this.config = config;
    }


    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, CHECK_PERIOD_MINUTES, CHECK_PERIOD_MINUTES, TimeUnit.MINUTES);
    }


    @Override
    public void run() {
        System.out.println("Checando bipes");

        long currentTime = System.currentTimeMillis();
        long checkPeriod = TimeUnit.MINUTES.toMillis(CHECK_PERIOD_MINUTES);


            try {
                List<Bipe> bipes = storage.getBipes();

                if (!bipes.isEmpty()) {
                    bipes.forEach(it -> {

                        if(it.getStatus() == StatusBipe.NAO_CONFIRMADO) {
                            if (it.getNotificacoes() <= 1) {
                                try {
                                    User userOriggem = storage.getObject(User.class, new Request(
                                            new Columns.All(),
                                            new Condition.Equals("id", it.getUserOrigem())));
                                    User userDestino = storage.getObject(User.class, new Request(
                                            new Columns.All(),
                                            new Condition.Equals("id", it.getUserDestino())));

                                    notificatorFirebase.sendDirect(
                                            userOriggem,
                                            new Mensagem(
                                                    "Bipe não confirmado",
                                                    userDestino.getName() + " não confirmou o ultimo bipe."));
                                    it.incrementNotificacoes();

                                } catch (StorageException e) {
                                    throw new RuntimeException(e);
                                } catch (MessageException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        bipeService.updateBipe(it);
                    });


                }

                System.out.println(bipes.size());
            } catch (StorageException e) {
                LOGGER.warn("Bipe error", e);

            }

    }


}
