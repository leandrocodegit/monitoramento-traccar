package org.traccar.api.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.traccar.api.enuns.StatusBipe;
import org.traccar.model.Bipe;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Singleton
public class BipeService {

    private final Storage storage;

    @Inject
    public BipeService(Storage storage) {
        this.storage = storage;
    }

    public void updateBipe(Bipe bipe){

        try {
            if(bipe.getStatus() == StatusBipe.CONFIRMADO){
                bipe.setNotificacoes(0);
                bipe.setExpiracao(convertLocalDateTimeToDate(LocalDateTime.now().plusMinutes(bipe.getTime())));
                bipe.setStatus(StatusBipe.PROCESSADO);
            }
            else{
                bipe.setStatus(StatusBipe.NAO_CONFIRMADO);
            }

            storage.updateObject(bipe, new Request(
                    new Columns.Include("expiracao", "status", "notificacoes"),
                    new Condition.Equals("id", bipe.getId())));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatusBipe(Bipe bipe){

        try {
            if(bipe.getStatus() == StatusBipe.CONFIRMADO){
                bipe.setExpiracao(convertLocalDateTimeToDate(LocalDateTime.now().plusMinutes(bipe.getTime())));
             }

            storage.updateObject(bipe, new Request(
                    new Columns.Include("expiracao", "status"),
                    new Condition.Equals("id", bipe.getId())));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    private static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
