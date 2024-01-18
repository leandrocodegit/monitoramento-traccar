/*
 * Copyright 2016 - 2017 Anton Tananaev (anton@traccar.org)
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
package org.traccar.api.resource;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.traccar.api.BaseObjectResource;
import org.traccar.config.Config;
import org.traccar.model.ManagedUser;
import org.traccar.model.User;
import org.traccar.model.Rotina;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.util.Collection;

@Path("veiculos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VeiculoResource extends BaseObjectResource<Rotina> {


    @Inject
    private Config config;

    @Context
    private HttpServletRequest request;

    public VeiculoResource() {
        super(Rotina.class);
    }

    @GET
    public Collection<Rotina> get() throws StorageException {
    if (permissionsService.notAdmin(getUserId())) {
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()));
        } else {
            return storage.getObjects(baseClass, new Request(new Columns.All()));
        }
    }

}
