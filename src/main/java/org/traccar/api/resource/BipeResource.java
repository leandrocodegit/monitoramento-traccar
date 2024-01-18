/*
 * Copyright 2017 - 2022 Anton Tananaev (anton@traccar.org)
 * Copyright 2017 - 2018 Andrey Kunitsyn (andrey@traccar.org)
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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.traccar.api.BaseObjectResource;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.api.enuns.StatusBipe;
import org.traccar.api.services.BipeService;
import org.traccar.handler.ComputedAttributesHandler;
import org.traccar.model.Attribute;
import org.traccar.model.Bipe;
import org.traccar.model.Device;
import org.traccar.model.User;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Path("bipe")
@Consumes(MediaType.APPLICATION_JSON)
public class BipeResource extends BaseObjectResource<Bipe> {

    @Inject
    private BipeService bipeService;

    public BipeResource()  { super(Bipe.class); }

    @Path("{id}")
    @POST
    public Response confirmarBipe(Bipe bipe) throws StorageException, IOException {

        Bipe bipeDB = storage.getObject(Bipe.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.Equals("id", bipe.getId()),
                        new Condition.Equals("userDestino", bipe.getUserDestino()))));

        if (bipeDB != null) {
                bipeDB.setStatus(StatusBipe.CONFIRMADO);
                bipeService.updateStatusBipe(bipeDB);
        }
        return Response.status(Response.Status.OK).build();
    }


}
