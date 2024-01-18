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
import net.fortuna.ical4j.model.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.broadcast.BroadcastInterface;
import org.traccar.broadcast.BroadcastMessage;
import org.traccar.broadcast.BroadcastService;
import org.traccar.config.Config;
import org.traccar.database.DeviceLookupService;
import org.traccar.database.NotificationManager;
import org.traccar.model.*;
import org.traccar.notification.MessageException;
import org.traccar.notificators.NotificatorFirebase;
import org.traccar.session.ConnectionManager;
import org.traccar.session.cache.CacheManager;
import org.traccar.storage.DataBaseSQL;
import org.traccar.storage.DatabaseStorage;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskRotinas implements ScheduleTask, BroadcastInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRotinas.class);

    public static final String ATTRIBUTE_DEVICE_INACTIVITY_START = "deviceInactivityStart";
    public static final String ATTRIBUTE_DEVICE_INACTIVITY_PERIOD = "deviceInactivityPeriod";
    public static final String ATTRIBUTE_LAST_UPDATE = "lastUpdate";

    private static final long CHECK_PERIOD_MINUTES = 1;

    private final DataBaseSQL storage;
    private final ConnectionManager notificationManager;

    private final NotificatorFirebase notificatorFirebase;

    private final Config config;
    private final CacheManager cacheManager;

    private final Timer timer;
    private final BroadcastService broadcastService;
    private final DeviceLookupService deviceLookupService;

    private final Map<Long, Set<ConnectionManager.UpdateListener>> listeners = new HashMap<>();
    private final Map<Long, Set<Long>> userDevices = new HashMap<>();
    private final Map<Long, Set<Long>> deviceUsers = new HashMap<>();

    private final Map<Long, Timeout> timeouts = new ConcurrentHashMap<>();

    @Inject
    public TaskRotinas(NotificatorFirebase notificatorFirebase, DataBaseSQL storage, ConnectionManager notificationManager, Config config, CacheManager cacheManager, Timer timer, BroadcastService broadcastService, DeviceLookupService deviceLookupService) {
        this.notificatorFirebase = notificatorFirebase;
        this.storage = storage;
        this.notificationManager = notificationManager;
        this.config = config;
        this.cacheManager = cacheManager;
        this.timer = timer;
        this.broadcastService = broadcastService;
        this.deviceLookupService = deviceLookupService;
    }


    @Override
    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, CHECK_PERIOD_MINUTES, CHECK_PERIOD_MINUTES, TimeUnit.MINUTES);
    }


    @Override
    public void run() {
        System.out.println("Checando rotinas");

        long currentTime = System.currentTimeMillis();
        long checkPeriod = TimeUnit.MINUTES.toMillis(CHECK_PERIOD_MINUTES);


        Map<Event, Position> events = new HashMap<>();


        try {



            // broadcastService.updateDevice(true, devices);

            try {
                List<Rotina> rotinas = storage.getRotinas();

                System.out.println(rotinas.size());

                User user = storage.getObject(User.class, new Request(
                        new Columns.All(),
                        new Condition.Equals("id", 1)));

            //   notificatorFirebase.send(new Notification(), user, new Event(), new Position());


                if (!rotinas.isEmpty()) {
                    rotinas.forEach(it -> {
                        try {
                            Event evt = new Event();

                            evt.setEventTime(new Date());
                            evt.setDeviceId(1);
                            evt.setType("rotinaIncompleta");
                            evt.setRotinaid(it.getId());

                            notificationManager.updateEvent(false, 1, evt);

                            storage.addObject(evt, new Request(new Columns.All()));

                            Device deviceDestino = storage.getObject(Device.class, new Request(
                                    new Columns.All(),
                                    new Condition.Equals("id", it.getUserId())));

                            var notifications =  storage.getNotificacoes("rotinaIncompleta", it.getDeviceId());
                            System.out.println(notifications.size());
                            storage.getNotificacoes("rotinaIncompleta", it.getDeviceId()).forEach(push -> {
                                try {
                                    notificatorFirebase.sendDirect(
                                            push,
                                            new Mensagem(
                                                    "Rotina incompleta",
                                                    deviceDestino.getName() + " não completou sua rotina."));
                                } catch (MessageException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        } catch (StorageException e) {
                            throw new RuntimeException(e);
                        }
                    });


                }

                System.out.println(rotinas.size());
            } catch (StorageException e) {
                e.printStackTrace();
            }

            System.out.println("Enviado update");

        } catch (Exception e) {
            LOGGER.warn("Database error", e);
        }

    }

    private long getAttribute(Device device, Map<Long, Group> groups, String key) {
        long deviceValue = device.getLong(key);
        if (deviceValue > 0) {
            return deviceValue;
        } else {
            long groupId = device.getGroupId();
            while (groupId > 0) {
                Group group = groups.get(groupId);
                if (group == null) {
                    return 0;
                }
                long groupValue = group.getLong(key);
                if (groupValue > 0) {
                    return groupValue;
                }
                groupId = group.getGroupId();
            }
            return 0;
        }
    }

    private boolean checkDevice(Device device, Map<Long, Group> groups, long currentTime, long checkPeriod) {
        long deviceInactivityStart = getAttribute(device, groups, ATTRIBUTE_DEVICE_INACTIVITY_START);
        if (deviceInactivityStart > 0) {
            long timeThreshold = device.getLastUpdate().getTime() + deviceInactivityStart;
            if (currentTime >= timeThreshold) {

                if (currentTime - checkPeriod < timeThreshold) {
                    return true;
                }

                long deviceInactivityPeriod = getAttribute(device, groups, ATTRIBUTE_DEVICE_INACTIVITY_PERIOD);
                if (deviceInactivityPeriod > 0) {
                    long count = (currentTime - timeThreshold - 1) / deviceInactivityPeriod;
                    timeThreshold += count * deviceInactivityPeriod;
                    return currentTime - checkPeriod < timeThreshold;
                }

            }
        }
        return false;
    }

}
